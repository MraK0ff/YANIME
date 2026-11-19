package su.afk.yummy.tv.feature.player.mobile.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonGroupDefaults
import androidx.compose.material3.ToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.utils.dubbingDisplayName
import su.afk.yummy.tv.core.designsystem.utils.dubbingLogoRes
import su.afk.yummy.tv.feature.player.presentation.R

/**
 * У каждой озвучки своя карточка. Балансеры активной озвучки сразу показаны в сгруппированной
 * адаптивной панели; при выборе другой озвучки сначала открывается подтверждение балансера.
 */
@Composable
internal fun MobilePlayerSourcesPage(
    dubbingNames: List<String>,
    dubbingEpisodeCounts: List<Int>,
    dubbingViews: List<Int>,
    dubbingAvailability: List<Boolean>,
    selectedDubbingIndex: Int,
    dubbingBalancerNames: List<List<String>>,
    dubbingBalancerIndices: List<List<Int>>,
    selectedBalancerGraphIndex: Int,
    onDubbingBalancerSelected: (dubbingIndex: Int, balancerGraphIndex: Int) -> Unit,
    onBack: () -> Unit,
) {
    var pendingDubbingIndex by remember { mutableIntStateOf(-1) }
    val pendingNames = dubbingBalancerNames.getOrNull(pendingDubbingIndex).orEmpty()
    val pendingIndices = dubbingBalancerIndices.getOrNull(pendingDubbingIndex).orEmpty()

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        MobilePlayerMenuPageHeader(
            title = stringResource(R.string.player_mobile_track_settings_title),
            onBack = onBack,
        )
        dubbingNames.forEachIndexed { index, rawName ->
            val enabled = dubbingAvailability.getOrElse(index) { true }
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
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
                    onClick = {
                        if (enabled && index != selectedDubbingIndex) pendingDubbingIndex = index
                    },
                )
                if (index == selectedDubbingIndex) {
                    val names = dubbingBalancerNames.getOrNull(index).orEmpty()
                    val indices = dubbingBalancerIndices.getOrNull(index).orEmpty()
                    if (names.isNotEmpty()) {
                        FlowRow(
                            modifier = Modifier.fillMaxWidth().padding(start = 12.dp, end = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp),
                        ) {
                            names.forEachIndexed { optionIndex, name ->
                                ToggleButton(
                                    checked = indices.getOrNull(optionIndex) == selectedBalancerGraphIndex,
                                    onCheckedChange = { checked ->
                                        if (checked) indices.getOrNull(optionIndex)?.let {
                                            onDubbingBalancerSelected(index, it)
                                        }
                                    },
                                    shapes = when (optionIndex) {
                                        0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                        names.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                        else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                    },
                                ) {
                                    Text(name, maxLines = 1)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (pendingDubbingIndex >= 0) {
        AlertDialog(
            onDismissRequest = { pendingDubbingIndex = -1 },
            title = {
                Text(dubbingNames.getOrNull(pendingDubbingIndex).orEmpty().dubbingDisplayName())
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.player_balancer_name_prefix))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        pendingNames.forEachIndexed { index, name ->
                            ToggleButton(
                                checked = false,
                                onCheckedChange = { checked ->
                                    val graphIndex = pendingIndices.getOrNull(index)
                                    if (checked && graphIndex != null) {
                                        onDubbingBalancerSelected(pendingDubbingIndex, graphIndex)
                                        pendingDubbingIndex = -1
                                    }
                                },
                                shapes = when (index) {
                                    0 -> ButtonGroupDefaults.connectedLeadingButtonShapes()
                                    pendingNames.lastIndex -> ButtonGroupDefaults.connectedTrailingButtonShapes()
                                    else -> ButtonGroupDefaults.connectedMiddleButtonShapes()
                                },
                            ) {
                                Text(name, maxLines = 1)
                            }
                        }
                    }
                    if (pendingNames.isEmpty()) {
                        Text(stringResource(R.string.player_episode_unavailable))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { pendingDubbingIndex = -1 }) {
                    Text(stringResource(android.R.string.cancel))
                }
            },
        )
    }
}
