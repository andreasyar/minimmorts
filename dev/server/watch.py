"""Rebuild on source/config changes; keep the last working JVM on compile errors."""
import hashlib
import os
from pathlib import Path
import shutil
import signal
import subprocess
import time

ROOT = Path('/workspace/server')
RUN = Path('/run/minimmorts')
child = None
stopping = False


def stop_child():
    global child
    if child is not None and child.poll() is None:
        os.killpg(child.pid, signal.SIGTERM)
        try:
            child.wait(timeout=10)
        except subprocess.TimeoutExpired:
            os.killpg(child.pid, signal.SIGKILL)
            child.wait()
    child = None


def shutdown(signum, frame):
    global stopping
    stopping = True
    stop_child()


def fingerprint():
    digest = hashlib.sha256()
    paths = [ROOT / 'pom.xml', ROOT / 'config.xml', *sorted((ROOT / 'src').rglob('*'))]
    for path in paths:
        if path.is_file():
            digest.update(str(path.relative_to(ROOT)).encode())
            digest.update(path.read_bytes())
    return digest.digest()


signal.signal(signal.SIGTERM, shutdown)
signal.signal(signal.SIGINT, shutdown)
RUN.mkdir(parents=True, exist_ok=True)
previous = None
try:
    while not stopping:
        current = fingerprint()
        if current != previous:
            previous = current
            print('Source changed: building server...', flush=True)
            build = subprocess.Popen(['mvn', '-B', '-ntp', 'clean', 'package'], cwd=ROOT, start_new_session=True)
            # Keep the build interruptible when Compose stops the container.
            while build.poll() is None and not stopping:
                time.sleep(0.2)
            if stopping:
                if build.poll() is None:
                    os.killpg(build.pid, signal.SIGTERM)
                    build.wait()
                break
            if build.returncode == 0:
                stop_child()
                shutil.copy2(Path('/build/server/target/minimmorts-server.jar'), RUN / 'server.jar')
                child = subprocess.Popen(
                    ['java', '-jar', str(RUN / 'server.jar'), str(ROOT / 'config.xml')],
                    cwd=RUN, start_new_session=True,
                )
                print('Server started. Restart resets the in-memory game.', flush=True)
            else:
                print('Build failed; keeping the previous server. Fix and save a source file to retry.', flush=True)
        if child is not None and child.poll() is not None:
            print(f'Server exited with code {child.returncode}; waiting for source changes.', flush=True)
            child = None
        time.sleep(1)
finally:
    stop_child()
