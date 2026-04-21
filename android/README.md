# Confirmation Saints — Android

> Upcoming Android port of [Confirmation Saints](../README.md). The iOS app is live on the [App Store](https://apps.apple.com/app/confirmation-saints/id6762463641); this port brings the same experience to Android.

## Status

🚧 **Not yet started.** Scaffolding only.

## Planned Stack

- **Kotlin** + **Jetpack Compose** + Material 3
- **min SDK 26 / target SDK 34** (tentative)
- **ViewModel + StateFlow** (mirrors the iOS `@Observable` `SaintListViewModel`)
- **DataStore<Preferences>** for `appLanguage` / `hasSeenWelcome`
- **kotlinx.serialization** for JSON decoding
- Suggested bundle id: `com.jorgebalderas.confirmationsaints`

## Data Source

Consumes `../SharedContent/` directly — **do not fork**. That directory is the canonical cross-platform source of truth for:

- `saints/saints-{en,es}.json`
- `categories/categories-{en,es}.json`
- `content/confirmation-info-{en,es}.json`
- `images/*.jpg`

Wire it into the Android build as an asset source set (e.g. `sourceSets["main"].assets.srcDir("../SharedContent")`) so both apps stay in sync.

## Porting Guide

See the **🤖 Android Port Guide** section in the [root README](../README.md#-android-port-guide) for:

- The canonical saint / category JSON schema
- What is reusable as-is vs. what needs native reimplementation
- Filtering rules (match on canonical English ids, render `display*` arrays)
- Diacritic-insensitive search requirement (use `java.text.Normalizer` NFD + strip combining marks)
- Decisions that must be respected (welcome-screen gating, locale auto-detect, feast-day format, branding)
