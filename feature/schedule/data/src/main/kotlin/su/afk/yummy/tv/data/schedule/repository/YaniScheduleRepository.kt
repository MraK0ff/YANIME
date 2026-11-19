package su.afk.yummy.tv.data.schedule.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.withContext
import su.afk.yummy.tv.core.preferences.api.YaniAccountSettingsStore
import su.afk.yummy.tv.core.preferences.settings.currentLanguageCode
import su.afk.yummy.tv.core.storage.document.DocumentCacheStorage
import su.afk.yummy.tv.core.storage.anime.AnimeStorage
import su.afk.yummy.tv.core.storage.offlinefirst.offlineFirstCache
import su.afk.yummy.tv.core.storage.schedule.AnimeScheduleStorage
import su.afk.yummy.tv.core.storage.schedule.isFresh
import su.afk.yummy.tv.data.schedule.network.YaniScheduleApi
import su.afk.yummy.tv.data.home.network.YaniHomeApi
import su.afk.yummy.tv.data.home.mapper.toAnimeEpisodeReleases
import su.afk.yummy.tv.data.schedule.storage.mapper.toAnimeScheduleCache
import su.afk.yummy.tv.domain.schedule.model.AnimeEpisodeRelease
import su.afk.yummy.tv.domain.schedule.model.AnimeScheduleDay
import su.afk.yummy.tv.domain.schedule.model.ReleasePredictionCalculator
import su.afk.yummy.tv.domain.schedule.repository.AnimeScheduleRepository
import su.afk.yummy.tv.data.schedule.storage.mapper.toScheduleDays as toStoredScheduleDays

private const val SCHEDULE_TTL_MS = 60 * 60 * 1000L
private const val FEED_POLL_INTERVAL_MS = 60 * 1000L
private const val EPISODE_RELEASE_HISTORY_KEY = "yummyanime:episode-release-history:v1"
private const val RELEASE_HISTORY_TTL_MS = Long.MAX_VALUE
private val releaseHistoryJson = Json { ignoreUnknownKeys = true }

@Serializable
private data class EpisodeReleaseHistoryDocument(val releases: List<EpisodeReleaseHistoryEntry> = emptyList())

@Serializable
private data class EpisodeReleaseHistoryEntry(
    val animeId: Int,
    val episodeNumber: Int,
    val firstVideoAtSeconds: Long,
    val firstVideoId: Int,
    val title: String = "",
    val posterUrl: String? = null,
    val playerName: String = "",
    val dubbing: String = "",
) {
    fun toDomain() = AnimeEpisodeRelease(
        animeId, episodeNumber, firstVideoAtSeconds, firstVideoId, title, posterUrl, playerName, dubbing,
    )

    companion object {
        fun fromDomain(release: AnimeEpisodeRelease) = EpisodeReleaseHistoryEntry(
            release.animeId, release.episodeNumber, release.firstVideoAtSeconds, release.firstVideoId,
            release.title, release.posterUrl, release.playerName, release.dubbing,
        )
    }
}

class YaniScheduleRepository(
    private val api: YaniScheduleApi,
    private val scheduleStore: AnimeScheduleStorage,
    private val settingsStore: YaniAccountSettingsStore,
    private val documentCache: DocumentCacheStorage,
    private val homeApi: YaniHomeApi,
    private val animeStorage: AnimeStorage,
) : AnimeScheduleRepository {
    private val releaseHistoryMutex = Mutex()
    private val feedRefreshMutex = Mutex()
    @Volatile private var lastFeedPollAtMs = 0L

    override suspend fun getSchedule(): List<AnimeScheduleDay> = withContext(Dispatchers.IO) {
        refreshEpisodeFeedIfNeeded()
        val languageCode = settingsStore.currentLanguageCode()
        val schedule = offlineFirstCache(
            read = { scheduleStore.getSchedule(languageCode) },
            isFresh = { it.isFresh(SCHEDULE_TTL_MS) },
            toDomain = { it.toStoredScheduleDays() },
            fetchAndSave = {
                val cache = api.getSchedule().response.toAnimeScheduleCache(
                    language = languageCode, cachedAt = System.currentTimeMillis(),
                )
                scheduleStore.saveSchedule(cache)
                cache
            },
        )
        val releases = readEpisodeReleaseHistory()
        val counts = try {
            animeStorage.observeAvailableEpisodeCounts(languageCode).first()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (_: Exception) {
            emptyMap()
        }
        schedule.withVideoReleaseHistory(releases).withAvailableEpisodeCounts(counts)
    }

    override suspend fun saveEpisodeReleases(releases: List<AnimeEpisodeRelease>) {
        releaseHistoryMutex.withLock {
            lastFeedPollAtMs = System.currentTimeMillis()
            if (releases.isEmpty()) return@withLock
            val merged = (readEpisodeReleaseHistory() + releases)
                .groupBy { listOf(it.animeId, it.episodeNumber, it.playerName.trim().lowercase(), it.dubbing.trim().lowercase(), it.firstVideoId) }
                .values.map { versions ->
                    val first = versions.minBy { it.firstVideoAtSeconds }
                    val metadata = versions.lastOrNull { it.title.isNotBlank() }
                    first.copy(
                        title = metadata?.title?.takeIf(String::isNotBlank) ?: first.title,
                        posterUrl = metadata?.posterUrl ?: first.posterUrl,
                    )
                }
                .sortedWith(compareBy<AnimeEpisodeRelease> { it.animeId }
                    .thenBy { it.playerName }.thenBy { it.dubbing }
                    .thenBy { it.episodeNumber }.thenBy { it.firstVideoAtSeconds })
            writeEpisodeReleaseHistory(merged)
        }
    }

    private suspend fun refreshEpisodeFeedIfNeeded() {
        feedRefreshMutex.withLock {
            val now = System.currentTimeMillis()
            if (now - lastFeedPollAtMs < FEED_POLL_INTERVAL_MS) return
            try {
                saveEpisodeReleases(homeApi.getFeed().response.newVideos.toAnimeEpisodeReleases())
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                lastFeedPollAtMs = now
            }
        }
    }

    private suspend fun readEpisodeReleaseHistory(): List<AnimeEpisodeRelease> =
        documentCache.getOrFetch(
            cacheKey = EPISODE_RELEASE_HISTORY_KEY,
            ttlMs = RELEASE_HISTORY_TTL_MS,
            decode = { releaseHistoryJson.decodeFromString<EpisodeReleaseHistoryDocument>(it).releases.map(EpisodeReleaseHistoryEntry::toDomain) },
            encode = { releaseHistoryJson.encodeToString(EpisodeReleaseHistoryDocument(it.map(EpisodeReleaseHistoryEntry::fromDomain))) },
            fetch = { emptyList() },
        )

    private suspend fun writeEpisodeReleaseHistory(releases: List<AnimeEpisodeRelease>) {
        documentCache.getOrFetch(
            cacheKey = EPISODE_RELEASE_HISTORY_KEY, ttlMs = RELEASE_HISTORY_TTL_MS, forceRefresh = true,
            decode = { releaseHistoryJson.decodeFromString<EpisodeReleaseHistoryDocument>(it).releases.map(EpisodeReleaseHistoryEntry::toDomain) },
            encode = { releaseHistoryJson.encodeToString(EpisodeReleaseHistoryDocument(it.map(EpisodeReleaseHistoryEntry::fromDomain))) },
            fetch = { releases },
        )
    }

    private fun List<AnimeScheduleDay>.withAvailableEpisodeCounts(counts: Map<Int, Int>) = map { day ->
        day.copy(items = day.items.map { item ->
            item.copy(availableEpisodeCount = counts[item.animeId]?.takeIf { it > 0 })
        })
    }

    private fun List<AnimeScheduleDay>.withVideoReleaseHistory(releases: List<AnimeEpisodeRelease>) = map { day ->
        day.copy(items = day.items.map { item ->
            val animeReleases = releases.filter { it.animeId == item.animeId }
            item.copy(
                videoReleases = animeReleases.sortedBy { it.firstVideoAtSeconds },
                releasePrediction = ReleasePredictionCalculator.predict(animeReleases, System.currentTimeMillis() / 1000L),
            )
        })
    }
}
