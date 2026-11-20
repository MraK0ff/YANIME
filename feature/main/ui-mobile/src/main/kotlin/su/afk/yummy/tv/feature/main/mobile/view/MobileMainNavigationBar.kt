package su.afk.yummy.tv.feature.main.mobile.view

import su.afk.yummy.tv.core.designsystem.seasonal.seasonalNavigationBarItemColors
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import su.afk.yummy.tv.core.designsystem.mobile.input.clickablePointer
import su.afk.yummy.tv.core.designsystem.onboarding.onboardingTarget
import su.afk.yummy.tv.feature.main.mobile.utils.mobileMainTabTargetId
import su.afk.yummy.tv.core.designsystem.seasonal.SeasonalBarDecoration
import su.afk.yummy.tv.feature.main.mobile.model.MobileMenuItem
import su.afk.yummy.tv.feature.main.presentation.R
import kotlin.math.abs
import kotlin.math.floor

@Composable
internal fun <T> MobileMainNavigationBar(
    selectedDestination: T,
    menuItems: List<MobileMenuItem<T>>,
    containerColor: Color,
    upFocusRequester: FocusRequester?,
    classicStyle: Boolean = false,
    searchFabEnabled: Boolean = true,
    onSearchSelected: () -> Unit,
    onDestinationSelected: (T) -> Unit,
) {
    val orderedItems = remember(menuItems) { menuItems }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 12.dp, vertical = 4.dp),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = if (searchFabEnabled) 64.dp else 0.dp)
                .height(64.dp),
            shape = if (classicStyle) RoundedCornerShape(0.dp) else RoundedCornerShape(32.dp),
            color = containerColor,
            contentColor = MaterialTheme.colorScheme.onSurface,
            tonalElevation = 0.dp,
            shadowElevation = 0.dp,
        ) {
            NavigationBar(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(orderedItems) {
                        if (orderedItems.size < 2) return@pointerInput
                        var totalDrag = 0f
                        var lastX = 0f
                        var startIndex = 0
                        var currentSelectedIndex = 0
                        var selectedDuringDrag = false
                        detectHorizontalDragGestures(
                            onDragStart = { offset ->
                                totalDrag = 0f
                                lastX = offset.x
                                startIndex = orderedItems.indexOfFirst { it.destination == selectedDestination }.coerceAtLeast(0)
                                currentSelectedIndex = startIndex
                                selectedDuringDrag = false
                            },
                            onHorizontalDrag = { change, amount ->
                                change.consume()
                                totalDrag += amount
                                lastX = change.position.x
                                val rawIndex = floor(
                                    (lastX / size.width.coerceAtLeast(1)) * orderedItems.size
                                ).toInt()
                                val index = rawIndex.coerceIn(0, orderedItems.lastIndex)
                                if (index != currentSelectedIndex) {
                                    selectedDuringDrag = true
                                    currentSelectedIndex = index
                                    onDestinationSelected(orderedItems[index].destination)
                                }
                            },
                            onDragEnd = {
                                if (abs(totalDrag) > size.width * 0.55f) {
                                    val edgeIndex = if (totalDrag < 0f) orderedItems.lastIndex else 0
                                    currentSelectedIndex = edgeIndex
                                    onDestinationSelected(orderedItems[edgeIndex].destination)
                                } else if (!selectedDuringDrag && abs(totalDrag) > 18f) {
                                    val direction = if (totalDrag < 0f) 1 else -1
                                    val next = (startIndex + direction)
                                        .coerceIn(0, orderedItems.lastIndex)
                                    onDestinationSelected(orderedItems[next].destination)
                                }
                                totalDrag = 0f
                            },
                            onDragCancel = { totalDrag = 0f },
                        )
                    },
                containerColor = Color.Transparent,
                tonalElevation = 0.dp,
                windowInsets = WindowInsets(0.dp),
            ) {
                val itemColors = seasonalNavigationBarItemColors()
                orderedItems.forEach { item ->
                    val selected = item.destination == selectedDestination
                    NavigationBarItem(
                        modifier = Modifier
                            .weight(1f)
                            .testTag("main_tab")
                            .onboardingTarget(mobileMainTabTargetId(item.destination))
                            .clickablePointer()
                            .focusProperties {
                                upFocusRequester?.takeIf { selected }?.let { up = it }
                            },
                        selected = selected,
                        colors = itemColors,
                        onClick = { onDestinationSelected(item.destination) },
                        icon = { MobileMainNavigationIcon(item, selected) },
                        label = {
                            Text(
                                item.label,
                                maxLines = 1,
                                style = MaterialTheme.typography.labelSmall.copy(letterSpacing = (-0.3).sp),
                                softWrap = false,
                                overflow = TextOverflow.Ellipsis,
                            )
                        },
                        alwaysShowLabel = true,
                    )
                }
            }
        }
        if (searchFabEnabled) FloatingActionButton(
            onClick = onSearchSelected,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(56.dp),
            shape = RoundedCornerShape(20.dp),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = stringResource(R.string.main_header_search),
            )
        }
        SeasonalBarDecoration(Modifier.align(Alignment.TopCenter))
    }
}
