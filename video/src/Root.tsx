import { Composition } from "remotion";
import { ConfirmationSaintsPromo } from "./ConfirmationSaintsPromo";

export const RemotionRoot: React.FC = () => {
  return (
    <>
      <Composition
        id="ConfirmationSaintsPromo"
        component={ConfirmationSaintsPromo}
        durationInFrames={900}
        fps={30}
        width={1080}
        height={1080}
      />
    </>
  );
};
