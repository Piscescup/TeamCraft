package io.github.piscescup.fabricmc.teamcraft.permission;

import io.github.piscescup.fabricmc.teamcraft.io.TeamcraftPermissionSavedData;
import net.minecraft.server.MinecraftServer;

/**
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class TeamPermissionManager {
    private static TeamCommandPermission permission = TeamCommandPermission.DEFAULT_PERMISSION_CONFIG;
    private static TeamcraftPermissionSavedData savedData;

    private TeamPermissionManager() {
    }

    public static synchronized TeamCommandPermission get() {
        return permission;
    }

    public static synchronized void load(MinecraftServer server) {
        savedData = TeamcraftPermissionSavedData.get(server);
        permission = savedData.permissions();
    }

    public static synchronized void updatePermission(String key, Permission newPermission) {
        permission.updatePermission(key, newPermission);
        savedData.setDirty();
    }

    public static synchronized Permission getPermission(String key) {
        return permission.getPermission(key);
    }

    /**
     * Releases references to a stopped server without modifying its saved data.
     */
    public static synchronized void unload(MinecraftServer server) {
        savedData = null;
        permission = TeamCommandPermission.DEFAULT_PERMISSION_CONFIG;
    }
}
