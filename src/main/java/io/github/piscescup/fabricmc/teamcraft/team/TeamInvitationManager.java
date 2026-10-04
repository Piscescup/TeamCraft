package io.github.piscescup.fabricmc.teamcraft.team;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.scores.PlayerTeam;

import java.util.IdentityHashMap;
import java.util.Map;

/** Keeps invitations isolated between server/world instances. */
public final class TeamInvitationManager {
    private static final Map<MinecraftServer, TeamInvitations<PlayerTeam>> SERVERS = new IdentityHashMap<>();

    private TeamInvitationManager() {
    }

    public static TeamInvitations<PlayerTeam> get(MinecraftServer server) {
        return SERVERS.computeIfAbsent(server, ignored -> new TeamInvitations<>());
    }

    public static void unload(MinecraftServer server) {
        SERVERS.remove(server);
    }
}
