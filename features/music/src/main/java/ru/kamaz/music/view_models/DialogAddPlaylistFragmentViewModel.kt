package ru.kamaz.music.view_models


import android.app.Application
import android.util.Log
import androidx.lifecycle.ViewModel
import ru.kamaz.music_api.interactor.InsertPlayList
import ru.kamaz.music_api.models.PlayListModel

import javax.inject.Inject

class DialogAddPlaylistFragmentViewModel @Inject constructor(
    application: Application,
    private val insertPlayList:InsertPlayList

): ViewModel(){


}