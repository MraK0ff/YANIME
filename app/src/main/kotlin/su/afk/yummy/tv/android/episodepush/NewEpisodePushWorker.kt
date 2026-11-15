package su.afk.yummy.tv.android.episodepush

import android.content.Context
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import su.afk.yummy.tv.core.common.coroutines.runSuspendCatching
import su.afk.yummy.tv.core.common.episode.episodeNumberOrNull
import su.afk.yummy.tv.core.common.widget.HomeScreenWidgetRefresher
import su.afk.yummy.tv.core.preferences.api.YaniAccountSettingsStore
import su.afk.yummy.tv.core.preferences.settings.EpisodePushSettingsStore
import su.afk.yummy.tv.domain.account.model.ProfileNotification
import su.afk.yummy.tv.domain.account.model.unreadBadgeCount
import su.afk.yummy.tv.domain.account.usecase.GetProfileNotificationsUseCase
import su.afk.yummy.tv.domain.account.usecase.ResolveNotificationAnimeIdUseCase
import su.afk.yummy.tv.domain.account.usecase.GetVideoSubscriptionsUseCase
import su.afk.yummy.tv.domain.anime.usecase.GetAnimeVideosUseCase
import su.afk.yummy.tv.domain.home.usecase.SelectLibraryEpisodePushesUseCase

/**
 * Раз в 3 часа ([NewEpisodePushScheduler], только пока пользователь вошёл в аккаунт) выполняет
 * две проверки подписок в фоне: забирает серверные уведомления профиля и сверяет подписанные
 * озвучки по `subscribed=true` из `/anime/{id}/videos`. Поэтому уведомления работают и при
 * закрытом приложении. История локальной проверки хранит последний номер серии на тайтл:
 * альтернативные озвучки, повторы и старые серии не создают повторных уведомлений.
 * Опрос идёт независимо от разрешения уведомлений, но системный пуш показывается только при
 * включённом переключателе и выданном разрешении. Проверка нескольких тайтлов ограничена
 * тремя параллельными сетевыми запросами, чтобы не перегружать сеть и сервер.
 */
@HiltWorker
class NewEpisodePushWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val accountSettingsStore: YaniAccountSettingsStore,
    private val episodePushSettingsStore: EpisodePushSettingsStore,
    private val getProfileNotifications: GetProfileNotificationsUseCase,
    private val resolveNotificationAnimeId: ResolveNotificationAnimeIdUseCase,
    private val getVideoSubscriptions: GetVideoSubscriptionsUseCase,
    private val getAnimeVideos: GetAnimeVideosUseCase,
    private val notificationService: NewEpisodePushNotificationService,
    private val widgetRefresher: HomeScreenWidgetRefresher,
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val userId = accountSettingsStore.yaniUserId.first()
        if (userId <= 0) return Result.success()

        return runSuspendCatching {
            val notifications = getProfileNotifications(limit = NOTIFICATIONS_PAGE_SIZE)
            // Тот же запрос обслуживает бейдж непрочитанных на главной — отдельного опроса
            // счётчиков ради него больше нет.
            accountSettingsStore.setYaniUnreadNotificationsCount(notifications.unreadBadgeCount())
            val newEpisodeNotifications = notifications.filter { it.isNewEpisode }
            val knownIds = episodePushSettingsStore.knownNotificationIds.first()

            val newOnes = if (BASELINE_NOTIFICATION_ID !in knownIds) {
                // Первый прогон: только базовая линия, без уведомлений о старых релизах.
                episodePushSettingsStore.addKnownNotificationIds(
                    newEpisodeNotifications.map(ProfileNotification::id).toSet() + BASELINE_NOTIFICATION_ID,
                )
                emptyList()
            } else {
                newEpisodeNotifications.filterNot { it.id in knownIds }
            }
            // Ленту опрашиваем всегда, но без разрешения пуш система всё равно выкинет — не тратим
            // сеть на resolveNotificationAnimeId. Id при этом помечаем известными, иначе после
            // выдачи разрешения прилетит бэклог.
            val pushEnabled = episodePushSettingsStore.enabled.first() &&
                NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()
            if (pushEnabled) {
                newOnes.forEach { notification -> pushNotification(notification) }
            }
            episodePushSettingsStore.addKnownNotificationIds(
                newOnes.map(ProfileNotification::id).toSet(),
            )
            // Локальная проверка подписок использует /anime/{id}/videos и поле subscribed,
            // а не неоднозначный список /lists/subs для определения конкретной озвучки.
            syncSubscribedEpisodes(userId, pushEnabled)
            // Вышла серия — подписи карточек виджета («Серия N») могли устареть.
            if (newOnes.isNotEmpty()) widgetRefresher.refresh()
        }.fold(
            onSuccess = { Result.success() },
            onFailure = { Result.retry() },
        )
    }

    /**
     * Проверяет все тайтлы из списка подписок. /lists/subs используется только для получения
     * anime_id, а конкретные подписанные озвучки определяются исключительно по subscribed=true
     * в /anime/{id}/videos. История хранит максимум серии на тайтл, поэтому альтернативная
     * озвучка той же серии не создаёт второе уведомление.
     */
    private suspend fun syncSubscribedEpisodes(userId: Int, pushEnabled: Boolean) {
        val subscriptions = getVideoSubscriptions(userId)
        val animeById = subscriptions.distinctBy { it.animeId }.associateBy { it.animeId }
        if (animeById.isEmpty()) {
            episodePushSettingsStore.setKnownSubscribedEpisodeNumbers(emptyMap())
            return
        }

        val previous = episodePushSettingsStore.knownSubscribedEpisodeNumbers.first()
        val nowSeconds = System.currentTimeMillis() / 1_000
        val recentPushes = episodePushSettingsStore.recentPushes.first()
        val notificationsAllowed = pushEnabled &&
            NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()
        val semaphore = Semaphore(3)

        val results = coroutineScope {
            animeById.keys.map { animeId ->
                async {
                    semaphore.withPermit {
                        try {
                            val videos = getAnimeVideos(animeId)
                            val latestEpisode = videos.asSequence()
                                .filter { it.isSubscribed }
                                .mapNotNull { video ->
                                    val number = video.episode.episodeNumberOrNull() ?: return@mapNotNull null
                                    if (number <= 0.0 || number % 1.0 != 0.0) null else number.toInt()
                                }
                                .maxOrNull()
                            SubscriptionPollResult(animeId, latestEpisode, failed = false)
                        } catch (cancelled: CancellationException) {
                            throw cancelled
                        } catch (_: Exception) {
                            SubscriptionPollResult(animeId, null, failed = true)
                        }
                    }
                }
            }.awaitAll()
        }

        val updated = previous.filterKeys { it in animeById }.toMutableMap()
        results.filterNot { it.failed }.forEach { result ->
            val latestEpisode = result.latestEpisode ?: return@forEach
            val oldEpisode = previous[result.animeId]
            updated[result.animeId] = maxOf(oldEpisode ?: latestEpisode, latestEpisode)

            if (oldEpisode != null && latestEpisode > oldEpisode && notificationsAllowed) {
                val lastPush = recentPushes[result.animeId] ?: Long.MIN_VALUE
                val dedupWindow = SelectLibraryEpisodePushesUseCase.DEDUP_WINDOW_SECONDS
                if (lastPush < nowSeconds - dedupWindow) {
                    val title = animeById[result.animeId]?.title.orEmpty()
                    notificationService.showSubscribedEpisode(
                        animeId = result.animeId,
                        title = title,
                        episodeNumber = latestEpisode,
                    )
                    episodePushSettingsStore.recordPush(result.animeId, nowSeconds)
                }
            }
        }
        // Успешно проверенные тайтлы обновляются; при сетевой ошибке сохраняем прежнее значение,
        // чтобы следующий запуск повторил проверку, а не потерял новую серию.
        episodePushSettingsStore.setKnownSubscribedEpisodeNumbers(updated)
        if (results.any { it.failed }) throw IllegalStateException("Some subscription feeds could not be refreshed")
    }

    private data class SubscriptionPollResult(
        val animeId: Int,
        val latestEpisode: Int?,
        val failed: Boolean,
    )

    private suspend fun pushNotification(notification: ProfileNotification) {
        val animeId = notification.animeSlug?.let { slug -> resolveNotificationAnimeId(slug) }
        val nowSeconds = System.currentTimeMillis() / 1_000
        if (animeId != null) {
            // Локальный воркер мог уже сообщить об этой серии — один пуш на тайтл за сутки.
            val lastPush = episodePushSettingsStore.recentPushes.first()[animeId]
            if (lastPush != null && lastPush >= nowSeconds - SelectLibraryEpisodePushesUseCase.DEDUP_WINDOW_SECONDS) {
                return
            }
        }
        notificationService.showNewEpisode(notification, animeId)
        if (animeId != null) episodePushSettingsStore.recordPush(animeId, nowSeconds)
    }

    private companion object {
        const val NOTIFICATIONS_PAGE_SIZE = 30
        // Separate marker: an empty first response must still count as an initialized baseline.
        const val BASELINE_NOTIFICATION_ID = Int.MIN_VALUE
    }
}
