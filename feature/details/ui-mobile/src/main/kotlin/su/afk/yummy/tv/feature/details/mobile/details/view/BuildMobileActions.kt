package su.afk.yummy.tv.feature.details.mobile.details.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import su.afk.yummy.tv.core.model.settings.DetailsButtonAction
import su.afk.yummy.tv.feature.details.details.DetailsState
import su.afk.yummy.tv.feature.details.mobile.details.model.MobileDetailsAction
import su.afk.yummy.tv.feature.details.presentation.R

@Composable
internal fun buildMobileActions(
    state: DetailsState.State,
    onSubscriptionsSelected: () -> Unit,
    onSubscriptionsLockedSelected: () -> Unit,
    onEpisodesSelected: () -> Unit,
): List<MobileDetailsAction> {
    // Трейлеры, видео блогеров, рейтинг и скриншоты на мобилке показаны блоками прямо в деталке.
    val availableActions = buildList {
        add(
            MobileDetailsAction(
                DetailsButtonAction.EPISODES,
                stringResource(R.string.details_episodes),
                stringResource(R.string.details_mobile_episodes_hint),
                Icons.Filled.VideoLibrary,
                onEpisodesSelected,
            ),
        )
        add(
            MobileDetailsAction(
                DetailsButtonAction.SUBSCRIPTIONS,
                stringResource(R.string.details_subscriptions),
                stringResource(R.string.details_mobile_subscriptions_hint),
                Icons.Filled.Notifications,
                onSubscriptionsSelected,
            ),
        )
    }
    val byAction = availableActions.associateBy { it.action }
    return DetailsButtonAction.defaultOrder
        .filterNot { it == DetailsButtonAction.WATCH || it == DetailsButtonAction.LIBRARY || it == DetailsButtonAction.FAVORITE }
        .mapNotNull { byAction[it] }
}
