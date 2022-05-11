package ru.kamaz.music_api.interactor

import ru.kamaz.music_api.Failure
import ru.kamaz.music_api.interfaces.Repository
import ru.kamaz.music_api.models.FavoriteSongs
import ru.kamaz.music_api.models.HistorySongs
import ru.sir.core.AsyncUseCase
import ru.sir.core.Either
import ru.sir.core.None

class QueryFavoriteMusic (private val repository: Repository): AsyncUseCase<String, QueryFavoriteMusic.Params, Failure>()  {
    data class Params(val data: String)
    override suspend fun run(params: Params): Either<Failure, String> = repository.queryFavoriteSongs(params.data)
}

