import { AbsoluteFill, Img, interpolate, Sequence, staticFile, useCurrentFrame } from "remotion";
import { theme } from "../theme";

// Duration: 270 frames (9s).
// Part 1 (0–150): hero saint-detail phone with "Bios. Quotes. Feast days. Offline." captions
// Part 2 (150–270): triptych of list/explore/about screens + "English + Español."
export const AppTourScene: React.FC = () => {
  return (
    <AbsoluteFill
      style={{
        background: `linear-gradient(160deg, ${theme.redDeep} 0%, ${theme.redDark} 100%)`,
      }}
    >
      <Sequence from={0} durationInFrames={150}>
        <DetailPart />
      </Sequence>
      <Sequence from={150} durationInFrames={120}>
        <TriptychPart />
      </Sequence>
    </AbsoluteFill>
  );
};

const DETAIL_CAPTIONS = [
  { text: "Bios.", at: 15 },
  { text: "Quotes.", at: 45 },
  { text: "Feast days.", at: 75 },
  { text: "Offline.", at: 105 },
];

const DetailPart: React.FC = () => {
  const frame = useCurrentFrame();
  const phoneIn = interpolate(frame, [0, 20], [60, 0], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });
  const phoneOpacity = interpolate(frame, [0, 14, 135, 150], [0, 1, 1, 0], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  return (
    <AbsoluteFill style={{ alignItems: "center", justifyContent: "center" }}>
      <div
        style={{
          position: "relative",
          width: 440,
          height: 987,
          borderRadius: 54,
          padding: 14,
          background: "#0A0A0A",
          boxShadow: "0 30px 80px rgba(0,0,0,0.55), 0 0 0 2px rgba(255,255,255,0.05)",
          transform: `translateY(${phoneIn}px)`,
          opacity: phoneOpacity,
        }}
      >
        <div
          style={{
            width: "100%",
            height: "100%",
            borderRadius: 42,
            overflow: "hidden",
            background: "#000",
          }}
        >
          <Img
            src={staticFile("screenshots/phone-screenshot-2-saint-detail.png")}
            style={{ width: "100%", height: "100%", objectFit: "cover" }}
          />
        </div>
        {/* Notch */}
        <div
          style={{
            position: "absolute",
            top: 26,
            left: "50%",
            transform: "translateX(-50%)",
            width: 130,
            height: 30,
            borderRadius: 16,
            background: "#000",
          }}
        />
      </div>

      {/* Caption column on the right */}
      <div
        style={{
          position: "absolute",
          right: 70,
          top: 0,
          bottom: 0,
          display: "flex",
          flexDirection: "column",
          justifyContent: "center",
          gap: 24,
          fontFamily: "'Cormorant Garamond', Georgia, serif",
          color: theme.cream,
          textAlign: "right",
        }}
      >
        {DETAIL_CAPTIONS.map((c) => {
          const op = interpolate(frame, [c.at, c.at + 10], [0, 1], {
            extrapolateLeft: "clamp",
            extrapolateRight: "clamp",
          });
          const exit = interpolate(frame, [135, 150], [1, 0], {
            extrapolateLeft: "clamp",
            extrapolateRight: "clamp",
          });
          const tx = interpolate(frame, [c.at, c.at + 10], [20, 0], {
            extrapolateLeft: "clamp",
            extrapolateRight: "clamp",
          });
          return (
            <div
              key={c.text}
              style={{
                opacity: op * exit,
                transform: `translateX(${tx}px)`,
                fontSize: 56,
                fontWeight: 500,
                letterSpacing: -0.5,
                color: c.text === "Offline." ? theme.gold : theme.cream,
              }}
            >
              {c.text}
            </div>
          );
        })}
      </div>
    </AbsoluteFill>
  );
};

const TRIPTYCH = [
  { file: "phone-screenshot-1-saints.png", caption: "Search" },
  { file: "phone-screenshot-4-explore.png", caption: "Filter" },
  { file: "phone-screenshot-3-about.png", caption: "Learn" },
];

const TriptychPart: React.FC = () => {
  const frame = useCurrentFrame();
  const containerOpacity = interpolate(frame, [0, 14, 105, 120], [0, 1, 1, 0], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  // Tagline "English + Español." fades in second half
  const taglineOpacity = interpolate(frame, [55, 70, 105, 120], [0, 1, 1, 0], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });
  const taglineTy = interpolate(frame, [55, 70], [14, 0], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  return (
    <AbsoluteFill
      style={{
        alignItems: "center",
        justifyContent: "center",
        opacity: containerOpacity,
      }}
    >
      <div
        style={{
          display: "flex",
          gap: 26,
          alignItems: "center",
          justifyContent: "center",
        }}
      >
        {TRIPTYCH.map((t, i) => {
          const stagger = i * 6;
          const itemIn = interpolate(frame, [stagger, stagger + 18], [80, 0], {
            extrapolateLeft: "clamp",
            extrapolateRight: "clamp",
          });
          const itemOp = interpolate(frame, [stagger, stagger + 18], [0, 1], {
            extrapolateLeft: "clamp",
            extrapolateRight: "clamp",
          });
          return (
            <div
              key={t.file}
              style={{
                transform: `translateY(${itemIn}px)`,
                opacity: itemOp,
                width: 280,
                height: 628,
                borderRadius: 32,
                padding: 8,
                background: "#0A0A0A",
                boxShadow: "0 20px 50px rgba(0,0,0,0.5)",
              }}
            >
              <div
                style={{
                  width: "100%",
                  height: "100%",
                  borderRadius: 24,
                  overflow: "hidden",
                }}
              >
                <Img
                  src={staticFile(`screenshots/${t.file}`)}
                  style={{ width: "100%", height: "100%", objectFit: "cover" }}
                />
              </div>
            </div>
          );
        })}
      </div>

      <div
        style={{
          marginTop: 46,
          opacity: taglineOpacity,
          transform: `translateY(${taglineTy}px)`,
          fontFamily: "'Cormorant Garamond', Georgia, serif",
          fontSize: 64,
          color: theme.cream,
          fontStyle: "italic",
          letterSpacing: -0.5,
        }}
      >
        English <span style={{ color: theme.gold, fontStyle: "normal" }}>+</span> Español
      </div>
    </AbsoluteFill>
  );
};
