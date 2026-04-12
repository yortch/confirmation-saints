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

### User Directives (Captured)
- **2026-04-12T17:13:10Z:** Jorge Balderas — App name changed to "Confirmation Saints". Update all references.
- **2026-04-12T16:29Z:** Jorge Balderas — Project scaffolded with cross-platform separation. Expand saint roster to 50-100+. Add "most popular saints" categories by year + all-time.

## Governance

- All meaningful changes require team consensus
- Document architectural decisions here
- Keep history focused on work, decisions focused on direction
