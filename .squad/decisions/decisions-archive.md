# Decisions Archive — confirmation-saints

Entries older than 30 days, archived 2026-04-21 by Scribe.

### Decision: Project Architecture — Catholic Saints iOS App
**Author:** Gandalf (Lead) | **Date:** 2026-04-12 | **Status:** Active

Greenfield iOS app for helping confirmation candidates choose a patron saint. Must support English/Spanish, be easy to update, and prepare for future Android version. Uses MVVM + SwiftUI with Observable macro on iOS 17+, XcodeGen for project management, dual localization (UI strings in .xcstrings + saint content in JSON), SharedContent directory for cross-platform data, and source attribution on all saints and images.

---

### Decision: UI Integration — Bilingual Data + Complete Views
**Author:** Frodo (iOS Dev) | **Date:** 2026-04-12 | **Status:** Implemented

Integrated Samwise's bilingual saint data (25 saints EN+ES) into the app and built out a complete 5-tab UI. Uses per-language loading (not LocalizedText struct), shared ViewModel pattern, environment-based language switching, category browsing via computed matching, and purple accent theme.

---

### Decision: Saint Data Schema Design
**Author:** Samwise (Data/Backend) | **Date:** 2025-07-15 | **Status:** Implemented

Platform-agnostic, bilingual data format for saints using separate JSON files per language with stable kebab-case IDs, nullable canonization dates, and affinities field for interest-based matching.

---

### Decision: Matching Fields Must Stay English in All Language Files
**Author:** Samwise (Data/Backend) | **Date:** 2025-07-16 | **Status:** Implemented

All matching fields (patronOf, affinities, tags, region, lifeState, ageCategory, gender) use English values in both saints-en.json and saints-es.json; only display fields are translated.

---

### Decision: User Directives — System Locale, Clickable Sources, Saint Images, Icon Design
**Author:** Jorge Balderas (via Copilot) | **Date:** 2026-04-12 | **Status:** Implemented

App language defaults to iOS system language with manual override; icon is white dove on red background; sources are clickable links; saint pictures included where available.

---

### Decision: Source URLs and Standardized Source Names
**Author:** Samwise (Data/Backend) | **Date:** 2026-04-12 | **Status:** Implemented

Added sourceURLs dictionary to all 27 saints with URLs pointing to saint-specific pages on various Catholic sources; standardized source names to English in ES file.

---

### Decision: Dictionary-Based In-App Localization
**Author:** Frodo (iOS Dev) | **Date:** 2026-04-12 | **Status:** Implemented

Created AppStrings.localized(_:language:) using a dictionary-based approach that maps EN keys to ES translations, allowing language switching without restarting.

---

### Decision: Reactive Language Switching Pattern
**Author:** Frodo (iOS Dev) | **Date:** 2025-07-18 | **Status:** Implemented

All views displaying localized content data must reactively observe the viewModel; never hold captured value-type snapshots. Detail views receive ID + viewModel reference, not pre-resolved objects.

---

### Decision: UI Polish — Splash Screen, Date Formatting, Tab Reorder
**Author:** Frodo (iOS Dev) | **Date:** 2026-04-13 | **Status:** Implemented

Added splash screen with 1.5s branded overlay, SaintDateFormatter utility for human-readable date display, and reordered tabs to About→Explore→Saints→Search→Settings with About as landing page.

---

### Decision: Spanish Display Tags and Affinities
**Author:** Samwise (Data/Backend) | **Date:** 2026-04-13 | **Status:** Implemented

Added displayTags and displayAffinities arrays to all saints in saints-es.json for localized UI display while keeping English fields intact for category matching.

---
