package ru.kamaz.music.view_models.list

import android.app.Application
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.kamaz.music_api.interactor.AllFolderWithMusicRV
import ru.kamaz.music_api.models.AllFolderWithMusic
import ru.sir.core.None
import ru.sir.presentation.base.BaseViewModel
import ru.sir.presentation.base.recycler_view.RecyclerViewBaseDataModel
import javax.inject.Inject

class ListViewModel @Inject constructor(
    application: Application,
    private val rvAllFolderWithMusic: AllFolderWithMusicRV
):BaseViewModel(application) {

    companion object {
        private const val RV_ITEM_MUSIC_FOLDER = 5
    }
    private val _items = MutableStateFlow<List<RecyclerViewBaseDataModel>>(emptyList())
    var items = _items.asStateFlow()

    init {
        rvAllFolderWithMusic(None()) { it.either({}, ::onAllFolderWithMusic) }
    }

    private fun onAllFolderWithMusic(folderMusic: List<AllFolderWithMusic>){
        _items.value = folderMusic.toRecyclerViewItemsFolder()
    }
    private fun List<AllFolderWithMusic>.toRecyclerViewItemsFolder(): List<RecyclerViewBaseDataModel> {
        val newList = mutableListOf<RecyclerViewBaseDataModel>()
        this.forEach { newList.add(RecyclerViewBaseDataModel(it, RV_ITEM_MUSIC_FOLDER
        )) }
        return newList
    }

}