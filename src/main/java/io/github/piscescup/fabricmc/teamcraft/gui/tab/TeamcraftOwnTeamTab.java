package io.github.piscescup.fabricmc.teamcraft.gui.tab;

import io.github.piscescup.fabricmc.teamcraft.config.TeamInfoData;
import io.github.piscescup.fabricmc.teamcraft.gui.TeamcraftConfigActions;
import io.github.piscescup.fabricmc.teamcraft.gui.TeamcraftPageContext;
import io.github.piscescup.fabricmc.teamcraft.gui.TeamcraftPages;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import io.github.piscescup.fabricmc.teamcraft.text.Msg;
import java.util.List;

/** Builds controls for editing the current player's managed team. */
public final class TeamcraftOwnTeamTab
    extends TeamcraftTab
{
    private final TeamcraftPageContext context;

    public TeamcraftOwnTeamTab(TeamcraftPageContext context) {
        super(context, TeamcraftPages.OWN_TEAM, TeamcraftTranslations.GUI_NAV_OWN_TEAM.key(),
            TeamcraftTranslations.GUI_NAV_OWN_TEAM_TOOLTIP.key());
        this.context = context;
    }

    @Override
    protected List<FooterAction> footerActions() {
        return List.of(closeAction(),
            action(TeamcraftTranslations.GUI_REFRESH, () -> TeamcraftConfigActions.refresh(this.context)),
            action(TeamcraftTranslations.GUI_SAVE_TEAM, () -> TeamcraftConfigActions.submitOwnTeam(this.context)));
    }

    @Override
    protected Component headerHint() {
        return Component.translatable(TeamcraftTranslations.GUI_EXPAND_HINT.key());
    }

    @Override
    protected void build() {
        addHeader(TeamcraftTranslations.GUI_CATEGORY_OWN_TEAM.key());
        if (context.ownTeam == null) {
            addTextRow(
                Component.translatable(TeamcraftTranslations.GUI_OWN_TEAM_NONE.key())
                    .withStyle(ChatFormatting.GRAY),
                Component.translatable(TeamcraftTranslations.GUI_OWN_TEAM_NONE_TOOLTIP.key())
            );
            return;
        }

        addValueRow(
            TeamcraftTranslations.GUI_OWN_TEAM_ID.key(),
            Component.literal(context.ownTeam.id()).withStyle(ChatFormatting.GRAY)
        );

        EditBox name = textBox(
            TeamcraftTranslations.GUI_OWN_TEAM_NAME.key(),
            TeamcraftTranslations.GUI_PLACEHOLDER_TEAM_NAME.key(),
            context.ownTeamName,
            TeamInfoData.MAX_DISPLAY_NAME_LENGTH,
            value -> context.ownTeamName = value
        );
        addLabeledWidget(
            TeamcraftTranslations.GUI_OWN_TEAM_NAME.key(),
            name,
            tooltip(TeamcraftTranslations.GUI_OWN_TEAM_NAME.key())
        );

        Button color = colorDropdownButton(
            Component.translatable(TeamcraftTranslations.GUI_OWN_TEAM_COLOR.key()),
            context.ownTeamcraftColor,
            value -> context.ownTeamcraftColor = value
        );
        addFullWidget(
            color,
            tooltip(TeamcraftTranslations.GUI_OWN_TEAM_COLOR.key())
        );

        Button friendly = cycleButton(
            List.of(false, true), context.ownTeamFriendlyFire,
            value -> Msg.tr(value ? TeamcraftTranslations.COMMON_ON.key() : TeamcraftTranslations.COMMON_OFF.key())
                .withStyle(value ? ChatFormatting.GREEN : ChatFormatting.RED),
            value -> context.ownTeamFriendlyFire = value);
        addLabeledWidget(
            TeamcraftTranslations.GUI_OWN_TEAM_FRIENDLY_FIRE.key(),
            friendly,
            tooltip(TeamcraftTranslations.GUI_OWN_TEAM_FRIENDLY_FIRE.key())
        );

        addHeader(TeamcraftTranslations.GUI_OWN_TEAM_MEMBERS.key());
        String ownName = this.minecraft.player == null ? "" : this.minecraft.player.getScoreboardName();
        for (String member : context.ownTeam.members()) {
            Component memberName = Component.literal(member).withStyle(ChatFormatting.WHITE);
            if (member.equals(ownName)) {
                Component hint = Component.translatable(TeamcraftTranslations.GUI_LEAVE_TEAM_TOOLTIP.key());
                addInlineActionRow(memberName,
                    button(Component.translatable(TeamcraftTranslations.GUI_LEAVE_TEAM.key()),
                        ignored -> TeamcraftConfigActions.leaveTeam(this.context)).hoverHint(hint), hint);
            } else {
                addTextRow(memberName, memberName);
            }
        }
    }
}
