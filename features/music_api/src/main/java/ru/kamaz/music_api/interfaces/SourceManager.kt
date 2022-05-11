package ru.kamaz.music_api.interfaces

import ru.kamaz.music_api.SourceType

interface SourceManager {

    fun getSelectedSource(): SourceType
}