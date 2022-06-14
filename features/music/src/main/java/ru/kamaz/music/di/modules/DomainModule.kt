package ru.kamaz.music.di.modules

import dagger.Module
import dagger.Provides
import ru.kamaz.music_api.domain.GetFilesUseCase
import ru.kamaz.music_api.interactor.*
import ru.kamaz.music_api.interfaces.Repository

@Module
class DomainModule {

    @Provides
    fun provideLoadDiskData(repository: Repository): LoadDiskData = LoadDiskData(repository)
    @Provides
    fun provideArtistLoadRV(repository: Repository): ArtistLoadRV = ArtistLoadRV(repository)
    @Provides
    fun provideAllFolderWithMusic(repository: Repository): AllFolderWithMusicRV = AllFolderWithMusicRV(repository)

    @Provides
    fun provideCategoryLoadRV(repository: Repository): CategoryLoadRV = CategoryLoadRV(repository)

    @Provides
    fun provideGetMusicCover(repository: Repository): GetMusicCover = GetMusicCover(repository)

    @Provides
    fun provideGetMusicPosition(repository: Repository): GetMusicPosition = GetMusicPosition(repository)
    @Provides
    fun provideGetUseCase(repository: Repository): GetFilesUseCase {
        return GetFilesUseCaseImpl(repository)
    }

    @Provides
    fun provideInsertFavoriteMusic(repository: Repository): InsertFavoriteMusic = InsertFavoriteMusic(repository)
    @Provides
    fun provideInsertLastMusic(repository: Repository): InsertLastMusic = InsertLastMusic(repository)
    @Provides
    fun provideQueryLastMusic(repository: Repository): QueryLastMusic = QueryLastMusic(repository)
    @Provides
    fun provideQueryFavoriteMusic(repository: Repository): QueryFavoriteMusic = QueryFavoriteMusic(repository)
   @Provides
    fun provideFavoriteMusicRV(repository: Repository): FavoriteMusicRV = FavoriteMusicRV(repository)
    @Provides
    fun providePlayListRV(repository: Repository): PlayListRV = PlayListRV(repository)
    @Provides
    fun provideInsertPlayList(repository: Repository): InsertPlayList = InsertPlayList(repository)
    @Provides
    fun provideDeleteFavoriteMusic(repository: Repository): DeleteFavoriteMusic = DeleteFavoriteMusic(repository)
}