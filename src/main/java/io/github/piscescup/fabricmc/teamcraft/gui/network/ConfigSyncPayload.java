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
    List<String> candidates,
    List<String> onlinePlayers,
    TeamInfoData ownTeam,
    List<TeamInfoData> teams
) implements CustomPacketPayload {
    public static final Type<ConfigSyncPayload> TYPE = new Type<>(References.fromPath("config_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigSyncPayload> CODEC =
        CustomPacketPayload.codec(ConfigSyncPayload::write, ConfigSyncPayload::new);

    public ConfigSyncPayload {
        candidates = List.copyOf(candidates);
        onlinePlayers = List.copyOf(onlinePlayers);
        teams = List.copyOf(teams);
    }

    private ConfigSyncPayload(RegistryFriendlyByteBuf buffer) {
        this(
            Response.fromId(buffer.readVarInt()),
            TeamcraftConfigData.read(buffer),
            readNames(buffer, "candidate", TeamcraftConfigData.MAX_CANDIDATES),
            readNames(buffer, "online player", TeamcraftConfigData.MAX_CANDIDATES),
            buffer.readBoolean() ? TeamInfoData.read(buffer) : null,
            readTeams(buffer)
        );
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeVarInt(this.response.ordinal());
        this.config.write(buffer);
        writeNames(buffer, this.candidates);
        writeNames(buffer, this.onlinePlayers);
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
        TEAM_NOT_FOUND,
        TEAM_DISBANDED,
        ALL_TEAMS_CLEARED;

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

    private static void writeNames(RegistryFriendlyByteBuf buffer, List<String> names) {
        buffer.writeVarInt(names.size());
        for (String name : names) {
            buffer.writeUtf(name, TeamcraftConfigData.MAX_NAME_LENGTH);
        }
    }

    private static List<String> readNames(RegistryFriendlyByteBuf buffer, String field, int maximum) {
        int size = buffer.readVarInt();
        if (size < 0 || size > maximum) {
            throw new IllegalArgumentException("Invalid TeamCraft " + field + " list size: " + size);
        }
        List<String> names = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            names.add(buffer.readUtf(TeamcraftConfigData.MAX_NAME_LENGTH));
        }
        return names;
    }
}
