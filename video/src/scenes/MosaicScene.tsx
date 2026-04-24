import { AbsoluteFill, Img, interpolate, staticFile, useCurrentFrame } from "remotion";
import { MOSAIC_SAINTS, ROTATING_TAGS } from "../data";
import { theme } from "../theme";

// Duration: 300 frames (10s). Frames 0-299 local.
const DURATION = 300;

const COL_COUNT = 3;
const TILES_PER_COL = 8; // enough to fill and loop
const TILE_W = 340;
const TILE_H = 440;
const GAP = 24;

// Distribute saints across 3 columns with a stable assignment.
const columns: string[][] = Array.from({ length: COL_COUNT }, () => []);
MOSAIC_SAINTS.forEach((s, i) => {
  columns[i % COL_COUNT].push(s.file);
});
// Pad each column to TILES_PER_COL by cycling.
columns.forEach((col) => {
  let i = 0;
  while (col.length < TILES_PER_COL) {
    col.push(col[i % Math.max(col.length, 1)] ?? MOSAIC_SAINTS[0].file);
    i += 1;
  }
});

// Each column moves at a slightly different speed for parallax.
const COL_SPEEDS = [0.7, 1.0, 0.85];

export const MosaicScene: React.FC = () => {
  const frame = useCurrentFrame();

  // Fade in over 15 frames, fade out over final 20.
  const opacity = interpolate(
    frame,
    [0, 15, DURATION - 20, DURATION],
    [0, 1, 1, 0],
    { extrapolateLeft: "clamp", extrapolateRight: "clamp" },
  );

  // Shared vignette / veil over images to keep text legible.
  return (
    <AbsoluteFill
      style={{
        background: `linear-gradient(180deg, ${theme.redDark} 0%, ${theme.redDeep} 100%)`,
        overflow: "hidden",
        opacity,
      }}
    >
      {/* Columns container */}
      <div
        style={{
          position: "absolute",
          inset: 0,
          display: "flex",
          justifyContent: "center",
          gap: GAP,
          alignItems: "flex-start",
        }}
      >
        {columns.map((col, ci) => {
          const speed = COL_SPEEDS[ci] ?? 1;
          // Pixels per frame.
          const pxPerFrame = 5 * speed;
          const totalColHeight = col.length * (TILE_H + GAP);
          // Offset each column's starting position so they don't align.
          const startOffset = ci * 140;
          // Use modulo so it appears to loop.
          const rawOffset = frame * pxPerFrame + startOffset;
          const offset = rawOffset % totalColHeight;

          return (
            <div
              key={ci}
              style={{
                position: "relative",
                width: TILE_W,
                height: "100%",
                overflow: "visible",
              }}
            >
              {/* Render two copies back-to-back so the loop is seamless */}
              {[0, 1].map((copyIdx) => (
                <div
                  key={copyIdx}
                  style={{
                    position: "absolute",
                    top: -offset + copyIdx * totalColHeight - TILE_H,
                    left: 0,
                    width: TILE_W,
                    display: "flex",
                    flexDirection: "column",
                    gap: GAP,
                  }}
                >
                  {col.map((file, ti) => (
                    <div
                      key={`${copyIdx}-${ti}`}
                      style={{
                        width: TILE_W,
                        height: TILE_H,
                        borderRadius: 16,
                        overflow: "hidden",
                        boxShadow: "0 20px 50px rgba(0,0,0,0.45)",
                        border: `1px solid rgba(212, 162, 74, 0.25)`,
                      }}
                    >
                      <Img
                        src={staticFile(`saints/${file}`)}
                        style={{
                          width: "100%",
                          height: "100%",
                          objectFit: "cover",
                          filter: "saturate(0.95) contrast(1.05)",
                        }}
                      />
                    </div>
                  ))}
                </div>
              ))}
            </div>
          );
        })}
      </div>

      {/* Vignette for legibility */}
      <AbsoluteFill
        style={{
          background:
            "radial-gradient(ellipse at center, rgba(0,0,0,0) 40%, rgba(58,6,8,0.85) 100%)",
          pointerEvents: "none",
        }}
      />

      {/* Rotating tag chips — one at a time, centered */}
      <TagTicker frame={frame} />
    </AbsoluteFill>
  );
};

const TagTicker: React.FC<{ frame: number }> = ({ frame }) => {
  // Show a new tag every ~24 frames (0.8s). We show during frames 20 → 270.
  const start = 20;
  const end = 270;
  const perTag = 24;
  if (frame < start || frame > end) return null;

  const idx = Math.floor((frame - start) / perTag) % ROTATING_TAGS.length;
  const local = (frame - start) % perTag;
  const opacity = interpolate(local, [0, 4, perTag - 6, perTag], [0, 1, 1, 0], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });
  const translate = interpolate(local, [0, 6], [10, 0], { extrapolateRight: "clamp" });

  const tag = ROTATING_TAGS[idx];
  return (
    <div
      style={{
        position: "absolute",
        bottom: 110,
        left: 0,
        right: 0,
        display: "flex",
        justifyContent: "center",
      }}
    >
      <div
        style={{
          opacity,
          transform: `translateY(${translate}px)`,
          padding: "18px 42px",
          borderRadius: 999,
          background: "rgba(26, 10, 12, 0.78)",
          border: `1px solid ${theme.gold}`,
          color: theme.cream,
          fontFamily: "'Inter', 'Helvetica Neue', Arial, sans-serif",
          fontSize: 44,
          fontWeight: 500,
          letterSpacing: 2,
          textTransform: "uppercase",
          backdropFilter: "blur(10px)",
        }}
      >
        {tag}
      </div>
    </div>
  );
};
