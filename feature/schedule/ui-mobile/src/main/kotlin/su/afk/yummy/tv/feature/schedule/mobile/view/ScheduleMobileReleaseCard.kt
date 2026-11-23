package su.afk.yummy.tv.feature.schedule.mobile.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.components.FallbackAsyncImage
import su.afk.yummy.tv.core.designsystem.theme.YummyShapeTokens
import su.afk.yummy.tv.core.designsystem.theme.YummySpacing
import su.afk.yummy.tv.core.model.anime.posterUrls
import su.afk.yummy.tv.feature.schedule.model.ScheduleReleaseUi
import su.afk.yummy.tv.feature.schedule.presentation.R
import su.afk.yummy.tv.feature.schedule.utils.remainingText
import su.afk.yummy.tv.feature.schedule.utils.timeLabel
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

@Composable
internal fun ScheduleMobileReleaseCard(
    release: ScheduleReleaseUi,
    now: ZonedDateTime,
    zone: ZoneId,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val item = release.item
    val releaseAt = Instant.ofEpochSecond(release.epochSeconds).atZone(zone)
    val aired = !releaseAt.isAfter(now)
    val remainingLabels = scheduleMobileRemainingLabels()
    val accentColor = if (aired) {
        MobileScheduleAiredColor
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val rowBackground = if (aired) {
        MobileScheduleAiredColor.copy(alpha = 0.14f)
    } else {
        MaterialTheme.colorScheme.surfaceContainerLow
    }

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 126.dp),
        shape = YummyShapeTokens.ProminentCard,
        colors = CardDefaults.cardColors(containerColor = rowBackground),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = YummySpacing.Large, vertical = YummySpacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(74.dp)
                    .height(106.dp)
                    .clip(YummyShapeTokens.Control)
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
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(6.dp))
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
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Text(
                text = releaseAt.timeLabel(),
                modifier = Modifier.widthIn(min = 58.dp),
                style = MaterialTheme.typography.headlineSmallEmphasized,
                color = accentColor,
                maxLines = 1,
            )
        }
    }
}
