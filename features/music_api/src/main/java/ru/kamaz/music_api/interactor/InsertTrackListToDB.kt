package ru.kamaz.music_api.interactor

import ru.kamaz.music_api.Failure
import ru.kamaz.music_api.interfaces.Repository
import ru.kamaz.music_api.models.FavoriteSongs
import ru.kamaz.music_api.models.Track
import ru.sir.core.AsyncUseCase
import ru.sir.core.Either
import ru.sir.core.None

class InsertTrackListToDB (private val repository: Repository) : AsyncUseCase<None, InsertTrackListToDB.Params, Failure>() {
    data class Params(val track: List<Track>)

    override suspend fun run(params: Params): Either<Failure, None> = repository.insertTrackList(params.track)
}