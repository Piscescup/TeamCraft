package io.github.piscescup.fabricmc.teamcraft.gui;

import io.github.piscescup.fabricmc.teamcraft.gui.network.InvitationActionPayload;
import io.github.piscescup.fabricmc.teamcraft.gui.network.InvitationSyncPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/** Correlates responses even when the originating page is closed or covered by a dialog. */
@Environment(EnvType.CLIENT)
public final class TeamcraftInvitationClient {
    private static final Map<UUID, PendingRequest> PENDING = new HashMap<>();
    private static final long TIMEOUT = TimeUnit.SECONDS.toNanos(15);

    private TeamcraftInvitationClient() {
    }

    public static void register() {
        ClientPlayNetworking.registerGlobalReceiver(InvitationSyncPayload.TYPE, (payload, context) -> {
            PendingRequest pending = PENDING.remove(payload.requestId());
            if (pending != null) { pending.response().accept(payload); }
        });
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            long now = System.nanoTime();
            var expired = PENDING.entrySet().stream()
                .filter(entry -> now - entry.getValue().startedAt() >= TIMEOUT)
                .map(Map.Entry::getKey).toList();
            for (UUID id : expired) {
                PendingRequest pending = PENDING.remove(id);
                if (pending != null) { pending.timeout().run(); }
            }
        });
        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> PENDING.clear());
    }

    public static boolean send(InvitationActionPayload payload, Consumer<InvitationSyncPayload> response, Runnable timeout) {
        if (!ClientPlayNetworking.canSend(InvitationActionPayload.TYPE)) { return false; }
        PENDING.put(payload.requestId(), new PendingRequest(System.nanoTime(), response, timeout));
        ClientPlayNetworking.send(payload);
        return true;
    }

    public static void cancel(UUID requestId) {
        if (requestId != null) { PENDING.remove(requestId); }
    }

    private record PendingRequest(long startedAt, Consumer<InvitationSyncPayload> response, Runnable timeout) {
    }
}
