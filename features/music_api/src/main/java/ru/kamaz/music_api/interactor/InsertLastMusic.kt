package ru.kamaz.music_api.interactor

import ru.kamaz.music_api.Failure
import ru.kamaz.music_api.interfaces.Repository
import ru.kamaz.music_api.models.HistorySongs
import ru.sir.core.AsyncUseCase
import ru.sir.core.Either
import ru.sir.core.None

class InsertLastMusic(private val repository: Repository) : AsyncUseCase<None, InsertLastMusic.Params, Failure>() {
        data class Params(val list: HistorySongs)

        override suspend fun run(params: Params): Either<Failure, None> = repository.insertHistorySong(params.list)
}
