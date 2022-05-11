package ru.kamaz.music_api.interactor

import ru.kamaz.music_api.interfaces.Repository
import ru.sir.core.AsyncUseCase
import ru.sir.core.Either
import ru.sir.core.None

class GetMusicCover(private val repository: Repository) : AsyncUseCase<String, GetMusicCover.Params, None>() {
    override suspend fun run(params: Params): Either<None, String> = repository.getMusicCover(params.albumId)

    data class Params(val albumId: Long)
}