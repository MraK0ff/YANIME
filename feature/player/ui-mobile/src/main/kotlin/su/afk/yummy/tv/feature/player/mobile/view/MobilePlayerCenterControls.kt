package su.afk.yummy.tv.feature.player.mobile.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.feature.player.presentation.R

@Composable
internal fun MobilePlayerCenterControls(
    wantsPlay: Boolean,
    hasPrevEpisode: Boolean,
    hasNextEpisode: Boolean,
    onPlayPause: () -> Unit,
    onPrevEpisode: () -> Unit,
    onNextEpisode: () -> Unit,
    loading: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(CENTER_CONTROLS_GAP),
    ) {
        Box(Modifier.size(CENTER_SWITCH_SLOT), contentAlignment = Alignment.Center) {
            if (hasPrevEpisode) {
                MobilePlayerActionButton(
                    icon = Icons.Filled.SkipPrevious,
                    contentDescription = stringResource(R.string.player_mobile_previous_episode),
                    onClick = onPrevEpisode,
                    iconScale = 0.66f,
                    surface = true,
                )
            }
        }

        val playPauseDescription = stringResource(
            if (wantsPlay) R.string.player_mobile_pip_pause else R.string.player_mobile_pip_play,
        )
        MobilePlayerActionButton(
            icon = if (wantsPlay) Icons.Filled.Pause else Icons.Filled.PlayArrow,
            contentDescription = playPauseDescription,
            onClick = onPlayPause,
            primary = true,
            loading = loading,
            sizeMultiplier = 1.08f,
            iconScale = 0.72f,
        )

        Box(Modifier.size(CENTER_SWITCH_SLOT), contentAlignment = Alignment.Center) {
            if (hasNextEpisode) {
                MobilePlayerActionButton(
                    icon = Icons.Filled.SkipNext,
                    contentDescription = stringResource(R.string.player_mobile_next_episode),
                    onClick = onNextEpisode,
                    iconScale = 0.66f,
                    surface = true,
                )
            }
        }
    }
}

private val CENTER_CONTROLS_GAP = 10.dp
private val CENTER_SWITCH_SLOT = 64.dp
