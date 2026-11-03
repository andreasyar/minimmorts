#!/usr/bin/env python3
"""Inventory legacy data without changing originals or importing assets."""
import collections
import gzip
import hashlib
import json
from pathlib import Path
import subprocess
import tarfile
import zipfile

ROOT = Path('/home/andrey/src/minimmorts-legacy')
OUT = Path(__file__).resolve().parent

def sha(b):
    return hashlib.sha256(b).hexdigest()

def classify(p):
    parts = p.parts
    if '.svn' in parts:
        return 'svn-metadata'
    if 'lib' in parts:
        return 'third-party-libraries'
    if 'screenshots' in parts:
        return 'screenshots'
    if p.suffix == '.png':
        return 'game-sprites'
    if p.name.startswith('sprites-v3.'):
        return 'sprite-archives'
    if p.name.startswith('minimmorts.') and p.suffix == '.txt':
        return 'server-logs'
    if 'svn-archive' in parts:
        return 'original-svn-archive'
    if 'build' in parts or 'dist' in parts:
        return 'generated-builds'
    if p.name in {'config.xml', 'config-home.xml'}:
        return 'server-configs'
    if 'nbproject' in parts or p.name in {'build.xml', 'manifest.mf'}:
        return 'ide-projects'
    if p.suffix in {'.java', '.js', '.as', '.html', '.sh', '.ps1'}:
        return 'source-and-client-pages'
    return 'previous-audit'

objects = subprocess.check_output(['git', 'cat-file', '--batch-all-objects', '--batch-check=%(objectname) %(objecttype)']).decode().splitlines()
git_hashes = {sha(subprocess.check_output(['git', 'cat-file', 'blob', row.split()[0]]))
              for row in objects if row.endswith(' blob')}
categories = collections.defaultdict(list)
details = collections.defaultdict(list)
archives = []
seen_archives = set()
db_candidates = []
total_files = total_bytes = 0
for p in sorted(ROOT.rglob('*')):
    if not p.is_file():
        continue
    b = p.read_bytes()
    h = sha(b)
    rel = p.relative_to(ROOT).as_posix()
    cat = classify(p)
    total_files += 1
    total_bytes += len(b)
    categories[cat].append((h, len(b)))
    if cat in {'game-sprites', 'screenshots', 'server-logs', 'server-configs', 'sprite-archives'} or p.suffix == '.jar':
        details[(cat, h)].append(dict(path=rel, bytes=len(b), sha256=h, in_git_raw=h in git_hashes))
    if p.suffix.lower() in {'.sql', '.dump', '.bak', '.db', '.sqlite', '.sqlite3', '.myd', '.myi', '.ibd'}:
        db_candidates.append(dict(path=rel, category=cat, bytes=len(b), sqlite=b.startswith(b'SQLite format 3\0')))
    if p.suffix in {'.zip', '.jar', '.bz2', '.gz'} and h not in seen_archives:
        seen_archives.add(h)
        if p.suffix in {'.zip', '.jar'}:
            with zipfile.ZipFile(p) as z:
                members = z.namelist()
        elif p.suffix == '.bz2':
            with tarfile.open(p) as t:
                members = t.getnames()
        else:
            expanded = gzip.decompress(b)
            members = [p.stem]
        archives.append(dict(path=rel, sha256=h, bytes=len(b), members=len(members),
                             database_members=[n for n in members if Path(n).suffix.lower() in {'.sql', '.dump', '.bak', '.db', '.sqlite', '.sqlite3', '.myd', '.myi', '.ibd'}],
                             follow_sprite_members=[n for n in members if 'peasant_follow' in n]))

summary = {}
for category, records in sorted(categories.items()):
    unique = dict(records)
    summary[category] = dict(files=len(records), bytes=sum(s for _, s in records),
                             unique_contents=len(unique), unique_bytes=sum(unique.values()))
sprites = ROOT / 'archive-findings-2026-10-25/sprites-v3'
sprite_records = [dict(name=p.name, bytes=p.stat().st_size, sha256=sha(p.read_bytes())) for p in sorted(sprites.glob('*.png'))]
resource_js = Path('client/src/js/resources.js').read_text()
import re
requested = sorted(set(re.findall(r'src:\s*"sprites/([^"]+)"', resource_js)))
present = {p.name for p in sprites.glob('*.png')}
result = dict(root=str(ROOT), total_files=total_files, total_bytes=total_bytes,
              categories=summary, canonical_sprites=sprite_records,
              missing_requested_sprites=sorted(set(requested)-present),
              unexpected_canonical_sprites=sorted(present-set(requested)),
              selected_unique_contents=[dict(category=k[0], sha256=k[1], copies=v) for k,v in details.items()],
              archives=archives, database_candidates=db_candidates)
(OUT / 'data-inventory.json').write_text(json.dumps(result, ensure_ascii=False, indent=2)+'\n')
print(json.dumps({k:v for k,v in result.items() if k not in {'selected_unique_contents', 'archives', 'database_candidates', 'canonical_sprites'}}, ensure_ascii=False, indent=2))
