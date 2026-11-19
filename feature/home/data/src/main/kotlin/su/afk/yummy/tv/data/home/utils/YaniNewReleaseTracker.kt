package su.afk.yummy.tv.data.home.utils

import java.math.BigDecimal
import java.util.Locale
import su.afk.yummy.tv.core.storage.home.HomeNewReleaseEntry
import su.afk.yummy.tv.data.home.dto.YaniVideoDto

/**
 * Maintains an anime-scoped history of the newest known regular episode.
 *
 * A card moves only when its regular episode number increases. Reposts, duplicate feed rows,
 * alternate dubs/players and stale snapshots cannot promote a card a second time.
 */
internal class YaniNewReleaseTracker {

    fun merge(
        videos: List<YaniVideoDto>,
        history: List<HomeNewReleaseEntry>,
        now: Long,
        /** Non-null means a schedule snapshot was successfully read; missing ids are unknown. */
        airedEpisodeCounts: Map<Int, Int>? = null,
    ): List<HomeNewReleaseEntry> {
        if (videos.isEmpty()) return history.withoutAlreadyKnownEpisodes(airedEpisodeCounts)

        val candidates = videos.mapIndexedNotNull { index, video ->
            val animeId = video.animeId?.takeIf { it > 0 } ?: return@mapIndexedNotNull null
            val episodeNumber = video.episodeTitle.toRegularEpisodeNumber()
                ?: return@mapIndexedNotNull null
            val title = video.title.takeIf(String::isNotBlank) ?: return@mapIndexedNotNull null
            Candidate(index, animeId, episodeNumber, video, title)
        }.groupBy { it.animeId }

        if (candidates.isEmpty()) return history.withoutAlreadyAiredEpisodes(airedEpisodeCounts)

        val previousById = history.associateBy { it.animeId }
        val isInitialSnapshot = previousById.isEmpty()
        val merged = previousById.toMutableMap()

        candidates.forEach { (animeId, items) ->
            // The API order is not a reliable signal. Pick the highest episode, then use stable
            // metadata ordering so alternative uploads of that episode resolve deterministically.
            val newest = items.sortedWith(
                compareByDescending<Candidate> { it.episodeNumber }
                    .thenBy { it.video.dubTitle.orEmpty().lowercase(Locale.ROOT) }
                    .thenBy { it.video.playerTitle.orEmpty().lowercase(Locale.ROOT) }
                    .thenBy { it.video.videoId ?: Int.MAX_VALUE }
                    .thenBy { it.video.animeUrl.orEmpty() }
                    .thenBy { it.video.title }
                    .thenBy { it.video.description }
                    .thenBy { it.index },
            ).firstOrNull() ?: return@forEach

            val airedCount = airedEpisodeCounts?.get(animeId)
            // Without schedule confirmation, or when the feed episode is older than the
            // currently aired episode, do not create/promote a "new episode" card.
            if (airedEpisodeCounts != null && airedCount == null) return@forEach
            if (airedCount != null && newest.episodeNumber <= airedCount) return@forEach

            val previous = previousById[animeId]
            when {
                previous == null -> {
                    merged[animeId] = newest.toEntry(
                        now = now,
                        firstSeenAt = now,
                        lastUpdatedAt = if (isInitialSnapshot) 0L else now,
                        position = if (isInitialSnapshot) merged.size else -1,
                    )
                }
                previous.episodeNumber <= 0 -> {
                    // Upgrade from the old feed cache, which did not remember episode numbers.
                    // Establish a baseline without falsely reporting a new episode.
                    merged[animeId] = newest.toEntry(
                        now = now,
                        firstSeenAt = previous.firstSeenAt,
                        lastUpdatedAt = 0L,
                        position = previous.position,
                    )
                }
                newest.episodeNumber > maxOf(previous.episodeNumber, airedCount ?: 0) -> {
                    merged[animeId] = newest.toEntry(
                        now = now,
                        firstSeenAt = previous.firstSeenAt,
                        lastUpdatedAt = now,
                        position = previous.position,
                    )
                }
                else -> {
                    // Same episode (including another dub) or an older repost: keep the selected
                    // upload and ordering intact. Only record that the anime was seen in this poll.
                    merged[animeId] = previous.copy(lastDetectedAt = now)
                }
            }
        }

        return merged.values
            .filter { entry ->
                val airedCount = airedEpisodeCounts?.get(entry.animeId)
                airedCount == null || entry.episodeNumber > airedCount
            }
            .sortedWith(
            compareByDescending<HomeNewReleaseEntry> { it.lastUpdatedAt > 0L }
                .thenByDescending { it.lastUpdatedAt }
                .thenBy { it.position }
                .thenBy { it.animeId },
        ).mapIndexed { index, entry -> entry.copy(position = index) }
    }

    /**
     * Remove cards left behind by an older app version when the feed only observed an old dub.
     * Keep entries when the current episode count is unknown; absence of data is not proof of staleness.
     */
    private fun List<HomeNewReleaseEntry>.withoutAlreadyAiredEpisodes(
        airedEpisodeCounts: Map<Int, Int>?,
    ): List<HomeNewReleaseEntry> =
        filter { entry ->
            val airedCount = airedEpisodeCounts?.get(entry.animeId)
            airedCount == null || entry.episodeNumber >= airedCount
        }.sortedBy { it.position }

    private data class Candidate(
        val index: Int,
        val animeId: Int,
        val episodeNumber: Int,
        val video: YaniVideoDto,
        val title: String,
    ) {
        fun toEntry(
            now: Long,
            firstSeenAt: Long,
            lastUpdatedAt: Long,
            position: Int,
        ) = HomeNewReleaseEntry(
            animeId = animeId,
            title = title,
            animeUrl = video.animeUrl.orEmpty(),
            description = video.description,
            episodeNumber = episodeNumber,
            episodeTitle = video.episodeTitle.orEmpty(),
            videoId = video.videoId,
            dubTitle = video.dubTitle,
            playerTitle = video.playerTitle,
            posterSmallUrl = video.poster?.small,
            posterMediumUrl = video.poster?.medium,
            posterBigUrl = video.poster?.big,
            posterFullsizeUrl = video.poster?.fullsize,
            posterMegaUrl = video.poster?.mega,
            firstSeenAt = firstSeenAt,
            lastDetectedAt = now,
            lastUpdatedAt = lastUpdatedAt,
            position = position,
        )
    }
}

/** Only whole positive episode numbers are regular episodes; OVA/SP and fractional specials are ignored. */
internal fun String?.toRegularEpisodeNumber(): Int? {
    val value = this?.trim()?.takeIf(String::isNotEmpty) ?: return null
    val match = REGULAR_EPISODE_PATTERN.matchEntire(value) ?: return null
    val whole = match.groupValues[1]
    val fraction = match.groupValues[2]
    val decimal = (if (fraction.isEmpty()) whole else "$whole.$fraction")
        .toBigDecimalOrNull() ?: return null
    if (decimal.stripTrailingZeros().scale() > 0) return null
    return runCatching { decimal.intValueExact() }.getOrNull()?.takeIf { it > 0 }
}

private val REGULAR_EPISODE_PATTERN = Regex(
    """^(\d+)\s*(?:[.,]\s*(\d+))?\s*(?:серия|серии|эпизод|эп\.?|episode|ep\.?)?$""",
    RegexOption.IGNORE_CASE,
)
