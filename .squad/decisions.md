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

### User Directives (Captured)
- **2026-04-12T17:13:10Z:** Jorge Balderas — App name changed to "Confirmation Saints". Update all references.
- **2026-04-12T16:29Z:** Jorge Balderas — Project scaffolded with cross-platform separation. Expand saint roster to 50-100+. Add "most popular saints" categories by year + all-time.

## Governance

- All meaningful changes require team consensus
- Document architectural decisions here
- Keep history focused on work, decisions focused on direction
