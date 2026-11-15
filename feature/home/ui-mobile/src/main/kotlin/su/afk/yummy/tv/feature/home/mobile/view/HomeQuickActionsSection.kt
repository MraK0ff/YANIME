package su.afk.yummy.tv.feature.home.mobile.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.RateReview
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.theme.YummyShapeTokens
import su.afk.yummy.tv.core.designsystem.theme.YummySpacing
import su.afk.yummy.tv.core.designsystem.mobile.MobileSectionHeader
import su.afk.yummy.tv.core.designsystem.preview.ScreenPreviewTheme

@Composable
internal fun HomeQuickActionsSection(
    title: String,
    scheduleTitle: String,
    reviewsTitle: String,
    onScheduleClick: () -> Unit,
    onReviewsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(YummySpacing.Small),
    ) {
        MobileSectionHeader(
            title = title,
            modifier = Modifier.padding(horizontal = YummySpacing.Large),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(YummySpacing.Small),
        ) {
            HomeQuickActionCard(
                title = scheduleTitle,
                icon = Icons.Filled.DateRange,
                onClick = onScheduleClick,
                modifier = Modifier.weight(1f),
            )
            HomeQuickActionCard(
                title = reviewsTitle,
                icon = Icons.Filled.RateReview,
                onClick = onReviewsClick,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun HomeQuickActionCard(
    title: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        onClick = onClick,
        modifier = modifier.heightIn(min = YummySpacing.TouchTarget + YummySpacing.Medium),
        shape = YummyShapeTokens.ProminentCard,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = YummySpacing.Medium),
            horizontalArrangement = Arrangement.spacedBy(YummySpacing.Small),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(YummySpacing.Large + YummySpacing.XSmall),
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMediumEmphasized,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Preview(name = "Schedule and reviews", showBackground = true)
@Composable
private fun HomeQuickActionsSectionPreview() = ScreenPreviewTheme {
    HomeQuickActionsSection(
        title = "Ещё",
        scheduleTitle = "Расписание",
        reviewsTitle = "Рецензии",
        onScheduleClick = {},
        onReviewsClick = {},
    )
}
