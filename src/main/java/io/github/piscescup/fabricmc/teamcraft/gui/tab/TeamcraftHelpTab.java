package io.github.piscescup.fabricmc.teamcraft.gui.tab;

import io.github.piscescup.fabricmc.teamcraft.gui.TeamcraftPageContext;
import io.github.piscescup.fabricmc.teamcraft.gui.TeamcraftPages;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/** Collapsible quick-start sections and a hierarchical command reference. */
public final class TeamcraftHelpTab
    extends TeamcraftTab {
    public TeamcraftHelpTab(TeamcraftPageContext context) {
        super(context, TeamcraftPages.HELP, TeamcraftTranslations.GUI_NAV_HELP.key(),
            TeamcraftTranslations.GUI_NAV_HELP_TOOLTIP.key());
    }

    @Override
    public boolean requiresServer() {
        return false;
    }

    @Override
    protected boolean showHoverTooltips() {
        return false;
    }

    @Override
    protected Component headerHint() {
        return Component.translatable(TeamcraftTranslations.GUI_EXPAND_HINT.key());
    }

    @Override
    protected void build() {
        addHelpSection(TeamcraftTranslations.GUI_HELP_QUICK_START, () -> {
            addText(TeamcraftTranslations.GUI_HELP_CANDIDATES);
            addText(TeamcraftTranslations.GUI_HELP_SPLIT);
            addText(TeamcraftTranslations.GUI_HELP_APPEARANCE);
            addText(TeamcraftTranslations.GUI_HELP_BUILD);
        });
        addHelpSection(TeamcraftTranslations.GUI_HELP_MANAGEMENT_TITLE, () -> {
            addText(TeamcraftTranslations.GUI_HELP_MANAGE_TEAMS);
            addText(TeamcraftTranslations.GUI_HELP_REMOVE_TEAMS);
        });
        addHelpSection(TeamcraftTranslations.GUI_HELP_COMMAND_TITLE, this::addCommandReference);
    }

    private void addCommandReference() {
        addTextRow(Component.translatable(TeamcraftTranslations.GUI_HELP_COMMAND_INTRO.key()), null);

        addCommandGroup(TeamcraftTranslations.GUI_HELP_COMMAND_GENERAL_TITLE,
            new HelpCommand("/teamcraft  |  /teamcraft help", TeamcraftTranslations.GUI_HELP_CMD_HELP),
            new HelpCommand("/teamcraft status", TeamcraftTranslations.GUI_HELP_CMD_STATUS));

        addCommandGroup(TeamcraftTranslations.GUI_HELP_COMMAND_CANDIDATES_TITLE,
            new HelpCommand("/teamcraft init <players...>", TeamcraftTranslations.GUI_HELP_CMD_INIT),
            new HelpCommand("/teamcraft init add <players...>", TeamcraftTranslations.GUI_HELP_CMD_INIT_ADD),
            new HelpCommand("/teamcraft init remove <players...>", TeamcraftTranslations.GUI_HELP_CMD_INIT_REMOVE),
            new HelpCommand("/teamcraft init list", TeamcraftTranslations.GUI_HELP_CMD_INIT_LIST),
            new HelpCommand("/teamcraft init clear", TeamcraftTranslations.GUI_HELP_CMD_INIT_CLEAR));

        addCommandGroup(TeamcraftTranslations.GUI_HELP_COMMAND_CONFIG_TITLE,
            new HelpCommand("/teamcraft config players-per-team <players>",
                TeamcraftTranslations.GUI_HELP_CMD_CONFIG_PLAYERS_PER_TEAM),
            new HelpCommand("/teamcraft config team-count <teams>",
                TeamcraftTranslations.GUI_HELP_CMD_CONFIG_TEAM_COUNT),
            new HelpCommand("/teamcraft config mode <fixed|random>",
                TeamcraftTranslations.GUI_HELP_CMD_CONFIG_MODE),
            new HelpCommand("/teamcraft config friendlyfire <true|false>",
                TeamcraftTranslations.GUI_HELP_CMD_CONFIG_FRIENDLY_FIRE),
            new HelpCommand("/teamcraft config colors <colors...>",
                TeamcraftTranslations.GUI_HELP_CMD_CONFIG_COLORS),
            new HelpCommand("/teamcraft config colors reset",
                TeamcraftTranslations.GUI_HELP_CMD_CONFIG_COLORS_RESET),
            new HelpCommand("/teamcraft config names <names...>",
                TeamcraftTranslations.GUI_HELP_CMD_CONFIG_NAMES),
            new HelpCommand("/teamcraft config names reset",
                TeamcraftTranslations.GUI_HELP_CMD_CONFIG_NAMES_RESET),
            new HelpCommand("/teamcraft config reset", TeamcraftTranslations.GUI_HELP_CMD_CONFIG_RESET));

        addCommandGroup(TeamcraftTranslations.GUI_HELP_COMMAND_BUILD_TITLE,
            new HelpCommand("/teamcraft build-teams", TeamcraftTranslations.GUI_HELP_CMD_BUILD),
            new HelpCommand("/teamcraft build-teams colors <colors...>",
                TeamcraftTranslations.GUI_HELP_CMD_BUILD_COLORS),
            new HelpCommand("/teamcraft build-teams names <names...>",
                TeamcraftTranslations.GUI_HELP_CMD_BUILD_NAMES));

        addCommandGroup(TeamcraftTranslations.GUI_HELP_COMMAND_TEAM_TITLE,
            new HelpCommand("/teamcraft manage team <team> color <color>",
                TeamcraftTranslations.GUI_HELP_CMD_TEAM_COLOR),
            new HelpCommand("/teamcraft manage team <team> name <name>",
                TeamcraftTranslations.GUI_HELP_CMD_TEAM_NAME),
            new HelpCommand("/teamcraft manage team <team> friendlyfire <true|false>",
                TeamcraftTranslations.GUI_HELP_CMD_TEAM_FRIENDLY_FIRE),
            new HelpCommand("/teamcraft manage team <team> info",
                TeamcraftTranslations.GUI_HELP_CMD_TEAM_INFO),
            new HelpCommand("/teamcraft invite player <player>", TeamcraftTranslations.HELP_INVITE),
            new HelpCommand("/teamcraft invite accept", TeamcraftTranslations.HELP_INVITE_ACCEPT),
            new HelpCommand("/teamcraft invite decline", TeamcraftTranslations.HELP_INVITE_DECLINE),
            new HelpCommand("/teamcraft manage leave", TeamcraftTranslations.HELP_LEAVE_TEAM));

        addCommandGroup(TeamcraftTranslations.GUI_HELP_COMMAND_CLEANUP_TITLE,
            new HelpCommand("/teamcraft clear", TeamcraftTranslations.GUI_HELP_CMD_CLEAR),
            new HelpCommand("/teamcraft reset", TeamcraftTranslations.GUI_HELP_CMD_RESET));
    }

    private void addHelpSection(TeamcraftTranslations title, Runnable contents) {
        addSection(title.key(), Component.translatable(title.key()).withStyle(ChatFormatting.WHITE),
            null, null, false, contents);
    }

    private void addCommandGroup(TeamcraftTranslations title, HelpCommand... commands) {
        addHelpSection(title, () -> {
            for (HelpCommand command : commands) {
                addCommand(command);
            }
        });
    }

    private void addText(TeamcraftTranslations text) {
        Component component = Component.translatable(text.key());
        addTextRow(component, null);
    }

    private void addCommand(HelpCommand command) {
        Component syntax = Component.literal(command.syntax()).withStyle(ChatFormatting.YELLOW);
        // Translation keys remain stable if groups are reordered or labels change.
        addSection("command/" + command.details().key(), syntax, null, null, false, () -> {
            // Repeat the full syntax here so it wraps rather than truncates on narrow screens.
            addTextRow(syntax.copy().withStyle(ChatFormatting.BOLD), null);
            addTextRow(Component.translatable(command.details().key()).withStyle(ChatFormatting.GRAY), null);
        });
    }

    private record HelpCommand(String syntax, TeamcraftTranslations details) {
    }
}
