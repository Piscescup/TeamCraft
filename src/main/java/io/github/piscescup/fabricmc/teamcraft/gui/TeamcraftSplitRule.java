package io.github.piscescup.fabricmc.teamcraft.gui;

import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.minecraft.network.chat.Component;

/** Split-rule choices displayed by the team configuration page. */
enum TeamcraftSplitRule {
    PLAYERS_PER_TEAM(TeamcraftTranslations.GUI_RULE_PLAYERS_PER_TEAM.key()),
    TEAM_COUNT(TeamcraftTranslations.GUI_RULE_TEAM_COUNT.key());

    private final String translationKey;

    TeamcraftSplitRule(String translationKey) {
        this.translationKey = translationKey;
    }

    Component displayName() {
        return Component.translatable(this.translationKey);
    }
}
