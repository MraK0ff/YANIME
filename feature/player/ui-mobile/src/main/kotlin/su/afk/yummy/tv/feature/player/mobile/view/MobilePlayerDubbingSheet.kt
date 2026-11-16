package su.afk.yummy.tv.feature.player.mobile.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.stringResource
import su.afk.yummy.tv.core.designsystem.baseScreen.BaseBottomSheet
import su.afk.yummy.tv.core.designsystem.baseScreen.HideSheetWindowSystemBars
import su.afk.yummy.tv.core.designsystem.utils.dubbingDisplayName
import su.afk.yummy.tv.core.designsystem.utils.dubbingLogoRes
import su.afk.yummy.tv.feature.player.model.PlayerPlaybackUiState
import su.afk.yummy.tv.feature.player.presentation.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text

/** Экран восстановления: выбор озвучки в общей системе поверхностей и строк плеера. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MobilePlayerDubbingSheet(
    playback: PlayerPlaybackUiState,
    onDubbingSelected: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    BaseBottomSheet(
        onDismissRequest = onDismiss,
        scrollableContent = true,
        dragHandle = {
            Box(Modifier.fillMaxWidth().padding(top = 12.dp, bottom = 4.dp), contentAlignment = Alignment.Center) {
                Text(stringResource(R.string.player_dubbing_title), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface)
            }
        },
    ) {
        HideSheetWindowSystemBars()
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            playback.dubbingNames.indices
                .filter { playback.dubbingAvailability.getOrElse(it) { true } }
                .forEach { index ->
                    val name = playback.dubbingNames[index]
                    MobilePlayerSelectionRow(
                        label = name.dubbingDisplayName(),
                        selected = index == playback.currentDubbingIndex,
                        enabled = true,
                        accentLabel = true,
                        logoRes = dubbingLogoRes(name),
                        logoFallbackIcon = Icons.Filled.Mic,
                        metaContent = { contentColor ->
                            MobilePlayerDubbingMeta(
                                views = playback.dubbingViews.getOrElse(index) { 0 },
                                episodeCount = playback.dubbingEpisodeCounts.getOrElse(index) { 0 },
                                sourceNames = playback.dubbingSourceNames.getOrElse(index) { "" },
                                contentColor = contentColor,
                            )
                        },
                        onClick = { onDubbingSelected(index) },
                    )
                }
        }
    }
}
