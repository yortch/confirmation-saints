# Legolas — History

## Project Context
- **Project:** confirmation-saints — Catholic Saints iOS App
- **User:** Jorge Balderas
- **Stack:** Swift / SwiftUI, iOS (iPhone + iPad)
- **Description:** App helping Catholic confirmation candidates (primarily teens, also adults) find and choose a patron saint. Features saint search by name, patron day, affinity, country, age, married status. Multilingual (EN/ES). Content sourced from Loyola Press, Focus, Lifeteen, Ascension Press, Hallow with attribution.
- **Key constraints:** Self-contained, easy content updates, cross-platform ready (Android later), include saint images with attribution.

## Learnings

### Test Foundation Ready (2026-04-12)
- **Gandalf** established Swift 6 concurrency foundation (Sendable models, @MainActor services)
- **Architecture**: MVVM with Observable macro — models are data-focused, services handle logic
- **XcodeGen setup** means .pbxproj regenerates from `project.yml` — never edit .pbxproj directly
- **Testable models**: Saint, Category, LocalizedText structs follow clean separation
- **Data layer ready**: 25 EN + 25 ES saints in `SharedContent/Data/saints-en/es.json`
- Test against: SaintDataService (JSON loading), LocalizedText (bilingual strings), filtering by affinity/category/country

### Welcome Screen & App Icon (2026-04-12)
- **Frodo** created `Views/Onboarding/WelcomeView.swift` — 4-page TabView onboarding with first-launch gating
- New files: `WelcomeView.swift`, modified `SettingsView.swift` (added "Show Welcome Screen" button)
- **Test coverage needed:** WelcomeView display logic, Settings button toggle, first-launch behavior
- **Samwise** generated app icon: 1024×1024 PNG with Chi-Rho design, purple gradient, gold accents, dove silhouette
- Icon integration complete; Xcode auto-generates smaller sizes from 1024×1024 source

### Anticipatory Android Test Scaffolding (2026-07-22)
- Aragorn was landing Phases 2–7 of the Android port on `squad/android-port` in parallel; my job was test scaffolding that does not depend on his yet-unwritten source.
- **Cross-platform parity guardrail** written as `tests/shared-content-parity.py` (Python, stdlib only). Enforces id parity, `sourceURLs` value-set parity, canonical field parity (`patronOf`, `affinities`, `tags`, `region`, `lifeState`, `ageCategory`, `gender`), per-saint image existence, and category group+value id parity. Exits 0/1/2 with diff on stderr. Ran against HEAD → **PASS** (70 saints in lockstep, no drift).
- **Rationale for Python (not XCTest / JUnit):** data invariants are language-agnostic; both platforms can share one guardrail instead of duplicating. Captured as `.squad/skills/cross-platform-json-parity-check/SKILL.md`.
- **Android unit test stubs** at `android/app/src/test/` — `SaintRepositoryTest`, `CategoryMatchingTest`, `LocalizationServiceTest`, `BirthDateParsingTest`. All `@Disabled` with TODO comments citing the specific contract from `.squad/decisions.md` (70-saint roster, English-canonical matching, in-app language switch, 0256 edge case). JUnit 5 (project already wires `junit-jupiter` + Turbine).
- **Android instrumentation stubs** at `android/app/src/androidTest/` — `WelcomeScreenNavigationTest`, `SaintListDisplayTest`, `LanguageSwitchTest`. `@Ignore`'d pending Aragorn's source.
- **CI hook** at `.github/workflows/android-ci.yml`, every job `if: false` so it's a scaffold only — flip on after Phases 2–7 stabilize.
- **Lane discipline:** wrote nothing under `android/app/src/main/` (Aragorn's lane). Found zero parity drift, so did NOT touch any JSON (Samwise's lane). Filed a decision inbox note at `.squad/decisions/inbox/legolas-parity-guardrail.md` asking Gandalf to confirm whether `country` should be canonical (currently excluded from the check — EN/ES country names may legitimately differ, e.g. "Italy" vs "Italia").
- **Key Android test conventions this session:**
  - JUnit 5 for `src/test/` (`org.junit.jupiter.api.Test`, `@Disabled`).
  - JUnit 4 for `src/androidTest/` (Compose UI test framework is still JUnit 4 — `org.junit.Test`, `@Ignore`). Do not accidentally mix these.
  - Package layout in test dirs mirrors `com.yortch.confirmationsaints` packages from `docs/android-architecture.md`.
