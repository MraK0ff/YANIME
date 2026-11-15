package su.afk.yummy.tv.feature.details.mobile.details.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.theme.YummyShapeTokens
import su.afk.yummy.tv.core.designsystem.theme.YummySpacing
import su.afk.yummy.tv.feature.details.mobile.details.model.DetailsLink

/** Крупная плоская кнопка без шеврона, чтобы боковые отступы были симметричны: «Показать комментарии» на всю ширину; сама учитывает системную навигацию внизу. */
@Composable
internal fun DetailsCommentsBar(
    link: DetailsLink,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = link.onClick,
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(start = YummySpacing.Large, end = YummySpacing.Large, bottom = YummySpacing.Large)
            .testTag(link.testTag),
        shape = YummyShapeTokens.ProminentCard,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = YummySpacing.TouchTarget + YummySpacing.Small)
                .padding(horizontal = YummySpacing.Large, vertical = YummySpacing.Medium),
            horizontalArrangement = Arrangement.spacedBy(YummySpacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = link.icon,
                contentDescription = null,
                modifier = Modifier.size(YummySpacing.XXLarge),
            )
            Text(
                text = link.label,
                style = MaterialTheme.typography.titleSmallEmphasized,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            if (!link.badge.isNullOrBlank()) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ) {
                    Text(
                        text = link.badge,
                        modifier = Modifier.padding(horizontal = YummySpacing.Small, vertical = YummySpacing.XSmall / 2),
                        style = MaterialTheme.typography.labelMedium,
                    )
                }
            }
        }
    }
}
