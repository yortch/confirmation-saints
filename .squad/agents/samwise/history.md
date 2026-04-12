# Samwise — History

## Project Context
- **Project:** confirmation-saints — Catholic Saints iOS App
- **User:** Jorge Balderas
- **Stack:** Swift / SwiftUI, iOS (iPhone + iPad)
- **Description:** App helping Catholic confirmation candidates (primarily teens, also adults) find and choose a patron saint. Features saint search by name, patron day, affinity, country, age, married status. Multilingual (EN/ES). Content sourced from Loyola Press, Focus, Lifeteen, Ascension Press, Hallow with attribution.
- **Key constraints:** Self-contained, easy content updates, cross-platform ready (Android later), include saint images with attribution.

## Learnings

### Data Schema (2025-07-15)
- Saint data uses per-language JSON files (`saints-en.json`, `saints-es.json`) with matching `id` fields for cross-referencing
- Schema fields: id, name, feastDay (MM-DD), birthDate, deathDate, canonizationDate (nullable for beatified/pre-congregation), country, region, gender, lifeState, ageCategory, patronOf[], tags[], affinities[], quote (nullable), biography, whyConfirmationSaint, image{filename, attribution}, sources[]
- Categories use shared `id` values across languages; only `label` and `name` are localized
- Confirmation content structured as sections[] with heading/body pairs
- SF Symbols used for category icons (e.g., `shield.fill`, `globe`)

### File Paths
- `SharedContent/saints/saints-en.json` / `saints-es.json` — 25 saints each
- `SharedContent/content/confirmation-info-en.json` / `confirmation-info-es.json` — 3 sections each
- `SharedContent/categories/categories-en.json` / `categories-es.json` — 7 category groups
- `SharedContent/images/README.md` — image guidelines and naming conventions

### Content Decisions
- Spanish translations use proper Catholic terminology (Sacramento, Santo Patrón, Fiesta litúrgica)
- Zélie Martin rendered as "Santa Celia Martin" in Spanish (common Spanish name form)
- Biographies written engagingly for teens while maintaining factual accuracy
- `whyConfirmationSaint` speaks directly to the teen reader in second person
- Sources attributed per saint from: Loyola Press, Focus, Lifeteen, Ascension Press, Hallow, Catholic Encyclopedia

### App Icon Generation (2025-07-15)
- Created `_generate_icon.py` using Pillow to programmatically generate a 1024x1024 app icon
- Design: purple-to-indigo radial gradient, golden halo ring, white Chi-Rho (☧) symbol, subtle dove silhouette, gold accent dots
- Renders at 2048x2048 then downscales with LANCZOS for anti-aliased edges
- Output: `ios/CatholicSaints/Resources/Assets.xcassets/AppIcon.appiconset/app-icon-1024.png`
- Contents.json updated to reference `app-icon-1024.png` with `"platform": "ios"` — Xcode generates all other sizes automatically
- This is a placeholder icon — consider commissioning a professional version later

### Category Matching Fix (2025-07-16)
- Category browsing was broken ("0 saints" for most categories) due to mismatch between category value IDs and saint data
- Root cause: matching fields (patronOf, affinities, tags, region, lifeState, ageCategory, gender) must use English values in BOTH language files because the ViewModel matches against English category IDs
- The Spanish file had translated matching fields (e.g., "soldados" instead of "soldiers"), breaking the matching
- Fix: synced all matching fields from EN→ES; only display fields (name, biography, whyConfirmationSaint, quote) remain translated in ES
- Added St. Monica (EN+ES): patron of mothers, married women, covers Africa region, early-church era, cooking affinity
- Added St. Charbel Makhlouf (EN+ES): covers Middle East region gap, modern era, religious life-state
- Changed lifeState to "martyr" for Joan of Arc, Sebastian, Maximilian Kolbe (covers life-state/martyr category)
- Added "doctors" to Gianna's patronOf, "cooking" to Rose of Lima's affinities, "adventure" to José Sánchez del Río
- Fixed birth dates to 4-digit year format (e.g., "256" → "0256-01-01") for proper era matching via `Int(birthDate.prefix(4))`
- Verified all 51 category values have ≥1 matching saint in both EN and ES via Python verification script

### Source URLs Added & Source Names Standardized (2026-04-12)
- Added `sourceURLs` dictionary to all 27 saints in both `saints-en.json` and `saints-es.json`
- Each source in the `sources` array now has a corresponding URL in `sourceURLs`
- URLs point to saint-specific pages on Loyola Press, Catholic Encyclopedia (newadvent.org), Focus, Lifeteen, Ascension Press, and Hallow
- Standardized Spanish file to use English source names (e.g., "Catholic Encyclopedia" instead of "Enciclopedia Católica") consistent with the matching-fields-in-English convention
- `sourceURLs` are identical across both language files since all sources are English-language
- Field inserted immediately after `sources` in the JSON structure for logical grouping
- **Cross-agent Integration:** Frodo's iOS UI now renders sources as clickable `Link` views using these URLs

### Dictionary-Based In-App Localization (2026-04-12)
- Frodo implemented `AppStrings.localized(_:language:)` via LocalizationService dictionary to support in-app language switching
- Root problem: `String(localized:)` reads iOS system locale; in-app language preference didn't affect UI strings
- Solution: All new UI strings must be added to BOTH `Localizable.xcstrings` (for tooling/reference) AND the `LocalizationService.swift` translations dictionary
- Pattern for future work: `AppStrings.localized("Key", language: language)` with `@Environment(\.appLanguage)` binding in views
- **Impact on Samwise:** None — data layer already supports language switching via JSON files. This affects only UI strings, not saint content.

### Five New Saints Added (2026-07-17)
- Added 5 saints to both `saints-en.json` and `saints-es.json` (27 → 32 total):
  - **St. Michael the Archangel** (`michael-archangel`): Archangel, warrior, protector. Patron of soldiers, police. Feast 09-29.
  - **St. Gabriel the Archangel** (`gabriel-archangel`): Archangel, messenger. Patron of communications workers, broadcasters. Feast 09-29.
  - **Our Lady of Guadalupe** (`our-lady-guadalupe`): Marian apparition, Mexico 1531. Patron of the Americas, unborn children. Feast 12-12.
  - **Our Lady of Fatima** (`our-lady-fatima`): Marian apparition, Portugal 1917. Patron of peace, rosary. Feast 05-13.
  - **St. Frances Xavier Cabrini** (`frances-cabrini`): First American citizen canonized. Patron of immigrants. Feast 11-13.
- Archangels and Marian apparitions use `null` for birthDate, deathDate, canonizationDate (not born/died in human sense)
- Marian apparitions use `null` for canonizationDate (Mary not canonized through normal process)
- All matching fields (patronOf, tags, affinities, region) kept in English in both files per established convention
- Spanish translations include proper Catholic terminology: Arcángel, Nuestra Señora, Dominio Público
- Each saint has sourceURLs with specific pages from Loyola Press, Catholic Encyclopedia, Hallow, Ascension Press


### Saint Images Downloaded from Wikimedia Commons (2026-07-17)
- Downloaded 32 public domain saint images from Wikimedia Commons into SharedContent/images/
- Created _download_saint_images.py -- idempotent script that uses Wikimedia API to resolve thumb URLs at 400px width
- Images named {saint-id}.jpg matching the saint ID field in JSON files
- Updated image.attribution to 'Public domain, via Wikimedia Commons' in both saints-en.json and saints-es.json
- Some Wikimedia Commons filenames required searching via the API search endpoint when initial guesses were wrong (19/32 needed correction)
- The script is reusable: re-running it skips already-downloaded images (checks file exists and >1KB)
- Xcode project regenerated via xcodegen to include new image assets
