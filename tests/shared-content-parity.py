#!/usr/bin/env python3
"""
Cross-platform parity guardrail for SharedContent/.

Checks that SharedContent/saints/saints-{en,es}.json and
SharedContent/categories/categories-{en,es}.json stay in lockstep,
per the "SharedContent/ is the Canonical Cross-Platform Data Source"
decision (2026-04-21).

Enforced invariants:
  1. Same set of saint ids in both language files.
  2. For every saint id, the set of source URLs (values of sourceURLs)
     is identical across EN/ES. URLs are the canonical shared key.
  3. For every saint id, these English-canonical fields match byte-for-byte
     across EN/ES:
        patronOf, affinities, tags, region, lifeState, ageCategory, gender
     (Display localization lives in optional display* arrays and other
     freely-translated fields — those are NOT checked here.)
  4. Every saint has a corresponding image file at
     SharedContent/images/<id>.jpg, except ids listed in
     KNOWN_MISSING_IMAGE_IDS (documented gaps where no free/CC-licensed
     portrait could be found), which must have no `image` field in
     either language file.
  5. categories-en.json vs categories-es.json: same group ids, same value
     ids inside each group.
  6. Every saint's `image.attribution` string must actually be translated
     between EN/ES, unless it is a documented language-agnostic license/
     boilerplate phrase (see ALLOWED_IDENTICAL_ATTRIBUTIONS below) — proper
     names, source titles, license names, and URLs are expected to stay
     identical, but full descriptive attribution sentences are not. This
     guards against a repeat of the St. Maria Troncatti incident, where the
     Spanish `image.attribution` shipped as an exact, untranslated copy of
     the English text (see .squad/decisions.md, 2026-09-20).

Exits non-zero and prints a clear diff on any failure.
Exits 0 on success.

Usage:
    python3 tests/shared-content-parity.py
    python3 tests/shared-content-parity.py --shared-content /path/to/SharedContent
"""

from __future__ import annotations

import argparse
import json
import sys
from pathlib import Path
from typing import Any

CANONICAL_LIST_FIELDS = ("patronOf", "affinities", "tags")
CANONICAL_SCALAR_FIELDS = ("region", "lifeState", "ageCategory", "gender")

# Documented image gaps: no free/CC-licensed portrait could be verified on
# Wikimedia Commons for these saints as of this writing (both canonized
# Oct. 19, 2025). They are intentionally recorded without an `image` field
# rather than using a non-free/fair-use image. Mirrors the equivalent
# allowlist in android/.../SaintRepositoryTest.kt
# (should_expose_image_filename_matching_saint_id) — keep both in sync.
#
# maria-troncatti now ships with the official FMA (Salesian Sisters)
# canonization logo (used unaltered per FMA's usage authorization), with an
# explicit `image.filename` of "maria-troncatti.jpg" — no longer a gap.
KNOWN_MISSING_IMAGE_IDS = frozenset({"peter-to-rot"})

# `image.attribution` is free-form, user-visible prose and — per the
# 2026-09-20 "Require bilingual attribution parity" policy — must be
# translated in both EN and ES. It is legitimate for the EN and ES strings
# to be byte-identical only when the text is a short, standardized,
# language-agnostic license/boilerplate phrase (the kind that is
# conventionally left untranslated even in fully localized products, much
# like a license SPDX identifier or a proper name). Anything else that is
# identical across locales is presumed to be an untranslated copy-paste and
# must fail this check.
#
# Add a new phrase here only when it is genuinely language-agnostic
# boilerplate (e.g. another standard public-domain/CC license credit line),
# not as a way to silence a real translation gap.
ALLOWED_IDENTICAL_ATTRIBUTIONS = frozenset(
    {
        "Public domain, via Wikimedia Commons",
    }
)

# Explicit regression guard: St. Maria Troncatti's Spanish `image.attribution`
# was shipped as an exact, untranslated copy of the English sentence below.
# This is redundant with the general ALLOWED_IDENTICAL_ATTRIBUTIONS check
# above (that check alone would already catch this), but is kept as a named,
# self-documenting sentinel for the specific incident that prompted this
# guardrail.
KNOWN_UNTRANSLATED_ATTRIBUTION_INCIDENTS = {
    "maria-troncatti": (
        "Official FMA (Salesian Sisters) canonization logo, face "
        "reproduction by Eng. Carlos David Pacurucu Regalado; used "
        "unaltered per FMA authorization, via cgfmanet.org"
    ),
}


def load_json(path: Path) -> dict[str, Any]:
    with path.open("r", encoding="utf-8") as f:
        return json.load(f)


def check_saints(shared: Path, errors: list[str]) -> None:
    en_path = shared / "saints" / "saints-en.json"
    es_path = shared / "saints" / "saints-es.json"
    images_dir = shared / "images"

    if not en_path.exists() or not es_path.exists():
        errors.append(f"Missing saints file(s): {en_path} / {es_path}")
        return

    en = {s["id"]: s for s in load_json(en_path)["saints"]}
    es = {s["id"]: s for s in load_json(es_path)["saints"]}

    en_ids = set(en)
    es_ids = set(es)

    if en_ids != es_ids:
        only_en = sorted(en_ids - es_ids)
        only_es = sorted(es_ids - en_ids)
        if only_en:
            errors.append(f"Saint ids only in EN (missing from ES): {only_en}")
        if only_es:
            errors.append(f"Saint ids only in ES (missing from EN): {only_es}")

    shared_ids = sorted(en_ids & es_ids)
    for sid in shared_ids:
        en_s = en[sid]
        es_s = es[sid]

        # 2. sourceURLs value set must match.
        en_urls = set((en_s.get("sourceURLs") or {}).values())
        es_urls = set((es_s.get("sourceURLs") or {}).values())
        if en_urls != es_urls:
            only_en = sorted(en_urls - es_urls)
            only_es = sorted(es_urls - en_urls)
            errors.append(
                f"[{sid}] sourceURLs drift\n"
                f"  only in EN: {only_en}\n"
                f"  only in ES: {only_es}"
            )

        # 3. Canonical identifier fields must match exactly (English values).
        for field in CANONICAL_LIST_FIELDS:
            en_v = en_s.get(field, [])
            es_v = es_s.get(field, [])
            if en_v != es_v:
                errors.append(
                    f"[{sid}] canonical list field '{field}' differs\n"
                    f"  EN: {en_v}\n"
                    f"  ES: {es_v}"
                )
        for field in CANONICAL_SCALAR_FIELDS:
            en_v = en_s.get(field)
            es_v = es_s.get(field)
            if en_v != es_v:
                errors.append(
                    f"[{sid}] canonical scalar field '{field}' differs "
                    f"(EN={en_v!r}, ES={es_v!r})"
                )

        # 4. Image file must exist for every saint (one image per id),
        # except the documented gaps in KNOWN_MISSING_IMAGE_IDS.
        has_en_image = "image" in en_s
        has_es_image = "image" in es_s
        if sid in KNOWN_MISSING_IMAGE_IDS:
            if has_en_image or has_es_image:
                errors.append(
                    f"[{sid}] is listed in KNOWN_MISSING_IMAGE_IDS but now has "
                    f"an 'image' field (EN={has_en_image}, ES={has_es_image}); "
                    "remove it from the allowlist instead of leaving both."
                )
            continue
        if not has_en_image or not has_es_image:
            errors.append(
                f"[{sid}] missing 'image' field (EN={has_en_image}, ES={has_es_image}) "
                "and is not in KNOWN_MISSING_IMAGE_IDS"
            )
            continue
        image = en_s.get("image") or {}
        filename = image.get("filename") or f"{sid}.jpg"
        img_path = images_dir / filename
        if not img_path.exists():
            errors.append(f"[{sid}] missing image file: {img_path}")

        # 6. image.attribution must be translated between EN/ES, unless it
        # is a documented language-agnostic license/boilerplate phrase.
        en_attribution = image.get("attribution")
        es_attribution = (es_s.get("image") or {}).get("attribution")
        if (
            en_attribution
            and es_attribution
            and en_attribution == es_attribution
            and en_attribution not in ALLOWED_IDENTICAL_ATTRIBUTIONS
        ):
            errors.append(
                f"[{sid}] image.attribution appears untranslated: EN and ES "
                f"are byte-identical ({en_attribution!r}) and this phrase is "
                "not in ALLOWED_IDENTICAL_ATTRIBUTIONS. Either translate the "
                "ES attribution or, if this is genuinely language-agnostic "
                "license boilerplate, add it to ALLOWED_IDENTICAL_ATTRIBUTIONS."
            )

        known_bad = KNOWN_UNTRANSLATED_ATTRIBUTION_INCIDENTS.get(sid)
        if known_bad is not None and es_attribution == known_bad:
            errors.append(
                f"[{sid}] regression: image.attribution (ES) is byte-identical "
                f"to the known untranslated English text from the original "
                f"St. Maria Troncatti incident ({known_bad!r}). It must be "
                "translated into Spanish."
            )


def check_categories(shared: Path, errors: list[str]) -> None:
    en_path = shared / "categories" / "categories-en.json"
    es_path = shared / "categories" / "categories-es.json"

    if not en_path.exists() or not es_path.exists():
        errors.append(f"Missing categories file(s): {en_path} / {es_path}")
        return

    en = load_json(en_path)["categories"]
    es = load_json(es_path)["categories"]

    en_groups = {g["id"]: g for g in en}
    es_groups = {g["id"]: g for g in es}

    if set(en_groups) != set(es_groups):
        errors.append(
            "Category group ids differ\n"
            f"  only in EN: {sorted(set(en_groups) - set(es_groups))}\n"
            f"  only in ES: {sorted(set(es_groups) - set(en_groups))}"
        )

    for gid in sorted(set(en_groups) & set(es_groups)):
        en_val_ids = {v["id"] for v in en_groups[gid].get("values", [])}
        es_val_ids = {v["id"] for v in es_groups[gid].get("values", [])}
        if en_val_ids != es_val_ids:
            errors.append(
                f"[categories:{gid}] value ids differ\n"
                f"  only in EN: {sorted(en_val_ids - es_val_ids)}\n"
                f"  only in ES: {sorted(es_val_ids - en_val_ids)}"
            )


def main(argv: list[str]) -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--shared-content",
        default=None,
        help="Path to SharedContent/ (default: <repo-root>/SharedContent)",
    )
    args = parser.parse_args(argv)

    if args.shared_content:
        shared = Path(args.shared_content).resolve()
    else:
        shared = (Path(__file__).resolve().parent.parent / "SharedContent").resolve()

    if not shared.is_dir():
        print(f"ERROR: SharedContent not found at {shared}", file=sys.stderr)
        return 2

    print(f"Checking parity under {shared} ...")

    errors: list[str] = []
    check_saints(shared, errors)
    check_categories(shared, errors)

    if errors:
        print("\n❌ Parity check FAILED:\n", file=sys.stderr)
        for e in errors:
            print(f" - {e}", file=sys.stderr)
        print(f"\n{len(errors)} issue(s) found.", file=sys.stderr)
        return 1

    print("✅ Parity check passed.")
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
