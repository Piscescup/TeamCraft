package io.github.piscescup.fabricmc.teamcraft.gui;

import io.github.piscescup.fabricmc.teamcraft.gui.network.ConfigSyncPayload;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.minecraft.ChatFormatting;

/** Applies authoritative server responses to the configuration screen. */
final class TeamcraftConfigResponseHandler {
    private TeamcraftConfigResponseHandler() {
    }

    static void handle(TeamcraftConfigScreen screen, ConfigSyncPayload payload) {
        screen.waitingForServer = false;

        switch (payload.response()) {
            case OPENED -> {
                loadAll(screen, payload);
                screen.showOverlay(TeamcraftTranslations.GUI_REFRESHED.key(), ChatFormatting.GREEN);
            }
            case SAVED -> {
                loadAll(screen, payload);
                screen.showOverlay(TeamcraftTranslations.GUI_SAVED.key(), ChatFormatting.GREEN);
            }
            case BUILT -> {
                loadAll(screen, payload);
                screen.page = TeamcraftConfigPage.ALL_TEAMS;
                screen.resetScroll();
                screen.showOverlay(TeamcraftTranslations.GUI_BUILT.key(), ChatFormatting.GREEN);
            }
            case TEAM_SAVED -> {
                screen.loadTeams(payload.ownTeam(), payload.teams());
                screen.showOverlay(TeamcraftTranslations.GUI_TEAM_SAVED.key(), ChatFormatting.GREEN);
            }
            case INVALID -> screen.showOverlay(
                TeamcraftTranslations.GUI_ERROR_INVALID_SERVER.key(),
                ChatFormatting.RED
            );
            case PERMISSION_DENIED -> {
                PermissionDeniedScreen.open(screen);
                return;
            }
            case NO_CANDIDATES -> {
                screen.loadConfig(payload.config());
                screen.loadCandidates(payload.candidates(), payload.onlinePlayers());
                screen.showOverlay(TeamcraftTranslations.GUI_ERROR_NO_CANDIDATES.key(), ChatFormatting.RED);
            }
            case TEAMS_EXIST -> {
                loadAll(screen, payload);
                screen.showOverlay(TeamcraftTranslations.GUI_ERROR_TEAMS_EXIST.key(), ChatFormatting.RED);
            }
            case TOO_MANY_TEAMS -> {
                screen.loadConfig(payload.config());
                screen.loadCandidates(payload.candidates(), payload.onlinePlayers());
                screen.showOverlay(TeamcraftTranslations.GUI_ERROR_TOO_MANY_TEAMS.key(), ChatFormatting.RED);
            }
            case TEAM_NOT_FOUND -> {
                screen.loadTeams(payload.ownTeam(), payload.teams());
                screen.showOverlay(TeamcraftTranslations.GUI_ERROR_TEAM_NOT_FOUND.key(), ChatFormatting.RED);
            }
            case TEAM_DISBANDED -> {
                screen.loadTeams(payload.ownTeam(), payload.teams());
                screen.page = TeamcraftConfigPage.ALL_TEAMS;
                screen.resetScroll();
                screen.showOverlay(TeamcraftTranslations.GUI_DETAILS_DISBANDED.key(), ChatFormatting.GREEN);
            }
            case ALL_TEAMS_CLEARED -> {
                screen.loadTeams(payload.ownTeam(), payload.teams());
                screen.page = TeamcraftConfigPage.ALL_TEAMS;
                screen.resetScroll();
                screen.showOverlay(TeamcraftTranslations.GUI_ALL_TEAMS_CLEARED.key(), ChatFormatting.GREEN);
            }
        }
        screen.rebuildPage();
    }

    private static void loadAll(TeamcraftConfigScreen screen, ConfigSyncPayload payload) {
        screen.loadConfig(payload.config());
        screen.loadCandidates(payload.candidates(), payload.onlinePlayers());
        screen.loadTeams(payload.ownTeam(), payload.teams());
    }
}
