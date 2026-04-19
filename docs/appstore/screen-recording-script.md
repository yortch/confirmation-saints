# App Review Screen Recording — Shot List & Script

**Purpose:** Satisfy item #1 of the App Store Review Guideline 2.1 information request.

**Apple's requirements:**
- Captured on a **physical device** (not simulator)
- Must **begin with launching the app** from the home screen
- Must show the typical user flow through the app's core features
- Must include any account / purchase / UGC / permission flows — **none apply to this app**, so none are shown

**Target length:** 60–90 seconds (ideal). Max 3 minutes. Apple prefers concise.
**Format:** MP4 or MOV. Portrait orientation. Upload directly in App Store Connect's "App Review Information → Attachments" area with your message response.

---

## Before You Record

1. **Use a physical iPhone.** Simulator recordings will be rejected.
2. **Install the current build** (1.0.0) from TestFlight or a local Xcode run.
3. **Set device language to English** (recording in English is fine — Apple reviewers speak English).
4. **Reset onboarding** so the Welcome screen appears: open Settings (inside the app) → "Reset Welcome Screen" if available, OR delete and reinstall the app.
5. **Enable Do Not Disturb** so no notifications pop up mid-recording.
6. **Set brightness to ~70%** and silence the device.
7. **Start iOS Screen Recording** (Control Center → record button). Do NOT enable microphone — you'll add captions, not voice.
8. Return to the **home screen** before tapping the app icon (this is mandatory — Apple requires the recording to start with launching the app).

---

## Shot List (target ~75 seconds)

Times are cumulative. Tap gently and pause 1–2 seconds between taps so Apple's reviewer can follow along.

### 1. Launch (0:00 – 0:05) — REQUIRED

- Home screen visible, Confirmation Saints icon visible.
- Tap the **Confirmation Saints** icon.
- Splash / app launches.

### 2. Welcome Screen (0:05 – 0:12)

- Welcome screen appears ("Welcome to Confirmation Saints").
- Pause 2 seconds so the reviewer can read the intro text.
- Tap **"Get Started"**.

### 3. Saints List — main feature (0:12 – 0:25)

- App lands on the **Saints** tab (or About — whichever is your default landing tab).
- If not already on Saints, tap the **Saints** tab at the bottom.
- Scroll the list slowly once (one finger-flick down, then back up). Shows breadth of content.
- Show the search bar. Optionally tap into search and type "Joan" → show St. Joan of Arc appears → clear search.

### 4. Saint Detail (0:25 – 0:40)

- Tap a well-known saint — **St. Joan of Arc** or **St. Francis of Assisi** — something universally recognizable.
- Detail view loads: image, name, feast day, patronages, biography, quote.
- Scroll down once to show the full biography and source citation.
- Tap the **back** button to return to the list.

### 5. Explore / Filter by Category (0:40 – 0:55)

- Tap the **Explore** tab.
- Show the categories (Interests, Regions, Eras, Age, Life State, Gender).
- Tap **Interests**.
- Tap a sub-category like **Music** or **Sports**.
- Filtered list of saints appears. Pause 1–2 seconds.
- Tap **back** to return.

### 6. Bilingual Toggle (0:55 – 1:10)

- Tap the **Settings** tab.
- Tap the **Language** picker.
- Select **Español**.
- UI strings switch to Spanish immediately.
- Tap the **Saints** tab — saint names and biographies are now in Spanish.
- Tap back into **Settings** → switch back to **English** (optional, shows it's reversible).

### 7. About (1:10 – 1:20)

- Tap the **About** tab.
- Show the "What is Confirmation?" content.
- Scroll once to show there's meaningful educational content.

### 8. Close (1:20 – 1:25)

- Swipe up / press home to return to the home screen.
- Stop recording.

---

## Captions / On-Screen Text (optional but recommended)

Apple's reviewer may not know the app. Add brief text overlays in a video editor (iMovie, CapCut, QuickTime) at each stage:

| Time | Caption |
|------|---------|
| 0:00 | "Launching Confirmation Saints" |
| 0:05 | "Welcome screen (first launch only)" |
| 0:12 | "Core feature: browse 70+ curated Catholic saints" |
| 0:25 | "Tap any saint for full bio, patronages, and sources" |
| 0:40 | "Explore: filter saints by interest, region, era, and more" |
| 0:55 | "Fully bilingual — English and Spanish" |
| 1:10 | "About: learn what Confirmation is" |

Keep each caption on screen for ~3 seconds. White text with a subtle drop shadow or dark bar works best.

---

## Things NOT to Record (and why)

Apple asked whether these flows exist. They do not. Do **not** fabricate or show:

- ❌ Account registration / login / deletion — the app has no accounts.
- ❌ In-app purchases / paywalls / subscriptions — the app is 100% free.
- ❌ User-generated content, posting, comments — all content is curated and bundled.
- ❌ Permission prompts (location, camera, contacts, mic, photos, notifications) — the app requests none.

The absence of these flows is already declared in text in `review-response.md` item #1. The video just needs to show the core browse experience.

---

## Quality Checklist (before uploading)

- [ ] Recorded on a physical iPhone (not simulator).
- [ ] Starts with the device home screen and the app launch.
- [ ] Shows Saints list, Saint detail, Explore filter, Settings language toggle, About.
- [ ] No notifications or banners visible.
- [ ] No personal info (other apps' icons are fine; messages/emails in banners are not).
- [ ] Length under 3 minutes (60–90s is ideal).
- [ ] Exported at device resolution (no scaling).
- [ ] Final file is MP4 or MOV, under ~500 MB.

---

## How to Submit

1. Go to App Store Connect → your app → the rejected submission.
2. In **App Review Information**, open the reviewer message thread from the rejection.
3. Paste the body of `review-response.md` as your reply.
4. Attach the MP4/MOV recording using the paperclip/attachment control.
5. Submit.

Apple usually re-reviews within 24–48 hours after a complete response.
