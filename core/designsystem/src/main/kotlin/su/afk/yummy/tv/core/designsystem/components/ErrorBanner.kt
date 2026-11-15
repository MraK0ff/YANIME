package su.afk.yummy.tv.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.R
import su.afk.yummy.tv.core.designsystem.focus.TvRetryButton
import su.afk.yummy.tv.core.designsystem.theme.YummySpacing

/**
 * Compact error with a retry action for a section of a screen (next-page failure, remote sync failure).
 * Full-screen failures go through `StateMessage`; form field errors through [InlineError].
 *
 * @param tv lays the message and a focusable [TvRetryButton] in a row for D-pad screens;
 *   otherwise the message is centered above a text button.
 */
@Composable
fun ErrorBanner(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    tv: Boolean = false,
) {
    val retryLabel = stringResource(R.string.common_retry)
    if (tv) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = YummySpacing.Large),
            horizontalArrangement = Arrangement.spacedBy(YummySpacing.XLarge, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.End,
                modifier = Modifier.weight(1f),
            )
            TvRetryButton(text = retryLabel, enabled = enabled, onClick = onRetry)
        }
    } else {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = YummySpacing.Medium, horizontal = YummySpacing.Large),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(YummySpacing.XSmall),
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )
            TextButton(
                onClick = onRetry,
                enabled = enabled,
                modifier = Modifier.heightIn(min = YummySpacing.TouchTarget),
            ) {
                Text(retryLabel)
            }
        }
    }
}
