package su.afk.yummy.tv.android.outbox

import io.ktor.client.plugins.ResponseException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import su.afk.yummy.tv.core.common.coroutines.runSuspendCatching
import su.afk.yummy.tv.core.model.error.isNetworkError
import su.afk.yummy.tv.core.model.mutation.PendingMutationFlusher
import su.afk.yummy.tv.core.storage.outbox.AnimeIdPayload
import su.afk.yummy.tv.core.storage.outbox.MarkWatchedPayload
import su.afk.yummy.tv.core.storage.outbox.NotificationIdPayload
import su.afk.yummy.tv.core.storage.outbox.PendingMutationEntry
import su.afk.yummy.tv.core.storage.outbox.PendingMutationOutbox
import su.afk.yummy.tv.core.storage.outbox.PendingMutationTypes
import su.afk.yummy.tv.core.storage.outbox.RemoveWatchedPayload
import su.afk.yummy.tv.core.storage.outbox.SetFavoritePayload
import su.afk.yummy.tv.core.storage.outbox.SetListPayload
import su.afk.yummy.tv.core.storage.outbox.SetRatingPayload
import su.afk.yummy.tv.core.storage.outbox.VoteReviewPayload
import su.afk.yummy.tv.domain.account.model.UserAnimeList
import su.afk.yummy.tv.domain.account.usecase.DeleteAllNotificationsUseCase
import su.afk.yummy.tv.domain.account.usecase.DeleteAnimeRatingUseCase
import su.afk.yummy.tv.domain.account.usecase.DeleteNotificationUseCase
import su.afk.yummy.tv.domain.account.usecase.MarkAllNotificationsReadUseCase
import su.afk.yummy.tv.domain.account.usecase.MarkNotificationReadUseCase
import su.afk.yummy.tv.domain.account.usecase.RemoveAnimeListUseCase
import su.afk.yummy.tv.domain.account.usecase.RemoveWatchedVideosUseCase
import su.afk.yummy.tv.domain.account.usecase.SaveVideoWatchProgressUseCase
import su.afk.yummy.tv.domain.account.usecase.SetAnimeFavoriteUseCase
import su.afk.yummy.tv.domain.account.usecase.SetAnimeListUseCase
import su.afk.yummy.tv.domain.account.usecase.SetAnimeRatingUseCase
import su.afk.yummy.tv.domain.reviews.model.ReviewVote
import su.afk.yummy.tv.domain.reviews.usecase.VoteReviewUseCase
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Дожимает мутации из durable outbox после сетевых сбоев. Общий runner сохраняет исходный порядок
 * операций между просмотром, библиотекой, оценками, голосами и уведомлениями.
 */
@Singleton
class PendingMutationFlushRunner @Inject constructor(
    private val outbox: PendingMutationOutbox,
    private val saveVideoWatchProgress: SaveVideoWatchProgressUseCase,
    private val removeWatchedVideos: RemoveWatchedVideosUseCase,
    private val setAnimeList: SetAnimeListUseCase,
    private val removeAnimeList: RemoveAnimeListUseCase,
    private val setAnimeFavorite: SetAnimeFavoriteUseCase,
    private val setAnimeRating: SetAnimeRatingUseCase,
    private val deleteAnimeRating: DeleteAnimeRatingUseCase,
    private val voteReview: VoteReviewUseCase,
    private val markNotificationRead: MarkNotificationReadUseCase,
    private val markAllNotificationsRead: MarkAllNotificationsReadUseCase,
    private val deleteNotification: DeleteNotificationUseCase,
    private val deleteAllNotifications: DeleteAllNotificationsUseCase,
) : PendingMutationFlusher {

    override suspend fun flush(): Boolean = flushLock.withLock { flushLocked() }

    private suspend fun flushLocked(): Boolean {
        for (entry in outbox.pending()) {
            val applied = runSuspendCatching { apply(entry) }
            val error = applied.exceptionOrNull()
            when {
                applied.isSuccess -> outbox.remove(entry.id)
                error.isPermanentRejection() -> outbox.remove(entry.id)
                error?.isNetworkError() == true -> return false
                entry.attemptCount + 1 >= MAX_ATTEMPTS -> outbox.remove(entry.id)
                else -> {
                    outbox.recordAttemptFailure(entry.id)
                    return false
                }
            }
        }
        return true
    }

    private fun Throwable?.isPermanentRejection(): Boolean {
        val status = (this as? ResponseException)?.response?.status?.value ?: return false
        return status in 400..499 && status !in RETRYABLE_CLIENT_STATUSES
    }

    private suspend fun apply(entry: PendingMutationEntry) {
        when (entry.type) {
            PendingMutationTypes.MARK_WATCHED -> {
                val payload = MarkWatchedPayload.decode(entry.payloadJson)
                saveVideoWatchProgress(
                    videoId = payload.videoId,
                    timeSeconds = payload.timeSeconds,
                    durationSeconds = payload.durationSeconds,
                    times = payload.times,
                )
            }
            PendingMutationTypes.REMOVE_WATCHED ->
                removeWatchedVideos(RemoveWatchedPayload.decode(entry.payloadJson).videoIds)
            PendingMutationTypes.SET_LIST -> {
                val payload = SetListPayload.decode(entry.payloadJson)
                val list = UserAnimeList.entries.first { it.id == payload.listId }
                setAnimeList(payload.animeId, list)
            }
            PendingMutationTypes.REMOVE_LIST ->
                removeAnimeList(AnimeIdPayload.decode(entry.payloadJson).animeId)
            PendingMutationTypes.SET_FAVORITE -> {
                val payload = SetFavoritePayload.decode(entry.payloadJson)
                setAnimeFavorite(payload.animeId, payload.favorite)
            }
            PendingMutationTypes.SET_RATING -> {
                val payload = SetRatingPayload.decode(entry.payloadJson)
                setAnimeRating(payload.animeId, payload.rating)
            }
            PendingMutationTypes.DELETE_RATING ->
                deleteAnimeRating(AnimeIdPayload.decode(entry.payloadJson).animeId)
            PendingMutationTypes.VOTE_REVIEW -> {
                val payload = VoteReviewPayload.decode(entry.payloadJson)
                val vote = ReviewVote.entries.first { it.apiValue == payload.voteApiValue }
                voteReview(payload.reviewId, vote)
            }
            PendingMutationTypes.MARK_NOTIFICATION_READ ->
                markNotificationRead(NotificationIdPayload.decode(entry.payloadJson).notificationId)
            PendingMutationTypes.MARK_ALL_NOTIFICATIONS_READ ->
                markAllNotificationsRead()
            PendingMutationTypes.DELETE_NOTIFICATION ->
                deleteNotification(NotificationIdPayload.decode(entry.payloadJson).notificationId)
            PendingMutationTypes.DELETE_ALL_NOTIFICATIONS ->
                deleteAllNotifications()
            else -> Unit
        }
    }

    private companion object {
        const val MAX_ATTEMPTS = 8
        val RETRYABLE_CLIENT_STATUSES = setOf(401, 408, 425, 429)
        val flushLock = Mutex()
    }
}
