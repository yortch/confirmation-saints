import { AbsoluteFill, interpolate, useCurrentFrame } from "remotion";

const BRAND_RED = "#B9161C";
const BRAND_RED_DEEP = "#6B0D10";

export const ConfirmationSaintsPromo: React.FC = () => {
  const frame = useCurrentFrame();
  const titleOpacity = interpolate(frame, [0, 20, 870, 900], [0, 1, 1, 0], {
    extrapolateRight: "clamp",
    extrapolateLeft: "clamp",
  });
  const subtitleOpacity = interpolate(frame, [20, 50], [0, 1], {
    extrapolateRight: "clamp",
    extrapolateLeft: "clamp",
  });

  return (
    <AbsoluteFill
      style={{
        background: `radial-gradient(circle at 50% 40%, ${BRAND_RED} 0%, ${BRAND_RED_DEEP} 100%)`,
        justifyContent: "center",
        alignItems: "center",
        fontFamily: "Georgia, 'Times New Roman', serif",
        color: "white",
        textAlign: "center",
      }}
    >
      <div style={{ opacity: titleOpacity }}>
        <div
          style={{
            fontSize: 110,
            fontWeight: 600,
            letterSpacing: -1,
            lineHeight: 1.05,
            textShadow: "0 4px 30px rgba(0,0,0,0.35)",
          }}
        >
          Confirmation
          <br />
          Saints
        </div>
        <div
          style={{
            marginTop: 28,
            fontSize: 34,
            fontWeight: 300,
            letterSpacing: 4,
            textTransform: "uppercase",
            opacity: subtitleOpacity,
            fontFamily: "'Helvetica Neue', Arial, sans-serif",
          }}
        >
          Placeholder — treatment pending approval
        </div>
      </div>
    </AbsoluteFill>
  );
};
