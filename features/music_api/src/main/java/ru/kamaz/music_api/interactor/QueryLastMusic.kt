package ru.kamaz.music_api.interactor

import ru.kamaz.music_api.Failure
import ru.kamaz.music_api.interfaces.Repository
import ru.sir.core.AsyncUseCase
import ru.sir.core.Either
import ru.sir.core.None

class QueryLastMusic(private val repository: Repository): AsyncUseCase<String,None,Failure>()  {

    override suspend fun run(params: None): Either<Failure, String> = repository.queryHistorySongs()
}

