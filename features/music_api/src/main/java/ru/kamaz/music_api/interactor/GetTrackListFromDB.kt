package ru.kamaz.music_api.interactor

import ru.kamaz.music_api.Failure
import ru.kamaz.music_api.interfaces.Repository
import ru.kamaz.music_api.models.Track
import ru.sir.core.AsyncUseCase
import ru.sir.core.Either
import ru.sir.core.FlowUseCase
import ru.sir.core.None

class GetTrackListFromDB (private val repository: Repository): AsyncUseCase<List<Track>,None,  Failure>() {
    override suspend fun run(params: None): Either<Failure, List<Track>> = repository.getTrackList()
}