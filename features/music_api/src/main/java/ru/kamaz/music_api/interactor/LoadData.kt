package ru.kamaz.music_api.interactor

import ru.kamaz.music_api.interfaces.Repository
import ru.kamaz.music_api.models.Track
import ru.sir.core.AsyncUseCase
import ru.sir.core.Either
import ru.sir.core.None


class LoadDiskData(private val repository: Repository): AsyncUseCase<List<Track>, None, None>()  {
    override suspend fun run(params: None): Either<None, List<Track>> = repository.loadDiskData()
}