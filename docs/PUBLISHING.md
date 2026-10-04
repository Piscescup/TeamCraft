# Publishing TeamCraft

Publishing a GitHub Release automatically builds all supported Minecraft versions, attaches the JARs to that Release, and publishes one version per JAR to Modrinth and CurseForge.

## One-time repository setup

Create the TeamCraft project on both Modrinth and CurseForge before using the workflow. The projects must already exist and your accounts must be allowed to upload files to them.

In the GitHub repository, open **Settings → Secrets and variables → Actions** and add:

### Variables

| Name                    | Value                             |
|-------------------------|-----------------------------------|
| `MODRINTH_PROJECT_ID`   | The Modrinth project ID or slug   |
| `CURSEFORGE_PROJECT_ID` | The numeric CurseForge project ID |

### Secrets

| Name               | Value                                                                           |
|--------------------|---------------------------------------------------------------------------------|
| `MODRINTH_TOKEN`   | A Modrinth personal access token with version creation and project write access |
| `CURSEFORGE_TOKEN` | A CurseForge API token that can upload files to the project                     |

Never commit either token to the repository.

## Create a release

1. Make sure the commit to release contains the correct code and supported-version configuration.
2. On GitHub, open **Releases → Draft a new release**.
3. Create a tag such as `v1.1.0` from that commit.
4. Add the release notes. The same notes are used as the Modrinth and CurseForge changelog.
5. Publish the Release.

The workflow at `.github/workflows/publish-release.yml` starts only when the Release is published. A GitHub pre-release is published as `beta`; a normal GitHub Release is published as `release`.

The release tag is the authoritative mod version. For example, tag `v1.1.0` builds JARs whose embedded mod version is `1.1.0`, even if `mod_version` in `gradle.properties` has not been updated yet.

Do not publish the same tag twice without first removing versions that were already created on Modrinth and CurseForge. Re-running a partially failed workflow can encounter duplicate version numbers on jobs that previously succeeded.
