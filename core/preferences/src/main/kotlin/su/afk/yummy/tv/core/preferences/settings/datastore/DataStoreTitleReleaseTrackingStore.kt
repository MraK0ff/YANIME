package su.afk.yummy.tv.core.preferences.settings.datastore

import android.util.Base64\nimport kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import su.afk.yummy.tv.core.preferences.settings.SettingsPreferenceKeys.titleReleaseTrackingKey
import su.afk.yummy.tv.core.preferences.settings.TitleReleaseTracking
import su.afk.yummy.tv.core.preferences.settings.TitleReleaseTrackingMode
import su.afk.yummy.tv.core.preferences.settings.TitleReleaseTrackingStore
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DataStoreTitleReleaseTrackingStore @Inject constructor(
    private val store: SettingsDataStore,
) : TitleReleaseTrackingStore {

    override val subscriptions: Flow<List<TitleReleaseTracking>> =
        store.stringSet(titleReleaseTrackingKey).map { entries ->
            entries.mapNotNull { decode(it) }.sortedBy { it.title.lowercase() }
        }

    override suspend fun upsert(subscription: TitleReleaseTracking) {
        store.edit { prefs ->
            val current = prefs[titleReleaseTrackingKey].orEmpty()
                .mapNotNull { decode(it) }.associateBy { it.animeId }.toMutableMap()
            current[subscription.animeId] = subscription.copy(
                knownEpisodeNumbers = subscription.knownEpisodeNumbers
                    .filter { it > 0 }.toSortedSet().toList().takeLast(MAX_KNOWN_EPISODES).toSet(),
                excludedDubbingKeys = subscription.excludedDubbingKeys.filter { it.isNotBlank() }.toSet(),
            )
            prefs[titleReleaseTrackingKey] = current.values.mapTo(mutableSetOf()) { encode(it) }
        }
    }

    override suspend fun remove(animeId: Int) {
        store.edit { prefs ->
            prefs[titleReleaseTrackingKey] = prefs[titleReleaseTrackingKey].orEmpty()
                .filterNot { it.substringBefore('|').toIntOrNull() == animeId }.toSet()
        }
    }

    override suspend fun recordEpisodes(animeId: Int, episodeNumbers: Set<Int>) {
        if (episodeNumbers.isEmpty()) return
        store.edit { prefs ->
            val current = prefs[titleReleaseTrackingKey].orEmpty()
                .mapNotNull { decode(it) }.associateBy { it.animeId }.toMutableMap()
            val subscription = current[animeId] ?: return@edit
            current[animeId] = subscription.copy(
                knownEpisodeNumbers = (subscription.knownEpisodeNumbers + episodeNumbers)
                    .filter { it > 0 }.toSortedSet().toList().takeLast(MAX_KNOWN_EPISODES).toSet(),
                initialized = true,
            )
            prefs[titleReleaseTrackingKey] = current.values.mapTo(mutableSetOf()) { encode(it) }
        }
    }

    private fun encode(item: TitleReleaseTracking): String = listOf(
        item.animeId.toString(),
        Base64.encodeToString(item.title.toByteArray(Charsets.UTF_8), Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING),
        item.mode.name,
        if (item.initialized) "1" else "0",
        item.knownEpisodeNumbers.sorted().joinToString(","),
        item.excludedDubbingKeys.sorted().joinToString(","),
    ).joinToString("|")

    private fun decode(raw: String): TitleReleaseTracking? {
        val parts = raw.split('|', limit = 6)
        if (parts.size != 6) return null
        val animeId = parts[0].toIntOrNull()?.takeIf { it > 0 } ?: return null
        val mode = runCatching { TitleReleaseTrackingMode.valueOf(parts[2]) }
            .getOrDefault(TitleReleaseTrackingMode.ALL)
        val title = runCatching {
            String(Base64.decode(parts[1], Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING), Charsets.UTF_8)
        }.getOrDefault("")
        return TitleReleaseTracking(
            animeId = animeId,
            title = title,
            mode = mode,
            initialized = parts[3] == "1",
            knownEpisodeNumbers = parts[4].split(',').mapNotNull { it.toIntOrNull() }.filter { it > 0 }.toSet(),
            excludedDubbingKeys = parts[5].split(',').filter { it.isNotBlank() }.toSet(),
        )
    }

    private companion object {
        const val MAX_KNOWN_EPISODES = 80
    }
}
