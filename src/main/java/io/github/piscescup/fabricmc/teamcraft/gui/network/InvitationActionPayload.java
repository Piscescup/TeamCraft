package io.github.piscescup.fabricmc.teamcraft.gui.network;

import io.github.piscescup.fabricmc.teamcraft.References;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.UUID;

/** The server resolves the team and player; the client only submits an action. */
public record InvitationActionPayload(UUID requestId, Action action, String playerName, UUID invitationId)
    implements CustomPacketPayload {
    public static final Type<InvitationActionPayload> TYPE = new Type<>(References.fromPath("invitation_action"));
    public static final StreamCodec<RegistryFriendlyByteBuf, InvitationActionPayload> CODEC =
        CustomPacketPayload.codec(InvitationActionPayload::write, InvitationActionPayload::new);

    private InvitationActionPayload(RegistryFriendlyByteBuf buffer) {
        this(buffer.readUUID(), readAction(buffer), buffer.readUtf(64), buffer.readBoolean() ? buffer.readUUID() : null);
    }

    private static Action readAction(RegistryFriendlyByteBuf buffer) {
        int id = buffer.readVarInt();
        if (id < 0 || id >= Action.values().length) {
            throw new IllegalArgumentException("Unknown TeamCraft invitation action: " + id);
        }
        return Action.values()[id];
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeUUID(requestId);
        buffer.writeVarInt(action.ordinal());
        buffer.writeUtf(playerName, 64);
        buffer.writeBoolean(invitationId != null);
        if (invitationId != null) {
            buffer.writeUUID(invitationId);
        }
    }

    @Override
    public Type<InvitationActionPayload> type() { return TYPE; }

    public enum Action { REFRESH, SEND, ACCEPT, DECLINE }
}
