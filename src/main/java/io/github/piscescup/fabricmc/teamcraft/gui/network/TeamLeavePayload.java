package io.github.piscescup.fabricmc.teamcraft.gui.network;

import io.github.piscescup.fabricmc.teamcraft.References;
import io.github.piscescup.fabricmc.teamcraft.config.TeamInfoData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

/** Self-only leave request. No player name or UUID can be supplied by the client. */
public record TeamLeavePayload(String teamId) implements CustomPacketPayload {
    public static final Type<TeamLeavePayload> TYPE = new Type<>(References.fromPath("team_leave"));
    public static final StreamCodec<RegistryFriendlyByteBuf, TeamLeavePayload> CODEC =
        CustomPacketPayload.codec(TeamLeavePayload::write, TeamLeavePayload::new);

    private TeamLeavePayload(RegistryFriendlyByteBuf buffer) {
        this(buffer.readUtf(TeamInfoData.MAX_TEAM_ID_LENGTH));
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeUtf(teamId, TeamInfoData.MAX_TEAM_ID_LENGTH);
    }

    @Override
    public Type<TeamLeavePayload> type() { return TYPE; }
}
