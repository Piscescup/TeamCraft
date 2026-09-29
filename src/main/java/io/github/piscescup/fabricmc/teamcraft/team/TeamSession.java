package io.github.piscescup.fabricmc.teamcraft.team;

import net.minecraft.world.scores.TeamColor;

import java.util.ArrayList;
import java.util.List;

/**
 * The mutable per-server state of one split-teams workflow: the ordered candidate
 * list, the split configuration, and the teams created by the last {@code start}.
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class TeamSession
{
    /**
     * The palette used when no custom colors are configured, ordered for readability.
     */
    public static final List<TeamColor> DEFAULT_PALETTE = List.of(
        TeamColor.RED, TeamColor.BLUE, TeamColor.GREEN, TeamColor.YELLOW,
        TeamColor.AQUA, TeamColor.LIGHT_PURPLE, TeamColor.GOLD, TeamColor.DARK_AQUA,
        TeamColor.DARK_GREEN, TeamColor.DARK_PURPLE, TeamColor.WHITE, TeamColor.GRAY,
        TeamColor.DARK_RED, TeamColor.DARK_BLUE, TeamColor.DARK_GRAY, TeamColor.BLACK);

    /**
     * The default number of players per team.
     */
    public static final int DEFAULT_TEAM_SIZE = 4;

    private final List<String> candidates = new ArrayList<>();
    private final List<String> createdTeams = new ArrayList<>();
    private int teamSize = DEFAULT_TEAM_SIZE;
    private Integer teamCount = null;
    private SplitMode mode = SplitMode.RANDOM;
    private boolean friendlyFire = false;
    private List<TeamColor> colors = new ArrayList<>();
    private List<String> names = new ArrayList<>();

    /**
     * @return the ordered, de-duplicated candidate usernames
     */
    public List<String> getCandidates() {
        return this.candidates;
    }

    /**
     * @return the internal ids ({@code teamcraft_1}, ...) of teams created by the last start
     */
    public List<String> getCreatedTeams() {
        return this.createdTeams;
    }

    /**
     * @return players per team; only meaningful when {@link #getTeamCount()} is {@code null}
     */
    public int getTeamSize() {
        return this.teamSize;
    }

    /**
     * Sets players-per-team mode and clears any team-count override.
     *
     * @param teamSize players per team, {@code >= 1}
     */
    public void setTeamSize(int teamSize) {
        this.teamSize = teamSize;
        this.teamCount = null;
    }

    /**
     * @return the fixed team-count override, or {@code null} when splitting by team size
     */
    public Integer getTeamCount() {
        return this.teamCount;
    }

    /**
     * Sets fixed-team-count mode, distributing players as evenly as possible.
     *
     * @param teamCount number of teams, {@code >= 1}
     */
    public void setTeamCount(int teamCount) {
        this.teamCount = teamCount;
    }

    /**
     * @return the split strategy
     */
    public SplitMode getMode() {
        return this.mode;
    }

    /**
     * @param mode the split strategy
     */
    public void setMode(SplitMode mode) {
        this.mode = mode;
    }

    /**
     * @return whether members of the same team may damage each other
     */
    public boolean isFriendlyFire() {
        return this.friendlyFire;
    }

    /**
     * @param friendlyFire whether members of the same team may damage each other
     */
    public void setFriendlyFire(boolean friendlyFire) {
        this.friendlyFire = friendlyFire;
    }

    /**
     * @return the configured color order; empty means the default palette
     */
    public List<TeamColor> getColors() {
        return this.colors;
    }

    /**
     * @param colors the color order to use, in team order; empty resets to default
     */
    public void setColors(List<TeamColor> colors) {
        this.colors = new ArrayList<>(colors);
    }

    /**
     * @return the configured team display names; empty means color-based auto names
     */
    public List<String> getNames() {
        return this.names;
    }

    /**
     * @param names the display names to use, in team order
     */
    public void setNames(List<String> names) {
        this.names = new ArrayList<>(names);
    }

    /**
     * Restores every configuration option to its default value.
     */
    public void resetConfig() {
        this.teamSize = DEFAULT_TEAM_SIZE;
        this.teamCount = null;
        this.mode = SplitMode.RANDOM;
        this.friendlyFire = false;
        this.colors = new ArrayList<>();
        this.names = new ArrayList<>();
    }

    /**
     * @return the palette in effect: the configured colors, or the default palette
     */
    public List<TeamColor> effectiveColors() {
        return this.colors.isEmpty() ? DEFAULT_PALETTE : this.colors;
    }

    /**
     * @param playerCount the number of candidates to distribute
     * @return how many teams {@code start} would create
     */
    public int resolveTeamCount(int playerCount) {
        if (this.teamCount != null) {
            return this.teamCount;
        }
        return (playerCount + this.teamSize - 1) / this.teamSize;
    }

    /**
     * @param playerCount the number of candidates to distribute
     * @return the size of each team; sum equals {@code playerCount}
     */
    public int[] teamSizes(int playerCount) {
        int teams = this.resolveTeamCount(playerCount);
        int[] sizes = new int[teams];
        if (this.teamCount != null) {
            int base = playerCount / teams;
            int remainder = playerCount % teams;
            for (int i = 0; i < teams; i++) {
                sizes[i] = base + (i < remainder ? 1 : 0);
            }
        }
        else {
            for (int i = 0; i < teams; i++) {
                sizes[i] = Math.max(0, Math.min(this.teamSize, playerCount - i * this.teamSize));
            }
        }
        return sizes;
    }
}
