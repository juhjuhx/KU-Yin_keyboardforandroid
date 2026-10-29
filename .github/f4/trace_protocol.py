"""TEST_ONLY bounded callback symbols, not exception messages or editor content."""
import re


def parse_trace(text):
    lines = [line.strip() for line in text.splitlines()]
    begin, end = 'KUYIN_FOCUS_TRACE_BEGIN', 'KUYIN_FOCUS_TRACE_END'
    if lines.count(begin) != 1 or lines.count(end) != 1 or lines.index(end) <= lines.index(begin):
        raise ValueError('Missing or duplicate trace delimiter')
    fields, frames = {}, {'first_frame':[],'last_frame':[]}
    for line in lines[lines.index(begin)+1:lines.index(end)]:
        key, separator, value = line.partition('=')
        if not separator: raise ValueError('Malformed trace')
        if key in frames:
            if len(frames[key]) >= 24 or not re.fullmatch(r'[A-Za-z0-9_.$]+#[A-Za-z0-9_$<>-]+:-?\d+',value):
                raise ValueError('Invalid callback symbol')
            frames[key].append(value)
        elif key in ['event','loss_count','first_elapsed_ns','last_elapsed_ns'] and key not in fields:
            fields[key]=value
        else: raise ValueError('Unknown or duplicate trace field')
    if set(fields)!=set(['event','loss_count','first_elapsed_ns','last_elapsed_ns']) or fields['event']!='FOCUS_LOSS_CALLBACK':
        raise ValueError('Incomplete trace identity')
    if not re.fullmatch(r'\d+',fields['loss_count']): raise ValueError('Invalid loss count')
    count=int(fields['loss_count'])
    if count==0:
        if fields['first_elapsed_ns']!='null' or fields['last_elapsed_ns']!='null' or any(frames.values()):
            raise ValueError('Contradictory absent callback')
    else:
        if not all(re.fullmatch(r'\d+',fields[key]) for key in ['first_elapsed_ns','last_elapsed_ns']):
            raise ValueError('Unverified guest time')
        if count==1 and (fields['first_elapsed_ns']!=fields['last_elapsed_ns'] or frames['first_frame']!=frames['last_frame']):
            raise ValueError('Contradictory single callback')
        if int(fields['first_elapsed_ns'])>int(fields['last_elapsed_ns']) or not all(frames.values()):
            raise ValueError('Incomplete callback ordering')
    return dict(**fields,**frames,clock_domain='GUEST_ELAPSED_REALTIME_NANOS',scope='CALLBACK_CALLER_ONLY_NOT_PROOF_OF_EXTERNAL_TRIGGER',timestamp_semantics='POST_SUPER_CALLBACK_OBSERVATION_NOT_IPC_TIME',history_scope='AFTER_SUBCLASS_INITIALIZATION_NOT_FULL_CONSTRUCTION_HISTORY')
