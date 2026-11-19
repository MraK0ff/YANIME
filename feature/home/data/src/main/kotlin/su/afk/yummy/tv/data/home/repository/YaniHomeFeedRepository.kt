package su.afk.yummy.tv.data.home.repository

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import su.afk.yummy.tv.core.analytics.api.AnalyticsTracker
import su.afk.yummy.tv.core.error.api.StringProvider
import su.afk.yummy.tv.core.model.anime.AnimeWatchProgress
import su.afk.yummy.tv.core.preferences.api.YaniAccountSettingsStore
import su.afk.yummy.tv.core.preferences.settings.PlayerSettingsStore
import su.afk.yummy.tv.core.preferences.settings.currentLanguageCode
import su.afk.yummy.tv.core.storage.anime.AnimeStorage
import su.afk.yummy.tv.core.storage.home.HomeFeedCache
import su.afk.yummy.tv.core.storage.home.HomeFeedStorage
import su.afk.yummy.tv.core.storage.home.HomeNewReleaseEntry
import su.afk.yummy.tv.core.storage.home.HOME_FEED_CONTAINER_NEW_RELEASES
import su.afk.yummy.tv.core.storage.home.isFresh
import su.afk.yummy.tv.core.storage.offlinefirst.offlineFirstCache
import su.afk.yummy.tv.core.storage.watchprogress.WatchProgressStorage
import su.afk.yummy.tv.data.home.mapper.LocalFeedContext
import su.afk.yummy.tv.data.home.mapper.localContinueWatchingItems
import su.afk.yummy.tv.data.home.mapper.withLocalOverrides
import su.afk.yummy.tv.data.home.network.YaniHomeApi
import su.afk.yummy.tv.data.home.utils.YaniNewReleaseTracker
import su.afk.yummy.tv.data.home.storage.mapper.toHomeContinueWatchingItem
import su.afk.yummy.tv.data.home.storage.mapper.toHomeFeedCache
import su.afk.yummy.tv.domain.home.model.ContinueWatchingProgressMigration
import su.afk.yummy.tv.domain.home.model.HomeContinueWatchingItem
import su.afk.yummy.tv.domain.home.model.HomeFeed
import su.afk.yummy.tv.domain.home.repository.HomeFeedRepository
import su.afk.yummy.tv.data.home.mapper.toAnimeEpisodeReleases
import su.afk.yummy.tv.domain.schedule.repository.AnimeScheduleRepository
import su.afk.yummy.tv.data.home.storage.mapper.toHomeFeed as toStoredHomeFeed


/**
 * The old cache had one card per anime but no episode number. Convert it into a zero-episode
 * baseline so the first history-aware poll enriches it without producing false new-release moves.
 */
private fun HomeFeedCache?.toLegacyReleaseHistory(now: Long): List<HomeNewReleaseEntry> {
    val baselineAt = this?.cachedAt?.takeIf { it > 0L } ?: now
    return this?.items
        .orEmpty()
        .asSequence()
        .filter { it.container == HOME_FEED_CONTAINER_NEW_RELEASES }
        .sortedBy { it.position }
        .distinctBy { it.itemId }
        .mapIndexed { index, item ->
            HomeNewReleaseEntry(
                animeId = item.itemId,
                title = item.title,
                animeUrl = "",
                description = item.description,
                episodeNumber = 0,
                episodeTitle = "",
                videoId = null,
                dubTitle = item.dubbing.takeIf(String::isNotBlank),
                playerTitle = item.playerName.takeIf(String::isNotBlank),
                posterSmallUrl = item.posterSmallUrl,
                posterMediumUrl = item.posterMediumUrl,
                posterBigUrl = item.posterBigUrl,
                posterFullsizeUrl = item.posterFullsizeUrl,
                posterMegaUrl = item.posterMegaUrl,
                firstSeenAt = baselineAt,
                lastDetectedAt = baselineAt,
                lastUpdatedAt = 0L,
                position = index,
            )
        }
        .toList()
}

private const val FEED_TTL_MS = 60 * 1000L
private const val FEED_CACHE_SIGNATURE_VERSION = "cw-local1"
private const val TAG = "YaniHomeFeed"

/**
 * Returns a whole episode number only for plain numeric episode labels.
 * Fractional specials (for example "7.5") must not be counted as episode 7.
 */
private fun String.toWholeEpisodeNumberOrNull(): Int? {
    val number = trim().toDoubleOrNull() ?: return null
    if (!number.isFinite() || number <= 0.0 || number % 1.0 != 0.0) return null
    return number.toInt()
}

class YaniHomeFeedRepository(
    private val api: YaniHomeApi,
    private val homeFeedStore: HomeFeedStorage,
    private val stringProvider: StringProvider,
    private val settingsStore: YaniAccountSettingsStore,
    private val playerSettingsStore: PlayerSettingsStore,
    private val watchProgressStore: WatchProgressStorage,
    private val animeStorage: AnimeStorage,
    private val analyticsTracker: AnalyticsTracker,
    private val scheduleRepository: AnimeScheduleRepository,
) : HomeFeedRepository {

    /** Параллельные загрузки ленты (экран и канал ТВ) ждут друг друга, а не качают её дважды. */
    private val fetchMutex = Mutex()

    override suspend fun getHomeFeed(): HomeFeed = getHomeFeed(forceRefresh = false)

    override suspend fun getCachedHomeFeed(): HomeFeed? = withContext(Dispatchers.IO) {
        val local = readLocalFeedContext()
        homeFeedStore.getFeed(local.languageCode, feedCacheSignature())
            ?.toStoredHomeFeed(stringProvider)
            ?.withLocalOverrides(local.watchEntries, local.hiddenIds)
    }

    override suspend fun refreshHomeFeed(): HomeFeed = getHomeFeed(forceRefresh = true)

    override suspend fun removeCachedContinueWatching(animeId: Int) {
        withContext(Dispatchers.IO) {
            watchProgressStore.suppressContinueWatchingDisplay(animeId)
            homeFeedStore.deleteContinueWatchingByAnimeId(animeId)
        }
    }

    override suspend fun getContinueWatchingVideoIds(animeId: Int): List<Int> =
        withContext(Dispatchers.IO) {
            watchProgressStore.continueWatching()
                .filter { it.animeId == animeId }
                .map { it.videoId }
                .filter { it > 0 }
                .distinct()
        }

    override suspend fun migrateContinueWatchingProgress(
        migration: ContinueWatchingProgressMigration,
    ) = withContext(Dispatchers.IO) {
        watchProgressStore.save(
            animeId = migration.animeId,
            episode = migration.episode,
            videoId = migration.videoId,
            episodeUrl = migration.episodeUrl,
            positionMs = migration.positionMs,
            durationMs = migration.durationMs,
            animeTitle = migration.animeTitle,
            posterUrl = migration.posterUrl,
            playerName = migration.playerName,
            dubbing = migration.dubbing,
            screenshotUrl = migration.screenshotUrl,
        )
        watchProgressStore.delete(migration.animeId, migration.previousEpisode)
    }

    override fun observeWatchedEpisodes(): Flow<Map<Int, Set<Int>>> =
        watchProgressStore.observeWatchedProgress()
            .map { entries ->
                entries
                    .groupBy { it.animeId }
                    .mapValues { (_, items) ->
                        items.mapNotNullTo(mutableSetOf()) { entry ->
                            // Спецвыпуски вроде «7.5» не должны засчитываться как серия 7.
                            entry.episode.toWholeEpisodeNumberOrNull()
                        }
                    }
            }
            .distinctUntilChanged()

    override fun observeContinueWatching(): Flow<List<HomeContinueWatchingItem>> =
        watchProgressStore.observeContinueWatching()
            .map(::localContinueWatchingItems)
            .distinctUntilChanged()

    override fun observeWatchHistory(): Flow<List<HomeContinueWatchingItem>> =
        watchProgressStore.observeAll()
            .map { entries -> entries.map { it.toHomeContinueWatchingItem() } }
            .distinctUntilChanged()

    override fun observeServerWatchProgress(): Flow<List<AnimeWatchProgress>> =
        flow { emitAll(animeStorage.observeServerWatchProgress(settingsStore.currentLanguageCode())) }
            .distinctUntilChanged()

    override fun observeAvailableEpisodes(): Flow<Map<Int, Int>> =
        flow { emitAll(animeStorage.observeAvailableEpisodeCounts(settingsStore.currentLanguageCode())) }
            .distinctUntilChanged()

    override fun observeContinueWatchingSuppressions(): Flow<Map<Int, Long>> =
        watchProgressStore.observeContinueWatchingSuppressionTimestamps()
            .distinctUntilChanged()

    override fun observeSuggestNextEpisodeOnWatched(): Flow<Boolean> =
        playerSettingsStore.suggestNextEpisodeOnWatched.distinctUntilChanged()

    private suspend fun getHomeFeed(forceRefresh: Boolean): HomeFeed = withContext(Dispatchers.IO) {
        // Единый снимок на весь вызов: используется во всех трёх ветках (свежий кэш, сеть,
        // fallback при ошибке), чтобы не пересчитывать его отдельно для сетевой ветки.
        val local = readLocalFeedContext()
        val watchSignature = feedCacheSignature()
        val requestedAtMs = System.currentTimeMillis()
        offlineFirstCache(
            forceRefresh = forceRefresh,
            read = { homeFeedStore.getFeed(local.languageCode, watchSignature) },
            isFresh = { it.isFresh(FEED_TTL_MS) },
            toDomain = { it.toStoredHomeFeed(stringProvider) },
            fetchAndSave = {
                fetchMutex.withLock {
                    // Лента, сохранённая другим вызовом уже после начала этого, подходит и для него.
                    homeFeedStore.getFeed(local.languageCode, watchSignature)
                        ?.takeIf { it.cachedAt >= requestedAtMs }
                        ?: fetchHomeFeed(local.languageCode, watchSignature)
                }
            },
            transform = { it.withLocalOverrides(local.watchEntries, local.hiddenIds) },
        )
    }

    private suspend fun fetchHomeFeed(
        languageCode: String,
        watchSignature: String,
    ): HomeFeedCache {
        analyticsTracker.log(TAG) { "Fetch feed language=$languageCode watchSignature=$watchSignature" }
        val dto = api.getFeed()
        analyticsTracker.log(TAG) { "Feed dto ${dto.summaryForLog()}" }
        try {
            // Even an empty feed is a successful poll: the schedule repository can avoid a duplicate request.
            scheduleRepository.saveEpisodeReleases(dto.response.newVideos.toAnimeEpisodeReleases())
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            analyticsTracker.log(TAG) { "Could not persist episode release history: ${error.message}" }
        }
        // A feed upload is not enough to prove that an episode is new: an alternate dub
        // of an older episode can appear in new_videos long after the episode itself aired.
        // Use the schedule's aired count as a conservative lower bound before promoting cards.
        val airedEpisodeCounts = try {
            scheduleRepository.getSchedule()
                .flatMap { it.items }
                .mapNotNull { item ->
                    item.airedEpisodes?.takeIf { it > 0 }
                        ?.let { count -> item.animeId to count }
                }
                .toMap()
        } catch (cancelled: CancellationException) {
            throw cancelled
        } catch (error: Exception) {
            analyticsTracker.log(TAG) { "Could not read known episode counts: ${error.message}" }
            emptyMap()
        }
        val now = System.currentTimeMillis()
        val previousCache = homeFeedStore.getFeed(languageCode, watchSignature)
        val storedHistory = homeFeedStore.getNewReleaseHistory()
        // Upgrade old installs: previous feed cards did not store their episode number. Keep them
        // as a baseline so they survive a feed rotation without being mistaken for new releases.
        val historyForMerge = storedHistory.ifEmpty {
            previousCache.toLegacyReleaseHistory(now)
        }
        val releaseHistory = YaniNewReleaseTracker().merge(
            videos = dto.response.newVideos,
            history = historyForMerge,
            now = now,
            airedEpisodeCounts = airedEpisodeCounts,
        )
        homeFeedStore.saveNewReleaseHistory(releaseHistory)
        val cache = dto.toHomeFeedCache(
            language = languageCode,
            watchSignature = watchSignature,
            cachedAt = now,
            newReleaseHistory = releaseHistory,
        )
        homeFeedStore.saveFeed(cache)
        analyticsTracker.log(TAG) {
            val feed = cache.toStoredHomeFeed(stringProvider)
            "Feed mapped ${feed.summaryForLog()} " +
                "continueSamples=${feed.continueWatchingItems.summaryForLog()}"
        }
        return cache
    }

    /** Язык, локальный прогресс (Room) и скрытые рекомендации независимы — читаются параллельно. */
    private suspend fun readLocalFeedContext(): LocalFeedContext = coroutineScope {
        val languageCode = async { settingsStore.currentLanguageCode() }
        val watchEntries = async { watchProgressStore.continueWatching() }
        val hiddenIds = async { settingsStore.hiddenRecommendationIds.first() }
        LocalFeedContext(
            languageCode = languageCode.await(),
            watchEntries = watchEntries.await(),
            hiddenIds = hiddenIds.await(),
        )
    }

    private fun feedCacheSignature(): String = FEED_CACHE_SIGNATURE_VERSION
}
