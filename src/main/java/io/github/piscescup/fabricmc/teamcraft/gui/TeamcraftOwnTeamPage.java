package io.github.piscescup.fabricmc.teamcraft.gui;

import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

/** Builds controls for editing the current player's managed team. */
final class TeamcraftOwnTeamPage {
    private TeamcraftOwnTeamPage() {
    }

    static void build(TeamcraftConfigScreen screen) {
        screen.addHeader(TeamcraftTranslations.GUI_CATEGORY_OWN_TEAM.key());
        if (screen.ownTeam == null) {
            screen.addTextRow(
                Component.translatable(TeamcraftTranslations.GUI_OWN_TEAM_NONE.key())
                    .withStyle(ChatFormatting.GRAY),
                Component.translatable(TeamcraftTranslations.GUI_OWN_TEAM_NONE_TOOLTIP.key())
            );
            return;
        }

        screen.addValueRow(
            TeamcraftTranslations.GUI_OWN_TEAM_ID.key(),
            Component.literal(screen.ownTeam.id()).withStyle(ChatFormatting.GRAY)
        );

        EditBox name = screen.textBox(
            TeamcraftTranslations.GUI_OWN_TEAM_NAME.key(),
            TeamcraftTranslations.GUI_PLACEHOLDER_TEAM_NAME.key(),
            screen.ownTeamName,
            TeamInfoData.MAX_DISPLAY_NAME_LENGTH,
            value -> screen.ownTeamName = value
        );
        screen.addLabeledWidget(
            TeamcraftTranslations.GUI_OWN_TEAM_NAME.key(),
            name,
            TeamcraftConfigScreen.tooltip(TeamcraftTranslations.GUI_OWN_TEAM_NAME.key())
        );

        Button color = screen.colorDropdownButton(
            Component.translatable(TeamcraftTranslations.GUI_OWN_TEAM_COLOR.key()),
            screen.ownTeamcraftColor,
            value -> screen.ownTeamcraftColor = value
        );
        screen.addFullWidget(
            color,
            TeamcraftConfigScreen.tooltip(TeamcraftTranslations.GUI_OWN_TEAM_COLOR.key())
        );

        CycleButton<Boolean> friendly = CycleButton.onOffBuilder(screen.ownTeamFriendlyFire)
            .create(
                Component.translatable(TeamcraftTranslations.GUI_OWN_TEAM_FRIENDLY_FIRE.key()),
                (button, value) -> screen.ownTeamFriendlyFire = value
            );
        screen.addFullWidget(
            friendly,
            TeamcraftConfigScreen.tooltip(TeamcraftTranslations.GUI_OWN_TEAM_FRIENDLY_FIRE.key())
        );

        screen.addValueRow(
            TeamcraftTranslations.GUI_OWN_TEAM_MEMBERS.key(),
            Component.literal(String.join(", ", screen.ownTeam.members())).withStyle(ChatFormatting.WHITE)
        );
    }
}
