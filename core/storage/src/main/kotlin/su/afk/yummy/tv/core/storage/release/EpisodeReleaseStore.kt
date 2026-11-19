package su.afk.yummy.tv.core.storage.release

internal class EpisodeReleaseStore(
    private val dao: EpisodeReleaseDao,
) : EpisodeReleaseStorage {
    override suspend fun getAll(): List<EpisodeReleaseEntry> = dao.getAll()

    override suspend fun upsertAll(entries: List<EpisodeReleaseEntry>) {
        if (entries.isNotEmpty()) dao.upsertAll(entries)
    }
}
