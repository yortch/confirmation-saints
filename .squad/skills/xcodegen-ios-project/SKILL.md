---
name: "xcodegen-ios-project"
description: "Create and manage iOS projects using XcodeGen instead of manual .pbxproj editing"
domain: "ios-project-setup"
confidence: "high"
source: "earned"
---

## Context
When creating or restructuring an iOS/SwiftUI project, use XcodeGen to generate the `.xcodeproj` from a declarative `project.yml` spec. This avoids merge conflicts and makes project structure version-controllable.

## Patterns
- Define `project.yml` at the repo root
- Run `xcodegen generate` to create/update the .xcodeproj
- Add `.xcodeproj` to `.gitignore` if desired (or commit it for CI convenience)
- Use `sources` with `excludes` to control what enters the build
- Use `type: folder` for folder references (like SharedContent)
- Set `GENERATE_INFOPLIST_FILE: true` to auto-generate Info.plist

## Examples
```yaml
targets:
  MyApp:
    type: application
    platform: iOS
    sources:
      - path: MyApp
        excludes:
          - "Resources/Preview Content"
      - path: SharedContent
        buildPhase: resources
        type: folder
    resources:
      - path: MyApp/Resources/Assets.xcassets
      - path: MyApp/Resources/Localizable.xcstrings
```

## Anti-Patterns
- Never edit `.pbxproj` manually when using XcodeGen
- Don't use `swift package init` for iOS apps (it creates library targets)
- Don't hardcode simulator names in build commands (they change across Xcode versions)
