# M4 baseline audit (no redesign in this round)

| Area | CURRENT | TARGET | BLOCKER | BACKEND DEP | SAFE AFTER D3 |
|---|---|---|---|---|---|
| CandidateBar | outlined preedit pill + X + boxed cards | 44–48dp clean strip, first-candidate emphasis | None (approved) | No | YES |
| Zhuyin layout | table stretch, ROW_3/4 wrong mapping | Gboard geometry + canonical Dachen | Canonical = KeyboardLayout.Dachen (dup must die) | No | YES |
| English QWERTY | fixed 16dp side stagger (dead strips) | row-inset geometry | None | No | YES |
| Symbols | 4 hardcoded rows | SymbolCatalog 7+ pages + tabs | G1 symbol-API decision | Maybe (native) | Data: YES; native merge: NO |
| Emoji | 40 hardcoded | categories + recents | Metadata source decision | No | YES |
| Touch | plain clickable | KeyboardKeySurface (tap/long/repeat/slide/popup) | Backspace repeat params | No | YES |
| Bottom row | 7 crowded keys, emoji permanent | ?123/，/space/。/Enter + toolbar lang | None | No | YES |
| Settings | partial (height/borders? verify) | 9 listed toggles | None | No | YES |
