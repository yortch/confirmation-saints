#!/usr/bin/env python3
"""
Download public domain saint images from Wikimedia Commons.
Images are saved to SharedContent/images/ as {saint-id}.jpg.
Uses Wikimedia Commons thumb URLs for reasonable file sizes (~400px wide).
"""

import json
import os
import ssl
import time
import urllib.request
import urllib.parse
import urllib.error

IMAGES_DIR = os.path.join(os.path.dirname(__file__), "SharedContent", "images")
SAINTS_EN = os.path.join(os.path.dirname(__file__), "SharedContent", "saints", "saints-en.json")
SAINTS_ES = os.path.join(os.path.dirname(__file__), "SharedContent", "saints", "saints-es.json")

TARGET_WIDTH = 400

# Mapping of saint-id -> Wikimedia Commons filename (exact filename on Commons)
SAINT_IMAGES = {
    "therese-of-lisieux": "Therese_von_Lisieux.jpg",
    "dominic-savio": "Life_of_Dominic_Savio_(page_6_crop).jpg",
    "carlo-acutis": "Covent_Garden,_Corpus_Christi_Catholic_Church,_Carlo_Acutis_memorial.jpg",
    "joan-of-arc": "Joan_of_Arc_miniature_graded.jpg",
    "maria-goretti": "Photograph_of_Saint_Maria_Goretti,_1902.jpg",
    "chiara-luce-badano": "Sassello_SV.jpg",
    "jose-sanchez-del-rio": "José_Sánchez_del_Río.jpg",
    "sebastian": "Saint_Sebastian_painting.jpg",
    "cecilia": "Raphael_St_Cecilia.jpg",
    "francis-of-assisi": "Francis_of_Assisi.jpg",
    "thomas-aquinas": "St-thomas-aquinas.jpg",
    "thomas-more": "Hans_Holbein,_the_Younger_-_Sir_Thomas_More_-_Google_Art_Project.jpg",
    "gianna-beretta-molla": "Gianna_Beretta_Molla.jpg",
    "louis-martin": "Louis_Martin_1.jpg",
    "zelie-martin": "Zélie_Martin_1.jpg",
    "mother-teresa": "Mutter_Teresa_von_Kalkutta.jpg",
    "john-paul-ii": "John_Paul_II_Medal_of_Freedom_2004.jpg",
    "pier-giorgio-frassati": "PIER_GIORGIO_FRASSATI1925.jpg",
    "kateri-tekakwitha": "Kateri_Tekakwitha.jpg",
    "augustine-of-hippo": "Gerard_Seghers_(attr)_-_The_Four_Doctors_of_the_Western_Church,_Saint_Augustine_of_Hippo_(354–430).jpg",
    "josephine-bakhita": "Photograph_of_Saint_Josephine_Bakhita_(c._1910).jpg",
    "juan-diego": "Juan-Diego.jpg",
    "pedro-calungsod": "Pedro_Calungsod_2012_stamp_(cropped).jpg",
    "rose-of-lima": "Santa_Rosa_de_Lima.jpg",
    "maximilian-kolbe": "Fr.Maximilian_Kolbe_1939.jpg",
    "monica": "Scheffer_Saint_Augustine_and_Saint_Monica.jpg",
    "charbel-makhlouf": "Charbel.jpg",
    "michael-archangel": "Guido_Reni_031.jpg",
    "gabriel-archangel": "Zurbaran-Ange-Gabriel.JPG",
    "our-lady-guadalupe": "Virgen_de_guadalupe1.jpg",
    "our-lady-fatima": "Our_Lady_of_Fatima_Statue.jpg",
    "frances-cabrini": "Francesca_Cabrini.JPG",
    "joseph": "Guido_Reni_-_St_Joseph_with_the_Infant_Jesus_-_WGA19304.jpg",
    "joachim": "Giotto_-_Scrovegni_-_-05-_-_Joachim's_Dream.jpg",
    "anne": "Leonardo_da_Vinci_-_Virgin_and_Child_with_Ss_Anne_and_John_the_Baptist.jpg",
    "john-bosco": "Don_Bosco.jpg",
    "marcellin-champagnat": "Marcellin_Champagnat.jpg",
    "peter-apostle": "Pope-peter_pprubens.jpg",
    "paul-apostle": "El_Greco_-_St._Paul_-_Google_Art_Project.jpg",
    "andrew-apostle": "El_Greco_-_Apostle_St_Andrew_-_WGA10610.jpg",
    "james-greater": "Guido_Reni_-_Saint_James_the_Greater_-_Google_Art_Project.jpg",
    "john-apostle": "Grandes_Heures_Anne_de_Bretagne_Saint_Jean.jpg",
    "philip-apostle": "Rubens_apostel_philippus.jpg",
    "bartholomew-apostle": "Bartholomaeus_San_Giovanni_in_Laterano_2006-09-07.jpg",
    "matthew-apostle": "The_Calling_of_Saint_Matthew-Caravaggo_(1599-1600).jpg",
    "thomas-apostle": "Caravaggio_-_The_Incredulity_of_Saint_Thomas.jpg",
    "james-less": "El_Greco_-_St._James_the_Less_-_Google_Art_Project.jpg",
    "jude-thaddeus": "El_Greco_-_St._Jude_Thaddeus_-_Google_Art_Project.jpg",
    "simon-zealot": "Anthony_van_Dyck_-_The_Apostle_Saint_Simon.jpg",
    "matthias-apostle": "Rubens_apostle_Matthias_grt.jpg",
    "pius-x": "Pius_X_pope.jpg",
    "patrick": "St._Patrick,_Bishop_of_Ireland_Met_DP890884.jpg",
    "catherine-of-siena": "Giovanni_Battista_Tiepolo_096.jpg",
    "martin-de-porres": "Martin_de_Porres.jpg",
    "blaise": "Ca'_Rezzonico_-_Sala_Tiepolo_-_San_Biagio_by_Giambattiasta_Tiepolo.jpg",
    "vitus": "Saint_Vitus.jpg",
    "genesius-of-rome": "Cristoforo_Moretti_-_Saint_Genesius_-_Google_Art_Project.jpg",
    "michael-mcgivney": "Father_McGivney_300.jpg",
    "teresa-of-the-andes": "Teresa_de_los_Andes.jpg",
    "joseph-mukasa-balikuddembe": "St_Joseph_Mukasa_c1920_d.png",
    "elizabeth-ann-seton": "Elizabeth_Ann_Seton_portrait_by_Amabilia_Filicchi_(cropped).jpg",
    "oscar-romero": "Monseñor_Romero_(colour).jpg",
    "faustina-kowalska": "Faustina.jpg",
    "mary-magdalene": "Paolo_Veronese,_The_Conversion_of_Mary_Magdalene.jpg",
    "boniface": "SaintBoniface.jpg",
    "catherine-of-alexandria": "Saint_Catherine_of_Alexandria_and_the_Angel_-_Francesco_Solimena.jpg",
    "john-the-baptist": "Leonardo_da_Vinci_-_Saint_John_the_Baptist_C2RMF_retouched.jpg",
    "clare-of-assisi": "Simone_Martini_047.jpg",
    "luke-evangelist": "St_Luke_the_Evangelist.jpg",
    "gemma-galgani": "Gemma_Galgani.jpg",
    "john-neumann": "Rt. Rev. John Nepomucene Neumann, half-length portrait, facing left LCCN96508709.jpg",
    "moses-the-black": "San Moises el Negro.jpg",
    "vladimir-of-kiev": "Saint Vladimir le Grand.jpg",
    "ignatius-of-loyola": "Ignatius_Loyola.jpg",
    "imelda-lambertini": "ImeldaLambertini.jpg",
    "isidore-the-farmer": "San_Isidro_Labrador.jpg",
    "teresa-of-avila": "Peter_Paul_Rubens_060.jpg",
    "anthony-of-padua": "Saint_Anthony_of_Padua.jpg",
    "josemaria-escriva": "Detail autel Jose Maria Escriva de Balaguer Peterskirche Vienna.jpg",
}


def get_image_url(commons_filename, width=TARGET_WIDTH):
    """Use Wikimedia API to get the thumb URL for a file."""
    api_url = (
        "https://commons.wikimedia.org/w/api.php?"
        "action=query&titles=File:{}&prop=imageinfo&iiprop=url"
        "&iiurlwidth={}&format=json"
    ).format(urllib.parse.quote(commons_filename), width)

    ctx = ssl.create_default_context()
    req = urllib.request.Request(api_url, headers={
        "User-Agent": "ConfirmationSaintsApp/1.0 (https://github.com/yortch/confirmation-saints; educational project)"
    })
    with urllib.request.urlopen(req, context=ctx, timeout=30) as resp:
        data = json.loads(resp.read().decode())

    pages = data.get("query", {}).get("pages", {})
    for page_id, page_data in pages.items():
        if page_id == "-1":
            return None
        imageinfo = page_data.get("imageinfo", [{}])
        if imageinfo:
            # Prefer thumburl for resized version
            return imageinfo[0].get("thumburl") or imageinfo[0].get("url")
    return None


def download_image(url, dest_path):
    """Download an image from a URL to a local path."""
    ctx = ssl.create_default_context()
    req = urllib.request.Request(url, headers={
        "User-Agent": "ConfirmationSaintsApp/1.0 (https://github.com/yortch/confirmation-saints; educational project)"
    })
    with urllib.request.urlopen(req, context=ctx, timeout=60) as resp:
        with open(dest_path, "wb") as f:
            f.write(resp.read())


def update_json_files(successful_saints):
    """Update image fields in both EN and ES JSON files."""
    for json_path in [SAINTS_EN, SAINTS_ES]:
        with open(json_path, "r", encoding="utf-8") as f:
            data = json.load(f)

        for saint in data["saints"]:
            if saint["id"] in successful_saints:
                saint["image"] = {
                    "filename": f"{saint['id']}.jpg",
                    "attribution": "Public domain, via Wikimedia Commons"
                }

        with open(json_path, "w", encoding="utf-8") as f:
            json.dump(data, f, indent=2, ensure_ascii=False)
            f.write("\n")

    print(f"Updated {len(successful_saints)} saints in both JSON files.")


def main():
    os.makedirs(IMAGES_DIR, exist_ok=True)

    successful = set()
    failed = []

    for saint_id, commons_filename in SAINT_IMAGES.items():
        dest = os.path.join(IMAGES_DIR, f"{saint_id}.jpg")

        # Skip if already downloaded
        if os.path.exists(dest) and os.path.getsize(dest) > 1000:
            print(f"  SKIP {saint_id} (already exists)")
            successful.add(saint_id)
            continue

        print(f"  Fetching URL for {saint_id} ({commons_filename})...")
        try:
            url = get_image_url(commons_filename)
            if not url:
                print(f"  FAIL {saint_id}: could not resolve URL")
                failed.append((saint_id, "URL not found"))
                continue

            print(f"  Downloading {saint_id}...")
            download_image(url, dest)

            size = os.path.getsize(dest)
            if size < 1000:
                os.remove(dest)
                print(f"  FAIL {saint_id}: file too small ({size} bytes)")
                failed.append((saint_id, "File too small"))
                continue

            print(f"  OK   {saint_id} ({size:,} bytes)")
            successful.add(saint_id)

            # Be polite to Wikimedia servers
            time.sleep(0.5)

        except Exception as e:
            print(f"  FAIL {saint_id}: {e}")
            failed.append((saint_id, str(e)))
            if os.path.exists(dest):
                os.remove(dest)

    print(f"\n{'='*60}")
    print(f"Downloaded: {len(successful)}/{len(SAINT_IMAGES)}")

    if failed:
        print(f"Failed ({len(failed)}):")
        for sid, reason in failed:
            print(f"  - {sid}: {reason}")

    # Update JSON files for successful downloads
    if successful:
        update_json_files(successful)

    return failed


if __name__ == "__main__":
    failed = main()
    exit(0 if not failed else 1)
