package su.afk.yummy.tv.core.preferences.settings

import kotlinx.coroutines.flow.Flow

enum class TitleReleaseTrackingMode { ALL, DUBBING_ONLY, SUBTITLES_ONLY }

data class TitleReleaseTracking(
    val animeId: Int,
    val title: String,
    val mode: TitleReleaseTrackingMode = TitleReleaseTrackingMode.ALL,
    val excludedDubbingKeys: Set<String> = emptySet(),
    val knownEpisodeNumbers: Set<Int> = emptySet(),
    val initialized: Boolean = false,
)

interface TitleReleaseTrackingStore {
    val subscriptions: Flow<List<TitleReleaseTracking>>
    suspend fun upsert(subscription: TitleReleaseTracking)
    suspend fun remove(animeId: Int)
    suspend fun recordEpisodes(animeId: Int, episodeNumbers: Set<Int>)
}
