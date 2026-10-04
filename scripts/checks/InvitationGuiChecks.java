package io.github.piscescup.fabricmc.teamcraft.team;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/** Standalone checks for GUI invitation identity and expiry. */
public final class InvitationGuiChecks {
    public static void main(String[] args) {
        AtomicLong clock = new AtomicLong();
        TeamInvitations<Object> invitations = new TeamInvitations<>(clock::get);
        UUID sender = UUID.randomUUID();
        UUID target = UUID.randomUUID();
        Object team = new Object();
        check(invitations.offer(target, sender, "Sender", team), "First offer succeeds");
        var original = invitations.find(target);
        check(original.id() != null, "GUI identity is present");
        check(original.team() == team && original.senderId().equals(sender), "Sender and exact team are retained");
        check(invitations.remainingSeconds(original) == 120, "Initial lifetime");
        check(!invitations.offer(target, sender, "Sender", new Object()), "No silent replacement");
        check(invitations.find(target).id().equals(original.id()), "Rejected duplicate retains original GUI identity");
        clock.addAndGet(TimeUnit.SECONDS.toNanos(1) + 1);
        check(invitations.remainingSeconds(original) == 119, "Lifetime rounds up");
        clock.set(TimeUnit.SECONDS.toNanos(120) - 1);
        check(invitations.find(target) != null && invitations.remainingSeconds(original) == 1, "Just before expiry");
        clock.incrementAndGet();
        check(invitations.find(target) == null && invitations.remainingSeconds(original) == 0, "Exact expiry boundary");
        check(invitations.offer(target, sender, "Sender", team), "Can offer after expiry");
        check(!invitations.find(target).id().equals(original.id()), "Old GUI identity cannot refer to a new invitation");
        invitations.remove(target);
        check(invitations.find(target) == null, "Consumed invitations cannot be replayed");
        TeamInvitations<Object> separateServer = new TeamInvitations<>(clock::get);
        check(invitations.offer(target, sender, "Sender", team), "New offer after consumption");
        check(separateServer.find(target) == null, "Server instances are isolated");
        clock.set(Long.MAX_VALUE - 100);
        var wrapping = new TeamInvitations<Object>(clock::get);
        wrapping.offer(target, sender, "Sender", team);
        clock.set(Long.MIN_VALUE + 100);
        check(wrapping.find(target) != null, "nanoTime wrap does not expire early");
        clock.addAndGet(TimeUnit.SECONDS.toNanos(120));
        check(wrapping.find(target) == null, "Expiry works across nanoTime wrap");
        checkLeaveCancellation();
        System.out.println("Invitation identity, expiry, and leave-team cancellation checks passed.");
    }

    private static void checkLeaveCancellation() {
        TeamInvitations<Object> invitations = new TeamInvitations<>(() -> 0L);
        UUID leaving = UUID.randomUUID();
        UUID otherSender = UUID.randomUUID();
        UUID outgoingA = UUID.randomUUID();
        UUID outgoingB = UUID.randomUUID();
        UUID unrelated = UUID.randomUUID();
        Object team = new Object();
        check(invitations.offer(outgoingA, leaving, "Leaving", team), "First outgoing invitation");
        check(invitations.offer(outgoingB, leaving, "Leaving", team), "Second outgoing invitation");
        check(invitations.offer(leaving, otherSender, "Other", team), "Incoming invitation");
        check(invitations.offer(unrelated, otherSender, "Other", team), "Unrelated invitation");
        invitations.cancelForPlayer(leaving);
        check(invitations.find(leaving) == null, "Leaving cancels received invitations");
        check(invitations.find(outgoingA) == null && invitations.find(outgoingB) == null,
            "Leaving cancels all sent invitations");
        check(invitations.find(unrelated) != null, "Other players' invitations remain intact");
        invitations.cancelForPlayer(leaving);
        check(invitations.find(unrelated) != null, "Repeated cancellation remains self-only");
        check(invitations.offer(outgoingA, otherSender, "Other", team), "Cancelled recipients can receive new invitations");
    }

    private static void check(boolean condition, String message) {
        if (!condition) { throw new AssertionError(message); }
    }
}
