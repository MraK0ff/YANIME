package su.afk.yummy.tv.core.storage.home

/** Абстракция над локальным кэшем главной ленты — позволяет подменять реализацию в тестах. */
interface HomeFeedStorage {

    suspend fun getFeed(
        language: String,
        watchSignature: String = HOME_FEED_GENERIC_WATCH_SIGNATURE,
    ): HomeFeedCache?

    suspend fun saveFeed(cache: HomeFeedCache)

    suspend fun deleteContinueWatchingByAnimeId(animeId: Int)

    /** Reads the cached Home presentation projection, not canonical release facts. */
    suspend fun getNewReleaseHistory(): List<HomeNewReleaseEntry>

    /** Saves the derived Home presentation projection. */
    suspend fun saveNewReleaseHistory(entries: List<HomeNewReleaseEntry>)
}
