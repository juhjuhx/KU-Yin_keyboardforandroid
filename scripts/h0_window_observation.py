"""Pure offline WMS predicates; no device collector, runtime gate or ADB effects.

Extracted unchanged from KU-Yin emulator_health.py at aa1bf8753a9e66ab249ba6921cd4ea22a3810a06.
The legacy diagnostic lineage remains separate from main.
"""
import re

SERVICE_ERROR = r'DUMP TIMEOUT|Permission Denial|Can.t find service|Error dumping'

def alert_state(window):
    """Visibility is per Window block: an existing hidden ANR is not a visible one."""
    unknown, visible = [], []
    blocks = re.split(r'(?m)^\s*Window #\d+ ', window)[1:]
    for block in blocks:
        if not re.search(r'Application Not Responding:|Application Error:|ty=SYSTEM_ALERT|\bSYSTEM_ERROR\b', block):
            continue
        flags = re.findall(r'\bisVisible=(true|false)\b', block)
        if flags == ['true']: visible.append('BLOCKING_SYSTEM_ALERT')
        elif flags != ['false']: unknown.append('ALERT_VISIBILITY_UNVERIFIED')
    return unknown, visible


def window_unfrozen(window):
    """Only one explicit false is evidence; absent/unknown/conflicting is not."""
    return (len(re.findall(r'\bmDisplayFrozen=', window)) == 1 and
            re.findall(r'\bmDisplayFrozen=([^\s]+)', window) == ['false'])


def complete_window(window):
    """API33 WMS observation, not an atomic snapshot or continuous health proof."""
    if 'WINDOW MANAGER WINDOWS' not in window or re.search(SERVICE_ERROR, window, re.I): return False
    focus = re.findall(r'mCurrentFocus=(Window\{[^\n}]+\})', window)
    headers = re.findall(r'(?m)^\s*Window #(\d+) (Window\{[^\n}]+\}):', window)
    blocks = re.split(r'(?m)^\s*Window #\d+ ', window)[1:]
    return (window_unfrozen(window) and len(focus) == 1 and bool(headers) and
            [int(index) for index, _ in headers] == list(range(len(headers))) and
            focus[0] in [title for _, title in headers] and
            'mSystemBooted=true mDisplayEnabled=true' in window and
            all(len(re.findall(r'\bisVisible=(?:true|false)\b', block.split('\n  mGlobalConfiguration=', 1)[0])) == 1
                for block in blocks))
