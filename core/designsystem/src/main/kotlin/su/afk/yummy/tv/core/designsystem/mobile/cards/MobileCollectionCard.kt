package su.afk.yummy.tv.core.designsystem.mobile.cards

import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.semantics.Role
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import su.afk.yummy.tv.core.designsystem.components.PostersCollage
import su.afk.yummy.tv.core.designsystem.mobile.input.clickablePointer
import su.afk.yummy.tv.core.designsystem.theme.YummyShapeTokens
import su.afk.yummy.tv.core.designsystem.theme.YummySpacing

private const val COLLAGE_ASPECT_RATIO = 2.4f

/** Плоская карточка коллекции: коллаж из трёх постеров, [badge] в верхнем правом углу и название ниже. */
@Composable
fun MobileCollectionCard(
    title: String,
    posterUrls: List<String>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: (@Composable BoxScope.() -> Unit)? = null,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = YummyShapeTokens.Card,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
    ) {
        Column(
            Modifier
                .clickablePointer()
                .combinedClickable(onClick = onClick, role = Role.Button),
        ) {
            PostersCollage(
                posterUrls = posterUrls,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(COLLAGE_ASPECT_RATIO),
                overlay = { badge?.invoke(this) },
            )
            Text(
                text = title,
                modifier = Modifier.padding(horizontal = YummySpacing.Medium, vertical = YummySpacing.Small),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleMediumEmphasized,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
