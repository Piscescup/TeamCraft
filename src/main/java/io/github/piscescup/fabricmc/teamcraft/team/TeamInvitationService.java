package io.github.piscescup.fabricmc.teamcraft.team;

import io.github.piscescup.fabricmc.teamcraft.permission.TeamPermissionManager;
import io.github.piscescup.fabricmc.teamcraft.text.Msg;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftColor;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;

import java.util.UUID;

import static io.github.piscescup.fabricmc.teamcraft.permission.TeamCommandPermission.*;

/** The same server-side rules apply to commands and GUI packets. */
public final class TeamInvitationService {
    private TeamInvitationService() {
    }

    public enum Status { SUCCESS, ERROR, PERMISSION_DENIED }

    public record Result(Status status, TeamcraftTranslations translation, Object... arguments) {
        public Component message() {
            return Msg.tr(translation.key(), arguments);
        }

        public Component commandMessage() {
            return status == Status.SUCCESS
                ? Msg.success(translation.key(), arguments) : Msg.error(translation.key(), arguments);
        }
    }

    public static Result send(MinecraftServer server, ServerPlayer sender, ServerPlayer target) {
        if (!hasPermission(sender, ROOT_KEY) || !hasPermission(sender, INVITE_KEY)) {
            return denied();
        }
        ServerScoreboard board = server.getScoreboard();
        PlayerTeam team = board.getPlayersTeam(sender.getScoreboardName());
        if (team == null || !TeamAssigner.isManagedTeamId(team.getName())) {
            return error(TeamcraftTranslations.INVITE_ERROR_NO_TEAM);
        }
        if (target == null) {
            return error(TeamcraftTranslations.GUI_INVITE_OFFLINE);
        }
        if (sender.getUUID().equals(target.getUUID())) {
            return error(TeamcraftTranslations.INVITE_ERROR_SELF);
        }
        if (board.getPlayersTeam(target.getScoreboardName()) != null) {
            return error(TeamcraftTranslations.INVITE_ERROR_HAS_TEAM, target.getDisplayName());
        }
        TeamInvitations<PlayerTeam> invitations = TeamInvitationManager.get(server);
        TeamInvitations.Invitation<PlayerTeam> pending = invitations.find(target.getUUID());
        if (pending != null && !isValid(board, pending)) {
            invitations.remove(target.getUUID());
        }
        if (!invitations.offer(target.getUUID(), sender.getUUID(), sender.getScoreboardName(), team)) {
            return error(TeamcraftTranslations.INVITE_ERROR_PENDING, target.getDisplayName());
        }
        target.sendSystemMessage(Msg.success(TeamcraftTranslations.INVITE_RECEIVED.key(),
            sender.getDisplayName(), teamName(team), TeamInvitations.LIFETIME_SECONDS));
        return success(TeamcraftTranslations.INVITE_SENT, target.getDisplayName(), teamName(team));
    }

    /** expectedId is required by the GUI; commands without an ID reply to the current invitation. */
    public static Result accept(MinecraftServer server, ServerPlayer player, UUID expectedId) {
        if (!hasPermission(player, ROOT_KEY)) {
            return denied();
        }
        TeamInvitations<PlayerTeam> invitations = TeamInvitationManager.get(server);
        TeamInvitations.Invitation<PlayerTeam> invitation = invitations.find(player.getUUID());
        if (invitation == null || (expectedId != null && !expectedId.equals(invitation.id()))) {
            return error(TeamcraftTranslations.INVITE_ERROR_NONE);
        }
        ServerScoreboard board = server.getScoreboard();
        if (!isValid(board, invitation)) {
            invitations.remove(player.getUUID());
            return error(TeamcraftTranslations.INVITE_ERROR_UNAVAILABLE);
        }
        if (board.getPlayersTeam(player.getScoreboardName()) != null) {
            invitations.remove(player.getUUID());
            return error(TeamcraftTranslations.INVITE_ERROR_HAS_TEAM, player.getDisplayName());
        }
        if (!board.addPlayerToTeam(player.getScoreboardName(), invitation.team())) {
            return error(TeamcraftTranslations.INVITE_ERROR_UNAVAILABLE);
        }
        invitations.remove(player.getUUID());
        ServerPlayer sender = server.getPlayerList().getPlayer(invitation.senderId());
        if (sender != null) {
            sender.sendSystemMessage(Msg.success(TeamcraftTranslations.INVITE_JOINED.key(),
                player.getDisplayName(), teamName(invitation.team())));
        }
        return success(TeamcraftTranslations.INVITE_ACCEPTED, teamName(invitation.team()));
    }

    public static Result decline(MinecraftServer server, ServerPlayer player, UUID expectedId) {
        if (!hasPermission(player, ROOT_KEY)) {
            return denied();
        }
        TeamInvitations<PlayerTeam> invitations = TeamInvitationManager.get(server);
        TeamInvitations.Invitation<PlayerTeam> invitation = invitations.find(player.getUUID());
        if (invitation == null || (expectedId != null && !expectedId.equals(invitation.id()))) {
            return error(TeamcraftTranslations.INVITE_ERROR_NONE);
        }
        invitations.remove(player.getUUID());
        ServerPlayer sender = server.getPlayerList().getPlayer(invitation.senderId());
        if (sender != null) {
            sender.sendSystemMessage(Msg.success(TeamcraftTranslations.INVITE_REJECTED.key(), player.getDisplayName()));
        }
        return success(TeamcraftTranslations.INVITE_DECLINED, teamName(invitation.team()));
    }

    public static boolean isValid(ServerScoreboard board, TeamInvitations.Invitation<PlayerTeam> invitation) {
        PlayerTeam team = invitation.team();
        // A new split can reuse an old ID; identity prevents accepting an invitation to its replacement.
        return TeamAssigner.isManagedTeamId(team.getName())
            && board.getPlayerTeam(team.getName()) == team
            && board.getPlayersTeam(invitation.senderName()) == team;
    }

    public static boolean hasPermission(ServerPlayer player, String key) {
        //#if MC >= 12111
        return Commands.hasPermission(TeamPermissionManager.getPermission(key).toPermission())
            .test(player.createCommandSourceStack());
        //#else
        //$$ return player.createCommandSourceStack().hasPermission(TeamPermissionManager.getPermission(key).toPermission());
        //#endif
    }

    private static Component teamName(PlayerTeam team) {
        return team.getDisplayName().copy().withColor(TeamcraftColor.ofTeam(team).textColor());
    }

    private static Result success(TeamcraftTranslations key, Object... args) {
        return new Result(Status.SUCCESS, key, args);
    }

    private static Result error(TeamcraftTranslations key, Object... args) {
        return new Result(Status.ERROR, key, args);
    }

    public static Result denied() {
        return new Result(Status.PERMISSION_DENIED, TeamcraftTranslations.GUI_ERROR_PERMISSION);
    }
}
