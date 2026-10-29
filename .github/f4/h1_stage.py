"""TEST_ONLY: a single original-oracle observation with extra read-only focus evidence."""
import time
from focus_protocol import parse_focus_dump


def observe(capture, oracle, clock=time.monotonic, selected_before=None):
    """One 10s stage, no retries/clicks/focus mutation. Original XML limit remains <=3s."""
    deadline = clock() + 10
    probes = {}
    def run(name, args, limit=3):
        remaining = deadline - clock()
        if remaining <= 0:
            raise TimeoutError('H1 observation budget exhausted before '+name)
        rec = capture.run(name, args, timeout=min(limit, remaining))
        if rec['exit'] != 0 or rec.get('timed_out'):
            raise ValueError('Command unverified: '+name)
        return rec['stdout']
    def window(name):
        remaining = deadline-clock()
        if remaining <= 0: raise TimeoutError('H1 window budget exhausted')
        rec = capture.window(name, timeout=min(3,remaining))
        if rec['exit'] != 0 or rec.get('timed_out'): raise ValueError('Window unverified: '+name)
        return rec['stdout']
    try:
        before_default = run('default-before', ['shell','settings','--user','0','get','secure','default_input_method'])
        before = window('window-before')
        for name in ['focus-before','focus-after']:
            probes[name] = parse_focus_dump(run(name, ['shell','dumpsys','activity',
                'io.github.juhjuhx.kuyin.acceptancehost/.AcceptanceEditorActivity','--kuyin-focus'], 1))
            if name == 'focus-before':
                xml = run('ui-0',['shell','-tt','uiautomator','dump','/dev/tty'])
        after = window('window-after')
        after_default = run('default-after', ['shell','settings','--user','0','get','secure','default_input_method'])
        result = oracle(before, xml, after, before_default if selected_before is None else selected_before, after_default)
        expected_default = before_default if selected_before is None else selected_before
        if result['status'] == 'HEALTHY' and (not before_default.strip() or
                before_default.strip() != expected_default.strip() or
                before_default.strip() != after_default.strip()):
            result = dict(status='INCONCLUSIVE',reasons=['INTERMEDIATE_DEFAULT_IME_CHANGED_OR_UNKNOWN'])
        if result['status'] == 'HEALTHY' and clock() > deadline:
            result = dict(status='INCONCLUSIVE',reasons=['H1_TOTAL_BUDGET_EXCEEDED'])
        required_true = ['editor_is_focused','editor_has_focus','window_has_focus',
            'editor_window_has_focus','editor_attached','root_focus_is_editor','node_input_focused']
        # These sequential observations may disagree; never replace the original XML oracle.
        if result['status'] == 'HEALTHY' and any(p[k] is not True for p in probes.values() for k in required_true):
            result = dict(status='INCONCLUSIVE',reasons=['FOCUS_OBSERVATIONS_NOT_CONSISTENT'])
        return dict(result=result,probes=probes,atomic_snapshot=False)
    except (ValueError, TimeoutError) as exc:
        return dict(result=dict(status='INCONCLUSIVE',reasons=[str(exc)]),probes=probes,atomic_snapshot=False)
