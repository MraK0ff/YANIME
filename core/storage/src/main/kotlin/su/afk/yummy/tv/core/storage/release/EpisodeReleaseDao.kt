package su.afk.yummy.tv.core.storage.release

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface EpisodeReleaseDao {
    @Query("SELECT * FROM episode_release_history ORDER BY animeId, episodeNumber, firstDetectedAtSeconds")
    suspend fun getAll(): List<EpisodeReleaseEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(entries: List<EpisodeReleaseEntry>)

    @Query("DELETE FROM episode_release_history")
    suspend fun clear()
}
