package ru.kamaz.music.view_models

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
import ru.kamaz.music.R
import ru.kamaz.music.services.MusicService
import ru.kamaz.music.services.MusicServiceInterface
import ru.kamaz.music.ui.fragments.MainListMusicFragment
import ru.kamaz.music.ui.producers.ItemType
import ru.kamaz.music.ui.producers.ItemType.RV_ITEM_MUSIC_FAVORITE
import ru.kamaz.music.view_models.list.FolderViewModel
import ru.kamaz.music_api.interactor.*
import ru.kamaz.music_api.models.*
import ru.sir.core.None
import ru.sir.presentation.base.BaseViewModel
import ru.sir.presentation.base.recycler_view.RecyclerViewBaseDataModel
import ru.sir.presentation.extensions.launchOn
import java.io.File
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class MainListMusicViewModel @Inject constructor(
    application: Application,
    private val categoryData: CategoryLoadRV,
    private val loadAllMusic: LoadDiskData,
    private val rvFavorite: FavoriteMusicRV,
    private val rvPlayList: PlayListRV,
    private val insertFavoriteMusic: InsertFavoriteMusic,
    private val deleteFavoriteMusic: DeleteFavoriteMusic,
    private val rvAllFolderWithMusic: AllFolderWithMusicRV
) : BaseViewModel(application), ServiceConnection, MusicServiceInterface.ViewModel {

    companion object {
        const val RV_ITEM_MUSIC_FAVORITE = 4
        const val RV_ITEM = 5
        const val RV_ITEM_MUSIC_CATEGORY = 6
        const val RV_ITEM_MUSIC_FOLDER = 7
        const val RV_ITEM_MUSIC_FOLDER_PLAYLIST = 9
    }

    private val _allMusic = MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    val allMusic = _allMusic

    private val _service = MutableStateFlow<MusicServiceInterface.Service?>(null)
    val service = _service.asStateFlow()

    val lastMusicChanged: StateFlow<String> by lazy {
        service.value?.lastMusic() ?: MutableStateFlow("")
    }

    init {
        categoryData(None()) { it.either({}, ::onCategoryLoaded) }
        rvAllFolderWithMusic(None()) { it.either({}, ::onAllFolderWithMusic) }
        val intent = Intent(context, MusicService::class.java)
        context.bindService(intent, this, Context.BIND_AUTO_CREATE)
        rvFavorite(None()) {it.either({}, ::loadFavoriteTracks)}
    }

    //////////////////////////////////////////////
    //////////////////////////////////////////////
    //////////////////////////////////////////////

    // TrackList

    val rvPosition = MutableStateFlow(0)

    private val _loadingMusic = MutableStateFlow<List<Track>>(emptyList())
    val loadingMusic = _loadingMusic

    private val _searchedMusic = MutableStateFlow<List<Track>>(emptyList())
    val searchedMusic = _searchedMusic

    private val _playlistIsEmpty = MutableStateFlow(false)
    val playlistIsEmpty = _playlistIsEmpty.asStateFlow()

    private val _lastMusic = MutableStateFlow("")
    val lastMusic = _lastMusic

    private fun loadDiskPlaylist(mode: String) {
        Log.i("ReviewTest", "loadDiskPlaylist: $mode ")
        loadAllMusic(mode) { it.either({ }, ::onDiskDataLoaded) }
        if (mode != "all") loadPlayListInCoroutine("all")
    }

    private fun loadPlayListInCoroutine(loadMode : String){
        CoroutineScope(Dispatchers.IO).launch {
            Thread.sleep(5000)
            loadDiskPlaylist(loadMode)
        }
    }

    private fun onDiskDataLoaded(data: List<Track>) {
        _playlistIsEmpty.value = data.isEmpty()
        _loadingMusic.value = data
        _searchedMusic.value = data
        convertToRecyclerViewItems(data as ArrayList<Track>)
    }

    private fun List<Track>.findPlayingMusic(title: String): List<Track> {
        for (i in this.indices) {
            if (title != "")  this[i].playing = this[i].id == title.toLong()
        }
        return this
    }


    private fun List<Track>.findFavoriteMusic(): List<Track> {
        CoroutineScope(Dispatchers.IO).launch {
            val it = rvFavorite.run(None())
            it.either({
            }, {
                (if (!it.isEmpty()) {
                    this@findFavoriteMusic.forEach{ tracks ->
                        it.forEach{ list ->
                            if (tracks.title == list.title && tracks.data == list.data){
                                tracks.favorite = true
                            }
                        }
                    }
                })
            })
        }
        return this
    }

    private fun convertToRecyclerViewItems(listTrack: ArrayList<Track>) {
        _allMusic.value = listTrack.findFavoriteMusic().findPlayingMusic(lastMusic.value).toRecyclerViewItemOfList(RV_ITEM)
    }

    fun lastMusic(title: String) {
        _lastMusic.value = title
        if (allMusic.value.isEmpty()) {
            loadDiskPlaylist("5")
        } else {
            onDiskDataLoaded(searchedMusic.value)
        }
    }

    fun onItemClick(track: Track, data: String) {
        service.value?.intMediaPlayer()
        if (track.id.toString() != lastMusic.value) {
            service.value?.initTrack(track, data)
            service.value?.resume()
            _lastMusic.value = track.id.toString()
        } else {
            service.value?.playOrPause()
        }
    }

    fun onLikeClicked(track: Track) {
        CoroutineScope(Dispatchers.IO).launch {
            if (track.favorite) {
                deleteFavoriteMusic.run(
                    DeleteFavoriteMusic.Params(
                        track
                    )
                )
            } else {
                insertFavoriteMusic.run(
                    InsertFavoriteMusic.Params(
                        track
                    )
                )
            }
        }
    }

    ////////////////////////////////////////////
    ///////////////////////////////////////////
    ////////////////////////////////////////////

    //CategoryMusic

    private val _categoryOfMusic = MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    var categoryOfMusic = _categoryOfMusic.asStateFlow()

    val _categoryList = MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    val categoryList = _categoryList.asStateFlow()

    private val _favoriteSongs = MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    val favoriteSongs = _favoriteSongs.asStateFlow()

    private val _listPlayList = MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    val listPlayList = _listPlayList.asStateFlow()

    fun loadFavoriteTracks(data: List<Track>){
        _favoriteSongs.value = data.toRecyclerViewItemOfList(RV_ITEM_MUSIC_FAVORITE)
    }

    fun getCategoryList(id: Int){
        _categoryList.value = loadingMusic.value.toRecyclerViewItemOfList(id)
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

    fun getPlayLists(){
        val listPlayList = ArrayList<PlayListModel>()
        listPlayList.add(PlayListModel(9999L, "Создать", "create_playlist", listOf(""), listOf("")))
        val scope = CoroutineScope(Job() + Dispatchers.IO)
        val job = scope.launch {
            val it = rvPlayList.run(None())
            it.either({

            },
                {
                    listPlayList.addAll(it)
                })
        }
        job.start()
        if (job.isCompleted){
            _listPlayList.value = listPlayList.toRecyclerViewItemsPlayList()
        }
    }

    private fun List<Track>.toRecyclerViewItemOfList(id: Int): List<RecyclerViewBaseDataModel> {
        var listType = MainListMusicFragment.RV_ITEM
        when (id) {
            0 -> listType = MainListMusicFragment.RV_ITEM_MUSIC_ARTIST
            1 -> listType = MainListMusicFragment.RV_ITEM_MUSIC_GENRES
            2 -> listType = MainListMusicFragment.RV_ITEM_MUSIC_ALBUMS
            3 -> listType = MainListMusicFragment.RV_ITEM_MUSIC_PLAYLIST
            4 -> listType = MainListMusicFragment.RV_ITEM_MUSIC_FAVORITE
            5 -> listType = MainListMusicFragment.RV_ITEM
            6 -> listType = MainListMusicFragment.RV_ITEM_MUSIC_CATEGORY
            7 -> listType = MainListMusicFragment.RV_ITEM_MUSIC_FOLDER
            8 -> listType = MainListMusicFragment.RV_ITEM_MUSIC_PLAYLIST_ADD_NEW
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

    val _foldersMusic = MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    val foldersMusic = _foldersMusic

    private val _folderMusicPlaylist = MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    val folderMusicPlaylist = _folderMusicPlaylist.asStateFlow()

    private fun onAllFolderWithMusic(folderMusic: List<AllFolderWithMusic>){
        _foldersMusic.value = folderMusic.toRecyclerViewItemsFolder()
    }
    private fun List<AllFolderWithMusic>.toRecyclerViewItemsFolder(): List<RecyclerViewBaseDataModel> {
        val newList = mutableListOf<RecyclerViewBaseDataModel>()
        this.forEach { newList.add(RecyclerViewBaseDataModel(it,
            RV_ITEM_MUSIC_FOLDER
        )) }
        return newList
    }

    fun fillFolderPlaylist(data: String){
        val trackList = ArrayList<Track>()
        searchedMusic.value.forEach {
            if (it.data.contains(data + File.separator + it.title.replace(" ", "_"))){
                trackList.add(it)
            }
        }
        _folderMusicPlaylist.value = trackList.toRecyclerViewItemOfList(RV_ITEM)
    }





    ///////////////////////////////////////////
    ///////////////////////////////////////////
    //////////////////////////////////////////

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
    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        _service.value = (service as MusicService.MyBinder).getService()
        this.service.value?.setViewModel(this)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        _service.value = null
    }
}