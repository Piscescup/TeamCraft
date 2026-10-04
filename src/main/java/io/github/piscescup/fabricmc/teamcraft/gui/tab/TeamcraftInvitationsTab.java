package io.github.piscescup.fabricmc.teamcraft.gui.tab;

import io.github.piscescup.fabricmc.teamcraft.gui.PermissionDeniedScreen;
import io.github.piscescup.fabricmc.teamcraft.gui.TeamcraftDialogScreen;
import io.github.piscescup.fabricmc.teamcraft.gui.TeamcraftFeedbackScreen;
import io.github.piscescup.fabricmc.teamcraft.gui.TeamcraftInvitationClient;
import io.github.piscescup.fabricmc.teamcraft.gui.TeamcraftPageContext;
import io.github.piscescup.fabricmc.teamcraft.gui.TeamcraftPages;
import io.github.piscescup.fabricmc.teamcraft.gui.network.InvitationActionPayload;
import io.github.piscescup.fabricmc.teamcraft.gui.network.InvitationSyncPayload;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

/** Received invitation and eligible online players, using the shared foldable row UI. */
public final class TeamcraftInvitationsTab extends TeamcraftTab {
    private final TeamcraftPageContext context;
    private InvitationSyncPayload snapshot;
    private UUID pendingRequest;
    private boolean actionPending;
    private boolean unavailable;
    private boolean accessDenied;
    private TeamcraftTranslations failureMessage = TeamcraftTranslations.GUI_ERROR_SERVER_UNSUPPORTED;
    private int pollTicks;

    public TeamcraftInvitationsTab(TeamcraftPageContext context) {
        super(context, TeamcraftPages.INVITATIONS, TeamcraftTranslations.GUI_NAV_INVITATIONS.key(),
            TeamcraftTranslations.GUI_NAV_INVITATIONS_TOOLTIP.key());
        this.context = context;
    }

    @Override
    public void tick() {
        super.tick();
        if (pendingRequest == null && !context.waitingForServer && !unavailable && !accessDenied && --pollTicks <= 0) {
            request(InvitationActionPayload.Action.REFRESH, "", null, false);
        }
    }

    @Override
    public void removed() {
        pollTicks = 0;
        // Polls have no effect to acknowledge. Mutations still complete after Close.
        if (!actionPending) {
            TeamcraftInvitationClient.cancel(pendingRequest);
            pendingRequest = null;
        }
        super.removed();
    }

    @Override
    protected List<FooterAction> footerActions() {
        return List.of(closeAction(), action(TeamcraftTranslations.GUI_REFRESH,
            () -> request(InvitationActionPayload.Action.REFRESH, "", null, true)));
    }

    @Override
    protected Component headerHint() {
        return tr(TeamcraftTranslations.GUI_EXPAND_HINT);
    }

    @Override
    protected void build() {
        if (snapshot == null) {
            addTextRow(tr(unavailable ? failureMessage
                : TeamcraftTranslations.GUI_LOADING).withStyle(ChatFormatting.GRAY), null);
            return;
        }
        if (accessDenied) {
            addTextRow(tr(TeamcraftTranslations.GUI_ERROR_PERMISSION).withStyle(ChatFormatting.RED), null);
            return;
        }
        addSection(TeamcraftTranslations.GUI_INVITE_RECEIVED.key(), tr(TeamcraftTranslations.GUI_INVITE_RECEIVED),
            "received", tr(TeamcraftTranslations.GUI_INVITE_RECEIVED_TOOLTIP), true, this::buildReceived);
        addSection(TeamcraftTranslations.GUI_INVITE_SEND.key(), tr(TeamcraftTranslations.GUI_INVITE_SEND),
            "send", tr(TeamcraftTranslations.GUI_INVITE_SEND_TOOLTIP), true, this::buildSend);
    }

    private void buildReceived() {
        var invitation = snapshot.invitation();
        if (invitation == null) {
            addTextRow(tr(TeamcraftTranslations.GUI_INVITE_NONE).withStyle(ChatFormatting.GRAY),
                tr(TeamcraftTranslations.GUI_INVITE_RECEIVED_TOOLTIP));
            return;
        }
        addValueRow(TeamcraftTranslations.GUI_INVITE_FROM.key(), Component.literal(invitation.senderName()));
        addValueRow(TeamcraftTranslations.GUI_INVITE_TEAM.key(), invitation.teamName());
        addValueRow(TeamcraftTranslations.TEAM_INFO_ID.key(), Component.literal(invitation.teamId()).withStyle(ChatFormatting.GRAY));
        addTextRow(Component.translatable(TeamcraftTranslations.GUI_INVITE_REMAINING.key(), invitation.secondsLeft())
            .withStyle(ChatFormatting.YELLOW), tr(TeamcraftTranslations.GUI_INVITE_RECEIVED_TOOLTIP));
        addTwoWidgets(
            button(tr(TeamcraftTranslations.GUI_INVITE_ACCEPT), ignored ->
                request(InvitationActionPayload.Action.ACCEPT, "", invitation.id(), true))
                .hoverHint(tr(TeamcraftTranslations.HELP_INVITE_ACCEPT)),
            button(tr(TeamcraftTranslations.GUI_INVITE_DECLINE), ignored ->
                request(InvitationActionPayload.Action.DECLINE, "", invitation.id(), true))
                .hoverHint(tr(TeamcraftTranslations.HELP_INVITE_DECLINE)),
            tr(TeamcraftTranslations.GUI_INVITE_RECEIVED_TOOLTIP));
    }

    private void buildSend() {
        if (snapshot.ownTeam() == null) {
            addTextRow(tr(TeamcraftTranslations.INVITE_ERROR_NO_TEAM).withStyle(ChatFormatting.GRAY), null);
            return;
        }
        addValueRow(TeamcraftTranslations.GUI_INVITE_TEAM.key(), snapshot.ownTeam().displayName().copy()
            .withColor(snapshot.ownTeam().color().textColor()));
        if (!snapshot.canInvite()) {
            addTextRow(tr(TeamcraftTranslations.GUI_ERROR_PERMISSION).withStyle(ChatFormatting.RED), null);
            return;
        }
        addTextRow(tr(TeamcraftTranslations.GUI_INVITE_SEND_TOOLTIP).withStyle(ChatFormatting.GRAY), null);
        if (snapshot.eligiblePlayers().isEmpty()) {
            addTextRow(tr(TeamcraftTranslations.GUI_INVITE_NO_PLAYERS).withStyle(ChatFormatting.GRAY), null);
        }
        for (String player : snapshot.eligiblePlayers()) {
            Component hint = Component.translatable(TeamcraftTranslations.GUI_INVITE_PLAYER_HINT.key(), player);
            addFullWidget(button(Component.translatable(TeamcraftTranslations.GUI_INVITE_PLAYER.key(), player),
                ignored -> request(InvitationActionPayload.Action.SEND, player, null, true)).hoverHint(hint),
                tr(TeamcraftTranslations.GUI_INVITE_SEND_TOOLTIP));
        }
    }

    private void request(InvitationActionPayload.Action action, String player, UUID invitationId, boolean manual) {
        if (context.waitingForServer) { return; }
        TeamcraftInvitationClient.cancel(pendingRequest);
        UUID id = UUID.randomUUID();
        pendingRequest = id;
        actionPending = manual;
        if (manual) {
            context.waitingForServer = true;
            updateEnabledState();
        }
        if (!TeamcraftInvitationClient.send(new InvitationActionPayload(id, action, player, invitationId),
            payload -> applyResponse(payload, action, manual),
            () -> failRequest(id, TeamcraftTranslations.GUI_INVITE_TIMEOUT))) {
            unavailable = true;
            failRequest(id, TeamcraftTranslations.GUI_ERROR_SERVER_UNSUPPORTED);
        }
    }

    private void applyResponse(InvitationSyncPayload payload, InvitationActionPayload.Action action, boolean manual) {
        if (!payload.requestId().equals(pendingRequest)) { return; }
        boolean changed = snapshot == null || !Objects.equals(snapshot.invitation(), payload.invitation())
            || !Objects.equals(snapshot.ownTeam(), payload.ownTeam())
            || !snapshot.eligiblePlayers().equals(payload.eligiblePlayers())
            || snapshot.canInvite() != payload.canInvite();
        finishRequest();
        unavailable = false;
        accessDenied = payload.response() == InvitationSyncPayload.Response.PERMISSION_DENIED;
        snapshot = payload;
        if (action == InvitationActionPayload.Action.ACCEPT && payload.response() == InvitationSyncPayload.Response.SUCCESS) {
            context.loadInvitationTeams(payload.ownTeam(), payload.teams());
        }
        if (!isDisplayed()) { return; }
        if (changed || accessDenied) { rebuildPage(); }
        if (accessDenied) {
            PermissionDeniedScreen.open(this);
        } else if (manual || payload.response() == InvitationSyncPayload.Response.ERROR) {
            showFeedback(payload.message(), payload.response() == InvitationSyncPayload.Response.ERROR
                ? TeamcraftFeedbackScreen.Type.ERROR : TeamcraftFeedbackScreen.Type.SUCCESS);
        }
    }

    private void failRequest(UUID id, TeamcraftTranslations message) {
        if (!id.equals(pendingRequest)) { return; }
        unavailable = true;
        failureMessage = message;
        finishRequest();
        if (isDisplayed()) {
            rebuildPage();
            showFeedback(message.key(), TeamcraftFeedbackScreen.Type.ERROR);
        }
    }

    private void finishRequest() {
        if (actionPending) { context.waitingForServer = false; }
        pendingRequest = null;
        actionPending = false;
        pollTicks = 40;
        context.screen().updateEnabledState();
    }

    private boolean isDisplayed() {
        //#if MC >= 260200
        Screen current = this.minecraft.gui.screen();
        //#else
        //$$ Screen current = this.minecraft.screen;
        //#endif
        return current == this || current instanceof TeamcraftDialogScreen dialog && dialog.hasParent(this);
    }

    private static net.minecraft.network.chat.MutableComponent tr(TeamcraftTranslations translation) {
        return Component.translatable(translation.key());
    }
}
