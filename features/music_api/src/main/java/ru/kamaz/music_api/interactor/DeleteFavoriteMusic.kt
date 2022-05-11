package ru.kamaz.music_api.interactor

import ru.kamaz.music_api.Failure
import ru.kamaz.music_api.interfaces.Repository
import ru.kamaz.music_api.models.FavoriteSongs
import ru.sir.core.AsyncUseCase
import ru.sir.core.Either
import ru.sir.core.None

class DeleteFavoriteMusic(private val repository: Repository) : AsyncUseCase<None, DeleteFavoriteMusic.Params, Failure>() {
    data class Params(val list: FavoriteSongs)

    override suspend fun run(params: Params): Either<Failure, None> = repository.deleteFavoriteSong(params.list)
}