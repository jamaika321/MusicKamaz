package ru.kamaz.music.view_models.fragments

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import ru.kamaz.music.R
import ru.kamaz.music.services.MusicService
import ru.kamaz.music.services.MusicServiceInterface
import ru.kamaz.music.ui.fragments.MainListMusicFragment
import ru.kamaz.music.ui.producers.ItemType
import ru.kamaz.music_api.interactor.*
import ru.kamaz.music_api.models.*
import ru.sir.core.None
import ru.sir.presentation.base.BaseViewModel
import ru.sir.presentation.base.recycler_view.RecyclerViewBaseDataModel
import java.io.File
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class MainListMusicViewModel @Inject constructor(
    application: Application,
    private val categoryData: CategoryLoadRV,
    private val rvFavorite: FavoriteMusicRV,
    private val insertPlayList: InsertPlayList,
    private val deletePlayList: DeletePlayList,
) : BaseViewModel(application), ServiceConnection, MusicServiceInterface.ViewModel {

    companion object {
        const val RV_ITEM_MUSIC_FAVORITE = 4
        const val RV_ITEM = 5
        const val RV_ITEM_MUSIC_CATEGORY = 6
        const val RV_ITEM_MUSIC_FOLDER = 7
    }

    private val _allMusic = MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    val allMusic = _allMusic

    private val _service = MutableStateFlow<MusicServiceInterface.Service?>(null)
    val service = _service.asStateFlow()

    private val _foldersMusic = MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    val foldersMusic = _foldersMusic

    private val _folderMusicPlaylist =
        MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    val folderMusicPlaylist = _folderMusicPlaylist.asStateFlow()

    private val _categoryOfMusic = MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    var categoryOfMusic = _categoryOfMusic.asStateFlow()

    private val _categoryList = MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    val categoryList = _categoryList.asStateFlow()

    private val _favoriteSongs = MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    val favoriteSongs = _favoriteSongs.asStateFlow()

    private val _listPlayList = MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    val listPlayList = _listPlayList.asStateFlow()

    private val _playListMusic = MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    val playListMusic = _playListMusic.asStateFlow()

    val lastMusicChanged: StateFlow<String> by lazy {
        service.value?.lastMusic() ?: MutableStateFlow("")
    }

    val serviceTracks: StateFlow<List<Track>> by lazy {
        service.value?.getAllTracks() ?: MutableStateFlow(emptyList())
    }

    val playLists: StateFlow<List<PlayListModel>> by lazy {
        service.value?.getPlayLists() ?: MutableStateFlow(emptyList())
    }

    val foldersList: StateFlow<List<AllFolderWithMusic>> by lazy {
        service.value?.getFoldersList() ?: MutableStateFlow(emptyList())
    }

    init {
        val intent = Intent(context, MusicService::class.java)
        context.bindService(intent, this, Context.BIND_AUTO_CREATE)
        testPlayList()
    }

    private fun testPlayList() {
        CoroutineScope(Dispatchers.IO).launch {
            insertPlayList.run(
                InsertPlayList.Params(
                    PlayListModel(
                        0L,
                        context.resources!!.getString(R.string.create_playlist),
                        "create_playlist",
                        arrayListOf("")
                    )
                )
            )
        }
    }

    fun loadAllDBLists() {
        categoryData(None()) { it.either({}, ::onCategoryLoaded) }
        getFoldersList()
        getFavoriteTracks()
        getPlayLists()
    }

    //////////////////////////////////////////////
    //////////////////////////////////////////////
    //////////////////////////////////////////////

    // TrackList

    val rvPosition = MutableStateFlow(0)
    val rvScrollState = MutableStateFlow(0)

    private val _playlistIsEmpty = MutableStateFlow(false)
    val playlistIsEmpty = _playlistIsEmpty.asStateFlow()

    private val _lastMusic = MutableStateFlow("")
    val lastMusic = _lastMusic

    private fun List<Track>.findPlayingMusic(data: String): List<Track> {
        for (i in this.indices) {
            if (data != "") this[i].playing = this[i].data == data
        }
        return this
    }


    private fun List<Track>.findFavoriteMusic(): List<Track> {
        this.forEach { tracks ->
            viewModelScope.launch {
                rvFavorite.run(None()).collect {
                    it.forEach { favorites ->
                        if (tracks.data == favorites.data) tracks.favorite = true
                    }
                }
            }
        }
        return this
    }

    private fun getFavoriteTracks() {
        viewModelScope.launch {
            rvFavorite.run(None()).collect {
                _favoriteSongs.value =
                    it.findPlayingMusic(lastMusic.value).toRecyclerViewItemOfList(
                        RV_ITEM
                    )
            }
        }
    }

    fun fillAllTracksList() {
        _allMusic.value = serviceTracks.value.findFavoriteMusic().findPlayingMusic(lastMusic.value)
            .toRecyclerViewItemOfList(
                RV_ITEM
            )
    }

    fun lastMusic(data: String, mode: MainListMusicFragment.ListState) {
        _lastMusic.value = data
        when (mode) {
            MainListMusicFragment.ListState.PLAYLIST -> {
                fillAllTracksList()
            }
            MainListMusicFragment.ListState.CATPLAYLIST -> {

            }
            MainListMusicFragment.ListState.FOLDPLAYLIST -> {

            }
            MainListMusicFragment.ListState.CATFAVORITES -> {

            }
        }
    }

    fun onItemClick(track: Track, data: String, source: PlayListSource) {
        if (track.data != lastMusic.value) {
            service.value?.initPlayListSource(track, source)
            service.value?.initTrack(track, data)
            service.value?.resume()
        } else {
            service.value?.playOrPause()
        }
    }

    fun deletePlaylist(name: String) {
        CoroutineScope(Dispatchers.IO).launch {
            deletePlayList.run(DeletePlayList.Params(name))
        }
    }

    fun onLikeClicked(track: Track) {
        service.value?.insertFavoriteMusic(track.data)
    }

    ////////////////////////////////////////////
    ///////////////////////////////////////////
    ////////////////////////////////////////////

    //CategoryMusic

    val activePlayListName = MutableStateFlow("")
    val activeFolderName = MutableStateFlow("")

    fun getCategoryList(id: Int) {
        _categoryList.value = serviceTracks.value.toRecyclerViewItemOfList(id)
    }

    private fun onCategoryLoaded(category: List<CategoryMusicModel>) {
        _categoryOfMusic.value = category.toRecyclerViewItemsCategory()
    }

    private fun List<CategoryMusicModel>.toRecyclerViewItemsCategory(): List<RecyclerViewBaseDataModel> {
        val newList = mutableListOf<RecyclerViewBaseDataModel>()
        this.forEach { newList.add(RecyclerViewBaseDataModel(it, RV_ITEM_MUSIC_CATEGORY)) }
        return newList
    }

    private fun List<PlayListModel>.toRecyclerViewItemsPlayList(): List<RecyclerViewBaseDataModel> {
        val newList = mutableListOf<RecyclerViewBaseDataModel>()
        this.forEach { newList.add(RecyclerViewBaseDataModel(it, ItemType.RV_ITEM_MUSIC_PLAYLIST)) }
        return newList
    }

    fun getFoldersList() {
        _foldersMusic.value = foldersList.value.toRecyclerViewItemsFolder()
    }

    fun getPlayLists() {
        _listPlayList.value = playLists.value.toRecyclerViewItemsPlayList()
    }

    fun getPlayListMusic(trackList: List<Track>) {
        _playListMusic.value = trackList.toRecyclerViewItemOfList(RV_ITEM)
    }

    private fun List<Track>.toRecyclerViewItemOfList(id: Int): List<RecyclerViewBaseDataModel> {
        var listType = MainListMusicFragment.RV_ITEM
        when (id) {
            0 -> listType = MainListMusicFragment.RV_ITEM_MUSIC_ARTIST
            2 -> listType = MainListMusicFragment.RV_ITEM_MUSIC_ALBUMS
            3 -> listType = MainListMusicFragment.RV_ITEM_MUSIC_PLAYLIST
            4 -> listType = MainListMusicFragment.RV_ITEM_MUSIC_FAVORITE
            5 -> listType = MainListMusicFragment.RV_ITEM
            6 -> listType = MainListMusicFragment.RV_ITEM_MUSIC_CATEGORY
            7 -> listType = MainListMusicFragment.RV_ITEM_MUSIC_FOLDER
            8 -> listType = MainListMusicFragment.RV_ITEM_PLAYLIST
        }
        val newList = mutableListOf<RecyclerViewBaseDataModel>()
        this.forEach {
            newList.add(
                RecyclerViewBaseDataModel(
                    it,
                    listType
                )
            )
        }
        return newList
    }


    /////////////////////////////////////////
    //////////////////////////////////////////
    ///////////////////////////////////////////

    //FolderMusic

    private fun List<AllFolderWithMusic>.toRecyclerViewItemsFolder(): List<RecyclerViewBaseDataModel> {
        val newList = mutableListOf<RecyclerViewBaseDataModel>()
        this.forEach {
            newList.add(
                RecyclerViewBaseDataModel(
                    it,
                    RV_ITEM_MUSIC_FOLDER
                )
            )
        }
        return newList
    }

    fun fillFolderPlaylist(data: String) {
        val trackList = ArrayList<Track>()
        serviceTracks.value.forEach {
            if (it.data.contains(data + File.separator + it.title.replace(" ", "_").replace(",", "_").replace("-", "_").replace("'", "_"))) {
                trackList.add(it)
            }
        }
        _folderMusicPlaylist.value = trackList.toRecyclerViewItemOfList(RV_ITEM)
    }

    ///////////////////////////////////////////
    ///////////////////////////////////////////
    //////////////////////////////////////////

    fun searchMusic(music: String) {
        _allMusic.value = filterRecyclerList(music, serviceTracks.value).toRecyclerViewItemOfList(
            RV_ITEM
        )
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
        return musicFilterList
    }

    override fun init() {
        val intent = Intent(context, MusicService::class.java)
        context.bindService(intent, this, Context.BIND_AUTO_CREATE)

    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        _service.value = (service as MusicService.MyBinder).getService()
        this.service.value?.setViewModel(this)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        _service.value = null
    }
}