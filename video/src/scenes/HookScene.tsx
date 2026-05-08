import { AbsoluteFill, interpolate, spring, useCurrentFrame, useVideoConfig } from "remotion";
import { theme } from "../theme";

// 0 – 120 frames (4s total: 2.5s hook + 1.5s promise)
// Owns frames 0–119 within its own Sequence timeline.
export const HookScene: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  // Radial burst grows in the first 18 frames.
  const burst = interpolate(frame, [0, 18], [0.2, 1], {
    extrapolateRight: "clamp",
    extrapolateLeft: "clamp",
  });

  // Hook title (2.5s window = 0–75).
  const hookIn = spring({ frame, fps, config: { damping: 200, mass: 0.8 }, durationInFrames: 18 });
  const hookOut = interpolate(frame, [62, 75], [1, 0], {
    extrapolateRight: "clamp",
    extrapolateLeft: "clamp",
  });
  const hookOpacity = Math.min(hookIn, hookOut);
  const hookTranslate = interpolate(hookIn, [0, 1], [18, 0]);

  // Promise title (1.5s window = 75–120).
  const promiseFrame = frame - 75;
  const promiseIn = spring({
    frame: promiseFrame,
    fps,
    config: { damping: 200, mass: 0.8 },
    durationInFrames: 16,
  });
  const promiseOut = interpolate(frame, [110, 120], [1, 0], {
    extrapolateRight: "clamp",
    extrapolateLeft: "clamp",
  });
  const promiseOpacity = promiseFrame < 0 ? 0 : Math.min(promiseIn, promiseOut);
  const promiseScale = interpolate(promiseIn, [0, 1], [0.94, 1]);

  return (
    <AbsoluteFill
      style={{
        background: `radial-gradient(circle at 50% 45%, ${theme.red} 0%, ${theme.redDeep} 55%, ${theme.redDark} 100%)`,
        justifyContent: "center",
        alignItems: "center",
        overflow: "hidden",
      }}
    >
      {/* Soft burst of light */}
      <div
        style={{
          position: "absolute",
          width: 1600,
          height: 1600,
          borderRadius: "50%",
          background:
            "radial-gradient(circle, rgba(255,230,180,0.35) 0%, rgba(255,180,140,0.10) 45%, rgba(0,0,0,0) 70%)",
          transform: `scale(${burst})`,
          filter: "blur(20px)",
        }}
      />

      {/* Hook */}
      {frame < 75 && (
        <div
          style={{
            position: "absolute",
            opacity: hookOpacity,
            transform: `translateY(${hookTranslate}px)`,
            textAlign: "center",
            color: theme.white,
            fontFamily: "'Cormorant Garamond', Georgia, serif",
          }}
        >
          <div style={{ fontSize: 96, fontWeight: 500, lineHeight: 1.05, letterSpacing: -1 }}>
            Preparing for
            <br />
            Confirmation?
          </div>
        </div>
      )}

      {/* Promise */}
      {frame >= 70 && (
        <div
          style={{
            position: "absolute",
            opacity: promiseOpacity,
            transform: `scale(${promiseScale})`,
            textAlign: "center",
            color: theme.cream,
            fontFamily: "'Cormorant Garamond', Georgia, serif",
          }}
        >
          <div style={{ fontSize: 130, fontWeight: 600, lineHeight: 1, letterSpacing: -2 }}>
            100+ saints.
          </div>
          <div
            style={{
              fontSize: 72,
              fontWeight: 400,
              fontStyle: "italic",
              marginTop: 16,
              color: theme.gold,
            }}
          >
            One is yours.
          </div>
        </div>
      )}
    </AbsoluteFill>
  );
};
