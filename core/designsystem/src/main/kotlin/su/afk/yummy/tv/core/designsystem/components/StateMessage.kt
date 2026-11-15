package su.afk.yummy.tv.core.designsystem.components

import su.afk.yummy.tv.core.designsystem.seasonal.SeasonalEmptyIllustration
import su.afk.yummy.tv.core.designsystem.seasonal.rememberSeasonalTheme
import su.afk.yummy.tv.core.model.seasonal.SeasonalTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.theme.YummySpacing

@Composable
fun StateMessage(
    title: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Filled.Info,
    iconSize: Dp = 48.dp,
    description: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    /** Когда false — блок занимает только ширину (для LazyColumn item / bottom sheet). */
    fillMaxSize: Boolean = true,
    /** Кастомная кнопка действия — рендерится вместо встроенной Button (например, TvRetryButton). */
    action: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .then(if (fillMaxSize) Modifier.fillMaxSize() else Modifier.fillMaxWidth())
            .padding(horizontal = 32.dp, vertical = if (fillMaxSize) 40.dp else 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        if (onAction == null && action == null && rememberSeasonalTheme() == SeasonalTheme.HALLOWEEN) {
            SeasonalEmptyIllustration(Modifier.size(iconSize * 2))
        } else {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            ) {
                Box(Modifier.padding(12.dp), contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = null, modifier = Modifier.size(iconSize))
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmallEmphasized,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
        )
        if (!description.isNullOrBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
        when {
            action != null -> {
                Spacer(Modifier.height(20.dp))
                action()
            }

            actionLabel != null && onAction != null -> {
                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = onAction,
                    modifier = Modifier.heightIn(min = YummySpacing.TouchTarget),
                ) {
                    Text(
                        actionLabel,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}
