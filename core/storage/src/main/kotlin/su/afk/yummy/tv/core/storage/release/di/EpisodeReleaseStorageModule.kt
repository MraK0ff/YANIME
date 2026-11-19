package su.afk.yummy.tv.core.storage.release.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import su.afk.yummy.tv.core.storage.db.AppDatabase
import su.afk.yummy.tv.core.storage.release.EpisodeReleaseStorage
import su.afk.yummy.tv.core.storage.release.EpisodeReleaseStore
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EpisodeReleaseStorageModule {
    @Provides
    @Singleton
    internal fun provideEpisodeReleaseStore(db: AppDatabase): EpisodeReleaseStore =
        EpisodeReleaseStore(db.episodeReleaseDao())

    @Provides
    @Singleton
    internal fun provideEpisodeReleaseStorage(store: EpisodeReleaseStore): EpisodeReleaseStorage = store
}
