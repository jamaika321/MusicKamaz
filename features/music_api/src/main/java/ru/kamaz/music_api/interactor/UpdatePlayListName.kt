package ru.kamaz.music_api.interactor

import ru.kamaz.music_api.Failure
import ru.kamaz.music_api.interfaces.Repository
import ru.sir.core.AsyncUseCase
import ru.sir.core.Either
import ru.sir.core.None

class UpdatePlayListName(private val repository: Repository) : AsyncUseCase<None, UpdatePlayListName.Params, Failure>() {
    data class Params(val name: String, val newName: String)
    override suspend fun run(params: Params): Either<Failure, None> = repository.updatePlayListName(params.name, params.newName)
}