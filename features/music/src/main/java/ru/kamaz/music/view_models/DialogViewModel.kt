package ru.kamaz.music.view_models

import android.app.Application
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class DialogViewModel @Inject constructor( application: Application): ViewModel(){

    private val _name = MutableStateFlow("")
    val name= _name.asStateFlow()

    fun sendName(text: String) {
        _name.value = text
    }
}