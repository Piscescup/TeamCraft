package io.github.piscescup.fabricmc.teamcraft.gui.network;

import io.github.piscescup.fabricmc.teamcraft.config.TeamInfoData;
import io.github.piscescup.fabricmc.teamcraft.config.TeamcraftConfigData;
import io.github.piscescup.fabricmc.teamcraft.team.*;
import io.github.piscescup.fabricmc.teamcraft.text.Msg;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftColor;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;

import java.util.Comparator;
import java.util.List;

import static io.github.piscescup.fabricmc.teamcraft.permission.TeamCommandPermission.*;

/** Common-side protocol registration; GUI actions never bypass command policy. */
public final class TeamcraftInvitationNetworking {
    private TeamcraftInvitationNetworking() {
    }

    public static void register() {
        //#if MC >= 260102
        PayloadTypeRegistry.serverboundPlay().register(InvitationActionPayload.TYPE, InvitationActionPayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(InvitationSyncPayload.TYPE, InvitationSyncPayload.CODEC);
        //#else
        //$$ PayloadTypeRegistry.playC2S().register(InvitationActionPayload.TYPE, InvitationActionPayload.CODEC);
        //$$ PayloadTypeRegistry.playS2C().register(InvitationSyncPayload.TYPE, InvitationSyncPayload.CODEC);
        //#endif
        ServerPlayNetworking.registerGlobalReceiver(InvitationActionPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            MinecraftServer server = context.server();
            TeamInvitationService.Result result;
            if (!TeamInvitationService.hasPermission(player, ROOT_KEY)) {
                result = TeamInvitationService.denied();
            } else {
                result = switch (payload.action()) {
                    case REFRESH -> null;
                    case SEND -> TeamInvitationService.send(server, player,
                        server.getPlayerList().getPlayerByName(payload.playerName()));
                    case ACCEPT, DECLINE -> payload.invitationId() == null
                        ? new TeamInvitationService.Result(TeamInvitationService.Status.ERROR, TeamcraftTranslations.INVITE_ERROR_NONE)
                        : payload.action() == InvitationActionPayload.Action.ACCEPT
                            ? TeamInvitationService.accept(server, player, payload.invitationId())
                            : TeamInvitationService.decline(server, player, payload.invitationId());
                };
            }
            sendSnapshot(server, player, payload, result);
        });
    }

    private static void sendSnapshot(MinecraftServer server, ServerPlayer player, InvitationActionPayload request,
                                     TeamInvitationService.Result result) {
        InvitationSyncPayload.Response response = result == null ? InvitationSyncPayload.Response.SNAPSHOT
            : switch (result.status()) {
                case SUCCESS -> InvitationSyncPayload.Response.SUCCESS;
                case ERROR -> InvitationSyncPayload.Response.ERROR;
                case PERMISSION_DENIED -> InvitationSyncPayload.Response.PERMISSION_DENIED;
            };
        Component message = result == null ? Msg.tr(TeamcraftTranslations.GUI_REFRESHED.key()) : result.message();
        if (response == InvitationSyncPayload.Response.PERMISSION_DENIED) {
            ServerPlayNetworking.send(player, new InvitationSyncPayload(request.requestId(), response, message,
                null, List.of(), List.of(), null, false));
            return;
        }
        ServerScoreboard board = server.getScoreboard();
        PlayerTeam own = board.getPlayersTeam(player.getScoreboardName());
        TeamInfoData ownTeam = own != null && TeamAssigner.isManagedTeamId(own.getName()) && TeamInfoData.canEncode(own)
            ? TeamInfoData.fromTeam(own) : null;
        boolean canInvite = ownTeam != null && TeamInvitationService.hasPermission(player, INVITE_KEY);
        List<String> eligible = canInvite ? server.getPlayerList().getPlayers().stream()
            .filter(target -> !target.getUUID().equals(player.getUUID()))
            .filter(target -> board.getPlayersTeam(target.getScoreboardName()) == null)
            .map(ServerPlayer::getScoreboardName).sorted(String.CASE_INSENSITIVE_ORDER)
            .limit(TeamcraftConfigData.MAX_CANDIDATES).toList() : List.of();
        // The full team list is only needed when joining updates other tabs, not for each poll.
        List<TeamInfoData> teams = request.action() == InvitationActionPayload.Action.ACCEPT
            && response == InvitationSyncPayload.Response.SUCCESS ? board.getPlayerTeams().stream()
            .filter(team -> TeamAssigner.isManagedTeamId(team.getName())).filter(TeamInfoData::canEncode)
            .sorted(Comparator.comparing(PlayerTeam::getName)).limit(TeamcraftConfigData.MAX_LIST_SIZE)
            .map(TeamInfoData::fromTeam).toList() : List.of();
        TeamInvitations<PlayerTeam> invitations = TeamInvitationManager.get(server);
        TeamInvitations.Invitation<PlayerTeam> pending = invitations.find(player.getUUID());
        if (pending != null && (!TeamInvitationService.isValid(board, pending) || own != null)) {
            invitations.remove(player.getUUID());
            pending = null;
        }
        InvitationSyncPayload.PendingInvitation invitation = pending == null ? null
            : new InvitationSyncPayload.PendingInvitation(pending.id(), pending.senderName(), pending.team().getName(),
                pending.team().getDisplayName().copy().withColor(TeamcraftColor.ofTeam(pending.team()).textColor()),
                invitations.remainingSeconds(pending));
        ServerPlayNetworking.send(player, new InvitationSyncPayload(request.requestId(), response, message,
            ownTeam, teams, eligible, invitation, canInvite));
    }
}
