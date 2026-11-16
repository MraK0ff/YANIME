package su.afk.yummy.tv.feature.player.mobile.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Промпт конца серии: вторичное действие слева (если есть), основное справа. Подписи в одну строку: если
 * кнопки не помещаются рядом, основная уходит на следующую строку, а не рвёт слово.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
internal fun MobilePlayerEndPrompt(
    title: String,
    primaryLabel: String,
    secondaryLabel: String?,
    onPrimary: () -> Unit,
    onSecondary: () -> Unit,
    modifier: Modifier = Modifier,
    /** Вторая строка под заголовком, как в диалоге со списком озвучек. */
    subtitle: String? = null,
) {
    Column(
        modifier = modifier
            .widthIn(max = 440.dp)
            .mobilePlayerGlassSurface(
                shape = MobilePlayerInteractiveShape,
                fill = Color.Black.copy(alpha = 0.78f),
                borderColor = MobilePlayerGlassBorder,
                elevation = 10.dp,
            )
            .padding(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.72f),
                )
            }
        }
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterHorizontally),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (secondaryLabel != null) {
                TextButton(onClick = onSecondary) {
                    Text(secondaryLabel, maxLines = 1)
                }
            }
            TextButton(onClick = onPrimary) {
                Text(primaryLabel, maxLines = 1)
            }
        }
    }
}
