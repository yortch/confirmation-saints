# Squad Decisions

## Active Decisions

### Cross-Platform Repository Restructure (2026-07-15)
**Author:** Gandalf (Lead)  
**Status:** Implemented

Reorganize repository to separate iOS (ios/), Android (android/), and shared content (SharedContent/ at root). All iOS-specific code and build files moved under ios/. SaintDataService bundle paths unchanged (folder reference works across structure). SharedContent/ supersedes old SharedContent/Data/.

**Impact:** All team members use `ios/` prefix for iOS file paths; build commands now require `cd ios` first.

---

### App Rename + Welcome Onboarding (2026-04-12)
**Author:** Frodo (iOS Dev)  
**Status:** Implemented

**App Rename:**
- Display name changed "Catholic Saints" → "Confirmation Saints" (per user request)
- Internal folder `CatholicSaints/` and bundle ID unchanged (preserves app store listing)
- Updated: `project.yml`, `README.md`

**Welcome/Onboarding Screen:**
- 4-page TabView (Welcome → Discover → Learn → Get Started) with PageTabViewStyle
- First-launch gating via `@AppStorage("hasSeenWelcome")`
- Replayable from Settings → "Show Welcome Screen"
- Purple/gold liturgical theme (consistent with accent color)
- Fully bilingual EN/ES via String Catalog
- Reusable `OnboardingPageView` component

**Impact on Other Agents:**
- **Legolas:** New `WelcomeView.swift` and modified `SettingsView.swift` require test coverage
- **Samwise:** No data changes
- **Gandalf:** Follows existing MVVM + @AppStorage pattern

---

### Programmatic App Icon with Chi-Rho Design (2025-07-15)
**Author:** Samwise (Data/Backend)  
**Status:** Implemented

**Design:**
- Chi-Rho (☧) symbol — oldest Christogram, universally recognized in Catholic tradition
- Purple gradient background (liturgical color of Confirmation)
- Gold accents (sacred/regal)
- Subtle dove silhouette (Holy Spirit)
- No text (poor readability at small sizes)

**Technical:**
- Generated via `_generate_icon.py` (Python + Pillow)
- Single 1024×1024 PNG: Xcode auto-generates all required sizes
- Output: `ios/CatholicSaints/Resources/Assets.xcassets/AppIcon.appiconset/app-icon-1024.png`
- Contents.json updated with iOS platform reference

**Trade-offs:**
- Programmatic generation = geometric/flat style only
- Chi-Rho less immediately recognizable to teens than simple cross, but more distinctive/unique
- **Placeholder** — Jorge may commission professional icon later

**Impact:**
- iOS icon now visible in simulator/device
- Android icon generation pending (different format requirements)
- Script is regenerable/modifiable if design tweaks needed

---

### Saint Image Sources from Wikimedia Commons (2026-07-17)
**Author:** Samwise (Data/Backend)  
**Status:** Implemented

All 32 saint images sourced from Wikimedia Commons using public domain or Creative Commons licensed artwork. Downloaded at 400px width thumbnails (~2.8MB total) for bundle efficiency. Images stored cross-platform in `SharedContent/images/`. Reproducible script (`_download_saint_images.py`) enables future updates. Attribution standardized to "Public domain, via Wikimedia Commons" across all saints.

**Impact:**
- Bundle size: ~2.8MB added (JPG format, 400px width)
- iOS/Android: Cross-platform ready in SharedContent/images/
- UI: No changes needed (SaintImageView.swift already handles image loading)

**Notes:** Some saints (Carlo Acutis, Chiara Luce Badano) have limited public domain imagery; best available options used.

---

### Source URL Replacement Strategy (2025-07-15)
**Author:** Frodo (iOS Dev)  
**Status:** Implemented

Link audit identified 46 broken source URLs across saints-en.json and saints-es.json. Five primary sources had widespread link rot (Loyola Press, Hallow, Ascension Press, Focus, Lifeteen). Replaced with verified alternatives:

1. **Franciscan Media** — Primary replacement (stable saint-of-the-day archive)
2. **CNA (Catholic News Agency)** — Secondary source for comprehensive coverage
3. **EWTN** — Tertiary source to avoid duplicate keys
4. **Hallow (updated paths)** — Migrated from `/blog/` to `/saints/`

**Impact:**
- All 32 saints retain ≥2 working source URLs
- No working URLs changed
- Both EN/ES files updated identically (matched by URL, not saint name)

---

### Diacritic-Insensitive Search Convention (2025-07-17)
**Author:** Frodo (iOS Dev)  
**Status:** Implemented

All string matching in search/filter logic uses diacritic-insensitive comparison via shared `String+Diacritics.swift` extension (`containsIgnoringDiacritics` / `equalsIgnoringDiacritics`), not `.lowercased().contains()`.

**Rationale:** Saint names include accented characters (Thérèse, José, María) common in French, Spanish, and other languages. Users typing on English keyboards won't include accents, so search must treat accented and unaccented characters as equivalent.

**Impact:**
- **Legolas (QA):** Search tests verify accent-insensitive matching
- **Samwise (Data):** Saint data retains proper accented names
- **Android:** Use Java/Kotlin `Normalizer` or `Collator` with `SECONDARY` strength

---

### SharedContent/ is the Canonical Cross-Platform Data Source (2026-04-21)
**Author:** Gandalf (Lead)  
**Status:** Decided

iOS v1.0.0 is live on the App Store. Android port is starting. To maintain sync across platforms, `SharedContent/` at the repo root is the **single source of truth** for all content-layer data and imagery:

- `SharedContent/saints/saints-{en,es}.json`
- `SharedContent/categories/categories-{en,es}.json`
- `SharedContent/content/confirmation-info-{en,es}.json`
- `SharedContent/images/*.jpg`

Neither platform forks or duplicates this directory. iOS consumes it via a folder-reference build phase and symlink; Android will consume it via Gradle asset source-set include.

**Key Contract:**
- **Canonical ids are English.** Fields driving matching (`patronOf`, `tags`, `affinities`, `region`, `lifeState`, `ageCategory`, `gender`) must contain identical English identifier values in every language file.
- **Display localization lives in optional `display*` arrays** (`displayPatronOf`, `displayTags`, `displayAffinities`) and freely-translated fields (`name`, `country`, `biography`, `quote`, `whyConfirmationSaint`).
- **Schema changes require PRs touching every language file** so they stay in lockstep.
- **Image filenames equal the saint `id`.** One image serves all languages.

**Impact:**
- **Samwise:** Continue treating `SharedContent/` as delivery target; schema changes require identical updates to both language files.
- **Frodo/Android dev:** Decode directly from these files; do not fork into platform-specific copies.
- **Legolas:** Add cross-platform parity test — for every saint id, both language files must have identical canonical-id values.
- **Gandalf:** Reject PRs duplicating `SharedContent/` into `ios/` or `android/`.

---

### HiltTestRunner Wiring for Android Instrumentation Tests (2026-04-21)
**Author:** Aragorn (Android Dev)  
**Status:** Implemented

Added HiltTestRunner infrastructure to enable @HiltAndroidTest support for 10 blocked UI tests in `android/app/src/androidTest/`.

**What was added:**
1. New `android/app/src/androidTest/java/com/yortch/confirmationsaints/HiltTestRunner.kt` — extends AndroidJUnitRunner, swaps in HiltTestApplication
2. `android/app/build.gradle.kts`:
   - `testInstrumentationRunner = "com.yortch.confirmationsaints.HiltTestRunner"`
   - `androidTestImplementation(libs.androidx.test.runner)` (1.6.2)
   - `androidTestImplementation(libs.hilt.android.testing)` (2.52)
   - `kspAndroidTest(libs.hilt.compiler)`
3. `android/gradle/libs.versions.toml`: added androidx-test-runner 1.6.2 catalog entries

**Verification:** `./gradlew :app:compileDebugAndroidTestKotlin` → BUILD SUCCESSFUL

**Impact on Legolas (QA):**
- 10 `@Ignore`'d tests in `android/app/src/androidTest/java/.../ui/` can now execute
- Remove `@Ignore`, annotate test class with `@HiltAndroidTest`, apply HiltAndroidRule + ComposeRule pattern
- MainActivity already @AndroidEntryPoint; ConfirmationSaintsApp already @HiltAndroidApp — no source changes needed

---

### Four Priority Saints Added — Batch 3 (2026-04-13)
**Author:** Samwise (Data/Backend)  
**Status:** Implemented

Added 4 saints to both `saints-en.json` and `saints-es.json`, bringing total from 50 → 54:

1. **St. Pius X** — Pope, patron of first communicants (Europe/Italy)
2. **St. Patrick** — Apostle of Ireland, patron of Ireland/engineers (Europe/Ireland)
3. **St. Catherine of Siena** — Doctor of the Church, mystic, patron of Italy/Europe (Europe/Italy)
4. **St. Martin de Porres** — Dominican lay brother, patron of mixed-race people/social justice (Americas/Peru)

**Data Decisions:**
- Patrick & Pius X: Pre-congregation saints → `canonizationDate: null` (consistent with apostles pattern)
- Catherine of Siena: `ageCategory: "young"` (died at 33)
- Martin de Porres: Region set to "Americas" (matching Peru/Latin America saints)
- Sources: Wikimedia Commons public domain images; Franciscan Media + Catholic Encyclopedia

**Impact:**
- Frodo (iOS): 4 new saints visible in search/browse — no UI changes needed
- Legolas (QA): Category coverage unchanged; region filters now include Martin de Porres in Americas
- Gandalf: No architectural changes

---

### Android Launcher Icon Polish — Red Background + Content-Aware Scaling (2026-04-22)
**Author:** Aragorn (Android Dev)  
**Status:** Implemented ✅

Jorge requested two fixes for the adaptive launcher icon:
1. Replace purple background (#4A148C) with red to match app branding
2. Increase visible content size beyond worst-case 43% scale

**Analysis:** Icon content is effectively circular (transparent corners), not square worst-case. At 61% scale, content diagonal ≈ 59dp, safely inside 66dp safe zone. Increases visible fill to ~75-85%.

**Implementation:**
- Background: `#B9161C` (icon gradient red, unifies branding)
- Foreground scale: 0.43 → 0.61 in `_generate_android_icon.py`
- All 5 densities regenerated, build verified ✅

**Files modified:**
- `android/app/src/main/res/values/colors.xml`
- `_generate_android_icon.py`
- `android/app/src/main/res/mipmap-*/ic_launcher_foreground.png` (×5)

**Approved by:** Jorge Balderas (visual verification)

---

### User Directives (Captured)
- **2026-04-12T17:13:10Z:** Jorge Balderas — App name changed to "Confirmation Saints". Update all references.
- **2026-04-12T16:29Z:** Jorge Balderas — Project scaffolded with cross-platform separation. Expand saint roster to 50-100+. Add "most popular saints" categories by year + all-time.
- **2026-04-13T01:12:10Z:** Jorge Balderas — Skip priority 3 (diversity gaps). Target ~75 saints total (not 90-100). Focus priorities 1, 2, 4.
- **2026-04-13T01:28:48Z:** Jorge Balderas — Skip priority 4. Focus ONLY priorities 1 & 2 (Pius X, Patrick, Catherine of Siena, Martin de Porres, key saints).

### 9 Saints Added — Batch 4 (10-Saint Sprint Outcome) (2026-04-23)
**Author:** Samwise (Data/Backend)  
**Status:** Implemented (count 79, open question on 80-target parity)

Added 9 new saints to both `saints-en.json` and `saints-es.json`, bringing roster from 70 → **79** saints per language. St. Luke the Evangelist (item #2 on Jorge's list) was already in the roster from a prior batch and was not re-added.

**Saints Added:**
1. St. John Neumann (19th-c. US Bishop, first US male saint)
2. St. Moses the Black (4th-c. Desert Father, martyr of non-violence)
3. St. Vladimir of Kiev (Bringer of Christianity to Kievan Rus')
4. St. Ignatius of Loyola (Founder of Jesuits, Spiritual Exercises)
5. Bl. Imelda Lambertini (Dominican child mystic, patron of first communicants)
6. St. Isidore the Farmer (Patron of farmers, married lay saint)
7. St. Teresa of Ávila (First female Doctor of the Church)
8. St. Anthony of Padua (Doctor of the Church, Franciscan)
9. St. Josemaría Escrivá (Founder of Opus Dei, 20th-century)

**Data Decisions:**
- "Doctor of the Church" tag now covers 4 saints: Augustine, Catherine of Siena, Teresa of Ávila, Anthony of Padua
- Blessed tier expanded to 5: Carlo Acutis, Chiara Luce Badano, Pier Giorgio Frassati, Michael McGivney, Imelda Lambertini
- Image licensing corrected for 3 saints: CC BY-SA 4.0 (Moses, Vladimir), CC0 (Josemaría)

**Verification:**
- iOS: ✅ BUILD SUCCEEDED
- Android: ✅ BUILD SUCCESSFUL (test count updated 70→79)
- Cross-platform parity: ✅ EN/ES matching validated

**Open Question:**
Target was 80 saints. Current count is 79. Jorge must decide: (1) accept 79, (2) add one more saint, or (3) update copy to "75+". See decisions/inbox files for full context.

**Impact:**
- Frodo (iOS): No UI changes; new saints appear automatically
- Aragorn (Android): `SaintRepositoryTest` updated; recommend switching to "minimum count" assertion for future-proofing
- Legolas (QA): Category coverage unchanged; verify "Doctor of the Church" tag if testing

---

### Documentation Updated to 80+ Saint Count (2026-04-23)
**Author:** Gandalf (Lead)  
**Status:** Implemented

Updated user-facing documentation to reflect "80+ saints" across 8 locations per Samwise's 10-saint expansion on `squad/add-saints-80-plus`:

**Changes:**
- README.md: Added "What's New" section, updated 2 count references (70→80+)
- docs/index.html: 6 instances updated (meta, hero badge, mock-ups, stats)
- docs/appstore/submission-info.md: 2 instances (promotional + description)
- docs/appstore/screen-recording-script.md: 1 instance (video caption)
- docs/appstore/review-response.md: 2 instances (value prop, user flow)

**Pattern for Future:**
Marketing copy is distributed across 5 files; all must be updated in lockstep when saint count changes.

**Deliberately Immutable:**
Historical records in `.squad/decisions.md`, agent history.md files, and logs remain unchanged (snapshots in time).

---

## Governance

- All meaningful changes require team consensus
- Document architectural decisions here
- Keep history focused on work, decisions focused on direction

### Android JVM Unit Tests — Implementation Complete (2026-04-22)
**Author:** Aragorn (Android Dev)  
**Status:** ✅ Complete

Implemented 22 unit test TODOs across 4 test files for core Android functionality: birth date parsing, saint data loading, localization service, and category matching. All tests pass.

## Tests Implemented

### 1. BirthDateParsingTest (6 tests)
- Extracted birth year from ISO date strings
- Added `DateFormatting.parseBirthYear(birthDate: String?): Int?` function
- Handles zero-padded years ("0256" → 256), null, and malformed inputs
- Guards against octal interpretation ("0088" → 88)

### 2. SaintRepositoryTest (5 tests)
- Verifies asset-backed JSON loading via Robolectric
- Confirms 70 saints in both EN/ES language files
- Validates identical saint ID sets across languages (canonical English IDs)
- Checks null canonizationDate deserialization (pre-congregation saints)
- Verifies image.filename == "${saint.id}.jpg" contract

### 3. LocalizationServiceTest (6 tests)
- Tests StateFlow-based language state management
- Verifies device locale fallback when no DataStore preference exists
- Checks live language switch without Activity restart
- Validates DataStore persistence across service recreation
- Tests AppStrings EN/ES lookup and missing-key fallback to English

### 4. CategoryMatchingTest (5 tests)
- Verifies cross-language matching on canonical English values
- Confirms EN/ES return identical saint ID sets for same category
- Guards against matching on localized display* arrays (Spanish terms)
- Tests empty-list return for unknown category values
- Validates "young" age category includes Catherine of Siena

## Technical Decisions

### Robolectric Asset Access
**Problem:** Tests loading saints from assets returned empty lists.  
**Solution:** Added `testOptions.unitTests.isIncludeAndroidResources = true` to build.gradle.kts. This enables Robolectric to access merged app assets at test runtime.  
**Why not robolectric.properties?** Let Android Gradle Plugin handle resource merging automatically — simpler and more maintainable.

### DataStore StateFlow Testing
**Challenge:** StateFlow starts with `initialValue` before DataStore read completes.  
**Pattern:** Tests must handle both scenarios:
1. Initial emission = system default → advance scheduler → DataStore value arrives
2. DataStore value arrives immediately (cached/fast read)

Used conditional assertion: if first emission is system default, wait for second emission.

### Test Dependencies
**Added:** `kotlinx-coroutines-test:1.8.1` for Turbine + StandardTestDispatcher support.  
**Existing:** Turbine, Robolectric, JUnit4, androidx.test.ext.junit already present.  
**Why coroutines-test?** Needed `runTest` + `StandardTestDispatcher` for deterministic StateFlow/DataStore testing.

## Key Contracts Validated

1. **Birth year extraction** handles zero-padded early-church saints (e.g., St. Genevieve born 0256).
2. **Cross-platform data parity**: EN/ES both expose identical saint ID sets (70 saints).
3. **Canonical English matching**: Category filters use English values regardless of UI language (guards against displayPatronOf/displayTags leaking into matching logic).
4. **Image filename convention**: Every saint's image.filename == `${saint.id}.jpg` (cross-platform SharedContent/ contract).
5. **Localization fallback**: Missing Spanish translations fall back to English key (no throw, no raw key display).

## Verification

```bash
cd android && ./gradlew :app:testDebugUnitTest
```

**Result:** BUILD SUCCESSFUL, 32 tests completed, 0 failures ✅

## Files Changed

**Production code:**
- `app/src/main/java/.../util/DateFormatting.kt` — added parseBirthYear function

**Test code:**
- `app/src/test/java/.../util/BirthDateParsingTest.kt` — implemented 6 TODOs
- `app/src/test/java/.../data/SaintRepositoryTest.kt` — implemented 5 TODOs
- `app/src/test/java/.../localization/LocalizationServiceTest.kt` — implemented 6 TODOs
- `app/src/test/java/.../data/CategoryMatchingTest.kt` — implemented 5 TODOs

**Build configuration:**
- `app/build.gradle.kts` — added kotlinx-coroutines-test dependency, enabled isIncludeAndroidResources
- `gradle/libs.versions.toml` — added kotlinx-coroutines-test:1.8.1 version + catalog entry

## Impact on Other Agents

- **Legolas (QA):** All JVM unit tests now implemented. Instrumentation tests (androidTest/) remain separate and were not modified.
- **Gandalf (Lead):** No architectural changes. Tests validate existing cross-platform contracts.
- **Frodo/Samwise:** iOS + data schemas unchanged. Tests confirm Android implements iOS-equivalent behavior.

## No Production Bugs Found

All tests passed on first green run after Robolectric asset config. No defects discovered in SaintRepository, LocalizationService, CategoryMatcher, or DateFormatting APIs.

---
