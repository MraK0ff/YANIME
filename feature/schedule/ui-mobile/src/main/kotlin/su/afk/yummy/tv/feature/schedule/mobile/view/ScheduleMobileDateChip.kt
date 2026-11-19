package su.afk.yummy.tv.feature.schedule.mobile.view

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.feature.schedule.model.ScheduleDayUi

@Composable
internal fun ScheduleMobileDateChip(
    group: ScheduleDayUi,
    selected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(22.dp)
    val backgroundTarget = when {
        selected -> MaterialTheme.colorScheme.primary
        group.isToday -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }
    val contentTarget = when {
        selected -> MaterialTheme.colorScheme.onPrimary
        group.isToday -> MaterialTheme.colorScheme.onTertiaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    val background by animateColorAsState(backgroundTarget, spring(), label = "schedule_date_background")
    val contentColor by animateColorAsState(contentTarget, spring(), label = "schedule_date_content")

    Box(
        modifier = modifier
            .width(76.dp)
            .height(84.dp)
            .clip(shape)
            .background(background, shape)
            
            .clickable(onClick = onSelected)
            .padding(horizontal = 8.dp, vertical = 10.dp),
    ) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(weight = 1f, fill = false),
                    text = group.weekdayLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = contentColor,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Box(
                    modifier = Modifier
                        .height(22.dp)
                        .widthIn(min = 22.dp)
                        .clip(RoundedCornerShape(11.dp))
                        .background(
                            color = if (selected) {
                                MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f)
                            } else if (group.isToday) {
                                MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.12f)
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.12f)
                            },
                        )
                        .padding(horizontal = 5.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = group.items.size.toString(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = contentColor,
                        maxLines = 1,
                    )
                }
            }
            Text(
                text = group.date.dayOfMonth.toString(),
                style = MaterialTheme.typography.headlineSmall,
                color = contentColor,
                maxLines = 1,
            )
        }
        if (group.isToday) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(3.dp)
                    .size(5.dp)
                    .background(contentColor, CircleShape),
            )
        }
    }
}
