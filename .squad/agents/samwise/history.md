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

### Current Data State (79 saints as of 2026-04-23)
- **EN/ES Files:** Both maintained in sync with matching-fields-in-English convention
- **Categories:** 7 category groups, verified all have ≥1 matching saint
- **Images:** 79 images in `SharedContent/images/`, most public domain; a few CC BY-SA 4.0 / CC0 fallbacks where no PD portrait was available
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


## Learnings

### Add-10-Saints Batch (2026-04-23) — 70 → 79
- **Requested list had a duplicate.** The user's 10-saint list included St. Luke the Evangelist, who was already in the roster. Delivered 9 new saints; final count 79, not 80. Always diff requested ids against the existing `saints` array before estimating the target count.
- **Modern saints lack public-domain portraits.** 20th-century saints (Josemaría Escrivá) typically have photos still under copyright (life + 70 years). Use statue/altar photos marked CC0/CC BY-SA on Wikimedia Commons, and set `attribution` honestly — do NOT default to "Public domain, via Wikimedia Commons" when the file is CC BY-SA 4.0 or CC0. Attribution set per-saint:
  - CC BY-SA 4.0 → `"CC BY-SA 4.0, via Wikimedia Commons"` (EN) / `"CC BY-SA 4.0, vía Wikimedia Commons"` (ES)
  - CC0 → `"CC0, via Wikimedia Commons"` (EN) / `"CC0, vía Wikimedia Commons"` (ES)
- **Finding Commons images programmatically.** The pattern: candidate list first, then fall back to `action=query&list=search&srnamespace=6` on Commons. Check `extmetadata.LicenseShortName` before committing the download to pick PD where possible.
- **"Blessed" vs "St." in name.** No schema flag for Blessed — we use the `name` prefix (`Bl.` / `Bta.`) and the `"Blessed"` tag (capital B, matching existing Carlo Acutis / Chiara / Pier Giorgio). `canonizationDate: null` for Blessed entries (consistent with pre-congregation saints).
- **Doctors of the Church.** Tag exactly `"Doctor of the Church"` (capitalized, spaces). Applied to Teresa of Ávila (new) and Anthony of Padua (new — he was named Doctor in 1946). Spanish displayTag: `"Doctora de la Iglesia"` for female, `"Doctor de la Iglesia"` for male.
- **Hardcoded saint-count test.** `android/app/src/test/java/.../data/SaintRepositoryTest.kt` has a literal count assertion (`should_return_exactly_N_saints_for_each_language`). Must be updated whenever the roster size changes. Grep for the old count in `android/app/src/test/` before and after.
- **Parity script is authoritative.** `python3 tests/shared-content-parity.py` catches drift in canonical fields (patronOf, tags, affinities, region, lifeState, ageCategory, gender) + sourceURLs value-set + image-file presence. Run it after every data edit.
- **Useful authoritative source URLs discovered this round:**
  - Franciscan Media `/saint-of-the-day/...` (feast-day slug)
  - CNA `catholicnewsagency.com/saint/<slug>-<numeric-id>` (numeric id required; slug alone 404s)
  - newadvent.org `/cathen/NNNNNa.htm` (Catholic Encyclopedia, stable)

### Add-2-Saints to 81 (2026-04-23) — St. George + St. Mariana de Jesús
- **Request:** Add 2 specific saints to bring roster to 81 (St. George for his feast day April 23, St. Mariana de Jesús de Paredes as first canonized saint of Ecuador).
- **Delivered:** Both saints added to EN/ES JSON files with complete schema (canonical English fields, Spanish display arrays, sourceURLs with Wikipedia primary, imageAttribution PD via Wikimedia Commons).
- **Attribution backfill audit:** All 9 saints from commit 8f5727a already had complete sourceURLs + imageAttribution. Frances Cabrini also complete. No gaps found — all recent additions properly attributed.
- **Skill update:** Modified `.squad/skills/adding-saints/SKILL.md` to establish **Wikipedia (EN + ES) as the FIRST trusted source** for biographical facts, feast dates, patronage, and canonization info. Wikimedia Commons second for images. Catholic.org/Franciscan Media third for tiebreakers. Emphasized that `sourceURLs` AND `imageAttribution` are REQUIRED fields for every saint.
- **Images:** Downloaded both from Wikimedia Commons (PD licensed). St. George: Paolo Uccello painting. St. Mariana: colonial portrait.
- **Verification:** Parity test ✅, Android build + tests ✅, iOS build ✅. Final count: 81 EN, 81 ES saints.
- **Key learning:** The Android test (`SaintRepositoryTest.kt`) was already updated to expect 81 saints in a prior commit on this branch, anticipating this addition. Always check HEAD state before assuming test updates are needed.

### Data Integrity: sources ↔ sourceURLs Lockstep (2026-04-23)
- **Issue:** 27 saints had `sources` array names mismatched with `sourceURLs` keys. Root cause: 2025-07 URL rewrite (Loyola Press → Franciscan Media / CNA / EWTN) didn't update `sources` display names.
- **Result:** iOS SaintDetailView fell back to non-tappable text for those entries; Cabrini was the user-reported case.
- **Fix (Frodo + Scribe):** Synced all 27 saints' `sources` to equal `Array(sourceURLs.keys)` in both `saints-en.json` and `saints-es.json` (commits 7fb793c, 14d07a9).
- **Rule established:** When adding/editing saints, keep `sources` and `sourceURLs` keys in lockstep. When rewriting URLs, rewrite source names in both places.
- **Cross-agent implications:** Gandalf flagged schema collapse possibility (future: single `[String: String]` map). Legolas flagged test need (`assert sources == Array(sourceURLs.keys)`). Documented in decisions.md.

### Sources Schema Migration — Collapsed Array (2026-04-23)
- Migrated `SharedContent/saints/saints-en.json` and `saints-es.json` to Gandalf's collapsed schema (one ordered array of `{name, url}`). 81 saints × 2 files = 162 records, 338 source entries rewritten. Commit `a99666d`.
- Fail-fast validation (orphan URL, missing URL, empty string) passed on first run — no manual fixups required.

### Saint Backlog to 100: Gap Analysis & Prioritization (2026-04-25)
- **Request:** Grow from 81 → 100 saints; use Life Teen confirmation list as source; prioritize Asia/Africa → Female → Modern saints; skip pronunciation-difficult candidates.
- **Source:** Life Teen article (132 candidates), already-included saints (65), candidates not yet in app (66).
- **Exclusions:** 7 saints filtered for pronunciation difficulty (Korean, Malayalam, Ukrainian, Polish, Old English, Greek). Examples: Benedicta Hyon Kyongnyon, Volodymyr Pryjma, John Chrysostom.
- **Final Backlog:** 19 saints selected (difficulty <4, pronunciation-friendly) + 8 backups. Frontloaded with Asia/Africa region saints (Charles Lwanga, Cyril of Alexandria, Perpetua, Gregory of Narek, etc.), then female saints (Pauline, Margaret, Hildegard, Katharine Drexel), then modern saints (Mary MacKillop, Padre Pio, Miguel Pro, Jacinta Marto).
- **Key Finding:** Female African martyr saints (Perpetua, Aquilina, Apollonia) fit multiple priority categories; Asia has strong Doctor/theologian representation (Basil, Ephrem, Gregory of Narek, Cyril of Jerusalem). Modern African male martyr (Charles Lwanga, 1964 canonization) is highest-impact modern addition.
- **Documented:** Formal backlog table (19 priority + 8 backup) written to `.squad/decisions/inbox/samwise-saint-backlog-100.md` with patronages, feast-day markers, region, and data-entry notes (match fields English, display fields Spanish, Wikimedia Commons image sourcing).

## 2026-04-25: Saint Backlog 100-Saint Initiative (COMPLETED)
- Researched Life Teen Confirmation saints list (132 candidates)
- Compared against current 81-saint app inventory
- Applied pronunciation filter (excluded 7 difficult names)
- Drafted prioritized 19-saint backlog with coverage analysis
- Generated 8 backup candidates
- Deliverable: samwise-saint-backlog-100.md → merged to decisions.md
- Status: Ready for Legolas validation & Samwise research phase
