import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
export default defineConfig({
  plugins: [vue()],
  server: {
    port: 5173,
    strictPort: true,
    proxy: { "/api": "http://127.0.0.1:8080" },
  },
  build: {
    rolldownOptions: {
      output: {
        manualChunks(id) {
          if (id.includes("/node_modules/element-plus/")) return "ui";
          if (/\/node_modules\/(?:@vue|vue|vue-router|pinia)\//.test(id))
            return "vue";
        },
      },
    },
  },
});
