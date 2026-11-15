package su.afk.yummy.tv.core.designsystem.mobile

import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.snapshotFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.collect

/**
 * State holder for tabs backed by [PagerState].
 *
 * [settledPage] is deliberately used for selection: [PagerState.currentPage] changes while
 * a drag is still in progress and must not be treated as a committed selection.
 */
@Stable
class MobileSwipeableTabsState internal constructor(
    val pagerState: PagerState,
    private val scope: CoroutineScope,
) {
    /** Page whose scroll/settling operation has completed. */
    val settledPage: Int get() = pagerState.settledPage

    /** Compatibility alias for callers that need the selected, settled page. */
    val currentPage: Int get() = settledPage

    /** Animate to [index], ignoring stale indices and duplicate requests. */
    fun selectPage(index: Int) {
        if (index !in 0 until pagerState.pageCount) return
        if (pagerState.settledPage == index || pagerState.targetPage == index) return

        scope.launch {
            pagerState.animateScrollToPage(index)
        }
    }
}

/**
 * Remembers a pager-backed tab state and synchronizes it with an external selection.
 *
 * The external state is updated only after the pager settles. This prevents intermediate
 * [PagerState.currentPage] values during a gesture from feeding back into the pager and
 * starting competing scroll animations.
 *
 * [pageCount] may change dynamically. [selectedPage] is normalized to the current range.
 */
@Composable
fun rememberMobileSwipeableTabsState(
    selectedPage: Int,
    pageCount: Int,
    onPageSelected: (Int) -> Unit,
): MobileSwipeableTabsState {
    val safePageCount = pageCount.coerceAtLeast(1)
    val safeSelectedPage = selectedPage.coerceIn(0, safePageCount - 1)
    val pagerState = rememberPagerState(
        initialPage = safeSelectedPage,
        pageCount = { safePageCount },
    )
    val scope = rememberCoroutineScope()
    val state = remember(pagerState, scope) { MobileSwipeableTabsState(pagerState, scope) }

    val currentOnPageSelected = rememberUpdatedState(onPageSelected)
    val currentSelectedPage = rememberUpdatedState(safeSelectedPage)

    // External selection -> pager. LaunchedEffect cancels an obsolete animation when the
    // selected page changes again, and settledPage avoids restarting an already completed move.
    LaunchedEffect(safeSelectedPage, safePageCount, pagerState) {
        if (pagerState.settledPage != safeSelectedPage) {
            pagerState.animateScrollToPage(safeSelectedPage)
        }
    }

    // Pager -> external state. Observe only completed page transitions, never intermediate
    // currentPage updates produced while the user is dragging between pages.
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .collect { page ->
                if (page in 0 until pagerState.pageCount &&
                    page != currentSelectedPage.value
                ) {
                    currentOnPageSelected.value(page)
                }
            }
    }

    return state
}
