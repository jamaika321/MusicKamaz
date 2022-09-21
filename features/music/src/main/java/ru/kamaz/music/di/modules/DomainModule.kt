package ru.kamaz.music.di.modules

import dagger.Module
import dagger.Provides
import ru.kamaz.music.domain.TestSettings
import ru.kamaz.music.services.MusicService
import ru.kamaz.music.utils.TWSetting
import ru.kamaz.music_api.domain.GetFilesUseCase
import ru.kamaz.music_api.interactor.*
import ru.kamaz.music_api.interfaces.Repository

@Module
class DomainModule {

    @Provides
    fun provideAllFolderWithMusic(repository: Repository): AllFolderWithMusicRV = AllFolderWithMusicRV(repository)
    @Provides
    fun provideUpdatePlayList(repository: Repository): UpdatePlayList = UpdatePlayList(repository)
    @Provides
    fun provideUpdatePlayListName(repository: Repository): UpdatePlayListName = UpdatePlayListName(repository)

    @Provides
    fun provideGetMusicPosition(repository: Repository): GetMusicPosition = GetMusicPosition(repository)
    @Provides
    fun provideDeletePlayList(repository: Repository): DeletePlayList = DeletePlayList(repository)

    @Provides
    fun provideInsertFavoriteMusic(repository: Repository): InsertFavoriteMusic = InsertFavoriteMusic(repository)
    @Provides
    fun provideInsertLastMusic(repository: Repository): InsertLastMusic = InsertLastMusic(repository)
    @Provides
    fun provideQueryLastMusic(repository: Repository): QueryLastMusic = QueryLastMusic(repository)
    @Provides
    fun provideFavoriteMusicRV(repository: Repository): FavoriteMusicRV = FavoriteMusicRV(repository)
    @Provides
    fun providePlayListRV(repository: Repository): PlayListRV = PlayListRV(repository)
    @Provides
    fun provideInsertPlayList(repository: Repository): InsertPlayList = InsertPlayList(repository)
    @Provides
    fun provideDeleteFavoriteMusic(repository: Repository): DeleteFavoriteMusic = DeleteFavoriteMusic(repository)

    @Provides
    fun provideTestSettings(twSetting: TWSetting): TestSettings = TestSettings.Base(twSetting)
    @Provides
    fun provideTWSetting(): TWSetting = TWSetting.open()
}