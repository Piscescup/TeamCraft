package io.github.piscescup.fabricmc.teamcraft.gui;

import io.github.piscescup.fabricmc.teamcraft.config.TeamcraftConfigData;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import io.github.piscescup.fabricmc.teamcraft.gui.TeamcraftFeedbackScreen.Type;

/** Executes footer actions and coordinates their client/server waiting state. */
public final class TeamcraftConfigActions {
    private TeamcraftConfigActions() {
    }

    public static void submitConfig(TeamcraftPageContext context, boolean buildTeams) {
        TeamcraftConfigDraft draft = TeamcraftConfigDraft.create(context);
        if (draft.error() != null) {
            context.showFeedback(draft.error(), Type.ERROR);
            return;
        }
        context.waitingForServer = true;
        context.updateEnabledState();
        if (!TeamcraftConfigClient.save(draft.config(), context.candidates, buildTeams)) {
            unsupportedServer(context);
        }
    }

    public static void submitOwnTeam(TeamcraftPageContext context) {
        if (context.ownTeam == null) {
            context.showFeedback(TeamcraftTranslations.GUI_ERROR_TEAM_NOT_FOUND.key(), Type.ERROR);
            return;
        }
        String name = context.ownTeamName.trim();
        if (name.isEmpty()) {
            context.showFeedback(TeamcraftTranslations.GUI_ERROR_EMPTY_TEAM_NAME.key(), Type.ERROR);
            return;
        }
        context.waitingForServer = true;
        context.updateEnabledState();
        if (!TeamcraftConfigClient.saveOwnTeam(
            context.ownTeam.id(),
            name,
            context.ownTeamcraftColor,
            context.ownTeamFriendlyFire
        )) {
            unsupportedServer(context);
        }
    }

    public static void refresh(TeamcraftPageContext context) {
        context.waitingForServer = true;
        context.updateEnabledState();
        if (!TeamcraftConfigClient.refresh()) {
            unsupportedServer(context);
        }
    }

    public static void leaveTeam(TeamcraftPageContext context) {
        if (context.waitingForServer) {
            return;
        }
        if (context.ownTeam == null) {
            context.showFeedback(TeamcraftTranslations.ERROR_NOT_IN_TEAM.key(), Type.ERROR);
            return;
        }
        context.waitingForServer = true;
        context.updateEnabledState();
        if (!TeamcraftConfigClient.leaveTeam(context.ownTeam.id())) {
            unsupportedServer(context);
        }
    }

    public static void clearAllTeams(TeamcraftPageContext context) {
        context.waitingForServer = true;
        context.updateEnabledState();
        if (!TeamcraftConfigClient.clearAllTeams()) {
            unsupportedServer(context);
        }
    }

    public static void restoreDefaults(TeamcraftPageContext context) {
        context.loadConfig(TeamcraftConfigData.defaults());
        context.resetScroll();
        context.rebuildPage();
        context.showFeedback(TeamcraftTranslations.GUI_DEFAULTS_READY.key(), Type.INFO);
    }

    private static void unsupportedServer(TeamcraftPageContext context) {
        context.waitingForServer = false;
        context.updateEnabledState();
        context.showFeedback(TeamcraftTranslations.GUI_ERROR_SERVER_UNSUPPORTED.key(), Type.ERROR);
    }
}
