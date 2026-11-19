package su.afk.yummy.tv.core.designsystem.mobile.cards

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.components.FallbackAsyncImage
import su.afk.yummy.tv.core.designsystem.components.ListStatusBadge
import su.afk.yummy.tv.core.designsystem.components.ListStatusBadgeHeight
import su.afk.yummy.tv.core.designsystem.dimensions.TITLE_POSTER_ASPECT_RATIO
import su.afk.yummy.tv.core.designsystem.dimensions.currentMobilePosterBadgeScale
import su.afk.yummy.tv.core.designsystem.dimensions.currentMobilePosterTextPadding
import su.afk.yummy.tv.core.designsystem.dimensions.currentMobilePosterTitleStyle
import su.afk.yummy.tv.core.designsystem.dimensions.currentMobilePosterTitleWeight
import su.afk.yummy.tv.core.designsystem.dimensions.currentMobilePosterWidth
import su.afk.yummy.tv.core.designsystem.locals.LocalLibraryListStatuses
import su.afk.yummy.tv.core.designsystem.mobile.input.clickablePointer
import su.afk.yummy.tv.core.designsystem.mobile.input.onSecondaryClick
import su.afk.yummy.tv.core.designsystem.seasonal.seasonalCardDecor
import su.afk.yummy.tv.core.designsystem.theme.YummyShapeTokens
import su.afk.yummy.tv.core.designsystem.theme.YummySpacing

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MobilePosterCard(
    title: String,
    posterUrls: List<String>,
    modifier: Modifier = Modifier.width(currentMobilePosterWidth()),
    subtitle: String? = null,
    caption: String? = null,
    captionMinLines: Int = 1,
    badge: String? = null,
    rating: Double? = null,
    titleMinLines: Int = 2,
    animeId: Int? = null,
    posterOverlay: @Composable BoxScope.() -> Unit = {},
    footer: @Composable ColumnScope.() -> Unit = {},
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier,
        shape = YummyShapeTokens.Card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
    ) {
        Column(
            Modifier
                .clickablePointer()
                .onSecondaryClick(onLongClick)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick,
                    role = Role.Button,
                ),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(TITLE_POSTER_ASPECT_RATIO)
                    .seasonalCardDecor(animeId ?: posterUrls.firstOrNull().hashCode()),
            ) {
                FallbackAsyncImage(
                    models = posterUrls,
                    contentDescription = null,
                    modifier = Modifier
                        .matchParentSize()
                        .background(MaterialTheme.colorScheme.surfaceContainerHighest),
                    contentScale = ContentScale.Crop,
                )
                if (!badge.isNullOrBlank()) {
                    Text(
                        text = badge,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(YummySpacing.Small)
                            .background(MaterialTheme.colorScheme.primary, YummyShapeTokens.Control)
                            .padding(horizontal = YummySpacing.Small, vertical = YummySpacing.XSmall),
                    )
                }
                rating?.takeIf { it > 0.0 }?.let { value ->
                    MobileRatingBadge(
                        rating = value,
                        scale = currentMobilePosterBadgeScale(),
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp),
                    )
                }
                val listStatus = animeId?.let { LocalLibraryListStatuses.current[it] }
                Box(
                    Modifier
                        .matchParentSize()
                        .padding(bottom = if (listStatus != null) ListStatusBadgeHeight else 0.dp),
                ) {
                    posterOverlay()
                }
                listStatus?.let { status ->
                    ListStatusBadge(
                        status = status,
                        modifier = Modifier.align(Alignment.BottomStart),
                    )
                }
            }
            Column(Modifier.padding(horizontal = currentMobilePosterTextPadding(), vertical = YummySpacing.Small)) {
                Text(
                    text = title,
                    minLines = titleMinLines,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    style = currentMobilePosterTitleStyle(),
                    fontWeight = currentMobilePosterTitleWeight(),
                )
                if (!subtitle.isNullOrBlank()) {
                    Spacer(Modifier.height(YummySpacing.XSmall))
                    Text(
                        text = subtitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (!caption.isNullOrBlank() || captionMinLines > 1) {
                    Spacer(Modifier.height(YummySpacing.XSmall))
                    Text(
                        text = caption.orEmpty(),
                        minLines = captionMinLines,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
        footer()
    }
}