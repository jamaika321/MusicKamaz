package ru.kamaz.music_api.interfaces
interface PathManager {

    fun getCurrentPath(): String
    fun getRootPathOfSource(): String
}