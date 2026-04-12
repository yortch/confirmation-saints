# Decision: Programmatic App Icon with Chi-Rho Design

**Date:** 2025-07-15
**Author:** Samwise (Data/Backend)
**Status:** Implemented

## Context
The app needed an icon. No AI image generation tools were available, so we generated one programmatically with Python + Pillow.

## Decision
- Used a **Chi-Rho (☧)** symbol as the central motif — the oldest Christogram, universally recognized in Catholic tradition and strongly associated with Confirmation
- **Purple gradient** background (liturgical color of Confirmation) with **gold accents** (sacred/regal)
- Subtle **dove silhouette** behind the symbol (Holy Spirit, the sacrament's core)
- No text on the icon (doesn't read well at small sizes)
- Single 1024x1024 PNG — Xcode auto-generates all required sizes

## Trade-offs
- Programmatic generation means geometric/flat style only — no organic or painterly textures
- The Chi-Rho may be less immediately recognizable to teens than a simple cross, but it's more distinctive and unique as an app icon
- This is a **placeholder** — Jorge may want to commission a professional icon later

## Impact
- iOS icon is now set; builds will show the new icon
- Android icon generation not yet done (different format requirements)
- The `_generate_icon.py` script can be re-run or modified if design tweaks are needed
