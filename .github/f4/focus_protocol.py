"""TEST_ONLY parse a bounded, text-free Activity focus record. No H1 verdict override."""
FIELDS = frozenset('editor_is_focused editor_has_focus window_has_focus editor_window_has_focus editor_attached editor_focusable_in_touch_mode root_focus_is_editor node_input_focused node_accessibility_focused last_recorded_connection_present'.split())


def parse_focus_dump(text):
    lines = [line.strip() for line in text.splitlines()]
    begin, end = 'KUYIN_FOCUS_TEST_ONLY', 'KUYIN_FOCUS_END'
    if lines.count(begin) != 1 or lines.count(end) != 1:
        raise ValueError('Missing or duplicate focus record delimiter')
    start, stop = lines.index(begin), lines.index(end)
    if stop <= start:
        raise ValueError('Invalid focus record order')
    values = {}
    for line in lines[start+1:stop]:
        key, separator, value = line.partition('=')
        if not separator or key not in FIELDS or key in values:
            raise ValueError('Unknown, missing or duplicate focus field')
        allowed = ['true','false','null'] if key == 'last_recorded_connection_present' else ['true','false']
        if value not in allowed:
            raise ValueError('Unverified focus value')
        values[key] = {'true':True,'false':False,'null':None}[value]
    if set(values) != FIELDS:
        raise ValueError('Incomplete focus record')
    return values
