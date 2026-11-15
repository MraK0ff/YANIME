# Технический аудит YANIME — feature/material3-expressive

Дата фиксации аудита: 2026-11-15  
Ветка: `feature/material3-expressive`  
Аудируемый HEAD: `31e9e5f024d9b4f1d13cb0a7b07415ec440e694a`

Этот документ фиксирует проверенные факты до дальнейшей модернизации. Он не означает, что все перечисленные системы уже завершены или прошли runtime-проверку.

## 1. Структура и границы модулей

В `settings.gradle.kts` обнаружено 181 включение Gradle-модуля. Репозиторий разделён на `app`, `core`, `feature` и `baselineprofile`.

Среди feature-доменов присутствуют: main, commonScreen, home, catalog, library, details, player, settings, playersetup, comments, reviews, posts, messages, bloggers, faq, glossary, pages, video-download, watch-later, watching, account, collection, schedule и update.

У ключевых функций используются отдельные модули API/domain/data/presentation/ui-mobile/ui-tv там, где это соответствует их ответственности. Нельзя считать, что все feature-модули имеют одинаковый набор слоёв: миграции должны учитывать реальную структуру каждого feature.

Общая дизайн-система размещена в `:core:designsystem`. Player разделён на API, domain, data, presentation, ui-common, ui-mobile и ui-tv.

## 2. Зафиксированный технологический стек

Значения прочитаны из `gradle/libs.versions.toml`:

| Компонент | Версия |
|---|---:|
| compileSdk | 37 |
| minSdk | 24 |
| Android Gradle Plugin | 9.5.1 |
| Kotlin | 2.4.21 |
| Compose BOM | 2026.09.00 |
| Material 3 | 1.5.0-rc01 |
| Activity | 1.13.0 |
| Core KTX | 1.19.1 |
| Lifecycle | 2.11.0 |
| Paging | 3.5.3 |
| Media3 | 1.11.1 |
| Coil | 3.6.3 |
| Ktor | 3.6.1 |
| OkHttp | 5.5.1 |
| Room | 2.8.5 |
| DataStore | 1.3.0 |
| WorkManager | 2.12.0 |
| Benchmark | 1.5.0 |

Версии являются зафиксированным состоянием каталога, а не утверждением о том, что каждая зависимость уже прошла runtime- и device-валидацию. Для обновления требуется сверка release notes, совместимости плагинов, разрешённого dependency graph и компиляции приложения.

Material 3 1.5.0-rc01 пока оставлен осознанно: текущая тема вызывает `MaterialExpressiveTheme` и `MotionScheme.expressive()`. Понижение без проверки доступности API в stable-артефакте недопустимо. Отдельная миграция нужна после выхода совместимой стабильной версии.

## 3. Дизайн-система

Подтверждено в `YummyTvTheme.kt`:

- единая точка входа `YummyTvTheme`;
- официальный `MaterialExpressiveTheme`;
- `MotionScheme.expressive()`;
- Dynamic Color на поддерживаемых Android;
- отдельные фирменные палитры;
- светлая, тёмная и AMOLED-схемы;
- отдельные mobile/TV typography;
- восстановление системных цветов status/navigation bars при возврате Activity в foreground.

Документация перечисляет общие токены spacing, shapes и motion. Наличие токенов не гарантирует, что каждый глубоко вложенный экран уже использует их. Нужен инвентаризационный проход по каждому UI-модулю и обоим форм-факторам.

## 4. Glass UI: фактическое состояние

`MobilePlayerGlassStyle.kt` реализует полупрозрачную заливку, clip, border и опциональную elevation. Это translucent surface, а не backdrop blur.

`BlurredBackdropPoster.kt` использует `Modifier.blur()` для самого изображения-постера. Это не размытие содержимого позади composable.

Для player overlay в том же окне нельзя объявлять backdrop blur реализованным без проверки платформенного механизма. Window Blur API относится к отдельным окнам; `Modifier.blur()`/RenderEffect не являются универсальной заменой backdrop blur. Для текущих in-window player controls безопасная базовая реализация — контрастная полупрозрачная поверхность. Настоящий window blur требует отдельного прототипа на Dialog/Popup, API-level fallback и проверки на устройстве.

## 5. Мобильный плеер

Подтверждённая точка входа: `feature/player/ui-mobile/.../PlayerMobileScreen.kt`.

Наблюдаемые архитектурные элементы:

- состояние экрана поступает извне как `PlayerState.State`, события передаются через `onEvent`, одноразовые эффекты — через `Flow<PlayerState.Effect>`;
- используется `rememberPlayerPlaybackUiState` для UI-представления состояния;
- есть `PlayerServiceWarmupEffect`;
- переходы/ошибки resolver обрабатываются отдельно от активного потока;
- `lastReadyPlayer` сохраняет последний готовый поток, пока загружается следующий;
- выбор качества, озвучки и балансера вынесен в отдельные sheet-компоненты;
- управление ориентацией, системными панелями и PiP имеет отдельные эффекты/контроллеры.

Перед любыми изменениями нужно проследить `MobileNativePlayer`, service/session lifecycle, overlay, gesture controller, track selection, PiP и TV player. Чисто визуальная задача не должна незаметно менять playback lifecycle.

## 6. CI и сборка

Workflow: `.github/workflows/android-build.yml`.

Проверенный workflow:
- запускается на push, pull request и вручную;
- отменяет устаревшие запуски на том же ref;
- устанавливает JDK 21 и Android SDK platform из Version Catalog;
- выполняет `:app:compileReleaseDebugKotlin`;
- собирает `:app:assembleReleaseDebug`;
- загружает APK, R8 mapping и Gradle profile;
- отдельно подписывает собранный APK публичным Android testkey;
- публикует релиз только для push в main.

Следовательно, реальная текущая CI-сборка — вариант `releaseDebug`, а не просто `assembleDebug`. Перед изменением workflow необходимо сохранить этот контракт и понять назначение R8/signing/release job.

## 7. Инструкции проекта

Корневой `AGENTS.md` уже содержит правила для архитектуры, Compose, API, lifecycle, CI и Git. В рамках аудита добавлены отдельные правила:
- проверка фактической доступности Material 3 Expressive API перед сменой версии;
- различение translucent surface и backdrop blur;
- ограничения Window Blur API и SurfaceView;
- предварительный аудит player lifecycle перед изменениями.

## 8. Проверки и ограничения аудита

Аудит выполнен чтением файлов и Git tree выбранной ветки через GitHub. В этой среде не запускались Gradle configuration, dependency resolution, Kotlin compilation, instrumentation tests, Macrobenchmark или установка APK на физическое устройство.

До заявления о стабильности требуется выполнить как минимум:
1. `./gradlew help` и проверку разрешения зависимостей;
2. compile-задачи затронутых модулей;
3. `:app:compileReleaseDebugKotlin`;
4. `:app:assembleReleaseDebug`;
5. проверку итогового APK и GitHub Actions conclusion.

## 9. Следующие этапы

1. Foundation: dependency graph, Gradle/build-logic, release-note compatibility и baseline сборка.
2. Design System: аудит реальных usages токенов и компонентов во всех feature-модулях.
3. Player: трассировка engine/session/lifecycle и аудит жестов/overlay до точечных изменений.
4. Screens: миграция оставшихся экранов по feature и mobile/TV отдельно.
5. Performance/accessibility: измерения до/после и исправления по профилю, а не на глаз.
6. Final verification: компиляция, сборка, APK artifact и runtime-проверка на целевых устройствах.


## 10. Дополнительный аудит Compose Lazy layouts — 2026-11-15

Проведён дополнительный проход по мобильным и TV UI-модулям с проверкой LazyColumn, LazyRow, LazyVerticalGrid, items, itemsIndexed, ключей элементов, Paging-placeholder и contentType.

### Что исправлено

- Мобильные экраны: главная/продолжение просмотра, библиотека и история, профиль и аккаунт, поиск пользователей, коллекции, рекомендации, связи аниме, FAQ, глоссарий, скриншоты, озвучки, подписки, страницы сайта, отзывы и порядок просмотра.
- TV-экраны: уведомления, блогеры, каталоги коллекций, экран коллекций, озвучки, связи аниме, скриншоты, похожие тайтлы, главная/продолжение просмотра, история библиотеки, посты, отзывы и подписки.
- Для гетерогенных lazy-layout добавлены осмысленные contentType для карточек, заголовков, пустых/ошибочных/загрузочных состояний и смешанных блоков контента.
- Для Paging history различаются загруженные записи и placeholder; placeholder сохраняют отдельные уникальные ключи по индексу.
- На главной ключ «Продолжить просмотр» опирается на стабильный animeId, а не на позицию и изменяемые поля записи.
- Исправление ключей Paging-placeholder защищено отдельным regression test в LazyKeysTest.

### Важные решения аудита

- Не удалять индекс из ключа автоматически: для null-placeholder он необходим, а некоторые списки намеренно допускают повторяющиеся сущности/позиционные блоки.
- lazyKey(prefix, id, index) уже игнорирует index для непустого ID и использует его только для placeholder. Поэтому вызовы с дополнительным index не обязательно создают нестабильный ключ.
- Для TV focus restoration ключ lazy item должен совпадать с ключом, используемым сохранением и восстановлением фокуса. Изменение такого ключа требует отдельной проверки сценариев удаления и reorder.
- contentType задаёт тип структуры композиции, а не уникальность элемента. Не следует добавлять ID или index в content type.

### CI

- Подтверждён успешный workflow 43750266076 для коммита bbd064456017b8fb44258d920636e96f0317b931.
- Для более позднего HEAD e234ed26daabc81c892014a98db3f889d723c382 запущен workflow 43752106813; на момент фиксации обновления он ещё выполнялся. Его результат не считается подтверждённым.
- Промежуточные запуски отменялись последующими push в ту же ветку; дальнейшие изменения следует группировать крупнее и проверять один итоговый запуск.

Этот проход повышает предсказуемость Compose lazy layouts, но не заменяет runtime-проверку на Android TV и мобильных устройствах, Macrobenchmark и проверку восстановления фокуса при реальных обновлениях данных.


## 11. Общие компоненты и токены форм — 2026-11-15

Продолжен аудит общих компонентов дизайн-системы:
- MobilePosterCard и MobileTitleRowCard используют семантическую роль Button и общие shape/spacing tokens.
- TvTitleCard использует общий TV focus/click контракт и YummyShapeTokens.Card.
- MobileSettingToggleRow использует единый toggleable semantics Role.Switch, а вложенный Switch не создаёт дублирующее accessibility-действие.
- MobileSettingOptionRow предоставляет Role.Button и учитывает disabled state.
- TvChip использует Role.Tab, selected state и TV focus feedback.
- StateMessage и TvRetryButton приведены к общим YummyShapeTokens/YummySpacing вместо локально заданных размеров и геометрии.

Не добавлялись искусственные accessibility-обёртки поверх уже корректных semantics. Следующие проверки должны включать TalkBack и реальное управление D-pad, поскольку чтение исходников не заменяет runtime-аудит.
