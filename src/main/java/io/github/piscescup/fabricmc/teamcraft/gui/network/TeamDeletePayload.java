package io.github.piscescup.fabricmc.teamcraft.gui.network;

import io.github.piscescup.fabricmc.teamcraft.References;
import io.github.piscescup.fabricmc.teamcraft.gui.TeamInfoData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Requests removal of one managed team or every TeamCraft-managed team. */
public record TeamDeletePayload(Target target, String teamId) implements CustomPacketPayload {
    public static final Type<TeamDeletePayload> TYPE = new Type<>(References.fromPath("team_delete"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TeamDeletePayload> CODEC =
        CustomPacketPayload.codec(TeamDeletePayload::write, TeamDeletePayload::new);

    public TeamDeletePayload(Target target) {
        this(target, "");
    }

    private TeamDeletePayload(RegistryFriendlyByteBuf buffer) {
        this(Target.fromId(buffer.readVarInt()), buffer.readUtf(TeamInfoData.MAX_TEAM_ID_LENGTH));
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(this.target.ordinal());
        buffer.writeUtf(this.teamId, TeamInfoData.MAX_TEAM_ID_LENGTH);
    }

    @Override
    public Type<TeamDeletePayload> type() {
        return TYPE;
    }

    public enum Target {
        ONE,
        ALL;

        private static Target fromId(int id) {
            if (id < 0 || id >= values().length) {
                throw new IllegalArgumentException("Unknown TeamCraft delete target: " + id);
            }
            return values()[id];
        }
    }
}
