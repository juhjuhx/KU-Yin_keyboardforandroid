# R3.3 H1 process/pipe evidence

## Direct evidence and limits

R3.2 CI [39514852906](https://github.com/juhjuhx/KU-Yin_keyboardforandroid/actions/runs/39514852906), source8acf4b24: bounded H0 HEALTHY, H1 INCONCLUSIVE, H2 NOT_EXECUTED. CompleteXML27311 bytes reached host at+2243ms and marker34 bytes at+2246ms; capture timed out at3007ms. These are host receive times, not guest event timestamps.

Window focus was the correct host component/HAS_DRAWN; XML's unique EditText had focused=false, and no node had focused=true. `RecordingEditText.kt:32–40` records a past connection creation; `EditorRecorder.kt:10–12` retains metadata. `input_connection_present=true` does not prove a live connection or focus at capture time. Before/after IME visibility also changed false/0px→true/775px. View focus vs accessibility observation and Android root cause remain UNRESOLVED.

## Single falsifiable hypothesis and minimal change

`observe_output` previously waited for both pipe EOFs before `proc.wait`; timeout killed/waited without preserving pre-cleanup child state. Therefore `exit=null` did not prove the adb direct child was still running: an exited child with a descendant holding pipes produces the same timeout.

New TEST_ONLY diagnostics record direct child PID/returncode before cleanup, each **observed** pipe EOF, host time, kill attempt and post-cleanup returncode. These observations are non-atomic and do not establish guest uiautomator exit. Incomplete capture remains timeout, exit=null and INCONCLUSIVE even if direct child exited0. Original adb argv,3s command cap,10s capture budget,assess_h1 AST,App/Host assertions and protected source unchanged. No production/JNI/native/signing change.

## RED/GREEN and review status

Three real-process tests distinguish: live child at deadline; exited parent with inherited pipes; natural exit with completed streams. Original code fails all three completion evidence assertions; modified collector passes. Initial independent review exposed startup timing in400ms fixtures and descendant cleanup risk. Setup now waits for explicit readiness/parent exit before measuring the existing400ms collector budget; fixtures run in isolated process groups and always clean their group. This changes test setup, not Android timeout.

Local baseline152 had one sandbox PTY failure (`test_h1_receives_real_xml_from_dump_terminal_not_just_success_marker`); a PTY-capable run passed155 after the three added tests. The first isolated old-code replay had a missing imported runner and is **not valid RED**; complete-import replay is stored separately. Current evidence/results live in the local checkpoint, including failures.

Final independent review is REVIEW_INCOMPLETE: reviewer quota exhausted after initial findings, before revised fixture approval. No new H0/H1 CI dispatched; no new H2/App/Host evidence. Next: independent review of exact diff+focused tests, then exactly one unchanged-APK/same-boot health-only H0→H1 run. H1 not trustworthy PASS means stop H2. No declaration of Android fix.
