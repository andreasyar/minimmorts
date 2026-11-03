import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import vm from 'node:vm';
import test from 'node:test';

test('classic game scripts preserve the native Map used by Vite', async () => {
  const context = vm.createContext({});
  const nativeMap = vm.runInContext('Map', context);
  const html = await readFile(new URL('../src/client.html', import.meta.url), 'utf8');
  for (const match of html.matchAll(/<script\b[^>]*\bsrc="([^"]+)"/gi)) {
    const source = await readFile(new URL(`../src/${match[1]}`, import.meta.url), 'utf8');
    vm.runInContext(source, context, { filename: match[1] });
  }
  assert.equal(vm.runInContext('Map', context), nativeMap);
  assert.equal(vm.runInContext('new Map([["transport", 1]]).get("transport")', context), 1);
  assert.equal(vm.runInContext('typeof GameMap', context), 'function');
  assert.equal(vm.runInContext('GameMap.getCellXOnMap(3)', context), 96);
});

async function createMapHarness({ complete = false, naturalWidth = 0 } = {}) {
  const images = [];
  const tiles = [];
  const visible = [];
  const context = vm.createContext({
    Image: class {
      constructor() {
        this.complete = complete;
        this.naturalWidth = naturalWidth;
        images.push(this);
      }
    },
    document: {
      createElement() {
        return {
          getContext() {
            return {
              createImageData: (w, h) => ({ data: new Uint8ClampedArray(w * h * 4) }),
              drawImage(image, x, y) {
                assert.ok(image.complete && image.naturalWidth > 0, 'Only loaded textures can be cached');
                tiles.push({ image, x, y });
              },
            };
          },
        };
      },
    },
  });
  for (const file of ['constants', 'resources', 'Sprite', 'SpriteLoader', 'Map']) {
    vm.runInContext(await readFile(new URL(`../src/js/${file}.js`, import.meta.url), 'utf8'), context);
  }
  const map = vm.runInContext('new GameMap(100, 100)', context);
  const camera = { x: 0, y: 0, translateAxis: (x, y) => ({ x, y }) };
  const screen = { drawImage: (...args) => visible.push(args) };
  return { context, map, images, tiles, visible, draw: () => map.draw(screen, camera) };
}

test('map waits for a cold texture, then caches and displays all grass tiles once', async () => {
  const h = await createMapHarness();
  h.draw();
  assert.equal(h.map.macromap, null);
  assert.equal(h.tiles.length, 0);
  assert.equal(h.visible.length, 0);

  h.images[0].complete = true;
  h.images[0].naturalWidth = 32;
  h.draw();
  assert.equal(h.tiles.length, 100 * 100);
  assert.ok(h.visible.length > 0);
  const cache = h.map.macromap;
  h.draw();
  assert.equal(h.map.macromap, cache);
  assert.equal(h.tiles.length, 100 * 100);

  const secondMap = vm.runInContext('new GameMap(100, 100)', h.context);
  assert.equal(secondMap.buildMacroMap(), true);
  assert.equal(h.tiles.length, 2 * 100 * 100);
  assert.equal(h.images.length, 1, 'Reconnect reuses the loaded texture');
});

test('map displays a texture already loaded from the browser cache', async () => {
  const h = await createMapHarness({ complete: true, naturalWidth: 32 });
  h.draw();
  assert.equal(h.tiles.length, 100 * 100);
  assert.ok(h.visible.length > 0);
});

test('failed texture does not leave a permanently empty map cache', async () => {
  const h = await createMapHarness({ complete: true, naturalWidth: 0 });
  h.draw();
  assert.equal(h.map.macromap, null);
  assert.equal(h.tiles.length, 0);
  assert.equal(h.visible.length, 0);
});
