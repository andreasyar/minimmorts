import { defineConfig } from 'vite';

export default defineConfig({
  root: 'src',
  cacheDir: '../node_modules/.vite',
  server: { host: '0.0.0.0', port: 5173, strictPort: true },
  build: { outDir: '../dist' },
  plugins: [{
    name: 'legacy-client-reload',
    configureServer(server) {
      server.middlewares.use((req, res, next) => {
        if (req.url === '/') req.url = '/client.html';
        next();
      });
    },
    handleHotUpdate({ server }) {
      // Classic scripts share global variables: replace the whole page.
      server.ws.send({ type: 'full-reload' });
      return [];
    },
  }],
});
