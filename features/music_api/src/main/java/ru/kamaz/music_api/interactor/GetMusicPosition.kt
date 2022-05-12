package ru.kamaz.music_api.interactor

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import ru.kamaz.music_api.interfaces.Repository

class GetMusicPosition(private val repository: Repository) {
    operator fun invoke(): Flow<Int> = repository.getMusicPositionFlow().flowOn(Dispatchers.IO)

}