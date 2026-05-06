# Changelog

All notable changes to the Kensa TeamCity Plugin will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

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
