package su.afk.yummy.tv.domain.schedule.repository

import su.afk.yummy.tv.domain.schedule.model.AnimeEpisodeRelease
import su.afk.yummy.tv.domain.schedule.model.AnimeScheduleDay

interface AnimeScheduleRepository {
    suspend fun getSchedule(): List<AnimeScheduleDay>

    /** Сохраняет новые наблюдения ленты; история первой публикации не перезаписывается. */
    /** Returns the canonical, source-aware local history used by all release projections. */
    suspend fun getEpisodeReleaseHistory(): List<AnimeEpisodeRelease>

    suspend fun saveEpisodeReleases(releases: List<AnimeEpisodeRelease>)
}
