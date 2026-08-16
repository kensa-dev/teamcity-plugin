# Changelog

All notable changes to the Kensa TeamCity Plugin will be documented in this file.
The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.3.2] - 2026-08-16

### Fixed

- **Duplicate failed-test entries in the build overview** — the test
  reporter published Kensa tests to TeamCity under
  `ClassName.Display Name`, while the test runner (Gradle/JUnit) had
  already reported the same tests as `ClassName.methodName`. TeamCity
  keys tests by full name, so every Kensa test was counted twice and
  each failure appeared as two distinct failed tests. The reporter now
  uses the method name, so its events merge with the runner's and the
  Kensa failure narrative attaches to the existing test entry.

## [0.3.1]

### Added

- **Custom `siteRoot` auto-discovery** — `KensaPaths.resolve()` now finds the
  Kensa report directory wherever the Gradle plugin's `siteRoot` points
  (default `build/kensa-site/`, or any custom path under the checkout)
  by walking for `manifest.json` (site mode) or `indices.json` (non-site
  sourceset). Multi-subproject aggregated sites — the canonical multi-
  module shape produced by the Gradle plugin's root-aggregator role —
  are discovered without any extra configuration.
- **Walk diagnostics in the build log** — when discovery falls back to
  the walk, the resolved path is logged at info-level. Multiple
  `manifest.json` candidates (e.g. a stale aggregate from a prior config)
  are warned with the full candidate list. Multiple non-site outputs
  with no aggregate manifest log a warning recommending site mode.
- **Quick-open report button** — a prominent "Open Kensa Report" button on
  the build results page (below the build summary). One click to the
  full-page report in a new tab — no need to enter the Kensa tab first.
  Conditional on the build having published Kensa output as an artifact.
- **Build action menu entry** — an "Open Kensa report" link in the build's
  action list, for menu-driven navigation. Same artifact gating.
- **Documented `.lastFinished` bookmark URL pattern** in the README — the
  stable per-build-config URL
  (`/repository/download/<id>/.lastFinished/kensa-site/index.html`) for
  bookmarking the latest report from outside TeamCity. Variants:
  `.lastSuccessful`, `.lastPinned`.

### Fixed

- **Empty default output directory no longer masks a real one** —
  `build/kensa-site/` or `build/kensa-output/` without any
  `manifest.json` / `indices.json` no longer suppresses discovery of
  a real output located elsewhere.

## [0.1.0]

### Added

- **Kensa Report tab** — embeds the Kensa HTML site as a per-build tab with
  an "Open in new tab" link for full-page rendering. Conditional on the
  build having published Kensa output as an artifact.
- **First-class test results** — every Kensa test surfaces in TeamCity's
  Tests UI by its human-readable Given-When-Then display name, joining
  the standard history graphs, flaky-test detection, and trends.
- **Failure summaries** — when a Kensa test fails, the Given-When-Then
  narrative + captured values + exception is attached as the test's failure
  detail in the Tests tab.
- **Frictionless detection** — `KensaPaths` resolves the Kensa output dir
  zero-config across single-sourceset (`build/kensa-output/`), multi-sourceset
  site mode (`build/kensa-site/`), and multi-module Gradle layouts.
- **Single opt-in build feature** — `Kensa Integration` with three configurable
  sub-feature toggles and an optional explicit-path override.
- **Docker dev harness** — `./gradlew devUp/devReload/devDown` brings up TC
  server + agent locally with `clearwave-example` mounted as the demo project.
- **Marketplace tooling** — `:signPlugin` and `:publishPlugin` gradle tasks
  reuse the env-var contract from `kensa-dev/intellij-plugin` so the same
  signing key works for both repos.

[Unreleased]: https://github.com/kensa-dev/teamcity-plugin/compare/0.3.2...HEAD
[0.3.2]: https://github.com/kensa-dev/teamcity-plugin/compare/0.3.1...0.3.2
[0.3.1]: https://github.com/kensa-dev/teamcity-plugin/compare/0.1.0...0.3.1
[0.1.0]: https://github.com/kensa-dev/teamcity-plugin/commits/0.1.0
