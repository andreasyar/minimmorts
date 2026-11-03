// Run in a separate Node 24 container, never in the runtime images.
import assert from 'node:assert/strict';

const base = process.env.CLIENT_URL || 'http://web';
const response = await fetch(base);
assert.equal(response.status, 200);
const html = await response.text();
assert.match(html, /window\.location\.host \+ '\/ws\/'/);
assert.doesNotMatch(html, /@vite\/client/);
for (const match of html.matchAll(/<script\b[^>]*\bsrc="([^"]+)"/gi)) {
  const script = await fetch(new URL(match[1], base));
  assert.equal(script.status, 200, match[1]);
  assert.doesNotMatch(script.headers.get('content-type') || '', /text\/html/);
}
assert.equal((await fetch(new URL('/sprites/grass.png', base))).status, 404);
assert.equal((await fetch(new URL('/healthz', base))).status, 200);

const sockets = [];
try {
  // Keep both connections open to check simultaneous players through Nginx.
  await Promise.all([1, 2].map(player => new Promise((resolve, reject) => {
    const url = new URL('/ws/', base);
    url.protocol = url.protocol === 'https:' ? 'wss:' : 'ws:';
    const socket = new WebSocket(url);
    sockets.push(socket);
    const timer = setTimeout(() => finish(new Error(`Player ${player}: no WELCOME`)), 10000);
    function finish(error) {
      clearTimeout(timer);
      if (error) reject(error);
      else resolve();
    }
    socket.addEventListener('open', () => socket.send(`LOGIN,Release${Date.now()}P${player},test`));
    socket.addEventListener('error', () => finish(new Error(`Player ${player}: WebSocket failed`)));
    socket.addEventListener('message', ({ data }) => {
      if (typeof data === 'string' && data.includes('WELCOME')) finish();
    });
  })));
  assert.ok(sockets.every(socket => socket.readyState === WebSocket.OPEN));
  console.log('Release OK: static client, no bundled sprites, two players through /ws/.');
} finally {
  for (const socket of sockets) socket.close();
}
