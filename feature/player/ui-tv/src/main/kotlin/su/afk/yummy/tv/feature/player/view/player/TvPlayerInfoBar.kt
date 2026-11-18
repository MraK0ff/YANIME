package su.afk.yummy.tv.feature.player.view.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.theme.overline
import su.afk.yummy.tv.core.designsystem.utils.dubbingDisplayName
import su.afk.yummy.tv.core.model.anime.EpisodeFlags
import su.afk.yummy.tv.feature.player.presentation.R

@Composable
internal fun TvPlayerInfoBar(
    visible: Boolean,
    animeTitle: String,
    episode: String,
    dubbing: String,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = visible && (animeTitle.isNotBlank() || episode.isNotBlank()),
        modifier = modifier
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(top = 24.dp, start = 48.dp, end = 48.dp),
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            if (animeTitle.isNotBlank()) {
                Text(
                    text = animeTitle,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                )
            }
            if (episode.isNotBlank()) {
                Text(
                    text = stringResource(R.string.player_episode_number, episode),
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White.copy(alpha = 0.82f),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
internal fun TvPlayerNameBadge(
    visible: Boolean,
    playerName: String,
    episodeFlags: EpisodeFlags?,
    modifier: Modifier = Modifier,
) {
    val fillerColor = Color(0xFFFFC107)
    val recapColor = Color(0xFF90CAF9)
    val flagLabel: Pair<String, Color>? = when {
        episodeFlags == null -> null
        episodeFlags.filler -> Pair(stringResource(R.string.player_episode_flag_filler), fillerColor)
        episodeFlags.recap -> Pair(stringResource(R.string.player_episode_flag_recap), recapColor)
        else -> null
    }
    AnimatedVisibility(
        visible = visible && flagLabel != null,
        modifier = modifier
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(top = 16.dp, end = 16.dp),
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            if (flagLabel != null) {
                Text(
                    text = flagLabel.first,
                    style = MaterialTheme.typography.labelMedium,
                    color = flagLabel.second,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(Color.Black.copy(alpha = 0.72f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                )
            }
        }
    }
}
