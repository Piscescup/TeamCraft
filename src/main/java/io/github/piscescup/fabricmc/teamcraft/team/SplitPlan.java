package io.github.piscescup.fabricmc.teamcraft.team;

import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftColor;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * One planned team: its scoreboard id, color, display name and members.
 *
 * @param teamId      the scoreboard team id, e.g. {@code teamcraft_1}
 * @param color       the team color
 * @param displayName the user-visible team name
 * @param members     the usernames assigned to this team
 */
public record SplitPlan(String teamId, TeamcraftColor color, Component displayName, List<String> members) {

    @NonNull
    @Override
    public String toString() {
        return visualTeamString() + ":" + members;
    }

    /**
     * Returns the readable, command-safe scoreboard id.
     */
    @NonNull
    public String visualTeamString() {
        return (displayName.getString() + "-" + teamId).replace(' ', '_');
    }
}
