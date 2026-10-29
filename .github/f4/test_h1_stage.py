import unittest
from h1_stage import observe
from test_focus_protocol import RECORD

class Capture:
    def __init__(self, focus=RECORD, fail=None): self.calls=[];self.focus=focus;self.fail=fail
    def run(self,name,args,timeout):
        self.calls.append((name,args,timeout))
        return dict(exit=1 if name==self.fail else 0,timed_out=False,stdout=self.focus if name.startswith('focus-') else '<XML>' if name=='ui-0' else 'ime')
    def window(self,name,timeout): return self.run(name,['window'],timeout)

class StageTest(unittest.TestCase):
    def test_read_only_bounded_original_oracle_receives_actual_observations(self):
        seen=[]
        def oracle(*args):seen.append(args);return dict(status='HEALTHY',reasons=[])
        c=Capture();result=observe(c,oracle,clock=lambda:0)
        self.assertEqual('HEALTHY',result['result']['status'])
        self.assertEqual([('ime','<XML>','ime')],[(x[0],x[1],x[3]) for x in seen])
        self.assertTrue(all(0<t<=3 for _,_,t in c.calls))
        self.assertEqual(1,sum(n=='ui-0' for n,_,_ in c.calls))
        self.assertFalse(any('tap' in args or 'start' in args or 'install' in args for _,args,_ in c.calls))
    def test_prelaunch_default_is_preserved_for_original_oracle(self):
        seen=[]
        def oracle(*args): seen.append(args);return dict(status='INCONCLUSIVE')
        observe(Capture(),oracle,selected_before='original-ime')
        self.assertEqual('original-ime',seen[0][3])
    def test_intermediate_default_change_is_not_discarded(self):
        class Changed(Capture):
            def run(self,name,args,timeout):
                record=super().run(name,args,timeout)
                if name=='default-before':record['stdout']='changed-ime'
                return record
        self.assertEqual('INCONCLUSIVE',observe(Changed(),lambda *a:dict(status='HEALTHY'),clock=lambda:0,selected_before='ime')['result']['status'])
    def test_last_command_overrun_cannot_be_healthy(self):
        times=iter([0]*8+[11])
        self.assertEqual('INCONCLUSIVE',observe(Capture(),lambda *a:dict(status='HEALTHY'),clock=lambda:next(times))['result']['status'])
    def test_original_failure_never_promoted(self):
        self.assertEqual('UNHEALTHY',observe(Capture(),lambda *a:dict(status='UNHEALTHY',reasons=['original']))['result']['status'])
    def test_unfocused_probe_can_only_downgrade(self):
        c=Capture(RECORD.replace('editor_is_focused=true','editor_is_focused=false'))
        self.assertEqual('INCONCLUSIVE',observe(c,lambda *a:dict(status='HEALTHY'))['result']['status'])
    def test_missing_probe_is_inconclusive(self):
        self.assertEqual('INCONCLUSIVE',observe(Capture(''),lambda *a:dict(status='HEALTHY'))['result']['status'])
    def test_failed_xml_is_not_retried(self):
        c=Capture(fail='ui-0');self.assertEqual('INCONCLUSIVE',observe(c,lambda *a:dict(status='HEALTHY'))['result']['status'])
        self.assertEqual(1,sum(n=='ui-0' for n,_,_ in c.calls))
    def test_expired_budget_does_not_launch_commands(self):
        times=iter([0,11]);c=Capture();self.assertEqual('INCONCLUSIVE',observe(c,lambda *a:dict(status='HEALTHY'),clock=lambda:next(times))['result']['status']);self.assertEqual([],c.calls)
if __name__=='__main__':unittest.main()
