package io.github.piscescup.fabricmc.teamcraft.gui;

import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.minecraft.network.chat.Component;

/** Routes each navigation item to the class responsible for building that page. */
enum TeamcraftConfigPage {
    TEAM_CONFIG(
        TeamcraftTranslations.GUI_NAV_TEAM_CONFIG.key(),
        TeamcraftTranslations.GUI_NAV_TEAM_CONFIG_TOOLTIP.key(),
        TeamcraftTeamConfigPage::build
    ),
    OWN_TEAM(
        TeamcraftTranslations.GUI_NAV_OWN_TEAM.key(),
        TeamcraftTranslations.GUI_NAV_OWN_TEAM_TOOLTIP.key(),
        TeamcraftOwnTeamPage::build
    ),
    ALL_TEAMS(
        TeamcraftTranslations.GUI_NAV_ALL_TEAMS.key(),
        TeamcraftTranslations.GUI_NAV_ALL_TEAMS_TOOLTIP.key(),
        TeamcraftAllTeamsPage::build
    ),
    HELP(
        TeamcraftTranslations.GUI_NAV_HELP.key(),
        TeamcraftTranslations.GUI_NAV_HELP_TOOLTIP.key(),
        TeamcraftHelpPage::build
    );

    final String tooltipKey;
    private final String labelKey;
    private final PageBuilder builder;

    TeamcraftConfigPage(String labelKey, String tooltipKey, PageBuilder builder) {
        this.labelKey = labelKey;
        this.tooltipKey = tooltipKey;
        this.builder = builder;
    }

    Component displayName() {
        return Component.translatable(this.labelKey);
    }

    void build(TeamcraftConfigScreen screen) {
        this.builder.build(screen);
    }

    @FunctionalInterface
    private interface PageBuilder {
        void build(TeamcraftConfigScreen screen);
    }
}
