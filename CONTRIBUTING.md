# Contributing to Android Keyboard

Thank you for your interest in contributing! This document explains how to
get started.

## Commit Convention

This project uses [Conventional Commits](https://www.conventionalcommits.org/).
Every commit message must follow the format:

```
<type>(<scope>): <description>
```

Common types:

| Type       | When to use                                    |
|------------|------------------------------------------------|
| `feat`     | A new feature or layout                        |
| `fix`      | A bug fix                                      |
| `docs`     | Documentation only                             |
| `chore`    | Build, CI, or tooling changes                  |
| `refactor` | Code restructure with no behaviour change      |
| `test`     | Adding or fixing tests                         |
| `perf`     | Performance improvement                        |

Examples:

```
feat(ime): add Hsu 26-key candidate selection
fix(dict): prevent crash on empty user dictionary
chore(ci): cache Gradle dependencies in workflow
```

## Development Setup

1. Clone the repository
2. Open in Android Studio (latest stable recommended)
3. Sync Gradle and install SDK components as prompted
4. Connect a device or start an emulator
5. Run the `app` module

### Build Requirements

- **JDK 17**
- **Android SDK** with the compileSdk and targetSdk versions defined in
  `app/build.gradle.kts`
- **Gradle** — use the included wrapper (`./gradlew`)

## Testing

All contributions must include or update tests where applicable.

- **Unit tests**: run with `./gradlew test`
- **Instrumented tests**: run with `./gradlew connectedAndroidTest`
- Layout and IME behaviour changes should include regression tests where
  feasible
- CI will run both unit and instrumented test suites on every pull request

## Pull Request Process

1. Fork the repository and create a feature branch from `main`
2. Make your changes, following the commit convention above
3. Ensure all tests pass locally before submitting
4. Open a pull request against `main` with a clear description of what
   changed and why
5. A maintainer will review — please respond to feedback promptly

## Code Style

- Follow existing code conventions in the module you are editing
- Prefer Kotlin for new code unless the surrounding module is Java
- Keep changes focused — one logical change per pull request where possible

## License

By contributing, you agree that your contributions will be licensed under the
same license as the project. See [LICENSE](LICENSE) for details.

## Code of Conduct

Be respectful, constructive, and inclusive. We are here to build a good
keyboard together.
