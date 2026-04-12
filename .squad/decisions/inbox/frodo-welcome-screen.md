# Decision: App Rename + Welcome Onboarding

**Author:** Frodo (iOS Dev)
**Date:** 2026-07-13
**Status:** Implemented

## App Rename
- Display name changed from "Catholic Saints" → "Confirmation Saints" per user preference
- Internal folder `CatholicSaints/` and bundle ID `com.jorgebalderas.CatholicSaints` unchanged (avoids breaking changes)
- Updated in: `project.yml`, `README.md`

## Welcome/Onboarding Screen
- 4-page TabView onboarding shown on first launch via `@AppStorage("hasSeenWelcome")`
- Pages: Welcome → Discover → Learn → Get Started (with "Let's Go!" CTA)
- Purple/gold liturgical theme consistent with existing accent color
- Fully bilingual EN/ES via String Catalog
- Replayable from Settings → "Show Welcome Screen"
- Pattern: `OnboardingPageView` reusable component for consistent page layout

## Impact on Other Agents
- **Samwise:** No data changes needed
- **Legolas:** New `WelcomeView.swift` and modified `SettingsView.swift` need test coverage
- **Gandalf:** No architecture changes — follows existing MVVM + @AppStorage pattern
