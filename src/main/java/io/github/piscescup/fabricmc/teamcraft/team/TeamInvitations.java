package io.github.piscescup.fabricmc.teamcraft.team;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.LongSupplier;

/** Short-lived invitations, confined to the server thread and never persisted. */
public final class TeamInvitations<T> {
    public static final int LIFETIME_SECONDS = 120;
    private static final long LIFETIME_NANOS = TimeUnit.SECONDS.toNanos(LIFETIME_SECONDS);

    private final Map<UUID, Invitation<T>> pending = new HashMap<>();
    private final LongSupplier clock;

    public TeamInvitations() {
        this(System::nanoTime);
    }

    TeamInvitations(LongSupplier clock) {
        this.clock = clock;
    }

    /** Keeps an existing live invitation rather than silently replacing its team. */
    public boolean offer(UUID targetId, UUID senderId, String senderName, T team) {
        long now = clock.getAsLong();
        expire(now);
        return pending.putIfAbsent(targetId, new Invitation<>(UUID.randomUUID(), senderId, senderName, team, now)) == null;
    }

    public Invitation<T> find(UUID targetId) {
        expire(clock.getAsLong());
        return pending.get(targetId);
    }

    public void remove(UUID targetId) {
        pending.remove(targetId);
    }

    /** Leaving must invalidate sent invitations even if the inviter rejoins the same team later. */
    public void cancelForPlayer(UUID playerId) {
        pending.remove(playerId);
        pending.values().removeIf(invitation -> invitation.senderId().equals(playerId));
    }

    public int remainingSeconds(Invitation<T> invitation) {
        long remaining = LIFETIME_NANOS - (clock.getAsLong() - invitation.createdAt());
        return (int) Math.max(0, (remaining + TimeUnit.SECONDS.toNanos(1) - 1) / TimeUnit.SECONDS.toNanos(1));
    }

    private void expire(long now) {
        pending.values().removeIf(invitation -> now - invitation.createdAt() >= LIFETIME_NANOS);
    }

    public record Invitation<T>(UUID id, UUID senderId, String senderName, T team, long createdAt) {
    }
}
