package su.afk.yummy.tv.feature.schedule

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import su.afk.yummy.tv.core.model.text.UiText
import su.afk.yummy.tv.core.mvi.UiEffect
import su.afk.yummy.tv.core.mvi.UiEvent
import su.afk.yummy.tv.core.mvi.UiState
import su.afk.yummy.tv.domain.schedule.model.AnimeScheduleDay
import su.afk.yummy.tv.feature.schedule.model.ScheduleTimelineUi

class ScheduleState {
    enum class LibraryFilter { ALL, LIBRARY }

    @Immutable
    data class State(
        val isLoading: Boolean = true,
        /** Идёт обновление по жесту pull-to-refresh поверх уже показанного расписания. */
        val isRefreshing: Boolean = false,
        val days: ImmutableList<AnimeScheduleDay> = persistentListOf(),
        val tvSchedule: ScheduleTimelineUi = ScheduleTimelineUi(),
        val libraryFilter: LibraryFilter = LibraryFilter.ALL,
        val hideWatched: Boolean = false,
        val error: UiText? = null,
    ) : UiState {
        /** Совместимость с актуальным upstream UI, где фильтр пока представлен переключателем. */
        val onlyInLibrary: Boolean get() = libraryFilter == LibraryFilter.LIBRARY
    }

    /** Пользовательские действия на экране расписания. */
    sealed interface Event : UiEvent {
        data object BackSelected : Event
        data object RetrySelected : Event
        data object RefreshRequested : Event
        data object InLibraryFilterToggled : Event
        data class DateSelected(val epochDay: Long) : Event
        data class LibraryFilterSelected(val filter: LibraryFilter) : Event
        data class HideWatchedToggled(val enabled: Boolean) : Event
        data class AnimeSelected(val animeId: Int) : Event
    }

    sealed interface Effect : UiEffect
}
