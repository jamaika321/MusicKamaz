package ru.kamaz.music.view_models


import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ru.kamaz.music.ui.fragments.MainListMusicFragment
import ru.kamaz.music_api.interactor.InsertPlayList
import ru.kamaz.music_api.models.PlayListModel
import ru.sir.presentation.base.BaseViewModel

import javax.inject.Inject

class DialogAddPlaylistFragmentViewModel @Inject constructor(
    application: Application,
    private val insertPlayList: InsertPlayList
): BaseViewModel(application){

    fun savePlayListOnDB(title: String){
        CoroutineScope(Dispatchers.IO).launch {
            insertPlayList.run(
                InsertPlayList.Params(
                    PlayListModel(13L, title, "", listOf(""), listOf(""))))
        }
    }

}