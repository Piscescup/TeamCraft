package io.github.piscescup.fabricmc.teamcraft.gui.network;

import io.github.piscescup.fabricmc.teamcraft.References;
import io.github.piscescup.fabricmc.teamcraft.gui.TeamInfoData;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftColor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.Objects;

/** Updates the managed team to which the sending player currently belongs. */
public record TeamUpdatePayload(
    String teamId,
    String displayName,
    TeamcraftColor color,
    boolean friendlyFire
) implements CustomPacketPayload {
    public static final Type<TeamUpdatePayload> TYPE = new Type<>(References.fromPath("team_update"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TeamUpdatePayload> CODEC =
        CustomPacketPayload.codec(TeamUpdatePayload::write, TeamUpdatePayload::new);

    private TeamUpdatePayload(RegistryFriendlyByteBuf buffer) {
        this(
            buffer.readUtf(TeamInfoData.MAX_TEAM_ID_LENGTH),
            buffer.readUtf(TeamInfoData.MAX_DISPLAY_NAME_LENGTH),
            readColor(buffer),
            buffer.readBoolean()
        );
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeUtf(this.teamId, TeamInfoData.MAX_TEAM_ID_LENGTH);
        buffer.writeUtf(this.displayName, TeamInfoData.MAX_DISPLAY_NAME_LENGTH);
        buffer.writeUtf(this.color.word(), 32);
        buffer.writeBoolean(this.friendlyFire);
    }

    private static TeamcraftColor readColor(RegistryFriendlyByteBuf buffer) {
        String name = buffer.readUtf(32);
        return Objects.requireNonNull(TeamcraftColor.byName(name), () -> "Unknown team color: " + name);
    }

    @Override
    public Type<TeamUpdatePayload> type() {
        return TYPE;
    }
}
