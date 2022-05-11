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
import ru.kamaz.music.services.MusicService
import ru.kamaz.music.services.MusicServiceInterface
import ru.kamaz.music_api.domain.GetFilesUseCase
import ru.kamaz.music_api.interactor.LoadDiskData
import ru.kamaz.music_api.interactor.LoadUsbData
import ru.kamaz.music_api.models.Track
import ru.sir.core.None
import ru.sir.presentation.base.BaseViewModel
import ru.sir.presentation.base.recycler_view.RecyclerViewBaseDataModel
import javax.inject.Inject

class TrackViewModel @Inject constructor(
    application: Application,
    private val loadDiskData: LoadDiskData,
    private val loadUsbData: LoadUsbData,
    private val getFilesUseCase: GetFilesUseCase
) : BaseViewModel(application), ServiceConnection, MusicServiceInterface.ViewModel {

    companion object {
        private const val RV_ITEM = 2
    }

    private var service: MusicServiceInterface.Service? = null


    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _items = MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    var items = _items.asStateFlow()

    private val _itemsUsb = MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    var itemsUsb = _itemsUsb.asStateFlow()

    private val _sourceEnum = MutableStateFlow(true)
    var sourceEnum = _sourceEnum.asStateFlow()

    private val _trackIsEmpty = MutableStateFlow(false)
    val trackIsEmpty = _trackIsEmpty.asStateFlow()


    override fun init() {
        _isLoading.value = true
        val intent = Intent(context, MusicService::class.java)
        context.bindService(intent, this, Context.BIND_AUTO_CREATE)

        loadDiskPlaylist()
        loadUsbPlaylist()

        Log.i("trackFrag1", "initVars: ${items.value}")

    }

    fun changeSource(sourceEnum: String){
        when (sourceEnum) {
            "2" -> _sourceEnum.value = true
            "3" -> _sourceEnum.value = false
        }
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
        _items.value = data.toRecyclerViewItems()
        _isLoading.value = false
    }
    private fun onUsbDataLoaded(data: List<Track>) {
        if (data.isEmpty()) {
            Log.d("mediaPlayer", "no")
            _trackIsEmpty.value = true
           // toast()
        } else _trackIsEmpty.value = false
        _itemsUsb.value = data.toRecyclerViewItems()
        _isLoading.value = false

    }
    fun onItemClick(track: Track, data: String) {
        service?.intMediaPlayer()
        service?.initTrack(track, data)
        service?.playOrPause()
        Log.i("onTrackClicked", "onTrackClicked")
    }

    private fun List<Track>.toRecyclerViewItems(): List<RecyclerViewBaseDataModel> {
        val newList = mutableListOf<RecyclerViewBaseDataModel>()
        this.forEach { newList.add(RecyclerViewBaseDataModel(it, RV_ITEM)) }
        return newList
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        Log.d("serWStart", "onServiceConnected: LIST-VM")
        this.service = (service as MusicService.MyBinder).getService()
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        service = null
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