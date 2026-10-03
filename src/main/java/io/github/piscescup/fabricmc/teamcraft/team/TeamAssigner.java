package io.github.piscescup.fabricmc.teamcraft.team;

import io.github.piscescup.fabricmc.teamcraft.References;
import io.github.piscescup.fabricmc.teamcraft.text.Msg;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftColor;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.PlayerTeam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Computes a split plan from a {@link TeamSession} and applies it to (or removes
 * it from) the vanilla scoreboard. A created team's readable scoreboard id ends
 * with an internal id using {@link #TEAM_ID_PREFIX}, so it can be identified
 * even after a server restart.
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class TeamAssigner
{
    /**
     * Prefix of every scoreboard team created by this mod.
     */
    public static final String TEAM_ID_PREFIX = References.MOD_ID + "_";

    private TeamAssigner() {
    }


    /**
     * Builds the split plan without touching the scoreboard.
     *
     * @param session        the session holding candidates and configuration
     * @param colorOverride  one-shot color list replacing the configured one; {@code null} to use config
     * @param nameOverride   one-shot name list replacing the configured one; {@code null} to use config
     * @return the planned teams, in team order
     */
    public static List<SplitPlan> buildPlan(TeamSession session, List<TeamcraftColor> colorOverride, List<String> nameOverride) {
        List<String> players = new ArrayList<>(session.getCandidates());
        if (session.getMode() == SplitMode.RANDOM) {
            Collections.shuffle(players);
        }

        int[] sizes = session.teamSizes(players.size());
        List<TeamcraftColor> palette = (colorOverride != null && !colorOverride.isEmpty())
            ? colorOverride
            : session.effectiveColors();
        List<String> names = (nameOverride != null) ? nameOverride : session.getNames();

        List<SplitPlan> plans = new ArrayList<>(sizes.length);
        int offset = 0;
        for (int i = 0; i < sizes.length; i++) {
            TeamcraftColor color = palette.get(i % palette.size());
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
            PlayerTeam team = board.addPlayerTeam(plan.visualTeamString());
            plan.color().applyTo(team);
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
        TeamcraftColor color = TeamcraftColor.ofTeam(team);
        Component displayName = removeLegacyAutoNameColor(team.getDisplayName());
        if (displayName != team.getDisplayName()) {
            team.setDisplayName(displayName);
        }
        team.setPlayerPrefix(Component.literal("[")
            .append(displayName)
            .append("] ")
            .withColor(color.textColor()));
    }

    /**
     * Repairs automatic names created by older builds, where the translated
     * color word carried its original color forever. The text and translation
     * key stay unchanged; only the embedded color override is removed.
     */
    private static Component removeLegacyAutoNameColor(Component displayName) {
        if (!(displayName.getContents() instanceof TranslatableContents contents)
            || !TeamcraftTranslations.TEAM_AUTO_NAME.key().equals(contents.getKey())) {
            return displayName;
        }

        Object[] arguments = contents.getArgs().clone();
        boolean changed = false;
        for (int index = 0; index < arguments.length; index++) {
            if (arguments[index] instanceof Component argument && argument.getStyle().getColor() != null) {
                arguments[index] = argument.copy().setStyle(
                    argument.getStyle().withColor((TextColor) null)
                );
                changed = true;
            }
        }
        if (!changed) {
            return displayName;
        }

        MutableComponent repaired = Component.translatableWithFallback(
            contents.getKey(),
            contents.getFallback(),
            arguments
        ).setStyle(displayName.getStyle());
        for (Component sibling : displayName.getSiblings()) {
            repaired.append(sibling.copy());
        }
        return repaired;
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
     * Removes one TeamCraft-managed scoreboard team and releases its members.
     *
     * @return whether the requested managed team existed and was removed
     */
    public static boolean disband(ServerScoreboard board, String teamId) {
        PlayerTeam team = board.getPlayerTeam(teamId);
        if (team == null || !isManagedTeamId(team.getName())) {
            return false;
        }
        for (String member : List.copyOf(team.getPlayers())) {
            board.removePlayerFromTeam(member, team);
        }
        board.removePlayerTeam(team);
        return true;
    }

    /**
     * @param board the server scoreboard
     * @return whether any team created by this mod still exists
     */
    public static boolean existsManagedTeam(ServerScoreboard board) {
        return !managedTeamIds(board).isEmpty();
    }

    /**
     * Accepts both legacy ids ({@code teamcraft_1}) and readable ids
     * ({@code Red_Team<teamcraft_1>}).
     */
    public static boolean isManagedTeamId(String teamId) {
        if (teamId == null || teamId.isEmpty()) {
            return false;
        }

        String internalId;

        int openBracket = teamId.lastIndexOf('-');

        internalId = teamId.substring(openBracket + 1);

        if (!internalId.startsWith(TEAM_ID_PREFIX)) {
            return false;
        }

        String sequence = internalId.substring(TEAM_ID_PREFIX.length());
        return !sequence.isEmpty() && sequence.chars().allMatch(Character::isDigit);
    }

    private static List<String> managedTeamIds(ServerScoreboard board) {
        List<String> ids = new ArrayList<>();
        for (String id : board.getTeamNames()) {
            if (isManagedTeamId(id)) {
                ids.add(id);
            }
        }
        return ids;
    }

    private static Component resolveName(List<String> names, int index, TeamcraftColor color) {
        if (index < names.size()) {
            return Component.literal(names.get(index));
        }
        return Msg.tr(TeamcraftTranslations.TEAM_AUTO_NAME.key(), Msg.plainColorName(color));
    }
}
