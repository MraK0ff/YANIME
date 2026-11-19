package su.afk.yummy.tv.feature.player.mobile.view

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.utils.dubbingDisplayName
import su.afk.yummy.tv.core.designsystem.utils.dubbingLogoRes
import su.afk.yummy.tv.feature.player.mobile.model.MobilePlayerTrackSettingsTab
import su.afk.yummy.tv.feature.player.presentation.R

/** Источники воспроизведения в общем меню: отдельные вкладки озвучки и балансеров. */
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
    val tabs = MobilePlayerTrackSettingsTab.entries
    var selectedTab by rememberSaveable { mutableStateOf(MobilePlayerTrackSettingsTab.Dubbing) }
    val isLandscape = LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE
    val dubbingTabLabel = stringResource(R.string.player_dubbing_title)
    val playerTabLabel = stringResource(R.string.player_name_prefix)
    val tabLabels: (MobilePlayerTrackSettingsTab) -> String = { tab ->
        when (tab) {
            MobilePlayerTrackSettingsTab.Dubbing -> dubbingTabLabel
            MobilePlayerTrackSettingsTab.Player -> playerTabLabel
        }
    }
    val tabsContent: @Composable (Modifier) -> Unit = { tabsModifier ->
        MobilePlayerTrackSettingsTabs(
            tabs = tabs,
            selectedTab = selectedTab,
            labelFor = tabLabels,
            onTabSelected = { selectedTab = it },
            compact = isLandscape,
            modifier = tabsModifier,
        )
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (isLandscape) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                MobilePlayerMenuPageHeader(
                    title = stringResource(R.string.player_mobile_track_settings_title),
                    onBack = onBack,
                    modifier = Modifier.weight(1f),
                )
                tabsContent(Modifier)
            }
        } else {
            MobilePlayerMenuPageHeader(
                title = stringResource(R.string.player_mobile_track_settings_title),
                onBack = onBack,
            )
            tabsContent(Modifier)
        }

        when (selectedTab) {
            MobilePlayerTrackSettingsTab.Dubbing -> {
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
            }

            MobilePlayerTrackSettingsTab.Player -> {
                Text(
                    text = stringResource(R.string.player_name_prefix),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 8.dp, top = 2.dp, bottom = 2.dp),
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
    }
}
