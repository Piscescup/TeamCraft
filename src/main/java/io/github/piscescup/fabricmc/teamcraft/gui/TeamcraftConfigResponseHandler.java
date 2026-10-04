package io.github.piscescup.fabricmc.teamcraft.gui;

import io.github.piscescup.fabricmc.teamcraft.gui.network.ConfigSyncPayload;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import io.github.piscescup.fabricmc.teamcraft.gui.TeamcraftFeedbackScreen.Type;

/** Applies authoritative server responses to the configuration context. */
final class TeamcraftConfigResponseHandler {
    private TeamcraftConfigResponseHandler() {
    }

    static void handle(TeamcraftPageContext context, ConfigSyncPayload payload) {
        context.waitingForServer = false;

        // Apply the authoritative snapshot before showing a result or selecting its return tab.
        String messageKey;
        Type type = Type.ERROR;

        switch (payload.response()) {
            case OPENED -> {
                loadAll(context, payload);
                messageKey = TeamcraftTranslations.GUI_REFRESHED.key();
                type = Type.SUCCESS;
            }
            case SAVED -> {
                loadAll(context, payload);
                messageKey = TeamcraftTranslations.GUI_SAVED.key();
                type = Type.SUCCESS;
            }
            case BUILT -> {
                loadAll(context, payload);
                context.selectPage(TeamcraftPages.ALL_TEAMS);
                messageKey = TeamcraftTranslations.GUI_BUILT.key();
                type = Type.SUCCESS;
            }
            case TEAM_SAVED -> {
                context.loadTeams(payload.ownTeam(), payload.teams());
                messageKey = TeamcraftTranslations.GUI_TEAM_SAVED.key();
                type = Type.SUCCESS;
            }
            case TEAM_LEFT -> {
                context.loadTeams(payload.ownTeam(), payload.teams());
                context.selectPage(TeamcraftPages.OWN_TEAM);
                messageKey = TeamcraftTranslations.GUI_TEAM_LEFT.key();
                type = Type.SUCCESS;
            }
            case NOT_IN_TEAM -> {
                context.loadTeams(payload.ownTeam(), payload.teams());
                messageKey = TeamcraftTranslations.ERROR_NOT_IN_TEAM.key();
            }
            case INVALID -> messageKey = TeamcraftTranslations.GUI_ERROR_INVALID_SERVER.key();
            case PERMISSION_DENIED -> {
                context.rebuildPage();
                PermissionDeniedScreen.open(context.screen());
                return;
            }
            case NO_CANDIDATES -> {
                context.loadConfig(payload.config());
                context.loadCandidates(payload.candidates(), payload.onlinePlayers());
                messageKey = TeamcraftTranslations.GUI_ERROR_NO_CANDIDATES.key();
            }
            case TEAMS_EXIST -> {
                loadAll(context, payload);
                messageKey = TeamcraftTranslations.GUI_ERROR_TEAMS_EXIST.key();
            }
            case TOO_MANY_TEAMS -> {
                context.loadConfig(payload.config());
                context.loadCandidates(payload.candidates(), payload.onlinePlayers());
                messageKey = TeamcraftTranslations.GUI_ERROR_TOO_MANY_TEAMS.key();
            }
            case TEAM_NOT_FOUND -> {
                context.loadTeams(payload.ownTeam(), payload.teams());
                messageKey = TeamcraftTranslations.GUI_ERROR_TEAM_NOT_FOUND.key();
            }
            case TEAM_DISBANDED -> {
                context.loadTeams(payload.ownTeam(), payload.teams());
                context.selectPage(TeamcraftPages.ALL_TEAMS);
                messageKey = TeamcraftTranslations.GUI_DETAILS_DISBANDED.key();
                type = Type.SUCCESS;
            }
            case ALL_TEAMS_CLEARED -> {
                context.loadTeams(payload.ownTeam(), payload.teams());
                context.selectPage(TeamcraftPages.ALL_TEAMS);
                messageKey = TeamcraftTranslations.GUI_ALL_TEAMS_CLEARED.key();
                type = Type.SUCCESS;
            }
            default -> throw new IllegalStateException("Unhandled response: " + payload.response());
        }
        context.rebuildPage();
        context.showFeedback(messageKey, type);
    }

    private static void loadAll(TeamcraftPageContext context, ConfigSyncPayload payload) {
        context.loadConfig(payload.config());
        context.loadCandidates(payload.candidates(), payload.onlinePlayers());
        context.loadTeams(payload.ownTeam(), payload.teams());
    }
}
