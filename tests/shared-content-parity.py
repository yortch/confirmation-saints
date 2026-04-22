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
     SharedContent/images/<id>.jpg.
  5. categories-en.json vs categories-es.json: same group ids, same value
     ids inside each group.

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

        # 4. Image file must exist for every saint (one image per id).
        image = en_s.get("image") or {}
        filename = image.get("filename") or f"{sid}.jpg"
        img_path = images_dir / filename
        if not img_path.exists():
            errors.append(f"[{sid}] missing image file: {img_path}")


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
