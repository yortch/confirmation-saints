import { AbsoluteFill, Img, interpolate, spring, staticFile, useCurrentFrame, useVideoConfig } from "remotion";
import { theme } from "../theme";

// Duration: 90 frames (3s). Icon + wordmark + store badges.
export const EndCard: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  const iconSpring = spring({
    frame,
    fps,
    config: { damping: 180, mass: 0.7 },
    durationInFrames: 22,
  });
  const iconScale = interpolate(iconSpring, [0, 1], [0.7, 1]);
  const iconOpacity = interpolate(frame, [0, 12], [0, 1], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  const wordOpacity = interpolate(frame, [14, 28], [0, 1], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });
  const wordTy = interpolate(frame, [14, 28], [20, 0], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  const badgesOpacity = interpolate(frame, [34, 52], [0, 1], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });
  const badgesTy = interpolate(frame, [34, 52], [18, 0], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  // Tiny gentle exit at the very end to avoid a hard cut
  const holdOut = interpolate(frame, [80, 90], [1, 0.92], {
    extrapolateLeft: "clamp",
    extrapolateRight: "clamp",
  });

  return (
    <AbsoluteFill
      style={{
        background: `radial-gradient(circle at 50% 40%, ${theme.red} 0%, ${theme.redDeep} 55%, ${theme.redDark} 100%)`,
        justifyContent: "center",
        alignItems: "center",
        opacity: holdOut,
      }}
    >
      <div style={{ display: "flex", flexDirection: "column", alignItems: "center", gap: 26 }}>
        <div
          style={{
            transform: `scale(${iconScale})`,
            opacity: iconOpacity,
            width: 280,
            height: 280,
            borderRadius: 60,
            overflow: "hidden",
            boxShadow: "0 30px 80px rgba(0,0,0,0.55), 0 0 0 2px rgba(212,162,74,0.35)",
            background: "#000",
          }}
        >
          <Img
            src={staticFile("icons/app-icon-1024.png")}
            style={{ width: "100%", height: "100%", objectFit: "cover" }}
          />
        </div>

        <div
          style={{
            opacity: wordOpacity,
            transform: `translateY(${wordTy}px)`,
            textAlign: "center",
            fontFamily: "'Cormorant Garamond', Georgia, serif",
            color: theme.white,
          }}
        >
          <div style={{ fontSize: 96, fontWeight: 600, letterSpacing: -1, lineHeight: 1 }}>
            Confirmation Saints
          </div>
          <div
            style={{
              marginTop: 14,
              fontSize: 34,
              color: theme.gold,
              fontStyle: "italic",
              letterSpacing: 0.5,
            }}
          >
            Free · Offline · Bilingual
          </div>
        </div>

        <div
          style={{
            opacity: badgesOpacity,
            transform: `translateY(${badgesTy}px)`,
            display: "flex",
            gap: 22,
            alignItems: "center",
            marginTop: 14,
          }}
        >
          <Img
            src={staticFile("badges/app-store-badge.svg")}
            style={{ height: 72, width: "auto" }}
          />
          <Img
            src={staticFile("badges/google-play-badge.png")}
            style={{ height: 72, width: "auto" }}
          />
        </div>
      </div>
    </AbsoluteFill>
  );
};
