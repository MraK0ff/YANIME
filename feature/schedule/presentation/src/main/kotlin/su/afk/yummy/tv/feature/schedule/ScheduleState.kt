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
        val onlyInLibrary: Boolean get() = libraryFilter == LibraryFilter.LIBRARY,
        val hideWatched: Boolean = false,
        val error: UiText? = null,
    ) : UiState

    /** Пользовательские действия на экране расписания. */
    sealed interface Event : UiEvent {
        /** Пользователь вернулся на предыдущий экран. */
        data object BackSelected : Event

        /** Пользователь запросил повторную загрузку расписания. */
        data object RetrySelected : Event

        /** Пользователь потянул список вниз, чтобы обновить расписание. */
        data object RefreshRequested : Event

        /** Пользователь выбрал дату по значению epoch day. */
        data class DateSelected(val epochDay: Long) : Event

        data class LibraryFilterSelected(val filter: LibraryFilter) : Event

        /** Совместимость с upstream UI, пока его фильтр остаётся переключателем. */
        data object InLibraryFilterToggled : Event

        data class HideWatchedToggled(val enabled: Boolean) : Event

        /** Пользователь выбрал аниме с указанным идентификатором. */
        data class AnimeSelected(val animeId: Int) : Event
    }

    sealed interface Effect : UiEffect
}
