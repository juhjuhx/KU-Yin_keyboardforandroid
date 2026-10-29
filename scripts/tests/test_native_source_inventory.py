"""Synthetic source archive tests; no real dictionary, APK or private data."""
import hashlib,importlib.util,io,json,tarfile,tempfile,tomllib,unittest
from pathlib import Path
from unittest.mock import patch
spec=importlib.util.spec_from_file_location('audit_native',Path(__file__).parents[1]/'audit_native_source_bundle.py')
module=importlib.util.module_from_spec(spec);spec.loader.exec_module(module)
ROOT='source-and-relink/libchewing/'

class InventoryTest(unittest.TestCase):
 def fixture(self, bad_checksum=False, unsafe=False, extra_license=None):
  temp=tempfile.TemporaryDirectory();self.addCleanup(temp.cleanup);path=Path(temp.name)/'source.tar.gz'
  cargo=b'[package]\nname="fixture"\nversion="1.0.0"\nlicense="MIT"\n'
  package_hash='4'*64
  lock=f'version=4\n[[package]]\nname="fixture"\nversion="1.0.0"\nsource="registry+https://example.invalid/index"\nchecksum="{package_hash}"\n'.encode()
  entries={ROOT+'Cargo.lock':lock,ROOT+'vendor/fixture/Cargo.toml':cargo,ROOT+'vendor/fixture/UNLICENSE':b'Synthetic notice only',ROOT+'vendor/fixture/.cargo-checksum.json':json.dumps({'package':package_hash,'files':{'Cargo.toml':('5'*64 if bad_checksum else hashlib.sha256(cargo).hexdigest()),'UNLICENSE':hashlib.sha256(b'Synthetic notice only').hexdigest()}}).encode()}
  if extra_license:
   relative,declared=extra_license
   if declared:
    cargo += ('license-file="'+relative+'"\n').encode()
    entries[ROOT+'vendor/fixture/Cargo.toml']=cargo
   entries[ROOT+'vendor/fixture/'+relative]=b'Synthetic nested notice'
   checks=json.loads(entries[ROOT+'vendor/fixture/.cargo-checksum.json'])
   checks['files']['Cargo.toml']=hashlib.sha256(cargo).hexdigest()
   checks['files'][relative]=hashlib.sha256(b'Synthetic nested notice').hexdigest()
   entries[ROOT+'vendor/fixture/.cargo-checksum.json']=json.dumps(checks).encode()
  if unsafe:entries[ROOT+'vendor/fixture/../escape']=b'bad'
  with tarfile.open(path,'w:gz') as tar:
   for name,data in entries.items():
    member=tarfile.TarInfo(name);member.size=len(data);tar.addfile(member,io.BytesIO(data))
  return path,hashlib.sha256(path.read_bytes()).hexdigest()
 def test_inventory_preserves_notice_evidence_without_claiming_binary_linkage(self):
  p,h=self.fixture();result=module.audit(p,h)
  self.assertEqual(1,result['registry_packages']);self.assertEqual(2,result['verified_vendor_files'])
  self.assertEqual('MIT',result['components'][0]['license_expression'])
  self.assertEqual('UNLICENSE',Path(result['components'][0]['license_files'][0]['member']).name)
  self.assertEqual('NOT_PROVEN',result['actual_binary_linkage']);self.assertFalse(result['release_ready'])
 def test_nested_license_text_is_not_omitted(self):
  p,h=self.fixture(extra_license=('docs/LICENSE-NESTED',False));result=module.audit(p,h)
  self.assertIn(ROOT+'vendor/fixture/docs/LICENSE-NESTED',[x['member'] for x in result['components'][0]['license_files']])
 def test_declared_nonstandard_license_name_is_resolved(self):
  p,h=self.fixture(extra_license=('docs/terms.txt',True));result=module.audit(p,h)
  self.assertIn(ROOT+'vendor/fixture/docs/terms.txt',[x['member'] for x in result['components'][0]['license_files']])
 def test_parses_same_open_archive_that_was_hashed(self):
  p,h=self.fixture();original_digest=module.hashlib.file_digest
  def replace_after_hash(stream,algorithm):
   digest=original_digest(stream,algorithm)
   replacement=p.with_suffix('.replacement');replacement.write_bytes(b'not the verified archive')
   replacement.replace(p)
   return digest
  with patch.object(module.hashlib,'file_digest',side_effect=replace_after_hash):
   result=module.audit(p,h)
  self.assertEqual(1,result['registry_packages'])
  self.assertEqual(h,result['source_archive_sha256'])
 def test_wrong_archive_identity_rejected(self):
  p,h=self.fixture()
  with self.assertRaises(ValueError):module.audit(p,'0'*64)
 def test_vendor_file_corruption_rejected(self):
  p,h=self.fixture(bad_checksum=True)
  with self.assertRaises(ValueError):module.audit(p,h)
 def test_unsafe_archive_path_rejected_without_extraction(self):
  p,h=self.fixture(unsafe=True)
  with self.assertRaises(ValueError):module.audit(p,h)
if __name__=='__main__':unittest.main()
