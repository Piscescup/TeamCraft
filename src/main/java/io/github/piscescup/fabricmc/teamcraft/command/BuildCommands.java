package io.github.piscescup.fabricmc.teamcraft.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.piscescup.fabricmc.teamcraft.permission.TeamPermissionManager;
import io.github.piscescup.fabricmc.teamcraft.team.SplitPlan;
import io.github.piscescup.fabricmc.teamcraft.team.TeamAssigner;
import io.github.piscescup.fabricmc.teamcraft.team.TeamSession;
import io.github.piscescup.fabricmc.teamcraft.team.TeamSessionManager;
import io.github.piscescup.fabricmc.teamcraft.text.Msg;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftColor;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;

import java.util.List;

import static io.github.piscescup.fabricmc.teamcraft.command.ConfigCommands.parseColorList;
import static io.github.piscescup.fabricmc.teamcraft.command.ConfigCommands.parseNameList;
import static io.github.piscescup.fabricmc.teamcraft.permission.TeamCommandPermission.BUILD_KEY;

/**
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
final class BuildCommands {
    static final LiteralArgumentBuilder<CommandSourceStack> BUILD_COMMAND = Commands.literal("build-teams")
        //#if MC >= 12111
        .requires(source -> Commands.hasPermission(TeamPermissionManager.getPermission(BUILD_KEY).toPermission())
            .test(source))
        //#else
        //$$ .requires(source -> source.hasPermission(TeamPermissionManager.getPermission(BUILD_KEY).toPermission()))
        //#endif
        .executes(ctx -> start(ctx, null, null))
        .then( Commands.literal("colors")
            .then(Commands.argument("colors", StringArgumentType.greedyString())
                .suggests(CommandUtils::suggestColors)
                .executes(BuildCommands::startColors))
        )
        .then( Commands.literal("names")
            .then(Commands.argument("names", StringArgumentType.greedyString())
                .executes(BuildCommands::startNames))
        );


    private static int startColors(CommandContext<CommandSourceStack> context) {
        List<TeamcraftColor> colors;
        try {
            colors = parseColorList(StringArgumentType.getString(context, "colors"));
        }
        catch (LocalizedArgumentException e) {
            context.getSource().sendFailure(e.component());
            return 0;
        }
        return start(context, colors, null);
    }

    private static int startNames(CommandContext<CommandSourceStack> context) {
        List<String> names;
        try {
            names = parseNameList(StringArgumentType.getString(context, "names"));
        }
        catch (LocalizedArgumentException e) {
            context.getSource().sendFailure(e.component());
            return 0;
        }
        return start(context, null, names);
    }

    private static int start(CommandContext<CommandSourceStack> context, List<TeamcraftColor> colorOverride, List<String> nameOverride) {
        CommandSourceStack source = context.getSource();
        TeamSession session = TeamSessionManager.get();
        List<String> candidates = session.getCandidates();

        if (candidates.isEmpty()) {
            source.sendFailure(
                Msg.error(TeamcraftTranslations.ERROR_NO_CANDIDATES.key())
            );
            return 0;
        }
        ServerScoreboard board = source.getServer().getScoreboard();
        if (TeamAssigner.existsManagedTeam(board)) {
            source.sendFailure(
                Msg.error(TeamcraftTranslations.ERROR_TEAMS_EXIST.key())
            );
            return 0;
        }
        int teamCount = session.resolveTeamCount(candidates.size());
        if (teamCount > candidates.size()) {
            source.sendFailure(
                Msg.error(TeamcraftTranslations.ERROR_TOO_MANY_TEAMS.key(), teamCount, candidates.size())
            );
            return 0;
        }

        List<SplitPlan> plans = TeamAssigner.buildPlan(session, colorOverride, nameOverride);
        TeamAssigner.apply(board, plans, session.isFriendlyFire());
        session.getCreatedTeams().clear();
        for (SplitPlan plan : plans) {
            session.getCreatedTeams().add(plan.visualTeamString());
        }

        MutableComponent summary = Msg.panel(TeamcraftTranslations.TITLE_SPLIT_RESULT.key())
            .append(
                Msg.row(
                    TeamcraftTranslations.RESULT_SUMMARY.key(),
                    Msg.tr(
                        TeamcraftTranslations.RESULT_SUMMARY_VALUE.key(),
                        plans.size(), candidates.size()
                    )
                )
            );
        for (int i = 0; i < plans.size(); i++) {
            SplitPlan plan = plans.get(i);
            MutableComponent teamName = plan.displayName()
                .copy()
                .withColor(plan.color().textColor());

            summary.append(
                Msg.bullet(
                    Msg.tr(
                        TeamcraftTranslations.RESULT_TEAM.key(),
                        i + 1, teamName, plan.members().size(),
                        Msg.joinedLiterals(plan.members())
                    )
                )
            );
        }
        source.sendSuccess(() -> summary, true);

        PlayerList playerList = source.getServer().getPlayerList();
        for (SplitPlan plan : plans) {
            for (String member : plan.members()) {
                ServerPlayer player = playerList.getPlayerByName(member);
                if (player != null) {
                    player.sendSystemMessage(
                        Msg.success(
                            TeamcraftTranslations.RESULT_ASSIGNED.key(),
                            plan.displayName().copy()
                                .withColor(plan.color().textColor())
                        )
                    );
                }
            }
        }
        return Command.SINGLE_SUCCESS;
    }
}
