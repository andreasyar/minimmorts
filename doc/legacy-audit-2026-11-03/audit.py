#!/usr/bin/env python3
"""Read-only comparison of local legacy material against every local Git blob.

Run from repository root; writes only evidence.json beside this script.
Only CRLF is normalized, without trimming or otherwise changing source code.
"""
import collections
import hashlib
import json
from pathlib import Path
import re
import sqlite3
import subprocess
import zipfile

ROOT = Path('/home/andrey/src/minimmorts-legacy')
OUT = Path(__file__).resolve().parent

def git(*args):
    return subprocess.check_output(['git', *args])

def digest(data):
    return hashlib.sha256(data.replace(b'\r\n', b'\n')).hexdigest()

objects = git('cat-file', '--batch-all-objects', '--batch-check=%(objectname) %(objecttype)').decode().splitlines()
blob_ids = [line.split()[0] for line in objects if line.endswith(' blob')]
blobs = {oid: git('cat-file', 'blob', oid) for oid in blob_ids}
by_hash = collections.defaultdict(list)
for oid, data in blobs.items():
    by_hash[digest(data)].append(oid)
head_ids = {line.split()[2].decode() for line in git('ls-tree', '-r', 'HEAD').splitlines()}
head_hashes = {digest(blobs[oid]) for oid in head_ids}
reachable_ids = {line.split()[0] for line in git('rev-list', '--objects', '--all').decode().splitlines()}
paths_by_blob = {}
for line in git('rev-list', '--objects', '--all').decode().splitlines():
    if ' ' in line:
        oid, path = line.split(' ', 1)
        paths_by_blob[oid] = path

EXTENSIONS = {'.java', '.js', '.as', '.html', '.htm', '.css', '.sh', '.ps1', '.sql', '.proto', '.xml', '.properties', '.mf', '.as3proj'}
SKIP = {'lib', 'build', 'dist', 'javadoc', 'docs', 'node_modules'}
records = []
content = {}
def add(path, data, kind):
    parts = Path(path.split('!/')[-1]).parts
    if any(part in SKIP for part in parts):
        return
    if '.svn' in parts and 'text-base' not in parts:
        return
    name = parts[-1]
    for suffix in ('.svn-base', '.netbeans-base'):
        if name.endswith(suffix):
            name = name[:-len(suffix)]
    if Path(name).suffix.lower() not in EXTENSIONS:
        return
    h = digest(data)
    content[h] = data
    matches = by_hash.get(h, [])
    records.append(dict(path=path, kind=kind, sha256_lf=h, bytes=len(data),
                        status=('head' if h in head_hashes else 'history' if any(o in reachable_ids for o in matches) else 'unreachable' if matches else 'absent'),
                        git_blobs=matches))

svn = []
for p in sorted(ROOT.rglob('*')):
    if not p.is_file():
        continue
    rel = p.relative_to(ROOT).as_posix()
    if p.name == 'wc.db':
        con = sqlite3.connect(p.as_uri() + '?mode=ro', uri=True)
        nodes = con.execute('select local_relpath, revision, checksum from nodes where kind="file"').fetchall()
        wc = dict(path=rel, repositories=con.execute('select root,uuid from repository').fetchall(),
                  revisions=sorted({r for _,r,_ in nodes if r is not None}), modifications=[])
        svn.append(wc)
        for local, revision, checksum in nodes:
            if checksum and checksum.startswith('$sha1$'):
                sha = checksum[6:]
                pristine = p.parent / 'pristine' / sha[:2] / (sha + '.svn-base')
                if pristine.exists():
                    add(rel + '!/' + local, pristine.read_bytes(), 'svn-pristine')
                    working = p.parent.parent / local
                    if working.is_file() and digest(working.read_bytes()) != digest(pristine.read_bytes()):
                        wc['modifications'].append(local)
        con.close()
    if p.suffix == '.zip' and p.name == 'minimmorts-source-archive.zip':
        with zipfile.ZipFile(p) as z:
            for info in z.infolist():
                if not info.is_dir():
                    add(rel + '!/' + info.filename, z.read(info), 'google-code-zip')
    if '.svn' not in p.parts or 'text-base' in p.parts:
        add(rel, p.read_bytes(), 'svn-text-base' if '.svn' in p.parts else 'working-file')

commits = []
for entry in git('log', '--all', '--format=%H%x00%aI%x00%B%x00%x1e').decode().split('\x1e'):
    fields = entry.strip().split('\0')
    if len(fields) < 3:
        continue
    oid, date, body = fields[:3]
    m = re.search(r'git-svn-id: (.*?)@(\d+) ([\w-]+)', body)
    commits.append(dict(commit=oid, date=date, svn_url=m[1] if m else None, revision=int(m[2]) if m else None))
log = (ROOT / 'svn-archive/minimmorts-svn-log.txt').read_text()
log_revs = sorted(set(map(int, re.findall(r'^r(\d+) \|', log, re.M))))
git_revs = sorted({c['revision'] for c in commits if c['revision'] is not None})
history_checks = []
for match in re.finditer(r'^r(\d+) \| (.*?) \| (.*?)\n\n(.*?)(?=\n-{10,}\n|\Z)', log, re.M | re.S):
    rev, author, date, message = match.groups()
    for commit in (c for c in commits if c['revision'] == int(rev)):
        body = git('show', '-s', '--format=%B', commit['commit']).decode()
        body = re.sub(r'git-svn-id:.*', '', body).strip()
        expected_message = '' if message.strip() == '(empty log message)' else message.strip()
        history_checks.append(dict(revision=int(rev), commit=commit['commit'],
                                   message_matches=body==expected_message,
                                   author_matches=(None if author == '(author not recorded)' else git('show', '-s', '--format=%ae', commit['commit']).decode().strip()==author),
                                   date_matches=commit['date'][:19]==date[:19]))
absent = collections.defaultdict(list)
for record in records:
    if record['status'] == 'absent':
        absent[record['sha256_lf']].append(record['path'])
nearest = []
for h, paths in absent.items():
    import difflib
    data = content[h].replace(b'\r\n', b'\n').decode('utf-8', 'replace')
    name = Path(paths[0]).name
    candidates = [oid for oid in blobs if Path(paths_by_blob.get(oid, '')).name == name] or list(blobs)
    best = max(candidates, key=lambda oid: difflib.SequenceMatcher(None, data, blobs[oid].replace(b'\r\n', b'\n').decode('utf-8', 'replace')).ratio())
    original = blobs[best].replace(b'\r\n', b'\n').decode('utf-8', 'replace')
    nearest.append(dict(sha256_lf=h, paths=paths, nearest_blob=best,
                        diff=''.join(difflib.unified_diff(original.splitlines(True), data.splitlines(True), fromfile=best, tofile=paths[0]))))
pristines = list(ROOT.rglob('pristine/*/*.svn-base'))
archive_trees = []
with zipfile.ZipFile(ROOT / 'svn-archive/minimmorts-source-archive.zip') as z:
    for prefix, ref in [('minimmorts/trunk/', 'origin/main'),
                        ('minimmorts/branches/lag-compenstation/', 'origin/lag-compenstation'),
                        ('minimmorts/branches/no-lag-compensation/', 'origin/no-lag-compensation'),
                        ('minimmorts/branches/reinit-late-clients/', 'origin/reinit-late-clients'),
                        ('minimmorts/wiki/', 'origin/wiki')]:
        actual = {n[len(prefix):]: digest(z.read(n)) for n in z.namelist()
                  if n.startswith(prefix) and not n.endswith('/') and '/.svn/' not in n}
        expected = {}
        for line in git('ls-tree', '-rz', ref).split(b'\0'):
            if line:
                header, path = line.split(b'\t', 1)
                expected[path.decode()] = digest(blobs[header.split()[2].decode()])
        archive_trees.append(dict(ref=ref, archive_prefix=prefix, archive_files=len(actual), git_files=len(expected),
                                  missing=sorted(expected.keys()-actual.keys()), extra=sorted(actual.keys()-expected.keys()),
                                  different=sorted(p for p in expected.keys() & actual.keys() if expected[p] != actual[p])))
result = dict(head=git('rev-parse', 'HEAD').decode().strip(),
              archive_trees=archive_trees,
              all_pristine_count=len(pristines),
              unmatched_pristines=[str(p.relative_to(ROOT)) for p in pristines if digest(p.read_bytes()) not in by_hash],
              fsck_output=git('fsck', '--full', '--no-reflogs').decode(),
              source_archive_sha256=hashlib.sha256((ROOT/'svn-archive/minimmorts-source-archive.zip').read_bytes()).hexdigest(),
              refs=git('show-ref').decode().splitlines(), blob_count=len(blobs), commit_count=len(commits),
              reachable_blob_count=len(set(blobs) & reachable_ids),
              counts=dict(collections.Counter(r['status'] for r in records)),
              by_kind={k: dict(collections.Counter(r['status'] for r in records if r['kind']==k)) for k in sorted({r['kind'] for r in records})},
              unique_content_count=len(content), unique_absent_count=len(absent),
              svn_log_revisions=log_revs, git_svn_revisions=git_revs,
              missing_revisions=sorted(set(log_revs)-set(git_revs)), svn_working_copies=svn,
              history_checks=history_checks,
              commits=commits, unmatched=nearest, files=records)
(OUT / 'evidence.json').write_text(json.dumps(result, ensure_ascii=False, indent=2)+'\n')
print(json.dumps({k: v for k,v in result.items() if k not in {'files', 'unmatched', 'commits', 'refs', 'svn_working_copies', 'history_checks'}}, ensure_ascii=False, indent=2))
for item in nearest:
    print('\nUNMATCHED', len(item['paths']), item['paths'][0], '\n', item['diff'][:6000])
