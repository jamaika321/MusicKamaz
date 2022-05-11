package ru.kamaz.music_api.interactor

import kotlinx.coroutines.flow.Flow
import ru.kamaz.music_api.Failure
import ru.kamaz.music_api.interfaces.Repository
import ru.kamaz.music_api.models.FavoriteSongs
import ru.sir.core.AsyncUseCase
import ru.sir.core.Either
import ru.sir.core.FlowUseCase
import ru.sir.core.None

class FavoriteMusicRV(private val repository: Repository): FlowUseCase<List<FavoriteSongs>, None>()  {
    override fun run(params: None): Flow<List<FavoriteSongs>> =repository.rvFavorite()
}