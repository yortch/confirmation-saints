# Android Adaptive Icons + Splash Screen

## Confidence: verified (corrected)

## Summary
Implement Android adaptive launcher icons with proper safe-zone padding to survive circle/squircle/teardrop launcher masks, and separate full-bleed splash screen icons to avoid double-padding cropping.

## The Problem
Android adaptive icons (API 26+) apply various mask shapes (circle, squircle, rounded square, teardrop) to the launcher icon foreground. If the visible content fills the entire 108dp canvas, it will be cropped by these masks. Additionally, reusing the adaptive foreground for the splash screen causes double-padding, making the logo appear too small.

## The 66dp-of-108dp Safe Zone Rule
**Key constraint:** Material adaptive icons use a 108×108dp canvas, but only the inner 66×66dp circle (centered) is guaranteed visible across all launcher shapes.

- Canvas: 108×108dp
- Safe zone: 66×66dp (center circle)
- Safe zone percentage: ~61% of canvas **linearly**, BUT...
- **Critical:** The correct scale depends on your **content shape**

### Pick Scale Based on Content Shape, Not Worst Case

**Rule:** Always measure your ACTUAL icon content shape before choosing a scale.

#### For SQUARE Content (fills corners)
⚠️ **Common mistake:** Using 60% scale because 66dp ÷ 108dp ≈ 61%. This ignores geometry!

For a **square** icon to fit within a **circular** safe zone:
- Safe circle diameter: 66dp
- Max square diagonal: 66dp
- Max square side: 66dp ÷ √2 ≈ **46.7dp** ≈ **43.2% of canvas**
- Required margins: ~31dp on each side (28.5% of canvas)

**The 60% mistake:**
- At 60% scale, content is 64.8dp × 64.8dp
- Diagonal: 91.6dp (calculated via Pythagorean theorem)
- Overshoot: 91.6dp - 66dp = **25.6dp beyond safe zone** ❌
- Result: visible cropping on circular launchers (e.g., Pixel)

**The 43% fix:**
- At 43% scale, content is 46.4dp × 46.4dp
- Diagonal: 65.7dp
- Clearance: 66dp - 65.7dp = **0.3dp inside safe zone** ✅
- Result: no cropping on any launcher shape

#### For CIRCULAR Content (transparent corners)
✅ **Optimal:** If your icon's visible content is effectively circular (e.g., logo inside a disc, transparent corners), you can safely scale up to **~61%**.

**Why 61% is safe for circular content:**
- Content fits within a circle of diameter D < source canvas diameter
- At 61% scale, content fills ~59-60dp of the 66dp safe circle
- Safe margin: ~6-7dp clearance
- Result: **optimal visibility** without clipping

**How to detect circular content:**
```python
from PIL import Image

img = Image.open("source-icon.png").convert("RGBA")
pixels = img.load()

# Sample corners for transparency (adjust threshold for your design)
corners = [(0, 0), (w-1, 0), (0, h-1), (w-1, h-1)]
transparent_corners = sum(1 for x, y in corners if pixels[x, y][3] < 10)

if transparent_corners >= 3:
    print("✅ Circular content: safe to use ~61% scale")
else:
    print("⚠️ Square content: must use 43% scale")
```

**Content shape examples:**
- **Circular (61% scale):** Logo inside a circle/disc with transparent corners (Confirmation Saints: dove + flame)
- **Square (43% scale):** Logo fills entire canvas edge-to-edge, content in corners

**Scale recommendations:**
- **Circular content:** 61% (optimal visibility)
- **Square content:** 43% (necessary for diagonal clearance)
- **Uncertain:** Use 43% (conservative, always safe)

## Implementation Pattern

### 1. Adaptive Launcher Icon Structure
```xml
<!-- android/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml -->
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background" />
    <foreground android:drawable="@mipmap/ic_launcher_foreground" />
</adaptive-icon>
```

### 2. Foreground Generation (Python + Pillow)
```python
# IMPORTANT: Choose scale based on actual content shape analysis
# - Circular content (transparent corners): 0.61 scale
# - Square content (fills corners): 0.43 scale

FOREGROUND_INNER_RATIO = 0.61  # Or 0.43 — see content shape detection above

def adaptive_foreground(src: Image.Image, px: int) -> Image.Image:
    """Transparent 108dp canvas with the source icon centered at safe-zone scale."""
    canvas = Image.new("RGBA", (px, px), (0, 0, 0, 0))
    inner = int(round(px * FOREGROUND_INNER_RATIO))
    resized = src.resize((inner, inner), Image.LANCZOS)
    offset = ((px - inner) // 2, (px - inner) // 2)
    canvas.paste(resized, offset, resized)
    return canvas

# Generate for each density
DENSITIES = {
    "mdpi": 1.0,   # 108px at mdpi
    "hdpi": 1.5,   # 162px at hdpi
    "xhdpi": 2.0,  # 216px at xhdpi
    "xxhdpi": 3.0, # 324px at xxhdpi
    "xxxhdpi": 4.0,# 432px at xxxhdpi
}

for density, scale in DENSITIES.items():
    px = int(108 * scale)
    out_path = f"mipmap-{density}/ic_launcher_foreground.png"
    adaptive_foreground(src, px).save(out_path)
```

### 3. Splash Screen Icon (Separate, Full-Bleed)
**Critical:** The splash screen must use a DIFFERENT drawable than the adaptive foreground.

```xml
<!-- android/app/src/main/res/values/themes.xml -->
<style name="Theme.App.Splash" parent="Theme.SplashScreen">
    <item name="windowSplashScreenBackground">@color/splash_background</item>
    <item name="windowSplashScreenAnimatedIcon">@mipmap/ic_splash</item>
    <item name="postSplashScreenTheme">@style/Theme.App</item>
</style>
```

Generate splash icons at 288dp (standard splash icon size), full-bleed with NO padding:
```python
SPLASH_DP = 288

for density, scale in DENSITIES.items():
    splash_px = int(SPLASH_DP * scale)
    resized = src.resize((splash_px, splash_px), Image.LANCZOS)
    resized.save(f"mipmap-{density}/ic_splash.png")
```

### 4. Legacy Icons (API <26)
Still needed for older devices:
```python
LEGACY_DP = 48

for density, scale in DENSITIES.items():
    legacy_px = int(LEGACY_DP * scale)
    # Square
    src.resize((legacy_px, legacy_px), Image.LANCZOS).save(
        f"mipmap-{density}/ic_launcher.png"
    )
    # Round (with circular mask)
    # ... apply circular mask, then save as ic_launcher_round.png
```

## Verification Checklist

### Verify Safe Zone (mdpi example)
```python
from PIL import Image
import math

fg = Image.open('mipmap-mdpi/ic_launcher_foreground.png')
width, height = fg.size
bbox = fg.getbbox()  # Find non-transparent content

if bbox:
    min_x, min_y, max_x, max_y = bbox
    content_width = max_x - min_x
    content_height = max_y - min_y
    
    # Calculate diagonal (for square content)
    content_diagonal_px = content_width * math.sqrt(2)
    content_diagonal_dp = content_diagonal_px  # mdpi scale = 1.0
    
    safe_circle_dp = 66
    
    if content_diagonal_dp <= safe_circle_dp:
        clearance = safe_circle_dp - content_diagonal_dp
        print(f"✅ FITS! Content diagonal {content_diagonal_dp:.1f}dp < {safe_circle_dp}dp")
        print(f"   Clearance: {clearance:.1f}dp")
    else:
        overshoot = content_diagonal_dp - safe_circle_dp
        print(f"❌ EXCEEDS! Content diagonal {content_diagonal_dp:.1f}dp > {safe_circle_dp}dp")
        print(f"   Overshoot: {overshoot:.1f}dp — WILL BE CROPPED")
```

### Build Verification
```bash
cd android && ./gradlew :app:assembleDebug
```
Must complete successfully with no resource errors.

### Visual Verification
1. **Launcher icon**: Install on device/emulator, check home screen on different launchers (Pixel, Samsung, etc.)
2. **Splash screen**: Launch app — logo should appear complete (not cropped) during splash

## Common Mistakes

### ❌ Mistake 0: Using worst-case scale for all content shapes
**THE MOST CRITICAL MISTAKE**
```python
# DON'T DO THIS (blindly)
FOREGROUND_INNER_RATIO = 0.60  # Assumes circular content, but may be square!
FOREGROUND_INNER_RATIO = 0.43  # Assumes square content, but may be circular!
```
**Problem:** Using 60% for square content causes cropping (diagonal exceeds 66dp safe circle). Using 43% for circular content wastes visible area (logo appears unnecessarily small).

**Math:**
- **Square content:** Max side = 66dp ÷ √2 ≈ 46.7dp ≈ **43% of 108dp canvas**
- **Circular content:** Max diameter = 66dp ≈ **61% of 108dp canvas**

**Fix:** **Always measure your actual content shape** (see "Pick Scale Based on Content Shape" above), then choose:
- **61% scale** for circular content (transparent corners)
- **43% scale** for square content (fills corners)

### ❌ Mistake 1: Using adaptive foreground for splash
```xml
<!-- DON'T DO THIS -->
<item name="windowSplashScreenAnimatedIcon">@mipmap/ic_launcher_foreground</item>
```
**Problem:** Double padding — adaptive foreground already has 21dp margins, splash system adds its own sizing constraints.

**Fix:** Use dedicated full-bleed splash icon.

### ❌ Mistake 2: Content fills entire 108dp canvas
```python
# DON'T DO THIS
canvas.paste(resized, (0, 0))  # No margins!
```
**Problem:** Content gets cropped by circular/squircle launcher masks.

**Fix:** Scale content to 43% and center it.

### ❌ Mistake 3: Inconsistent densities
**Problem:** Generating only mdpi/xhdpi, missing hdpi/xxhdpi/xxxhdpi.

**Fix:** Generate all five density buckets.

## Files Generated
For Confirmation Saints project:
```
android/app/src/main/res/
├── mipmap-anydpi-v26/
│   ├── ic_launcher.xml          # Adaptive icon config
│   └── ic_launcher_round.xml    # Round adaptive config
├── mipmap-mdpi/
│   ├── ic_launcher.png          # 48×48px legacy
│   ├── ic_launcher_round.png    # 48×48px legacy round
│   ├── ic_launcher_foreground.png  # 108×108px (60% content)
│   └── ic_splash.png            # 288×288px full-bleed
├── mipmap-hdpi/
│   └── ... (same files, 1.5x scale)
├── mipmap-xhdpi/
│   └── ... (2x scale)
├── mipmap-xxhdpi/
│   └── ... (3x scale)
└── mipmap-xxxhdpi/
    └── ... (4x scale)
```

## When to Use This Skill
- Setting up Android app icons for the first time
- Regenerating icons after design changes
- Debugging "icon appears cropped" issues on launcher or splash screen
- Porting iOS app icon to Android (requires adaptive treatment)

## Related Files
- `_generate_android_icon.py` — Icon generator script
- `android/app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml` — Adaptive icon config
- `android/app/src/main/res/values/themes.xml` — Splash screen theme
- `android/app/src/main/res/values/colors.xml` — Background colors

## References
- [Android Adaptive Icons Guide](https://developer.android.com/develop/ui/views/launch/icon_design_adaptive)
- [Splash Screen API](https://developer.android.com/develop/ui/views/launch/splash-screen)
- Material Design: Icon keyline shapes show the 66dp safe zone requirement
