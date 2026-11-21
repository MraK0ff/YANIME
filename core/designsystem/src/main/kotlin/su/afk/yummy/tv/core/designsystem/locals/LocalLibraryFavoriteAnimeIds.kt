package su.afk.yummy.tv.core.designsystem.locals

import androidx.compose.runtime.compositionLocalOf

/** Избранные animeId текущей локальной библиотеки — независимо от статуса списка. */
val LocalLibraryFavoriteAnimeIds = compositionLocalOf<Set<Int>> { emptySet() }