### Source Names Standardized to English in Both Language Files
**Author:** Samwise (Data/Backend)
**Status:** Implemented
**Date:** 2025-07-17

The Spanish `saints-es.json` had some source names translated (e.g., "Enciclopedia Catolica"). These were standardized to English ("Catholic Encyclopedia") to match the EN file. This is consistent with the existing convention that matching/reference fields use English values in both language files, while only display text (name, biography, etc.) is localized.

The new `sourceURLs` dictionary is keyed by source name and is identical across both language files. This simplifies app logic - no need for per-language URL mapping.

**Impact:** iOS and future Android code can treat `sources` and `sourceURLs` as language-independent fields.
