package su.afk.yummy.tv.feature.details.mobile.subscriptions

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.io.IOException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import su.afk.yummy.tv.core.designsystem.baseScreen.BaseBottomSheet
import su.afk.yummy.tv.core.designsystem.components.YummyLoadingIndicator
import su.afk.yummy.tv.core.designsystem.error.asString
import su.afk.yummy.tv.core.designsystem.mobile.NotificationPermissionGateHost
import su.afk.yummy.tv.core.designsystem.mobile.rememberNotificationPermissionGate
import su.afk.yummy.tv.core.designsystem.mobile.layout.mobileContentMaxWidth
import su.afk.yummy.tv.core.designsystem.mobile.state.MobileMessage
import su.afk.yummy.tv.core.designsystem.preview.ScreenPreviewTheme
import su.afk.yummy.tv.core.designsystem.utils.dubbingDisplayName
import su.afk.yummy.tv.core.model.text.UiText
import su.afk.yummy.tv.core.preferences.settings.TitleReleaseTrackingMode
import su.afk.yummy.tv.feature.details.mobile.subscriptions.view.SubscriptionMobileRow
import su.afk.yummy.tv.feature.details.presentation.R
import su.afk.yummy.tv.feature.details.subscriptions.SubscriptionsState
import su.afk.yummy.tv.core.designsystem.R as CoreR

@OptIn(ExperimentalMaterial3Api::class)
@Preview(name = "Default", device = "spec:width=412dp,height=915dp,dpi=420", showBackground = true)
@Composable
private fun SubscriptionsMobileScreenDefaultPreview() = ScreenPreviewTheme {
    SubscriptionsMobileScreen(SubscriptionsState.State(isLoading = false, isLocalTrackingLoading = false), emptyFlow()) {}
}

@Composable
@Preview(name = "Loading", device = "spec:width=412dp,height=915dp,dpi=420", showBackground = true)
private fun SubscriptionsMobileScreenLoadingPreview() = ScreenPreviewTheme {
    SubscriptionsMobileScreen(SubscriptionsState.State(isLoading = true), emptyFlow()) {}
}

@Preview(name = "Error", device = "spec:width=412dp,height=915dp,dpi=420", showBackground = true)
@Composable
private fun SubscriptionsMobileScreenErrorPreview() = ScreenPreviewTheme {
    SubscriptionsMobileScreen(
        SubscriptionsState.State(isLoading = false, isSignedIn = true, error = UiText.Failure(IOException())),
        emptyFlow(),
    ) {}
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SubscriptionsMobileScreen(
    state: SubscriptionsState.State,
    effect: Flow<SubscriptionsState.Effect>,
    onEvent: (SubscriptionsState.Event) -> Unit,
) {
    val notificationPermissionGate = rememberNotificationPermissionGate()
    BaseBottomSheet(
        onDismissRequest = { onEvent(SubscriptionsState.Event.BackSelected) },
        title = stringResource(R.string.details_subscriptions),
    ) {
        LazyColumn(
            modifier = Modifier.mobileContentMaxWidth().fillMaxWidth(),
            contentPadding = PaddingValues(bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item(key = "local_title_tracking") {
                LocalTitleTrackingCard(
                    state = state,
                    onTrackingToggled = { enabled ->
                        val event = SubscriptionsState.Event.LocalTrackingToggled(enabled)
                        if (enabled) notificationPermissionGate { onEvent(event) } else onEvent(event)
                    },
                    onModeSelected = { onEvent(SubscriptionsState.Event.LocalTrackingModeSelected(it)) },
                    onDubbingExcluded = { key, excluded ->
                        onEvent(SubscriptionsState.Event.LocalDubbingExclusionToggled(key, excluded))
                    },
                )
            }

            item(key = "server_subscriptions_header") {
                Text(
                    text = stringResource(R.string.details_server_subscriptions_title),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }

            if (!state.isSignedIn) {
                item(key = "server_subscriptions_sign_in") {
                    Text(
                        text = stringResource(R.string.details_local_tracking_server_sign_in),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else if (state.isLoading && state.subscriptions.isEmpty()) {
                item(key = "server_subscriptions_loading") {
                    Column(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        YummyLoadingIndicator()
                    }
                }
            } else if (state.error != null && state.subscriptions.isEmpty()) {
                item(key = "server_subscriptions_error") {
                    MobileMessage(
                        title = state.error?.asString().orEmpty(),
                        icon = Icons.Filled.Warning,
                        actionLabel = stringResource(CoreR.string.common_retry),
                        onAction = { onEvent(SubscriptionsState.Event.RetrySelected) },
                        fillMaxSize = false,
                    )
                }
            } else if (state.subscriptions.isEmpty()) {
                item(key = "server_subscriptions_empty") {
                    Text(
                        text = stringResource(R.string.details_subscriptions_empty),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                items(state.subscriptions, key = { it.key }) { option ->
                    SubscriptionMobileRow(
                        option = option,
                        onClick = {
                            val toggle = SubscriptionsState.Event.SubscriptionToggled(option.key)
                            if (option.isSubscribed) onEvent(toggle)
                            else notificationPermissionGate { onEvent(toggle) }
                        },
                    )
                }
            }
        }
    }

    NotificationPermissionGateHost(
        state = notificationPermissionGate,
        permissionWasRequested = state.notificationPermissionRequested,
        onPermissionRequested = { onEvent(SubscriptionsState.Event.NotificationPermissionRequested) },
        explanationRes = CoreR.string.notification_permission_explanation_episode_push,
    )
}

@Composable
private fun LocalTitleTrackingCard(
    state: SubscriptionsState.State,
    onTrackingToggled: (Boolean) -> Unit,
    onModeSelected: (TitleReleaseTrackingMode) -> Unit,
    onDubbingExcluded: (String, Boolean) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                    Text(stringResource(R.string.details_local_tracking_title), style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = stringResource(R.string.details_local_tracking_description),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Switch(
                    checked = state.isLocalTrackingEnabled,
                    onCheckedChange = onTrackingToggled,
                    enabled = state.localTrackingAvailable || state.isLocalTrackingEnabled,
                )
            }

            if (!state.localTrackingAvailable && !state.isLocalTrackingEnabled) {
                Text(
                    text = stringResource(R.string.details_local_tracking_unavailable),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (state.isLocalTrackingEnabled) {
                Text(stringResource(R.string.details_local_tracking_enabled), style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    FilterChip(
                        selected = state.localTrackingMode == TitleReleaseTrackingMode.ALL,
                        onClick = { onModeSelected(TitleReleaseTrackingMode.ALL) },
                        label = { Text(stringResource(R.string.details_local_tracking_all)) },
                    )
                    FilterChip(
                        selected = state.localTrackingMode == TitleReleaseTrackingMode.DUBBING_ONLY,
                        onClick = { onModeSelected(TitleReleaseTrackingMode.DUBBING_ONLY) },
                        label = { Text(stringResource(R.string.details_local_tracking_dubbing)) },
                    )
                    FilterChip(
                        selected = state.localTrackingMode == TitleReleaseTrackingMode.SUBTITLES_ONLY,
                        onClick = { onModeSelected(TitleReleaseTrackingMode.SUBTITLES_ONLY) },
                        label = { Text(stringResource(R.string.details_local_tracking_subtitles)) },
                    )
                }

                Text(stringResource(R.string.details_local_tracking_exclusions), style = MaterialTheme.typography.labelLarge)
                if (state.availableDubbings.isEmpty()) {
                    Text(
                        text = stringResource(R.string.details_local_tracking_empty),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    state.availableDubbings.forEach { option ->
                        val modeAllowsOption = when (state.localTrackingMode) {
                            TitleReleaseTrackingMode.ALL -> true
                            TitleReleaseTrackingMode.DUBBING_ONLY -> !option.isSubtitles
                            TitleReleaseTrackingMode.SUBTITLES_ONLY -> option.isSubtitles
                        }
                        if (modeAllowsOption) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .toggleable(
                                        value = option.key in state.excludedDubbingKeys,
                                        role = Role.Checkbox,
                                        onValueChange = { onDubbingExcluded(option.key, it) },
                                    )
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(
                                    checked = option.key in state.excludedDubbingKeys,
                                    onCheckedChange = null,
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = option.name.dubbingDisplayName(),
                                        style = MaterialTheme.typography.bodyMedium,
                                    )
                                    Text(
                                        text = stringResource(
                                            R.string.details_mobile_subscription_episode_count,
                                            option.episodesCount,
                                        ),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
