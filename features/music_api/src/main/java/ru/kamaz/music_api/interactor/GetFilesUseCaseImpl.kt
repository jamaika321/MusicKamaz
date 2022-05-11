package ru.kamaz.music_api.interactor

import ru.kamaz.music_api.SourceType
import ru.kamaz.music_api.domain.GetFilesUseCase
import ru.kamaz.music_api.interfaces.Repository
import java.io.File

class GetFilesUseCaseImpl (
    private val repository: Repository
) : GetFilesUseCase {

    //получаем файлы из определенной директории
    override  fun getFiles(directoryPath: String): List<File> {
        return repository.getFiles(directoryPath)
    }

    //файлы из корневого каталога источника
    override suspend fun getFilesFromSource(sourceType: SourceType): List<File> {
        return repository.rootFilesFromSource(sourceType)
    }
}