package io.github.piscescup.fabricmc.teamcraft.gui;

import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftColor;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.world.scores.PlayerTeam;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** A client-safe snapshot of one TeamCraft-managed scoreboard team. */
public record TeamInfoData(
    String id,
    Component displayName,
    TeamcraftColor color,
    boolean friendlyFire,
    List<String> members
) {
    /**
     * Includes the readable display-name prefix, the internal TeamCraft id and
     * legacy ids accidentally produced from Component#toString().
     */
    public static final int MAX_TEAM_ID_LENGTH = 256;
    public static final int MAX_DISPLAY_NAME_LENGTH = 64;
    public static final int MAX_MEMBERS = 1024;

    public TeamInfoData {
        id = Objects.requireNonNull(id, "id");
        if (id.length() > MAX_TEAM_ID_LENGTH) {
            throw new IllegalArgumentException("Team id exceeds " + MAX_TEAM_ID_LENGTH + " characters");
        }
        displayName = Objects.requireNonNull(displayName, "displayName");
        color = Objects.requireNonNull(color, "color");
        members = List.copyOf(members);
    }

    public static TeamInfoData fromTeam(PlayerTeam team) {
        return new TeamInfoData(
            team.getName(),
            team.getDisplayName(),
            TeamcraftColor.ofTeam(team),
            team.isAllowFriendlyFire(),
            List.copyOf(team.getPlayers())
        );
    }

    public static boolean canEncode(PlayerTeam team) {
        return team.getName().length() <= MAX_TEAM_ID_LENGTH;
    }

    public void write(RegistryFriendlyByteBuf buffer) {
        buffer.writeUtf(this.id, MAX_TEAM_ID_LENGTH);
        ComponentSerialization.STREAM_CODEC.encode(buffer, this.displayName);
        buffer.writeUtf(this.color.word(), 32);
        buffer.writeBoolean(this.friendlyFire);
        buffer.writeVarInt(this.members.size());
        for (String member : this.members) {
            buffer.writeUtf(member, 64);
        }
    }

    public static TeamInfoData read(RegistryFriendlyByteBuf buffer) {
        String id = buffer.readUtf(MAX_TEAM_ID_LENGTH);
        Component displayName = ComponentSerialization.STREAM_CODEC.decode(buffer);
        String colorName = buffer.readUtf(32);
        TeamcraftColor color = Objects.requireNonNull(
            TeamcraftColor.byName(colorName),
            () -> "Unknown team color: " + colorName
        );
        boolean friendlyFire = buffer.readBoolean();
        int memberCount = buffer.readVarInt();
        if (memberCount < 0 || memberCount > MAX_MEMBERS) {
            throw new IllegalArgumentException("Invalid TeamCraft member list size: " + memberCount);
        }
        List<String> members = new ArrayList<>(memberCount);
        for (int i = 0; i < memberCount; i++) {
            members.add(buffer.readUtf(64));
        }
        return new TeamInfoData(id, displayName, color, friendlyFire, members);
    }
}
