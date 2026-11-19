package su.afk.yummy.tv.feature.schedule.mobile.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.components.FallbackAsyncImage
import su.afk.yummy.tv.core.designsystem.components.ListStatusBadge
import su.afk.yummy.tv.core.designsystem.theme.YummySemanticColors
import su.afk.yummy.tv.core.designsystem.locals.LocalLibraryListStatuses
import su.afk.yummy.tv.core.designsystem.theme.YummyShapeTokens
import su.afk.yummy.tv.core.model.anime.posterUrls
import su.afk.yummy.tv.feature.schedule.model.ScheduleReleaseUi
import su.afk.yummy.tv.domain.schedule.model.ReleasePrediction
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
    val aired = release.aired
    val remainingLabels = scheduleMobileRemainingLabels()
    val listStatus = LocalLibraryListStatuses.current[item.animeId]
    val accentColor = if (aired && listStatus == null) MobileScheduleAiredColor else MaterialTheme.colorScheme.onSurfaceVariant
    val rowBackground = MaterialTheme.colorScheme.surfaceContainerLow

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(144.dp),
        shape = YummyShapeTokens.Card,
        colors = CardDefaults.cardColors(containerColor = rowBackground),
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
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
                modifier = Modifier.weight(1f).fillMaxSize().padding(start = 12.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
                verticalArrangement = Arrangement.Center,
            ) {
                listStatus?.let { status ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        ListStatusBadge(status = status, compact = true)
                        if (release.isFavorite) FavoriteScheduleBadge()
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMediumEmphasized,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Spacer(modifier = Modifier.height(6.dp))
                if (listStatus == null && release.isFavorite) FavoriteScheduleBadge()
                if (release.isPredicted) {
                    val prediction = item.releasePrediction
                    val confidenceLabel = when (release.predictionConfidence) {
                        ReleasePrediction.Confidence.HIGH -> stringResource(R.string.schedule_prediction_high)
                        ReleasePrediction.Confidence.MEDIUM -> stringResource(R.string.schedule_prediction_medium)
                        ReleasePrediction.Confidence.LOW -> stringResource(R.string.schedule_prediction_low)
                        else -> ""
                    }
                    Text(
                        text = buildString {
                            append(stringResource(R.string.schedule_prediction_label))
                            prediction?.sourceDubbing?.takeIf { it.isNotBlank() }?.let { append(" · ").append(it) }
                            if (confidenceLabel.isNotBlank()) append(" · ").append(confidenceLabel)
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.tertiary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                }
                Text(
                    text = when {
                        aired -> stringResource(R.string.schedule_episode_aired, release.episode)
                        release.isAwaitingVideo -> stringResource(
                            R.string.schedule_episode_waiting,
                            release.episode,
                            releaseAt.remainingText(now, remainingLabels),
                        )
                        else -> stringResource(R.string.schedule_episode_future, release.episode) + "\n" + releaseAt.remainingText(now, remainingLabels)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = accentColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            Column(modifier = Modifier.padding(end = 12.dp), horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = if (release.isPredicted && release.predictionConfidence == ReleasePrediction.Confidence.LOW) "—" else releaseAt.timeLabel(),
                    modifier = Modifier.widthIn(min = 58.dp),
                    style = MaterialTheme.typography.headlineSmallEmphasized,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                )
            }
        }
    }
}

@Composable
private fun FavoriteScheduleBadge() {
    Icon(
        imageVector = Icons.Filled.Favorite,
        contentDescription = stringResource(R.string.schedule_favorite),
        tint = YummySemanticColors.StatusFavorite,
        modifier = Modifier.size(16.dp),
    )
}
