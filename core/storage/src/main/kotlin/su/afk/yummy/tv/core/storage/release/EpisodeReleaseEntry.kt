package su.afk.yummy.tv.core.storage.release

import androidx.room.Entity
import androidx.room.Index

/**
 * Canonical local observation of an episode publication in YummyAnime.
 * Episode identity excludes player/dubbing; each source publication is a separate row.
 */
@Entity(
    tableName = "episode_release_history",
    primaryKeys = ["animeId", "episodeNumber", "playerKey", "dubbingKey"],
    indices = [
        Index(value = ["animeId", "episodeNumber"], name = "index_episode_release_history_episode"),
        Index(value = ["firstDetectedAtSeconds"], name = "index_episode_release_history_detected"),
    ],
)
data class EpisodeReleaseEntry(
    val animeId: Int,
    val episodeNumber: Int,
    val playerKey: String,
    val dubbingKey: String,
    val playerName: String,
    val dubbing: String,
    val firstVideoAtSeconds: Long,
    val firstVideoId: Int,
    val title: String,
    val posterUrl: String?,
    val firstDetectedAtSeconds: Long,
)
