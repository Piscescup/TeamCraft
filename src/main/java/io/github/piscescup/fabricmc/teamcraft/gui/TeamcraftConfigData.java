package io.github.piscescup.fabricmc.teamcraft.gui;

import io.github.piscescup.fabricmc.teamcraft.team.SplitMode;
import io.github.piscescup.fabricmc.teamcraft.team.TeamSession;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.scores.TeamColor;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A transport-friendly snapshot of the settings edited by the TeamCraft GUI.
 *
 * <p>The live values remain owned by the logical server in {@link TeamSession};
 * this record is only used to move an atomic snapshot between the server and a
 * client screen.</p>
 */
public record TeamcraftConfigData(
    boolean fixedTeamCount,
    int playersPerTeam,
    int teamCount,
    SplitMode mode,
    boolean friendlyFire,
    List<TeamColor> colors,
    List<String> names
) {
    public static final int MIN_PLAYERS_PER_TEAM = 1;
    public static final int MAX_PLAYERS_PER_TEAM = 1000;
    public static final int MIN_TEAM_COUNT = 1;
    public static final int MAX_TEAM_COUNT = 100;
    public static final int MAX_LIST_SIZE = 100;
    public static final int MAX_CANDIDATES = 1000;
    public static final int MAX_NAME_LENGTH = 64;

    private static final int DEFAULT_TEAM_COUNT = 2;

    public TeamcraftConfigData {
        mode = Objects.requireNonNull(mode, "mode");
        colors = List.copyOf(colors);
        names = List.copyOf(names);
    }

    /**
     * Creates a snapshot from the current server session.
     */
    public static TeamcraftConfigData fromSession(TeamSession session) {
        Integer configuredTeamCount = session.getTeamCount();
        return new TeamcraftConfigData(
            configuredTeamCount != null,
            session.getTeamSize(),
            configuredTeamCount == null ? DEFAULT_TEAM_COUNT : configuredTeamCount,
            session.getMode(),
            session.isFriendlyFire(),
            session.getColors(),
            session.getNames()
        );
    }

    /**
     * @return the values shown after pressing the GUI's restore-defaults button
     */
    public static TeamcraftConfigData defaults() {
        return new TeamcraftConfigData(
            false,
            TeamSession.DEFAULT_TEAM_SIZE,
            DEFAULT_TEAM_COUNT,
            SplitMode.RANDOM,
            false,
            List.of(),
            List.of()
        );
    }

    /**
     * Validates every value sent by a client before it touches server state.
     */
    public ValidationResult validate() {
        if (this.playersPerTeam < MIN_PLAYERS_PER_TEAM || this.playersPerTeam > MAX_PLAYERS_PER_TEAM) {
            return ValidationResult.INVALID_PLAYERS_PER_TEAM;
        }
        if (this.teamCount < MIN_TEAM_COUNT || this.teamCount > MAX_TEAM_COUNT) {
            return ValidationResult.INVALID_TEAM_COUNT;
        }
        if (this.colors.size() > MAX_LIST_SIZE) {
            return ValidationResult.TOO_MANY_COLORS;
        }
        if (this.names.size() > MAX_LIST_SIZE) {
            return ValidationResult.TOO_MANY_NAMES;
        }
        if (this.names.stream().anyMatch(name -> name.isBlank() || name.length() > MAX_NAME_LENGTH)) {
            return ValidationResult.INVALID_NAME;
        }
        return ValidationResult.VALID;
    }

    /**
     * Applies this already-validated snapshot to the server session.
     */
    public void applyTo(TeamSession session) {
        // Set the inactive value too, so switching rules in the GUI preserves it.
        session.setTeamSize(this.playersPerTeam);
        if (this.fixedTeamCount) {
            session.setTeamCount(this.teamCount);
        }
        session.setMode(this.mode);
        session.setFriendlyFire(this.friendlyFire);
        session.setColors(this.colors);
        session.setNames(this.names);
    }

    /**
     * Writes a bounded representation used by both GUI payloads.
     */
    public void write(FriendlyByteBuf buffer) {
        buffer.writeBoolean(this.fixedTeamCount);
        buffer.writeVarInt(this.playersPerTeam);
        buffer.writeVarInt(this.teamCount);
        buffer.writeUtf(this.mode.getSerializedName(), 16);
        buffer.writeBoolean(this.friendlyFire);

        buffer.writeVarInt(this.colors.size());
        for (TeamColor color : this.colors) {
            buffer.writeUtf(color.getSerializedName(), 32);
        }

        buffer.writeVarInt(this.names.size());
        for (String name : this.names) {
            buffer.writeUtf(name, MAX_NAME_LENGTH);
        }
    }

    /**
     * Reads the bounded representation used by both GUI payloads.
     */
    public static TeamcraftConfigData read(FriendlyByteBuf buffer) {
        boolean fixedTeamCount = buffer.readBoolean();
        int playersPerTeam = buffer.readVarInt();
        int teamCount = buffer.readVarInt();

        String modeName = buffer.readUtf(16);
        SplitMode mode = Objects.requireNonNull(
            SplitMode.fromName(modeName),
            () -> "Unknown TeamCraft split mode: " + modeName
        );
        boolean friendlyFire = buffer.readBoolean();

        int colorCount = readCollectionSize(buffer, "colors");
        List<TeamColor> colors = new ArrayList<>(colorCount);
        for (int i = 0; i < colorCount; i++) {
            String colorName = buffer.readUtf(32);
            colors.add(Objects.requireNonNull(
                TeamColor.byName(colorName),
                () -> "Unknown team color: " + colorName
            ));
        }

        int nameCount = readCollectionSize(buffer, "names");
        List<String> names = new ArrayList<>(nameCount);
        for (int i = 0; i < nameCount; i++) {
            names.add(buffer.readUtf(MAX_NAME_LENGTH));
        }

        return new TeamcraftConfigData(
            fixedTeamCount,
            playersPerTeam,
            teamCount,
            mode,
            friendlyFire,
            colors,
            names
        );
    }

    private static int readCollectionSize(FriendlyByteBuf buffer, String field) {
        int size = buffer.readVarInt();
        if (size < 0 || size > MAX_LIST_SIZE) {
            throw new IllegalArgumentException("Invalid TeamCraft " + field + " list size: " + size);
        }
        return size;
    }

    public enum ValidationResult {
        VALID,
        INVALID_PLAYERS_PER_TEAM,
        INVALID_TEAM_COUNT,
        TOO_MANY_COLORS,
        TOO_MANY_NAMES,
        INVALID_NAME
    }
}
