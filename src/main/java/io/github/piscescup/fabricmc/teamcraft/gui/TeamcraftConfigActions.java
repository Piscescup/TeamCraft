package io.github.piscescup.fabricmc.teamcraft.gui;

import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.minecraft.ChatFormatting;

/** Executes footer actions and coordinates their client/server waiting state. */
final class TeamcraftConfigActions {
    private TeamcraftConfigActions() {
    }

    static void submitConfig(TeamcraftConfigScreen screen, boolean buildTeams) {
        TeamcraftConfigDraft draft = TeamcraftConfigDraft.create(screen);
        if (draft.error() != null) {
            screen.showOverlay(draft.error(), ChatFormatting.RED);
            return;
        }
        screen.waitingForServer = true;
        screen.updateEnabledState();
        if (!TeamcraftConfigClient.save(draft.config(), screen.candidates, buildTeams)) {
            screen.waitingForServer = false;
            screen.updateEnabledState();
        }
    }

    static void submitOwnTeam(TeamcraftConfigScreen screen) {
        if (screen.ownTeam == null) {
            screen.showOverlay(TeamcraftTranslations.GUI_ERROR_TEAM_NOT_FOUND.key(), ChatFormatting.RED);
            return;
        }
        String name = screen.ownTeamName.trim();
        if (name.isEmpty()) {
            screen.showOverlay(TeamcraftTranslations.GUI_ERROR_EMPTY_TEAM_NAME.key(), ChatFormatting.RED);
            return;
        }
        screen.waitingForServer = true;
        screen.updateEnabledState();
        if (!TeamcraftConfigClient.saveOwnTeam(
            screen.ownTeam.id(),
            name,
            screen.ownTeamcraftColor,
            screen.ownTeamFriendlyFire
        )) {
            screen.waitingForServer = false;
            screen.updateEnabledState();
        }
    }

    static void refresh(TeamcraftConfigScreen screen) {
        screen.waitingForServer = true;
        screen.updateEnabledState();
        if (!TeamcraftConfigClient.refresh()) {
            screen.waitingForServer = false;
            screen.updateEnabledState();
        }
    }

    static void clearAllTeams(TeamcraftConfigScreen screen) {
        screen.waitingForServer = true;
        screen.updateEnabledState();
        if (!TeamcraftConfigClient.clearAllTeams()) {
            screen.waitingForServer = false;
            screen.updateEnabledState();
        }
    }

    static void restoreDefaults(TeamcraftConfigScreen screen) {
        screen.loadConfig(TeamcraftConfigData.defaults());
        screen.resetScroll();
        screen.rebuildPage();
        screen.showOverlay(TeamcraftTranslations.GUI_DEFAULTS_READY.key(), ChatFormatting.YELLOW);
    }
}
