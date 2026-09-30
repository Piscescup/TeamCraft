package io.github.piscescup.fabricmc.teamcraft.gui.network;

import io.github.piscescup.fabricmc.teamcraft.References;
import io.github.piscescup.fabricmc.teamcraft.gui.TeamcraftConfigData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Sends one complete configuration draft and optionally starts the split. */
public record ConfigUpdatePayload(TeamcraftConfigData config, boolean buildTeams) implements CustomPacketPayload {
    public static final Type<ConfigUpdatePayload> TYPE = new Type<>(References.fromPath("config_update"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigUpdatePayload> CODEC =
        CustomPacketPayload.codec(ConfigUpdatePayload::write, ConfigUpdatePayload::new);

    private ConfigUpdatePayload(RegistryFriendlyByteBuf buffer) {
        this(TeamcraftConfigData.read(buffer), buffer.readBoolean());
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        this.config.write(buffer);
        buffer.writeBoolean(this.buildTeams);
    }

    @Override
    public Type<ConfigUpdatePayload> type() {
        return TYPE;
    }
}
