package su.afk.yummy.tv.core.designsystem.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.skeleton.rememberAnimationsEnabled
import su.afk.yummy.tv.core.designsystem.theme.YummyShapeTokens
import su.afk.yummy.tv.core.designsystem.theme.YummySpacing

/**
 * Информационная строка-сообщение с сервера. Встраивается в вёрстку так же, как [OfflineBanner]
 * (не диалог и не оверлей): раздвигает контент и не перекрывает плеер. `null` — скрыта.
 */
@Composable
fun InfoBanner(
    message: String?,
    modifier: Modifier = Modifier,
) {
    // Сохраняем последний текст до конца exit-transition, чтобы баннер не исчезал пустым.
    val lastMessage = remember { arrayOf("") }
    if (message != null) lastMessage[0] = message
    val animationsEnabled = rememberAnimationsEnabled()

    AnimatedVisibility(
        visible = message != null,
        modifier = modifier,
        enter = if (animationsEnabled) expandVertically() + fadeIn() else EnterTransition.None,
        exit = if (animationsEnabled) shrinkVertically() + fadeOut() else ExitTransition.None,
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = YummySpacing.Large, vertical = YummySpacing.Small),
            shape = YummyShapeTokens.Pill,
            color = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            shadowElevation = 6.dp,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = YummySpacing.Large, vertical = YummySpacing.Small),
                horizontalArrangement = Arrangement.spacedBy(YummySpacing.Small, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                )
                Text(
                    text = lastMessage[0],
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}
