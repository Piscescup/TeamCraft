package io.github.piscescup.fabricmc.teamcraft.team;

/**
 * Holds the single {@link TeamSession} for the running server. The session is
 * reset when the server stops so a fresh world never inherits stale candidates
 * or configuration.
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class TeamSessionManager
{
    private static TeamSession session = new TeamSession();

    private TeamSessionManager() {
    }

    /**
     * @return the session of the currently running server
     */
    public static synchronized TeamSession get() {
        return session;
    }

    /**
     * Replaces the session with a fresh, default one.
     */
    public static synchronized void reset() {
        session = new TeamSession();
    }
}
