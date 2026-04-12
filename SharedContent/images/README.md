# Saint Images

This directory will contain images for all saints in the app.

## Image Requirements
- Format: JPG or PNG
- Recommended size: 600x800px (portrait orientation)
- All images must be public domain or properly licensed
- Each image's attribution is stored in the saint's JSON data

## Image Naming Convention
Images are named to match the saint's `id` field in the JSON files:
- `therese-of-lisieux.jpg`
- `carlo-acutis.jpg`
- `francis-of-assisi.jpg`

## Sources for Public Domain Saint Images
- Wikimedia Commons (Public Domain)
- The Metropolitan Museum of Art (Open Access)
- National Gallery of Art (Open Access)
- Catholic tradition artwork (pre-1928, public domain in US)

## Attribution
Each saint entry in `saints-en.json` / `saints-es.json` includes an `image` object with:
- `filename`: the image file name
- `attribution`: credit/license information

Always verify image licensing before adding to the project.
