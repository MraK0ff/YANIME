package su.afk.yummy.tv.data.schedule.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
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
import su.afk.yummy.tv.core.storage.offlinefirst.offlineFirstCache
import su.afk.yummy.tv.core.storage.schedule.AnimeScheduleStorage
import su.afk.yummy.tv.core.storage.schedule.isFresh
import su.afk.yummy.tv.data.schedule.network.YaniScheduleApi
import su.afk.yummy.tv.data.home.network.YaniHomeApi
import su.afk.yummy.tv.data.home.mapper.toAnimeEpisodeReleases
import su.afk.yummy.tv.data.schedule.storage.mapper.toAnimeScheduleCache
import su.afk.yummy.tv.domain.schedule.model.AnimeEpisodeRelease
import su.afk.yummy.tv.domain.schedule.model.AnimeScheduleDay
import su.afk.yummy.tv.domain.schedule.model.AnimeScheduleItem
import su.afk.yummy.tv.core.model.anime.AnimePoster
import su.afk.yummy.tv.domain.schedule.repository.AnimeScheduleRepository
import su.afk.yummy.tv.data.schedule.storage.mapper.toScheduleDays as toStoredScheduleDays

private const val SCHEDULE_TTL_MS = 60 * 60 * 1000L
private const val FEED_POLL_INTERVAL_MS = 60 * 1000L
private const val EPISODE_RELEASE_HISTORY_KEY = "yummyanime:episode-release-history:v1"
private val RELEASE_HISTORY_TTL_MS = Long.MAX_VALUE
private val releaseHistoryJson = Json { ignoreUnknownKeys = true }

@Serializable
private data class EpisodeReleaseHistoryDocument(
    val releases: List<EpisodeReleaseHistoryEntry> = emptyList(),
)

@Serializable
private data class EpisodeReleaseHistoryEntry(
    val animeId: Int,
    val episodeNumber: Int,
    val firstVideoAtSeconds: Long,
    val firstVideoId: Int,
    val title: String = "",
    val posterUrl: String? = null,
) {
    fun toDomain() = AnimeEpisodeRelease(
        animeId = animeId,
        episodeNumber = episodeNumber,
        firstVideoAtSeconds = firstVideoAtSeconds,
        firstVideoId = firstVideoId,
        title = title,
        posterUrl = posterUrl,
    )

    companion object {
        fun fromDomain(release: AnimeEpisodeRelease) = EpisodeReleaseHistoryEntry(
            animeId = release.animeId,
            episodeNumber = release.episodeNumber,
            firstVideoAtSeconds = release.firstVideoAtSeconds,
            firstVideoId = release.firstVideoId,
            title = release.title,
            posterUrl = release.posterUrl,
        )
    }
}

class YaniScheduleRepository(
    private val api: YaniScheduleApi,
    private val scheduleStore: AnimeScheduleStorage,
    private val settingsStore: YaniAccountSettingsStore,
    private val documentCache: DocumentCacheStorage,
    private val homeApi: YaniHomeApi,
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
                    language = languageCode,
                    cachedAt = System.currentTimeMillis(),
                )
                scheduleStore.saveSchedule(cache)
                cache
            },
        )
        schedule.withVideoReleaseHistory(readEpisodeReleaseHistory())
    }

    override suspend fun saveEpisodeReleases(releases: List<AnimeEpisodeRelease>) {
        releaseHistoryMutex.withLock {
            lastFeedPollAtMs = System.currentTimeMillis()
            if (releases.isEmpty()) return@withLock
            val previous = readEpisodeReleaseHistory()
            val merged = (previous + releases)
                .groupBy { it.animeId to it.episodeNumber }
                .values
                .map { episodeVersions ->
                    val first = episodeVersions.minWith(
                        compareBy<AnimeEpisodeRelease> { it.firstVideoAtSeconds }
                            .thenBy { it.firstVideoId },
                    )
                    val metadata = episodeVersions.lastOrNull { it.title.isNotBlank() }
                    first.copy(
                        title = metadata?.title?.takeIf(String::isNotBlank) ?: first.title,
                        posterUrl = metadata?.posterUrl ?: first.posterUrl,
                    )
                }
                .sortedWith(compareBy<AnimeEpisodeRelease> { it.firstVideoAtSeconds }.thenBy { it.animeId }.thenBy { it.episodeNumber })
            writeEpisodeReleaseHistory(merged)
        }
    }

    /**
     * Календарь тоже должен работать независимо от того, открывалась ли главная. Лента опрашивается
     * не чаще раза в минуту; ошибки ленты не мешают показать будущие даты расписания.
     */
    private suspend fun refreshEpisodeFeedIfNeeded() {
        feedRefreshMutex.withLock {
            val now = System.currentTimeMillis()
            if (now - lastFeedPollAtMs < FEED_POLL_INTERVAL_MS) return
            try {
                val feed = homeApi.getFeed()
                saveEpisodeReleases(feed.response.newVideos.toAnimeEpisodeReleases())
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                // Не повторяем неудачный запрос на каждом пересчёте календаря.
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
            fetch = { EpisodeReleaseHistoryDocument() },
        )

    private suspend fun writeEpisodeReleaseHistory(releases: List<AnimeEpisodeRelease>) {
        documentCache.getOrFetch(
            cacheKey = EPISODE_RELEASE_HISTORY_KEY,
            ttlMs = RELEASE_HISTORY_TTL_MS,
            forceRefresh = true,
            decode = { releaseHistoryJson.decodeFromString<EpisodeReleaseHistoryDocument>(it).releases.map(EpisodeReleaseHistoryEntry::toDomain) },
            encode = { releaseHistoryJson.encodeToString(EpisodeReleaseHistoryDocument(it.map(EpisodeReleaseHistoryEntry::fromDomain))) },
            fetch = { EpisodeReleaseHistoryDocument(releases) },
        )
    }

    private fun List<AnimeScheduleDay>.withVideoReleaseHistory(
        releases: List<AnimeEpisodeRelease>,
    ): List<AnimeScheduleDay> {
        if (releases.isEmpty()) return this
        val byAnime = releases.groupBy { it.animeId }
        val existingIds = flatMap { it.items }.mapTo(mutableSetOf()) { it.animeId }
        val updated = map { day ->
            day.copy(items = day.items.map { item ->
                item.copy(videoReleases = byAnime[item.animeId].orEmpty().sortedBy { it.firstVideoAtSeconds })
            })
        }
        val additional = byAnime.filterKeys { it !in existingIds }.mapNotNull { (animeId, items) ->
            val latest = items.maxByOrNull { it.firstVideoAtSeconds } ?: return@mapNotNull null
            AnimeScheduleItem(
                animeId = animeId,
                title = latest.title.ifBlank { "Anime $animeId" },
                poster = latest.posterUrl?.takeIf(String::isNotBlank)?.let { AnimePoster(small = null, medium = null, big = null, fullsize = null, mega = it) },
                nextDateEpochSeconds = null,
                previousDateEpochSeconds = null,
                airedEpisodes = null,
                totalEpisodes = null,
                videoReleases = items.sortedBy { it.firstVideoAtSeconds },
            )
        }
        return if (additional.isEmpty()) updated else updated + AnimeScheduleDay(
            title = "YummyAnime",
            items = additional,
        )
    }
}
