package su.afk.yummy.tv.feature.home.mobile.view

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.components.FallbackAsyncImage
import su.afk.yummy.tv.core.designsystem.mobile.cards.MobileRatingBadge
import su.afk.yummy.tv.core.model.anime.posterUrls
import su.afk.yummy.tv.domain.home.model.HomeFeedItem
import su.afk.yummy.tv.core.designsystem.theme.YummyShapeTokens
import su.afk.yummy.tv.core.designsystem.theme.YummySpacing

/** Фиксированная высота: длина названия и описания не должна менять высоту карусели. */
private val HERO_CARD_HEIGHT = 224.dp
private val HERO_POSTER_SHAPE = YummyShapeTokens.ProminentCard

@Composable
internal fun HomeHeroCard(
    item: HomeFeedItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val posterUrls = item.poster.posterUrls()
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(HERO_CARD_HEIGHT)
            .clickable(role = Role.Button, onClick = onClick),
        shape = YummyShapeTokens.HeroSurface,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
        ) {
            // blur на API < 31 не работает — фон тогда просто прикрыт скримом
            FallbackAsyncImage(
                models = posterUrls,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .matchParentSize()
                    .blur(22.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
            )
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(
                        Brush.horizontalGradient(
                            colorStops = arrayOf(
                                0f to Color.Black.copy(alpha = 0.38f),
                                0.45f to MaterialTheme.colorScheme.surface.copy(alpha = 0.78f),
                                1f to MaterialTheme.colorScheme.surface.copy(alpha = 0.94f),
                            ),
                        ),
                    ),
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(YummySpacing.Large),
                horizontalArrangement = Arrangement.spacedBy(YummySpacing.Large),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(2f / 3f)
                        .shadow(4.dp, HERO_POSTER_SHAPE)
                        .clip(HERO_POSTER_SHAPE)
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                ) {
                    FallbackAsyncImage(
                        models = posterUrls,
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize(),
                    )
                    item.rating?.takeIf { it > 0.0 }?.let { rating ->
                        MobileRatingBadge(
                            rating = rating,
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(4.dp),
                        )
                    }
                }
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(YummySpacing.Small),
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.titleMediumEmphasized,
                        color = MaterialTheme.colorScheme.onSurface,
                        minLines = 2,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                    HomeHeroMeta(item)
                    if (item.description.isNotBlank()) {
                        Text(
                            text = item.description,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 7,
                            overflow = TextOverflow.Ellipsis,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeroMeta(item: HomeFeedItem) {
    item.year?.let { year ->
        Text(
            text = year.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
