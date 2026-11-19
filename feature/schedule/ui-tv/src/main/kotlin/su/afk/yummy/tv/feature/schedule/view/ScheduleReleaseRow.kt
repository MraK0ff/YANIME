package su.afk.yummy.tv.feature.schedule.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import su.afk.yummy.tv.core.designsystem.components.FallbackAsyncImage
import su.afk.yummy.tv.core.designsystem.components.ListStatusBadge
import su.afk.yummy.tv.core.designsystem.components.badgeColor
import su.afk.yummy.tv.core.designsystem.locals.LocalLibraryListStatuses
import su.afk.yummy.tv.core.designsystem.theme.YummyShapeTokens
import su.afk.yummy.tv.core.model.anime.posterUrls
import su.afk.yummy.tv.feature.schedule.model.ScheduleReleaseUi
import su.afk.yummy.tv.feature.schedule.presentation.R
import su.afk.yummy.tv.feature.schedule.utils.remainingText
import su.afk.yummy.tv.feature.schedule.utils.timeLabel
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@Composable
internal fun ScheduleReleaseRow(
    release: ScheduleReleaseUi,
    now: ZonedDateTime,
    zone: ZoneId,
    focusRequester: FocusRequester,
    leftFocusRequester: FocusRequester?,
    upFocusRequester: FocusRequester?,
    selected: Boolean,
    onFocused: () -> Unit,
    onClick: () -> Unit,
) {
    val item = release.item
    val releaseAt = Instant.ofEpochSecond(release.epochSeconds).atZone(zone)
    val aired = releaseAt.isAfter(now).not()
    val remainingLabels = scheduleRemainingLabels()
    val listStatus = LocalLibraryListStatuses.current[item.animeId]
    val accentColor = if (aired && listStatus == null) AiredColor else MaterialTheme.colorScheme.onSurfaceVariant
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val active = focused || selected
    val shape = YummyShapeTokens.Card
    val rowScale = if (focused) 1.025f else 1f
    val rowBackground = when {
        focused || selected -> MaterialTheme.colorScheme.surfaceContainerHigh
        listStatus != null -> listStatus.badgeColor().copy(alpha = 0.12f)
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(126.dp)
            .zIndex(if (focused) 1f else 0f)
            .focusRequester(focusRequester)
            .focusProperties {
                leftFocusRequester?.let { left = it }
                upFocusRequester?.let { up = it }
            }
            .onPreviewKeyEvent { event ->
                if (event.type != KeyEventType.KeyDown || event.key != Key.DirectionLeft) {
                    return@onPreviewKeyEvent false
                }
                leftFocusRequester?.requestFocus() == true
            }
            .onFocusChanged { if (it.isFocused) onFocused() }
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
    ) {
        Box(
            modifier = Modifier
                .matchParentSize()
                .graphicsLayer {
                    scaleX = rowScale
                    scaleY = rowScale
                }
                .clip(shape)
                .background(rowBackground, shape)
                .border(
                    width = if (active) 2.dp else 0.dp,
                    color = if (active) MaterialTheme.colorScheme.primary else Color.Transparent,
                    shape = shape,
                ),
        )
        Row(
            modifier = Modifier
                .matchParentSize()
                .padding(end = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(0.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(96.dp)
                    .fillMaxHeight()
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)),
            ) {
                FallbackAsyncImage(
                    models = item.poster.posterUrls(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight().padding(start = 12.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                listStatus?.let { status ->
                    ListStatusBadge(status = status, compact = true)
                    Spacer(modifier = Modifier.height(6.dp))
                }
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleLargeEmphasized,
                    color = if (active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (aired) {
                        stringResource(R.string.schedule_episode_aired, release.episode)
                    } else {
                        stringResource(
                            R.string.schedule_episode_future,
                            release.episode,
                            releaseAt.remainingText(now, remainingLabels),
                        )
                    },
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                if (release.isFavorite) {
                    Text(text = "♥", color = su.afk.yummy.tv.core.designsystem.theme.YummySemanticColors.StatusFavorite, style = MaterialTheme.typography.titleMedium)
                }
                Text(
                    text = releaseAt.timeLabel(),
                    modifier = Modifier.widthIn(min = 82.dp),
                    style = MaterialTheme.typography.headlineMediumEmphasized,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}
