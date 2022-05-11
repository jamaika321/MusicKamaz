package ru.kamaz.music_api.interactor

import ru.kamaz.music_api.interfaces.Repository
import ru.kamaz.music_api.models.CategoryMusicModel
import ru.sir.core.AsyncUseCase
import ru.sir.core.Either
import ru.sir.core.None

class CategoryLoadRV(private val repository: Repository): AsyncUseCase<List<CategoryMusicModel>, None, None>()  {
    override suspend fun run(params: None): Either<None, List<CategoryMusicModel>> = repository.rvCategory()
}