package io.github.piscescup.fabricmc.teamcraft.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.piscescup.fabricmc.teamcraft.team.SplitMode;
import io.github.piscescup.fabricmc.teamcraft.team.TeamAssigner;
import io.github.piscescup.fabricmc.teamcraft.team.TeamAssigner.SplitPlan;
import io.github.piscescup.fabricmc.teamcraft.team.TeamSession;
import io.github.piscescup.fabricmc.teamcraft.team.TeamSessionManager;
import io.github.piscescup.fabricmc.teamcraft.text.Msg;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.TeamArgument;
import net.minecraft.commands.arguments.TeamColorArgument;
import net.minecraft.commands.arguments.selector.EntitySelector;
import net.minecraft.commands.arguments.selector.EntitySelectorParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.permissions.PermissionSet;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.TeamColor;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The {@code /teamcraft} command tree: pick candidates ({@code init}), configure
 * the split ({@code config}), create the teams ({@code start}), adjust them
 * ({@code team}) and tear everything down ({@code clear}/{@code reset}).
 * The root command is available to players; entity selectors are elevated only
 * while resolving TeamCraft's candidate-list arguments.
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class TeamcraftCommand
{
    /**
     * Matches one team-name token: a quoted section (spaces allowed) or a bare word.
     */
    private static final Pattern NAME_TOKEN = Pattern.compile("\"([^\"]*)\"|(\\S+)");

    public static final LiteralArgumentBuilder<CommandSourceStack> INIT_COMMAND = Commands.literal("init")
        .then( Commands.argument("players", StringArgumentType.greedyString())
            .suggests(TeamcraftCommand::suggestPlayers)
            .executes(TeamcraftCommand::initSet)
        )
        .then( Commands.literal("add")
            .then(Commands.argument("players", StringArgumentType.greedyString())
                .suggests(TeamcraftCommand::suggestPlayers)
                .executes(TeamcraftCommand::initAdd))
        )
        .then( Commands.literal("remove")
            .then(Commands.argument("players", StringArgumentType.greedyString())
                .suggests(TeamcraftCommand::suggestPlayers)
                .executes(TeamcraftCommand::initRemove))
        )
        .then( Commands.literal("list")
            .executes(TeamcraftCommand::initList)
        )
        .then( Commands.literal("clear")
            .executes(TeamcraftCommand::initClear)
        );

    public static final LiteralArgumentBuilder<CommandSourceStack> CONFIG_COMMANDS = Commands.literal("config")
        .then(Commands.literal("players-per-team")
            .then(Commands.argument("players", IntegerArgumentType.integer(1, 1000))
                .executes(TeamcraftCommand::configPlayersPerTeam))
        )
        .then(Commands.literal("team-count")
            .then(Commands.argument("teams", IntegerArgumentType.integer(1, 100))
                .executes(TeamcraftCommand::configTeamCount))
        )
        .then(Commands.literal("mode")
            .then( Commands.argument("split_mode", StringArgumentType.word())
                .suggests( (cxt, builder) ->{
                    Arrays.stream(SplitMode.values())
                        .map(SplitMode::getSerializedName)
                        .forEach(builder::suggest);
                    return builder.buildFuture();
                })
                .executes(s -> {
                    String mode = StringArgumentType.getString(s, "split_mode");
                    SplitMode splitMode = SplitMode.fromName(mode);
                    return configMode(s, splitMode);
                })
            )
        )
        .then(Commands.literal("friendlyfire")
            .then(Commands.argument("value", BoolArgumentType.bool())
                .executes(TeamcraftCommand::configFriendlyFire))

        )
        .then(Commands.literal("colors")
            .then(Commands.literal("reset")
                .executes(TeamcraftCommand::configColorsReset))
            .then(Commands.argument("colors", StringArgumentType.greedyString())
                .suggests(TeamcraftCommand::suggestColors)
                .executes(TeamcraftCommand::configColors))
        )
        .then(Commands.literal("names")
            .then(Commands.literal("reset")
                .executes(TeamcraftCommand::configNamesReset))
            .then(Commands.argument("names", StringArgumentType.greedyString())
                .executes(TeamcraftCommand::configNames))
        )
        .then(Commands.literal("reset")
            .executes(TeamcraftCommand::configReset)
        );

    public static final LiteralArgumentBuilder<CommandSourceStack> BUILD_COMMAND = Commands.literal("build-teams")
        .executes(ctx -> start(ctx, null, null))
        .then( Commands.literal("colors")
            .then(Commands.argument("colors", StringArgumentType.greedyString())
                .suggests(TeamcraftCommand::suggestColors)
                .executes(TeamcraftCommand::startColors))
        )
        .then( Commands.literal("names")
            .then(Commands.argument("names", StringArgumentType.greedyString())
                .executes(TeamcraftCommand::startNames))
        );

    public static final LiteralArgumentBuilder<CommandSourceStack> TEAM_MANAGE_COMMAND = Commands.literal("team-manage")
        .then( Commands.argument("team", TeamArgument.team())
            .then(Commands.literal("color")
                .then(Commands.argument("color", TeamColorArgument.teamColor())
                    .executes(TeamcraftCommand::teamColor))
            )
            .then(Commands.literal("name")
                .then(Commands.argument("name", StringArgumentType.string())
                    .executes(TeamcraftCommand::teamName))
            )
            .then(Commands.literal("friendlyfire")
                .then(Commands.argument("value", BoolArgumentType.bool())
                    .executes(TeamcraftCommand::teamFriendlyFire))
            )
            .then(Commands.literal("info")
                .executes(TeamcraftCommand::teamInfo)
            )
        );

    private TeamcraftCommand() {
    }

    /**
     * Registers the command tree. Called once from the mod initializer.
     */
    public static void register(
        CommandDispatcher<CommandSourceStack> dispatcher,
        CommandBuildContext context,
        Commands.CommandSelection selection
    ) {
        dispatcher.register(Commands.literal("teamcraft")
            .requires(Commands.hasPermission(Commands.LEVEL_ALL))
            .executes(TeamcraftCommand::help)
            .then(Commands.literal("help").executes(TeamcraftCommand::help))
            .then(Commands.literal("status").executes(TeamcraftCommand::status))
            .then(INIT_COMMAND)
            .then(CONFIG_COMMANDS)
            .then(BUILD_COMMAND)
            .then(TEAM_MANAGE_COMMAND)
            .then(Commands.literal("clear").executes(TeamcraftCommand::clearTeams))
            .then(Commands.literal("reset").executes(TeamcraftCommand::resetAll)));
    }

    // ------------------------------------------------------------------
    // help / status
    // ------------------------------------------------------------------

    private static int help(CommandContext<CommandSourceStack> context) {
        MutableComponent help = Msg.panel(TeamcraftTranslations.TITLE_HELP.key())
            .append(Msg.helpLine("/teamcraft init <players...>", TeamcraftTranslations.HELP_INIT.key()))
            .append(Msg.helpLine("/teamcraft init add|remove <players...>", TeamcraftTranslations.HELP_INIT_EDIT.key()))
            .append(Msg.helpLine("/teamcraft init list|clear", TeamcraftTranslations.HELP_INIT_MANAGE.key()))
            .append(Msg.helpLine("/teamcraft config players-per-team <players>", TeamcraftTranslations.HELP_PLAYERS_PER_TEAM.key()))
            .append(Msg.helpLine("/teamcraft config team-count <teams>", TeamcraftTranslations.HELP_TEAM_COUNT.key()))
            .append(Msg.helpLine("/teamcraft config mode <fixed|random>", TeamcraftTranslations.HELP_MODE.key()))
            .append(Msg.helpLine("/teamcraft config friendlyfire <true|false>", TeamcraftTranslations.HELP_FRIENDLY_FIRE.key()))
            .append(Msg.helpLine("/teamcraft config colors <colors...>", TeamcraftTranslations.HELP_COLORS.key()))
            .append(Msg.helpLine("/teamcraft config names <names...>", TeamcraftTranslations.HELP_NAMES.key()))
            .append(Msg.helpLine("/teamcraft build-teams [colors|names ...]", TeamcraftTranslations.HELP_START.key()))
            .append(Msg.helpLine("/teamcraft team-manage <team> color|name|friendlyfire|info", TeamcraftTranslations.HELP_TEAM.key()))
            .append(Msg.helpLine("/teamcraft status", TeamcraftTranslations.HELP_STATUS.key()))
            .append(Msg.helpLine("/teamcraft clear", TeamcraftTranslations.HELP_CLEAR.key()))
            .append(Msg.helpLine("/teamcraft reset", TeamcraftTranslations.HELP_RESET.key()));
        context.getSource().sendSystemMessage(help);
        return Command.SINGLE_SUCCESS;
    }

    private static int status(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        TeamSession session = TeamSessionManager.get();

        MutableComponent report = Msg.panel(TeamcraftTranslations.TITLE_STATUS.key());
        List<String> candidates = session.getCandidates();
        Component candidateValue = candidates.isEmpty()
            ? Msg.tr(TeamcraftTranslations.COMMON_EMPTY.key())
            : Msg.tr(TeamcraftTranslations.STATUS_CANDIDATES_VALUE.key(), candidates.size(), Msg.joinedLiterals(candidates));
        report.append(Msg.row(TeamcraftTranslations.STATUS_CANDIDATES.key(), candidateValue));

        if (session.getTeamCount() != null) {
            report.append(Msg.row(TeamcraftTranslations.STATUS_SPLIT_RULE.key(),
                Msg.tr(TeamcraftTranslations.STATUS_RULE_TEAM_COUNT.key(), session.getTeamCount())));
        }
        else {
            report.append(Msg.row(TeamcraftTranslations.STATUS_SPLIT_RULE.key(),
                Msg.tr(TeamcraftTranslations.STATUS_RULE_PLAYERS_PER_TEAM.key(), session.getTeamSize())));
        }
        report.append(Msg.row(TeamcraftTranslations.STATUS_MODE.key(), session.getMode().displayName()));
        report.append(Msg.row(TeamcraftTranslations.STATUS_FRIENDLY_FIRE.key(),
            Msg.tr(session.isFriendlyFire() ? TeamcraftTranslations.COMMON_ON.key() : TeamcraftTranslations.COMMON_OFF.key())));

        List<TeamColor> palette = session.effectiveColors();
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
            report.append(Msg.row(TeamcraftTranslations.STATUS_CREATED_TEAMS.key(), Msg.tr(TeamcraftTranslations.COMMON_NONE.key())));
        }
        else {
            report.append(Msg.row(TeamcraftTranslations.STATUS_CREATED_TEAMS.key(), Msg.tr(TeamcraftTranslations.STATUS_CREATED_COUNT.key(), managed.size())));
            for (String id : managed) {
                PlayerTeam team = board.getPlayerTeam(id);
                if (team == null) {
                    continue;
                }
                report.append(Msg.bullet(Msg.tr(TeamcraftTranslations.STATUS_CREATED_TEAM.key(), id,
                    team.getDisplayName(), team.getPlayers().size())));
            }
        }
        source.sendSystemMessage(report);
        return Command.SINGLE_SUCCESS;
    }

    // ------------------------------------------------------------------
    // init
    // ------------------------------------------------------------------

    private static int initSet(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        List<String> names = selectedNames(context);
        TeamSession session = TeamSessionManager.get();
        session.getCandidates().clear();
        session.getCandidates().addAll(names);
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Msg.success(TeamcraftTranslations.INIT_SET.key(), names.size(), Msg.joinedLiterals(names)), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int initAdd(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        List<String> names = selectedNames(context);
        List<String> candidates = TeamSessionManager.get().getCandidates();
        int added = 0;
        int skipped = 0;
        for (String name : names) {
            if (candidates.contains(name)) {
                skipped++;
            }
            else {
                candidates.add(name);
                added++;
            }
        }
        CommandSourceStack source = context.getSource();
        final int addedCount = added;
        final int skippedCount = skipped;
        source.sendSuccess(() -> skippedCount > 0
            ? Msg.success(TeamcraftTranslations.INIT_ADD_SKIPPED.key(), addedCount, candidates.size(), skippedCount)
            : Msg.success(TeamcraftTranslations.INIT_ADD.key(), addedCount, candidates.size()), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int initRemove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        List<String> names = selectedNames(context);
        List<String> candidates = TeamSessionManager.get().getCandidates();
        int removed = 0;
        int missing = 0;
        for (String name : names) {
            if (candidates.remove(name)) {
                removed++;
            }
            else {
                missing++;
            }
        }
        CommandSourceStack source = context.getSource();
        final int removedCount = removed;
        final int missingCount = missing;
        source.sendSuccess(() -> missingCount > 0
            ? Msg.success(TeamcraftTranslations.INIT_REMOVE_MISSING.key(), removedCount, candidates.size(), missingCount)
            : Msg.success(TeamcraftTranslations.INIT_REMOVE.key(), removedCount, candidates.size()), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int initList(CommandContext<CommandSourceStack> context) {
        List<String> candidates = TeamSessionManager.get().getCandidates();
        CommandSourceStack source = context.getSource();
        if (candidates.isEmpty()) {
            source.sendSystemMessage(Msg.error(TeamcraftTranslations.INIT_LIST_EMPTY.key()));
        }
        else {
            source.sendSystemMessage(Msg.panel(TeamcraftTranslations.TITLE_CANDIDATES.key())
                .append(Msg.row(TeamcraftTranslations.STATUS_CANDIDATES.key(),
                    Msg.tr(TeamcraftTranslations.STATUS_CANDIDATES_VALUE.key(), candidates.size(), Msg.joinedLiterals(candidates)))));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int initClear(CommandContext<CommandSourceStack> context) {
        TeamSessionManager.get().getCandidates().clear();
        context.getSource().sendSuccess(() -> Msg.success(TeamcraftTranslations.INIT_CLEAR.key()), false);
        return Command.SINGLE_SUCCESS;
    }

    // ------------------------------------------------------------------
    // config
    // ------------------------------------------------------------------

    private static int configPlayersPerTeam(CommandContext<CommandSourceStack> context) {
        int players = IntegerArgumentType.getInteger(context, "players");
        TeamSessionManager.get().setTeamSize(players);
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Msg.success(TeamcraftTranslations.CONFIG_PLAYERS_PER_TEAM.key(), players), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int configTeamCount(CommandContext<CommandSourceStack> context) {
        int teams = IntegerArgumentType.getInteger(context, "teams");
        TeamSessionManager.get().setTeamCount(teams);
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Msg.success(TeamcraftTranslations.CONFIG_TEAM_COUNT.key(), teams), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int configMode(CommandContext<CommandSourceStack> context, SplitMode mode) {
        TeamSessionManager.get().setMode(mode);
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Msg.success(TeamcraftTranslations.CONFIG_MODE.key(), mode.displayName()), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int configFriendlyFire(CommandContext<CommandSourceStack> context) {
        boolean value = BoolArgumentType.getBool(context, "value");
        TeamSessionManager.get().setFriendlyFire(value);
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Msg.success(TeamcraftTranslations.CONFIG_FRIENDLY_FIRE.key(),
            Msg.tr(value ? TeamcraftTranslations.COMMON_ON.key() : TeamcraftTranslations.COMMON_OFF.key())), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int configColors(CommandContext<CommandSourceStack> context) {
        String raw = StringArgumentType.getString(context, "colors").trim();
        // the greedy argument can swallow the word meant for the reset literal; accept it here
        if (raw.equalsIgnoreCase("reset")) {
            return configColorsReset(context);
        }
        List<TeamColor> colors;
        try {
            colors = parseColorList(raw);
        }
        catch (LocalizedArgumentException e) {
            context.getSource().sendFailure(e.component());
            return 0;
        }
        TeamSessionManager.get().setColors(colors);
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> {
            return Msg.success(TeamcraftTranslations.CONFIG_COLORS.key(), Msg.joinedColors(colors));
        }, false);
        return Command.SINGLE_SUCCESS;
    }

    private static int configColorsReset(CommandContext<CommandSourceStack> context) {
        TeamSessionManager.get().setColors(List.of());
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Msg.success(TeamcraftTranslations.CONFIG_COLORS_RESET.key()), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int configNames(CommandContext<CommandSourceStack> context) {
        List<String> names;
        try {
            names = parseNameList(StringArgumentType.getString(context, "names"));
        }
        catch (LocalizedArgumentException e) {
            context.getSource().sendFailure(e.component());
            return 0;
        }
        TeamSessionManager.get().setNames(names);
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Msg.success(TeamcraftTranslations.CONFIG_NAMES.key(), Msg.joinedLiterals(names)), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int configNamesReset(CommandContext<CommandSourceStack> context) {
        TeamSessionManager.get().setNames(List.of());
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Msg.success(TeamcraftTranslations.CONFIG_NAMES_RESET.key()), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int configReset(CommandContext<CommandSourceStack> context) {
        TeamSessionManager.get().resetConfig();
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Msg.success(TeamcraftTranslations.CONFIG_RESET.key()), false);
        return Command.SINGLE_SUCCESS;
    }

    // ------------------------------------------------------------------
    // start
    // ------------------------------------------------------------------

    private static int startColors(CommandContext<CommandSourceStack> context) {
        List<TeamColor> colors;
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

    private static int start(CommandContext<CommandSourceStack> context, List<TeamColor> colorOverride, List<String> nameOverride) {
        CommandSourceStack source = context.getSource();
        TeamSession session = TeamSessionManager.get();
        List<String> candidates = session.getCandidates();

        if (candidates.isEmpty()) {
            source.sendFailure(Msg.error(TeamcraftTranslations.ERROR_NO_CANDIDATES.key()));
            return 0;
        }
        ServerScoreboard board = source.getServer().getScoreboard();
        if (TeamAssigner.existsManagedTeam(board)) {
            source.sendFailure(Msg.error(TeamcraftTranslations.ERROR_TEAMS_EXIST.key()));
            return 0;
        }
        int teamCount = session.resolveTeamCount(candidates.size());
        if (teamCount > candidates.size()) {
            source.sendFailure(Msg.error(TeamcraftTranslations.ERROR_TOO_MANY_TEAMS.key(), teamCount, candidates.size()));
            return 0;
        }

        List<SplitPlan> plans = TeamAssigner.buildPlan(session, colorOverride, nameOverride);
        TeamAssigner.apply(board, plans, session.isFriendlyFire());
        session.getCreatedTeams().clear();
        for (SplitPlan plan : plans) {
            session.getCreatedTeams().add(plan.teamId());
        }

        MutableComponent summary = Msg.panel(TeamcraftTranslations.TITLE_SPLIT_RESULT.key())
            .append(Msg.row(TeamcraftTranslations.RESULT_SUMMARY.key(), Msg.tr(TeamcraftTranslations.RESULT_SUMMARY_VALUE.key(), plans.size(), candidates.size())));
        for (int i = 0; i < plans.size(); i++) {
            SplitPlan plan = plans.get(i);
            MutableComponent teamName = plan.displayName().copy().withColor(plan.color().textColor());
            summary.append(Msg.bullet(Msg.tr(TeamcraftTranslations.RESULT_TEAM.key(), i + 1, teamName,
                plan.members().size(), Msg.joinedLiterals(plan.members()))));
        }
        source.sendSuccess(() -> summary, true);

        PlayerList playerList = source.getServer().getPlayerList();
        for (SplitPlan plan : plans) {
            for (String member : plan.members()) {
                ServerPlayer player = playerList.getPlayerByName(member);
                if (player != null) {
                    player.sendSystemMessage(Msg.success(TeamcraftTranslations.RESULT_ASSIGNED.key(),
                        plan.displayName().copy().withColor(plan.color().textColor())));
                }
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    // ------------------------------------------------------------------
    // team
    // ------------------------------------------------------------------

    private static int teamColor(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        PlayerTeam team = TeamArgument.getTeam(context, "team");
        if (!isManaged(team)) {
            source.sendFailure(notManagedMessage(team));
            return 0;
        }
        TeamColor color = TeamColorArgument.getTeamColor(context, "color");
        team.setColor(Optional.of(color));
        TeamAssigner.applyPrefix(team);
        source.sendSuccess(() -> Msg.success(TeamcraftTranslations.TEAM_COLOR.key(),
            team.getDisplayName().copy().withColor(color.textColor()), Msg.colorName(color)), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int teamName(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        PlayerTeam team = TeamArgument.getTeam(context, "team");
        if (!isManaged(team)) {
            source.sendFailure(notManagedMessage(team));
            return 0;
        }
        String name = StringArgumentType.getString(context, "name");
        if (name.isBlank()) {
            source.sendFailure(Msg.error(TeamcraftTranslations.ERROR_EMPTY_NAME.key()));
            return 0;
        }
        team.setDisplayName(Component.literal(name));
        TeamAssigner.applyPrefix(team);
        source.sendSuccess(() -> Msg.success(TeamcraftTranslations.TEAM_NAME.key(), team.getName(), Component.literal(name)), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int teamFriendlyFire(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        PlayerTeam team = TeamArgument.getTeam(context, "team");
        if (!isManaged(team)) {
            source.sendFailure(notManagedMessage(team));
            return 0;
        }
        boolean value = BoolArgumentType.getBool(context, "value");
        team.setAllowFriendlyFire(value);
        source.sendSuccess(() -> Msg.success(TeamcraftTranslations.TEAM_FRIENDLY_FIRE.key(), team.getDisplayName(),
            Msg.tr(value ? TeamcraftTranslations.COMMON_ON.key() : TeamcraftTranslations.COMMON_OFF.key())), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int teamInfo(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        PlayerTeam team = TeamArgument.getTeam(context, "team");
        if (!isManaged(team)) {
            source.sendFailure(notManagedMessage(team));
            return 0;
        }
        MutableComponent info = Msg.panel(TeamcraftTranslations.TITLE_TEAM_INFO.key())
            .append(Msg.row(TeamcraftTranslations.TEAM_INFO_ID.key(), Component.literal(team.getName())))
            .append(Msg.row(TeamcraftTranslations.TEAM_INFO_NAME.key(), team.getDisplayName()))
            .append(Msg.row(TeamcraftTranslations.TEAM_INFO_COLOR.key(), Msg.colorName(team.getColor().orElse(TeamColor.WHITE))))
            .append(Msg.row(TeamcraftTranslations.TEAM_INFO_FRIENDLY_FIRE.key(),
                Msg.tr(team.isAllowFriendlyFire() ? TeamcraftTranslations.COMMON_ON.key() : TeamcraftTranslations.COMMON_OFF.key())))
            .append(Msg.row(TeamcraftTranslations.TEAM_INFO_MEMBERS.key(), Msg.tr(TeamcraftTranslations.TEAM_INFO_MEMBERS_VALUE.key(),
                team.getPlayers().size(), Msg.joinedLiterals(team.getPlayers()))));
        source.sendSystemMessage(info);
        return Command.SINGLE_SUCCESS;
    }

    // ------------------------------------------------------------------
    // clear / reset
    // ------------------------------------------------------------------

    private static int clearTeams(CommandContext<CommandSourceStack> context) {
        int removed = TeamAssigner.clear(context.getSource().getServer().getScoreboard());
        TeamSessionManager.get().getCreatedTeams().clear();
        CommandSourceStack source = context.getSource();
        if (removed == 0) {
            source.sendSuccess(() -> Msg.success(TeamcraftTranslations.CLEAR_NONE.key()), false);
        }
        else {
            source.sendSuccess(() -> Msg.success(TeamcraftTranslations.CLEAR_DONE.key(), removed), true);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int resetAll(CommandContext<CommandSourceStack> context) {
        int removed = TeamAssigner.clear(context.getSource().getServer().getScoreboard());
        TeamSessionManager.reset();
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Msg.success(TeamcraftTranslations.RESET_DONE.key(), removed), true);
        return Command.SINGLE_SUCCESS;
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private static List<String> selectedNames(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String input = StringArgumentType.getString(context, "players").trim();
        if (input.isEmpty()) {
            throw EntityArgument.NO_PLAYERS_FOUND.create();
        }

        LinkedHashSet<String> names = new LinkedHashSet<>();
        CommandSourceStack source = context.getSource();
        if (input.startsWith("@")) {
            EntitySelector selector = new EntitySelectorParser(new StringReader(input), true).parse();
            PermissionSet originalPermissions = source.permissions();
            PermissionSet selectorPermissions = permission ->
                permission == Permissions.COMMANDS_ENTITY_SELECTORS
                    || originalPermissions.hasPermission(permission);
            for (ServerPlayer player : selector.findPlayers(source.withPermission(selectorPermissions))) {
                names.add(player.getGameProfile().name());
            }
        }
        else {
            PlayerList playerList = source.getServer().getPlayerList();
            for (String playerName : input.split("\\s+")) {
                ServerPlayer player = playerList.getPlayerByName(playerName);
                if (player == null) {
                    throw EntityArgument.NO_PLAYERS_FOUND.create();
                }
                names.add(player.getGameProfile().name());
            }
        }

        if (names.isEmpty()) {
            throw EntityArgument.NO_PLAYERS_FOUND.create();
        }
        return new ArrayList<>(names);
    }

    private static CompletableFuture<Suggestions> suggestPlayers(
        CommandContext<CommandSourceStack> context,
        SuggestionsBuilder builder
    ) {
        String remaining = builder.getRemaining();
        int tokenStart = remaining.lastIndexOf(' ') + 1;
        String current = remaining.substring(tokenStart).toLowerCase(Locale.ROOT);
        SuggestionsBuilder tokenBuilder = builder.createOffset(builder.getStart() + tokenStart);

        List<String> suggestions = new ArrayList<>(List.of("@a", "@p", "@r", "@s"));
        suggestions.addAll(context.getSource().getOnlinePlayerNames());
        for (String suggestion : suggestions) {
            if (suggestion.toLowerCase(Locale.ROOT).startsWith(current)) {
                tokenBuilder.suggest(suggestion);
            }
        }
        return tokenBuilder.buildFuture();
    }

    private static boolean isManaged(PlayerTeam team) {
        return team.getName().startsWith(TeamAssigner.TEAM_ID_PREFIX);
    }

    private static Component notManagedMessage(PlayerTeam team) {
        return Msg.error(TeamcraftTranslations.ERROR_NOT_MANAGED.key(), team.getName(), TeamAssigner.TEAM_ID_PREFIX);
    }

    private static List<String> managedTeamIds(ServerScoreboard board) {
        List<String> ids = new ArrayList<>();
        for (String id : board.getTeamNames()) {
            if (id.startsWith(TeamAssigner.TEAM_ID_PREFIX)) {
                ids.add(id);
            }
        }
        return ids;
    }

    private static List<TeamColor> parseColorList(String input) {
        List<TeamColor> colors = new ArrayList<>();
        List<String> invalid = new ArrayList<>();
        for (String word : input.trim().split("\\s+")) {
            TeamColor color = Msg.parseColor(word);
            if (color == null) {
                invalid.add(word);
            }
            else {
                colors.add(color);
            }
        }
        String valid = String.join(" ", Msg.validColorWords());
        if (!invalid.isEmpty()) {
            throw new LocalizedArgumentException(TeamcraftTranslations.ERROR_INVALID_COLORS.key(),
                String.join(" ", invalid), valid);
        }
        if (colors.isEmpty()) {
            throw new LocalizedArgumentException(TeamcraftTranslations.ERROR_NO_COLORS.key(), valid);
        }
        return colors;
    }

    private static List<String> parseNameList(String input) {
        List<String> names = new ArrayList<>();
        Matcher matcher = NAME_TOKEN.matcher(input);
        while (matcher.find()) {
            String token = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            if (!token.isBlank()) {
                names.add(token);
            }
        }
        if (names.isEmpty()) {
            throw new LocalizedArgumentException(TeamcraftTranslations.ERROR_NO_NAMES.key());
        }
        return names;
    }

    private static CompletableFuture<Suggestions> suggestColors(CommandContext<CommandSourceStack> context, SuggestionsBuilder builder) {
        String input = builder.getRemaining();
        String last = input.substring(input.lastIndexOf(' ') + 1).toLowerCase(Locale.ROOT);
        for (String word : Msg.validColorWords()) {
            if (word.startsWith(last)) {
                builder.suggest(word);
            }
        }
        return builder.buildFuture();
    }

    /**
     * Keeps parser failures localizable without leaking already-rendered server
     * strings into player-facing messages.
     */
    private static final class LocalizedArgumentException extends IllegalArgumentException
    {
        private final String key;
        private final Object[] args;

        private LocalizedArgumentException(String key, Object... args) {
            this.key = key;
            this.args = args;
        }

        private Component component() {
            return Msg.error(this.key, this.args);
        }
    }
}
