package io.github.piscescup.fabricmc.teamcraft.team;

import io.github.piscescup.fabricmc.teamcraft.text.Msg;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.TeamColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Computes a split plan from a {@link TeamSession} and applies it to (or removes
 * it from) the vanilla scoreboard. Teams created here always use the
 * {@link #TEAM_ID_PREFIX} prefix so they can be identified even after a server
 * restart.
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class TeamAssigner
{
    /**
     * Prefix of every scoreboard team created by this mod.
     */
    public static final String TEAM_ID_PREFIX = "teamcraft_";

    private TeamAssigner() {
    }

    /**
     * One planned team: its scoreboard id, color, display name and members.
     *
     * @param teamId      the scoreboard team id, e.g. {@code teamcraft_1}
     * @param color       the team color
     * @param displayName the user-visible team name
     * @param members     the usernames assigned to this team
     */
    public record SplitPlan(String teamId, TeamColor color, Component displayName, List<String> members)
    {
    }

    /**
     * Builds the split plan without touching the scoreboard.
     *
     * @param session        the session holding candidates and configuration
     * @param colorOverride  one-shot color list replacing the configured one; {@code null} to use config
     * @param nameOverride   one-shot name list replacing the configured one; {@code null} to use config
     * @return the planned teams, in team order
     */
    public static List<SplitPlan> buildPlan(TeamSession session, List<TeamColor> colorOverride, List<String> nameOverride) {
        List<String> players = new ArrayList<>(session.getCandidates());
        if (session.getMode() == SplitMode.RANDOM) {
            Collections.shuffle(players);
        }

        int[] sizes = session.teamSizes(players.size());
        List<TeamColor> palette = (colorOverride != null && !colorOverride.isEmpty())
            ? colorOverride
            : session.effectiveColors();
        List<String> names = (nameOverride != null) ? nameOverride : session.getNames();

        List<SplitPlan> plans = new ArrayList<>(sizes.length);
        int offset = 0;
        for (int i = 0; i < sizes.length; i++) {
            TeamColor color = palette.get(i % palette.size());
            Component name = resolveName(names, i, color);
            List<String> members = List.copyOf(players.subList(offset, offset + sizes[i]));
            offset += sizes[i];
            plans.add(new SplitPlan(TEAM_ID_PREFIX + (i + 1), color, name, members));
        }
        return plans;
    }

    /**
     * Applies a plan to the scoreboard: creates each team, styles it, and adds
     * its members. The caller must ensure no managed teams exist yet.
     *
     * @param board        the server scoreboard
     * @param plans        the teams to create
     * @param friendlyFire whether teammates may damage each other
     */
    public static void apply(ServerScoreboard board, List<SplitPlan> plans, boolean friendlyFire) {
        for (SplitPlan plan : plans) {
            PlayerTeam team = board.addPlayerTeam(plan.teamId());
            team.setColor(Optional.of(plan.color()));
            team.setDisplayName(plan.displayName());
            team.setAllowFriendlyFire(friendlyFire);
            applyPrefix(team);
            for (String member : plan.members()) {
                board.addPlayerToTeam(member, team);
            }
        }
    }

    /**
     * Rebuilds the {@code [队名] } prefix of a team in its current color. Must be
     * called after every display-name or color change.
     *
     * @param team the team to restyle
     */
    public static void applyPrefix(PlayerTeam team) {
        TeamColor color = team.getColor().orElse(TeamColor.WHITE);
        team.setPlayerPrefix(Component.literal("[")
            .append(team.getDisplayName())
            .append("] ")
            .withColor(color.textColor()));
    }

    /**
     * Removes every scoreboard team created by this mod, releasing its members.
     * Works across server restarts because teams are found by id prefix.
     *
     * @param board the server scoreboard
     * @return how many teams were removed
     */
    public static int clear(ServerScoreboard board) {
        List<String> managed = managedTeamIds(board);
        for (String id : managed) {
            PlayerTeam team = board.getPlayerTeam(id);
            if (team == null) {
                continue;
            }
            for (String member : List.copyOf(team.getPlayers())) {
                board.removePlayerFromTeam(member, team);
            }
            board.removePlayerTeam(team);
        }
        return managed.size();
    }

    /**
     * @param board the server scoreboard
     * @return whether any team created by this mod still exists
     */
    public static boolean existsManagedTeam(ServerScoreboard board) {
        return !managedTeamIds(board).isEmpty();
    }

    private static List<String> managedTeamIds(ServerScoreboard board) {
        List<String> ids = new ArrayList<>();
        for (String id : board.getTeamNames()) {
            if (id.startsWith(TEAM_ID_PREFIX)) {
                ids.add(id);
            }
        }
        return ids;
    }

    private static Component resolveName(List<String> names, int index, TeamColor color) {
        if (index < names.size()) {
            return Component.literal(names.get(index));
        }
        return Msg.tr("team.auto_name", Msg.colorName(color));
    }
}
