"""TEST_ONLY: collect CI APK identities and fresh per-ABI relink evidence. No key export."""
from pathlib import Path
import hashlib
import json
import os
import re
import shutil
import subprocess
import sys
import zipfile
import uuid

root = Path.cwd()
assert os.environ.get('GITHUB_REPOSITORY') == 'juhjuhx/KU-Yin_keyboardforandroid'
# Full unit tasks can touch render outputs. Recheck the final tree, not just the binder's earlier view.
manifest_path = 'docs/community/media/ROBORAZZI_RENDER_MANIFEST.json'
assert subprocess.check_output(['git','rev-parse','HEAD'],text=True).strip() == os.environ['GITHUB_SHA']
assert subprocess.check_output(['git','diff','HEAD','--name-only'],text=True).splitlines() == [manifest_path]
assert subprocess.check_output(['git','diff','HEAD','--',manifest_path]) == (root/'render-metadata.patch').read_bytes()
baseline = json.loads(subprocess.check_output(['git','show','HEAD:'+manifest_path],text=True))
assert json.loads((root/manifest_path).read_text())['output'] == baseline['output']
assert hashlib.sha256((root/baseline['output']['path']).read_bytes()).hexdigest() == baseline['output']['sha256']
out = root/'dist'
out.mkdir(exist_ok=False)
native = out/'native'
native.mkdir()
sdk = Path(os.environ['ANDROID_HOME'])
tools = sorted((sdk/'build-tools').glob('*/aapt'))
assert tools
buildtools = tools[-1].parent
ndk = sdk/'ndk/28.2.13676358'
llvm = ndk/'toolchains/llvm/prebuilt/linux-x86_64/bin'
sha = lambda data: hashlib.sha256(data).hexdigest()

def cmd(argv):
    p = subprocess.run([str(a) for a in argv], capture_output=True, text=True, timeout=120)
    return dict(argv=[str(a) for a in argv], exit=p.returncode, stdout=p.stdout, stderr=p.stderr)

def put(name, data):
    p = out/name
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_bytes(data)
    return dict(path=str(p.relative_to(out)), size=len(data), sha256=sha(data))

apks = {}
for label, relative in {
    'app': 'app/build/outputs/apk/debug/app-debug.apk',
    'app_test': 'app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk',
    'host': 'acceptance-host/build/outputs/apk/debug/acceptance-host-debug.apk',
    'host_test': 'acceptance-host/build/outputs/apk/androidTest/debug/acceptance-host-debug-androidTest.apk',
}.items():
    src = root/relative
    signing = cmd([buildtools/'apksigner', 'verify', '--verbose', '--print-certs', src])
    badging = cmd([buildtools/'aapt', 'dump', 'badging', src])
    assert signing['exit'] == badging['exit'] == 0
    certs = re.findall(r'Signer #\d+ certificate SHA-256 digest: ([0-9a-fA-F:]+)', signing['stdout'])
    assert len(certs) == 1
    package = re.search(r"package: name='([^']+)' versionCode='([^']+)' versionName='([^']*)'", badging['stdout'])
    assert package
    data = src.read_bytes()
    with zipfile.ZipFile(src) as z: assert z.testzip() is None
    dest = 'KU-Yin-0.2.0-f3.1-test-v20006-CI-debug.apk' if label == 'app' else label+'.apk'
    apks[label] = dict(**put(dest, data), package=package[1], code=package[2], name=package[3],
                       signer_sha256=certs[0].replace(':','').lower(), signing=signing, badging=badging)
app = apks['app']
min_sdk = re.search(r"sdkVersion:'(\d+)'", app['badging']['stdout'])
target_sdk = re.search(r"targetSdkVersion:'(\d+)'", app['badging']['stdout'])
assert min_sdk and target_sdk and (int(min_sdk[1]), int(target_sdk[1])) == (24,36)
assert (app['package'], app['code'], app['name']) == ('io.github.juhjuhx.kuyin','20006','0.2.0-f3.1-test')
shutil.copy2(root/'.github/f3/relink_native.py', native/'relink_native.py')
abis = ['armeabi-v7a','arm64-v8a','x86','x86_64']
records = []
lib_entries = []
with zipfile.ZipFile(out/app['path']) as apk:
    actual_abis = {name.split('/')[1] for name in apk.namelist() if name.startswith('lib/') and name.endswith('.so')}
    assert actual_abis == set(abis)
    for name in apk.namelist():
        if not name.endswith('/'):
            data = apk.read(name)
            lib_entries.append(dict(path=name, size=len(data), sha256=sha(data)))
    for abi in abis:
        objects = list((root/'app/.cxx').glob('Debug/*/'+abi+'/CMakeFiles/chewing-jni.dir/chewing_jni.cpp.o'))
        assert len(objects) == 1, (abi, objects)
        archive = root/'app/src/main/cpp/.deps/libchewing'/abi/'lib/libchewing_capi.a'
        inputs = native/'relink-inputs'/abi
        inputs.mkdir(parents=True)
        shutil.copy2(objects[0], inputs/'chewing_jni.cpp.o')
        shutil.copy2(archive, inputs/'libchewing_capi.a')
        for name in ['build.ninja','compile_commands.json']:
            src = objects[0].parents[2]/name
            if src.is_file(): shutil.copy2(src, inputs/name)
        packaged = native/'packaged'/abi/'libchewing-jni.so'
        packaged.parent.mkdir(parents=True)
        packaged.write_bytes(apk.read('lib/'+abi+'/libchewing-jni.so'))
        rebuilt = native/'relinked'/abi/'libchewing-jni.so'
        rec = dict(abi=abi, object_sha256=sha(objects[0].read_bytes()), archive_sha256=sha(archive.read_bytes()),
                   packaged_sha256=sha(packaged.read_bytes()), source_object=str(objects[0].relative_to(root)))
        rec['link'] = cmd([sys.executable, native/'relink_native.py', '--ndk', ndk, '--abi', abi, '--output', rebuilt])
        rec['sections'] = {}
        if rec['link']['exit'] == 0:
            for section in ['.text','.rodata']:
                hashes = {}
                for label, binary in [('packaged',packaged),('relinked',rebuilt)]:
                    dst = inputs/(label+section+'.bin')
                    capture = cmd([llvm/'llvm-objcopy', '--dump-section', section+'='+str(dst), binary])
                    hashes[label] = dict(exit=capture['exit'], sha256=sha(dst.read_bytes()) if dst.is_file() else None)
                hashes['match'] = (all(v['exit'] == 0 and v['sha256'] for v in hashes.values()) and
                                   hashes['packaged']['sha256'] == hashes['relinked']['sha256'])
                rec['sections'][section] = hashes
            rec['relinked_sha256'] = sha(rebuilt.read_bytes())
        rec['sections_match'] = rec['link']['exit'] == 0 and all(x['match'] for x in rec['sections'].values())
        records.append(rec)
relink = dict(source=os.environ['GITHUB_SHA'], records=records,
              verdict='SECTION_EQUIVALENT' if all(x['sections_match'] for x in records) else 'NOT_SECTION_EQUIVALENT',
              limitations='Fresh relink of fresh JNI objects/prebuilt archives. Not Rust cross-rebuild, whole-binary equivalence, legal approval or Runtime.')
(out/'NATIVE_RELINK.json').write_text(json.dumps(relink,indent=2)+'\n')
(out/'APK_ASSET_INVENTORY.json').write_text(json.dumps(lib_entries,indent=2)+'\n')
manifest = dict(source_sha=os.environ['GITHUB_SHA'], main_base='8a4890254df8e10235e2efeab05193c2f793aee4',
    channel='CI_TEST_ONLY_NOT_ALPHA_UPGRADE_CANDIDATE', apks=apks,
    min_sdk=int(min_sdk[1]),target_sdk=int(target_sdk[1]),abis=abis,
    original_alpha_signer_match=app['signer_sha256']=='46488e99373f3c1af0c630642f2d9ec459b8db671391f3ad817efb3aae99da94',
    owner_key_transmitted=False, runtime='NOT_EXECUTED', release_authorized=False,
    generated_render_metadata_patch_sha256=sha((root/'render-metadata.patch').read_bytes()),
    source_tree_scope='Git HEAD plus explicit generated render-metadata patch; no product-source modification',
    source_archive_scope='Git HEAD only; apply accompanying render-metadata.patch for exact verified workspace')
with (out/'SOURCE.tar.gz').open('wb') as f: subprocess.run(['git','archive','--format=tar.gz','HEAD'],stdout=f,check=True)
manifest['source_archive_sha256'] = sha((out/'SOURCE.tar.gz').read_bytes())
(out/'BUILD_MANIFEST.json').write_text(json.dumps(manifest,indent=2)+'\n')
resolved = json.loads((root/'runtime-artifacts.json').read_text())
assert resolved['source'] == os.environ['GITHUB_SHA']
shutil.copy2(root/'runtime-artifacts.json', out/'RUNTIME_ARTIFACTS.json')
components = []
for a in resolved['artifacts']:
    ref = 'maven:'+a['group']+':'+a['name']+':'+a['version']+':'+str(a['classifier'])+':'+a['extension']
    components.append({'type':'library','bom-ref':ref,'group':a['group'],'name':a['name'],'version':a['version'],
        'hashes':[{'alg':'SHA-256','content':a['sha256']}],
        'properties':[{'name':'evidence_scope','value':'Resolved debugRuntimeClasspath artifact; license and DEX inclusion not certified'}]})
for item in lib_entries:
    if item['path'].startswith('lib/') and item['path'].endswith('.so'):
        components.append({'type':'library','bom-ref':item['path'],'name':item['path'],
            'hashes':[{'alg':'SHA-256','content':item['sha256']}],
            'properties':[{'name':'license_review','value':'OPEN; static Rust components not exhaustively enumerated'}]})
(out/'SBOM.cdx.json').write_text(json.dumps({'bomFormat':'CycloneDX','specVersion':'1.5',
    'serialNumber':'urn:uuid:'+str(uuid.uuid4()),'version':1,
    'metadata':{'component':{'type':'application','bom-ref':'KU-Yin','name':'KU-Yin','version':app['name'],
        'hashes':[{'alg':'SHA-256','content':app['sha256']} ]},
        'properties':[{'name':'source_commit','value':os.environ['GITHUB_SHA']},
                      {'name':'completeness','value':'INCOMPLETE_LICENSE_RUST_DATA_MEDIA; no release approval'}]},
    'components':components,'compositions':[{'aggregate':'incomplete','assemblies':['KU-Yin']}]},indent=2)+'\n')
put('render-metadata.patch',(root/'render-metadata.patch').read_bytes())
for relative in ['LICENSE','NOTICE','scripts/bootstrap_native_deps.sh','app/src/main/cpp/CMakeLists.txt','app/src/main/cpp/chewing_jni.cpp']:
    put('source-inputs/'+relative,(root/relative).read_bytes())
(out/'SHA256SUMS').write_text(''.join(sha(p.read_bytes())+'  '+str(p.relative_to(out))+'\n' for p in sorted(out.rglob('*')) if p.is_file() and p.name!='SHA256SUMS'))
print('CI_TEST_ONLY',app['sha256'],app['signer_sha256'],relink['verdict'])
if not all(x['sections_match'] for x in records): raise SystemExit(2)
