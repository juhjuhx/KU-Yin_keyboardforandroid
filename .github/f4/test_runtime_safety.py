"""No ADB: exercise mutation boundaries of the disposable-only runtime continuation."""
import json
import os
from pathlib import Path
import tempfile
import unittest
from unittest.mock import patch
from p0_runtime import run

class RuntimeSafetyTest(unittest.TestCase):
    def receipts(self, directory, h1='HEALTHY'):
        for stage,status in [('H0','HEALTHY'),('H1',h1)]:
            (directory/(stage+'_EFFECTIVE.json')).write_text(json.dumps(dict(status=status)))

    def test_physical_serial_is_rejected_before_any_command(self):
        with tempfile.TemporaryDirectory() as d, patch.dict(os.environ, {'GITHUB_ACTIONS':'true'}):
            def forbidden(*args,**kwargs): self.fail('Physical device command attempted')
            with self.assertRaises(AssertionError): run('owner-phone',Path(d),forbidden,forbidden,'boot')

    def test_inconclusive_h1_never_installs_product(self):
        with tempfile.TemporaryDirectory() as d, patch.dict(os.environ, {'GITHUB_ACTIONS':'true'}):
            out=Path(d);self.receipts(out,h1='INCONCLUSIVE')
            def forbidden(*args,**kwargs): self.fail('Mutation before H1 gate')
            with self.assertRaises(AssertionError): run('emulator-5554',out,forbidden,forbidden,'boot')

    def test_boot_change_stops_before_apk_or_install_work(self):
        with tempfile.TemporaryDirectory() as d, patch.dict(os.environ, {'GITHUB_ACTIONS':'true'}):
            out=Path(d);self.receipts(out);commands=[]
            def command(argv,**kwargs):
                commands.append(argv)
                return dict(exit=0,timed_out=False,stdout='0' if 'get-current-user' in argv else 'different-boot')
            with self.assertRaises(AssertionError): run('emulator-5554',out,command,lambda *a:None,'expected-boot')
            self.assertEqual(2,len(commands))
            self.assertFalse(any('install' in argv for argv in commands))

if __name__=='__main__': unittest.main()
