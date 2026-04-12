# App Icon Generation

## Confidence: low

## Summary
Generate app icons programmatically using Python (Pillow) when AI image generation tools aren't available. Creates properly sized iOS and Android app icons with vector-style designs using shapes, gradients, and text.

## Pattern

### iOS App Icon Requirements
- Single 1024x1024 PNG (Xcode auto-generates all sizes from this)
- No transparency (iOS clips to rounded rect automatically)
- Place at: `{ios_project}/Resources/Assets.xcassets/AppIcon.appiconset/`
- Update `Contents.json` to reference the file

### Android App Icon Requirements
- Adaptive icon: foreground (108x108dp) + background (108x108dp)
- Legacy icon: 512x512 PNG
- Place at: `android/app/src/main/res/mipmap-*/`

### Programmatic Generation (Python + Pillow)
```python
from PIL import Image, ImageDraw, ImageFont
# 1. Create 1024x1024 canvas
# 2. Draw gradient background
# 3. Add shapes (cross, dove, halo, etc.) using draw primitives
# 4. Add text if desired
# 5. Save as PNG
```

### AI Image Generation Prompt Template
When an AI image tool IS available, use this prompt structure:
```
A modern app icon for a Catholic confirmation saint finder app called "Confirmation Saints".
Style: Flat design, minimal, modern. Purple and gold color scheme.
Elements: [describe key visual — e.g., stylized cross with golden halo, dove, or open book]
Requirements: Square format, no text, simple shapes that read well at small sizes.
No photographic elements. Vector/flat illustration style.
```

## When to Use
- User requests an app icon or logo
- New project setup needs branding
- Rebranding or icon refresh

## Related Files
- `ios/{project}/Resources/Assets.xcassets/AppIcon.appiconset/`
- `android/app/src/main/res/mipmap-*/`
