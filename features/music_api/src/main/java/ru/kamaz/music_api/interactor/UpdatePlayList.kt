package ru.kamaz.music_api.interactor

import ru.kamaz.music_api.Failure
import ru.kamaz.music_api.interfaces.Repository
import ru.kamaz.music_api.models.PlayListModel
import ru.sir.core.AsyncUseCase
import ru.sir.core.Either
import ru.sir.core.None

class UpdatePlayList(private val repository: Repository) : AsyncUseCase<None, UpdatePlayList.Params, Failure>() {
    data class Params(val name: String, val title: List<String>, val data: List<String>)
    override suspend fun run(params: Params): Either<Failure, None> = repository.updatePlayList(params.name, params.title, params.data)
}