import { artisanModuleBundle } from "@artisan/editor-sdk/bundle";
import { defineConfig } from "vite";

// One ES module, React and the SDK left to the editor: the jar serves it as the module's view.
export default defineConfig({
  ...artisanModuleBundle({
    entry: "src/index.ts",
    outDir: "../src/main/resources/artisan-editor",
  }),
  test: { include: ["src/**/*.test.ts"] },
});
