package su.afk.yummy.tv.feature.player.mobile.view

import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Audiotrack
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material.icons.filled.Bedtime
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BrightnessMedium
import androidx.compose.material.icons.filled.Cast
import androidx.compose.material3.Icon
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import su.afk.yummy.tv.core.designsystem.utils.dubbingDisplayName
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
        modifier = modifier.padding(top = topInset).size(buttonSize).clickable(onClick = onBack),
    ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(DesignSystemR.string.common_back),
            tint = Color.White, modifier = Modifier.size(24.dp))
    }
}

@Composable
internal fun MobilePlayerTopBar(
    title: String,
    episode: String,
    episodeTitle: String? = null,
    dubbing: String,
    playerName: String,
    offlinePlayback: Boolean = false,
    episodeFlags: EpisodeFlags?,
    qualityLabel: String,
    qualityOptions: List<String>,
    selectedQuality: String?,
    adaptiveQuality: Boolean,
    onAdaptiveQualityChange: (Boolean) -> Unit,
    onQualitySelected: (String) -> Unit,
    onAdaptiveLimitSelected: (String) -> Unit,
    onAdaptiveLimitBestSelected: () -> Unit,
    speedOptions: List<Float>,
    onSpeedSelected: (Float) -> Unit,
    titleStartPadding: Dp = 4.dp,
    onEpisodeClick: () -> Unit,
    onDubbingClick: () -> Unit,
    showAudioTracks: Boolean,
    showSubtitleTracks: Boolean,
    onMoreMenuItem: (MobilePlayerSettingsPage) -> Unit,
    onQualityClick: () -> Unit,
    selectedSpeed: Float,
    onSpeedClick: () -> Unit,
    onSpeedLongPress: () -> Unit,
    visible: Boolean,
    episodeExpanded: Boolean = false,
    dubbingExpanded: Boolean = false,
    onPopupVisibilityChange: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    if (!visible) return
    // All player actions open a page in the single settings sheet; no legacy popup menus.
    Box(
        modifier = modifier.fillMaxSize(),
    ) {
        Column(
            modifier = Modifier.align(Alignment.TopCenter).fillMaxWidth(0.94f).padding(top = 36.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            if (title.isNotBlank()) {
                Text(
                    title,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Clip,
                    textAlign = TextAlign.Center,
                    style = TextStyle(color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.SemiBold),
                    modifier = Modifier.fillMaxWidth(),
                )
            }
            if (episode.isNotBlank()) {
                Row(
                    modifier = Modifier.clickable(onClick = onEpisodeClick),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    Text(
                        buildString {
                            append(stringResource(R.string.player_mobile_episode_number, episode))
                            episodeTitle?.takeIf { it.isNotBlank() }?.let { append(" * ").append(it) }
                        },
                        color = Color.White.copy(alpha = 0.72f),
                        maxLines = 1,
                        style = TextStyle(color = Color.White.copy(alpha = 0.72f), fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                    )
                    Icon(
                        imageVector = if (episodeExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.72f),
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            MobilePlayerEpisodeFlagLabel(episodeFlags, Modifier.padding(top = 2.dp))
        }
        Row(
            Modifier.align(Alignment.BottomEnd).padding(end = 4.dp, bottom = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            if (!offlinePlayback && qualityOptions.isNotEmpty()) {
                TopBarQualityAction(qualityLabel, onQualityClick)
            }
            TopBarSpeedAction(selectedSpeed, onSpeedClick)
            TopBarAction(Icons.Filled.MoreVert, stringResource(R.string.player_mobile_actions)) {
                onMoreMenuItem(MobilePlayerSettingsPage.Root)
            }
        }
    }
}

@Composable
private fun TopBarQualityAction(label: String, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.widthIn(min = 72.dp, max = 112.dp)
            .size(height = mobilePlayerControlButtonSize(), width = 92.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Icon(Icons.Filled.HighQuality, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
            BasicText(
                text = label,
                style = TextStyle(color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                maxLines = 1,
                softWrap = false,
            )
        }
    }
}

@Composable
private fun TopBarSpeedAction(speed: Float, onClick: () -> Unit) {
    val label = if (speed % 1f == 0f) "${speed.toInt()}x" else "${speed}x"
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(width = 50.dp, height = mobilePlayerControlButtonSize())
            .clickable(onClick = onClick),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            Icon(Icons.Filled.Speed, contentDescription = null, tint = Color.White, modifier = Modifier.size(17.dp))
            BasicText(text = label,
                style = TextStyle(color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                maxLines = 1)
        }
    }
}

@Composable
private fun TopBarAction(icon: ImageVector, description: String, onClick: () -> Unit) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(width = 40.dp, height = mobilePlayerControlButtonSize()).clickable(onClick = onClick),
    ) {
        Icon(icon, description, tint = Color.White, modifier = Modifier.size(24.dp))
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
    Text(text, color = color, maxLines = 1, style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier.mobilePlayerGlassSurface().padding(horizontal = 8.dp, vertical = 3.dp))
}
