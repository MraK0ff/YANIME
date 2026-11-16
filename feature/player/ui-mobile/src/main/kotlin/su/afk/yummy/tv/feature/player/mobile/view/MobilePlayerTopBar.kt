package su.afk.yummy.tv.feature.player.mobile.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.model.anime.EpisodeFlags
import su.afk.yummy.tv.feature.player.mobile.model.MobilePlayerSettingsPage
import su.afk.yummy.tv.feature.player.presentation.R
import su.afk.yummy.tv.core.designsystem.R as DesignSystemR

@Composable
internal fun MobilePlayerBackButton(
    onBack: () -> Unit,
    visible: Boolean,
    buttonSize: Dp = 48.dp,
    topInset: Dp = 0.dp,
    modifier: Modifier = Modifier,
) {
    if (!visible) return
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.padding(top = topInset),
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(buttonSize),
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                stringResource(DesignSystemR.string.common_back),
                tint = Color.White,
                modifier = Modifier.size(24.dp),
            )
        }
    }
}

@Composable
internal fun MobilePlayerTopBar(
    title: String,
    episode: String,
    episodeTitle: String? = null,
    offlinePlayback: Boolean = false,
    episodeFlags: EpisodeFlags?,
    qualityLabel: String,
    qualityOptions: List<String>,
    onEpisodeClick: () -> Unit,
    onMoreMenuItem: (MobilePlayerSettingsPage) -> Unit,
    onQualityClick: () -> Unit,
    selectedSpeed: Float,
    onSpeedClick: () -> Unit,
    visible: Boolean,
    episodeExpanded: Boolean = false,
    modifier: Modifier = Modifier,
) {
    if (!visible) return
    Box(modifier = modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .height(164.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.Black.copy(alpha = 0.56f),
                            Color.Black.copy(alpha = 0.20f),
                            Color.Transparent,
                        ),
                    ),
                ),
        )

        // Controls sit directly over the video; no decorative button dock.
        Row(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 8.dp, end = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (!offlinePlayback && qualityOptions.isNotEmpty()) {
                TopBarQualityAction(qualityLabel, onQualityClick)
            }
            TopBarSpeedAction(selectedSpeed, onSpeedClick)
            TopBarAction(
                Icons.Filled.Settings,
                stringResource(R.string.player_mobile_playback_settings_title),
            ) {
                onMoreMenuItem(MobilePlayerSettingsPage.Root)
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(0.88f)
                .padding(top = 46.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            if (title.isNotBlank()) {
                Text(
                    text = title,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (episode.isNotBlank()) {
                Row(
                    modifier = Modifier
                        .heightIn(min = MobilePlayerMinimumTouchTarget)
                        .clickable(
                            role = Role.Button,
                            onClickLabel = stringResource(R.string.player_mobile_episode_number, episode),
                            onClick = onEpisodeClick,
                        )
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        text = buildString {
                            append(stringResource(R.string.player_mobile_episode_number, episode))
                            episodeTitle?.takeIf { it.isNotBlank() }?.let { append(" · ").append(it) }
                        },
                        color = Color.White.copy(alpha = 0.82f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelLarge,
                    )
                    Icon(
                        imageVector = if (episodeExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.78f),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            MobilePlayerEpisodeFlagLabel(episodeFlags, Modifier.padding(top = 1.dp))
        }
    }
}

@Composable
private fun TopBarQualityAction(label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .heightIn(min = MobilePlayerMinimumTouchTarget)
            .clickable(
                role = Role.Button,
                onClickLabel = stringResource(R.string.player_quality_title),
                onClick = onClick,
            )
            .padding(horizontal = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(Icons.Filled.HighQuality, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = Color.White,
            maxLines = 1,
            softWrap = false,
        )
    }
}

@Composable
private fun TopBarSpeedAction(speed: Float, onClick: () -> Unit) {
    val label = if (speed % 1f == 0f) "${speed.toInt()}x" else "${speed}x"
    Row(
        modifier = Modifier
            .heightIn(min = MobilePlayerMinimumTouchTarget)
            .clickable(
                role = Role.Button,
                onClickLabel = stringResource(R.string.player_speed_title),
                onClick = onClick,
            )
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Icon(Icons.Filled.Speed, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
            color = Color.White,
            maxLines = 1,
        )
    }
}

@Composable
private fun TopBarAction(icon: ImageVector, description: String, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(48.dp)
            .clickable(
                role = Role.Button,
                onClickLabel = description,
                onClick = onClick,
            ),
    ) {
        Icon(icon, description, tint = Color.White, modifier = Modifier.size(23.dp))
    }
}

private val FlagFillerColor = Color(0xFFFFC107)
private val FlagRecapColor = Color(0xFF90CAF9)

@Composable
private fun MobilePlayerEpisodeFlagLabel(flags: EpisodeFlags?, modifier: Modifier = Modifier) {
    val (text, color) = when {
        flags == null -> return
        flags.filler -> stringResource(R.string.player_episode_flag_filler) to FlagFillerColor
        flags.recap -> stringResource(R.string.player_episode_flag_recap) to FlagRecapColor
        else -> return
    }
    Text(
        text,
        color = color,
        maxLines = 1,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.padding(horizontal = 4.dp, vertical = 2.dp),
    )
}
