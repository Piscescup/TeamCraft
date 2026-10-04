# TeamCraft

TeamCraft is a Fabric mod for quickly creating and managing Minecraft teams.
Use its graphical interface or commands to select players, configure team rules, choose names and colors, 
control friendly fire, and automatically assign players to teams.

## Features

- Split players by team size or by a fixed number of teams
- Assign players in list order or randomly
- Customize team names and color rotation
- View and manage your own team
- Browse, edit, or disband existing teams
- Set a separate permission level for each command
- Config by gui

## Requirements

TeamCraft requires:

|  Dependency   |                        Version                         |  Required   |
|:-------------:|:------------------------------------------------------:|:-----------:|
| Fabric Loader |                      \>=`0.19.5`                       |     Yes     |
|  Fabric API   | [Fabric API Page](https://modrinth.com/mod/fabric-api) |     Yes     |
|     Java      |                     `JDK` Maching                      |     Yes     |
|   Mod Menu    |   [Mod Menu Page](https://modrinth.com/mod/modmenu)    | Recommanded |

The following table shows the minimum Java version required for each supported Minecraft version:

|                       Minecraft version                        |   Java version   |
|:--------------------------------------------------------------:|:----------------:|
| 1.21, 1.21.1, 1.21.3, 1.21.4, 1.21.5, 1.21.8, 1.21.10, 1.21.11 | Java 21 or newer |
|                       26.1.2, 26.2, 26.3                       | Java 25 or newer |


> [!IMPORTANT]
> For multiplayer, the server and the clients must have **TeamCraft** installed. 


## Installation

1. Install Fabric Loader for your Minecraft version.
2. Download the matching Fabric API and TeamCraft releases.
3. Place both JAR files in the game's or server's `mods` folder.
4. Start the game or server.

## Quick Start

### Using the graphical interface

1. Join a world or server with TeamCraft installed.
2. Press `Backspace` to open TeamCraft, or click TeamCraft's configuration button in Mod Menu.
3. Add players to the candidate list on the Team Setup page.
4. Choose a split rule:
   - **Players per team:** limits the maximum size of each team and 
   calculates the required number of teams automatically.
   - **Fixed team count:** creates the specified number of teams and distributes players as evenly as possible.
5. Choose ordered or random assignment and configure friendly fire.
6. Add custom team names and colors if needed.
7. Click **Apply** to save the settings, or **Save & Split** to create the teams immediately.

Click **[+]** to expand a settings group. Hover over a setting to see its purpose and examples.
Page shortcuts can be changed on TeamCraft's **Hotkeys** page or in Minecraft's **Options → Controls** menu.

### Using commands

The following example selects all online players and randomly assigns them to teams of up to two players:

```text
/teamcraft init @a
/teamcraft config players-per-team 2
/teamcraft config mode random
/teamcraft config friendlyfire false
/teamcraft build-teams
```

To create exactly three teams instead, use:

```text
/teamcraft config team-count 3
/teamcraft build-teams
```

## GUI Pages

### Team Setup

Manage candidate players, split rules, assignment order, friendly fire, team colors, and team names.

### My Team

View your current TeamCraft team and change its name, color, or friendly-fire setting.
Changing the color does not rename the team.
Click **Leave Team** beside your own name in the member list to leave your TeamCraft team. This does not save pending edits,
disband the team, or remove other members; empty teams are kept.

### All Teams

Browse all teams managed by TeamCraft, inspect their members and settings, open team details, or disband teams.

### Invitations

Open the **Invitations** tab while connected to a TeamCraft server. Under **Invite Players**,
click an online player to invite them to your current team. Only players without a team are listed.
Under **Received Invitation**, check the inviter, team, and remaining time, then click **Accept**
or **Decline**. This page refreshes automatically while open; **Refresh** also updates it manually.
Joining updates **My Team** and **All Teams** without changing your unsaved split configuration.

Sending requires membership in a TeamCraft team and the `invite` permission. Accepting and declining
do not require permission to send invitations. Invitations expire after 120 seconds.

### Hotkeys

Bind shortcuts to open **Team Setup**, **All Teams**, or **My Team** directly while playing.
Team Setup defaults to `Backspace`; the other two shortcuts are unbound by default.

Click a binding button, then press a keyboard or mouse button. Press `Esc` to unbind it,
use **×** to clear it, or **RESET** to restore its default. Conflicting bindings are shown in red.
Bindings are saved locally, and server permissions still apply when opening or editing team settings.

### Help

View instructions for the graphical interface and the complete command reference. 
Mod Menu can also open this read-only Help page when you are not in a world.

## Command Guide

### Candidate players

|              Command              |                                        Description                                         |
|:---------------------------------:|:------------------------------------------------------------------------------------------:|
|  `/teamcraft init <players...>`   | Replace the candidate list with the selected players; selectors such as `@a` are supported |
|  `/teamcraft init add <player>`   |                              Add player to the candidate list                              |
| `/teamcraft init remove <player>` |                           Remove player from the candidate list                            |
|      `/teamcraft init list`       |                              Show the current candidate list                               |
|      `/teamcraft init clear`      |                                  Clear the candidate list                                  |

### Team configuration

|                    Command                     |                             Description                              |
|:----------------------------------------------:|:--------------------------------------------------------------------:|
| `/teamcraft config players-per-team <players>` | Calculate the team count from the maximum number of players per team |
|     `/teamcraft config team-count <teams>`     |                    Create a fixed number of teams                    |
|    `/teamcraft config mode <fixed\|random>`    |               Assign players in list order or randomly               |
| `/teamcraft config friendlyfire <true\|false>` |                   Enable or disable friendly fire                    |
|     `/teamcraft config colors <colors...>`     |         Set the color rotation, for example `red blue green`         |
|        `/teamcraft config colors reset`        |                  Restore the default color rotation                  |
|      `/teamcraft config names <names...>`      |        Set team names; names containing spaces must be quoted        |
|        `/teamcraft config names reset`         |                     Restore automatic team names                     |
|           `/teamcraft config reset`            |                Restore the default team configuration                |

### Creating and managing teams

|                          Command                           |                                  Description                                  |
|:----------------------------------------------------------:|:-----------------------------------------------------------------------------:|
|                  `/teamcraft build-teams`                  |          Create teams and assign players using the current settings           |
|        `/teamcraft build-teams colors <colors...>`         |                  Create teams with a one-time color override                  |
|         `/teamcraft build-teams names <names...>`          |                  Create teams with a one-time name override                   |
|                    `/teamcraft status`                     |          Show the candidate list, current settings, and active teams          |
|                 `/teamcraft manage leave`                  | Leave your own TeamCraft team without disbanding it or removing other members |
|            `/teamcraft manage team <team> info`            |                     Show information about your own team                      |
|       `/teamcraft manage team <team> color <color>`        |               Change your own team's color without renaming it                |
|        `/teamcraft manage team <team> name <name>`         |                      Change your own team's display name                      |
| `/teamcraft manage team <team> friendlyfire <true\|false>` |                 Change your own team's friendly-fire setting                  |
|                     `/teamcraft clear`                     |  Disband TeamCraft teams while keeping the candidate list and configuration   |
|                     `/teamcraft reset`                     |   Disband teams, clear the candidate list, and restore all default settings   |
|                     `/teamcraft help`                      |                           Show command help in chat                           |

Minecraft's command suggestions can be used to select players, teams, colors, and permission levels.

For `/teamcraft manage team`, `<team>` must be your own TeamCraft team's ID or current
display name. Suggestions only include these two values. Renaming updates the suggested
name while the ID stays unchanged. Quote names containing spaces. You cannot manage
another player's team through this command, and the existing permission checks still apply.

### Team invitations

Any member of a TeamCraft team can invite an online player who does not already belong to a team:

```text
/teamcraft invite player PlayerName
```

The invited player can respond with:

```text
/teamcraft invite accept
/teamcraft invite decline
```

Invitations expire after 120 seconds. Each player can have one pending invitation;
another invitation does not overwrite it. Accepting joins the inviter's existing team
without changing its name, color, or settings. Invitations become invalid if the team
is disbanded or the inviter leaves it, and are discarded when the server stops.
Invitations do not change the split candidate list or enforce the players-per-team split setting.
Use `/teamcraft manage leave`, or **Leave Team** on the **My Team** page, before accepting
an invitation to a different team. Leaving cancels pending invitations sent or received
by you. It does not remove you from the split candidate list or change the configuration;
a future split can assign you again if you remain a candidate.

## Permissions

Game mode and command permissions are independent. A player in Survival mode may still have administrator permissions when they are a server operator, the owner of a single-player world, or playing with cheats enabled.

Check a permission setting:

```text
/teamcraft permission <key> get
```

Change a permission setting:

```text
/teamcraft permission <key> set <level>
```

Available permission levels:

```text
all < moderators < gamemasters < admins < owners
```


Available permission keys and the commands they control:


|            Command             |            Permission Key            | Default Vaule |
|:------------------------------:|:------------------------------------:|:-------------:|
|          `/teamcraft`          |                `root`                |     `all`     |
|     `/teamcraft init ...`      |                `init`                |     `all`     |
|     `/teamcraft config ..`     |               `config`               |     `all`     |
|    `/teamcraft status ...`     |               `status`               |     `all`     |
|    `/teamcraft manage ...`     |               `manage`               | `gamemasters` |
|     `/teamcraft build ...`     |               `build`                | `gamemasters` |
|  `/teamcraft permission ...`   |             `permission`             |   `admins`    |
|       `/teamcraft clear`       |               `clear`                | `gamemasters` |
|       `/teamcraft reset`       |               `reset`                |   `admins`    |
|       `/teamcraft help`        |                `help`                |     `all`     |
| `/teamcraft invite player ...` |               `invite`               |     `all`     |
|   `/teamcraft manage leave`    | `leaveteam` (also requires `manage`) |     `all`     |



By default, `init`, `config`, `build`, `status`, and `help` are available to all players. The `manage`, `clear`, `reset`, and `permission` commands require the `gamemasters` permission level.

For example, to allow only game masters and higher-level users to create teams:

```text
/teamcraft permission build set gamemasters
```

Setting `root` to an administrator-only level hides the entire `/teamcraft` command tree from players without the required permission.

The `invite` permission controls sending invitations. Accepting or declining requires
access to the root command, but does not require the sending permission.

## Notes

- TeamCraft saves candidate lists, team configuration, and permission settings per world.
- Use `clear` when you want to split the players again without losing the current setup.
- Use `reset` to remove TeamCraft teams and restore the default state.
- Changing an existing team's color changes its display color without changing its name.
- If the server rejects an action from the graphical interface, the client displays a permission-denied dialog.

## License

TeamCraft is available under the [Apache License 2.0](LICENSE).
