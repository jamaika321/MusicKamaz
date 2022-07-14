package ru.kamaz.music_api.interactor

import ru.kamaz.music_api.interfaces.Repository
import ru.kamaz.music_api.models.PlayListModel
import ru.sir.core.AsyncUseCase
import ru.sir.core.Either
import ru.sir.core.None

class PlayListRV(private val repository: Repository): AsyncUseCase<List<PlayListModel>, None, None>()  {
    override suspend fun run(params: None): Either<None, List<PlayListModel>> = repository.rvPlayList()
}