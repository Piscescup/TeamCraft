package io.github.piscescup.fabricmc.teamcraft.gui.network;

import io.github.piscescup.fabricmc.teamcraft.gui.TeamcraftConfigData;
import io.github.piscescup.fabricmc.teamcraft.gui.TeamInfoData;
import io.github.piscescup.fabricmc.teamcraft.team.TeamAssigner;
import io.github.piscescup.fabricmc.teamcraft.team.TeamAssigner.SplitPlan;
import io.github.piscescup.fabricmc.teamcraft.team.TeamSession;
import io.github.piscescup.fabricmc.teamcraft.team.TeamSessionManager;
import io.github.piscescup.fabricmc.teamcraft.text.Msg;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.scores.PlayerTeam;
import org.jspecify.annotations.NonNull;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/** Common-side registration and validation for the configuration GUI protocol. */
public final class TeamcraftConfigNetworking {
    private TeamcraftConfigNetworking() {
    }

    public static void register() {
        PayloadTypeRegistry.serverboundPlay().register(ConfigRequestPayload.TYPE, ConfigRequestPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ConfigUpdatePayload.TYPE, ConfigUpdatePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(TeamUpdatePayload.TYPE, TeamUpdatePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ConfigSyncPayload.TYPE, ConfigSyncPayload.CODEC);

        ServerPlayNetworking.registerGlobalReceiver(ConfigRequestPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            if (!hasConfigPermission(player)) {
                send(player, ConfigSyncPayload.Response.PERMISSION_DENIED);
                return;
            }
            send(player, ConfigSyncPayload.Response.OPENED);
        });

        ServerPlayNetworking.registerGlobalReceiver(ConfigUpdatePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            TeamSession session = TeamSessionManager.get();

            if (!hasConfigPermission(player)) {
                send(player, ConfigSyncPayload.Response.PERMISSION_DENIED);
                return;
            }

            TeamcraftConfigData config = payload.config();
            if (config.validate() != TeamcraftConfigData.ValidationResult.VALID) {
                send(player, ConfigSyncPayload.Response.INVALID);
                return;
            }

            config.applyTo(session);
            ConfigSyncPayload.Response response = payload.buildTeams()
                ? buildTeams(context.server())
                : ConfigSyncPayload.Response.SAVED;
            send(player, response);
        });

        ServerPlayNetworking.registerGlobalReceiver(TeamUpdatePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            if (!hasConfigPermission(player)) {
                send(player, ConfigSyncPayload.Response.PERMISSION_DENIED);
                return;
            }

            ServerScoreboard board = context.server().getScoreboard();
            PlayerTeam team = board.getPlayersTeam(player.getScoreboardName());
            if (team == null
                || !team.getName().startsWith(TeamAssigner.TEAM_ID_PREFIX)
                || !team.getName().equals(payload.teamId())) {
                send(player, ConfigSyncPayload.Response.TEAM_NOT_FOUND);
                return;
            }
            if (payload.displayName().isBlank()) {
                send(player, ConfigSyncPayload.Response.INVALID);
                return;
            }

            team.setDisplayName(Component.literal(payload.displayName().trim()));
            team.setColor(Optional.of(payload.color()));
            team.setAllowFriendlyFire(payload.friendlyFire());
            TeamAssigner.applyPrefix(team);
            send(player, ConfigSyncPayload.Response.TEAM_SAVED);
        });
    }

    private static ConfigSyncPayload.Response buildTeams(MinecraftServer server) {
        TeamSession session = TeamSessionManager.get();
        List<String> candidates = session.getCandidates();
        if (candidates.isEmpty()) {
            return ConfigSyncPayload.Response.NO_CANDIDATES;
        }

        ServerScoreboard board = server.getScoreboard();
        if (TeamAssigner.existsManagedTeam(board)) {
            return ConfigSyncPayload.Response.TEAMS_EXIST;
        }

        int teamCount = session.resolveTeamCount(candidates.size());
        if (teamCount > candidates.size()) {
            return ConfigSyncPayload.Response.TOO_MANY_TEAMS;
        }

        List<SplitPlan> plans = TeamAssigner.buildPlan(session, null, null);
        TeamAssigner.apply(board, plans, session.isFriendlyFire());
        session.getCreatedTeams().clear();
        plans.stream().map(SplitPlan::teamId).forEach(session.getCreatedTeams()::add);

        PlayerList playerList = server.getPlayerList();
        for (SplitPlan plan : plans) {
            for (String member : plan.members()) {
                ServerPlayer player = playerList.getPlayerByName(member);
                if (player != null) {
                    player.sendSystemMessage(Msg.success(
                        "result.assigned",
                        plan.displayName().copy().withColor(plan.color().textColor())
                    ));
                }
            }
        }
        return ConfigSyncPayload.Response.BUILT;
    }

    private static boolean hasConfigPermission(@NonNull ServerPlayer player) {
        // Keep the GUI permission identical to the existing /teamcraft command root.
        return Commands.hasPermission(Commands.LEVEL_ALL).test(player.createCommandSourceStack());
    }

    private static void send(ServerPlayer player, ConfigSyncPayload.Response response) {
        ServerScoreboard board = player.createCommandSourceStack().getServer().getScoreboard();
        List<TeamInfoData> teams = board.getPlayerTeams().stream()
            .filter(team -> team.getName().startsWith(TeamAssigner.TEAM_ID_PREFIX))
            .sorted(Comparator.comparing(PlayerTeam::getName))
            .map(TeamInfoData::fromTeam)
            .toList();

        PlayerTeam own = board.getPlayersTeam(player.getScoreboardName());
        TeamInfoData ownTeam = own != null && own.getName().startsWith(TeamAssigner.TEAM_ID_PREFIX)
            ? TeamInfoData.fromTeam(own)
            : null;

        ServerPlayNetworking.send(player, new ConfigSyncPayload(
            response,
            TeamcraftConfigData.fromSession(TeamSessionManager.get()),
            ownTeam,
            teams
        ));
    }
}
