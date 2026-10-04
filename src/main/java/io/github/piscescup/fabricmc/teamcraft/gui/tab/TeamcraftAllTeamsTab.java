package io.github.piscescup.fabricmc.teamcraft.gui.tab;



import io.github.piscescup.fabricmc.teamcraft.config.TeamInfoData;
import io.github.piscescup.fabricmc.teamcraft.gui.TeamDetailsScreen;
import io.github.piscescup.fabricmc.teamcraft.gui.TeamcraftConfigActions;
import io.github.piscescup.fabricmc.teamcraft.gui.TeamcraftPageContext;
import io.github.piscescup.fabricmc.teamcraft.gui.TeamcraftPages;
import io.github.piscescup.fabricmc.teamcraft.text.Msg;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

import java.util.List;

/** Builds collapsible managed-team sections and their details entry points. */
public final class TeamcraftAllTeamsTab
    extends TeamcraftTab {
    private final TeamcraftPageContext context;

    public TeamcraftAllTeamsTab(TeamcraftPageContext context) {
        super(context, TeamcraftPages.ALL_TEAMS, TeamcraftTranslations.GUI_NAV_ALL_TEAMS.key(),
            TeamcraftTranslations.GUI_NAV_ALL_TEAMS_TOOLTIP.key());
        this.context = context;
    }

    @Override
    protected List<FooterAction> footerActions() {
        return List.of(closeAction(),
            action(TeamcraftTranslations.GUI_REFRESH, () -> TeamcraftConfigActions.refresh(this.context)),
            action(TeamcraftTranslations.GUI_ALL_TEAMS_CLEAR, () -> TeamcraftConfigActions.clearAllTeams(this.context)));
    }

    @Override
    protected Component headerHint() {
        return Component.translatable(TeamcraftTranslations.GUI_EXPAND_HINT.key());
    }

    @Override
    protected void build() {
        addHeader(TeamcraftTranslations.GUI_CATEGORY_ALL_TEAMS.key());
        if (context.teams.isEmpty()) {
            addTextRow(
                Component.translatable(TeamcraftTranslations.GUI_ALL_TEAMS_NONE.key())
                    .withStyle(ChatFormatting.GRAY),
                Component.translatable(TeamcraftTranslations.GUI_ALL_TEAMS_NONE.key())
            );
            return;
        }

        for (TeamInfoData team : context.teams) {
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
            addSection("team/" + team.id(), label, team.id(), details, false,
                () -> addTeamDetails(team, details));
        }
    }

    private void addTeamDetails(TeamInfoData team, Component tooltip) {
        addValueRow(TeamcraftTranslations.TEAM_INFO_ID.key(),
            Component.literal(team.id()).withStyle(ChatFormatting.GRAY));
        addValueRow(TeamcraftTranslations.TEAM_INFO_COLOR.key(),
            Msg.colorName(team.color()).copy().withColor(team.color().textColor()));
        addValueRow(TeamcraftTranslations.TEAM_INFO_FRIENDLY_FIRE.key(),
            Component.translatable(team.friendlyFire()
                ? TeamcraftTranslations.COMMON_ON.key() : TeamcraftTranslations.COMMON_OFF.key())
                .withStyle(team.friendlyFire() ? ChatFormatting.GREEN : ChatFormatting.RED));
        addTextRow(Component.translatable(TeamcraftTranslations.GUI_DETAILS_MEMBERS.key(),
            team.members().size()).withStyle(ChatFormatting.GRAY), null);
        for (int index = 0; index < team.members().size(); index++) {
            addTextRow(Component.translatable(TeamcraftTranslations.GUI_DETAILS_MEMBER.key(),
                index + 1, team.members().get(index)).withStyle(ChatFormatting.WHITE), null);
        }
        addFullWidget(button(Component.translatable(TeamcraftTranslations.GUI_DETAILS_TITLE.key()),
            ignored -> openScreen(new TeamDetailsScreen(this, team)))
            .hoverHint(Component.translatable(TeamcraftTranslations.GUI_BUTTON_DETAILS.key())), tooltip);
    }
}
