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

    lateinit var listTrack : ArrayList<Track>

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



    @OptIn(ExperimentalStdlibApi::class)
    fun filterRecyclerList (constraint: String) : ArrayList<Track> {
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
        _items.value = filterRecyclerList(music).toRecyclerViewItems()


//       val sdfsdf= listTrack[0].title.lowercase(Locale.ROOT)
//        Log.i("FORTESTSEARCH", "searchMusic: ${listTrack.size}")
//        listTrack.filter {
//            it.title == "Весна"
//        }
//
//        Log.i("FORTESTSEARCH", "searchMusic: $music")
//        for (i in 0..listTrack.size-1){
//            Log.i("FORTESTSEARCH", "searchMusic: ${listTrack[i].title}")
//        }
//        Log.i("FORTESTSEARCH", "searchMusic: ${listTrack.size}")
//        Log.i("FORTESTSEARCH", "searchMusic: ${sdfsdf}")
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
        listTrack = data as ArrayList<Track>
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