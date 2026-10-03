package io.github.piscescup.fabricmc.teamcraft.gui;

import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/** Builds the quick-start and command-reference page. */
final class TeamcraftHelpPage {
    private TeamcraftHelpPage() {
    }

    static void build(TeamcraftConfigScreen screen) {
        addQuickStart(screen);
        addCommandReference(screen);
    }

    private static void addQuickStart(TeamcraftConfigScreen screen) {
        screen.addHeader(TeamcraftTranslations.GUI_HELP_QUICK_START.key());
        addText(screen, TeamcraftTranslations.GUI_HELP_CANDIDATES);
        addText(screen, TeamcraftTranslations.GUI_HELP_SPLIT);
        addText(screen, TeamcraftTranslations.GUI_HELP_APPEARANCE);
        addText(screen, TeamcraftTranslations.GUI_HELP_BUILD);

        screen.addHeader(TeamcraftTranslations.GUI_HELP_MANAGEMENT_TITLE.key());
        addText(screen, TeamcraftTranslations.GUI_HELP_MANAGE_TEAMS);
        addText(screen, TeamcraftTranslations.GUI_HELP_REMOVE_TEAMS);
    }

    private static void addCommandReference(TeamcraftConfigScreen screen) {
        screen.addHeader(TeamcraftTranslations.GUI_HELP_COMMAND_TITLE.key());
        screen.addTextRow(Component.translatable(TeamcraftTranslations.GUI_HELP_COMMAND_INTRO.key()), null);

        screen.addHeader(TeamcraftTranslations.GUI_HELP_COMMAND_GENERAL_TITLE.key());
        addCommand(screen, "/teamcraft  |  /teamcraft help", TeamcraftTranslations.GUI_HELP_CMD_HELP);
        addCommand(screen, "/teamcraft status", TeamcraftTranslations.GUI_HELP_CMD_STATUS);

        screen.addHeader(TeamcraftTranslations.GUI_HELP_COMMAND_CANDIDATES_TITLE.key());
        addCommand(screen, "/teamcraft init <players...>", TeamcraftTranslations.GUI_HELP_CMD_INIT);
        addCommand(screen, "/teamcraft init add <players...>", TeamcraftTranslations.GUI_HELP_CMD_INIT_ADD);
        addCommand(screen, "/teamcraft init remove <players...>", TeamcraftTranslations.GUI_HELP_CMD_INIT_REMOVE);
        addCommand(screen, "/teamcraft init list", TeamcraftTranslations.GUI_HELP_CMD_INIT_LIST);
        addCommand(screen, "/teamcraft init clear", TeamcraftTranslations.GUI_HELP_CMD_INIT_CLEAR);

        screen.addHeader(TeamcraftTranslations.GUI_HELP_COMMAND_CONFIG_TITLE.key());
        addCommand(screen, "/teamcraft config players-per-team <players>",
            TeamcraftTranslations.GUI_HELP_CMD_CONFIG_PLAYERS_PER_TEAM);
        addCommand(screen, "/teamcraft config team-count <teams>",
            TeamcraftTranslations.GUI_HELP_CMD_CONFIG_TEAM_COUNT);
        addCommand(screen, "/teamcraft config mode <fixed|random>",
            TeamcraftTranslations.GUI_HELP_CMD_CONFIG_MODE);
        addCommand(screen, "/teamcraft config friendlyfire <true|false>",
            TeamcraftTranslations.GUI_HELP_CMD_CONFIG_FRIENDLY_FIRE);
        addCommand(screen, "/teamcraft config colors <colors...>",
            TeamcraftTranslations.GUI_HELP_CMD_CONFIG_COLORS);
        addCommand(screen, "/teamcraft config colors reset",
            TeamcraftTranslations.GUI_HELP_CMD_CONFIG_COLORS_RESET);
        addCommand(screen, "/teamcraft config names <names...>",
            TeamcraftTranslations.GUI_HELP_CMD_CONFIG_NAMES);
        addCommand(screen, "/teamcraft config names reset",
            TeamcraftTranslations.GUI_HELP_CMD_CONFIG_NAMES_RESET);
        addCommand(screen, "/teamcraft config reset", TeamcraftTranslations.GUI_HELP_CMD_CONFIG_RESET);

        screen.addHeader(TeamcraftTranslations.GUI_HELP_COMMAND_BUILD_TITLE.key());
        addCommand(screen, "/teamcraft build-teams", TeamcraftTranslations.GUI_HELP_CMD_BUILD);
        addCommand(screen, "/teamcraft build-teams colors <colors...>",
            TeamcraftTranslations.GUI_HELP_CMD_BUILD_COLORS);
        addCommand(screen, "/teamcraft build-teams names <names...>",
            TeamcraftTranslations.GUI_HELP_CMD_BUILD_NAMES);

        screen.addHeader(TeamcraftTranslations.GUI_HELP_COMMAND_TEAM_TITLE.key());
        addCommand(screen, "/teamcraft team-manage <team> color <color>",
            TeamcraftTranslations.GUI_HELP_CMD_TEAM_COLOR);
        addCommand(screen, "/teamcraft team-manage <team> name <name>",
            TeamcraftTranslations.GUI_HELP_CMD_TEAM_NAME);
        addCommand(screen, "/teamcraft team-manage <team> friendlyfire <true|false>",
            TeamcraftTranslations.GUI_HELP_CMD_TEAM_FRIENDLY_FIRE);
        addCommand(screen, "/teamcraft team-manage <team> info",
            TeamcraftTranslations.GUI_HELP_CMD_TEAM_INFO);

        screen.addHeader(TeamcraftTranslations.GUI_HELP_COMMAND_CLEANUP_TITLE.key());
        addCommand(screen, "/teamcraft clear", TeamcraftTranslations.GUI_HELP_CMD_CLEAR);
        addCommand(screen, "/teamcraft reset", TeamcraftTranslations.GUI_HELP_CMD_RESET);
    }

    private static void addText(TeamcraftConfigScreen screen, TeamcraftTranslations text) {
        Component component = Component.translatable(text.key());
        screen.addTextRow(component, component);
    }

    private static void addCommand(
        TeamcraftConfigScreen screen,
        String command,
        TeamcraftTranslations details
    ) {
        Component text = Component.literal(command)
            .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)
            .append("\n")
            .append(Component.translatable(details.key()).withStyle(ChatFormatting.GRAY));
        screen.addTextRow(text, null);
    }
}
