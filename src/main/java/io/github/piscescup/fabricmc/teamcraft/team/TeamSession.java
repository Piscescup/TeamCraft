package io.github.piscescup.fabricmc.teamcraft.team;

import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftColor;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.RandomAccess;

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
    public static final List<TeamcraftColor> DEFAULT_PALETTE = List.of(
        TeamcraftColor.RED, TeamcraftColor.BLUE, TeamcraftColor.GREEN, TeamcraftColor.YELLOW,
        TeamcraftColor.AQUA, TeamcraftColor.LIGHT_PURPLE, TeamcraftColor.GOLD, TeamcraftColor.DARK_AQUA,
        TeamcraftColor.DARK_GREEN, TeamcraftColor.DARK_PURPLE, TeamcraftColor.WHITE, TeamcraftColor.GRAY,
        TeamcraftColor.DARK_RED, TeamcraftColor.DARK_BLUE, TeamcraftColor.DARK_GRAY, TeamcraftColor.BLACK);

    /**
     * The default number of players per team.
     */
    public static final int DEFAULT_TEAM_SIZE = 4;

    private final List<String> candidates = new DirtyList<>(this::markChanged);
    private final List<String> createdTeams = new DirtyList<>(this::markChanged);
    private int teamSize = DEFAULT_TEAM_SIZE;
    private Integer teamCount = null;
    private SplitMode mode = SplitMode.RANDOM;
    private boolean friendlyFire = false;
    private List<TeamcraftColor> colors = new ArrayList<>();
    private List<String> names = new ArrayList<>();
    private Runnable changeListener = () -> {
    };

    /**
     * @return the ordered, de-duplicated candidate usernames
     */
    public List<String> getCandidates() {
        return this.candidates;
    }

    /**
     * @return the readable scoreboard ids ({@code Red_Team@teamcraft_1}, ...)
     * of teams created by the last start
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
        if (this.teamSize != teamSize || this.teamCount != null) {
            this.teamSize = teamSize;
            this.teamCount = null;
            markChanged();
        }
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
        if (!Objects.equals(this.teamCount, teamCount)) {
            this.teamCount = teamCount;
            markChanged();
        }
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
        Objects.requireNonNull(mode, "mode");
        if (this.mode != mode) {
            this.mode = mode;
            markChanged();
        }
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
        if (this.friendlyFire != friendlyFire) {
            this.friendlyFire = friendlyFire;
            markChanged();
        }
    }

    /**
     * @return the configured color order; empty means the default palette
     */
    public List<TeamcraftColor> getColors() {
        return this.colors;
    }

    /**
     * @param colors the color order to use, in team order; empty resets to default
     */
    public void setColors(List<TeamcraftColor> colors) {
        List<TeamcraftColor> replacement = new ArrayList<>(colors);
        if (!this.colors.equals(replacement)) {
            this.colors = replacement;
            markChanged();
        }
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
        List<String> replacement = new ArrayList<>(names);
        if (!this.names.equals(replacement)) {
            this.names = replacement;
            markChanged();
        }
    }

    /**
     * Restores every configuration option to its default value.
     */
    public void resetConfig() {
        boolean changed = this.teamSize != DEFAULT_TEAM_SIZE
            || this.teamCount != null
            || this.mode != SplitMode.RANDOM
            || this.friendlyFire
            || !this.colors.isEmpty()
            || !this.names.isEmpty();
        this.teamSize = DEFAULT_TEAM_SIZE;
        this.teamCount = null;
        this.mode = SplitMode.RANDOM;
        this.friendlyFire = false;
        this.colors = new ArrayList<>();
        this.names = new ArrayList<>();
        if (changed) {
            markChanged();
        }
    }

    /**
     * Restores candidates, created-team references and configuration defaults.
     */
    public void resetAll() {
        this.candidates.clear();
        this.createdTeams.clear();
        resetConfig();
    }

    /**
     * Installs the callback used by the server persistence adapter.
     */
    public void setChangeListener(Runnable changeListener) {
        this.changeListener = Objects.requireNonNull(changeListener, "changeListener");
    }

    private void markChanged() {
        this.changeListener.run();
    }

    /**
     * @return the palette in effect: the configured colors, or the default palette
     */
    public List<TeamcraftColor> effectiveColors() {
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

    /** A list implementation that marks the owning session dirty on mutation. */
    private static final class DirtyList<E> extends AbstractList<E> implements RandomAccess {
        private final List<E> values = new ArrayList<>();
        private final Runnable changed;

        private DirtyList(Runnable changed) {
            this.changed = changed;
        }

        @Override
        public E get(int index) {
            return this.values.get(index);
        }

        @Override
        public int size() {
            return this.values.size();
        }

        @Override
        public E set(int index, E element) {
            E previous = this.values.set(index, element);
            if (!Objects.equals(previous, element)) {
                this.changed.run();
            }
            return previous;
        }

        @Override
        public void add(int index, E element) {
            this.values.add(index, element);
            this.modCount++;
            this.changed.run();
        }

        @Override
        public E remove(int index) {
            E removed = this.values.remove(index);
            this.modCount++;
            this.changed.run();
            return removed;
        }

        @Override
        public void clear() {
            if (!this.values.isEmpty()) {
                this.values.clear();
                this.modCount++;
                this.changed.run();
            }
        }
    }
}
