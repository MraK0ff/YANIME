package su.afk.yummy.tv.feature.details.mobile.details.view

import su.afk.yummy.tv.core.designsystem.onboarding.onboardingTarget
import su.afk.yummy.tv.feature.details.mobile.details.utils.mobileDetailsActionTargetId
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.feature.details.mobile.details.model.MobileDetailsAction
import su.afk.yummy.tv.core.designsystem.theme.YummyShapeTokens
import su.afk.yummy.tv.core.designsystem.theme.YummySpacing

/** Плоская плитка раздела (серии, подписки): тональная заливка без обводки и тени, иконка, название и пояснение. */
@Composable
internal fun DetailsActionCard(
    action: MobileDetailsAction,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = action.onClick,
        modifier = modifier
            .heightIn(min = YummySpacing.TouchTarget + YummySpacing.Large)
            .onboardingTarget(mobileDetailsActionTargetId(action.action)),
        shape = YummyShapeTokens.Control,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = YummySpacing.Large, vertical = YummySpacing.Medium),
            horizontalArrangement = Arrangement.spacedBy(YummySpacing.Medium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = action.icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp),
            )
            Column(verticalArrangement = Arrangement.spacedBy(YummySpacing.XSmall)) {
                Text(
                    text = action.label,
                    style = MaterialTheme.typography.titleSmallEmphasized,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = action.subtitle,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}
