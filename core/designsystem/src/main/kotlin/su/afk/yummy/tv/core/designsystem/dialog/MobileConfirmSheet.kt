package su.afk.yummy.tv.core.designsystem.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.baseScreen.BaseBottomSheet
import su.afk.yummy.tv.core.designsystem.theme.YummyShapeTokens
import su.afk.yummy.tv.core.designsystem.theme.YummySpacing

/** Мобильный вид запроса подтверждения: шторка поверх текущего экрана, строки как в пикерах деталки. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun MobileConfirmSheet(
    request: ConfirmDialogRequest,
    onAnswer: (Boolean) -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    BaseBottomSheet(
        onDismissRequest = { onAnswer(false) },
        title = request.title,
        verticalArrangement = Arrangement.spacedBy(YummySpacing.Medium),
    ) {
        Text(
            text = request.message,
            style = MaterialTheme.typography.bodyMedium,
            color = colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        ConfirmRow(
            text = request.confirmText,
            filled = true,
            testTag = "confirm_dialog_accept",
            onClick = { onAnswer(true) },
        )
        ConfirmRow(
            text = request.dismissText,
            filled = false,
            testTag = "confirm_dialog_dismiss",
            onClick = { onAnswer(false) },
        )
    }
}

@Composable
private fun ConfirmRow(
    text: String,
    filled: Boolean,
    testTag: String,
    onClick: () -> Unit,
) {
    if (filled) {
        Button(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().heightIn(min = YummySpacing.TouchTarget).testTag(testTag),
            shape = YummyShapeTokens.Control,
        ) {
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    } else {
        TextButton(
            onClick = onClick,
            modifier = Modifier.fillMaxWidth().heightIn(min = YummySpacing.TouchTarget).testTag(testTag),
            shape = YummyShapeTokens.Control,
        ) {
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}
