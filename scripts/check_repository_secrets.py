#!/usr/bin/env python3
"""Local credential tripwire, not proof that a repository contains no secrets.

Reads only Git tracked files or reachable Git blob objects. Never prints values,
contacts a credential provider, follows worktree symlinks, or scans the home dir.
"""
import argparse
import json
from pathlib import Path
import re
import subprocess

LIMIT = 2 * 1024 * 1024
RULES = {
    'PRIVATE_KEY': rb'-----BEGIN (?:RSA |EC |DSA |OPENSSH |ENCRYPTED )?PRIVATE KEY-----',
    'GITHUB_TOKEN': rb'(?<![A-Za-z0-9_])(?:gh[pousr]_[A-Za-z0-9]{36,255}|github_pat_[A-Za-z0-9_]{60,255})(?![A-Za-z0-9_])',
    'AWS_ACCESS_KEY': rb'(?<![A-Z0-9])(?:AKIA|ASIA)[A-Z0-9]{16}(?![A-Z0-9])',
    'GOOGLE_API_KEY': rb'(?<![A-Za-z0-9_-])AIza[A-Za-z0-9_-]{35}(?![A-Za-z0-9_-])',
    'OPENAI_TOKEN': rb'(?<![A-Za-z0-9_-])sk-(?:proj-|svcacct-)?[A-Za-z0-9_-]{40,255}(?![A-Za-z0-9_-])',
}
PRIVATE_SUFFIXES = {'.jks', '.keystore', '.p12', '.pfx', '.key'}


def find_secrets(data):
    return [{'rule': rule, 'line': data[:m.start()].count(b'\n') + 1}
            for rule, pattern in RULES.items() for m in re.finditer(pattern, data)]


def git(root, *args):
    return subprocess.check_output(['git', *args], cwd=root)


def private_path(path):
    p = Path(path)
    return (p.suffix.lower() in PRIVATE_SUFFIXES or
            (p.name.startswith('.env') and p.name not in {'.env.example', '.env.template'}) or
            p.name in {'key.properties', 'keystore.properties'})


def scan_repository(root, history=False):
    root = Path(root).resolve()
    findings, skipped, scanned = [], [], 0
    def inspect(path, data, blob=None):
        nonlocal scanned
        if private_path(path):
            findings.append({'path': path, 'blob': blob, 'rule': 'PRIVATE_FILENAME'});return
        if len(data) > LIMIT or b'\0' in data:
            skipped.append({'path': path, 'blob': blob, 'reason': 'LARGE_OR_BINARY'});return
        scanned += 1
        findings.extend(dict(row, path=path, blob=blob) for row in find_secrets(data))
    if not history:
        for raw in git(root, 'ls-files', '-z').split(b'\0'):
            if not raw: continue
            name = raw.decode('utf-8', errors='surrogateescape');path = root / name
            if private_path(name):
                findings.append({'path': name, 'rule': 'PRIVATE_FILENAME'});continue
            if path.is_symlink() or not path.is_file():
                skipped.append({'path': name, 'reason': 'SYMLINK_OR_MISSING'});continue
            if path.stat().st_size > LIMIT:
                skipped.append({'path': name, 'reason': 'LARGE'});continue
            inspect(name, path.read_bytes())
    else:
        # rev-list --objects emits one representative path per blob. Inspect
        # changed paths separately so a private alias cannot hide behind a safe name.
        history_paths = git(root, 'log', '--all', '--root', '--format=', '--name-only', '--no-renames', '-z')
        for raw in set(history_paths.split(b'\0')):
            name = raw.decode('utf-8', errors='replace').strip('\n')
            if name and private_path(name): findings.append({'path': name, 'rule': 'PRIVATE_HISTORICAL_FILENAME'})
        names = {}
        for line in git(root, 'rev-list', '--objects', '--all').decode('utf-8', errors='replace').splitlines():
            oid, _, path = line.partition(' ')
            if path:
                if private_path(path): findings.append({'path': path, 'blob': oid, 'rule': 'PRIVATE_FILENAME'})
                names.setdefault(oid, path)
        # One batch-check, then bounded synchronous reads avoid unbounded IPC queues.
        info = subprocess.check_output(['git', 'cat-file', '--batch-check'], cwd=root,
                                       input=''.join(oid + '\n' for oid in names).encode())
        with subprocess.Popen(['git', 'cat-file', '--batch'], cwd=root, stdin=subprocess.PIPE, stdout=subprocess.PIPE) as proc:
            for line in info.decode().splitlines():
                oid, kind, size = line.split();path = names[oid]
                if kind != 'blob': continue
                if private_path(path):
                    findings.append({'path': path, 'blob': oid, 'rule': 'PRIVATE_FILENAME'});continue
                if int(size) > LIMIT:
                    skipped.append({'path': path, 'blob': oid, 'reason': 'LARGE'});continue
                proc.stdin.write((oid + '\n').encode());proc.stdin.flush()
                header = proc.stdout.readline().split();assert header[0].decode() == oid and header[1] == b'blob'
                data = proc.stdout.read(int(header[2]));assert proc.stdout.read(1) == b'\n'
                inspect(path, data, oid)
            proc.stdin.close();assert proc.wait(timeout=30) == 0
    return {'scope': 'REACHABLE_HISTORY' if history else 'TRACKED_WORKTREE', 'scanned_texts': scanned,
            'findings': findings, 'skipped': skipped, 'limitation': 'Pattern tripwire only; binary/large/symlink exclusions are not audited secret-free.'}


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--history', action='store_true')
    args = parser.parse_args()
    result = scan_repository(Path(__file__).resolve().parents[1], args.history)
    print(json.dumps(result, ensure_ascii=True, indent=2))
    return 1 if result['findings'] else 0

if __name__ == '__main__': raise SystemExit(main())
