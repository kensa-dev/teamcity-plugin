# Releasing

Releases are driven from GitHub, mirroring the `kensa-dev/intellij-plugin`
flow. The same Marketplace signing key works for both repos.

## One-time setup

Add the following secrets to the GitHub repository (Settings → Secrets and
variables → Actions). They use the **same names** as the IntelliJ plugin so
the values can be copy-pasted between repos:

| Secret                 | Contents                                              |
|------------------------|-------------------------------------------------------|
| `PRIVATE_KEY`          | The full PEM private key (multi-line, with header/footer). |
| `CERTIFICATE_CHAIN`    | The full PEM cert chain.                              |
| `PRIVATE_KEY_PASSWORD` | Passphrase for the private key. Optional — set if the key is encrypted. |
| `PUBLISH_TOKEN`        | A JetBrains Marketplace upload token (<https://plugins.jetbrains.com/author/me/tokens>). |

## Per-release flow

1. **Add notes** to the `[Unreleased]` section of `CHANGELOG.md` describing
   what's new in the upcoming version. Commit + push to `master`.

2. **Bump version** in `build.gradle.kts` (`version = "X.Y.Z"`) and
   `teamcity-plugin.xml` (`<version>X.Y.Z</version>`). Commit + push.

3. The **Build** workflow (`.github/workflows/build.yml`) runs on the push:
   - `:server:serverPlugin` (assembles the plugin zip)
   - `check` (runs all unit tests)
   - Creates a **draft GitHub release** tagged `vX.Y.Z` with notes pulled from
     the `[Unreleased]` section of `CHANGELOG.md`.

4. **Review the draft release** on GitHub. Edit the notes if needed (the
   text becomes the release-asset description and is propagated back to
   `CHANGELOG.md` after publishing).

5. **Publish the draft.** This triggers `release.yml`:
   - `patchChangelog` — moves `[Unreleased]` notes into a new `[X.Y.Z]`
     section and creates a fresh empty `[Unreleased]`.
   - `:signPlugin` + `:publishPlugin` — signs with the kensa-dev key and
     uploads to JetBrains Marketplace.
   - Uploads the signed zip to the GitHub release as an asset.
   - Opens a PR with the patched `CHANGELOG.md` for review.

6. **Merge the changelog PR** to keep `master`'s `CHANGELOG.md` in sync.

## Local release flow (no GitHub)

If you ever need to release without GitHub Actions:

```bash
export PRIVATE_KEY="$(cat /path/to/private.pem)"
export CERTIFICATE_CHAIN="$(cat /path/to/chain.crt)"
export PRIVATE_KEY_PASSWORD='your-pass-or-omit'
export PUBLISH_TOKEN='perm:...'

./gradlew test :server:serverPlugin
./gradlew signPlugin                  # build/distributions/kensa-teamcity-plugin-signed.zip
./gradlew publishPlugin               # uploads to Marketplace
```

## First-time marketplace listing

The very first upload usually goes through the web UI rather than the API so
you can attach a logo, screenshots, and category metadata:

1. `./gradlew signPlugin` to produce the signed zip locally.
2. Go to <https://plugins.jetbrains.com/plugin/add> and upload the signed zip.
3. Fill in: category (TeamCity → Test Reports), logo (use Kensa's `Logo.svg`),
   1–2 screenshots of the report tab and the failure-narrative panel.
4. Submit for review. Subsequent updates can use `./gradlew publishPlugin`
   (or just publish a GitHub release draft — the Action handles it).

## Plugin verification

The rodm gradle plugin's `:server:serverPlugin` task validates the descriptor
against JetBrains' XSD on every build. There's no separate plugin-verifier for
TC plugins (unlike IntelliJ); cross-version compatibility is best validated by
spinning up the docker harness against multiple TC server versions.
