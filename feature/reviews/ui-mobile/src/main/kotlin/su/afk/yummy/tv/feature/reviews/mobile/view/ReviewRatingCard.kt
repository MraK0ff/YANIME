package su.afk.yummy.tv.feature.reviews.mobile.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.theme.YummySemanticColors
import su.afk.yummy.tv.core.designsystem.theme.YummyShapeTokens
import su.afk.yummy.tv.core.designsystem.theme.YummySpacing
import su.afk.yummy.tv.domain.reviews.model.ReviewRating
import su.afk.yummy.tv.feature.reviews.mobile.utils.reviewScoreColor
import su.afk.yummy.tv.feature.reviews.presentation.R

@Composable
internal fun ReviewRatingCard(
    rating: ReviewRating,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = YummyShapeTokens.Card,
    ) {
        Column(
            modifier = Modifier.padding(YummySpacing.Large),
            verticalArrangement = Arrangement.spacedBy(YummySpacing.Medium),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = stringResource(R.string.review_rating_title),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                rating.average?.let { score ->
                    Surface(
                        color = score.reviewScoreColor(),
                        contentColor = YummySemanticColors.OnScoreBadge,
                        shape = YummyShapeTokens.Control,
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = YummySpacing.Medium, vertical = YummySpacing.Small),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(YummySpacing.XSmall),
                        ) {
                            Icon(Icons.Filled.Star, contentDescription = null)
                            Text(
                                text = "$score / 10",
                                style = MaterialTheme.typography.labelLarge,
                            )
                        }
                    }
                }
            }
            rating.categories.forEach { category ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = category.name,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "${category.score} / 10",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }
    }
}
