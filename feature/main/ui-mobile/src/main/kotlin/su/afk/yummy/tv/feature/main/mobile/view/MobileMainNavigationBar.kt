package su.afk.yummy.tv.feature.main.mobile.view

import su.afk.yummy.tv.core.designsystem.seasonal.seasonalNavigationBarItemColors
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.AnchoredDraggableState
import androidx.compose.foundation.gestures.DraggableAnchors
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.anchoredDraggable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import su.afk.yummy.tv.core.designsystem.mobile.input.clickablePointer
import su.afk.yummy.tv.core.designsystem.onboarding.onboardingTarget
import su.afk.yummy.tv.core.designsystem.seasonal.SeasonalBarDecoration
import su.afk.yummy.tv.feature.main.mobile.model.MobileMenuItem
import su.afk.yummy.tv.feature.main.mobile.utils.mobileMainTabTargetId
import su.afk.yummy.tv.feature.main.presentation.R

@Composable
internal fun <T> MobileMainNavigationBar(
    selectedDestination: T,
    menuItems: List<MobileMenuItem<T>>,
    containerColor: Color,
    upFocusRequester: FocusRequester?,
    classicStyle: Boolean = false,
    searchFabEnabled: Boolean = true,
    catalogFabEnabled: Boolean = false,
    onSearchSelected: () -> Unit,
    onCatalogRandomSelected: () -> Unit = {},
    onDestinationSelected: (T) -> Unit,
) {
    val orderedItems = remember(menuItems) { menuItems }
    val isClassic = classicStyle
    val selectedItemIndex = orderedItems.indexOfFirst { it.destination == selectedDestination }
        .takeIf { it >= 0 } ?: 0
    val dragState = remember {
        AnchoredDraggableState(initialValue = selectedItemIndex)
    }

    // Keep anchors tied to the measured navigation surface. The action tile is a
    // real slot in the classic bar, but never a destination in the swipe sequence.
    val dragModifier = Modifier
        .onSizeChanged { measuredSize ->
            val destinationCount = orderedItems.size
            if (destinationCount > 0 && measuredSize.width > 0) {
                val slotCount = destinationCount + if (isClassic && searchFabEnabled) 1 else 0
                val anchors = DraggableAnchors {
                    orderedItems.indices.forEach { index ->
                        val anchor = measuredSize.width.toFloat() *
                            (index + 0.5f) / slotCount.coerceAtLeast(1)
                        index at anchor
                    }
                }
                dragState.updateAnchors(anchors)
            }
        }
        .anchoredDraggable(
            state = dragState,
            orientation = Orientation.Horizontal,
        )

    // External navigation (deep links/back handling) and swipe navigation share
    // the same state. A destination is emitted only after the gesture settles.
    LaunchedEffect(selectedDestination, orderedItems) {
        dragState.snapTo(selectedItemIndex)
    }
    LaunchedEffect(dragState.settledValue) {
        if (orderedItems.isNotEmpty()) {
            val settledIndex = dragState.settledValue.coerceIn(0, orderedItems.lastIndex)
            val settledDestination = orderedItems.getOrNull(settledIndex)?.destination
            if (settledDestination != null && settledDestination != selectedDestination) {
                onDestinationSelected(settledDestination)
            }
        }
    }

    val actionEnabled = searchFabEnabled

    val onAction = {
        if (catalogFabEnabled) onCatalogRandomSelected() else onSearchSelected()
    }
    val actionIcon = if (catalogFabEnabled) Icons.Default.Shuffle else Icons.Default.Search
    val actionDescription = stringResource(
        if (catalogFabEnabled) R.string.main_header_random_anime else R.string.main_header_search,
    )


    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(
                start = if (isClassic) 0.dp else 8.dp,
                end = if (isClassic) 0.dp else 8.dp,
                bottom = if (isClassic) 0.dp else 8.dp,
            ),
    ) {
        if (isClassic) {
            Surface(
                modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().height(80.dp),
                color = containerColor,
                contentColor = MaterialTheme.colorScheme.onSurface,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp,
            ) {
                NavigationBar(
                    modifier = Modifier.fillMaxSize().then(dragModifier),
                    containerColor = Color.Transparent,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 0.dp,
                    windowInsets = WindowInsets(0.dp),
                ) {
                    val itemColors = seasonalNavigationBarItemColors()
                    orderedItems.forEachIndexed { index, item ->
                        val selected = index == dragState.currentValue.coerceIn(0, orderedItems.lastIndex)
                        NavigationBarItem(
                            modifier = Modifier
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
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            alwaysShowLabel = true,
                        )
                    }

                    if (actionEnabled && isClassic) {
                        NavigationBarItem(
                            modifier = Modifier.testTag("main_navigation_action"),
                            selected = false,
                            onClick = onAction,
                            colors = itemColors,
                            icon = {
                                Surface(
                                    modifier = Modifier.size(40.dp),
                                    shape = RoundedCornerShape(14.dp),
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(actionIcon, contentDescription = actionDescription, modifier = Modifier.size(24.dp))
                                    }
                                }
                            },
                            label = {
                                Text(
                                    if (catalogFabEnabled) "Рандом" else "Поиск",
                                    maxLines = 1,
                                    softWrap = false,
                                    overflow = TextOverflow.Ellipsis,
                                )
                            },
                            alwaysShowLabel = true,
                        )
                    }
                }
            }

        } else {
            Row(
                modifier = Modifier.align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.height(64.dp).then(dragModifier),
                    shape = RoundedCornerShape(24.dp),
                    color = MaterialTheme.colorScheme.surfaceContainerHigh,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 0.dp,
                    shadowElevation = 2.dp,
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                    ) {
                        orderedItems.forEachIndexed { index, item ->
                            val selected = index == dragState.currentValue.coerceIn(0, orderedItems.lastIndex)
                            val itemShape = CircleShape
                            Box(
                                modifier = Modifier
                                    .then(
                                        if (selected) Modifier
                                            .height(48.dp)
                                            .background(MaterialTheme.colorScheme.secondaryContainer, itemShape)
                                            .clickable { onDestinationSelected(item.destination) }
                                            .padding(horizontal = 10.dp)
                                        else Modifier
                                            .size(48.dp)
                                            .background(Color.Transparent, itemShape)
                                            .clickable { onDestinationSelected(item.destination) },
                                    )
                                    .onboardingTarget(mobileMainTabTargetId(item.destination))
                                    .testTag("main_tab")
                                    .focusProperties {
                                        upFocusRequester?.takeIf { selected }?.let { up = it }
                                    }
                                    .animateContentSize(),
                                contentAlignment = Alignment.Center,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    MobileMainNavigationIcon(item, selected)
                                    AnimatedVisibility(
                                        visible = selected,
                                        enter = fadeIn(),
                                        exit = fadeOut(),
                                    ) {
                                        Text(
                                            text = item.label,
                                            maxLines = 1,
                                            softWrap = false,
                                            overflow = TextOverflow.Clip,
                                            fontSize = 12.sp,
                                            lineHeight = 16.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                if (actionEnabled) {
                    FloatingActionButton(
                        onClick = onAction,
                        modifier = Modifier.size(56.dp),
                        shape = CircleShape,
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ) {
                        Icon(actionIcon, contentDescription = actionDescription, modifier = Modifier.size(24.dp))
                    }
                }
            }
        }

        if (isClassic) SeasonalBarDecoration(Modifier.align(Alignment.TopCenter))
    }
}
