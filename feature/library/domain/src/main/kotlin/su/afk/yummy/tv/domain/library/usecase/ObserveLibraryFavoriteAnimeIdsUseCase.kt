package su.afk.yummy.tv.domain.library.usecase

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import su.afk.yummy.tv.domain.library.repository.LibraryRepository
import javax.inject.Inject

/** Наблюдает избранные тайтлы локальной библиотеки, включая гостевой режим и офлайн. */
class ObserveLibraryFavoriteAnimeIdsUseCase @Inject constructor(
    private val repository: LibraryRepository,
) {
    operator fun invoke(): Flow<Set<Int>> = repository.observeAll()
        .map { items -> items.asSequence().filter { it.isFavorite }.map { it.animeId }.toSet() }
        .distinctUntilChanged()
}