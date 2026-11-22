package su.afk.yummy.tv.core.designsystem.baseScreen

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import su.afk.yummy.tv.core.designsystem.R
import su.afk.yummy.tv.core.designsystem.components.StateMessage
import su.afk.yummy.tv.core.model.ErrorItem
import su.afk.yummy.tv.core.designsystem.skeleton.rememberAnimationsEnabled
import su.afk.yummy.tv.core.designsystem.theme.YummyMotion
import su.afk.yummy.tv.core.designsystem.theme.YummyShapeTokens
import su.afk.yummy.tv.core.designsystem.theme.YummySpacing

@Composable
fun DefaultErrorContent(
    errorItem: ErrorItem,
    onRetry: () -> Unit,
) {
    val context = LocalContext.current
    val animationsEnabled = rememberAnimationsEnabled()
    // A new error must not inherit the expanded diagnostic panel from the previous one.
    var showDetails by remember(errorItem) { mutableStateOf(false) }

    val copyText = remember(errorItem) { errorItem.toClipboardText() }

    val hasDetails = remember(errorItem) {
        errorItem.code != null ||
            errorItem.method != null ||
            errorItem.url != null ||
            errorItem.requestId != null ||
            (errorItem.body?.isNotBlank() == true) ||
            (errorItem.cause?.isNotBlank() == true)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = YummySpacing.Large, vertical = YummySpacing.XXLarge),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 520.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            StateMessage(
                title = errorItem.title.ifBlank { stringResource(R.string.error_title_fallback) },
                icon = Icons.Filled.Warning,
                description = errorItem.message,
                fillMaxSize = false,
            )

            Spacer(Modifier.height(YummySpacing.Large))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(YummySpacing.Small),
            ) {
                if (hasDetails) {
                    OutlinedButton(
                        onClick = { showDetails = !showDetails },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = YummySpacing.TouchTarget),
                    ) {
                        Text(
                            if (showDetails) {
                                stringResource(R.string.hide_details)
                            } else {
                                stringResource(R.string.details)
                            },
                        )
                    }
                }

                DefaultErrorRetryButton(
                    text = stringResource(R.string.common_retry),
                    onClick = onRetry,
                    modifier = Modifier.weight(1f),
                )
            }

            AnimatedVisibility(
                visible = showDetails && hasDetails,
                enter = if (animationsEnabled) fadeIn(tween(YummyMotion.StandardEffectsMillis)) else EnterTransition.None,
                exit = if (animationsEnabled) fadeOut(tween(YummyMotion.FastEffectsMillis)) else ExitTransition.None,
            ) {
                Column(
                    modifier = Modifier
                        .padding(top = YummySpacing.Large)
                        .fillMaxWidth(),
                    horizontalAlignment = Alignment.Start,
                ) {
                    errorItem.code?.let {
                        Text(
                            text = stringResource(R.string.error_http_code, it.toString()),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    errorItem.method?.let {
                        Text(
                            text = stringResource(R.string.error_method, it),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    errorItem.url?.let {
                        Text(
                            text = stringResource(R.string.error_url, it),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    errorItem.requestId?.let {
                        Text(
                            text = stringResource(R.string.error_request_id, it),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    errorItem.cause?.takeIf { it.isNotBlank() }?.let {
                        Spacer(Modifier.height(YummySpacing.Small))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    errorItem.body?.takeIf { it.isNotBlank() }?.let { body ->
                        Spacer(Modifier.height(YummySpacing.Medium))
                        Text(
                            text = stringResource(R.string.error_body, body),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }

                    Spacer(Modifier.height(YummySpacing.Medium))
                    OutlinedButton(
                        onClick = { context.copyToClipboard(copyText) },
                        modifier = Modifier
                            .align(Alignment.CenterHorizontally)
                            .heightIn(min = YummySpacing.TouchTarget),
                    ) {
                        Text(stringResource(R.string.copy))
                    }
                }
            }
        }
    }
}

@Composable
private fun DefaultErrorRetryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val focused by interactionSource.collectIsFocusedAsState()
    val shape = YummyShapeTokens.Control
    val colorScheme = MaterialTheme.colorScheme
    val containerColor = if (focused) colorScheme.primary else Color.Transparent
    val contentColor = if (focused) colorScheme.onPrimary else colorScheme.onSurface
    val borderColor = if (focused) colorScheme.primary else colorScheme.outline.copy(alpha = 0.72f)

    Button(
        modifier = modifier.heightIn(min = YummySpacing.TouchTarget),
        onClick = onClick,
        shape = shape,
        interactionSource = interactionSource,
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
        ),
        border = BorderStroke(
            width = if (focused) 2.dp else 1.dp,
            color = borderColor,
        ),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
    ) {
        Text(text = text, style = MaterialTheme.typography.labelLargeEmphasized)
    }
}

private fun ErrorItem.toClipboardText(): String {
    val lines = buildList {
        add("Title: $title")
        add("Message: $message")
        code?.let { add("HTTP: $it") }
        method?.let { add("Method: $it") }
        url?.let { add("URL: $it") }
        requestId?.let { add("RequestId: $it") }
        cause?.let { add("Cause: $it") }
        body?.takeIf { it.isNotBlank() }?.let {
            add("")
            add("Body:")
            add(it)
        }
    }
    return lines.joinToString("\n")
}

private fun Context.copyToClipboard(text: String) {
    val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText("error", text))
}
