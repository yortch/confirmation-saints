# Frodo — History

## Core Context

- **Project:** confirmation-saints
- **User:** Jorge Balderas
- **My role:** iOS Dev (Swift/SwiftUI)
- **Joined:** 2026-04-12

### iOS App Foundation (2026-04-12)
- **Architecture:** MVVM + SwiftUI with Observable macro (iOS 17+); XcodeGen generates .xcodeproj from project.yml
- **Swift 6 Concurrency:** All models Sendable, services @MainActor
- **UI Localization:** .xcstrings String Catalog (EN/ES); in-app language switch via @AppStorage("appLanguage") + custom EnvironmentValues
- **Navigation:** NavigationStack (not deprecated NavigationView); no duplicate .navigationDestination for same type in hierarchy
- **Data Model:** Per-language JSON files (saints-en/es.json, categories-en/es.json) in SharedContent/
- **Theme:** Purple accent (liturgical), SF Symbols, gradient avatars; FlowLayout for tag chips
- **Build:** XcodeGen regen successful; Swift 6 typecheck passes clean on all 13 files

---

## Key Learnings

### UI/UX Patterns
- **NavigationStack conflicts:** Never declare duplicate `.navigationDestination` for the same type at multiple hierarchy levels — only the outermost one (on/near the `NavigationStack`) should exist. CategorySaintsListView bug (2025-07-15) was caused by inner `.navigationDestination` conflicting with parent.
- **Closure vs value NavigationLinks:** Mixing closure-based and value-based NavigationLinks in the same NavigationStack causes resolution issues on iOS 17+. Use one style consistently per stack.
- **Default tab selection:** Use `@State selectedTab = 1` + `TabView(selection:)` with `.tag(0)...tag(4)` to set default (Explore tab).
- **In-app language switching:** `String(localized:)` always uses system locale; for in-app switching, use a manual translation dictionary via `AppStrings.localized(_:language:)`.
- **Dynamic locale from system:** Use `Locale.current.language.languageCode` as default; @AppStorage default only applies when no prior value saved.

### Data & Integration
- **Source URL patterns:** Loyola Press saint pages are gone entirely (no new pattern). Franciscan Media, Catholic News Agency, EWTN are reliable with stable patterns.
- **Sources ↔ sourceURLs sync:** `sources` display array and `sourceURLs` dictionary must have matching keys. When updating URLs, rewrite both or schema risks non-tappable links.
- **Cross-language saint matching:** Match by URL value (not name) since EN/ES names differ. Single EN/ES file pair can have different broken URLs for same saint.
- **SaintImageView pattern:** Shared component tries asset catalog → bundle path → fallback initial circle. Eliminates duplicated `colorForSaint` helpers across views.

### Build & Release
- **Version reading:** Use `Bundle.main.infoDictionary?["CFBundleShortVersionString"]` for dynamic version display (no hardcoding).
- **Simulator screenshots:** Tap navigation in app UI is required to change in-app language state — system locale flags do NOT update @AppStorage. Capture at 1320×2868 (2x for 6.9" device) for App Store.
- **XcodeGen:** Run `xcrun swiftc -typecheck -swift-version 6` to verify syntax before XcodeGen regen; `xcodebuild ... build` for full compile.

---

## Work Timeline

### App Rename & Welcome Screen (2026-04-13)
- Renamed app from "Catholic Saints" → "Confirmation Saints" (display name only; folder stays `CatholicSaints/`)
- Updated `project.yml` (INFOPLIST_KEY_CFBundleDisplayName, PRODUCT_NAME) and README.md
- Created `Views/Onboarding/WelcomeView.swift` — 4-page TabView onboarding with PageTabViewStyle
- Uses `@AppStorage("hasSeenWelcome")` flag: shown on first launch, skipped after
- Settings gets "Show Welcome Screen" button that resets `hasSeenWelcome` to false
- Purple/gold liturgical gradient theme with custom LatinCrossView (Canvas-based Christian cross)
- Build verified clean with 14 Swift files

### UI Polish & Localization Overhaul (2025-07-16)
- Default tab → Explore (index 1)
- Fixed Explore → Saint navigation (removed conflicting .navigationDestination)
- Replaced SF Symbol `cross.fill` with custom LatinCrossView
- Created `AppStrings.localized(_:language:)` — all String(localized:) calls replaced for in-app language switching
- All new strings added to Localizable.xcstrings with EN/ES translations

### Source URL Fixes (2025-07-17)
- Replaced 46 broken sourceURLs across saints-en.json and saints-es.json
- Updated sources: Loyola Press → Franciscan Media, Hallow → CNA/EWTN, Ascension Press/Focus/Lifeteen → Franciscan Media/EWTN
- Matched by URL value (not name) for EN/ES sync

### Settings Content Sources Overhaul (2026-04-23)
- Added 2 new sources: Wikipedia (biographical) + Wikimedia Commons (public domain images)
- Updated 6 existing sources with descriptors and verified URLs
- Created `ContentSource` struct with name, url, description properties
- Replaced static `ForEach` over strings with `Link` views (opens Safari)
- Added external-link icon on right for discoverability
- Added 4 new localization strings (EN/ES)
- Swapped section order: Support & Legal now appears before Content Sources
- Build verified: BUILD SUCCEEDED

### iOS Sources Schema — `SourceEntry` struct (2026-04-23)
- Added `SourceEntry: Codable, Hashable, Sendable { name; url }` to `Saint.swift`
- Removed old `sources: [String]` and `sourceURLs: [String: String]?` fields
- Simplified `SaintDetailView.sourcesSection` — direct iteration, no dictionary lookup
- Commit `b449f59`

### Saint Source Links Fix (2026-04-23)
- Fixed non-tappable source links on Cabrini and 26 other saints
- Root cause: `sources` array and `sourceURLs` dictionary had divergent keys
- Fix: Synced each saint's `sources` array to be `Array(sourceURLs.keys)` (27 saints × 2 languages)
- No Swift code changes needed
- Commit `7fb793c` on branch `squad/add-saints-80-plus`

### Settings Screen Screenshot Capture for App Store v1.0.1 (2026-04-23)
- Captured updated Settings screen for iOS v1.0.1 submission showing 81 saints count
- Fixed hardcoded version → dynamic `Bundle.main.infoDictionary["CFBundleShortVersionString"]`
- Captured English + Spanish (in-app language toggled, not system locale)
- 1320×2868px (2x scale for 6.9" device), App Store ready
- **Lesson:** Simulator locale flags do NOT update in-app language state; must toggle through app UI

### App Store Submission Prep (2026-04-24)
- Verified SettingsView displays dynamic saint count
- saints-en.json verified contains 81 saints (from 79)
- Build succeeded on iPhone 17 Pro Max simulator
- Screenshots captured for App Store submission v1.0.1

### iOS Version Bump Convention (2026-04-25)
- Confirmed iOS marketing version is set in `ios/project.yml` via `MARKETING_VERSION`; run `cd ios && xcodegen generate` so `CatholicSaints.xcodeproj/project.pbxproj` mirrors it. `CURRENT_PROJECT_VERSION` remains the separate build number.

### v1.0.2 Release Orchestration Completed (2026-04-25)
- **Session:** v1.0.2 Over 100 Saints batch orchestration
- **Outcome:** iOS 1.0.2 (build 2) bumped and validated; 103-saint content parity confirmed
- **Cross-team:** Samwise (22-saint content) ✅, Aragorn (Android 1.0.2) ✅, Legolas (batch sign-off) ✅
- **Release status:** GO for App Store submission

### Modern Day Saints iOS Filter (2026-04-25)
- Added iOS quick-filter support for `era == "modern-day"`, defined as saints/blesseds with `birthDate` year >= 1900; keep this deterministic birth-year logic in SwiftUI filters for parity with category browsing.

### iOS Simulator Launch Diagnosis (2026-04-25)
- `objc[...] UIAccessibilityLoaderWebShared is implemented in both ... WebKit.axbundle ...` on iOS 26.4 simulator is a harmless simulator/runtime accessibility-bundle warning, not an app crash. Verified `xcodebuild -project CatholicSaints.xcodeproj -scheme CatholicSaints -destination 'platform=iOS Simulator,name=iPhone 17' build` succeeds and `simctl launch` starts `com.jorgebalderas.ConfirmationSaints`; no crash reports were generated.
- **Cross-team validation:** Legolas independently reproduced on iPhone 17 + iPad A16 simulators, both builds succeeded, both apps launched without crash. Diagnosis APPROVED. Modern Day Saints work cleared to proceed.

### Tappable Saint Images on iOS Detail Screen (2026-04-29)
- Confirmed iOS bundles `SharedContent/` as a folder resource via `ios/project.yml`; `SaintImageView` loads saint portraits from `SharedContent/images` with asset-catalog fallback.
- Confirmed Android bundles the same images through `syncSharedContent`, which copies `SharedContent/images/*.jpg` into `android/app/src/main/assets/images/` before `preBuild`.
- Added iOS detail-screen tap affordance only when the local bundled image resolves; the existing circular portrait remains unchanged and opens the same raster image larger in a localized sheet.

### SwiftUI Duplicate Chip IDs (2026-04-29)
- For saint detail chip rows, repeated display strings can be legitimate across affinity/tag data (e.g., "prayer"). Use collection indices as local `ForEach` IDs for repeated string chips so SwiftUI keeps each rendered chip distinct without mutating saint content.

### iOS 1.0.3 Release Prep (2026-04-29)
- iOS version source of truth is `ios/project.yml`; for App Store release prep update both `MARKETING_VERSION` and monotonically-increasing `CURRENT_PROJECT_VERSION`, then regenerate with `cd ios && xcodegen generate`. For v1.0.3 this is marketing version 1.0.3, build 3.
- Keep App Store release notes in `docs/appstore/submission-info.md` iOS-specific; Android/Google Play notes and version metadata can diverge during closed testing.

- Promoted newly captured App Store screenshots 09/10 into submission slots 03/05 after confirming 1284 x 2778 dimensions; no resize was needed.

## Learnings

### Spanish App Store Screenshots (2026-04-29)
- Captured Spanish App Store screenshots by setting simulator app defaults directly: `appLanguage=es` and `hasSeenWelcome=true`, then launching the real app and using `xcrun simctl io <device> screenshot` with absolute output paths.
- Output set completed under `docs/appstore/es/`: `03-saints-list.png`, `04-explore.png`, `05-saint-detail.png`, `06-settings.png`, and `07-ipad-about.png`; existing `01-welcome.png` and `02-about.png` were verified and left intact.
- iPhone captures used the existing iPhone 14 Plus simulator at 1284×2778; iPad capture used iPad Pro 13-inch (M5) at 2064×2752 to match `docs/appstore/07-ipad-about.png`.
- `simctl io screenshot` succeeded without macOS screenshot-permission blockage; relative output paths failed in this environment, so use absolute paths for reliable capture.

### Android Closed Testing Marketing Page (2026-04-30)
- Updated `docs/index.html` hero with a "Download test version for Android" CTA that anchors to `#platform-android` and activates the Android platform tab via safe hash handling.
- Android tab now states the app is in Google Play closed testing and instructs testers to first join `testers-community@googlegroups.com` at `https://groups.google.com/g/testers-community`.
- After joining the tester group, Android users are directed to download from `https://play.google.com/store/apps/details?id=com.yortch.confirmationsaints`.
