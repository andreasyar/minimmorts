// Run: docker compose exec -T client node /workspace/dev/smoke.mjs
import assert from 'node:assert/strict';

const clientUrl = process.env.CLIENT_URL || 'http://localhost:5173';
const serverUrl = process.env.SERVER_URL || 'ws://server:8015/';
const response = await fetch(clientUrl);
assert.equal(response.status, 200);
const html = await response.text();
assert.match(html, /MINIMMORTS HTML5 client/);
for (const match of html.matchAll(/<script\b[^>]*\bsrc="([^"]+)"/gi)) {
  const script = await fetch(new URL(match[1], clientUrl));
  assert.equal(script.status, 200, `Missing script: ${match[1]}`);
  assert.doesNotMatch(script.headers.get('content-type') || '', /text\/html/);
}
console.log('HTTP: client page and scripts are available.');

await new Promise((resolve, reject) => {
  const socket = new WebSocket(serverUrl);
  const timer = setTimeout(() => finish(new Error('Timed out waiting for WELCOME')), 10000);
  function finish(error) {
    clearTimeout(timer);
    socket.close();
    if (error) reject(error);
    else resolve();
  }
  socket.addEventListener('open', () => socket.send(`LOGIN,Smoke${Date.now()},test`));
  socket.addEventListener('error', () => finish(new Error('WebSocket failed')));
  socket.addEventListener('message', ({ data }) => {
    if (typeof data === 'string' && data.includes('WELCOME')) finish();
  });
});
console.log('WebSocket: login accepted, initial game state received.');
