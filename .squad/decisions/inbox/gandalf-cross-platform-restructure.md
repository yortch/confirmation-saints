# Decision: Cross-Platform Repository Restructure

**Date:** 2026-07-15
**Author:** Gandalf (Lead)
**Status:** Implemented

## Context

The repo had iOS code (CatholicSaints/, CatholicSaints.xcodeproj/, project.yml) at the root alongside SharedContent/. Jorge wants to add an Android app to the same repository. The flat structure made it unclear what was iOS-specific vs shared.

## Decision

Reorganize the repository into a cross-platform layout:

```
ios/                  # All iOS-specific code and project files
SharedContent/        # Cross-platform data (stays at repo root)
android/              # Future Android app placeholder
```

### Key choices:
1. **SharedContent/ stays at root** — It's the shared layer, not owned by either platform
2. **iOS moves under ios/** — Clean separation, future Android gets android/
3. **git mv for all moves** — Preserves file history
4. **project.yml references ../SharedContent/** — XcodeGen picks up shared content via relative path
5. **SaintDataService unchanged** — Bundle paths still reference "SharedContent/saints" etc. since it's a folder reference in the bundle
6. **SharedContent/Data/ removed** — Superseded by Samwise's richer per-language data in saints/, categories/, content/

## Impact

- All team members must use `ios/` prefix for iOS file paths going forward
- Build commands now require `cd ios` first
- SharedContent/ paths unchanged for Samwise's data work
