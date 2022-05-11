package ru.kamaz.music_api.interactor

import ru.kamaz.music_api.Failure
import ru.kamaz.music_api.interfaces.Repository
import ru.kamaz.music_api.models.PlayListModel
import ru.sir.core.AsyncUseCase
import ru.sir.core.Either
import ru.sir.core.None

class InsertPlayList(private val repository: Repository) : AsyncUseCase<None, InsertPlayList.Params, Failure>() {
    data class Params(val list: PlayListModel)
    override suspend fun run(params: Params): Either<Failure, None> = repository.insertPlayList(params.list)
}