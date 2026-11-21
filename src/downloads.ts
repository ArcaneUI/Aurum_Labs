import economyJar from "./assets/dl/aurum-economy-1.1.0.jar?url";
import economySrc from "./assets/dl/aurum-economy-1.1.0-src.zip?url";
import seasonsJar from "./assets/dl/aurum-seasons-1.1.0.jar?url";
import backpacksJar from "./assets/dl/aurum-backpacks-1.1.0.jar?url";
import farmingJar from "./assets/dl/aurum-farming-1.1.0.jar?url";
import oceansJar from "./assets/dl/aurum-oceans-1.1.0.jar?url";
import machinesJar from "./assets/dl/aurum-machines-1.1.0.jar?url";

export type DL = { url: string; name: string; size: string };

export const JAR: DL = { url: economyJar, name: "aurum-economy-1.1.0.jar", size: "86 KB" };
export const SRC: DL = { url: economySrc, name: "aurum-economy-1.1.0-src.zip", size: "100 KB" };

export const MOD_JARS: Record<string, DL> = {
  "aurum-economy": JAR,
  "aurum-seasons": { url: seasonsJar, name: "aurum-seasons-1.1.0.jar", size: "17 KB" },
  "aurum-backpacks": { url: backpacksJar, name: "aurum-backpacks-1.1.0.jar", size: "19 KB" },
  "aurum-farming": { url: farmingJar, name: "aurum-farming-1.1.0.jar", size: "12 KB" },
  "aurum-oceans": { url: oceansJar, name: "aurum-oceans-1.1.0.jar", size: "11 KB" },
  "aurum-machines": { url: machinesJar, name: "aurum-machines-1.1.0.jar", size: "15 KB" },
};
