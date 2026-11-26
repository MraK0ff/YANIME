package su.afk.yummy.tv.core.designsystem.mobile.cards

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.components.FallbackAsyncImage
import su.afk.yummy.tv.core.designsystem.components.YummyLoadingIndicator
import su.afk.yummy.tv.core.designsystem.mobile.input.clickablePointer
import su.afk.yummy.tv.core.designsystem.theme.YummyShapeTokens
import su.afk.yummy.tv.core.designsystem.theme.YummySpacing

@Composable
fun MobileProgressMediaCard(
    title: String,
    imageModels: List<Any>,
    subtitle: String?,
    trailingSubtitle: String? = null,
    progress: Float,
    modifier: Modifier = Modifier.width(220.dp),
    subtitleColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    imageOverlay: @Composable BoxScope.() -> Unit = {},
    isLoading: Boolean = false,
    /** Крупный блеклый текст под картинкой: виден, пока превью грузится или его нет. */
    placeholderText: String? = null,
    titleMaxLines: Int = 2,
    onLongClick: (() -> Unit)? = null,
    onClick: () -> Unit,
) {
    Card(
        modifier = modifier
            .clickablePointer()
            .combinedClickable(enabled = !isLoading, onLongClick = onLongClick, onClick = onClick, role = Role.Button),
        shape = YummyShapeTokens.Card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f)
                .background(MaterialTheme.colorScheme.surfaceContainerHighest),
        ) {
            if (!placeholderText.isNullOrBlank()) {
                Text(
                    text = placeholderText,
                    style = MaterialTheme.typography.headlineLarge,
                    color = Color.White.copy(alpha = 0.15f),
                    modifier = Modifier.align(Alignment.Center),
                )
            }
            FallbackAsyncImage(
                models = imageModels,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.matchParentSize(),
            )
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                trackColor = Color.Black.copy(alpha = 0.4f),
                gapSize = 0.dp,
                drawStopIndicator = {},
            )
            imageOverlay()
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.45f)),
                    contentAlignment = Alignment.Center,
                ) {
                    YummyLoadingIndicator(modifier = Modifier.size(28.dp), color = Color.White, strokeWidth = 3.dp)
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = YummySpacing.Large, vertical = YummySpacing.Medium),
            verticalArrangement = Arrangement.spacedBy(YummySpacing.XSmall),
        ) {
            Text(
                text = title,
                minLines = minOf(2, titleMaxLines.coerceAtLeast(1)),
                maxLines = titleMaxLines.coerceAtLeast(1),
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.bodyMediumEmphasized,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (!subtitle.isNullOrBlank()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(YummySpacing.Small),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = subtitle,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = MaterialTheme.typography.bodySmall,
                        color = subtitleColor,
                        modifier = Modifier.weight(1f),
                    )
                    if (!trailingSubtitle.isNullOrBlank()) {
                        Text(
                            text = trailingSubtitle,
                            maxLines = 1,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.68f),
                        )
                    }
                }
            }
        }
    }
}
