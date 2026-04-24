import { AbsoluteFill, Img, interpolate, spring, staticFile, useCurrentFrame, useVideoConfig } from "remotion";
import { HERO } from "../data";
import { theme } from "../theme";

// Duration: 120 frames (4s). Shows the hero saint card after the mosaic resolves.
export const SaintCardScene: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  const appear = spring({
    frame,
    fps,
    config: { damping: 200, mass: 0.6 },
    durationInFrames: 24,
  });
  const scale = interpolate(appear, [0, 1], [0.86, 1]);
  const opacity = interpolate(frame, [0, 10, 105, 120], [0, 1, 1, 0], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  // Quote fades in a beat after the card lands.
  const quoteOpacity = interpolate(frame, [30, 48], [0, 1], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  return (
    <AbsoluteFill
      style={{
        background: `radial-gradient(circle at 50% 40%, ${theme.redDeep} 0%, ${theme.redDark} 100%)`,
        justifyContent: "center",
        alignItems: "center",
        opacity,
      }}
    >
      <div
        style={{
          transform: `scale(${scale})`,
          width: 720,
          background: theme.cream,
          borderRadius: 28,
          padding: 36,
          boxShadow: "0 30px 80px rgba(0,0,0,0.55)",
          border: `2px solid ${theme.gold}`,
          fontFamily: "'Inter', 'Helvetica Neue', Arial, sans-serif",
          color: theme.ink,
        }}
      >
        <div
          style={{
            width: "100%",
            height: 520,
            borderRadius: 18,
            overflow: "hidden",
            marginBottom: 28,
          }}
        >
          <Img
            src={staticFile(`saints/${HERO.file}`)}
            style={{
              width: "100%",
              height: "100%",
              objectFit: "cover",
              objectPosition: "center 20%",
            }}
          />
        </div>
        <div
          style={{
            fontFamily: "'Cormorant Garamond', Georgia, serif",
            fontSize: 60,
            fontWeight: 600,
            lineHeight: 1.05,
          }}
        >
          {HERO.name}
        </div>
        <div
          style={{
            marginTop: 10,
            fontSize: 26,
            color: theme.redDeep,
            fontWeight: 500,
            letterSpacing: 0.5,
          }}
        >
          {HERO.years} · Feast: {HERO.feastDay}
        </div>
        <div style={{ marginTop: 6, fontSize: 24, color: "#4A2B2E" }}>
          {HERO.patronOf}
        </div>
        <div
          style={{
            marginTop: 22,
            paddingTop: 18,
            borderTop: `1px solid rgba(107, 13, 16, 0.25)`,
            fontFamily: "'Cormorant Garamond', Georgia, serif",
            fontStyle: "italic",
            fontSize: 34,
            lineHeight: 1.25,
            color: theme.redDeep,
            opacity: quoteOpacity,
          }}
        >
          &ldquo;{HERO.quote}&rdquo;
        </div>
      </div>
    </AbsoluteFill>
  );
};
