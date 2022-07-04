package ru.kamaz.music.view_models

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.viewModelScope
import com.eckom.xtlibrary.twproject.music.presenter.MusicPresenter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.kamaz.music.services.MusicService
import ru.kamaz.music.services.MusicServiceInterface
import ru.kamaz.music_api.interactor.*
import ru.kamaz.music_api.models.AllFolderWithMusic
import ru.kamaz.music_api.models.CategoryMusicModel
import ru.kamaz.music_api.models.FavoriteSongs
import ru.kamaz.music_api.models.Track
import ru.sir.core.None
import ru.sir.presentation.base.BaseViewModel
import ru.sir.presentation.base.recycler_view.RecyclerViewBaseDataModel
import ru.sir.presentation.extensions.launchOn
import java.util.*
import javax.inject.Inject

class MainListMusicViewModel @Inject constructor(
    application: Application,
    private val loadAllMusic: LoadDiskData,
    private val rvFavorite: FavoriteMusicRV,
    private val insertFavoriteMusic: InsertFavoriteMusic,
    private val deleteFavoriteMusic: DeleteFavoriteMusic
) : BaseViewModel(application), ServiceConnection, MusicServiceInterface.ViewModel {

    companion object {
        private const val RV_ITEM = 2
        private const val RV_ITEM_MUSIC_CATEGORY = 3
        private const val RV_ITEM_MUSIC_FOLDER = 4
    }

    private val _allMusic = MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    val allMusic = _allMusic

    val lastMusicChanged: StateFlow<String> by lazy {
        service.value?.lastMusic() ?: MutableStateFlow("")
    }

    //////////////////////////////////////////////
    //////////////////////////////////////////////
    //////////////////////////////////////////////

    // TrackList

    private val _loadingMusic = MutableStateFlow<List<Track>>(emptyList())
    val loadingMusic = _loadingMusic

    private val _searchedMusic = MutableStateFlow<List<Track>>(emptyList())
    val searchedMusic = _searchedMusic

    private val _playlistIsEmpty = MutableStateFlow(false)
    val playlistIsEmpty = _playlistIsEmpty.asStateFlow()

    private val _lastMusic = MutableStateFlow("")
    val lastMusic = _lastMusic

    private fun loadDiskPlaylist(mode: String) {
        Log.i("ReviewTest", "loadDiskPlaylist: ")
        loadAllMusic(mode) { it.either({ }, ::onDiskDataLoaded) }
        if (mode != "all") loadPlayListInCoroutine()
    }

    private fun loadPlayListInCoroutine() {
        CoroutineScope(Dispatchers.IO).launch {
            Thread.sleep(2000)
            loadDiskPlaylist("all")
        }
    }

    private fun onDiskDataLoaded(data: List<Track>) {
        if (data.isEmpty()) {
            Log.d("ReviewTest", "onDiskDataLoaded")
            _playlistIsEmpty.value = true
        } else {
            _playlistIsEmpty.value = false
        }
        _loadingMusic.value = data
        _searchedMusic.value = data
        convertToRecyclerViewItems(data as ArrayList<Track>)
    }

    private fun List<Track>.findPlayingMusic(title: String): List<Track> {
        for (i in this.indices) {
            this[i].playing = this[i].id == title.toLong()
        }
        return this
    }

    private fun List<Track>.findFavoriteMusic(): List<Track> {
        rvFavorite.run(None()).launchOn(viewModelScope) {
            if (it.isNotEmpty()) {
                for (track in this.indices) {
                    for (i in it.indices) {
                        if (this[track].title == it[i].title) {
                            this[track].favorite = true
                        }
                    }
                }
            }
        }
        return this
    }

    private fun convertToRecyclerViewItems(listTrack: ArrayList<Track>) {
        _allMusic.value = listTrack.findFavoriteMusic().findPlayingMusic(lastMusic.value).toRecyclerViewItems()
    }

    fun lastMusic(title: String) {
        _lastMusic.value = title
        if (allMusic.value.isEmpty()) {
            loadDiskPlaylist("5")
        } else {
            convertToRecyclerViewItems(searchedMusic.value as ArrayList<Track>)
        }
    }

    fun onItemClick(track: Track, data: String) {
        service.value?.intMediaPlayer()
        if (track.title != lastMusic.value) {
            service.value?.initTrack(track, data)
            service.value?.resume()
            when (track.source) {
                "disk" -> service.value?.sourceSelection(MusicService.SourceEnum.DISK)
                "usb" -> service.value?.sourceSelection(MusicService.SourceEnum.USB)
            }
        } else {
            service.value?.playOrPause()
        }
    }

    fun onLikeClicked(track: Track) {
        CoroutineScope(Dispatchers.IO).launch {
            if (track.favorite) {
                deleteFavoriteMusic.run(
                    DeleteFavoriteMusic.Params(
                        FavoriteSongs(
                            track.id.toInt(),
                            track.data,
                            track.title,
                            track.artist
                        )
                    )
                )
            } else {
                insertFavoriteMusic.run(
                    InsertFavoriteMusic.Params(
                        FavoriteSongs(
                            track.id.toInt(),
                            track.data,
                            track.title,
                            track.artist
                        )
                    )
                )
            }
        }
    }

    ////////////////////////////////////////////
    ///////////////////////////////////////////
    ////////////////////////////////////////////


    private val _service = MutableStateFlow<MusicServiceInterface.Service?>(null)
    val service = _service.asStateFlow()

    fun searchMusic(music: String) {
        convertToRecyclerViewItems(filterRecyclerList(music, loadingMusic.value))
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun filterRecyclerList(constraint: String, listTrack: List<Track>): ArrayList<Track> {
        val musicFilterList: ArrayList<Track>
        if (constraint.isEmpty()) {
            musicFilterList = listTrack as ArrayList<Track>
        } else {
            val resultList = ArrayList<Track>()
            for (row in listTrack) {
                if (row.title.lowercase(Locale.ROOT).contains(constraint.lowercase(Locale.ROOT))) {
                    resultList.add(row)
                }
            }
            musicFilterList = resultList
        }
        _searchedMusic.value = musicFilterList
        return musicFilterList
    }


    override fun init() {
        val intent = Intent(context, MusicService::class.java)
        context.bindService(intent, this, Context.BIND_AUTO_CREATE)

    }

    private fun List<Track>.toRecyclerViewItems(): List<RecyclerViewBaseDataModel> {
        val newList = mutableListOf<RecyclerViewBaseDataModel>()
        this.forEach { newList.add(RecyclerViewBaseDataModel(it, RV_ITEM)) }
        return newList
    }


    private fun List<CategoryMusicModel>.toRecyclerViewItemsCategory(): List<RecyclerViewBaseDataModel> {
        val newList = mutableListOf<RecyclerViewBaseDataModel>()
        this.forEach { newList.add(RecyclerViewBaseDataModel(it, RV_ITEM_MUSIC_CATEGORY)) }
        return newList
    }


    private fun List<AllFolderWithMusic>.toRecyclerViewItemsFolder(): List<RecyclerViewBaseDataModel> {
        val newList = mutableListOf<RecyclerViewBaseDataModel>()
        this.forEach { newList.add(RecyclerViewBaseDataModel(it, RV_ITEM_MUSIC_FOLDER)) }
        return newList
    }


    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        _service.value = (service as MusicService.MyBinder).getService()
        this.service.value?.setViewModel(this)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        _service.value = null
    }
}