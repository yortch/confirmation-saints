# Gandalf — History

## Project Context
- **Project:** confirmation-saints — Catholic Saints iOS/Android App
- **User:** Jorge Balderas
- **Stack:** Swift/SwiftUI (iOS), Kotlin/Compose (Android), shared `SharedContent/` JSON as canonical cross-platform data source
- **Description:** Helps Catholic confirmation candidates (teens/adults) find a patron saint via search by name, patron day, affinity, country, age, married status. Multilingual (EN/ES).

## Condensed Learnings (summarized 2026-09-20; full detail archived in git history of this file)

- **Architecture:** MVVM + SwiftUI (Observable, Swift 6 concurrency), XcodeGen (`project.yml`), dual localization (`.xcstrings` for UI, JSON `LocalizedText`/per-language files for content). `SharedContent/` is the single source of truth for both platforms — Android wires it via Gradle asset source-set, never forks it.
- **Android port:** Package `com.yortch.confirmationsaints`; StateFlow+DataStore replace @Observable/@AppStorage; CompositionLocal for language (not system locale/strings.xml); kotlinx.serialization; Hilt DI; Coil 3 for images.
- **Data model evolution:** Migrated from `LocalizedText{en,es}` to per-language `saints-en.json`/`saints-es.json` with canonical English matching fields (`patronOf`/`tags`/`affinities`/`region`/`lifeState`/`ageCategory`/`gender`) + parallel `display*` arrays. `sources`/`sourceURLs` parallel-array bug class fixed by collapsing to ordered `{name, url}` array.
- **Canonical status verification:** "Saint" vs "Blessed" is never implicit — always verify via Wikipedia canonization/beatification sections (e.g., Sára Salkaházi and Miguel Pro are Blessed, not canonized; require "Bl."/"Bta." prefix + `canonizationDate: null`).
- **Saint count / marketing copy:** Roster grew 70→81→103+ over multiple batches; marketing copy (README, docs/index.html, docs/appstore/*, docs/android/*) must be updated in lockstep across all locations each time the count changes — track a single source of truth to avoid drift.
- **Modern Day Saints filter:** Deterministic definition — birth year >= 1900, computed identically in iOS `matchesEra()` and Android `CategoryMatcher.matchesEra()`. New filters should always be defined as deterministic functions of existing data fields with exact cross-platform matching logic specified before implementation.
- **Workflow:** Multiple approved-but-uncommitted feature batches can coexist in the working tree; document batch composition/approval status in decisions/inbox and confirm batch independence with validators rather than blocking on a single-feature-per-review rule.
- **Platform-specific docs:** iOS/Android submission notes are kept in separate files (`docs/appstore/submission-info.md` vs `docs/android/submission-info.md`) since platform release content can diverge even when sharing content/version.

### Reviewed & Approved: Maria Troncatti Spanish Attribution Fix (2026-09-20)
- Reviewed Samwise's `maria-troncatti` Spanish `image.attribution` fix in `saints-es.json` and Legolas's new bilingual attribution parity + Troncatti sentinel tests in `tests/shared-content-parity.py`.
- Captured governed policy: every user-visible image attribution/source string must be localized in EN and ES; attribution changes must update parity tests.
- **APPROVED.** `python3 tests/shared-content-parity.py` passes; targeted Android content tests pass. No product commit requested/created.

### Approved: v1.1.1 Release Readiness (2026-09-20)
- Reviewed Frodo's iOS 1.1.1/build 15 prep (11/11 XCTest pass, docs updated) and Aragorn's Android 1.1.1/versionCode 6 prep (46/46 unit tests pass, signed upload-key AAB verified), plus the Troncatti attribution fix/parity regression.
- **APPROVED** local release readiness for v1.1.1 on both platforms.
- Captured governed directive: repo uses Xcode Cloud for iOS releases — after approval, commit/push working branch, merge to `main`, push `main` to trigger the Cloud build; no local iOS archive required (policy `d3f62297`).
- Remaining execution (product commit/push/merge, Android Play Console upload) is outside Gandalf's/Scribe's scope.
