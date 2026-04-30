---
name: "appstore-screenshots"
description: "Capture localized iOS App Store screenshots from real Simulator UI"
domain: "ios-release"
confidence: "medium"
source: "earned"
---

## Context
Use this when preparing App Store screenshots for Confirmation Saints. Screenshots should come from the real SwiftUI app running in Simulator, not renamed or mocked files.

## Pattern
1. Build the iOS simulator app with the existing Xcode project:
   ```bash
   cd ios && xcodebuild -project CatholicSaints.xcodeproj -scheme CatholicSaints -destination 'platform=iOS Simulator,id=<UDID>' CODE_SIGNING_ALLOWED=NO build -quiet
   ```
2. Install the built `.app` onto the target simulator.
3. Set app state before launch:
   ```bash
   xcrun simctl spawn <UDID> defaults write com.jorgebalderas.ConfirmationSaints appLanguage es
   xcrun simctl spawn <UDID> defaults write com.jorgebalderas.ConfirmationSaints hasSeenWelcome -bool true
   ```
4. Launch the app, wait for the splash overlay to clear, navigate using the Simulator UI, then capture with an absolute path:
   ```bash
   xcrun simctl io <UDID> screenshot /absolute/path/to/output.png
   ```
5. Verify dimensions with Pillow or `file`, and OCR/inspect enough text to confirm the language is correct.

## Device Targets Used
- iPhone 14 Plus simulator: 1284×2778 screenshots.
- iPad Pro 13-inch (M5) simulator: 2064×2752 screenshots, matching the existing iPad App Store target.

## Gotchas
- Use the app's in-app `appLanguage` setting; simulator locale alone does not update `@AppStorage("appLanguage")`.
- In this environment, `simctl io screenshot` failed for relative output paths but succeeded with absolute paths.
- Wait several seconds after launch so the splash screen does not cover the target UI.
