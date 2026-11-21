package su.afk.yummy.tv.feature.main.mobile.view

import su.afk.yummy.tv.core.designsystem.seasonal.seasonalNavigationBarItemColors
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusProperties
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
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
import kotlin.math.abs
import kotlin.math.roundToInt

private const val CLASSIC_ACTION_TILE = 0
private const val EXPRESSIVE_FLOATING = 1

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
    var dragPreviewIndex by remember { mutableStateOf<Float?>(null) }
    var panelVariant by remember(classicStyle) {
        mutableIntStateOf(if (classicStyle) CLASSIC_ACTION_TILE else EXPRESSIVE_FLOATING)
    }
    var showVariantMenu by remember { mutableStateOf(false) }

    val isClassic = panelVariant == CLASSIC_ACTION_TILE
    val actionInBar = isClassic
    val actionEnabled = searchFabEnabled

    val onAction = {
        if (catalogFabEnabled) onCatalogRandomSelected() else onSearchSelected()
    }
    val actionIcon = if (catalogFabEnabled) Icons.Default.Shuffle else Icons.Default.Search
    val actionDescription = stringResource(
        if (catalogFabEnabled) R.string.main_header_random_anime else R.string.main_header_search,
    )

    LaunchedEffect(selectedDestination) { dragPreviewIndex = null }

    val selectedItemIndex = orderedItems.indexOfFirst { it.destination == selectedDestination }
        .takeIf { it >= 0 }
    val animatedPreviewIndex by animateFloatAsState(
        targetValue = dragPreviewIndex ?: (selectedItemIndex?.toFloat() ?: -1f),
        animationSpec = spring(dampingRatio = 0.92f, stiffness = 900f),
        label = "navigationDragPreview",
    )

    val dragModifier = Modifier.pointerInput(orderedItems, selectedDestination) {
        if (orderedItems.size < 2) return@pointerInput
        var totalDrag = 0f
        var startIndex = 0
        var previewIndex = 0f
        var lastFingerX = 0f
        detectHorizontalDragGestures(
            onDragStart = { offset ->
                totalDrag = 0f
                startIndex = orderedItems.indexOfFirst {
                    it.destination == selectedDestination
                }.coerceAtLeast(0)
                lastFingerX = offset.x
                previewIndex = (offset.x / size.width.coerceAtLeast(1) * orderedItems.size)
                    .coerceIn(0f, orderedItems.lastIndex.toFloat())
                dragPreviewIndex = previewIndex
            },
            onHorizontalDrag = { change, amount ->
                change.consume()
                totalDrag += amount
                lastFingerX = change.position.x
                previewIndex = (lastFingerX / size.width.coerceAtLeast(1) * orderedItems.size)
                    .coerceIn(0f, orderedItems.lastIndex.toFloat())
                dragPreviewIndex = previewIndex
            },
            onDragEnd = {
                val nearest = previewIndex.roundToInt().coerceIn(0, orderedItems.lastIndex)
                val target = if (nearest == startIndex && abs(totalDrag) > 24f) {
                    (startIndex + if (totalDrag < 0f) 1 else -1)
                        .coerceIn(0, orderedItems.lastIndex)
                } else nearest
                if (target != startIndex) {
                    dragPreviewIndex = target.toFloat()
                    onDestinationSelected(orderedItems[target].destination)
                } else dragPreviewIndex = null
            },
            onDragCancel = { dragPreviewIndex = null },
        )
    }

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
                        val selected = animatedPreviewIndex >= 0f &&
                            index == animatedPreviewIndex.roundToInt().coerceIn(0, orderedItems.lastIndex)
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

                    if (actionEnabled && actionInBar) {
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

            if (actionEnabled && actionFloatsAbove) {
                FloatingActionButton(
                    onClick = onAction,
                    modifier = Modifier.align(Alignment.TopEnd).offset(y = (-28).dp).padding(end = 16.dp).size(56.dp),
                    shape = CircleShape,
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ) {
                    Icon(actionIcon, contentDescription = actionDescription, modifier = Modifier.size(24.dp))
                }
            }
        } else {
            Row(
                modifier = Modifier.align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Surface(
                    modifier = Modifier.height(64.dp),
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
                            val selected = animatedPreviewIndex >= 0f &&
                                index == animatedPreviewIndex.roundToInt().coerceIn(0, orderedItems.lastIndex)
                            val itemShape = CircleShape
                            Box(
                                modifier = Modifier
                                    .then(
                                        if (selected) Modifier
                                            .height(48.dp)
                                            .clip(itemShape)
                                            .background(MaterialTheme.colorScheme.secondaryContainer)
                                            .clickable { onDestinationSelected(item.destination) }
                                            .padding(horizontal = 10.dp)
                                        else Modifier
                                            .size(48.dp)
                                            .clip(itemShape)
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

        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(y = if (isClassic) (-44).dp else (-42).dp)
                .padding(end = if (isClassic) 8.dp else if (actionEnabled) 68.dp else 8.dp),
        ) {
            IconButton(onClick = { showVariantMenu = true }) {
                Icon(Icons.Default.Tune, contentDescription = "Выбрать вариант панели", tint = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            DropdownMenu(expanded = showVariantMenu, onDismissRequest = { showVariantMenu = false }) {
                listOf(
                    "Классическая · действие в панели",
                    "Современная · компактная капсула",
                ).forEachIndexed { index, label ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        leadingIcon = {
                            if (panelVariant == index) Icon(Icons.Default.Tune, contentDescription = null)
                        },
                        onClick = {
                            panelVariant = index
                            showVariantMenu = false
                        },
                    )
                }
            }
        }

        if (isClassic) SeasonalBarDecoration(Modifier.align(Alignment.TopCenter))
    }
}
