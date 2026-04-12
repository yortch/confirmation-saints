# Decision: Saint Image Sources from Wikimedia Commons

**Date:** 2026-07-17
**Agent:** Samwise (Data/Backend)
**Status:** Implemented

## Decision
All 32 saint images are sourced from Wikimedia Commons, using public domain or Creative Commons licensed artwork. Images are downloaded at 400px width thumbnails to keep bundle size reasonable (~2.8MB total for all 32 images).

## Rationale
- Wikimedia Commons provides reliable, well-cataloged public domain images
- The API allows programmatic access with thumb resizing, so we don't store oversized files
- A reproducible script (`_download_saint_images.py`) means images can be re-downloaded or updated if needed
- Attribution is standardized to "Public domain, via Wikimedia Commons" across all saints

## Impact
- **Bundle size:** ~2.8MB added for 32 JPG images
- **iOS/Android:** Images are in `SharedContent/images/` (cross-platform ready)
- **UI:** `SaintImageView.swift` already handles loading these images from the bundle — no UI changes needed

## Notes
- Some saints (Carlo Acutis, Chiara Luce Badano) have limited public domain imagery; we used the best available option
- The download script uses a polite User-Agent and 0.5s delay between requests per Wikimedia policy
