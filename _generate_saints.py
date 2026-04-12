import json
import copy

# Load existing data
with open('SharedContent/saints/saints-en.json') as f:
    en_data = json.load(f)
with open('SharedContent/saints/saints-es.json') as f:
    es_data = json.load(f)

existing_ids = {s['id'] for s in en_data['saints']}
print(f"Existing: {len(existing_ids)} saints")

# Add popularity to existing saints
popularity_map = {
    "therese-of-lisieux": {"allTime": 92, "trending": [{"year": 2026, "rank": 8}, {"year": 2025, "rank": 10}]},
    "dominic-savio": {"allTime": 60, "trending": []},
    "carlo-acutis": {"allTime": 78, "trending": [{"year": 2026, "rank": 1}, {"year": 2025, "rank": 1}]},
    "joan-of-arc": {"allTime": 88, "trending": [{"year": 2026, "rank": 15}]},
    "maria-goretti": {"allTime": 72, "trending": []},
    "chiara-luce-badano": {"allTime": 55, "trending": [{"year": 2026, "rank": 18}, {"year": 2025, "rank": 22}]},
    "jose-sanchez-del-rio": {"allTime": 58, "trending": [{"year": 2025, "rank": 25}]},
    "sebastian": {"allTime": 80, "trending": []},
    "cecilia": {"allTime": 82, "trending": []},
    "francis-of-assisi": {"allTime": 97, "trending": [{"year": 2026, "rank": 6}]},
    "thomas-aquinas": {"allTime": 85, "trending": []},
    "thomas-more": {"allTime": 70, "trending": []},
    "gianna-beretta-molla": {"allTime": 62, "trending": [{"year": 2025, "rank": 20}]},
    "louis-martin": {"allTime": 50, "trending": []},
    "zelie-martin": {"allTime": 50, "trending": []},
    "mother-teresa": {"allTime": 95, "trending": [{"year": 2026, "rank": 4}, {"year": 2025, "rank": 3}]},
    "john-paul-ii": {"allTime": 96, "trending": [{"year": 2026, "rank": 3}, {"year": 2025, "rank": 2}]},
    "pier-giorgio-frassati": {"allTime": 72, "trending": [{"year": 2026, "rank": 2}, {"year": 2025, "rank": 5}]},
    "kateri-tekakwitha": {"allTime": 65, "trending": [{"year": 2025, "rank": 18}]},
    "augustine-of-hippo": {"allTime": 90, "trending": []},
    "josephine-bakhita": {"allTime": 68, "trending": [{"year": 2025, "rank": 15}]},
    "juan-diego": {"allTime": 75, "trending": []},
    "pedro-calungsod": {"allTime": 48, "trending": []},
    "rose-of-lima": {"allTime": 70, "trending": []},
    "maximilian-kolbe": {"allTime": 82, "trending": [{"year": 2025, "rank": 16}]},
}

for saint in en_data['saints']:
    saint['popularity'] = popularity_map.get(saint['id'], {"allTime": 50, "trending": []})

for saint in es_data['saints']:
    saint['popularity'] = popularity_map.get(saint['id'], {"allTime": 50, "trending": []})

print("Added popularity to existing saints")
