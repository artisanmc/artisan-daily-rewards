import { type FrontendModule, injectModuleStyles } from "@artisan/editor-sdk";
import { RewardsGrid } from "./RewardsGrid";
import { STYLES } from "./styles";

injectModuleStyles("daily", STYLES);

/**
 * The daily module's view in the Artisan editor, served by its jar: the Rewards screen
 * (`daily:rewards`, component `rewards`) as a grid of the cycle. The Settings screen keeps
 * the generated form.
 */
const daily: FrontendModule = {
  id: "daily",
  displayName: "Daily rewards",
  schemaVersion: "daily/v1",
  sidebar: { icon: () => null, label: "Daily rewards", to: "/m/daily" },
  routes: [],
  screens: { rewards: RewardsGrid },
};

export default daily;
