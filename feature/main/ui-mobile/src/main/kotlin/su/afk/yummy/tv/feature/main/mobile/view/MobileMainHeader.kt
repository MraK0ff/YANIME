package su.afk.yummy.tv.feature.main.mobile.view

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import su.afk.yummy.tv.core.designsystem.components.AppBrandIcon
import su.afk.yummy.tv.core.designsystem.seasonal.seasonalAvatarHat
import su.afk.yummy.tv.feature.main.presentation.R

/** Компактная нейтральная шапка: визуально продолжает фон экрана, а не образует отдельную панель. */
@Composable
internal fun MobileMainHeader(
    modifier: Modifier = Modifier,
    containerColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.background,
    avatarUrl: String,
    unreadNotificationsCount: Int,
    showSearchBar: Boolean = true,
    onSearchSelected: () -> Unit,
    onNotificationsSelected: () -> Unit,
    onSettingsSelected: () -> Unit,
    onProfileSelected: () -> Unit,
) {
    Surface(
        modifier = modifier,
        color = containerColor,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(48.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppBrandIcon(Modifier.size(34.dp))
            Spacer(Modifier.weight(1f))
            if (showSearchBar) {
                Surface(
                    modifier = Modifier
                        .width(156.dp)
                        .height(38.dp)
                        .clickable(onClick = onSearchSelected),
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.surfaceContainerLow,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                        Text(
                            text = stringResource(R.string.main_header_search_hint),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(start = 8.dp),
                            maxLines = 1,
                        )
                    }
                }
            }
            IconButton(onClick = onSettingsSelected) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = stringResource(R.string.main_header_settings),
                )
            }
            Box(
                modifier = Modifier.size(48.dp),
                contentAlignment = Alignment.Center,
            ) {
                IconButton(onClick = onNotificationsSelected) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = stringResource(R.string.main_header_notifications),
                    )
                }
                if (unreadNotificationsCount > 0) {
                    Badge(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(top = 3.dp, end = 2.dp),
                    ) {
                        Text(
                            if (unreadNotificationsCount > 99) "99+" else unreadNotificationsCount.toString(),
                        )
                    }
                }
            }
            IconButton(onClick = onProfileSelected) {
                if (avatarUrl.isNotBlank()) {
                    AsyncImage(
                        model = avatarUrl,
                        contentDescription = stringResource(R.string.main_header_profile),
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(32.dp)
                            .seasonalAvatarHat()
                            .clip(CircleShape)
                            .border(
                                1.5.dp,
                                MaterialTheme.colorScheme.outlineVariant,
                                CircleShape,
                            ),
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.AccountCircle,
                        contentDescription = stringResource(R.string.main_header_profile),
                        modifier = Modifier.size(30.dp),
                    )
                }
            }
        }
    }
}
