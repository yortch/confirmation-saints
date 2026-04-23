# Squad Decisions Archive (Older than 30 days)

## Archived Decisions

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

### Source URL Replacement Strategy (2025-07-15)
**Author:** Frodo (iOS Dev)  
**Status:** Implemented

Link audit identified 46 broken source URLs across saints-en.json and saints-es.json. Five primary sources had widespread link rot (Loyola Press, Hallow, Ascension Press, Focus, Lifeteen). Replaced with verified alternatives:

1. **Franciscan Media** — Primary replacement (stable saint-of-the-day archive)
2. **CNA (Catholic News Agency)** — Secondary source for comprehensive coverage
3. **EWTN** — Tertiary source to avoid duplicate keys
4. **Hallow (updated paths)** — Migrated from `/blog/` to `/saints/`

**Impact:**
- All 32 saints retain ≥2 working source URLs
- No working URLs changed
- Both EN/ES files updated identically (matched by URL, not saint name)

---

### Diacritic-Insensitive Search Convention (2025-07-17)
**Author:** Frodo (iOS Dev)  
**Status:** Implemented

All string matching in search/filter logic uses diacritic-insensitive comparison via shared `String+Diacritics.swift` extension (`containsIgnoringDiacritics` / `equalsIgnoringDiacritics`), not `.lowercased().contains()`.

**Rationale:** Saint names include accented characters (Thérèse, José, María) common in French, Spanish, and other languages. Users typing on English keyboards won't include accents, so search must treat accented and unaccented characters as equivalent.

**Impact:**
- **Legolas (QA):** Search tests verify accent-insensitive matching
- **Samwise (Data):** Saint data retains proper accented names
- **Android:** Use Java/Kotlin `Normalizer` or `Collator` with `SECONDARY` strength

---
