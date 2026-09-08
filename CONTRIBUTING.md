# Contributing to android-keyboard

Thank you for your interest in android-keyboard! This project is a fork of [fcitx5-android](https://github.com/fcitx5-android/fcitx5-android), focused on providing a privacy-first, high-performance Android bopomofo input experience.

## Scope and Boundaries

Before opening an Issue or PR, please note:
1. **Upstream issues**: If the problem relates to fcitx5 engine core, general upstream UI, or generic protocols, report to [fcitx5-android upstream](https://github.com/fcitx5-android/fcitx5-android/issues) first.
2. **This repo**: Issues about DaChen/Hsu/Eten26 layouts, OpenCC conversion, custom Canvas frontend, or project-specific privacy/security designs belong here.
3. **Out of scope**: Nine-grid (九宮格), Japanese twelve-key, and engine-controlled keyboard layouts are not promised in this phase (see docs/ROADMAP.md).

## Environment Setup

This project involves Kotlin + C++ (JNI) + Rust mixed compilation. Ensure:
1. **Android SDK/NDK**: Configure sdk.dir and ndk.dir in local.properties.template.
2. **Rust toolchain**: libchewing 0.13.x requires Rust.
   `ash
   curl --proto =https --tlsv1.2 -sSf https://sh.rustup.rs | sh
   rustup toolchain install 1.88.0  # MSRV for libchewing 0.13.x
   `
3. **JDK 17**: Temurin 17 or equivalent recommended.

## Conventional Commits

This project follows [Conventional Commits](https://www.conventionalcommits.org/). Every commit must follow:

`
<type>(<scope>): <subject>
`

| Type       | When to use                              |
|------------|------------------------------------------|
| feat       | New feature or layout                    |
| fix        | Bug fix                                  |
| docs       | Documentation only                       |
| chore      | Build, CI, or tooling changes            |
| refactor   | Code restructure with no behaviour change|
| test       | Adding or fixing tests                   |
| perf       | Performance improvement                  |

Examples:
`
feat(decoder): pin chewing with 3 layouts, Dachen default
feat(opencc): one-tap simp-trad s2tw/tw2s with fallback
fix(frontend): prevent candidate popup in password fields
chore(security): isolated id/provider/minimal perms
docs(decoder): pin libchewing spec for T5
`

## Testing

All contributions must include or update tests where applicable.
- Unit tests: ./gradlew test
- Instrumented tests: ./gradlew connectedAndroidTest
- Layout and IME behaviour changes should include regression tests where feasible.
- CI runs both suites on every PR.

## Pull Request Process

1. Fork the repo and create a feature branch from main
2. Make changes following the commit convention above
3. Ensure all tests pass locally before submitting
4. Open a PR against main with a clear description
5. Respond to feedback promptly

## Code Style

- Follow existing conventions in the module you edit.
- Prefer Kotlin for new code unless the surrounding module is Java.
- Keep changes focused -- one logical change per PR.

## License

By contributing, you agree your contributions are licensed under the same terms as the project. See [LICENSE](LICENSE).

## Code of Conduct

Be respectful, constructive, and inclusive.
