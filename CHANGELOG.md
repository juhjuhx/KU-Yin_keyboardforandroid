# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

### Added
- Default DaChen (大千) 4x10 bopomofo layout support.
- Hsu (Xushi) and Eten26 layout options.
- OpenCC one-tap simplified-traditional conversion (default s2tw / tw2s, fallback s2t / t2s).
- User dictionary memory with import/export (.zip format, compatible with Linux).
- Frontend uses Kotlin View/Canvas single-container self-drawing, achieving <8ms onDraw performance budget.

### Changed
- Upgraded decoding engine to libchewing 0.13.x (Rust refactored version).
- Core architecture refactored to three-layer design (APP / CORE / Upstream), achieving complete Core/UI decoupling.

### Fixed
- Mitigated libchewing #836 chewing_Reset semantic change state reset issue.
- Fixed candidate characters leaking to screen in password fields (R005).
- Fixed crash risk caused by OTP multi-field keyboard rebuild (R004).

### Security
- Adhered to zero INTERNET permission design, ensuring dictionary and input behavior never leave the process.
- Used submodule SHA pinning to prevent supply-chain attacks.

## [0.1.0] -- 2026-09-08 (Wave 0 Scaffold)

### Added
- Initial fork-mirror scaffold (based on fcitx5-android).
- Complete project documentation system (README, ARCHITECTURE, AUDIT, ROADMAP, 14 core documents).
- Established LGPL-2.1 license and complete NOTICE per-item attribution.
- Completed Wave 0 architecture self-check and DECODER-PIN.md spec freeze.
