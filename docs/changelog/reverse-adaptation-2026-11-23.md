# YANIME reverse adaptation to current Helandy upstream

Snapshot: 2026-11-23

## Immutable source and working branches

- Legacy YANIME snapshot: `backup/legacy-main-preserved-2026-11-23`
- Legacy head: `4f1517f5a7e6451b802a37d6331ca31942c03d84`
- Exact upstream base: `Helandy/YummyTV@792bbd9cf9d10bc4dd9667c2bc83a69791159470`
- Integration branch: `rebuild/yanime-on-upstream-main-2026-11-23`
- The integration branch is nine commits ahead of the upstream base; `main` has not been modified.

## Integration principle

The target is current Helandy architecture plus YANIME's product decisions and fork-specific behavior. The task is not to copy old files over new files, nor to adapt Helandy to old YANIME architecture.

For each legacy feature:
1. Identify its introducing commit and the exact code at that point.
2. Inspect the common ancestor version.
3. Trace the corresponding upstream changes after the fork's change.
4. Use the current upstream implementation as the structural base.
5. Recreate the intended YANIME behavior using current APIs, models, lifecycle and storage contracts.
6. Test compatibility and preserve user data.

Never restore a legacy file solely because it is fork-only. Verify dependencies, call sites, migrations and whether upstream deliberately replaced or removed it.

## Three-tree audit

Trees compared by path and blob SHA: common ancestor `6097ba265acc7adfab6bb22485fdcc1b6d9ec0df`, legacy YANIME head and upstream 1.31.0 snapshot.

| Metric | Count |
|---|---:|
| Common ancestor files | 4,348 |
| Legacy YANIME files | 4,482 |
| Upstream files | 4,613 |
| Legacy-changed paths | 1,075 |
| Upstream-changed paths | 1,107 |
| Changed by both sides | 699 |
| Same final blob on both sides | 398 |
| Different final blobs on both sides | 301 |
| Legacy-only changed paths | 376 |
| Upstream-only changed paths | 408 |

- The 398 converged paths need no duplicate port.
- The 301 divergent overlaps require three-way adaptation.
- The 376 legacy-only paths are candidates for review, not an automatic bulk copy.
- The 408 upstream-only paths are part of the new baseline and must remain.

## Adaptations already made

1. **Application identity**
   - `yummytv.applicationId` now resolves to `app.yanime`.
   - The explicit `yanime.applicationId` alias is retained for fork tooling.
2. **Deep links**
   - All eight custom URI intent filters now use `yanime`, not `yummytv`.
   - Newer upstream permissions and HTTPS intent filters were retained.
3. **Release-debug package identity**
   - Removed the upstream `.releasedebug` application ID suffix to preserve the legacy YANIME package identity for the releaseDebug artifact.
   - The ordinary debug variant keeps its separate `.debug` suffix.

## Feature migration worklist

### Player
- Rebuild the legacy frosted-glass controls and timeline on the current player implementation.
- Preserve the unified settings surface, time overlay preferences, elapsed/remaining/duration/finish-time modes and mobile/TV parity.
- Reconcile with upstream extractor, buffering, playback-speed, scrubbing and lifecycle changes.
- Do not restore old player files if upstream changed their contracts.

### Home feed and release tracking
- Preserve the distinction between episode premiere and actual YummyAnime video publication.
- Track episode/video/dubbing history so a late alternate dub of an old episode does not create a false new-episode card.
- Reimplement the tracker on current feed DTOs, cache models and repository contracts.
- First adapted slice: the current feed mapper now selects the highest regular episode per anime, ignores fractional specials, and suppresses older feed uploads when the existing local video cache already knows a newer episode count. Unknown counts do not suppress entries.
- The repository passes the current AnimeStorage.observeAvailableEpisodeCounts() snapshot into the mapper instead of introducing a parallel legacy storage API.
- Added mapper tests for stale alternate uploads, highest-episode selection and fractional specials. These tests are authored but have not yet been run.
- Preserve smart tracking and background notifications only after adapting to current worker and notification APIs.

### Schedule
- Preserve list-aware filtering, watched/favorite/hidden states, actual video release ordering and the intended compact 7-day schedule UX.
- Adapt to current schedule domain models and mappers; do not resurrect removed legacy types without checking upstream replacements.

### Navigation and design
- Preserve the minimal YANIME navigation/header behavior where compatible.
- Keep the established minimal/frosted-glass direction. Do not introduce a new theme system or broad redesign without an explicit requirement.
- Adapt components to current Material 3 and navigation contracts.

### Storage and migration
- Preserve every user-data migration and verify the current Room schema identity.
- Reconcile the legacy migration chain with upstream's current chain; do not drop or renumber historical migrations.

### Identity, CI and release delivery
- Keep `app.yanime`, YANIME branding, deep links, release endpoint and APK naming.
- Adapt the current upstream build workflow rather than replacing its Gradle/AGP setup with the old workflow.
- Verify releaseDebug and stable package IDs, signing behavior, update compatibility, R8 mapping and Room schema artifacts.

## Validation gate

Before promotion:
- Compile affected modules and assemble the YANIME debug APK.
- Run relevant unit/instrumentation tests for release tracking, schedule mapping, player settings and database migrations.
- Test Room upgrades from supported legacy schemas.
- Verify deep links, application identity, update endpoint and APK asset.
- Install over a previous YANIME build and verify data continuity.
- Compare the final feature inventory with the complete legacy commit register.

## Status

- [x] Exact legacy branch copied and verified byte-for-byte at its original head.
- [x] New integration branch created directly from the verified upstream main SHA.
- [x] Three-tree audit reproduced by full Git tree/blob comparison.
- [x] Application ID, URI scheme and releaseDebug package identity adapted.
- [x] Adapted the first home-feed release-filtering slice to current upstream storage and DTO contracts.
- [ ] Complete per-feature three-way migration.
- [ ] Complete build, runtime, migration and installation verification.
- [ ] Promote only after all gates pass; do not move `main` prematurely.
