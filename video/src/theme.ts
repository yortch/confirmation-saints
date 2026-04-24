export const theme = {
  red: "#B9161C",
  redDeep: "#6B0D10",
  redDark: "#3A0608",
  gold: "#D4A24A",
  cream: "#F5EFE4",
  ink: "#1A0A0C",
  white: "#FFFFFF",
} as const;

// Standard ease-out-expo-ish; matches decisions.md motion language.
export const EASE_OUT = [0.22, 1, 0.36, 1] as const;
export const EASE_IN_OUT = [0.65, 0, 0.35, 1] as const;
