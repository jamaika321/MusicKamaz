package ru.kamaz.music.view_models.fragments

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.widget.Toast
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.kamaz.music.R
import ru.kamaz.music.domain.TestSettings
import ru.kamaz.music.media.AppMediaManager
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
    private val insertPlayList: InsertPlayList,
    private val deletePlayList: DeletePlayList,
    private val updatePlayListName: UpdatePlayListName,
    private val mediaManager: AppMediaManager,
    private val testSettings: TestSettings
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

    private val _foldersLists = MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    val foldersLists = _foldersLists

    private val _folderMusicPlaylist =
        MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    val folderMusicPlaylist = _folderMusicPlaylist.asStateFlow()

    private val _artistsPlaylist =
        MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    val artistsPlaylist = _artistsPlaylist.asStateFlow()

    private val _albumsPlaylist =
        MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    val albumsPlaylist = _albumsPlaylist.asStateFlow()

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

    val usbConnected: StateFlow<Boolean> by lazy {
        service.value?.checkUSBConnection() ?: MutableStateFlow(false)
    }

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
        bisButtonListener()
    }

    private fun bisButtonListener(){
        testSettings.start {
            when (it) {
                5 -> scrollList()
                6 -> scrollList()
            }
        }
    }

    private fun scrollList(){
        serviceTracks.value.find { it.playing }.let { track ->
            if (track == null){
                serviceTracks.value.find { it.playing }.let {
                    if (it != null){
                        it.scrollPosition = true
                    }
                }
            } else {
                serviceTracks.value.forEachIndexed { index, track ->
                    if (track.scrollPosition){
                        serviceTracks.value[index].scrollPosition = false
                        if (serviceTracks.value.size != index+1){
                            serviceTracks.value[index+1].scrollPosition = true
                        } else {
                            serviceTracks.value[0].scrollPosition = true
                        }
                        return@forEachIndexed
                    }
                }
            }
        }
    }

    private fun testPlayList() {
        CoroutineScope(Dispatchers.IO).launch {
            insertPlayList.run(
                InsertPlayList.Params(
                    PlayListModel(
                        0L,
                        "create_050820221536",
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

    private val _lastMusic = MutableStateFlow("")
    val lastMusic = _lastMusic

    private fun List<Track>.findPlayingMusic(data: String): List<Track> {
        for (i in this.indices) {
            if (data != "") this[i].playing = this[i].data == data
        }
        return this
    }

    fun deleteTrackFromMemory(data: String){
        mediaManager.deleteTrackFromMemory(data)
    }

    private fun getFavoriteTracks() {
        val staticFavoriteSongs = ArrayList<Track>(emptyList())
        if (staticFavoriteSongs.isEmpty()) {
            serviceTracks.value.forEach { track ->
                if (track.favorite) staticFavoriteSongs.add(track)
            }
        }
        _favoriteSongs.value = staticFavoriteSongs.findPlayingMusic(lastMusic.value).toRecyclerViewItemOfList(RV_ITEM)
    }

    fun fillAllTracksList() {
        _allMusic.value = serviceTracks.value.findPlayingMusic(lastMusic.value)
            .toRecyclerViewItemOfList(
                RV_ITEM
            )
    }

    fun lastMusic(data: String, mode: MainListMusicFragment.ListState) {
        _lastMusic.value = data
        when (mode) {
            MainListMusicFragment.ListState.MAINPLAYLIST -> {
                fillAllTracksList()
            }
            MainListMusicFragment.ListState.CATPLAYLIST -> {
                getCategoryList(0)
            }
            MainListMusicFragment.ListState.FOLDPLAYLIST -> {
                fillFolderPlaylist(activeFolderName.value)
            }
            MainListMusicFragment.ListState.FAVORITEPLAYLIST -> {
                getFavoriteTracks()
            }
            MainListMusicFragment.ListState.PLAYLISTMUSIC -> {
                getPlayListMusic()
            }
            MainListMusicFragment.ListState.ALBUMSPLAYLIST -> {
                fillAlbumsPlayList(activeAlbumName.value)
            }
            MainListMusicFragment.ListState.ARTISTPLAYLIST -> {
                fillArtistsPlayList(activeArtistName.value)
            }
        }
    }

    fun onItemClick(track: Track, data: String, source: PlayListSource) {
        if (track.data != lastMusic.value) {
            service.value?.initPlayListSource(source)
            service.value?.initTrack(track, data)
            service.value?.resume()
        } else {
            service.value?.initPlayListSource(source)
            service.value?.playOrPause()
        }
    }

    fun deletePlaylist(name: String) {
        CoroutineScope(Dispatchers.IO).launch {
            deletePlayList.run(DeletePlayList.Params(name))
        }
    }

    fun renamePlayList(name: String, newName: String) {
        if (newName != "") {
            CoroutineScope(Dispatchers.IO).launch {
                updatePlayListName.run(UpdatePlayListName.Params(name, newName))
            }
            Toast.makeText(context, R.string.saved , Toast.LENGTH_SHORT).show()
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
    val activeArtistName = MutableStateFlow("")
    val activeAlbumName = MutableStateFlow("")

    fun getCategoryList(id: Int) {
        when (id) {
            0 -> {
                _categoryList.value = fillArtistList(serviceTracks.value).toRecyclerViewItemOfList(id)
            }
            2 -> {
                _categoryList.value = fillAlbumsList(serviceTracks.value).toRecyclerViewItemOfList(id)
            }
        }
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
        _foldersLists.value = foldersList.value.toRecyclerViewItemsFolder()
    }

    fun getPlayLists() {
        _listPlayList.value = playLists.value.toRecyclerViewItemsPlayList()
    }

    fun getPlayListMusic() {
        val name = activePlayListName.value
        val trackList = ArrayList<Track>()
        playLists.value.forEach { playList ->
            if (playList.title == name) {
                playList.trackDataList.forEach { data ->
                    serviceTracks.value.forEach { tracks ->
                        if (data == tracks.data) {
                            trackList.add(tracks)
                        }
                    }
                }
            }
        }
        _playListMusic.value = trackList.findPlayingMusic(lastMusic.value).toRecyclerViewItemOfList(RV_ITEM)
    }

    private fun List<Track>.toRecyclerViewItemOfList(id: Int): List<RecyclerViewBaseDataModel> {
        var listType = MainListMusicFragment.RV_ITEM
        when (id) {
            0 -> listType = MainListMusicFragment.RV_ITEM_MUSIC_ARTIST
            2 -> {
                listType = MainListMusicFragment.RV_ITEM_MUSIC_ALBUMS
            }
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

    private fun fillAlbumsList(trackList: List<Track>): List<Track>{
        val albumsList = ArrayList<Track>(emptyList())
        trackList.forEach { track ->
            albumsList.find{ it.album == track.album }.let {
                if (it == null) albumsList.add(track)
            }
        }
        return albumsList
    }

    private fun fillArtistList(trackList: List<Track>): List<Track>{
        val artistsList = ArrayList<Track>(emptyList())
        trackList.forEach { track ->
            artistsList.find{ it.artist == track.artist }.let {
                if (it == null) artistsList.add(track)
            }
        }
        return artistsList
    }

    fun fillArtistsPlayList(name: String){
        val artistPlayList = ArrayList<Track>(emptyList())
        serviceTracks.value.forEach {
            if (it.artist.contains(name)){
                artistPlayList.add(it)
            }
        }
        _artistsPlaylist.value = artistPlayList.findPlayingMusic(lastMusic.value).toRecyclerViewItemOfList(RV_ITEM)
    }

    fun fillAlbumsPlayList(name: String){
        val albumsPlayList = ArrayList<Track>(emptyList())
        serviceTracks.value.forEach {
            if (it.album.contains(name)){
                albumsPlayList.add(it)
            }
        }
        _albumsPlaylist.value = albumsPlayList.findPlayingMusic(lastMusic.value).toRecyclerViewItemOfList(RV_ITEM)
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
        val trackList = ArrayList<Track>(emptyList())
        serviceTracks.value.forEach {
            if (it.data.contains(data + File.separator, ignoreCase = true)) {
                trackList.add(it)
            }
        }
        _folderMusicPlaylist.value = trackList.findPlayingMusic(lastMusic.value).toRecyclerViewItemOfList(RV_ITEM)
    }

    ///////////////////////////////////////////
    ///////////////////////////////////////////
    ///////////////////////////////////////////

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