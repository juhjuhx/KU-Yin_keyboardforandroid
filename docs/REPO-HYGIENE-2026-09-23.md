# Repo hygiene record — 2026-09-23

## Fact

`main@73e810f` contains one commit unrelated to the KU-Yin Android IME:

- `taui-workspace/TAUI_REDESIGN.md`
- `taui-workspace/taui-workspace-standalone-handoff.html`

(TAUI standalone web workspace prototype + its redesign notes.)

## Decision

The C4 Android development line (`c4-compose-libchewing-switch`) intentionally
does NOT merge that commit:

- no merge of `main@73e810f` into the C4 branch,
- no rebase of the C4 branch onto it,
- the TAUI files are not part of the Android build (no source references them).

## Suggested follow-up (NOT done in this round)

Revert or remove the unrelated TAUI files from KU-Yin `main` to keep the
Android product history clean. Do this as an explicit, reviewed commit —
no force-push of `main`, no destructive history rewrite.
