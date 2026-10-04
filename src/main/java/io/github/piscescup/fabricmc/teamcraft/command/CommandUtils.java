package io.github.piscescup.fabricmc.teamcraft.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.piscescup.fabricmc.teamcraft.team.TeamAssigner;
import io.github.piscescup.fabricmc.teamcraft.text.Msg;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import org.jspecify.annotations.NonNull;

import java.util.*;
import java.util.concurrent.CompletableFuture;

/**
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
final class CommandUtils {
    static CompletableFuture<Suggestions> suggestColors(@NonNull CommandContext<CommandSourceStack> context, @NonNull SuggestionsBuilder builder) {
        String input = builder.getRemaining();
        String last = input
            .substring(input.lastIndexOf(' ') + 1)
            .toLowerCase(Locale.ROOT);

        for (String word : Msg.validColorWords()) {
            if (word.startsWith(last)) {
                builder.suggest(word);
            }
        }

        return builder.buildFuture();
    }

    static CompletableFuture<Suggestions> suggestOwnTeam(
        @NonNull CommandContext<CommandSourceStack> context,
        SuggestionsBuilder builder
    ) {
        PlayerTeam team = ownManagedTeam(context.getSource());
        if (team != null) {
            String id = team.getName();
            String displayName = team.getDisplayName().getString();
            // Only the player's own team is considered, so another team's
            // identical display name does not make this name ambiguous.
            if (!displayName.isBlank() && !displayName.equals(id)) {
                suggestTeamToken(
                    builder,
                    displayName,
                    Msg.tr(TeamcraftTranslations.TEAM_INFO_ID.key())
                        .append(": ")
                        .append(id)
                );
            }
            suggestTeamToken(
                builder,
                id,
                Msg.tr(TeamcraftTranslations.TEAM_INFO_NAME.key())
                    .append(": ")
                    .append(team.getDisplayName())
            );
        }
        return builder.buildFuture();
    }

    /** Resolve membership afresh for both completion and execution; never trust a cached suggestion. */
    static PlayerTeam ownManagedTeam(CommandSourceStack source) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            return null;
        }
        ServerScoreboard board = source.getServer().getScoreboard();
        PlayerTeam team = board.getPlayersTeam(player.getScoreboardName());
        return team != null && TeamAssigner.isManagedTeamId(team.getName()) ? team : null;
    }

    private static void suggestTeamToken(@NonNull SuggestionsBuilder builder, String value, Component tooltip) {
        String escaped = StringArgumentType.escapeIfRequired(value);
        String remaining = builder.getRemainingLowerCase();
        String unquotedRemaining = remaining.startsWith("\"") ?
            remaining.substring(1) :
            remaining;

        if (value.toLowerCase(Locale.ROOT).startsWith(unquotedRemaining)
            || escaped.toLowerCase(Locale.ROOT).startsWith(remaining)) {
            builder.suggest(escaped, tooltip);
        }
    }
}
