# Confirmation Saints

> *Help confirmation candidates discover and choose their patron saint.*

A SwiftUI iOS app (iPhone + iPad) that helps Catholic confirmation candidates — primarily teenagers, but also adults — explore the lives of the saints and find a meaningful patron for their confirmation journey.

## 🎯 Target Audience

- **Primary:** Teenagers preparing for the Sacrament of Confirmation
- **Secondary:** Adults receiving Confirmation later in life
- **Also useful for:** Catechists, youth ministers, RCIA leaders

## 📱 Features

- **Browse Saints** — Scroll through a curated list of saints with localized names and descriptions
- **Search & Filter** — Find saints by name, interests/affinities (sports, music, art, science), country of origin, and more
- **Filter by Life Stage** — Young saints, married saints
- **Saint Detail Pages** — Biography, feast day, patron associations, source attribution
- **About Confirmation** — Explain the sacrament and the tradition of choosing a patron saint
- **Bilingual** — Full English and Spanish support (UI strings + saint content)
- **Source Attribution** — Every saint entry cites its information sources (Loyola Press, Catholic News Agency, Franciscan Media, etc.)
- **Image Attribution** — Saint images include proper attribution where available

## 🏗️ Architecture

### Cross-Platform Repository Structure

This repository is organized for **cross-platform development**. Shared content lives at the root; platform-specific code lives in its own directory.

```
confirmation-saints/
├── SharedContent/            # Cross-platform content (JSON data, images)
│   ├── saints/               # Saint data (per-language JSON)
│   ├── categories/           # Category definitions (per-language JSON)
│   ├── content/              # Confirmation info (per-language JSON)
│   └── images/               # Saint images with attribution
├── ios/                      # iOS app (SwiftUI)
│   ├── CatholicSaints/       # Swift source
│   │   ├── App/              # App entry point
│   │   ├── Models/           # Data models (Codable structs)
│   │   ├── ViewModels/       # Observable view models
│   │   ├── Views/            # SwiftUI views
│   │   ├── Services/         # Data loading
│   │   └── Resources/        # Assets, localization
│   ├── CatholicSaints.xcodeproj/
│   └── project.yml           # XcodeGen spec
├── android/                  # Future Android app (Kotlin/Compose)
│   └── README.md
└── README.md
```

### Pattern: MVVM with SwiftUI (iOS)

### Data Flow

1. **JSON data** lives in `SharedContent/` — platform-agnostic, designed for reuse on Android
2. **`SaintDataService`** loads and decodes JSON from the app bundle
3. **`SaintListViewModel`** holds state, exposes filtered results
4. **SwiftUI views** observe the view model and render

### Localization Strategy

| Content Type | Format | Location |
|---|---|---|
| UI strings (labels, buttons) | String Catalog (.xcstrings) | `ios/CatholicSaints/Resources/Localizable.xcstrings` |
| Saint content (bios, descriptions) | JSON with `LocalizedText` | `SharedContent/saints/` |
| Category names | JSON with `LocalizedText` | `SharedContent/categories/` |

**Why this split?**
- UI strings use Apple's native String Catalog for easy Xcode editing and pluralization
- Saint content uses JSON `LocalizedText` objects (`{"en": "...", "es": "..."}`) so the same data files work on Android without conversion

### Key Design Decisions

- **iOS 17+ deployment target** — Uses modern SwiftUI APIs (NavigationStack, Observable macro)
- **Swift 6 concurrency** — Sendable types, @MainActor annotations
- **JSON-first content** — All saint data in JSON for cross-platform reuse
- **XcodeGen** — Project file generated from `ios/project.yml` (no manual .pbxproj edits)
- **Cross-platform ready** — SharedContent/ at repo root shared between iOS and future Android

## 📖 Content Sources & Attribution

Saint information is sourced from trusted Catholic resources. Each saint entry includes specific source citations:

- [Loyola Press](https://www.loyolapress.com/catholic-resources/saints/)
- [Catholic News Agency](https://www.catholicnewsagency.com/saints)
- [Franciscan Media](https://www.franciscanmedia.org/saint-of-the-day/)
- [Focus](https://focus.org/)
- [Lifeteen](https://lifeteen.com/)
- [Ascension Press](https://ascensionpress.com/)
- [Hallow](https://hallow.com/)

Images are used with attribution. Each saint's `imageAttribution` field credits the source.

## 🔨 Build & Run

### Prerequisites

- **Xcode 15+** (tested with Xcode 16)
- **iOS 17.0+ SDK**
- **[XcodeGen](https://github.com/yonaskolb/XcodeGen)** — Install with `brew install xcodegen`

### Steps

```bash
# 1. Generate the Xcode project from project.yml
cd ios
xcodegen generate

# 2. Open in Xcode
open CatholicSaints.xcodeproj

# 3. Select a simulator (iPhone or iPad) and hit ⌘R
```

### Command-Line Build

```bash
cd ios

# Generate project
xcodegen generate

# Build for simulator
xcodebuild -project CatholicSaints.xcodeproj \
  -scheme CatholicSaints \
  -sdk iphonesimulator \
  -destination 'platform=iOS Simulator,name=iPhone 16' \
  build

# Build for device (no signing)
xcodebuild -project CatholicSaints.xcodeproj \
  -target CatholicSaints \
  -sdk iphoneos \
  build CODE_SIGNING_ALLOWED=NO
```

## ➕ How to Add a New Saint

1. Open `SharedContent/saints/saints-en.json` (or `saints-es.json` for Spanish)
2. Add a new saint object following the existing schema:

```json
{
  "id": "st-unique-id",
  "name": { "en": "Saint Name", "es": "Santo Nombre" },
  "feastDay": "Month Day",
  "birthYear": 1234,
  "deathYear": 1300,
  "canonizationYear": 1500,
  "countryOfOrigin": "Country",
  "biography": { "en": "English bio...", "es": "Spanish bio..." },
  "shortDescription": { "en": "Short desc", "es": "Desc corta" },
  "patronOf": [{ "en": "Something", "es": "Algo" }],
  "affinities": ["sports", "music"],
  "categories": ["young-saints", "martyrs"],
  "isYoungSaint": false,
  "wasMarried": false,
  "imageName": null,
  "imageAttribution": null,
  "sources": [{ "name": "Source Name", "url": "https://..." }]
}
```

3. Build and run — the saint appears automatically

### Available Affinities

`sports`, `music`, `art`, `science`, `education`, `military`, `writing`, `nature`, `healing`, `leadership`

### Available Categories

`young-saints`, `women`, `men`, `martyrs`, `doctors-of-church`, `founders`

## 🌍 How to Add a New Language

### UI Strings
1. Open `ios/CatholicSaints/Resources/Localizable.xcstrings` in Xcode
2. Click "+" to add a new language
3. Translate each string entry

### Saint Content
1. Add a new field to `LocalizedText` in `ios/CatholicSaints/Models/Saint.swift`:
   ```swift
   struct LocalizedText: Codable, Hashable, Sendable {
       let en: String
       let es: String
       let fr: String  // Add new language
   }
   ```
2. Update the `localized` computed property to handle the new language code
3. Add the new language translations to every `LocalizedText` object in `SharedContent/saints/` and `SharedContent/categories/`

## 🗺️ Future Plans

- **Android version** — `SharedContent/` JSON is designed for cross-platform reuse. The `android/` directory is scaffolded and ready for a Kotlin/Jetpack Compose app that reads the same data files.
- **More saints** — Expand from seed data to a comprehensive database
- **Favorites** — Let users save saints they're considering
- **Saint of the Day** — Daily featured saint based on feast day
- **Quiz / Match** — "Which saint is most like you?" interactive feature
- **Offline-first** — All content bundled, no network required
- **Dark mode** — Full dark mode support with themed saint imagery
- **Accessibility** — VoiceOver, Dynamic Type, high contrast support

## 📄 License

TBD

---

*Built with ❤️ for confirmation candidates everywhere.*
