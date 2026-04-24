// Saint data used across scenes. All image files live in public/saints/
// and are loaded via staticFile() at render time.

export type MosaicSaint = { file: string };

// 22 saints in the mosaic — diverse in gender, era, region, age category.
export const MOSAIC_SAINTS: MosaicSaint[] = [
  { file: "therese-of-lisieux.jpg" },
  { file: "francis-of-assisi.jpg" },
  { file: "joan-of-arc.jpg" },
  { file: "maximilian-kolbe.jpg" },
  { file: "dominic-savio.jpg" },
  { file: "juan-diego.jpg" },
  { file: "our-lady-guadalupe.jpg" },
  { file: "mother-teresa.jpg" },
  { file: "john-paul-ii.jpg" },
  { file: "faustina-kowalska.jpg" },
  { file: "kateri-tekakwitha.jpg" },
  { file: "pier-giorgio-frassati.jpg" },
  { file: "chiara-luce-badano.jpg" },
  { file: "maria-goretti.jpg" },
  { file: "anthony-of-padua.jpg" },
  { file: "teresa-of-avila.jpg" },
  { file: "patrick.jpg" },
  { file: "michael-archangel.jpg" },
  { file: "josephine-bakhita.jpg" },
  { file: "gianna-beretta-molla.jpg" },
  { file: "jose-sanchez-del-rio.jpg" },
  { file: "carlo-acutis.jpg" },
];

// Tags that rotate over the mosaic — drawn from tags/patronOf across the library.
export const ROTATING_TAGS = [
  "martyr",
  "young",
  "mystic",
  "teacher",
  "missionary",
  "mother",
  "sports",
  "music",
  "science",
  "medicine",
  "tech",
  "peacemaker",
];

// Hero saint — data copied verbatim from SharedContent/saints/saints-en.json.
export const HERO = {
  name: "Bl. Carlo Acutis",
  file: "carlo-acutis.jpg",
  feastDay: "October 12",
  years: "1991 – 2006",
  patronOf: "Patron of the internet, programmers, and youth",
  quote: "The Eucharist is my highway to heaven.",
  attribution: "Public domain, via Wikimedia Commons",
} as const;
