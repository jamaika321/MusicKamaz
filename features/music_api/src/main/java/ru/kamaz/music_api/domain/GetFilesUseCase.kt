package ru.kamaz.music_api.domain

import ru.kamaz.music_api.SourceType
import java.io.File

interface GetFilesUseCase {
    fun getFiles(directoryPath: String): List<File>
    suspend fun getFilesFromSource(sourceType: SourceType): List<File>
}