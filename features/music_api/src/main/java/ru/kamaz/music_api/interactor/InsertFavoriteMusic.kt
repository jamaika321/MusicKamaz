package ru.kamaz.music_api.interactor

import ru.kamaz.music_api.Failure
import ru.kamaz.music_api.interfaces.Repository
import ru.kamaz.music_api.models.FavoriteSongs
import ru.kamaz.music_api.models.Track
import ru.sir.core.AsyncUseCase
import ru.sir.core.Either
import ru.sir.core.None

class InsertFavoriteMusic(private val repository: Repository) : AsyncUseCase<None,InsertFavoriteMusic.Params,Failure>() {
    data class Params(val list: Track)

    override suspend fun run(params: Params): Either<Failure, None> = repository.insertFavoriteSong(params.list)
}