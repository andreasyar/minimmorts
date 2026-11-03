// Resolve in a temporary directory so updating the lock does not touch the
// shared node_modules volume used by the running dev server.
import { copyFile, mkdtemp, rm } from 'node:fs/promises';
import { tmpdir } from 'node:os';
import path from 'node:path';
import { spawnSync } from 'node:child_process';

const temporary = await mkdtemp(path.join(tmpdir(), 'minimmorts-lock-'));
try {
  await copyFile(new URL('../package.json', import.meta.url), path.join(temporary, 'package.json'));
  try {
    await copyFile(new URL('../package-lock.json', import.meta.url), path.join(temporary, 'package-lock.json'));
  } catch (error) {
    if (error.code !== 'ENOENT') throw error;
  }
  const result = spawnSync('npm', ['install', '--package-lock-only', '--ignore-scripts', '--no-audit', '--no-fund'], { cwd: temporary, stdio: 'inherit' });
  if (result.status !== 0) throw new Error('Lock update failed');
  await copyFile(path.join(temporary, 'package-lock.json'), new URL('../package-lock.json', import.meta.url));
} finally {
  await rm(temporary, { recursive: true, force: true });
}
