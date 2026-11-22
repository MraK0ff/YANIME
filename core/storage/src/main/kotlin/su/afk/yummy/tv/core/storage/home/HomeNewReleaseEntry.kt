package su.afk.yummy.tv.core.storage.home

import androidx.room.Entity
import androidx.room.Index

/**
 * Durable per-anime snapshot of the latest regular episode observed in /feed.
 * The row survives feed pagination/rotation so cards do not disappear with one API response.
 */
@Entity(
    tableName = "home_new_release_history",
    primaryKeys = ["animeId"],
    indices = [Index(value = ["position"], name = "index_home_new_release_history_position")],
)
data class HomeNewReleaseEntry(
    val animeId: Int,
    val title: String,
    val animeUrl: String,
    val description: String,
    val episodeNumber: Int,
    val episodeTitle: String,
    val videoId: Int?,
    val dubTitle: String?,
    val playerTitle: String?,
    val posterSmallUrl: String?,
    val posterMediumUrl: String?,
    val posterBigUrl: String?,
    val posterFullsizeUrl: String?,
    val posterMegaUrl: String?,
    val firstSeenAt: Long,
    val lastDetectedAt: Long,
    /** Zero means the item was part of the initial baseline, not a newly discovered release. */
    val lastUpdatedAt: Long,
    val position: Int,
)
