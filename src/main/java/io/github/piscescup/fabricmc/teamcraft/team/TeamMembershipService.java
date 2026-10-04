package io.github.piscescup.fabricmc.teamcraft.team;

import io.github.piscescup.fabricmc.teamcraft.permission.TeamPermissionManager;
import io.github.piscescup.fabricmc.teamcraft.text.Msg;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftColor;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;

import static io.github.piscescup.fabricmc.teamcraft.permission.TeamCommandPermission.*;

/** Shared self-service membership changes, with no authority to remove other players. */
public final class TeamMembershipService {
    private TeamMembershipService() {
    }

    public enum Outcome { LEFT, PERMISSION_DENIED, NOT_IN_TEAM, TEAM_CHANGED }

    public record Result(Outcome outcome, Component teamName) {
        public Component commandMessage() {
            return switch (outcome) {
                case LEFT -> Msg.success(TeamcraftTranslations.TEAM_LEFT.key(), teamName);
                case PERMISSION_DENIED -> Msg.error(TeamcraftTranslations.GUI_ERROR_PERMISSION.key());
                case NOT_IN_TEAM -> Msg.error(TeamcraftTranslations.ERROR_NOT_IN_TEAM.key());
                case TEAM_CHANGED -> Msg.error(TeamcraftTranslations.GUI_ERROR_TEAM_NOT_FOUND.key());
            };
        }
    }

    /** A GUI request names its displayed team; a command leaves the current team (null expected ID). */
    public static Result leave(ServerPlayer player, String expectedTeamId) {
        if (!TeamPermissionManager.hasPermission(player, ROOT_KEY)
            || !TeamPermissionManager.hasPermission(player, LEAVE_TEAM_KEY)) {
            return new Result(Outcome.PERMISSION_DENIED, Component.empty());
        }
        MinecraftServer server = player.createCommandSourceStack().getServer();
        ServerScoreboard board = server.getScoreboard();
        PlayerTeam team = board.getPlayersTeam(player.getScoreboardName());
        if (team == null || !TeamAssigner.isManagedTeamId(team.getName())) {
            return new Result(Outcome.NOT_IN_TEAM, Component.empty());
        }
        if (expectedTeamId != null && !team.getName().equals(expectedTeamId)) {
            return new Result(Outcome.TEAM_CHANGED, Component.empty());
        }
        Component name = team.getDisplayName().copy().withColor(TeamcraftColor.ofTeam(team).textColor());
        // Vanilla scoreboard removal synchronizes membership and name prefix to connected clients.
        // Keep the team itself, other members, candidates and split configuration unchanged.
        board.removePlayerFromTeam(player.getScoreboardName(), team);
        TeamInvitationManager.get(server).cancelForPlayer(player.getUUID());
        return new Result(Outcome.LEFT, name);
    }
}
