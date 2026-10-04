package io.github.piscescup.fabricmc.teamcraft.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.piscescup.fabricmc.teamcraft.permission.TeamPermissionManager;
import io.github.piscescup.fabricmc.teamcraft.team.TeamAssigner;
import io.github.piscescup.fabricmc.teamcraft.team.TeamSession;
import io.github.piscescup.fabricmc.teamcraft.team.TeamSessionManager;
import io.github.piscescup.fabricmc.teamcraft.text.Msg;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftColor;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.world.scores.PlayerTeam;

import java.util.ArrayList;
import java.util.List;

import static io.github.piscescup.fabricmc.teamcraft.permission.TeamCommandPermission.STATUS_KEY;

/**
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
final class StatusCommands {
    static final LiteralArgumentBuilder<CommandSourceStack> STATUS_COMMAND = Commands.literal("status")
        //#if MC >= 12111
        .requires(source -> Commands.hasPermission(TeamPermissionManager.getPermission(STATUS_KEY).toPermission())
            .test(source))
        //#else
        //$$ .requires(source -> source.hasPermission(TeamPermissionManager.getPermission(STATUS_KEY).toPermission()))
        //#endif
        .executes(StatusCommands::status);

    static int status(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        TeamSession session = TeamSessionManager.get();

        MutableComponent report = Msg.panel(TeamcraftTranslations.TITLE_STATUS.key());
        List<String> candidates = session.getCandidates();
        Component candidateValue = candidates.isEmpty()
            ? Msg.tr(TeamcraftTranslations.COMMON_EMPTY.key())
            : Msg.tr(
                TeamcraftTranslations.STATUS_CANDIDATES_VALUE.key(),
                candidates.size(), Msg.joinedLiterals(candidates)
            );
        report.append(Msg.row(
            TeamcraftTranslations.STATUS_CANDIDATES.key(),
            candidateValue
        ));

        if (session.getTeamCount() != null) {
            report.append(
                Msg.row(
                    TeamcraftTranslations.STATUS_SPLIT_RULE.key(),
                    Msg.tr(
                        TeamcraftTranslations.STATUS_RULE_TEAM_COUNT.key(),
                        session.getTeamCount()
                    )
                )
            );
        }
        else {
            report.append(
                Msg.row(
                    TeamcraftTranslations.STATUS_SPLIT_RULE.key(),
                    Msg.tr(TeamcraftTranslations.STATUS_RULE_PLAYERS_PER_TEAM.key(), session.getTeamSize())
                )
            );
        }
        report.append(Msg.row(TeamcraftTranslations.STATUS_MODE.key(), session.getMode().displayName()));
        report.append(
            Msg.row(
                TeamcraftTranslations.STATUS_FRIENDLY_FIRE.key(),
                Msg.tr(
                    session.isFriendlyFire() ?
                        TeamcraftTranslations.COMMON_ON.key() :
                        TeamcraftTranslations.COMMON_OFF.key()
                )
            )
        );

        List<TeamcraftColor> palette = session.effectiveColors();
        MutableComponent colors = Msg.tr(session.getColors().isEmpty() ? TeamcraftTranslations.COMMON_DEFAULT.key() : TeamcraftTranslations.COMMON_CUSTOM.key())
            .append("  ")
            .append(Msg.joinedColors(palette));
        report.append(Msg.row(TeamcraftTranslations.STATUS_COLORS.key(), colors));

        MutableComponent names = session.getNames().isEmpty()
            ? Msg.tr(TeamcraftTranslations.STATUS_NAMES_AUTO.key())
            : Msg.tr(TeamcraftTranslations.COMMON_CUSTOM.key()).append("  ").append(Msg.joinedLiterals(session.getNames()));
        report.append(Msg.row(TeamcraftTranslations.STATUS_NAMES.key(), names));

        ServerScoreboard board = source.getServer().getScoreboard();
        List<String> managed = managedTeamIds(board);
        if (managed.isEmpty()) {
            report.append(
                Msg.row(
                    TeamcraftTranslations.STATUS_CREATED_TEAMS.key(),
                    Msg.tr(TeamcraftTranslations.COMMON_NONE.key())
                )
            );
        }
        else {
            report.append(
                Msg.row(
                    TeamcraftTranslations.STATUS_CREATED_TEAMS.key(),
                    Msg.tr(TeamcraftTranslations.STATUS_CREATED_COUNT.key(), managed.size())
                )
            );
            for (String id : managed) {
                PlayerTeam team = board.getPlayerTeam(id);
                if (team == null) {
                    continue;
                }
                report.append(
                    Msg.bullet(
                        Msg.tr(
                            TeamcraftTranslations.STATUS_CREATED_TEAM.key(),
                            id, team.getDisplayName(), team.getPlayers().size()
                        )
                    )
                );
            }
        }
        source.sendSystemMessage(report);
        return Command.SINGLE_SUCCESS;
    }

    private static List<String> managedTeamIds(ServerScoreboard board) {
        List<String> ids = new ArrayList<>();
        for (String id : board.getTeamNames()) {
            if (TeamAssigner.isManagedTeamId(id)) {
                ids.add(id);
            }
        }
        return ids;
    }
}
