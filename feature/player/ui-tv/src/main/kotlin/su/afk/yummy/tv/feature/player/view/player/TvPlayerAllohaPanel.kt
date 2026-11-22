package su.afk.yummy.tv.feature.player.view.player

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.res.stringResource
import su.afk.yummy.tv.feature.player.presentation.R

/**
 * Alloha audio tracks use the same compact selection surface and focus behavior as
 * dubbing, quality, and balancer pickers.
 */
@Composable
internal fun TvPlayerAllohaPanel(
    visible: Boolean,
    audioTrackNames: List<String>,
    selectedAudioTrackIndex: Int,
    onAudioTrackSelected: (index: Int) -> Unit,
    selectedFocusRequester: FocusRequester,
    modifier: Modifier = Modifier,
    onExitDown: (() -> Unit)? = null,
) {
    TvPlayerSelectionPanel(
        visible = visible,
        title = stringResource(R.string.player_dubbing_title),
        items = audioTrackNames,
        selectedIndex = selectedAudioTrackIndex,
        selectedFocusRequester = selectedFocusRequester,
        modifier = modifier,
        onItemSelected = onAudioTrackSelected,
        onExitDown = onExitDown,
    )
}
