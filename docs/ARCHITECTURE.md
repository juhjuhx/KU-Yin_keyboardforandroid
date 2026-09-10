# Architecture

KU-Yin 当前是一套原生 Android IME。项目自有代码以 Kotlin/View 和 C++ JNI 为主，中文解码委托给 libchewing C API。

```text
Android Framework
EditorInfo / InputConnection / InputMethodService lifecycle
                    │
                    ▼
        ChewingInputMethodService
                    │
        ┌───────────┼───────────────┐
        ▼           ▼               ▼
  EditorPolicy  ImeSession      KeyboardView /
                 Controller      CandidateView
        │           │               │
        └───────────┴───────┬───────┘
                            ▼
                    ChewingEngine contract
                            │
                            ▼
                  AndroidChewingEngine
                            │
                            ▼
                       JNI / C++
                            │
                            ▼
                    libchewing C API
```

## Boundaries

`ChewingInputMethodService` 是 Android adapter，负责 framework lifecycle、`InputConnection` 与 UI orchestration。`EditorPolicy` 将 `EditorInfo` 的 password、force-ASCII、no-personalized-learning 和 editor action 统一规范化。`ImeSessionController` 决定当前使用 Dachen 还是 ASCII surface，以及候选/composition/learning policy。

`ChewingEngine` 是 Kotlin 侧的 decoder contract；`AndroidChewingEngine` 是唯一应直接调用 JNI 的 Android adapter。UI 不应直接依赖 native symbols。

## Native boundary

JNI 层只做类型/生命周期转换与 libchewing C API forwarding。system dictionary 与 user dictionary 路径由 Kotlin 层准备后传入。native context 在重新初始化与关闭时显式 delete；JNI strings 在调用后释放。

## Storage

系统词典从 APK assets 安装到 `noBackupFilesDir/libchewing/system`，user dictionary 位于 `noBackupFilesDir/libchewing/user/userdict.dat`。这是 app-private 且不参与 Android backup 的存储；目前没有额外应用层加密。

## Current layout model

当前正式暴露的大千/Dachen layout 与 decoder 固定为同一语义，避免 UI 与 libchewing keyboard mode 漂移。Hsu/Eten26 只保留为未来 roadmap，在有完整可视布局、测试与 editor integration 之前不对使用者宣称支持。

## Runtime verification

Build 成功与 IME runtime 成功分离。CI 的 blocking path 覆盖 contracts、JVM tests、native bootstrap、Debug/Release APK；Android emulator runtime smoke 目前用于安装/注册/enable/select/JNI 证据，headless IME-window-visible assertion 仍为 non-blocking。

## Non-goals

当前不引入 Compose 主渲染、不新增 KU-Yin 自有 Rust 层、不把 OpenCC/Emoji/clipboard/prediction 等规划写成已完成能力。若未来直接消费新版 libchewing Rust 构建或改 ABI，需独立架构决策。
