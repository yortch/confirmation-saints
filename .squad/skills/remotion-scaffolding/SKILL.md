# Skill: Remotion Scaffolding

**When to use:** Spinning up a new Remotion (React + TypeScript) video project in a subdirectory of an existing repo, without running the interactive `create-video` wizard.

**Why this pattern:** `npx create-video@latest` is interactive and its non-interactive flags change between releases. Hand-scaffolding is deterministic, pins versions, and keeps the project minimal (no extra templates, eslint configs, etc.).

---

## Steps

### 1. Pick a location and confirm the latest Remotion version

```bash
mkdir -p <project>/video/src
npm --cache <project>/video/.npm-cache view remotion version   # e.g. 4.0.451
```

Using a **local npm cache** inside the project sidesteps broken-perms issues in `~/.npm/_cacache/` (a real-world hazard when a past sudo install left root-owned files behind). The cache dir is gitignored.

### 2. Create six files

**`video/package.json`** — pin ALL `@remotion/*` packages to the same version:

```json
{
  "name": "<app>-video",
  "private": true,
  "scripts": {
    "start": "remotion studio",
    "render": "remotion render <CompositionId> out/<CompositionId>.mp4 --codec=h264",
    "upgrade": "remotion upgrade"
  },
  "dependencies": {
    "@remotion/bundler": "4.0.451",
    "@remotion/cli": "4.0.451",
    "@remotion/google-fonts": "4.0.451",
    "@remotion/renderer": "4.0.451",
    "react": "19.0.0",
    "react-dom": "19.0.0",
    "remotion": "4.0.451"
  },
  "devDependencies": {
    "@types/react": "19.0.0",
    "typescript": "5.8.3"
  }
}
```

**`video/tsconfig.json`** — strict, bundler resolution, react-jsx, noEmit.

**`video/remotion.config.ts`** — set codec/pixelFormat/entry-point/overwriteOutput.

**`video/src/index.ts`:**
```ts
import { registerRoot } from "remotion";
import { RemotionRoot } from "./Root";
registerRoot(RemotionRoot);
```

**`video/src/Root.tsx`** — registers one `<Composition>` per output variant (square, vertical, horizontal, cutdowns). Keep them all in one project; don't spin separate projects.

**`video/src/<Composition>.tsx`** — the actual component. Start with a branded placeholder (gradient + title) so you can verify the render pipeline before building the real treatment.

### 3. Install + verify

```bash
cd video
npm --cache ./.npm-cache install --no-audit --no-fund
npx tsc --noEmit                                              # type-check
npx remotion render <CompositionId> out/preview.mp4 --codec=h264   # smoke render
```

A 30s 1080×1080 placeholder renders in ~30s on a modern laptop and produces a ~1–2 MB MP4. If that works, the scaffold is valid.

### 4. Gitignore

Add to the repo root `.gitignore`:

```
video/node_modules/
video/out/
video/.cache/
video/.remotion/
video/.npm-cache/
```

Also create a minimal `video/.gitignore` for local safety.

---

## Gotchas

- **Version lockstep.** Mixed `@remotion/*` versions break the bundler with cryptic errors. Always upgrade via `remotion upgrade`.
- **Pixel format.** Always set `yuv420p` in `remotion.config.ts` — it's the most compatible format for social platforms (LinkedIn, IG, X). Some players choke on `yuv444p` (Remotion's default for certain codecs).
- **Chrome download.** First render downloads a headless Chromium (~150MB). Budget for this on first run in CI or on a fresh machine.
- **Asset access.** To use files from outside `video/`, copy them into `video/public/` and load via `staticFile()`. Relative-path imports outside the project break the bundler.
- **npm cache permissions.** If `npm` errors with `EACCES` on `~/.npm/_cacache/`, use `npm --cache ./local-cache install` as a workaround and report the perms issue (don't `sudo` fix it without knowing why it's root-owned).

---

## Example: this repo

Scaffolded at `video/` on 2026-04-24 for the Confirmation Saints promo video.
- Composition: `ConfirmationSaintsPromo` (1080×1080, 30fps, 30s).
- Smoke-rendered in 30s → `video/out/preview.mp4` (1.6 MB).
- Full story: `.squad/decisions/inbox/galadriel-video-setup.md`.
