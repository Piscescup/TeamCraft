package io.github.piscescup.fabricmc.teamcraft.gui.network;

import io.github.piscescup.fabricmc.teamcraft.gui.TeamcraftConfigData;
import io.github.piscescup.fabricmc.teamcraft.gui.TeamInfoData;
import io.github.piscescup.fabricmc.teamcraft.permission.TeamPermissionManager;
import io.github.piscescup.fabricmc.teamcraft.team.SplitPlan;
import io.github.piscescup.fabricmc.teamcraft.team.TeamAssigner;
import io.github.piscescup.fabricmc.teamcraft.team.TeamSession;
import io.github.piscescup.fabricmc.teamcraft.team.TeamSessionManager;
import io.github.piscescup.fabricmc.teamcraft.text.Msg;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.scores.PlayerTeam;
//#if MC >= 12111
//#if MC >= 12111
import org.jspecify.annotations.NonNull;
//#endif
//#endif

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

import static io.github.piscescup.fabricmc.teamcraft.permission.TeamCommandPermission.BUILD_KEY;
import static io.github.piscescup.fabricmc.teamcraft.permission.TeamCommandPermission.CLEAR_KEY;
import static io.github.piscescup.fabricmc.teamcraft.permission.TeamCommandPermission.CONFIG_KEY;
import static io.github.piscescup.fabricmc.teamcraft.permission.TeamCommandPermission.INIT_KEY;
import static io.github.piscescup.fabricmc.teamcraft.permission.TeamCommandPermission.MANAGE_KEY;
import static io.github.piscescup.fabricmc.teamcraft.permission.TeamCommandPermission.ROOT_KEY;

/** Common-side registration and validation for the configuration GUI protocol. */
public final class TeamcraftConfigNetworking {
    private TeamcraftConfigNetworking() {
    }

    public static void register() {
        //#if MC >= 260102
        PayloadTypeRegistry.serverboundPlay().register(ConfigRequestPayload.TYPE, ConfigRequestPayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(ConfigUpdatePayload.TYPE, ConfigUpdatePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(TeamUpdatePayload.TYPE, TeamUpdatePayload.CODEC);
        PayloadTypeRegistry.serverboundPlay().register(TeamDeletePayload.TYPE, TeamDeletePayload.CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ConfigSyncPayload.TYPE, ConfigSyncPayload.CODEC);
        //#else
        //$$ PayloadTypeRegistry.playC2S().register(ConfigRequestPayload.TYPE, ConfigRequestPayload.CODEC);
        //$$ PayloadTypeRegistry.playC2S().register(ConfigUpdatePayload.TYPE, ConfigUpdatePayload.CODEC);
        //$$ PayloadTypeRegistry.playC2S().register(TeamUpdatePayload.TYPE, TeamUpdatePayload.CODEC);
        //$$ PayloadTypeRegistry.playC2S().register(TeamDeletePayload.TYPE, TeamDeletePayload.CODEC);
        //$$ PayloadTypeRegistry.playS2C().register(ConfigSyncPayload.TYPE, ConfigSyncPayload.CODEC);
        //#endif

        ServerPlayNetworking.registerGlobalReceiver(ConfigRequestPayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            if (!hasPermission(player, ROOT_KEY)) {
                send(player, ConfigSyncPayload.Response.PERMISSION_DENIED);
                return;
            }
            send(player, ConfigSyncPayload.Response.OPENED);
        });

        ServerPlayNetworking.registerGlobalReceiver(ConfigUpdatePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            TeamSession session = TeamSessionManager.get();

            boolean changesConfig = !payload.config().equals(TeamcraftConfigData.fromSession(session));
            boolean changesCandidates = !payload.candidates().equals(session.getCandidates());
            if (!hasPermission(player, ROOT_KEY)
                || (changesConfig && !hasPermission(player, CONFIG_KEY))
                || (changesCandidates && !hasPermission(player, INIT_KEY))
                || (payload.buildTeams() && !hasPermission(player, BUILD_KEY))) {
                send(player, ConfigSyncPayload.Response.PERMISSION_DENIED);
                return;
            }

            TeamcraftConfigData config = payload.config();
            if (config.validate() != TeamcraftConfigData.ValidationResult.VALID
                || !validCandidates(payload.candidates())) {
                send(player, ConfigSyncPayload.Response.INVALID);
                return;
            }

            config.applyTo(session);
            session.getCandidates().clear();
            session.getCandidates().addAll(payload.candidates());
            ConfigSyncPayload.Response response = payload.buildTeams()
                ? buildTeams(context.server())
                : ConfigSyncPayload.Response.SAVED;
            send(player, response);
        });

        ServerPlayNetworking.registerGlobalReceiver(TeamUpdatePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            if (!hasPermission(player, ROOT_KEY) || !hasPermission(player, MANAGE_KEY)) {
                send(player, ConfigSyncPayload.Response.PERMISSION_DENIED);
                return;
            }

            ServerScoreboard board = context.server().getScoreboard();
            PlayerTeam team = board.getPlayersTeam(player.getScoreboardName());
            if (team == null
                || !TeamAssigner.isManagedTeamId(team.getName())
                || !team.getName().equals(payload.teamId())) {
                send(player, ConfigSyncPayload.Response.TEAM_NOT_FOUND);
                return;
            }
            if (payload.displayName().isBlank()) {
                send(player, ConfigSyncPayload.Response.INVALID);
                return;
            }

            team.setDisplayName(Component.literal(payload.displayName().trim()));
            payload.color().applyTo(team);
            team.setAllowFriendlyFire(payload.friendlyFire());
            TeamAssigner.applyPrefix(team);
            send(player, ConfigSyncPayload.Response.TEAM_SAVED);
        });

        ServerPlayNetworking.registerGlobalReceiver(TeamDeletePayload.TYPE, (payload, context) -> {
            ServerPlayer player = context.player();
            String permissionKey = payload.target() == TeamDeletePayload.Target.ALL
                ? CLEAR_KEY
                : MANAGE_KEY;
            if (!hasPermission(player, ROOT_KEY) || !hasPermission(player, permissionKey)) {
                send(player, ConfigSyncPayload.Response.PERMISSION_DENIED);
                return;
            }

            ServerScoreboard board = context.server().getScoreboard();
            TeamSession session = TeamSessionManager.get();
            if (payload.target() == TeamDeletePayload.Target.ALL) {
                TeamAssigner.clear(board);
                session.getCreatedTeams().clear();
                send(player, ConfigSyncPayload.Response.ALL_TEAMS_CLEARED);
                return;
            }

            if (!TeamAssigner.disband(board, payload.teamId())) {
                send(player, ConfigSyncPayload.Response.TEAM_NOT_FOUND);
                return;
            }
            session.getCreatedTeams().remove(payload.teamId());
            send(player, ConfigSyncPayload.Response.TEAM_DISBANDED);
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
        plans.stream().map(SplitPlan::visualTeamString).forEach(session.getCreatedTeams()::add);

        PlayerList playerList = server.getPlayerList();
        for (SplitPlan plan : plans) {
            for (String member : plan.members()) {
                ServerPlayer player = playerList.getPlayerByName(member);
                if (player != null) {
                    player.sendSystemMessage(Msg.success(
                        TeamcraftTranslations.RESULT_ASSIGNED.key(),
                        plan.displayName().copy().withColor(plan.color().textColor())
                    ));
                }
            }
        }
        return ConfigSyncPayload.Response.BUILT;
    }

    //#if MC >= 12111
    private static boolean hasPermission(
        //#if MC >= 12111
        @NonNull
        //#endif
        ServerPlayer player,
        String permissionKey
    ) {
        return Commands.hasPermission(TeamPermissionManager.getPermission(permissionKey).toPermission())
            .test(player.createCommandSourceStack());
    }
    //#else
    //$$ private static boolean hasPermission(ServerPlayer player, String permissionKey) {
    //$$     return player.createCommandSourceStack()
    //$$         .hasPermission(TeamPermissionManager.getPermission(permissionKey).toPermission());
    //$$ }
    //#endif

    private static boolean validCandidates(List<String> candidates) {
        return candidates.size() <= TeamcraftConfigData.MAX_CANDIDATES
            && new LinkedHashSet<>(candidates).size() == candidates.size()
            && candidates.stream().noneMatch(name ->
                name.isBlank() || name.length() > TeamcraftConfigData.MAX_NAME_LENGTH);
    }

    private static void send(ServerPlayer player, ConfigSyncPayload.Response response) {
        ServerScoreboard board = player.createCommandSourceStack().getServer().getScoreboard();
        TeamSession session = TeamSessionManager.get();
        List<TeamInfoData> teams = board.getPlayerTeams().stream()
            .filter(team -> TeamAssigner.isManagedTeamId(team.getName()))
            .filter(TeamInfoData::canEncode)
            .sorted(Comparator.comparing(PlayerTeam::getName))
            .map(TeamInfoData::fromTeam)
            .toList();

        PlayerTeam own = board.getPlayersTeam(player.getScoreboardName());
        TeamInfoData ownTeam = own != null
            && TeamAssigner.isManagedTeamId(own.getName())
            && TeamInfoData.canEncode(own)
            ? TeamInfoData.fromTeam(own)
            : null;

        List<String> onlinePlayers = player.createCommandSourceStack().getServer().getPlayerList().getPlayers().stream()
            .map(online ->
                //#if MC >= 12110
                online.getGameProfile().name()
                //#else
                //$$ online.getGameProfile().getName()
                //#endif
            )
            .sorted(String.CASE_INSENSITIVE_ORDER)
            .limit(TeamcraftConfigData.MAX_CANDIDATES)
            .toList();

        ServerPlayNetworking.send(player, new ConfigSyncPayload(
            response,
            TeamcraftConfigData.fromSession(session),
            session.getCandidates(),
            onlinePlayers,
            ownTeam,
            teams
        ));
    }
}
