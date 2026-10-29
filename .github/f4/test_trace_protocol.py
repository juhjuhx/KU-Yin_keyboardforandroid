import unittest
from trace_protocol import parse_trace
BASE='''KUYIN_FOCUS_TRACE_BEGIN
event=FOCUS_LOSS_CALLBACK
loss_count=1
first_elapsed_ns=10
last_elapsed_ns=10
first_frame=android.view.View#clearFocus:30
last_frame=android.view.View#clearFocus:30
KUYIN_FOCUS_TRACE_END
'''
class TraceTest(unittest.TestCase):
 def test_valid_callback_symbols(self): self.assertEqual(['android.view.View#clearFocus:30'],parse_trace(BASE)['last_frame'])
 def test_absent_callback_is_not_fabricated(self):
  text=BASE.replace('loss_count=1','loss_count=0').replace('=10','=null').replace('first_frame=android.view.View#clearFocus:30\n','').replace('last_frame=android.view.View#clearFocus:30\n','')
  self.assertEqual('0',parse_trace(text)['loss_count'])
 def test_unknown_text_rejected(self):
  with self.assertRaises(ValueError):parse_trace(BASE.replace('KUYIN_FOCUS_TRACE_END','text=private\nKUYIN_FOCUS_TRACE_END'))
 def test_truncated_record_rejected(self):
  with self.assertRaises(ValueError):parse_trace(BASE.replace('KUYIN_FOCUS_TRACE_END',''))
 def test_unbounded_frames_rejected(self):
  with self.assertRaises(ValueError):parse_trace(BASE.replace('last_frame=android.view.View#clearFocus:30\n','last_frame=android.view.View#clearFocus:30\n'*25))
 def test_single_callback_cannot_have_different_first_and_last(self):
  with self.assertRaises(ValueError):parse_trace(BASE.replace('last_elapsed_ns=10','last_elapsed_ns=11'))
 def test_unordered_guest_time_rejected(self):
  with self.assertRaises(ValueError):parse_trace(BASE.replace('last_elapsed_ns=10','last_elapsed_ns=1'))
if __name__=='__main__':unittest.main()
