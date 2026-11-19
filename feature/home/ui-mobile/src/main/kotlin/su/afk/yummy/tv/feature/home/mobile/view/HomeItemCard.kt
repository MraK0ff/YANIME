package su.afk.yummy.tv.feature.home.mobile.view

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.theme.YummyShapeTokens
import su.afk.yummy.tv.core.designsystem.theme.YummySpacing
import su.afk.yummy.tv.core.designsystem.mobile.cards.MobilePosterCard
import su.afk.yummy.tv.core.model.anime.posterUrls
import su.afk.yummy.tv.domain.home.model.HomeFeedItem

@Composable
internal fun HomeItemCard(
    item: HomeFeedItem,
    showMetadata: Boolean,
    showYear: Boolean,
    cardWidth: Dp,
    /** Подпись под названием; в блоке новых серий — дата выхода серии. */
    subtitle: String? = null,
    /** Бейдж поверх постера; в блоке новых серий — номер вышедшей серии. */
    badge: String? = null,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    MobilePosterCard(
        modifier = Modifier.width(cardWidth),
        title = item.title,
        posterUrls = item.poster.posterUrls(),
        animeId = item.id,
        subtitle = subtitle ?: item.description.takeIf { showMetadata && it.isNotBlank() },
        badge = badge,
        rating = item.rating.takeIf { showMetadata },
        highlightSelectedList = item.isFromSelectedList,
        titleMinLines = if (showMetadata) 1 else 2,
        posterOverlay = {
            if (showYear) {
                item.year?.let { year ->
                    Text(
                        text = year.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.inverseSurface,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(YummySpacing.XSmall)
                            .background(
                                MaterialTheme.colorScheme.inverseOnSurface,
                                YummyShapeTokens.Compact,
                            )
                            .padding(horizontal = YummySpacing.Small - 2.dp, vertical = YummySpacing.XSmall - 1.dp),
                    )
                }
            }
        },
        onLongClick = onLongClick,
        onClick = onClick,
    )
}
