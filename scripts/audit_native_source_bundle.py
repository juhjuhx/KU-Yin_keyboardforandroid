#!/usr/bin/env python3
"""Offline source-bundle inventory. Never extracts, executes code or certifies shipped linkage."""
import argparse
import hashlib
import json
import re
from pathlib import Path, PurePosixPath
import tarfile
import tomllib

PREFIX = 'source-and-relink/libchewing/'


def audit(archive, expected_sha256):
    archive = Path(archive)
    with archive.open('rb') as source:
        digest = hashlib.file_digest(source, 'sha256').hexdigest()
        if digest != expected_sha256:
            raise ValueError('Source archive identity mismatch')
        hashes, sizes, metadata, names = {}, {}, {}, set()
        source.seek(0)
        with tarfile.open(fileobj=source, mode='r|gz') as tar:
            for member in tar:
                relative = PurePosixPath(member.name)
                if relative.is_absolute() or '..' in relative.parts or str(relative) in names:
                    raise ValueError('Unsafe or duplicate archive member')
                names.add(str(relative))
                if not member.isfile() or not member.name.startswith(PREFIX):
                    continue
                tail = member.name[len(PREFIX):]
                if tail != 'Cargo.lock' and not tail.startswith('vendor/'):
                    continue
                stream = tar.extractfile(member)
                h = hashlib.sha256()
                keep = tail == 'Cargo.lock' or tail.endswith('/Cargo.toml') or tail.endswith('/.cargo-checksum.json')
                chunks = []
                for chunk in iter(lambda: stream.read(65536), b''):
                    h.update(chunk)
                    if keep:
                        chunks.append(chunk)
                        if sum(map(len,chunks)) > 4*1024*1024:
                            raise ValueError('Metadata file exceeds bounded parser size')
                hashes[member.name], sizes[member.name] = h.hexdigest(), member.size
                if keep:
                    metadata[member.name] = b''.join(chunks)
    lock = tomllib.loads(metadata[PREFIX+'Cargo.lock'].decode())
    registry = {}
    for package in lock['package']:
        if package.get('source','').startswith('registry+'):
            key = (package['name'],package['version'])
            if key in registry: raise ValueError('Ambiguous registry package')
            registry[key] = package['checksum']
    components, found, verified = [], set(), 0
    for member, data in sorted(metadata.items()):
        tail = member[len(PREFIX):]
        if not tail.startswith('vendor/') or tail.count('/') != 2 or not tail.endswith('/Cargo.toml'):
            continue
        package = tomllib.loads(data.decode())['package']
        key = (package['name'],package['version'])
        if key in found or key not in registry: raise ValueError('Unexpected vendor identity')
        found.add(key)
        base = member.rsplit('/',1)[0]+'/'
        checks = json.loads(metadata[base+'.cargo-checksum.json'])
        if checks['package'] != registry[key]: raise ValueError('Registry checksum mismatch')
        for name, expected in checks['files'].items():
            path = PurePosixPath(name)
            if path.is_absolute() or '..' in path.parts or hashes.get(base+name) != expected:
                raise ValueError('Vendor file checksum mismatch')
            verified += 1
        declared = package.get('license-file')
        if declared and (not isinstance(declared,str) or PurePosixPath(declared).is_absolute() or '..' in PurePosixPath(declared).parts):
            raise ValueError('Unsafe declared license-file')
        declared_member = base+str(PurePosixPath(declared)) if declared else None
        license_names = [name for name in sorted(hashes) if name.startswith(base) and
            re.fullmatch(r'(?:LICEN[CS]E|UNLICENSE|COPYING|COPYRIGHT|NOTICE)(?:[._-].*)?',PurePosixPath(name).name,re.I)]
        if declared_member in hashes and declared_member not in license_names:
            license_names.append(declared_member)
        license_files = [dict(member=name,size=sizes[name],sha256=hashes[name]) for name in license_names]
        components.append(dict(name=key[0],version=key[1],registry_checksum=registry[key],source_manifest=member,
            source_manifest_sha256=hashes[member],license_expression=package.get('license'),license_file_declaration=package.get('license-file'),
            license_files=license_files,declared_license_status='PRESENT' if declared_member in hashes else 'MISSING' if declared else 'NOT_DECLARED',
            license_text_status='PRESENT' if license_files else 'MISSING_STANDALONE_LICENSE_TEXT',
            binary_linkage='NOT_PROVEN',declared_file_hashes_verified=len(checks['files'])))
    if not registry or found != set(registry): raise ValueError('Vendor and lock identity sets differ')
    return dict(schema=1,source_archive_sha256=digest,source_prefix=PREFIX,lock_packages=len(lock['package']),
        registry_packages=len(registry),verified_vendor_files=verified,components=components,
        actual_binary_linkage='NOT_PROVEN',release_ready=False,
        limitations=['Vendor candidates are not an Android selected/link graph','Rust std/toolchain, dictionary and media rights not covered','Presence of license text is not legal approval'])


def main():
    parser=argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--archive',type=Path,required=True)
    parser.add_argument('--expected-sha256',required=True)
    parser.add_argument('--output',type=Path,required=True)
    args=parser.parse_args()
    report=audit(args.archive,args.expected_sha256)
    with args.output.open('x') as stream: json.dump(report,stream,indent=2);stream.write('\n')
    print('SOURCE_INVENTORY_ONLY',report['registry_packages'],report['verified_vendor_files'])

if __name__=='__main__':main()
