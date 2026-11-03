// The legacy client uses classic scripts and arguments.callee. Preserve script
// ordering and non-strict execution rather than converting it to ES modules.
import { cp, mkdir, readFile, readdir, rm, copyFile } from 'node:fs/promises';
import { fileURLToPath } from 'node:url';
import path from 'node:path';
import vm from 'node:vm';

const root = fileURLToPath(new URL('../', import.meta.url));
const source = path.join(root, 'src');
const output = path.join(root, 'dist');
for (const file of await readdir(path.join(source, 'js'))) {
  if (file.endsWith('.js')) {
    new vm.Script(await readFile(path.join(source, 'js', file), 'utf8'), { filename: file });
  }
}
const html = await readFile(path.join(source, 'client.html'), 'utf8');
for (const match of html.matchAll(/<script\b[^>]*>([\s\S]*?)<\/script>/gi)) {
  new vm.Script(match[1], { filename: 'client.html' });
}
for (const match of html.matchAll(/<script\b[^>]*\bsrc="([^"]+)"/gi)) {
  await readFile(path.join(source, match[1]));
}
await mkdir(output, { recursive: true });
// Keep the volume's mount point; only remove its contents.
for (const entry of await readdir(output)) await rm(path.join(output, entry), { recursive: true, force: true });
await cp(source, output, { recursive: true });
await copyFile(path.join(output, 'client.html'), path.join(output, 'index.html'));
console.log('Client built in dist/ (classic scripts and local assets preserved).');
