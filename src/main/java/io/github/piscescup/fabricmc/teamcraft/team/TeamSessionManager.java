package io.github.piscescup.fabricmc.teamcraft.team;

import io.github.piscescup.fabricmc.teamcraft.io.TeamcraftSavedData;
import net.minecraft.server.MinecraftServer;

/**
 * Exposes the {@link TeamSession} belonging to the running server's world.
 * While a server is active, the session is backed by {@link TeamcraftSavedData}
 * and every mutation marks that world-owned data dirty.
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class TeamSessionManager
{
    private static TeamSession session = new TeamSession();
    private static TeamcraftSavedData savedData;

    private TeamSessionManager() {
    }

    /**
     * @return the session of the currently running server
     */
    public static synchronized TeamSession get() {
        return session;
    }

    /**
     * Attaches the manager to the persistent data of the server's current world.
     */
    public static synchronized void load(MinecraftServer server) {
        savedData = TeamcraftSavedData.get(server);
        session = savedData.session();
    }

    /**
     * Clears the current world's session and persists the default state.
     */
    public static synchronized void reset() {
        session.resetAll();
    }

    /**
     * Releases references to a stopped server without modifying its saved data.
     */
    public static synchronized void unload() {
        savedData = null;
        session = new TeamSession();
    }
}
