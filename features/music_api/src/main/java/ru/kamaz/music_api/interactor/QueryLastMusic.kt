package ru.kamaz.music_api.interactor

import ru.kamaz.music_api.interfaces.Repository
import ru.kamaz.music_api.models.HistorySongs
import ru.sir.core.AsyncUseCase
import ru.sir.core.Either
import ru.sir.core.None

class QueryLastMusic(private val repository: Repository): AsyncUseCase<HistorySongs, QueryLastMusic.Params, None>()  {
    data class Params(val id: Int)
    override suspend fun run(params: Params): Either<None, HistorySongs> = repository.queryHistorySongs(params.id)
}

