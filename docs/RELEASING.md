# Releasing

The plugin is signed and published to JetBrains Marketplace from the same Kensa
signing key used for the IntelliJ plugin (`kensa-dev/intellij-plugin`).

## Required environment variables

| Variable               | Contents                                              |
|------------------------|-------------------------------------------------------|
| `PRIVATE_KEY`          | The full PEM private key (multi-line, including header/footer). |
| `CERTIFICATE_CHAIN`    | The full PEM cert chain.                              |
| `PRIVATE_KEY_PASSWORD` | The passphrase for the private key. Optional — set if the key is encrypted. |
| `PUBLISH_TOKEN`        | A JetBrains Marketplace upload token (get one at <https://plugins.jetbrains.com/author/me/tokens>). |

These are the same names the IntelliJ plugin reads, so the same secrets file or
CI configuration carries across both repos.

## Local release flow

1. Bump the `version` in `build.gradle.kts` and the `<version>` in
   `teamcity-plugin.xml` to match.
2. Update the `<change-notes>` section of `teamcity-plugin.xml` (or the description) with what's new.
3. Export the secrets into your shell:

   ```bash
   export PRIVATE_KEY="$(cat ~/path/to/private.pem)"
   export CERTIFICATE_CHAIN="$(cat ~/path/to/chain.crt)"
   export PRIVATE_KEY_PASSWORD='your-pass-or-omit'
   export PUBLISH_TOKEN='perm:...'
   ```

4. Build, sign, publish:

   ```bash
   ./gradlew test :server:serverPlugin   # build + verify the unit tests still pass
   ./gradlew signPlugin                  # produces build/distributions/kensa-teamcity-plugin-signed.zip
   ./gradlew publishPlugin               # uploads to Marketplace
   ```

5. Tag the release locally (do **not** push without confirmation per project policy):

   ```bash
   git tag v$(grep '^version' build.gradle.kts | head -1 | cut -d'"' -f2)
   ```

## What `signPlugin` does

- Downloads `marketplace-zip-signer-cli.jar` from JetBrains' GitHub releases
  (cached under `build/zip-signer/`).
- Writes the env-var contents to ephemeral files under `build/zip-signer/work/`
  (these never leave the host and are gitignored alongside `build/`).
- Invokes `java -jar marketplace-zip-signer-cli.jar sign ...` against the
  unsigned zip from `:server:serverPlugin` and writes the signed zip to
  `build/distributions/`.

## What `publishPlugin` does

- Depends on `signPlugin`, so it always uploads the signed artifact.
- POSTs the zip to `https://plugins.jetbrains.com/plugin/uploadPlugin` with
  `xmlId=kensa-teamcity-plugin` and the `PUBLISH_TOKEN` as Bearer auth.
- For the **first** release, JetBrains will create the plugin entry. For
  updates, the `xmlId` matches the existing plugin so the version is added.

## First-time marketplace setup

The very first upload usually goes through the web UI rather than the API so
you can attach a logo, screenshots, and category metadata:

1. Run `./gradlew signPlugin` once to produce the signed zip.
2. Go to <https://plugins.jetbrains.com/plugin/add> and upload the signed zip.
3. Fill in: category (TeamCity → Test Reports), logo (use Kensa's `Logo.svg`),
   1–2 screenshots of the report tab and the failure-narrative panel.
4. Submit for review. Subsequent updates can use `./gradlew publishPlugin`.

## Plugin verification

The rodm gradle plugin's built-in `:server:serverPlugin` task validates the
descriptor against JetBrains' XSD on every build. There's no separate
plugin-verifier for TC plugins (unlike IntelliJ); cross-version compatibility
is best validated by spinning up the docker harness against multiple TC server
versions.
