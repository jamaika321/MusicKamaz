package ru.kamaz.music_api.interactor

import ru.kamaz.music_api.interfaces.Repository
import ru.kamaz.music_api.models.Track
import ru.sir.core.AsyncUseCase
import ru.sir.core.Either
import ru.sir.core.None


class LoadDiskData(private val repository: Repository): AsyncUseCase<List<Track>, String, None>()  {
    override suspend fun run(params: String): Either<None, List<Track>> = repository.loadDiskData(params)
}
class LoadUsbData(private val repository: Repository): AsyncUseCase<List<Track>, String, None>()  {
    override suspend fun run(params: String): Either<None, List<Track>> = repository.loadUsbData(params)
}