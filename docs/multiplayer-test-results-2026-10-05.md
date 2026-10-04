# Multiplayer server integration results — 2026-10-05

The final batch `all20261005b` passed on all 11 supported versions: **94 assertions
per version, 1,034 assertions total**. Gradle exited successfully in 2 minutes 4 seconds.

| Minecraft | Result | Passed assertions |
| --- | --- | ---: |
| 1.21 | PASS | 94 |
| 1.21.1 | PASS | 94 |
| 1.21.3 | PASS | 94 |
| 1.21.4 | PASS | 94 |
| 1.21.5 | PASS | 94 |
| 1.21.8 | PASS | 94 |
| 1.21.10 | PASS | 94 |
| 1.21.11 | PASS | 94 |
| 26.1.2 | PASS | 94 |
| 26.2 | PASS | 94 |
| 26.3 | PASS | 94 |

## What was actually tested

Each version ran its own real dedicated server, with Fabric Loader 0.19.5 and its
configured Fabric API version, under JDK 25. Four distinct Survival-mode test players
were registered through the player list using in-memory connections. No existing
world was reused. All test servers stopped after the checks.

The suite exercised real command dispatch, scoreboard state and broadcast packets,
permission policies, own-team name/ID suggestions, renaming, color and friendly-fire
changes, invites, acceptance/decline/replay protection, leaving and invitation
cancellation, four-player splitting, clearing and retrying. It also invoked the real
registered GUI server handlers and round-tripped their payload codecs with live
server registries. The GUI request IDs and resulting team snapshots were checked.

The first exploratory batch exposed a **test-harness startup ordering error**, not
a reproduced production defect: test actions could run inside `SERVER_STARTED`
before TeamCraft loaded its world permissions, and 1.21.8 could wait indefinitely
for initial chunk/entity loading. Running tests after five completed server ticks
resolved both; the entire final batch was then rerun on fresh worlds.
No production feature changes were made for these tests.

## Limits — not a full multiplayer-client certification

No graphical Minecraft clients or TCP login handshakes were tested. Server GUI
handlers were called directly; codecs were exercised separately. The following
remain **untested**:

- GUI rendering, mouse/keyboard input, modal/tooltip layers, clipping and hotkeys.
- Real client login/channel negotiation and actual client-side packet reception.
- Latency, network disconnects/reconnects and asynchronous client callbacks.
- Restart persistence and third-party mod combinations.
- Older versions running under their minimum supported Java runtime rather than JDK 25.
- 26.3 Vulkan/OpenGL startup and graphics-driver behavior.

See [the testing guide and real-client checklist](multiplayer-testing.md) for reruns
and remaining manual checks. The test-only mod is opt-in and excluded from releases.

Generated evidence is under `build/multiplayer-tests/all20261005b/` (`report.md`,
`summary.json`, `gradle.log`). Per-version results, logs and fresh worlds remain under
`versions/<version>/build/multiplayer-tests/all20261005b/`. These generated files can
be removed by `clean`; this summary is preserved separately in `docs/`.
