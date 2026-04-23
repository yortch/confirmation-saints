# Skill: cross-platform-asset-sync-via-gradle

> Pattern for bridging a canonical cross-platform content directory (at a repo
> root) into an Android app's bundled assets at build time, without forking or
> committing generated files.

## When to use

You have a monorepo where multiple platforms (iOS, Android, web, …) share a
canonical content directory — JSON, images, videos — and you need Android to
consume it without:

- Duplicating files into `android/app/src/main/assets/`
- Letting generated files creep into version control
- Requiring contributors to run a manual "regenerate" step

This is a common pattern when iOS consumes the directory via a folder reference
(Xcode) or symlink, and Android needs a native equivalent.

## The pattern

1. **Declare a `Sync` task** in `app/build.gradle.kts` with explicit
   `include(...)` filters — whitelist what ships in the APK rather than
   bundling the whole directory.
2. **Wire it as a `preBuild` dependency** so every IDE sync and CLI build runs
   it automatically.
3. **Destination = `src/main/assets/`**, which Android's `AssetManager` reads
   at runtime.
4. **Gitignore the destination** except for a single `assets/README.md`
   explaining it's build-generated. This keeps the directory in git (so the
   source set resolves) but its contents out.
5. **Use `preserve { include("README.md") }`** so the Sync doesn't nuke the
   explainer file.

## Minimal reference implementation

```kotlin
// android/app/build.gradle.kts
val sharedContentDir = rootProject.layout.projectDirectory.dir("../SharedContent")
val generatedAssetsDir = layout.projectDirectory.dir("src/main/assets")

val syncSharedContent by tasks.registering(Sync::class) {
    group = "my-project"
    description = "Copies SharedContent/ into app assets/ for bundling."

    from(sharedContentDir.dir("saints")) { include("*.json") }
    from(sharedContentDir.dir("images"))  { include("*.jpg"); into("images") }

    into(generatedAssetsDir)
    preserve { include("README.md") }

    doFirst { logger.lifecycle("sync: ${sharedContentDir.asFile} -> ${generatedAssetsDir.asFile}") }
}

tasks.named("preBuild").configure { dependsOn(syncSharedContent) }
```

```gitignore
# .gitignore
android/app/src/main/assets/*.json
android/app/src/main/assets/images/
```

## Why `Sync`, not alternatives?

| Alternative                               | Problem                                                        |
| ----------------------------------------- | -------------------------------------------------------------- |
| `Copy` task                               | Leaves stale files when sources are deleted                    |
| `sourceSets["main"].assets.srcDir(...)`   | Bundles everything in the dir; no whitelist control            |
| Manual `cp` script in a `preBuild` hook   | Not incremental; breaks Gradle's up-to-date caching            |
| Symlinks                                  | Work on macOS/Linux dev boxes, break on Windows and CI runners |

`Sync` gives you mirroring, incrementality, up-to-date checks, and a
declarative include/exclude filter — all for free.

## Gotchas

- **Symlinks in `SharedContent/`** — by default `Sync` follows them. If your
  canonical directory has symlinks pointing outside the repo (unusual),
  disable follow-symlinks explicitly.
- **Image MIME types** — if your shared images include formats Android doesn't
  natively decode at all sizes (e.g. AVIF on older APIs), transcode upstream.
- **Build caching** — task inputs are the `from(...)` directories; Gradle's
  up-to-date check works out of the box.
- **Running just the task**: `./gradlew :app:syncSharedContent` for debugging.
- **Case sensitivity**: macOS filesystems are case-insensitive by default;
  Linux CI is not. Keep filename casing consistent across platforms.

## Originating project

confirmation-saints (iOS + Android cross-platform app). iOS uses an Xcode
folder reference + `ios/SharedContent -> ../SharedContent` symlink; Android
uses this task. See
`.squad/decisions/inbox/aragorn-sharedcontent-gradle-sync-task.md` for the
full decision record.
