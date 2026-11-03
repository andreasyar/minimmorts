#!/usr/bin/env python3
"""Compile project files and package a distribution without Docker images or sprites."""
import argparse
import hashlib
import json
from pathlib import Path
import re
import shutil
import subprocess
import tarfile
import tempfile
import xml.etree.ElementTree as ET

ROOT = Path(__file__).resolve().parents[1]


def run(*args, **kwargs):
    subprocess.run(args, cwd=ROOT, check=True, **kwargs)


def main():
    argparse.ArgumentParser(description=__doc__).parse_args()
    version = json.loads((ROOT / 'client/package.json').read_text())['version']
    if not re.fullmatch(r'\d+\.\d+\.\d+', version):
        raise SystemExit('Expected a release version: major.minor.patch')
    lock = json.loads((ROOT / 'client/package-lock.json').read_text())
    pom = ET.parse(ROOT / 'server/pom.xml').getroot()
    server_version = pom.find('{http://maven.apache.org/POM/4.0.0}version').text
    compose = (ROOT / 'release/compose.yaml').read_text()
    if (server_version != version or lock['version'] != version
            or lock['packages']['']['version'] != version
            or f'x-project-version: {version}\n' not in compose):
        raise SystemExit('Client, lockfile, Maven and Compose versions must match')

    output = ROOT / 'release/dist'
    output.mkdir(exist_ok=True)
    name = f'minimmorts-{version}'
    archive = output / f'{name}.tar.gz'
    if archive.exists():
        raise SystemExit(f'Refusing to overwrite {archive}')
    with tempfile.TemporaryDirectory(dir=output) as staging:
        bundle = Path(staging) / name
        (bundle / 'config').mkdir(parents=True)
        (bundle / 'sprites').mkdir()
        run('docker', 'build', '--file', 'release/build.Dockerfile',
            '--output', f'type=local,dest={bundle}', '.')
        shutil.copyfile(ROOT / 'release/compose.yaml', bundle / 'compose.yaml')
        shutil.copyfile(ROOT / 'server/config.xml', bundle / 'config/server.xml')
        shutil.copyfile(ROOT / 'release/README.md', bundle / 'README.md')
        shutil.copyfile(ROOT / 'release/nginx.conf', bundle / 'config/nginx.conf')
        for service in ('web', 'server'):
            shutil.copyfile(ROOT / f'release/{service}.Dockerfile', bundle / f'{service}.Dockerfile')
        # Only these runtime inputs are sent to Docker when the user builds.
        (bundle / '.dockerignore').write_text(
            '*\n!client/\n!client/**\n!server/\n!server/minimmorts-server.jar\n'
            '!config/\n!config/**\n!web.Dockerfile\n!server.Dockerfile\n'
            'client/sprites/\n')
        resources = (ROOT / 'client/src/js/resources.js').read_text()
        names = sorted(set(re.findall(r'src: "sprites/([^"]+)"', resources)))
        (bundle / 'SPRITES.txt').write_text('\n'.join(names) + '\n')
        checksums = []
        for path in sorted(bundle.rglob('*')):
            if path.is_file():
                with path.open('rb') as source:
                    digest = hashlib.file_digest(source, 'sha256').hexdigest()
                checksums.append(f'{digest}  {path.relative_to(bundle).as_posix()}\n')
        (bundle / 'SHA256SUMS').write_text(''.join(checksums))
        temporary_archive = Path(staging) / archive.name
        with tarfile.open(temporary_archive, 'w:gz') as target:
            target.add(bundle, arcname=name)
        temporary_archive.replace(archive)
    print(archive)


if __name__ == '__main__':
    main()
