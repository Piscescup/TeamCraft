package io.github.piscescup.fabricmc.teamcraft.gui.network;

import io.github.piscescup.fabricmc.teamcraft.References;
import io.github.piscescup.fabricmc.teamcraft.config.TeamInfoData;
import io.github.piscescup.fabricmc.teamcraft.config.TeamcraftConfigData;
import io.github.piscescup.fabricmc.teamcraft.team.TeamInvitations;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Authoritative invitation state and the result of the matching request. */
public record InvitationSyncPayload(UUID requestId, Response response, Component message,
                                    TeamInfoData ownTeam, List<TeamInfoData> teams,
                                    List<String> eligiblePlayers, PendingInvitation invitation, boolean canInvite)
    implements CustomPacketPayload {
    public static final Type<InvitationSyncPayload> TYPE = new Type<>(References.fromPath("invitation_sync"));
    public static final StreamCodec<RegistryFriendlyByteBuf, InvitationSyncPayload> CODEC =
        CustomPacketPayload.codec(InvitationSyncPayload::write, InvitationSyncPayload::new);

    public InvitationSyncPayload {
        teams = List.copyOf(teams);
        eligiblePlayers = List.copyOf(eligiblePlayers);
    }

    private InvitationSyncPayload(RegistryFriendlyByteBuf buffer) {
        this(buffer.readUUID(), readResponse(buffer), ComponentSerialization.STREAM_CODEC.decode(buffer),
            buffer.readBoolean() ? TeamInfoData.read(buffer) : null, readTeams(buffer), readPlayers(buffer),
            buffer.readBoolean() ? PendingInvitation.read(buffer) : null, buffer.readBoolean());
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeUUID(requestId);
        buffer.writeVarInt(response.ordinal());
        ComponentSerialization.STREAM_CODEC.encode(buffer, message);
        buffer.writeBoolean(ownTeam != null);
        if (ownTeam != null) { ownTeam.write(buffer); }
        buffer.writeVarInt(teams.size());
        for (TeamInfoData team : teams) { team.write(buffer); }
        buffer.writeVarInt(eligiblePlayers.size());
        for (String player : eligiblePlayers) { buffer.writeUtf(player, 64); }
        buffer.writeBoolean(invitation != null);
        if (invitation != null) { invitation.write(buffer); }
        buffer.writeBoolean(canInvite);
    }

    private static Response readResponse(RegistryFriendlyByteBuf buffer) {
        int id = buffer.readVarInt();
        if (id < 0 || id >= Response.values().length) {
            throw new IllegalArgumentException("Unknown TeamCraft invitation response: " + id);
        }
        return Response.values()[id];
    }

    private static List<TeamInfoData> readTeams(RegistryFriendlyByteBuf buffer) {
        int size = checkedSize(buffer, TeamcraftConfigData.MAX_LIST_SIZE);
        List<TeamInfoData> teams = new ArrayList<>(size);
        for (int i = 0; i < size; i++) { teams.add(TeamInfoData.read(buffer)); }
        return teams;
    }

    private static List<String> readPlayers(RegistryFriendlyByteBuf buffer) {
        int size = checkedSize(buffer, TeamcraftConfigData.MAX_CANDIDATES);
        List<String> players = new ArrayList<>(size);
        for (int i = 0; i < size; i++) { players.add(buffer.readUtf(64)); }
        return players;
    }

    private static int checkedSize(RegistryFriendlyByteBuf buffer, int maximum) {
        int size = buffer.readVarInt();
        if (size < 0 || size > maximum) { throw new IllegalArgumentException("Invalid invitation list size: " + size); }
        return size;
    }

    @Override
    public Type<InvitationSyncPayload> type() { return TYPE; }

    public enum Response { SNAPSHOT, SUCCESS, ERROR, PERMISSION_DENIED }

    public record PendingInvitation(UUID id, String senderName, String teamId, Component teamName, int secondsLeft) {
        private static PendingInvitation read(RegistryFriendlyByteBuf buffer) {
            UUID id = buffer.readUUID();
            String sender = buffer.readUtf(64);
            String teamId = buffer.readUtf(TeamInfoData.MAX_TEAM_ID_LENGTH);
            Component name = ComponentSerialization.STREAM_CODEC.decode(buffer);
            int seconds = buffer.readVarInt();
            if (seconds < 0 || seconds > TeamInvitations.LIFETIME_SECONDS) { throw new IllegalArgumentException("Invalid invitation lifetime: " + seconds); }
            return new PendingInvitation(id, sender, teamId, name, seconds);
        }

        private void write(RegistryFriendlyByteBuf buffer) {
            buffer.writeUUID(id);
            buffer.writeUtf(senderName, 64);
            buffer.writeUtf(teamId, TeamInfoData.MAX_TEAM_ID_LENGTH);
            ComponentSerialization.STREAM_CODEC.encode(buffer, teamName);
            buffer.writeVarInt(secondsLeft);
        }
    }
}
