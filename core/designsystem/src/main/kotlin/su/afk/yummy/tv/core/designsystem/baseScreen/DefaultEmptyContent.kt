package su.afk.yummy.tv.core.designsystem.baseScreen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import su.afk.yummy.tv.core.designsystem.R
import su.afk.yummy.tv.core.designsystem.theme.YummySpacing

@Composable
internal fun DefaultEmptyContent() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(YummySpacing.Large),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = stringResource(R.string.empty_screen),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = YummySpacing.Small),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmallEmphasized,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}
