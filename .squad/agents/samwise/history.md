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

### Cross-Agent Sync: Image & URL Updates (2026-04-12T21:12:34Z)
**From:** Samwise (saint-images) + Frodo (source-urls) completion  
**Status:** ✅ Merged into decisions.md
- All 32 saints now have verified public domain images from Wikimedia Commons
- All 46 broken source URLs replaced with verified alternatives (Franciscan Media, CNA, EWTN, updated Hallow)
- Saint data integrity maintained across EN/ES files
- Decision records: "Saint Image Sources from Wikimedia Commons" and "Source URL Replacement Strategy"

### Saints Batch 2: Holy Family, Educators, Apostles (2026-07-17)
- Added 18 saints to both `saints-en.json` and `saints-es.json` (32 → 50 total)
- **Holy Family:** St. Joseph, St. Joachim, St. Anne
- **Educators:** St. John Bosco (Don Bosco), St. Marcellin Champagnat
- **Apostles (13):** Peter, Paul, Andrew, James the Greater, John, Philip, Bartholomew, Matthew, Thomas, James the Less, Jude Thaddeus, Simon the Zealot, Matthias
- All matching fields (patronOf, affinities, tags, region, lifeState, ageCategory, gender) kept in English in both EN and ES files per established convention
- Only display fields (name, biography, whyConfirmationSaint, quote) translated to Spanish; country translated per existing pattern (e.g., "Francia", "Italia")
- Pre-congregation saints (apostles, Holy Family) use `null` for canonizationDate; approximate birth/death dates used with 4-digit year format
- Downloaded 18 public domain images from Wikimedia Commons at 400px width; updated `_download_saint_images.py` with new mappings
- Some Wikimedia filenames required searching via API (8/18 initial guesses were wrong — apostle art filenames vary widely)
- sourceURLs use Catholic Encyclopedia (newadvent.org), Franciscan Media, CNA, and EWTN — verified URL patterns match existing saints
- Xcode project regenerated via xcodegen after adding images

### Four Priority Saints Added (2026-07-17)
- Added 4 saints to both `saints-en.json` and `saints-es.json` (50 → 54 total):
  - **St. Pius X** (`pius-x`): Pope, patron of first communicants. Feast 08-21. Known as "Pope of the Eucharist."
  - **St. Patrick** (`patrick`): Apostle of Ireland, patron of Ireland/engineers. Feast 03-17. Pre-congregation saint, canonizationDate null.
  - **St. Catherine of Siena** (`catherine-of-siena`): Doctor of the Church, mystic. Patron of Italy/Europe. Feast 04-29. ageCategory "young" (died at 33).
  - **St. Martin de Porres** (`martin-de-porres`): Dominican lay brother, patron of mixed-race people/social justice. Feast 11-03. Region "Americas" (Peru).
- All matching fields (patronOf, tags, affinities, region, lifeState, ageCategory, gender) kept in English in both EN and ES per established convention
- Only display fields (name, biography, whyConfirmationSaint, quote) and country translated to Spanish
- Sources: Franciscan Media + Catholic Encyclopedia (newadvent.org) — verified URL patterns
- Downloaded 4 public domain images from Wikimedia Commons at 400px width; updated `_download_saint_images.py` with new mappings
- Wikimedia filenames found via API search: Pius_X_pope.jpg, St._Patrick,_Bishop_of_Ireland_Met_DP890884.jpg, Giovanni_Battista_Tiepolo_096.jpg, Martin_de_Porres.jpg

### Spanish Display Tags & Affinities (2026-04-13)
- Added `displayTags` and `displayAffinities` arrays to all 54 saints in `saints-es.json`
- These provide properly translated Spanish versions for UI display while keeping English `tags`/`affinities` intact for category matching
- Gender-appropriate forms used for female saints (e.g., "mística", "escritora", "Doctora de la Iglesia", "fundadora", "misionera")
- Translation covers 100+ unique tags and 65 unique affinities
- Swift changes: `Saint.swift` model has optional `displayTags`/`displayAffinities`; `SaintDetailView` uses display versions for rendering; `SaintListViewModel` searches both English and Spanish arrays
- **Key insight:** Tags/affinities serve dual purposes (matching + display). Adding parallel display arrays avoids breaking category browsing while fixing the Spanish display bug
- Pattern: Any future localized display fields should follow this `display*` prefix convention to keep matching fields stable

