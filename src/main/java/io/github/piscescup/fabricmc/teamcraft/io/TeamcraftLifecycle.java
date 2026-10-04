package io.github.piscescup.fabricmc.teamcraft.io;

import io.github.piscescup.fabricmc.teamcraft.permission.TeamPermissionManager;
import io.github.piscescup.fabricmc.teamcraft.team.TeamSessionManager;
import io.github.piscescup.fabricmc.teamcraft.team.TeamInvitationManager;
import net.minecraft.server.MinecraftServer;

/**
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class TeamcraftLifecycle {
    private TeamcraftLifecycle() {}

    public static void load(MinecraftServer server) {
        TeamSessionManager.load(server);
        TeamPermissionManager.load(server);
    }

    public static void unload(MinecraftServer server) {
        TeamInvitationManager.unload(server);
        TeamSessionManager.unload(server);
        TeamPermissionManager.unload(server);
    }
}
