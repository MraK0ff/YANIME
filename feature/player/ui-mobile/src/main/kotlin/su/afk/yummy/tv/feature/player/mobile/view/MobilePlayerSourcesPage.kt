package su.afk.yummy.tv.feature.player.mobile.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.utils.dubbingDisplayName
import su.afk.yummy.tv.core.designsystem.utils.dubbingLogoRes
import su.afk.yummy.tv.feature.player.presentation.R

/** Источники воспроизведения как страница общего меню, без отдельной шторки. */
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
    val availableDubbingIndices = remember(dubbingNames, dubbingAvailability) {
        dubbingNames.indices.filter { dubbingAvailability.getOrElse(it) { true } }
    }
    val availableBalancerIndices = remember(balancerNames, balancerAvailability) {
        balancerNames.indices.filter { balancerAvailability.getOrElse(it) { true } }
    }

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
        availableDubbingIndices.forEach { index ->
            val name = dubbingNames[index]
            val selected = index == selectedDubbingIndex
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                MobilePlayerSelectionRow(
                    label = name.dubbingDisplayName(),
                    selected = selected,
                    enabled = true,
                    accentLabel = true,
                    logoRes = dubbingLogoRes(name),
                    logoFallbackIcon = Icons.Filled.Mic,
                    metaContent = { contentColor ->
                        MobilePlayerDubbingMeta(
                            views = dubbingViews.getOrElse(index) { 0 },
                            episodeCount = dubbingEpisodeCounts.getOrElse(index) { 0 },
                            sourceNames = "",
                            contentColor = contentColor,
                        )
                    },
                    onClick = { onDubbingSelected(index) },
                )
                if (selected && availableBalancerIndices.isNotEmpty()) {
                    Text(
                        text = stringResource(R.string.player_balancer_name_prefix),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 44.dp, top = 2.dp),
                    )
                    Column(
                        modifier = Modifier.padding(start = 36.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                    ) {
                        availableBalancerIndices.forEach { balancerIndex ->
                            val balancerName = balancerNames[balancerIndex]
                            MobilePlayerSelectionRow(
                                label = balancerName,
                                selected = balancerIndex == selectedBalancerIndex,
                                enabled = true,
                                accentLabel = true,
                                metaContent = { contentColor ->
                                    Text(
                                        text = stringResource(
                                            if (balancerExternal.getOrElse(balancerIndex) { false }) {
                                                R.string.player_balancer_external_meta
                                            } else {
                                                R.string.player_balancer_meta
                                            },
                                        ),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = contentColor,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )
                                },
                                onClick = { onBalancerSelected(balancerIndex) },
                            )
                        }
                    }
                }
            }
        }
    }
}
