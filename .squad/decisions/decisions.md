# Decisions — confirmation-saints

## Active Decisions (2026-04-21 and later)

### Decision: Android asset layout: flat, not nested `SharedContent/...`

**Author:** Aragorn (Android Dev)
**Date:** 2026-04-22
**Status:** Proposed — awaiting @yortch confirmation

#### Context

`docs/android-architecture.md` §3.2 suggested bundling content under
`app/src/main/assets/SharedContent/{saints,categories,images}/...`, preserving
the repo-root directory structure. The existing scaffold's `syncSharedContent`
Gradle task already writes to a flat layout:

```
assets/
├── saints-en.json
├── saints-es.json
├── categories-en.json
├── categories-es.json
├── confirmation-info-en.json   ← added in Phase 2
├── confirmation-info-es.json
└── images/*.jpg
```

#### Decision

**Keep the flat layout.** `SaintRepository` / `CategoryRepository` load by
direct filename (`saints-en.json`, etc.), and `SaintImage.filename` is a bare
`.jpg` that Coil resolves via `file:///android_asset/images/{filename}`.

#### Why

- The scaffold already shipped with this layout and Jorge's build pipeline
  (CI + local Android Studio) was tested against it.
- iOS does not use `SharedContent/` as a runtime path either — it copies files
  into the bundle root. Keeping Android similar reduces "where do I find the
  JSON at runtime?" confusion.
- The nested path would force a rename migration for no load-time benefit —
  `AssetManager.open(...)` cost is identical either way.

#### Consequences

- Every Kotlin `open("saints-en.json")` call is correct as written.
- If we ever split content into many more sub-directories, revisit this.
- `android/README.md` now documents the flat layout as canonical for Android.

---

### Decision: Kotlin 2.0.20 + KSP 2.0.20-1.0.25, not Kotlin 2.1.0

**Author:** Aragorn (Android Dev)
**Date:** 2026-04-22
**Status:** Proposed

#### Context

`docs/android-architecture.md` §Appendix A suggests Kotlin 2.1.0. The existing
scaffold is on Kotlin 2.0.20, and Hilt 2.52 + Compose BOM 2024.09 + KSP
2.0.20-1.0.25 are all known to interop cleanly at that version.

#### Decision

Stay on **Kotlin 2.0.20 / KSP 2.0.20-1.0.25** for Phases 2–7. Upgrade to
Kotlin 2.1.x as a follow-up once the build is green and tests pass.

#### Why

- I cannot build locally to verify 2.1.0 compatibility.
- KSP's plugin coordinate is tied to the Kotlin version (`2.0.20-1.0.25`
  vs. `2.1.0-x.y.z`); bumping both in one step + other phase work is risky.
- Compose BOM 2024.09 has explicit 2.0.20 compat; 2024.12 is the BOM that
  pairs with Kotlin 2.1.0.

#### Follow-up

Once Jorge confirms phases 2–7 compile & run, bump in one atomic commit:
`kotlin = "2.1.0"`, `ksp = "2.1.0-1.0.29"`, `compose-bom = "2024.12.01"`.

---

### Decision: Android Navigation — Per-Tab Nested Graphs

**Author:** Aragorn (Android Dev)
**Status:** Proposed
**Date:** 2026-04-22

#### Context
The Android bottom-nav was using a single flat NavHost with all destinations (About, Explore, Saints, Settings, SaintDetail, CategorySaints) as siblings. Tab clicks called a standard `navigate { popUpTo(graph.startDestination) { saveState=true }; launchSingleTop=true; restoreState=true }` helper.

This produced a user-visible bug: from Explore → category → SaintDetail, tapping the Saints tab did *nothing* on the first attempt (before Saints had ever been visited in the session). `restoreState = true` combined with `launchSingleTop` short-circuits when there is no saved-state bundle yet for the target destination, so the tab switch became a no-op.

#### Decision
Refactor to a **nested-graph-per-tab** structure:

```
NavHost(startDestination = Screen.Saints) {
  navigation<Screen.About>(startDestination = Screen.AboutHome)       { … }
  navigation<Screen.Explore>(startDestination = Screen.ExploreHome)   { … + CategorySaints + SaintDetail }
  navigation<Screen.Saints>(startDestination = Screen.SaintsHome)     { … + SaintDetail }
  navigation<Screen.Settings>(startDestination = Screen.SettingsHome) { … }
}
```

- Each tab owns its own back stack.
- Detail destinations are registered **inside** the tab graph(s) that navigate to them. The same route type (`Screen.SaintDetail`) appears in both Explore and Saints graphs — Nav Compose resolves `navigate(Screen.SaintDetail(id))` against the current back stack's graph, pushing onto the correct tab.
- Selected-tab detection uses `destination.hierarchy.any { it.route == tabQualifiedName }` instead of `currentRoute.startsWith(...)` so deep routes inside a tab still highlight the tab.
- `navigateTopLevel` keeps `popUpTo(graph.findStartDestination().id) + saveState + restoreState` — it now works reliably because each nested graph has an independent saved-state bundle.

New `Screen` sealed variants: `AboutHome`, `ExploreHome`, `SaintsHome`, `SettingsHome` (inner start destinations). Existing tab markers (`About`, `Explore`, `Saints`, `Settings`) are now graph routes, not leaf screens.

#### Rationale
- Canonical Compose pattern for bottom-nav + back-stack-per-tab.
- Matches iOS `TabView { NavigationStack { ... } }` semantics — each tab remembers where the user was.
- Fixes the first-tap no-op bug with no reliance on `restoreState`/`saveState` internals.
- No persistence impact: none of these routes are serialized to disk or deep-linked.

#### Impact
- **Aragorn (me):** MainScaffold + Screen.kt updated. `resolveTitle` now matches `*Home` suffixes for tab titles; detail-title logic unchanged.
- **Gandalf:** Architecture doc (`docs/android-architecture.md`) should note the nested-graph pattern if/when nav structure is documented.
- **Legolas:** Any nav-focused UI test fixtures must use the new route types (`Screen.SaintsHome`, etc.) for tab start destinations; tab markers are still valid for `navigateTopLevel` targets.
- **Samwise / iOS:** No impact.

#### Verification
`./gradlew :app:assembleDebug` passes. Manual test steps:
1. Cold start → lands on Saints tab ✓
2. Explore → category → SaintDetail → tap Saints tab → lands on Saints list ✓ (primary bug fix)
3. Back to Explore → still on SaintDetail (tab state preserved) ✓
4. TopAppBar back arrow on SaintDetail pops to that tab's list ✓

---

### Decision: Aragorn — TopAppBar Back Navigation & Detail Title Resolution

**Date:** 2026-04-22
**Author:** Aragorn (Android)
**Status:** Proposed

#### Context

`MainScaffold.kt` owns a single shared `TopAppBar` above the `NavHost`. Before this change, pushing `SaintDetail` or `CategorySaints` left the user stranded — no back button, and `SaintDetail` showed an empty title.

#### Decision

1. **Shared TopAppBar, gated back button.** Keep one top-level `TopAppBar` in the root `Scaffold`. Render a navigation icon only when both:
   - `navController.previousBackStackEntry != null` (stack is actually poppable), and
   - `currentRoute` matches a detail class (contains `"SaintDetail"` or `"CategorySaints"`).

   This guarantees top-level tabs (About/Explore/Saints/Settings) never show a back affordance, matching iOS tab root behavior.

2. **RTL-aware icon.** Use `Icons.AutoMirrored.Filled.ArrowBack`. The non-mirrored variant is deprecated for directional glyphs and would look wrong in Arabic/Hebrew builds (not a current locale, but free correctness).

3. **Detail title via shared VM scoped to the nav entry.** `resolveTitle` is `@Composable` and calls `hiltViewModel(backStackEntry)` to obtain the *same* `SaintListViewModel` instance that `SaintDetailScreen` uses. Title is `state.saints.firstOrNull { it.id == saintId }?.name ?: ""`. Fallback is an empty string (not a spinner) — the back button renders regardless so the user can always escape.

4. **CategorySaints title unchanged.** Still pulled from the typed route arg (`Screen.CategorySaints.title`), already localized at push time.

#### Alternatives considered

- **Per-screen Scaffolds + per-screen TopAppBar.** Would give each screen full control but duplicates bottom-bar plumbing and fights the current single-Scaffold design.
- **Mutable state hoisted from SaintDetailScreen up to MainScaffold for the title.** More moving parts; the VM-per-entry approach is simpler and already how the screen works.

#### Implications

- Any future detail destination must add its route keyword to the `isDetailRoute` check (or refactor to a `Screen.isDetail` marker). Worth a follow-up if a third detail type is added.
- `SaintListViewModel` is now resolved once per `SaintDetail` entry (already true from `SaintDetailScreen`); no new instance is created by the title resolver.

---

### Decision: Android Instrumentation Tests — All 12 Tests Live ✅

**Author:** Legolas (QA)
**Date:** 2026-07-24
**Branch:** squad/android-port
**Status:** ✅ COMPLETE

#### Summary

All 12 instrumentation tests are now live (0 `@Ignore`'d) following Aragorn's HiltTestRunner landing:

| File | Live | @Ignore'd |
|------|------|-----------|
| `ui/WelcomeScreenNavigationTest.kt` | 4 | 0 |
| `ui/SaintListDisplayTest.kt` | 4 | 0 |
| `ui/LanguageSwitchTest.kt` | 4 | 0 |
| **Total** | **12** | **0** |

Compilation: `cd android && ./gradlew :app:compileDebugAndroidTestKotlin` → **BUILD SUCCESSFUL**.

#### Test Architecture

All three test classes use the same wiring pattern:

```kotlin
@HiltAndroidTest
class XTest {
    @get:Rule(order = 0) val hiltRule = HiltAndroidRule(this)
    @get:Rule(order = 1) val composeRule = createEmptyComposeRule()

    @Inject lateinit var prefs: PreferencesRepository

    @Before fun setUp() {
        hiltRule.inject()
        runBlocking {
            prefs.setHasSeenWelcome(false)   // or true, per file default
            prefs.setLanguage(AppLanguage.EN)
        }
    }

    @Test fun t() {
        runBlocking { prefs.setHasSeenWelcome(...) }  // per-test reseed
        ActivityScenario.launch(MainActivity::class.java).use {
            composeRule.waitUntil(5_000) { /* landmark */ }
            // assertions…
        }
    }
}
```

**Key choice:** `createEmptyComposeRule()` + manual `ActivityScenario.launch` (vs. `createAndroidComposeRule<MainActivity>()`). The auto-launching rule starts MainActivity before `@Before` runs — too early to seed `hasSeenWelcome=true` for the "skip welcome on relaunch" test. Full write-up in `.squad/skills/android-compose-instrumentation/SKILL.md` (skill bumped to **medium** confidence).

#### Contracts Exercised

- **Welcome gating** (WelcomeScreenNavigationTest):
  - `hasSeenWelcome=false` → Welcome pager on MainActivity launch.
  - Advance pager × 3 → "Let's Go!" → `onComplete` → `markWelcomeSeen()` → DataStore persists `true` → AppRoot swaps to MainScaffold → SaintsHome.
  - `hasSeenWelcome=true` pre-launch → Welcome never shown.

- **Saint list rendering & search** (SaintListDisplayTest):
  - Renders rows from `saints-en.json` (asserted via known saint names).
  - Search filters in-place; non-matching saints disappear.
  - Diacritic-insensitive match: "therese" finds St. Thérèse of Lisieux.
  - Empty state: "No Saints Found" on no-match query.

- **Live language switch** (LanguageSwitchTest):
  - EN baseline renders "Settings"/"Language".
  - Tap "Español" → without Activity restart, strings recompose to "Ajustes"/"Idioma" across top-bar + bottom-nav.
  - Navigate to Saints tab → saint rows reload from `saints-es.json` ("Santa Teresa de Lisieux").
  - `scenario.recreate()` preserves the selection (DataStore → LocalizationService re-emits ES).

#### Unblocked by

- Aragorn's HiltTestRunner wiring (Hilt 2.52, `androidx.test:runner` 1.6.2, `kspAndroidTest` for `hilt-android-compiler`).
- No new production dependencies — `androidx.test:runner` transitively provides `androidx.test.core.app.ActivityScenario`.

#### Status

CI hook in `.github/workflows/android-ci.yml` remains `if: false` scaffold; flipping it on requires a real Android emulator runner (separate decision for Gandalf).

---

### Decision: Cross-Platform Parity Check is the Guardrail

**Author:** Legolas (Tester)
**Date:** 2026-07-22
**Status:** Proposed (awaiting Gandalf)

#### Context

Aragorn is implementing the Android port in parallel with iOS. The
"SharedContent/ is the Canonical Cross-Platform Data Source" decision
(2026-04-21) established canonical English identifiers as a cross-platform
contract. Until now there was no automated check enforcing it — just
convention and reviewer vigilance.

#### Decision

Adopt `tests/shared-content-parity.py` as the **single cross-platform
parity guardrail**. Neither iOS (XCTest) nor Android (JUnit) should
duplicate this check — they call into canonical data that the Python
script has already validated.

Rationale:
- Platform-neutral: runs in CI, pre-commit, and on any dev box with
  Python 3, no Xcode or Gradle required.
- Fails fast on PRs that touch `SharedContent/` before a full iOS or
  Android build would catch the drift at runtime.
- Keeps iOS and Android test suites focused on *their* concerns
  (rendering, navigation, view models) rather than re-implementing
  data invariants twice.

#### Invariants enforced

1. `saints-en.json` and `saints-es.json` expose the same set of saint ids.
2. `sourceURLs` value set matches per saint across EN/ES.
3. Canonical English fields match byte-for-byte per saint across EN/ES:
   `patronOf`, `affinities`, `tags`, `region`, `lifeState`,
   `ageCategory`, `gender`.
4. Every saint has an image at `SharedContent/images/<id>.jpg`.
5. `categories-en.json` vs `categories-es.json`: same group ids and same
   value ids per group.

#### Current status (2026-07-22 run)

`python3 tests/shared-content-parity.py` → **PASS** against
`SharedContent/` at HEAD of `squad/android-port`. All 70 saints in lockstep.
No action needed from Samwise.

#### Requested of others

- **Gandalf:** Ratify or amend the invariant list above. In particular,
  confirm whether `country` should be canonical (currently excluded —
  we allow localized country names like "Italia" vs "Italy").
- **Samwise:** Run the script before any PR touching `SharedContent/`.
  If it fails, fix the data — do not weaken the script.
- **Aragorn:** No action. Continue implementing Phases 2–7.
- **Gandalf (CI):** When ready, flip `if: false` in
  `.github/workflows/android-ci.yml` to gate PRs on this check.

---

### Decision: Android Adaptive Icons — 66dp Safe Zone + Separate Splash Icon

**Author:** Aragorn (Android Dev)  
**Date:** 2026-04-22  
**Status:** Implemented

#### Problem
User reported that the Android app icon appeared cropped in two places:
1. **Home screen launcher** — icon looked cut off
2. **System splash screen** — logo appeared much smaller than intended

#### Root Cause
Investigation revealed:
- **Launcher icon was CORRECT**: The adaptive foreground properly implements the 66dp-of-108dp safe zone rule (60% scale, 21px margins at mdpi)
- **Splash screen was BROKEN**: `themes.xml` was reusing `@mipmap/ic_launcher_foreground` for `windowSplashScreenAnimatedIcon`, which already has adaptive padding. This caused double-padding — the logo was only 60% of 60% = 36% of expected size.

#### Decision
1. **Keep adaptive launcher icon implementation unchanged** — it correctly implements Material Design's adaptive icon safe zone requirements
2. **Create dedicated splash icon** (`ic_splash.png`) at 288dp, full-bleed with NO padding
3. **Update `themes.xml`** to use `@mipmap/ic_splash` for splash screen instead of the adaptive foreground
4. **Update icon generator script** to generate splash icons alongside launcher icons

#### Implementation
- Added `ic_splash.png` generation to `_generate_android_icon.py` (5 density buckets: mdpi through xxxhdpi)
- Updated `Theme.ConfirmationSaints.Splash` in `themes.xml` to reference `@mipmap/ic_splash`
- Verified build: `./gradlew :app:assembleDebug` → BUILD SUCCESSFUL

#### Technical Details: The 66dp Safe Zone Rule
Android adaptive icons use a 108×108dp canvas, but launcher masks (circle/squircle/teardrop/rounded square) can crop it. Only the inner 66×66dp circle is guaranteed visible:
- Canvas: 108dp
- Safe zone: 66dp (center circle) = ~61% of canvas
- **Best practice: 60% scale** provides 21-22dp margins on all sides

Our implementation:
```python
FOREGROUND_INNER_RATIO = 0.60  # 60% content in 108dp canvas
```

At mdpi (1x density):
- Canvas: 108px
- Content: ~65px (60% scale)
- Margins: 21-22px on all sides ✅

#### Files Changed
- `android/app/src/main/res/values/themes.xml` — updated splash icon reference
- `android/app/src/main/res/mipmap-*/ic_splash.png` — new splash icons (5 densities)
- `_generate_android_icon.py` — added splash icon generation

#### Impact on Other Agents
- **Legolas (QA):** Splash screen now displays logo correctly — verify on physical device/emulator if testing UI
- **Gandalf (Lead):** Pattern is reusable — documented in `.squad/skills/android-adaptive-icons/`
- **iOS (Frodo):** No impact — iOS uses single 1024×1024 PNG, no adaptive/splash separation needed

#### Reusability
Created `.squad/skills/android-adaptive-icons/SKILL.md` documenting:
- The 66dp-of-108dp safe zone rule
- When to use separate splash icons vs adaptive foreground
- Python generator script pattern
- Verification checklist

This is a cross-platform concern — any future Android icon regeneration should follow this pattern.

#### Related Decisions
- **"Programmatic App Icon with Chi-Rho Design" (2025-07-15)** — defined the iOS icon design; this decision adapts it for Android adaptive icons

#### ⚠️ CORRECTION (2026-04-22)
**The 60% scale claim in this decision was geometrically incorrect.** The linear calculation (66dp ÷ 108dp ≈ 61%) ignored the diagonal trap: square content rotated in a circular mask must fit within 66dp along its *diagonal*, not just width/height. This resulted in visible cropping on circular launchers. **See the follow-up decision below: "Android Launcher Icon Scale Correction: 60% → 43%"** for the fix and full geometric analysis. The splash screen fix documented above remains valid and unchanged.

---

### Decision: Android Launcher Icon Scale Correction: 60% → 43%

**Author:** Aragorn (Android Dev)  
**Date:** 2026-04-22  
**Status:** Implemented  

#### Context

Jorge reported that the Android launcher icon on the home screen was cropped, despite earlier work claiming the 60% scale was correct for the 66dp safe zone. The splash screen icon (separate full-bleed PNG) was rendering correctly.

#### Problem

The original adaptive icon implementation used **60% scale** based on a linear calculation: 66dp safe zone ÷ 108dp canvas ≈ 61%. However, this ignores a critical geometric constraint:

**Android's circular launcher masks require the content's DIAGONAL to fit within the 66dp safe zone circle, not just its width/height.**

#### Root Cause: The Diagonal Trap

For a **square** icon within a **circular** mask:
- Safe zone: 66dp diameter circle
- Maximum square side: 66dp ÷ √2 ≈ **46.7dp**
- As percentage of 108dp canvas: **43.2%**

**At 60% scale (BROKEN):**
- Content: 64.8dp × 64.8dp
- Diagonal: 91.6dp (via Pythagorean theorem)
- **Overshoot: 25.6dp beyond the 66dp safe zone** (39% too large)
- Result: Visible cropping on circular launchers ❌

**At 43% scale (FIXED):**
- Content: 46.4dp × 46.4dp
- Diagonal: 65.7dp
- **Clearance: 0.3dp inside the 66dp safe zone**
- Result: No cropping on any launcher shape ✅

#### Solution

1. Updated `_generate_android_icon.py`: changed `FOREGROUND_INNER_RATIO = 0.60` → `0.43`
2. Added geometric explanation in code comments
3. Regenerated all 5 density variants of `ic_launcher_foreground.png`
4. Verified all densities fit within safe zone (0.0-0.9dp clearance)
5. Build successful: `./gradlew :app:assembleDebug`

#### Measurements (Proof)

**Before (60% scale):**
```
mdpi:    Canvas 108×108px, Content 65×65px, Margins 21px (19.4%), Diagonal 91.9dp ❌
xxxhdpi: Canvas 432×432px, Content 259×259px, Margins 86px (19.9%), Diagonal 366.3dp ❌
```

**After (43% scale):**
```
mdpi:    Canvas 108×108px, Content 46×46px, Margins 31px (28.7%), Diagonal 65.1dp ✅
xxxhdpi: Canvas 432×432px, Content 186×186px, Margins 123px (28.5%), Diagonal 262.9dp ✅
```

#### Impact

- **Android dev (Aragorn):** Generator script corrected; skill document updated with diagonal verification technique
- **iOS (Frodo):** No impact (separate 1024×1024 PNG, no safe zone constraints)
- **Samwise/Legolas/Gandalf:** No action required
- **Future Android icons:** Use 43% scale, always verify diagonal fits in circle

#### Design Tradeoff

The 43% scale means the launcher icon appears **smaller** on the home screen than the previous 60% version. However, this is the **correct** implementation per Material Design adaptive icon spec. The alternative (keeping 60%) results in visible cropping, which is unacceptable.

Jorge confirmed via emulator/device testing that the splash screen now looks correct (full-bleed) and requested this launcher icon fix after uninstall/reinstall ruled out launcher cache issues.

#### References

- Material Design: [Adaptive Icons](https://developer.android.com/develop/ui/views/launch/icon_design_adaptive)
- Safe zone constraint: 66dp circle within 108dp square canvas
- Pythagorean theorem: diagonal = side × √2

#### Related Decisions

- **"Android Adaptive Icons — 66dp Safe Zone + Separate Splash Icon" (2026-04-22)** — splash screen fix; launcher scale claim in that decision was incorrect (see ⚠️ correction above)

---

### Decision: v1.0.1 App Store Submission Copy Conventions

**Date:** 2026-04-23  
**Decider:** Gandalf  
**Status:** Active

#### Context

Preparing `docs/appstore/submission-info.md` for v1.0.1 release. App grew from 50 saints at v1.0.0 launch to **81 saints** in v1.0.1. Need to decide how to communicate the update in release notes, promotional text, and description.

#### Decision

1. **Release notes use user-facing language only.** Lead with value: "31 new saints" and "81 Catholic saints" total. No internal details (schema refactors, integrity tests, export compliance declarations, git branches).

2. **Promotional text uses concrete counts when viable.** Changed "80+ saints" → "81 saints" (153 chars, under 170 limit). Rationale: specific number conveys precision and abundance without misleading. Easy to update if roster grows further.

3. **Description uses concrete counts for main value prop.** Changed "over 80 Catholic saints" → "81 Catholic saints" (1893 chars total, under 4000 limit). Same rationale as promotional text.

4. **Character count verification is mandatory.** Always report promotional text (170 max), description (4000 max), and release notes (4000 max) lengths to confirm App Store Connect submission will succeed.

#### Consequences

### Positive
- Release notes are scannable and user-focused — candidates care about more choice, not implementation churn.
- Concrete "81 saints" number communicates completeness and scale without overpromising.
- Character counts verified before submission = no surprises in App Store Connect.

### Negative
- "81 saints" becomes stale when roster grows. Acceptable tradeoff — promotional text is easy to update in next release.

#### Alternatives Considered

- **"80+ saints"** — less precise, but future-proof. Rejected: at 81 saints, precision is a marketing strength.
- **"More saints added"** without count — vague. Rejected: users want to know how many.
- **List all 31 new saints by name in release notes** — too long, reduces scannability. Rejected: mentioned a few highlights (Clare, Faustina, Romero) and "many more" is sufficient.

#### References

- `docs/appstore/submission-info.md` (updated)
- `ios/project.yml` (MARKETING_VERSION=1.0.1, CURRENT_PROJECT_VERSION=2)
- Git log v1.0.0..HEAD (31 saints added in commits leading to 1.0.1)

---

### Decision: Introduce "Modern Day Saints" Category/Filter

**Date:** 2026-04-25  
**Author:** Gandalf (Lead/Architect)  
**Status:** Approved & Implemented

#### Problem Statement

Users want a quick, user-facing filter to discover saints from the modern era—particularly those from the 20th and 21st centuries who faced challenges and contexts relevant to today's teens. The existing "modern" (1800–1949) and "contemporary" (1950+) era categories are machine-driven and not presented as quick-filter chips; they are also too broad and historical in naming.

#### Decision

**Define "Modern Day Saints" as saints born in or after 1900.**

- **Deterministic criterion:** Derived from existing `birthDate` field
- **Cross-platform:** Identical logic on iOS and Android
- **13 qualifying saints:** Carlo Acutis, Chiara Luce Badano, José Sánchez del Río, Gianna Beretta Molla, Teresa of Calcutta, John Paul II, Pier Giorgio Frassati, Teresa of the Andes, Óscar Romero, Faustina Kowalska, Josemaría Escrivá, Jacinta Marto, Francisco Marto
- **No schema changes:** Uses existing `birthDate` field; added new `modern-day` value to `era` category group

#### Implementation

- **Data:** Added `modern-day` value to `era` category in EN/ES category JSON files (Samwise)
- **iOS:** Added `modern-day` case to `matchesEra()` in `SaintListViewModel.swift`; wired to quick-filter chips (Frodo)
- **Android:** Added `modern-day` case to `matchesEra()` in `CategoryMatcher.kt`; wired to quick-filter chips (Aragorn)
- **Validation:** EN/ES parity confirmed; all 13 saints match; iOS/Android filtering verified (Legolas)

#### Consequences

- Modern Day Saints filter integrates seamlessly into existing category flow
- Cross-platform parity maintained; no platform-specific branching
- EN/ES labels: "Modern Day Saints (Born 1900+)" (EN), "Santos de Hoy (Nacidos en 1900+)" (ES)
- No migration or compatibility concerns

#### References

- `.squad/decisions/inbox/gandalf-modern-day-saints.md` (contract)
- `.squad/orchestration-log/2026-04-25T16-39-46Z-*.md` (implementation logs)

---

### Decision: Modern Day Saints Review Scope Separation

**Date:** 2026-04-25  
**Author:** Gandalf (Lead/Architect)  
**Status:** Clarification — Scope documented, no blocking issues

#### Context

Two independent, user-approved batches remain in the working tree:

1. **Batch 1 (Prior, Approved):** Android dark-mode WelcomeScreen fixes + platform-specific submission notes
   - Files: `android/app/src/.../WelcomeScreen.kt`, `docs/appstore/submission-info.md`, `docs/android/submission-info.md`
   - Status: Approved; no functional defects reported

2. **Batch 2 (Current, Modern Day Saints):** Feature expansion across shared data + iOS/Android platforms
   - Files: 17+ modified (categories, filtering logic, localization, tests)
   - Status: User-approved QA; Legolas passed feature behavior

#### Analysis

- **Zero semantic overlap:** Changes touch entirely separate code concerns
- **No file contamination:** No file modified by both batches
- **Both approved:** Each batch passed independent validation
- **No blocker issues:** Modern Day Saints feature behavior is sound

#### Decision

**Treat both batches as distinct, valid pending changes.** No file edits needed. Both batches can coexist in working tree safely and be committed independently.

#### Rationale for Future Sessions

When multiple user-approved tasks remain uncommitted:
1. Document batch composition in decisions inbox with dates and approval status
2. Track each batch's validation independently
3. At review time, validate batch independence (not co-contamination)
4. Coordinator decides commit grouping: single batch, multiple batches, or separate PRs

#### References

- `.squad/decisions/inbox/gandalf-modern-review-scope.md` (detailed analysis)
- Orchestration logs: Gandalf isolation, Legolas revalidation

---

### Update: Modern Day Saints Data Category

**Date:** 2026-04-25  
**Author:** Samwise (Data/Backend)  
**Status:** Implemented

#### Changes

- **Category Updates:**
  - Added `modern-day` value to `era` category in `SharedContent/categories/categories-en.json`
  - Added matching `modern-day` value in `SharedContent/categories/categories-es.json`
  - Labels: "Modern Day Saints (Born 1900+)" (EN), "Santos de Hoy (Nacidos en 1900+)" (ES)

- **Saint Set Validation (13 saints, all born 1900+):**
  - EN/ES parity confirmed: matching IDs and identical `birthDate` values
  - Current roster: 103 EN saints, 103 ES saints
  - No malformed birthDate values; null values limited to expected cases (archangels, Marian apparitions)

#### Details

| ID | EN Name | ES Name | Birth Date |
|----|---------|---------|------------|
| carlo-acutis | Bl. Carlo Acutis | Beato Carlo Acutis | 1991-05-03 |
| chiara-luce-badano | Bl. Chiara Luce Badano | Beata Chiara Luce Badano | 1971-10-29 |
| jose-sanchez-del-rio | St. José Sánchez del Río | San José Sánchez del Río | 1913-03-28 |
| gianna-beretta-molla | St. Gianna Beretta Molla | Santa Gianna Beretta Molla | 1922-10-04 |
| mother-teresa | St. Teresa of Calcutta | Santa Teresa de Calcuta | 1910-08-26 |
| john-paul-ii | St. John Paul II | San Juan Pablo II | 1920-05-18 |
| pier-giorgio-frassati | Bl. Pier Giorgio Frassati | Beato Pier Giorgio Frassati | 1901-04-06 |
| teresa-of-the-andes | St. Teresa of the Andes | Santa Teresa de los Andes | 1900-07-13 |
| oscar-romero | St. Óscar Romero | San Óscar Romero | 1917-08-15 |
| faustina-kowalska | St. Faustina Kowalska | Santa Faustina Kowalska | 1905-08-25 |
| josemaria-escriva | St. Josemaría Escrivá | San Josemaría Escrivá | 1902-01-09 |
| jacinta-marto | St. Jacinta Marto | Santa Jacinta Marto | 1910-03-11 |
| francisco-marto | St. Francisco Marto | San Francisco Marto | 1908-06-11 |

#### Platform Readiness

iOS (Frodo) and Android (Aragorn) can now implement filtering logic knowing the exact saint set and EN/ES parity is guaranteed.

#### References

- `.squad/decisions/inbox/samwise-modern-day-saints-data.md` (data validation details)
- `.squad/orchestration-log/2026-04-25T16-39-46Z-samwise.md` (implementation log)

---

### Decision: Deterministic Android Localization StateFlow Tests

**Author:** Aragorn (Android Dev)
**Date:** 2026-04-25
**Status:** Approved

#### Context

`LocalizationService.language` is a `StateFlow` seeded with `AppLanguage.fromSystemLocale()` and then backed by DataStore. A recreated service may not emit a second item if the persisted language equals the system-locale fallback because `StateFlow` suppresses duplicate values. This caused `LocalizationServiceTest::should_persist_language_choice_to_datastore` to timeout with `TurbineTimeoutCancellationException` under non-default JVM locales.

#### Decision

Android tests verifying language switching or persistence should:
1. Choose a target language opposite the current/system fallback
2. Assert the final `StateFlow.value` after advancing the `StandardTestDispatcher`
3. Pass the test coroutine scope into `PreferenceDataStoreFactory.create(scope = testScope)` to ensure deterministic persistence scheduling

#### Rationale

This keeps behavior strong: the test proves a user-selected language overrides the device locale and survives service recreation. It avoids brittle Turbine expectations that depend on a non-guaranteed second emission.

#### Validation

- Reproduced the locale-dependent failure under `-Duser.language=es --rerun-tasks`
- Fixed `LocalizationServiceTest` without production-code changes
- Focused localization tests and full Android JVM unit tests pass under both Spanish and default locales
- Committed `e7ca6c3 Fix localization persistence unit test` to `develop`
- PR #6 CI reran with all checks green (Android build + unit tests, SharedContent parity, GitGuardian Security)

#### References

- `.squad/orchestration-log/2026-04-25T17-26-51Z-aragorn.md` (fix execution log)
- `.squad/orchestration-log/2026-04-25T17-26-51Z-legolas.md` (QA approval log)

---

## Archived Decisions (older than 2026-03-22)

See `decisions-archive.md` for foundational iOS architecture decisions from 2026-04-12 to 2026-04-13.
