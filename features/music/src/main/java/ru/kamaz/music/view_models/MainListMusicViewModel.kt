package ru.kamaz.music.view_models

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import com.eckom.xtlibrary.twproject.music.presenter.MusicPresenter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.kamaz.music.services.MusicService
import ru.kamaz.music.services.MusicServiceInterface
import ru.kamaz.music_api.interactor.AllFolderWithMusicRV
import ru.kamaz.music_api.interactor.CategoryLoadRV
import ru.kamaz.music_api.interactor.LoadDiskData
import ru.kamaz.music_api.models.AllFolderWithMusic
import ru.kamaz.music_api.models.CategoryMusicModel
import ru.kamaz.music_api.models.Track
import ru.sir.core.None
import ru.sir.presentation.base.BaseViewModel
import ru.sir.presentation.base.recycler_view.RecyclerViewBaseDataModel
import javax.inject.Inject

class MainListMusicViewModel @Inject constructor(
    application: Application,
    private val loadData: LoadDiskData,
    private val categoryData: CategoryLoadRV,
    private val rvAllFolderWithMusic: AllFolderWithMusicRV
) : BaseViewModel(application), ServiceConnection, MusicServiceInterface.ViewModel {

    companion object {
        private const val RV_ITEM = 2
        private const val RV_ITEM_MUSIC_CATEGORY = 3
        private const val RV_ITEM_MUSIC_FOLDER = 4
    }

    private var service: MusicServiceInterface.Service? = null

    init {
        Log.i("trackState", "loading ")
    }


    val pp = MusicPresenter(context)

    fun getUsbList() {
        pp.openUSBList()
    }


    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private val _items = MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    var items = _items.asStateFlow()

    private val _huitems = MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    var huitems = _huitems.asStateFlow()

    private val _favorite = MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    var favorite = _favorite.asStateFlow()

    private val _folder = MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    var folder = _folder.asStateFlow()


    override fun init() {
        _isLoading.value = true
        loadData(None()) { it.either({}, ::onDataLoaded) }
        categoryData(None()) { it.either({}, ::onCategoryLoaded) }
        rvAllFolderWithMusic(None()) { it.either({}, ::onAllFolderWithMusic) }
        val intent = Intent(context, MusicService::class.java)
        context.bindService(intent, this, Context.BIND_AUTO_CREATE)
    }

    private fun onDataLoaded(data: List<Track>) {
        _items.value = data.toRecyclerViewItems()
        _isLoading.value = false
    }

    private fun onCategoryLoaded(category: List<CategoryMusicModel>) {
        _huitems.value = category.toRecyclerViewItemsCategory()
    }


    private fun onAllFolderWithMusic(folderMusic: List<AllFolderWithMusic>) {
        _folder.value = folderMusic.toRecyclerViewItemsFolder()
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