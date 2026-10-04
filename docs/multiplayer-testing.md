# Multiplayer testing

## Automated server integration tests

Run from PowerShell with JDK 25:

```powershell
./scripts/verify-multiplayer.ps1 -JavaHome 'E:/Environments/Java/Java25/Azul Zulu JDK 25.32.17'
```

Run a single version:

```powershell
./scripts/verify-multiplayer.ps1 -JavaHome 'E:/Environments/Java/Java25/Azul Zulu JDK 25.32.17' -Versions '1.21'
```

The script uses the versions in `settings.json`. Each version starts a real dedicated
server with four distinct Survival-mode server players and in-memory connections.
It exercises the actual command dispatcher, scoreboard broadcasts, invitation and
membership services, registered GUI server handlers, and payload codecs using the
running server's registries. It does not create four graphical clients or perform a
network login handshake. GUI handlers are invoked directly after codec checks.

Coverage includes:

- Own-team-only ID/name suggestions, renaming, quoted and duplicate names.
- Non-operator permissions, root permission, manual cross-team attempts.
- Name/color/friendly-fire updates and scoreboard notifications to all four connections.
- Invitation sending, acceptance, decline, replay/stale identities, cancellation on leave.
- Self-only leave, stale team IDs, remaining members and candidate preservation.
- Four-player team splitting, existing-team rejection, clearing and retrying.
- GUI snapshots, own-team changes, permissions, invitation responses and codec round-trips.

Test code is an opt-in, separate test mod. Ordinary runs and release jars do not
include it. No additional production dependency is introduced. Test configuration
uses Loom's separate test source set, as described in the
[Fabric testing documentation](https://docs.fabricmc.net/develop/automatic-testing).

Each run gets a unique directory under `versions/<version>/build/multiplayer-tests/`.
Existing development worlds are never reused or deleted. Servers bind only to
`127.0.0.1` on an automatically selected port and stop after checks. The script only
copies an existing accepted development-server EULA; it does not accept one itself.
Each server task has a three-minute timeout. No test worlds are automatically deleted.

The combined report, per-version status and Gradle log are written under
`build/multiplayer-tests/<run-id>/`. Each version also retains `test-result.txt`
and the server log in its isolated directory. Generated artifacts are removed by
the usual build cleanup; preserve reports elsewhere if you need them longer.

## Required real-client checks

Automated PASS does not certify GUI appearance, client login or input handling.
For **each supported Minecraft version**, run a separate disposable server/world
and join it with at least three actual clients using distinct names. Existing
`runTestClient1`, `runTestClient2`, etc. tasks provide separate client directories.
Use an operator for administration and non-operators for permission checks.

- [ ] All clients connect without payload-registry, codec or login errors.
- [ ] Split assigns every online candidate and all clients show consistent names/colors.
- [ ] Team name changes refresh suggestions and prefixes on teammates and other teams.
- [ ] Ordinary players are denied restricted commands and GUI actions without state changes.
- [ ] Own-team management cannot modify a different team by ID or display name.
- [ ] Invite arrives on the other client; accept/decline update the proper tabs and membership.
- [ ] Leave beside the local player's name removes only that player, not other members.
- [ ] Permission/error modal stays above all page content, especially in 1.21.
- [ ] Hover hints, buttons, clipping, scrolling, expanded groups and Reset behave correctly.
- [ ] Help has no unwanted hover overlays; common hotkeys work without conflicting input.
- [ ] Closing a page or disconnecting during a request does not reopen old pages or lock controls.
- [ ] Clear/rebuild, repeated clicks, invitation expiry and simultaneous actions are safe.
- [ ] Restart preserves teams/settings/permissions and discards transient invitations.
- [ ] Test the intended Java runtime and mod combination, not only the JDK 25 development run.

For 26.3, separately check client startup with the chosen renderer; the headless
server tests do not exercise Vulkan/OpenGL or native graphics drivers.
