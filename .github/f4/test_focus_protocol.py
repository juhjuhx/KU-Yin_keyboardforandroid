"""Deterministic public wire fixtures; never access Android or private typing."""
import unittest
from focus_protocol import parse_focus_dump

RECORD = '''    KUYIN_FOCUS_TEST_ONLY
    editor_is_focused=true
    editor_has_focus=true
    window_has_focus=true
    editor_window_has_focus=true
    editor_attached=true
    editor_focusable_in_touch_mode=true
    root_focus_is_editor=true
    node_input_focused=true
    node_accessibility_focused=false
    last_recorded_connection_present=true
    KUYIN_FOCUS_END
'''

class FocusProtocolTest(unittest.TestCase):
    def test_exact_boolean_record_with_framework_wrapper(self):
        result = parse_focus_dump('ACTIVITY package\n'+RECORD+'\n')
        self.assertIs(True, result['editor_is_focused'])
        self.assertIs(False, result['node_accessibility_focused'])
        self.assertEqual(10, len(result))
    def test_unfocused_is_preserved_not_promoted(self):
        result = parse_focus_dump(RECORD.replace('editor_is_focused=true','editor_is_focused=false'))
        self.assertIs(False, result['editor_is_focused'])
    def test_last_recorded_connection_can_be_unknown_not_active_proof(self):
        self.assertIsNone(parse_focus_dump(RECORD.replace('connection_present=true','connection_present=null'))['last_recorded_connection_present'])
    def test_null_view_state_is_not_allowed(self):
        with self.assertRaises(ValueError): parse_focus_dump(RECORD.replace('editor_attached=true','editor_attached=null'))
    def test_missing_marker(self):
        with self.assertRaises(ValueError): parse_focus_dump(RECORD.replace('KUYIN_FOCUS_TEST_ONLY','WRONG'))
    def test_missing_end(self):
        with self.assertRaises(ValueError): parse_focus_dump(RECORD.replace('KUYIN_FOCUS_END',''))
    def test_duplicate_marker(self):
        with self.assertRaises(ValueError): parse_focus_dump(RECORD+RECORD)
    def test_missing_field(self):
        with self.assertRaises(ValueError): parse_focus_dump(RECORD.replace('    editor_attached=true\n',''))
    def test_duplicate_field(self):
        with self.assertRaises(ValueError): parse_focus_dump(RECORD.replace('    editor_attached=true\n','    editor_attached=true\n'*2))
    def test_unknown_field(self):
        with self.assertRaises(ValueError): parse_focus_dump(RECORD.replace('    KUYIN_FOCUS_END','    text=DO_NOT_COLLECT\n    KUYIN_FOCUS_END'))
    def test_invalid_boolean(self):
        with self.assertRaises(ValueError): parse_focus_dump(RECORD.replace('editor_attached=true','editor_attached=yes'))
    def test_crlf_is_transport_not_semantic_change(self):
        self.assertEqual(parse_focus_dump(RECORD),parse_focus_dump(RECORD.replace('\n','\r\n')))

if __name__=='__main__': unittest.main()
