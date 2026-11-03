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
