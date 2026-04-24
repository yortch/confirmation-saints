import { AbsoluteFill, Audio, Series, staticFile } from "remotion";
import { loadFont as loadCormorant } from "@remotion/google-fonts/CormorantGaramond";
import { loadFont as loadInter } from "@remotion/google-fonts/Inter";
import { HookScene } from "./scenes/HookScene";
import { MosaicScene } from "./scenes/MosaicScene";
import { SaintCardScene } from "./scenes/SaintCardScene";
import { AppTourScene } from "./scenes/AppTourScene";
import { EndCard } from "./scenes/EndCard";
import { useEffect, useState } from "react";

loadCormorant();
loadInter();

// Timeline (30s @ 30fps = 900 frames):
//   0–120   Hook + Promise      (4s)
//   120–420 Mosaic              (10s)
//   420–540 Saint Card (Carlo)  (4s)
//   540–810 App Tour            (9s)
//   810–900 End Card            (3s)
export const ConfirmationSaintsPromo: React.FC = () => {
  const [hasAudio, setHasAudio] = useState(false);
  useEffect(() => {
    let cancelled = false;
    fetch(staticFile("audio/track.mp3"), { method: "HEAD" })
      .then((r) => {
        if (!cancelled) setHasAudio(r.ok);
      })
      .catch(() => {
        /* no-op */
      });
    return () => {
      cancelled = true;
    };
  }, []);

  return (
    <AbsoluteFill style={{ backgroundColor: "#3A0608" }}>
      <Series>
        <Series.Sequence durationInFrames={120}>
          <HookScene />
        </Series.Sequence>
        <Series.Sequence durationInFrames={300}>
          <MosaicScene />
        </Series.Sequence>
        <Series.Sequence durationInFrames={120}>
          <SaintCardScene />
        </Series.Sequence>
        <Series.Sequence durationInFrames={270}>
          <AppTourScene />
        </Series.Sequence>
        <Series.Sequence durationInFrames={90}>
          <EndCard />
        </Series.Sequence>
      </Series>

      {hasAudio ? <Audio src={staticFile("audio/track.mp3")} volume={0.85} /> : null}
    </AbsoluteFill>
  );
};
