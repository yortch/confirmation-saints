# assets/ content is build-generated

Everything in this directory (except this README) is produced by the `syncSharedContent`
Gradle task, which copies files from the repo-root `SharedContent/` directory into the
Android app's asset bundle at build time.

**Do not edit files here directly.** Edit them in `SharedContent/` instead — they are the
canonical cross-platform source of truth shared with the iOS app.

Generated files:

- `saints-en.json`, `saints-es.json`
- `categories-en.json`, `categories-es.json`
- `images/*.jpg`

See `android/app/build.gradle.kts` (`syncSharedContent` task) for the exact wiring.
