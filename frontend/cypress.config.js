import { defineConfig } from "cypress";

export default defineConfig({
  e2e: {
    specPattern: "cypress/e2e/**/*.{cy,spec}.{js,jsx,ts,tsx}",
    // baseUrl: "http://ip23us1.sit.kmutt.ac.th",
    //  baseUrl: "http://localhost:5173",
    baseUrl: "https://intproj23.sit.kmutt.ac.th/us1",
  },
  component: {
    specPattern: "src/**/__tests__/*.{cy,spec}.{js,ts,jsx,tsx}",
    devServer: {
      framework: "vue",
      bundler: "vite",
    },
  },
});
