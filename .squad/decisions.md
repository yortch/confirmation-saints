# Squad Decisions

## Active Decisions

### Decision: Sources Schema Refactor (2026-04-23)

**Date:** 2026-04-23  
**Author:** Gandalf (Lead/Architect)  
**Status:** Ready for implementation  
**Unblocks:** Samwise (data), Frodo (iOS), Aragorn (Android), Legolas (test)

---

#### 1. New Schema Shape

Replace `sources: [String]` + `sourceURLs: {String: String}?` with a single ordered array:

```json
"sources": [
  { "name": "Franciscan Media", "url": "https://www.franciscanmedia.org/..." },
  { "name": "Catholic News Agency", "url": "https://www.catholicnewsagency.com/..." }
]
```

**Field names:** `name` and `url` (not `title`/`href` — matches existing usage and is idiomatic).  
**Order:** Display order. Render top-to-bottom as listed.  
**Nullability:** `sources` is required (may be empty `[]`). Each entry must have non-empty `name` and `url`.

---

#### 2. Migration Strategy

One-shot rewrite of `saints-en.json` and `saints-es.json`. Match by saint `id` (identical across both files).

##### Edge-case handling:

| Scenario | Resolution |
|----------|------------|
| `sources[]` entry **not in** `sourceURLs{}` | **Fail migration** — must be fixed manually first. Do NOT silently drop. |
| `sourceURLs{}` key **not in** `sources[]` | **Fail migration** — orphan URL indicates data error. Fix manually. |
| URL is empty string | **Fail migration** — every source must have non-empty URL. |

**Rationale:** We're shipping 81 saints, ~150 sources. Better to fail fast now than silently lose data. Samwise fixes any mismatches before running migration.

---

#### 3. iOS Model Change

In `ios/CatholicSaints/Models/Saint.swift`:

```swift
struct SourceEntry: Codable, Hashable, Sendable {
    let name: String
    let url: String
}

struct Saint: Codable, Identifiable, Hashable, Sendable {
    // ... existing fields ...
    let sources: [SourceEntry]  // ← replaces sources: [String] + sourceURLs: [String: String]?
}
```

Remove the old `sources: [String]` and `sourceURLs: [String: String]?` fields entirely.

---

#### 4. Android Model Change

In `android/.../data/model/Saint.kt`:

```kotlin
@Serializable
data class SourceEntry(
    val name: String,
    val url: String,
)

@Serializable
data class Saint(
    // ... existing fields ...
    val sources: List<SourceEntry> = emptyList(),  // ← replaces sources + sourceURLs
)
```

Remove the old `sources: List<String>` and `sourceURLs: Map<String, String>?` fields.

---

#### 5. View Render Logic

##### iOS (`SaintDetailView.swift` — `sourcesSection`):
```swift
ForEach(saint.sources, id: \.name) { source in
    Link(destination: URL(string: source.url)!) {
        HStack {
            Text(source.name).font(.subheadline)
            Spacer()
            Image(systemName: "arrow.up.right.square").font(.caption)
        }
        .foregroundStyle(.blue)
    }
}
```
No lookup needed — iterate and render directly.

##### Android (`SaintDetailScreen.kt` — `SourcesSection`):
```kotlin
saint.sources.forEach { source ->
    Row(Modifier.clickable { uriHandler.openUri(source.url) }.padding(12.dp)) {
        Text(source.name, Modifier.weight(1f), color = MaterialTheme.colorScheme.primary)
        Icon(Icons.Default.OpenInNew, ...)
    }
}
```
No `sourceURLs?.get()` lookup — direct property access.

---

#### 6. Integrity Test

**Placement:** `android/app/src/test/java/com/yortch/confirmationsaints/data/SourcesIntegrityTest.kt`  
(Android JVM tests run in CI without emulator; this is where `SaintParsingTest.kt` already lives.)

##### Assertions (per saint, both language files):

1. **sources is non-empty** — every saint must cite at least one source.
2. **Each entry has non-empty `name`** — no blank labels.
3. **Each entry has non-empty `url`** — no broken links allowed.
4. **URL is well-formed** — `startsWith("https://")` (all our sources use HTTPS).
5. **Parity check (cross-file):**
   - Same saint IDs exist in both `saints-en.json` and `saints-es.json`.
   - Same number of sources per saint ID.
   - Same URLs per saint ID (order may differ, but set must match — content is language-agnostic).

##### Why Android?
Android already has `SaintParsingTest.kt` + Gradle `testDebugUnitTest` that loads `SharedContent/` via copy task. Legolas adds the new test alongside. iOS lacks a JSON-test pattern; adding one is more work with less CI benefit.

---

#### 7. Work Decomposition

| Agent | Task | Touches | Depends On |
|-------|------|---------|------------|
| **Samwise** | Migrate `saints-en.json` + `saints-es.json` to new schema | `SharedContent/saints/*.json` | None — start immediately |
| **Frodo** | Update `Saint.swift`, `SaintsFile`, and `sourcesSection` | `ios/.../Models/Saint.swift`, `ios/.../SaintDetailView.swift` | Samwise (needs new schema to decode) |
| **Aragorn** | Update `Saint.kt`, `SaintsFile.kt`, and `SourcesSection` | `android/.../model/Saint.kt`, `android/.../SaintDetailScreen.kt` | Samwise (needs new schema) |
| **Legolas** | Add `SourcesIntegrityTest.kt` with assertions above | `android/.../data/SourcesIntegrityTest.kt` | Samwise (test runs against new data) |

##### Parallelism:
- **Samwise starts first** — data migration unblocks everyone else.
- **Frodo + Aragorn + Legolas can start stub work immediately** (model shapes are defined above), but must wait for Samwise's commit before final testing.
- **No file-touch conflicts** — each agent owns separate files.

##### Hard sequencing:
1. Samwise commits data changes.
2. Frodo/Aragorn/Legolas pull, complete implementations, run tests.
3. Single merge to `squad/add-saints-80-plus` once all four pass CI.

---

#### Summary

This schema change eliminates the mismatch bug class by design. One ordered array of `{name, url}` objects replaces the fragile parallel-array pattern. Migration fails loudly on any inconsistency. The integrity test prevents future regressions.

Total scope: 2 JSON files, 2 model files, 2 view files, 1 test file. Four agents, minimal sequencing friction.

---

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

### 81 Saints + Wikipedia-First Attribution Policy (2026-04-23)
**Author:** Samwise (Data/Backend)  
**Status:** Implemented ✅

Added St. George and St. Mariana de Jesús de Paredes to reach 81 saints total. Audited attribution for all recent additions (9 saints from commit 8f5727a + Frances Cabrini) — all complete. Established **Wikipedia as the primary trusted source** for new saint research in the adding-saints skill documentation.

**Saints Added:**
- **St. George** (feast April 23): Early Christian martyr (~280-303 AD), patron of England, soldiers, scouts. Dragon legend symbolizes triumph of good over evil.
- **St. Mariana de Jesús de Paredes** (feast May 26): "Lily of Quito" — first canonized saint of Ecuador (1950). Mystic, penitent, laywoman (Third Order Franciscan).

**Attribution Audit (All Complete):**
- 9 saints from batch 8f5727a: John Neumann, Moses the Black, Vladimir of Kiev, Ignatius of Loyola, Imelda Lambertini, Isidore the Farmer, Teresa of Ávila, Anthony of Padua, Josemaría Escrivá
- Frances Cabrini (earlier batch)
- **Result:** All ≥2 sources, no gaps. `sourceURLs` + `imageAttribution` complete on every entry.

**Skill Update: Trusted Sources (in order)**
1. **Wikipedia (EN + ES articles)** — biographical facts, feast date, patronage, canonization date. Use English Wikipedia URL in `sourceURLs` for both language files.
2. **Wikimedia Commons** — images. Check `extmetadata.LicenseShortName` via API. Prefer PD; CC BY-SA acceptable with attribution.
3. **Catholic.org / Franciscan Media / vaticannews.va / CNA** — tiebreakers, spiritual context.

**Key Enforcement:** Both `sourceURLs` (dictionary) AND `imageAttribution` (string) are **REQUIRED**; never leave blank.

**Why Wikipedia First?**
- Consistency: Wikipedia exists for virtually every canonized saint + most Blessed
- Reliability: Cites primary sources; peer-reviewed via edit history; EN/ES cross-validate
- Accessibility: Free, multilingual, comprehensive
- License transparency: Wikimedia Commons has robust metadata via API
- Already in use: Both new saints used Wikipedia as primary source

**Why Not Wikipedia Only?**
- Spiritual context missing: Wikipedia is factual but doesn't explain *why* compelling for confirmation candidates
- Image gaps: Not all saints have PD images on Commons (especially 20th-century); need fallback to CC BY-SA/CC0

**Verification:**
- ✅ `python3 tests/shared-content-parity.py` → PASSED
- ✅ iOS build: BUILD SUCCEEDED
- ✅ Android: BUILD SUCCESSFUL, test count 79→81
- ✅ Cross-platform parity: EN/ES matching validated

**Marketing Copy Resolution:**
"80+ saints" copy now truthful at 81 saints. Prior discrepancy (docs vs. roster count) resolved.

**Impact:**
- **Frodo (iOS):** 2 new saints visible in search/browse — no UI changes needed
- **Aragorn (Android):** Build passes; test expected 81
- **Legolas (QA):** Category coverage unchanged; schema compliant
- **Future batches:** Use Wikipedia-first workflow from updated skill doc

**Files Changed:**
- `SharedContent/saints/saints-en.json` (2 saints added)
- `SharedContent/saints/saints-es.json` (2 saints added)
- `SharedContent/images/george.jpg`, `mariana-de-jesus-de-paredes.jpg`
- `.squad/skills/adding-saints/SKILL.md` (Wikipedia-first policy)

---

### iOS Settings Content Sources UI (2026-04-23)
**Author:** Frodo (iOS Dev)  
**Status:** Implemented ✅

Added 8 tappable SwiftUI `Link` components to Settings → Content Sources section. Sources include Wikipedia (EN/ES), Wikimedia Commons, Catholic.org, Franciscan Media, CNA, EWTN, Hallow.

**Implementation:**
- Each link has external-link glyph (`Image(systemName: "arrow.up.right")`)
- 4 new localized strings (EN+ES) added to `Localizable.xcstrings`
- `LocalizationService.swift` wired for string lookup
- Tint color matches accent color for visual consistency

**Verification:**
- ✅ iOS build: BUILD SUCCEEDED
- ✅ Localizable.xcstrings: No missing EN/ES pairs
- ✅ No compiler warnings

**Impact:**
- Users can tap to external sources directly from Settings
- Mirrors Android implementation (9 sources)
- Aligns with Wikipedia-first attribution policy

**Files Changed:**
- `ios/CatholicSaints/Views/SettingsView.swift` (8 Link components)
- `ios/CatholicSaints/Localization/LocalizationService.swift` (4 strings)
- `ios/CatholicSaints/Resources/Localizable.xcstrings` (4 new entries)

---

### Android Settings Content Sources UI (2026-04-23)
**Author:** Aragorn (Android Dev)  
**Status:** Implemented ✅

Added 9 tappable source rows to Settings → Content Sources. Created `ContentSource` data class and `SourceRow` composable. Uses `LocalUriHandler.current.openUri()` to launch links. All localized via `AppStrings.kt` (EN+ES). OpenInNew icon on each row; `contentDescription` for a11y.

**Implementation:**
- `ContentSource` data class holds label, URI, icon
- `SourceRow` composable renders tappable row with OpenInNew icon
- 9 sources: Wikipedia (EN+ES), Wikimedia Commons, Catholic.org, Franciscan Media, CNA, EWTN, Hallow, +1
- All labels localized (EN+ES) in AppStrings
- Live language switching in Settings updates labels

**Test Update:**
- `SaintRepositoryTest`: Updated test count 79→81 (reflects 81-saint roster)
- Recommendation: Switch to "minimum count" assertion for future-proofing

**Verification:**
- ✅ Android build: BUILD SUCCESSFUL
- ✅ 32 unit tests pass; 0 failures
- ✅ No compiler warnings

**Impact:**
- Settings mirrors iOS (source browsing UI)
- Aligns with Wikipedia-first attribution policy
- A11y compliant (all icons described)
- Saint count now future-locked at 81

**Files Changed:**
- `android/app/src/main/java/.../data/ContentSource.kt` (new)
- `android/app/src/main/java/.../ui/composables/SourceRow.kt` (new)
- `android/app/src/main/java/.../ui/SettingsScreen.kt` (9 rows added)
- `android/app/src/main/java/.../localization/AppStrings.kt` (9 new entries EN+ES)
- `android/app/src/test/java/.../data/SaintRepositoryTest.kt` (count 79→81)

---

### Saint `sources` Array Integrity (2026-04-23)
**Author:** Frodo (iOS Dev)  
**Status:** Implemented ✅

**Context:**
`SaintDetailView.sourcesSection` renders each item in `saint.sources` as a tappable `Link` only when the name is also a key in `saint.sourceURLs`. A 2025-07 URL sweep replaced publisher URLs (Loyola Press, Hallow, Ascension, Lifeteen, Focus) with new ones (Franciscan Media, CNA, EWTN) in `sourceURLs`, but did not update the `sources` display array. Result: 27 of 79 saints had non-tappable source names (Cabrini was the user-reported case).

**Decision:**
For every saint with `sourceURLs`, the `sources` array MUST equal `Array(sourceURLs.keys)`. Enforced immediately by syncing all 27 offending entries in both `saints-en.json` and `saints-es.json` (commits `7fb793c`, `14d07a9`).

**Implications:**
- **Samwise (Data):** Keep `sources` and `sourceURLs` keys in lockstep when adding/editing saints. When rewriting URLs, update both fields.
- **Gandalf (Architect):** Future schema migration should collapse into single `[String: String]` map to make this class of bug impossible.
- **Legolas (Tests):** Add data-integrity test: `for saint in all: assert saint.sources == Array(saint.sourceURLs.keys) when sourceURLs non-empty`.

**Android Note (Aragorn):**
Android `SaintDetailScreen.SourcesSection` (Compose) already correctly gates tappability on `saint.sourceURLs?.get(source) != null` using `LocalUriHandler`. No code change needed — repaired JSON flows in via `syncSharedContent` Gradle task. Companion UI reorder (Support & Legal above Content Sources) shipped for Android parity (commits `8532dd3`, `680b4f2`).

**Debugging Tip:**
For cross-platform data issues: canonical source is `SharedContent/saints/*.json`; `android/app/src/main/assets/saints-*.json` is build-generated and may lag. A real mismatch in `SharedContent/` is a bug; assets-only mismatch just means regeneration pending.

**Impact:**
- ✅ Cabrini now tappable
- ✅ 27 saints repaired
- ✅ Parity on iOS/Android Settings UI
- Data integrity rule documented for future maintenance

---

### Decision: Promotional Video Production Pipeline (2026-04-24)

**Date:** 2026-04-24  
**Author:** Galadriel (Video/Motion specialist)  
**Status:** In design (awaiting 4 creative decisions from Jorge)  
**Unblocks:** Content team (Apple App Store previews, social marketing assets)

---

#### 1. Video Scaffold & Delivery Strategy

**Framework:** Remotion 4.0.451 (React + TypeScript, Node.js runtime)  
**Project location:** `video/` directory at repo root (isolated from iOS/Android codebases)

**Composition contract:**

| ID | Aspect Ratio | FPS | Duration | Target |
|----|---|---|---|---|
| ConfirmationSaintsPromo | 1080×1080 (square) | 30 | 30s (900 frames) | LinkedIn square, portable to Instagram/X |

Future variants (15s cutdown, 9:16 vertical for Reels/Shorts/TikTok) will be separate compositions in the same Remotion project, not separate projects.

**Render pipeline:**
- Dev: `cd video && npm start` → Remotion Studio (interactive preview)
- Production: `cd video && npm run render` → `video/out/ConfirmationSaintsPromo.mp4` (H.264, yuv420p)
- Verified: Full 30s placeholder renders in ~30s, output ~1.6 MB

**Delivery:** Approved renders copied from `video/out/` → `docs/video/` for App Store, social channels, and marketing site distribution.

---

#### 2. Creative Direction (Three Treatments Proposed)

**Recommended: Treatment A — "Find Your Saint"**

One-line concept: A mosaic of saint portraits flows upward, then resolves onto one featured saint who "chooses you"—demonstrating the app's core promise of personal discovery.

**Why Treatment A:**
- Leads with audience emotion ("Preparing for Confirmation?") not product
- Mosaic shot is visually distinctive on feed (high engagement potential)
- Still includes real app UI (screenshots segment) so viewers understand the product
- Works muted (LinkedIn autoplay default)

**Shot-by-shot (30s, 900 frames @ 30fps):**

| Time | Frames | Shot |
|------|--------|------|
| 0.0–2.5s | 0–75 | Black → red (#B9161C) radial burst. Title: "Preparing for Confirmation?" (serif display, white, center) |
| 2.5–4.0s | 75–120 | Subtitle dissolves in: "81 saints. One is yours." |
| 4.0–14.0s | 120–420 | Saint mosaic: ~20 portraits from `SharedContent/images/` flow upward in 3 parallel columns (varying speeds). Overlay: rotating tags ("martyr," "young," "mystic," "teacher," "missionary," "sports," "music") |
| 14.0–18.0s | 420–540 | Mosaic slows, collapses into single highlighted card (featured saint detail: portrait, name, feast day, one patronage line) |
| 18.0–23.0s | 540–690 | Card transitions into phone frame showing `docs/android/phone-screenshot-2-saint-detail.png`. Captions: "Bios. Quotes. Feast days. Offline." |
| 23.0–27.0s | 690–810 | Triptych: 3 more phone screenshots (saints list, explore/filters, about) — crossfade, ~1.3s each. Caption: "English + Español." |
| 27.0–30.0s | 810–900 | App icon zooms on red background. Wordmark: "Confirmation Saints" + small line: "Free on App Store & Google Play." |

**Alternative Treatments (B & C also analyzed and available; see orchestration log):**
- **Treatment B:** "One Quote at a Time" — three saint quotes over portraits, devotional tone
- **Treatment C:** "Swipe to Discover" — simulated user journey through app UI, product-demo focus

---

#### 3. Asset Sourcing & Attribution

**Images:** ~20 saint JPGs from `SharedContent/images/` — Wikimedia Commons public domain or CC licensed. Wikimedia attribution preserved in end-card fine print or supplementary credits block per final treatment.

**Screenshots:** 4 Android phone screenshots from `docs/android/` (team-authored, free to use).

**Audio:** Currently undefined. LinkedIn autoplays muted; audio optional. If included, must be royalty-free. Jorge to confirm decision.

**Fonts:** Serif display (Cormorant Garamond or equivalent) for headings + clean sans (Inter) for UI copy, both via `@remotion/google-fonts`.

---

#### 4. Open Questions for Creative Approval

Treatment A awaits Jorge's input on 4 decisions before full implementation:

1. **Featured saint** in card-resolve moment (sec 14–18): Carlo Acutis recommended (recent teen, high relevance), or Thérèse of Lisieux, or your choice?
2. **Wordmark style:** Plain text "Confirmation Saints" or Play Store icon lockup?
3. **Audio track:** Royalty-free ambient pad (for users who unmute), or silent?
4. **Store badges:** Official App Store / Google Play SVGs at end, or omit?

---

#### 5. Technical Stack Decisions

- **Version pinning:** All `@remotion/*` packages same version (mixed versions break bundler). Use `npm run upgrade` (wraps `remotion upgrade`), never individual `npm update`.
- **Composition types:** Every composition explicitly declares `width`, `height`, `fps`, `durationInFrames`.
- **Rendering strategy:** H.264 codec, yuv420p pixel format for max platform compatibility.
- **CI/CD:** No automated render workflow yet. Manual renders via dev machine. Revisit once approved treatment ships regularly.

---

#### 6. File Manifest

**Source files:**
- `video/README.md` — setup + render instructions
- `video/package.json` — Remotion 4.0.451, React 19, TypeScript 5.8
- `video/src/ConfirmationSaintsPromo.tsx` — main composition (placeholder, awaiting decisions)
- `.squad/agents/galadriel/charter.md` — role + boundaries
- `.squad/agents/galadriel/history.md` — learnings + context
- `.squad/skills/remotion-scaffolding/remotion-scaffolding.md` — reusable scaffold guide for future video projects

**Gitignore entries added to root `.gitignore`:**
- `video/node_modules/`
- `video/out/` (render outputs)
- `video/.cache/`
- `video/.remotion/`
- `video/.npm-cache/`

---

#### 7. Boundaries Reaffirmed

Galadriel does NOT:
- Modify iOS (Swift) or Android (Kotlin) app code — defers to Frodo/Aragorn
- Alter saint data, localization JSON, or SharedContent — defers to Samwise
- Define product architecture — defers to Gandalf

Galadriel MAY:
- Read any app source for visual reference (screenshots, colors, copy)
- Use saint images from `SharedContent/images/` with proper attribution preserved

---

#### Summary

Video production pipeline ready for creative implementation once 4 Jorge decisions are confirmed. Remotion scaffold validated (smoke render success). Three treatment options delivered; Treatment A recommended. Work begins post-approval.

