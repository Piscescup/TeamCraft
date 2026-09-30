package io.github.piscescup.fabricmc.teamcraft.gui.network;

import io.github.piscescup.fabricmc.teamcraft.References;
import io.github.piscescup.fabricmc.teamcraft.gui.TeamcraftConfigData;
import io.github.piscescup.fabricmc.teamcraft.gui.TeamInfoData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import java.util.ArrayList;
import java.util.List;

/** Returns the authoritative server snapshot and the result of a GUI action. */
public record ConfigSyncPayload(
    Response response,
    TeamcraftConfigData config,
    TeamInfoData ownTeam,
    List<TeamInfoData> teams
) implements CustomPacketPayload {
    public static final Type<ConfigSyncPayload> TYPE = new Type<>(References.fromPath("config_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigSyncPayload> CODEC =
        CustomPacketPayload.codec(ConfigSyncPayload::write, ConfigSyncPayload::new);

    public ConfigSyncPayload {
        teams = List.copyOf(teams);
    }

    private ConfigSyncPayload(RegistryFriendlyByteBuf buffer) {
        this(
            Response.fromId(buffer.readVarInt()),
            TeamcraftConfigData.read(buffer),
            buffer.readBoolean() ? TeamInfoData.read(buffer) : null,
            readTeams(buffer)
        );
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(this.response.ordinal());
        this.config.write(buffer);
        buffer.writeBoolean(this.ownTeam != null);
        if (this.ownTeam != null) {
            this.ownTeam.write(buffer);
        }
        buffer.writeVarInt(this.teams.size());
        for (TeamInfoData team : this.teams) {
            team.write(buffer);
        }
    }

    @Override
    public Type<ConfigSyncPayload> type() {
        return TYPE;
    }

    public enum Response {
        OPENED,
        SAVED,
        BUILT,
        TEAM_SAVED,
        INVALID,
        PERMISSION_DENIED,
        NO_CANDIDATES,
        TEAMS_EXIST,
        TOO_MANY_TEAMS,
        TEAM_NOT_FOUND;

        private static Response fromId(int id) {
            if (id < 0 || id >= values().length) {
                throw new IllegalArgumentException("Unknown TeamCraft config response: " + id);
            }
            return values()[id];
        }
    }

    private static List<TeamInfoData> readTeams(RegistryFriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        if (size < 0 || size > TeamcraftConfigData.MAX_LIST_SIZE) {
            throw new IllegalArgumentException("Invalid TeamCraft team list size: " + size);
        }
        List<TeamInfoData> teams = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            teams.add(TeamInfoData.read(buffer));
        }
        return teams;
    }
}
