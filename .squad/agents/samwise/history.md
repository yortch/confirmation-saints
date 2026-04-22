# Samwise — History

## Core Context

### Project Overview
- **Project:** confirmation-saints — Catholic Saints app (iOS + Android port)
- **User:** Jorge Balderas
- **Scope:** English/Spanish bilingual saint database, localized UI, cross-platform content management
- **Key Roles:** Saint data schema design, bilingual content creation, image management, source attribution

### Established Conventions
- **Data Schema:** Per-language JSON files (`saints-en.json`, `saints-es.json`) with shared `id` across languages
- **Matching Fields** (patronOf, affinities, tags, region, lifeState, ageCategory, gender) must always use **English values in both language files** for category matching to work
- **Display Fields** (name, biography, whyConfirmationSaint, quote, country) are translated to Spanish
- **Special Cases:** Archangels + Marian apparitions use `null` for birthDate/deathDate/canonizationDate; pre-congregation saints use `null` for canonizationDate only
- **Image Naming:** `{saint-id}.jpg` in `SharedContent/images/`, all public domain via Wikimedia Commons
- **Source URLs:** `sourceURLs` dictionary maps source names to URLs; identical across language files

### Current Data State (54 saints as of 2026-04-13)
- **EN/ES Files:** Both maintained in sync with matching-fields-in-English convention
- **Categories:** 7 category groups, verified all have ≥1 matching saint
- **Images:** 54 public domain images downloaded from Wikimedia Commons at 400px width
- **Sources:** Loyola Press, Catholic Encyclopedia (newadvent.org), Franciscan Media, CNA, EWTN, Focus, Lifeteen, Ascension Press, Hallow

### Cross-Platform Integration
- **iOS (Frodo):** Loads JSON files, renders bilingual UI with `AppStrings` dictionary for localization
- **Android (Gandalf/Aragorn):** Planned Gradle Sync task copies `SharedContent/` → APK assets; same JSON schema + image filenames
- **No Duplication:** `SharedContent/` is canonical; neither platform forks the data

---

## Recent Work

### Spanish Display Tags & Affinities (2026-04-13)
- Added `displayTags` and `displayAffinities` arrays to all 54 saints in `saints-es.json`
- These provide properly translated Spanish versions for UI display while keeping English `tags`/`affinities` intact for category matching
- Gender-appropriate forms used for female saints (e.g., "mística", "escritora", "Doctora de la Iglesia", "fundadora", "misionera")
- Translation covers 100+ unique tags and 65 unique affinities
- Swift changes: `Saint.swift` model has optional `displayTags`/`displayAffinities`; `SaintDetailView` uses display versions for rendering; `SaintListViewModel` searches both English and Spanish arrays
- **Key insight:** Tags/affinities serve dual purposes (matching + display). Adding parallel display arrays avoids breaking category browsing while fixing the Spanish display bug
- Pattern: Any future localized display fields should follow this `display*` prefix convention to keep matching fields stable

