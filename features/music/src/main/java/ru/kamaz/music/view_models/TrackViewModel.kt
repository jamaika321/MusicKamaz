package ru.kamaz.music.view_models

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import ru.biozzlab.twmanager.domain.interfaces.MusicManagerListener
import ru.kamaz.music.services.MusicService
import ru.kamaz.music.services.MusicServiceInterface
import ru.kamaz.music_api.domain.GetFilesUseCase
import ru.kamaz.music_api.interactor.*
import ru.kamaz.music_api.models.FavoriteSongs
import ru.kamaz.music_api.models.Track
import ru.sir.core.Either
import ru.sir.core.None
import ru.sir.presentation.base.BaseViewModel
import ru.sir.presentation.base.recycler_view.RecyclerViewBaseDataModel
import ru.sir.presentation.extensions.launchOn
import java.util.*
import javax.inject.Inject
import kotlin.coroutines.coroutineContext

class TrackViewModel @Inject constructor(
    application: Application,
    private val loadDiskData: LoadDiskData,
    private val loadUsbData: LoadUsbData,
    private val rvFavorite: FavoriteMusicRV,
    private val insertFavoriteMusic: InsertFavoriteMusic,
    private val deleteFavoriteMusic: DeleteFavoriteMusic
) : BaseViewModel(application), ServiceConnection, MusicServiceInterface.ViewModel,
    MusicManagerListener {

    companion object {
        private const val RV_ITEM = 2
    }

    private val _service = MutableStateFlow<MusicServiceInterface.Service?>(null)
    val service = _service.asStateFlow()

    private val _itemsAll = MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    var itemsAll = _itemsAll.asStateFlow()

    private val _trackIsEmpty = MutableStateFlow(false)
    val trackIsEmpty = _trackIsEmpty.asStateFlow()

    val isUsbModeOn: StateFlow<Boolean> by lazy {
        service.value?.usbModeOn() ?: MutableStateFlow(false)
    }

    val _rvPosition = MutableStateFlow(0)
    val rvPosition = _rvPosition

    private val _sourceEnum = MutableStateFlow("DISK")
    var sourceEnum = _sourceEnum.asStateFlow()

    private val _lastMusic = MutableStateFlow("")
    val lastMusic = _lastMusic

    val lastMusicChanged: StateFlow<String> by lazy {
        service.value?.lastMusic() ?: MutableStateFlow("")
    }

    lateinit var listAllTrack: ArrayList<Track>
    lateinit var changedListTrack: ArrayList<Track>

    override fun init() {
        val intent = Intent(context, MusicService::class.java)
        context.bindService(intent, this, Context.BIND_AUTO_CREATE)
        Log.i("TrackFragmentView", "init: ")
    }

    fun lastMusic(title: String) {
        _lastMusic.value = title
        if (itemsAll.value.isEmpty()) {
            loadDiskPlaylist("5")
        } else {
            convertToRecyclerViewItems(changedListTrack)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun filterRecyclerList(constraint: String, listTrack: ArrayList<Track>): ArrayList<Track> {
        val musicFilterList: ArrayList<Track>
        if (constraint.isEmpty()) {
            musicFilterList = listTrack
        } else {
            val resultList = ArrayList<Track>()
            for (row in listTrack) {
                if (row.title.lowercase(Locale.ROOT).contains(constraint.lowercase(Locale.ROOT))) {
                    resultList.add(row)
                }
            }
            musicFilterList = resultList
        }
        changedListTrack = musicFilterList
        return musicFilterList
    }

    fun searchMusic(music: String) {
        convertToRecyclerViewItems(filterRecyclerList(music, listAllTrack))
    }

    fun loadDiskPlaylist(mode: String) {
        Log.i("ReviewTest", "loadDiskPlaylist: ")
        loadDiskData(mode) { it.either({ }, ::onDiskDataLoaded) }
        if (mode != "all") loadPlayListInCoroutine()
    }

    private fun loadPlayListInCoroutine() {
        CoroutineScope(Dispatchers.IO).launch {
            Thread.sleep(2000)
            loadDiskPlaylist("all")
        }
    }


    fun changeSource(sourceEnum: String) {
        _sourceEnum.value = sourceEnum
    }

    private fun convertToRecyclerViewItems(listTrack: ArrayList<Track>) {
        _itemsAll.value = listTrack.findFavoriteMusic().findPlayingMusic(lastMusic.value).toRecyclerViewItems()
    }

    private fun onDiskDataLoaded(data: List<Track>) {
        if (data.isEmpty()) {
            Log.d("ReviewTest", "onDiskDataLoaded")
            _trackIsEmpty.value = true
        } else {
            _trackIsEmpty.value = false
        }
        listAllTrack = data as ArrayList<Track>
        changedListTrack = data
        convertToRecyclerViewItems(data)
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

    fun onItemClick(track: Track, data: String) {
//        service.value?.intMediaPlayer()
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

    private fun List<Track>.toRecyclerViewItems(): List<RecyclerViewBaseDataModel> {
        Log.i("ReviewTest", "toRecyclerViewItems: ")
        val newList = mutableListOf<RecyclerViewBaseDataModel>()
        this.forEach { newList.add(RecyclerViewBaseDataModel(it, RV_ITEM)) }
        return newList
    }


    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        Log.d("testPlayTrack", "onServiceConnected")
        _service.value = (service as MusicService.MyBinder).getService()
        this.service.value?.setViewModel(this)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        _service.value = null
    }

    override fun addListener() {
        TODO("Not yet implemented")
    }

    override fun removeListener() {
        TODO("Not yet implemented")
    }

    override fun onCheckPosition(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onUpdateSeekBar(duration: Int) {

    }

    override fun selectBtMode() {
        TODO("Not yet implemented")
    }
}