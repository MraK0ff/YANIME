package su.afk.yummy.tv.android.episodepush

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import su.afk.yummy.tv.core.preferences.settings.EpisodePushSettingsStore
import su.afk.yummy.tv.core.preferences.settings.TitleReleaseTrackingMode
import su.afk.yummy.tv.core.preferences.settings.TitleReleaseTrackingStore
import su.afk.yummy.tv.domain.account.model.SubscriptionKeys
import su.afk.yummy.tv.domain.anime.usecase.RefreshAnimeVideosUseCase

/**
 * Polls device-local title subscriptions even when the app process is not running.
 * Episode numbers are deduplicated across all dubbings and players, so the first matching upload
 * creates one notification and later alternate uploads of the same episode are silent.
 */
@HiltWorker
class TitleReleaseTrackingWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val trackingStore: TitleReleaseTrackingStore,
    private val refreshAnimeVideos: RefreshAnimeVideosUseCase,
    private val pushSettings: EpisodePushSettingsStore,
    private val notificationService: NewEpisodePushNotificationService,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val subscriptions = trackingStore.subscriptions.first()
        if (subscriptions.isEmpty()) return Result.success()

        val pushEnabled = pushSettings.enabled.first() &&
            NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()
        var hasFailures = false

        subscriptions.forEach { subscription ->
            try {
                val videos = refreshAnimeVideos(subscription.animeId)
                val matchingEpisodes = videos.asSequence()
                    .filter { video ->
                        val dubbingKey = SubscriptionKeys.dubbingKey(video.dubbing)
                        dubbingKey !in subscription.excludedDubbingKeys &&
                            when (subscription.mode) {
                                TitleReleaseTrackingMode.ALL -> true
                                TitleReleaseTrackingMode.DUBBING_ONLY -> !video.dubbing.isSubtitleLabel()
                                TitleReleaseTrackingMode.SUBTITLES_ONLY -> video.dubbing.isSubtitleLabel()
                            }
                    }
                    .mapNotNull { it.episode.toRegularEpisodeNumber() }
                    .toSet()

                if (!subscription.initialized) {
                    trackingStore.upsert(
                        subscription.copy(
                            knownEpisodeNumbers = matchingEpisodes,
                            initialized = true,
                        ),
                    )
                    continue
                }

                val newEpisodes = matchingEpisodes - subscription.knownEpisodeNumbers
                // Persist the deduplication keys before notifying: a process interruption after
                // notify() must not make WorkManager deliver the same episode again on retry.
                trackingStore.recordEpisodes(subscription.animeId, matchingEpisodes)
                if (pushEnabled) {
                    newEpisodes.sorted().forEach { episode ->
                        notificationService.showTrackedTitleEpisode(
                            animeId = subscription.animeId,
                            title = subscription.title,
                            episodeNumber = episode,
                        )
                    }
                }
            } catch (cancelled: kotlinx.coroutines.CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                hasFailures = true
            }
        }

        return if (hasFailures) Result.retry() else Result.success()
    }

    private fun String.isSubtitleLabel(): Boolean {
        val normalized = trim().lowercase().replace('ё', 'е')
        return normalized.contains("субтитр") ||
            normalized.contains("subtitle") ||
            normalized == "sub" ||
            normalized.startsWith("sub ")
    }

    private fun String.toRegularEpisodeNumber(): Int? {
        val match = EPISODE_NUMBER_PATTERN.matchEntire(trim()) ?: return null
        val whole = match.groupValues[1]
        val fraction = match.groupValues[2]
        val value = (if (fraction.isEmpty()) whole else "$whole.$fraction")
            .toBigDecimalOrNull() ?: return null
        if (value.stripTrailingZeros().scale() > 0) return null
        return runCatching { value.intValueExact() }.getOrNull()?.takeIf { it > 0 }
    }

    private companion object {
        val EPISODE_NUMBER_PATTERN = Regex(
            """^(\d+)(?:[.,](\d+))?\s*(?:серия|серии|эпизод|эп\.?|episode|ep\.?)?$""",
            RegexOption.IGNORE_CASE,
        )
    }
}
