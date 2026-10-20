import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// The browser talks to Vite (port 5173). Vite forwards anything starting with /api
// to Spring Boot (port 8080). The browser sees ONE origin, so no CORS setup is needed.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': { target: 'http://localhost:8080', changeOrigin: true },
    },
  },
})
