# Reverse adaptation: YANIME on Helandy 1.31.0

Snapshot: 2026-11-23

## Branch safety

- Preserved legacy YANIME tree: `backup/legacy-main-preserved-2026-11-23`
- Preserved legacy head: `4f1517f5a7e6451b802a37d6331ca31942c03d84`
- New integration work branch: `rebuild/yanime-reverse-adaptation-1.31.0`
- New integration base: `6eb84ed9603f371133cd50c84207d181d122c7a0`
- Verified upstream snapshot: `Helandy/YummyTV@792bbd9cf9d10bc4dd9667c2bc83a69791159470`

The legacy branch is immutable during this work. The integration branch is separate; `main` is not moved by this reconstruction.

## Correct integration rule

The target is not the old YANIME tree with upstream patches applied. The target is the current Helandy architecture with YANIME's product decisions and fork-specific behavior reimplemented on top.

For each legacy feature, inspect:
1. the common ancestor state;
2. the exact legacy YANIME implementation and its introducing commit;
3. the upstream evolution after that point;
4. the current upstream implementation and extension points;
5. the behavior that must survive, then implement that behavior using current APIs and architecture.

Do not copy a legacy file wholesale when upstream changed or removed that file. Do not restore code merely because its path is fork-only: verify dependencies, call sites, migrations, and current upstream equivalents.

## Three-tree audit

Git trees were compared by path and blob SHA for the common ancestor, legacy YANIME, and upstream 1.31.0.

| Metric | Count |
|---|---:|
| Common ancestor files | 4,348 |
| Legacy YANIME files | 4,482 |
| Upstream 1.31.0 files | 4,613 |
| Legacy-changed paths | 1,075 |
| Upstream-changed paths | 1,107 |
| Paths changed by both | 699 |
| Both sides end with identical blob | 398 |
| Both sides end with different blobs | 301 |
| Legacy-only changed paths | 376 |
| Upstream-only changed paths | 408 |

Interpretation:
- 398 overlapping paths already converge and should not be reimplemented.
- 301 overlapping paths need three-way adaptation; upstream is the structural base.
- 376 legacy-only paths are candidates, not automatic copy targets. Check whether upstream removed/replaced their dependencies.
- 408 upstream-only paths are part of the new baseline and must remain.

## First verified identity adaptation

The upstream manifest still declared the custom URI scheme `yummytv` in eight intent-filter entries. The legacy YANIME manifest used `yanime`. The integration branch now restores `yanime` in all eight entries without replacing the current upstream manifest or discarding its newer permissions/intent filters.

Commit: `e1a75a9a5f7857629267e10c67dba8b8b385d546`

The application ID remains `app.yanime` through the current Gradle property `yummytv.applicationId`; retain the property name where upstream build logic expects it.

## Feature migration worklist

### Product identity and delivery
- Preserve app ID `app.yanime`, YANIME display name, icon/branding, URI scheme, YANIME release endpoint and APK naming.
- Keep upstream Gradle/AGP changes; adapt CI and release workflow rather than reverting to the legacy workflow.
- Verify debug/release package IDs, update compatibility, release assets, R8 mapping and Room schema artifacts.

### Player
- Reimplement the legacy glass/frosted mobile player, timeline and controls using the current player state and UI contracts.
- Preserve the single unified settings surface, time overlay preferences, timecode modes and TV parity.
- Reconcile with upstream player fixes, extractor changes, playback-speed behavior and the current 1.31.0 player lifecycle.
- Do not restore old player files where upstream replaced their contracts.

### Home feed and release tracking
- Preserve the distinction between anime premiere and actual YummyAnime video publication.
- Keep per-episode/per-video/dubbing awareness so later alternate dubs do not create false duplicate episode cards.
- Rebuild the legacy release-history tracker on current home-feed models, cache and API contracts.
- Preserve smart new-episode tracking and background notifications only after checking current worker, notification and account APIs.

### Schedule
- Preserve user-list-aware filtering, watched/favorite/hidden states, actual video release ordering and the intended 7-day schedule UX.
- Adapt to current schedule domain models and mapper contracts; do not restore removed legacy model classes without checking upstream replacements.

### Navigation and design
- Preserve the minimal YANIME navigation and mobile header/bottom-bar behavior where still compatible.
- Do not impose a new theme system or blanket visual redesign. Retain the established minimal/frosted-glass direction and adapt components to the current Material 3 implementation.

### Storage and migrations
- Preserve every user-data migration and ensure the current Room schema identity matches the generated schema.
- Review old migrations against the current upstream migration chain; never drop or renumber historical migrations casually.

## Required validation before promotion

- Compile all affected modules and assemble the YANIME debug APK.
- Run relevant unit tests, especially release tracking, schedule mapping, player settings and migration tests.
- Verify Room upgrades from supported legacy schemas.
- Verify YANIME deep links, app identity, update endpoint and release asset.
- Install over a previous YANIME build and verify data continuity.
- Compare the final feature inventory against the legacy commit register before considering `main`.

## Current status

- [x] Legacy tree copied to a dedicated backup branch.
- [x] Separate integration branch created from the 1.31.0-era upstream base.
- [x] Three-tree path/blob audit reproduced.
- [x] First identity-specific adaptation committed: restore YANIME URI scheme.
- [ ] Complete per-feature three-way migration.
- [ ] Complete runtime, migration and build verification.
- [ ] Promote only after review; do not force-update `main` during the work.
