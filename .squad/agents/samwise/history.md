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
