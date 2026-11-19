package su.afk.yummy.tv.domain.schedule.repository

import su.afk.yummy.tv.domain.schedule.model.AnimeEpisodeRelease
import su.afk.yummy.tv.domain.schedule.model.AnimeScheduleDay

interface AnimeScheduleRepository {
    suspend fun getSchedule(): List<AnimeScheduleDay>

    /** Returns the canonical, source-aware local history used by all release projections. */
    suspend fun getEpisodeReleaseHistory(): List<AnimeEpisodeRelease>

    /** Saves feed observations without replacing the first publication timestamps. */
    suspend fun saveEpisodeReleases(releases: List<AnimeEpisodeRelease>)
}
