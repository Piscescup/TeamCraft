package io.github.piscescup.fabricmc.teamcraft.gui.network;

import io.github.piscescup.fabricmc.teamcraft.References;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Requests the current server-owned TeamCraft configuration. */
public record ConfigRequestPayload() implements CustomPacketPayload {
    public static final ConfigRequestPayload INSTANCE = new ConfigRequestPayload();
    public static final Type<ConfigRequestPayload> TYPE = new Type<>(References.fromPath("config_request"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigRequestPayload> CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public Type<ConfigRequestPayload> type() {
        return TYPE;
    }
}
