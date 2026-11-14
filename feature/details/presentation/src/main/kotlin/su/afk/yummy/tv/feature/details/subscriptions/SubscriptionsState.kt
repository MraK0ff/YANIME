package su.afk.yummy.tv.feature.details.subscriptions

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.ImmutableSet
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import su.afk.yummy.tv.core.model.text.UiText
import su.afk.yummy.tv.core.mvi.UiEffect
import su.afk.yummy.tv.core.mvi.UiEvent
import su.afk.yummy.tv.core.mvi.UiState
import su.afk.yummy.tv.core.preferences.settings.TitleReleaseTrackingMode
import su.afk.yummy.tv.feature.details.details.model.SubscriptionOption

@Immutable
data class TitleReleaseDubbingOption(
    val key: String,
    val name: String,
    val isSubtitles: Boolean,
    val episodesCount: Int,
)

class SubscriptionsState {
    @Immutable
    data class State(
        val isLoading: Boolean = true,
        val error: UiText? = null,
        val subscriptions: ImmutableList<SubscriptionOption> = persistentListOf(),
        val isSignedIn: Boolean = false,
        val title: String = "",
        val isLocalTrackingLoading: Boolean = true,
        val isLocalTrackingEnabled: Boolean = false,
        val localTrackingMode: TitleReleaseTrackingMode = TitleReleaseTrackingMode.ALL,
        val availableDubbings: ImmutableList<TitleReleaseDubbingOption> = persistentListOf(),
        val excludedDubbingKeys: ImmutableSet<String> = persistentSetOf(),
        /** Системный запрос разрешения на уведомления уже показывали. */
        val notificationPermissionRequested: Boolean = false,
    ) : UiState

    sealed interface Event : UiEvent {
        data object BackSelected : Event
        data object RetrySelected : Event
        data class SubscriptionToggled(val key: String) : Event
        data class LocalTrackingToggled(val enabled: Boolean) : Event
        data class LocalTrackingModeSelected(val mode: TitleReleaseTrackingMode) : Event
        data class LocalDubbingExclusionToggled(val key: String, val excluded: Boolean) : Event
        data object NotificationPermissionRequested : Event
    }

    sealed interface Effect : UiEffect
}
