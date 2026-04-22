# Android Adaptive Icons + Splash Screen

## Confidence: high

## Summary
Implement Android adaptive launcher icons with proper safe-zone padding to survive circle/squircle/teardrop launcher masks, and separate full-bleed splash screen icons to avoid double-padding cropping.

## The Problem
Android adaptive icons (API 26+) apply various mask shapes (circle, squircle, rounded square, teardrop) to the launcher icon foreground. If the visible content fills the entire 108dp canvas, it will be cropped by these masks. Additionally, reusing the adaptive foreground for the splash screen causes double-padding, making the logo appear too small.

## The 66dp-of-108dp Safe Zone Rule
**Key constraint:** Material adaptive icons use a 108×108dp canvas, but only the inner 66×66dp circle (centered) is guaranteed visible across all launcher shapes.

- Canvas: 108×108dp
- Safe zone: 66×66dp (center circle)
- Safe zone percentage: ~61% of canvas
- **Recommended scale: 60%** to provide safe margins

### Why 60% scale?
- At 60% scale, content is ~65dp in a 108dp canvas
- Margins: (108 - 65) / 2 ≈ 21-22dp on all sides
- This exceeds the minimum safe zone requirement: (108 - 66) / 2 = 21dp
- Works across all launcher mask shapes

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
def adaptive_foreground(src: Image.Image, px: int) -> Image.Image:
    """Transparent 108dp canvas with the source icon centered at safe-zone scale."""
    canvas = Image.new("RGBA", (px, px), (0, 0, 0, 0))
    inner = int(round(px * 0.60))  # 60% scale for safe zone
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

fg = Image.open('mipmap-mdpi/ic_launcher_foreground.png')
bbox = fg.getbbox()  # Find non-transparent content
if bbox:
    min_x, min_y, max_x, max_y = bbox
    margins = (min_x, min_y, 107 - (max_x - 1), 107 - (max_y - 1))
    
    # All margins should be >= 21px at mdpi
    assert all(m >= 21 for m in margins), "Content exceeds safe zone!"
    print(f"✅ Margins: {margins} — Fits 66dp safe zone")
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

**Fix:** Scale content to 60% and center it.

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
