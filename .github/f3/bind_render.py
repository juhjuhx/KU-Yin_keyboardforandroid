"""After a successful real record only, retain its explicit generated metadata patch."""
from pathlib import Path
import hashlib
import json
import os
import subprocess

root = Path.cwd()
manifest_path = 'docs/community/media/ROBORAZZI_RENDER_MANIFEST.json'
p = root/manifest_path
manifest = json.loads(p.read_text())
files = [entry['path'] for entry in manifest['source_files']]
output = manifest['output']['path']
digest = lambda path: hashlib.sha256((root/path).read_bytes()).hexdigest()
# Never silently replace a changed picture or weaken its existing output oracle.
assert digest(output) == manifest['output']['sha256']
manifest['source_commit'] = os.environ['GITHUB_SHA']
manifest['command'] = './gradlew :app:recordRoborazziDebug --tests com.example.GreetingScreenshotTest --no-build-cache --rerun-tasks --stacktrace'
manifest['exit'] = 0
manifest['environment']['configuration'] = 'Ubuntu GitHub runner / Robolectric NativeGraphics / Pixel8 JVM test config; no Android device'
for entry in manifest['source_files']:
    entry['sha256'] = digest(entry['path'])
manifest['provenance_note'] = 'F3 20006: real CI record before verification. This generated metadata-only patch must be reviewed and committed before final clean-head CI.'
p.write_text(json.dumps(manifest,ensure_ascii=False,indent=2)+'\n')
changed = subprocess.check_output(['git','diff','--name-only'],text=True).splitlines()
assert changed == [manifest_path], changed
patch = subprocess.check_output(['git','diff','--',manifest_path])
Path('render-metadata.patch').write_bytes(patch)
Path('render-evidence.json').write_text(json.dumps(dict(source_commit=os.environ['GITHUB_SHA'],
    command=manifest['command'],exit=0,files={name:digest(name) for name in files+[output]},
    generated_metadata_path=manifest_path,generated_metadata_patch_sha256=hashlib.sha256(patch).hexdigest(),
    source_tree_scope='Git HEAD plus explicit generated metadata patch only; no product-source mutation'),indent=2)+'\n')
