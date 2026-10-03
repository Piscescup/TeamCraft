package io.github.piscescup.fabricmc.teamcraft.gui;

import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.minecraft.network.chat.Component;

/** Validates the editable GUI state and produces the server-bound configuration. */
record TeamcraftConfigDraft(TeamcraftConfigData config, Component error) {
    static TeamcraftConfigDraft create(TeamcraftConfigScreen screen) {
        TeamcraftConfigData defaults = TeamcraftConfigData.defaults();
        Integer playersPerTeam = parseBoundedInteger(
            screen.playersPerTeamText,
            TeamcraftConfigData.MIN_PLAYERS_PER_TEAM,
            TeamcraftConfigData.MAX_PLAYERS_PER_TEAM
        );
        Integer teamCount = parseBoundedInteger(
            screen.teamCountText,
            TeamcraftConfigData.MIN_TEAM_COUNT,
            TeamcraftConfigData.MAX_TEAM_COUNT
        );

        // Only the value belonging to the selected split rule is user-facing.
        // A stale invalid value from the hidden rule must not block saving.
        if (!screen.fixedTeamCount && playersPerTeam == null) {
            return error(Component.translatable(
                TeamcraftTranslations.GUI_ERROR_PLAYERS_PER_TEAM.key(),
                TeamcraftConfigData.MIN_PLAYERS_PER_TEAM,
                TeamcraftConfigData.MAX_PLAYERS_PER_TEAM
            ));
        }
        if (screen.fixedTeamCount && teamCount == null) {
            return error(Component.translatable(
                TeamcraftTranslations.GUI_ERROR_TEAM_COUNT.key(),
                TeamcraftConfigData.MIN_TEAM_COUNT,
                TeamcraftConfigData.MAX_TEAM_COUNT
            ));
        }
        if (playersPerTeam == null) {
            playersPerTeam = defaults.playersPerTeam();
        }
        if (teamCount == null) {
            teamCount = defaults.teamCount();
        }

        if (screen.configuredNames.size() > TeamcraftConfigData.MAX_LIST_SIZE) {
            return error(Component.translatable(
                TeamcraftTranslations.GUI_ERROR_TOO_MANY_VALUES.key(),
                TeamcraftConfigData.MAX_LIST_SIZE
            ));
        }

        return new TeamcraftConfigDraft(new TeamcraftConfigData(
            screen.fixedTeamCount,
            playersPerTeam,
            teamCount,
            screen.mode,
            screen.friendlyFire,
            screen.configuredColors,
            screen.configuredNames
        ), null);
    }

    private static TeamcraftConfigDraft error(Component error) {
        return new TeamcraftConfigDraft(null, error);
    }

    private static Integer parseBoundedInteger(String value, int minimum, int maximum) {
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed >= minimum && parsed <= maximum ? parsed : null;
        }
        catch (NumberFormatException ignored) {
            return null;
        }
    }
}
