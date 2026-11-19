package su.afk.yummy.tv.feature.player.mobile.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.utils.dubbingDisplayName
import su.afk.yummy.tv.core.designsystem.utils.dubbingLogoRes
import su.afk.yummy.tv.feature.player.presentation.R

/** Единая страница выбора: озвучка и балансер показаны вместе, без переключения вкладок. */
@Composable
internal fun MobilePlayerSourcesPage(
    dubbingNames: List<String>,
    dubbingEpisodeCounts: List<Int>,
    dubbingViews: List<Int>,
    dubbingAvailability: List<Boolean>,
    selectedDubbingIndex: Int,
    onDubbingSelected: (Int) -> Unit,
    balancerNames: List<String>,
    balancerAvailability: List<Boolean>,
    balancerExternal: List<Boolean>,
    selectedBalancerIndex: Int,
    onBalancerSelected: (Int) -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MobilePlayerMenuPageHeader(
            title = stringResource(R.string.player_mobile_track_settings_title),
            onBack = onBack,
        )

        Text(
            text = stringResource(R.string.player_dubbing_title),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 2.dp),
        )
        dubbingNames.forEachIndexed { index, rawName ->
            val enabled = dubbingAvailability.getOrElse(index) { true }
            MobilePlayerSelectionRow(
                label = rawName.dubbingDisplayName(),
                selected = index == selectedDubbingIndex,
                enabled = enabled,
                accentLabel = true,
                logoRes = dubbingLogoRes(rawName),
                logoFallbackIcon = Icons.Filled.Mic,
                metaContent = { contentColor ->
                    if (enabled) {
                        MobilePlayerDubbingMeta(
                            views = dubbingViews.getOrElse(index) { 0 },
                            episodeCount = dubbingEpisodeCounts.getOrElse(index) { 0 },
                            sourceNames = "",
                            contentColor = contentColor,
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.player_episode_unavailable),
                            style = MaterialTheme.typography.labelSmall,
                            color = contentColor.copy(alpha = 0.68f),
                        )
                    }
                },
                onClick = { onDubbingSelected(index) },
            )
        }

        Text(
            text = stringResource(R.string.player_balancer_name_prefix),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 8.dp, top = 10.dp, bottom = 2.dp),
        )
        balancerNames.forEachIndexed { index, name ->
            val enabled = balancerAvailability.getOrElse(index) { true }
            MobilePlayerSelectionRow(
                label = name,
                selected = index == selectedBalancerIndex,
                enabled = enabled,
                accentLabel = true,
                logoFallbackIcon = Icons.Filled.GraphicEq,
                metaContent = { contentColor ->
                    Text(
                        text = stringResource(
                            when {
                                !enabled -> R.string.player_episode_unavailable
                                balancerExternal.getOrElse(index) { false } -> R.string.player_balancer_external_meta
                                else -> R.string.player_balancer_meta
                            },
                        ),
                        style = MaterialTheme.typography.labelSmall,
                        color = if (enabled) contentColor else contentColor.copy(alpha = 0.68f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                },
                onClick = { onBalancerSelected(index) },
            )
        }
    }
}
