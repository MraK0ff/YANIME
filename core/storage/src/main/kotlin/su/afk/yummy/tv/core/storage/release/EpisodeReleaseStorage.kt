package su.afk.yummy.tv.core.storage.release

/** Single local source of truth for observed episode and dubbing publication events. */
interface EpisodeReleaseStorage {
    suspend fun getAll(): List<EpisodeReleaseEntry>
    suspend fun upsertAll(entries: List<EpisodeReleaseEntry>)
}
