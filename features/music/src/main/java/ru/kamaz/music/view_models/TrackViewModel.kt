package ru.kamaz.music.view_models

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import ru.biozzlab.twmanager.domain.interfaces.MusicManagerListener
import ru.kamaz.music.services.MusicService
import ru.kamaz.music.services.MusicServiceInterface
import ru.kamaz.music_api.domain.GetFilesUseCase
import ru.kamaz.music_api.interactor.GetTrackListFromDB
import ru.kamaz.music_api.interactor.LoadDiskData
import ru.kamaz.music_api.models.Track
import ru.sir.core.Either
import ru.sir.core.None
import ru.sir.presentation.base.BaseViewModel
import ru.sir.presentation.base.recycler_view.RecyclerViewBaseDataModel
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class TrackViewModel @Inject constructor(
    application: Application,
    private val loadTrackList: GetTrackListFromDB,
    private val loadDiskData: LoadDiskData,
    private val getFilesUseCase: GetFilesUseCase
) : BaseViewModel(application), ServiceConnection, MusicServiceInterface.ViewModel, MusicManagerListener{

    companion object {
        private const val RV_ITEM = 2
    }

    private val _service = MutableStateFlow<MusicServiceInterface.Service?>(null)
    val service = _service.asStateFlow()

    private val _itemsAll = MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    var itemsAll = _itemsAll.asStateFlow()

    private val _trackIsEmpty = MutableStateFlow(false)
    val trackIsEmpty = _trackIsEmpty.asStateFlow()

    val isNotConnectedUsb: StateFlow<Boolean> by lazy {
        service.value?.checkUSBConnection() ?: MutableStateFlow(false)
    }

    private val _lastMusic = MutableStateFlow("")
    val lastMusic = _lastMusic

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying

    val lastMusicChanged : StateFlow<String> by lazy {
        service.value?.lastMusic() ?: MutableStateFlow("")
    }

    lateinit var listAllTrack : ArrayList<Track>
    lateinit var changedListTrack : ArrayList<Track>

    override fun init() {
        Log.i("ReviewTest", "TrackViewModelInit")
        val intent = Intent(context, MusicService::class.java)
        context.bindService(intent, this, Context.BIND_AUTO_CREATE)
        loadDiskPlaylist()


    }

    fun lastMusic(title: String){
        _lastMusic.value = title
        if (itemsAll.value.isEmpty()){
            loadDataFromDB()
        }else{
            convertToRecyclerViewItems(changedListTrack)
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    fun filterRecyclerList (constraint: String, listTrack: ArrayList<Track>) : ArrayList<Track> {
        val musicFilterList : ArrayList<Track>
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

    fun searchMusic(music: String){
        convertToRecyclerViewItems(filterRecyclerList(music, listAllTrack))
    }

    fun checkUsbConnection(){
        service.value?.usbConnectionCheck()
    }

    fun loadDiskPlaylist(){
        Log.i("ReviewTest", "loadDiskPlaylist: ")
        loadDiskData(None()) { it.either({  }, ::onDiskDataLoaded) }
    }

    fun loadDataFromDB(){
        loadTrackList(None()) { it.either({  }, ::onDiskDataLoaded)}
    }

    private fun convertToRecyclerViewItems(listTrack: ArrayList<Track>){
        listTrack.findPlayingMusic(lastMusic.value)
        _itemsAll.value = listAllTrack.toRecyclerViewItems()
    }

    private fun onDiskDataLoaded(data: List<Track>) {
        if (data.isEmpty()) {
            Log.d("ReviewTest", "onDiskDataLoaded")
            _trackIsEmpty.value = true
            loadDiskPlaylist()
        } else {
            _trackIsEmpty.value = false
        }
        listAllTrack = data as ArrayList<Track>
        changedListTrack = data
        convertToRecyclerViewItems(data)
    }

    private fun List<Track>.findPlayingMusic(title:String): List<Track>{
        for (i in this.indices){
            this[i].playing = this[i].title == title
        }
        return this
    }

    fun onItemClick(track: Track, data: String) {
//        service.value?.intMediaPlayer()
        if (track.title != lastMusic.value) {
            service.value?.initTrack(track, data)
            service.value?.resume()
        } else {
            service.value?.playOrPause()
        }
    }

    private fun List<Track>.toRecyclerViewItems(): List<RecyclerViewBaseDataModel> {
        Log.i("ReviewTest", "toRecyclerViewItems: ")
        val newList = mutableListOf<RecyclerViewBaseDataModel>()
        this.forEach { newList.add(RecyclerViewBaseDataModel(it, RV_ITEM)) }
        return newList
    }

    override fun onPause() {
        insertTrackListToDB()
        super.onPause()
    }

    fun insertTrackListToDB(){
        Log.i("ReviewTest", "insertTrackListToDB: ")
        service.value?.insertTrackListToDB(listAllTrack)
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