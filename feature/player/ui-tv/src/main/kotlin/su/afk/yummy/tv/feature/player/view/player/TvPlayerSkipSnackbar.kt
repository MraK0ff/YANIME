package su.afk.yummy.tv.feature.player.view.player

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun TvPlayerSkipSnackbar(
    text: String?,
    modifier: Modifier = Modifier,
) {
    AnimatedVisibility(
        visible = text != null,
        modifier = modifier,
        enter = fadeIn(),
        exit = fadeOut(),
    ) {
        Text(
            text = text.orEmpty(),
            style = MaterialTheme.typography.titleSmall,
            color = Color.White,
            modifier = Modifier
                .background(Color(0xFF101318).copy(alpha = 0.76f), RoundedCornerShape(12.dp))
                .border(1.dp, Color.White.copy(alpha = 0.14f), RoundedCornerShape(12.dp))
                .padding(horizontal = 22.dp, vertical = 13.dp),
        )
    }
}
