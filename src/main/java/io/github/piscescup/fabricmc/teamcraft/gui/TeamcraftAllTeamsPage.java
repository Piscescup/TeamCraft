package io.github.piscescup.fabricmc.teamcraft.gui;

import io.github.piscescup.fabricmc.teamcraft.text.Msg;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

/** Builds the managed-team list and opens a selected team's details. */
final class TeamcraftAllTeamsPage {
    private TeamcraftAllTeamsPage() {
    }

    static void build(TeamcraftConfigScreen screen) {
        screen.addHeader(TeamcraftTranslations.GUI_CATEGORY_ALL_TEAMS.key());
        if (screen.teams.isEmpty()) {
            screen.addTextRow(
                Component.translatable(TeamcraftTranslations.GUI_ALL_TEAMS_NONE.key())
                    .withStyle(ChatFormatting.GRAY),
                Component.translatable(TeamcraftTranslations.GUI_ALL_TEAMS_NONE.key())
            );
            return;
        }

        for (TeamInfoData team : screen.teams) {
            Component label = team.displayName().copy()
                .withColor(team.color().textColor())
                .append(Component.literal("  •  " + team.members().size()).withStyle(ChatFormatting.GRAY));
            Component details = Component.translatable(
                TeamcraftTranslations.GUI_ALL_TEAMS_TOOLTIP.key(),
                team.id(),
                Msg.colorName(team.color()),
                Component.translatable(
                    team.friendlyFire()
                        ? TeamcraftTranslations.COMMON_ON.key()
                        : TeamcraftTranslations.COMMON_OFF.key()
                ),
                String.join(", ", team.members())
            );
            Button teamRow = Button.builder(label, ignored -> screen.openTeamDetails(team)).build();
            screen.addFullWidget(teamRow, details);
        }
    }
}
