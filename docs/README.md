# Kensa TeamCity Plugin

Surfaces Kensa BDD test reports inside TeamCity. Three features, gated behind a single opt-in build feature:

1. **Kensa Report tab** — embeds the Kensa HTML site as a per-build tab.
2. **First-class test results** — Kensa tests appear in TeamCity's tests UI with their human-readable display names.
3. **Failure summaries** — failed tests carry the Given-When-Then narrative + captured values + exception inline in the test details.

## Local development

Requires Docker and a local checkout of `kensa-dev/clearwave-example` (the canonical demo project — already produces real Kensa output). Set `KENSA_EXAMPLE_PATH` to the absolute path of your checkout before running the dev tasks (e.g. `export KENSA_EXAMPLE_PATH=~/code/clearwave-example`, or put it in `docker/.env`).

```bash
./gradlew devUp           # build the plugin, start TC + agent in docker
# open http://localhost:8111 — first run requires admin setup + EULA (~5 min)
# data/ is persisted, so first-run is one-time

./gradlew devReload       # rebuild plugin, restart TC server (~30s)
./gradlew devDown         # stop the stack
```

After first-run setup, configure a TC build that runs `./gradlew test` against `/sample-projects/clearwave-example` and add the **Kensa Integration** build feature.

## Tests

```bash
./gradlew test                       # unit tests across all modules
./gradlew :server:serverPlugin       # build the plugin zip
```

## Layout

- `common/` — shared JSON DTOs and path resolution. No TC dependencies.
- `agent/` — agent-side: lifecycle adapter, service-message emitter, narrative builder.
- `server/` — server-side: build feature, report tab.
- `docker/` — local dev harness.
