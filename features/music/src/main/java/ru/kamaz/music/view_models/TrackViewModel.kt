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
import ru.kamaz.music_api.interactor.LoadDiskData
import ru.kamaz.music_api.interactor.LoadUsbData
import ru.kamaz.music_api.models.Track
import ru.sir.core.Either
import ru.sir.core.None
import ru.sir.presentation.base.BaseViewModel
import ru.sir.presentation.base.recycler_view.RecyclerViewBaseDataModel
import java.util.*
import javax.inject.Inject

class TrackViewModel @Inject constructor(
    application: Application,
    private val loadDiskData: LoadDiskData,
    private val loadUsbData: LoadUsbData,
    private val getFilesUseCase: GetFilesUseCase
) : BaseViewModel(application), ServiceConnection, MusicServiceInterface.ViewModel, MusicManagerListener{

    companion object {
        private const val RV_ITEM = 2
    }

    private val _service = MutableStateFlow<MusicServiceInterface.Service?>(null)
    val service = _service.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _items = MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    var items = _items.asStateFlow()

    private val _itemsAll = MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    var itemsAll = _itemsAll.asStateFlow()

    private val _trackIsEmpty = MutableStateFlow(false)
    val trackIsEmpty = _trackIsEmpty.asStateFlow()

    val isNotConnectedUsb: StateFlow<Boolean> by lazy {
        service.value?.checkUSBConnection() ?: MutableStateFlow(false)
    }

    lateinit var listAllTrack : ArrayList<Track>
    lateinit var listTrack : ArrayList<Track>

    override fun init() {
        _isLoading.value = true
        val intent = Intent(context, MusicService::class.java)
        context.bindService(intent, this, Context.BIND_AUTO_CREATE)

        loadUsbPlaylist()
        loadDiskPlaylist()

        Log.i("trackFrag1", "initVars: ${items.value}")

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
        return musicFilterList
    }

    fun searchMusic(music: String){
        if (isNotConnectedUsb.value){
            _itemsAll.value = filterRecyclerList(music, listAllTrack).toRecyclerViewItems()
        } else {
            _items.value = filterRecyclerList(music, listTrack).toRecyclerViewItems()
        }

    }

    fun checkUsbConnection(){
        service.value?.usbConnectionCheck()
    }

    fun loadUsbPlaylist(){
        loadUsbData(None()) { it.either({  }, ::onUsbDataLoaded) }
    }

    fun loadDiskPlaylist(){
        loadDiskData(None()) { it.either({  }, ::onDiskDataLoaded) }
    }



    private fun onDiskDataLoaded(data: List<Track>) {
        if (data.isEmpty()) {
            Log.d("mediaPlayer", "no")
            _trackIsEmpty.value = true
           // toast()
        } else _trackIsEmpty.value = false
        listTrack = data as ArrayList<Track>
        _items.value = data.toRecyclerViewItems()
        _isLoading.value = false
    }
    private fun onUsbDataLoaded(data: List<Track>) {
        if (data.isEmpty()) {
            Log.d("mediaPlayer", "no")
            _trackIsEmpty.value = true
           // toast()
        } else _trackIsEmpty.value = false
        listAllTrack = data as ArrayList<Track>
        _itemsAll.value = data.toRecyclerViewItems()
        _isLoading.value = false

    }
    fun onItemClick(track: Track, data: String) {
        service.value?.intMediaPlayer()
        service.value?.initTrack(track, data)
        service.value?.playOrPause()
        Log.i("onTrackClicked", "onTrackClicked")
    }

    private fun List<Track>.toRecyclerViewItems(): List<RecyclerViewBaseDataModel> {
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
        Toast.makeText(context, "$duration", Toast.LENGTH_SHORT).show()
    }

    override fun selectBtMode() {
        TODO("Not yet implemented")
    }
}