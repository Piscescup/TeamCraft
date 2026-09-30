package io.github.piscescup.fabricmc.teamcraft.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
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
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.TeamArgument;
import net.minecraft.commands.arguments.TeamColorArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerPlayer;
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
 * Requires permission level 2.
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
        .then( Commands.argument("players", EntityArgument.players())
            .executes(TeamcraftCommand::initSet)
        )
        .then( Commands.literal("add")
            .then(Commands.argument("players", EntityArgument.players())
                .executes(TeamcraftCommand::initAdd))
        )
        .then( Commands.literal("remove")
            .then(Commands.argument("players", EntityArgument.players())
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
                    configMode(s, splitMode);
                    return Command.SINGLE_SUCCESS;
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
        MutableComponent help = Msg.panel("title.help")
            .append(Msg.helpLine("/teamcraft init <players...>", "help.init"))
            .append(Msg.helpLine("/teamcraft init add|remove <players...>", "help.init_edit"))
            .append(Msg.helpLine("/teamcraft init list|clear", "help.init_manage"))
            .append(Msg.helpLine("/teamcraft config players-per-team <players>", "help.players_per_team"))
            .append(Msg.helpLine("/teamcraft config team-count <teams>", "help.team_count"))
            .append(Msg.helpLine("/teamcraft config mode <fixed|random>", "help.mode"))
            .append(Msg.helpLine("/teamcraft config friendlyfire <true|false>", "help.friendly_fire"))
            .append(Msg.helpLine("/teamcraft config colors <colors...>", "help.colors"))
            .append(Msg.helpLine("/teamcraft config names <names...>", "help.names"))
            .append(Msg.helpLine("/teamcraft build-teams [colors|names ...]", "help.start"))
            .append(Msg.helpLine("/teamcraft team-manage <team> color|name|friendlyfire|info", "help.team"))
            .append(Msg.helpLine("/teamcraft status", "help.status"))
            .append(Msg.helpLine("/teamcraft clear", "help.clear"))
            .append(Msg.helpLine("/teamcraft reset", "help.reset"));
        context.getSource().sendSystemMessage(help);
        return Command.SINGLE_SUCCESS;
    }

    private static int status(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        TeamSession session = TeamSessionManager.get();

        MutableComponent report = Msg.panel("title.status");
        List<String> candidates = session.getCandidates();
        Component candidateValue = candidates.isEmpty()
            ? Msg.tr("common.empty")
            : Msg.tr("status.candidates_value", candidates.size(), Msg.joinedLiterals(candidates));
        report.append(Msg.row("status.candidates", candidateValue));

        if (session.getTeamCount() != null) {
            report.append(Msg.row("status.split_rule",
                Msg.tr("status.rule_team_count", session.getTeamCount())));
        }
        else {
            report.append(Msg.row("status.split_rule",
                Msg.tr("status.rule_players_per_team", session.getTeamSize())));
        }
        report.append(Msg.row("status.mode", session.getMode().displayName()));
        report.append(Msg.row("status.friendly_fire",
            Msg.tr(session.isFriendlyFire() ? "common.on" : "common.off")));

        List<TeamColor> palette = session.effectiveColors();
        MutableComponent colors = Msg.tr(session.getColors().isEmpty() ? "common.default" : "common.custom")
            .append("  ")
            .append(Msg.joinedColors(palette));
        report.append(Msg.row("status.colors", colors));

        MutableComponent names = session.getNames().isEmpty()
            ? Msg.tr("status.names_auto")
            : Msg.tr("common.custom").append("  ").append(Msg.joinedLiterals(session.getNames()));
        report.append(Msg.row("status.names", names));

        ServerScoreboard board = source.getServer().getScoreboard();
        List<String> managed = managedTeamIds(board);
        if (managed.isEmpty()) {
            report.append(Msg.row("status.created_teams", Msg.tr("common.none")));
        }
        else {
            report.append(Msg.row("status.created_teams", Msg.tr("status.created_count", managed.size())));
            for (String id : managed) {
                PlayerTeam team = board.getPlayerTeam(id);
                if (team == null) {
                    continue;
                }
                report.append(Msg.bullet(Msg.tr("status.created_team", id,
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
        source.sendSuccess(() -> Msg.success("init.set", names.size(), Msg.joinedLiterals(names)), false);
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
            ? Msg.success("init.add_skipped", addedCount, candidates.size(), skippedCount)
            : Msg.success("init.add", addedCount, candidates.size()), false);
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
            ? Msg.success("init.remove_missing", removedCount, candidates.size(), missingCount)
            : Msg.success("init.remove", removedCount, candidates.size()), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int initList(CommandContext<CommandSourceStack> context) {
        List<String> candidates = TeamSessionManager.get().getCandidates();
        CommandSourceStack source = context.getSource();
        if (candidates.isEmpty()) {
            source.sendSystemMessage(Msg.error("init.list_empty"));
        }
        else {
            source.sendSystemMessage(Msg.panel("title.candidates")
                .append(Msg.row("status.candidates",
                    Msg.tr("status.candidates_value", candidates.size(), Msg.joinedLiterals(candidates)))));
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int initClear(CommandContext<CommandSourceStack> context) {
        TeamSessionManager.get().getCandidates().clear();
        context.getSource().sendSuccess(() -> Msg.success("init.clear"), false);
        return Command.SINGLE_SUCCESS;
    }

    // ------------------------------------------------------------------
    // config
    // ------------------------------------------------------------------

    private static int configPlayersPerTeam(CommandContext<CommandSourceStack> context) {
        int players = IntegerArgumentType.getInteger(context, "players");
        TeamSessionManager.get().setTeamSize(players);
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Msg.success("config.players_per_team", players), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int configTeamCount(CommandContext<CommandSourceStack> context) {
        int teams = IntegerArgumentType.getInteger(context, "teams");
        TeamSessionManager.get().setTeamCount(teams);
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Msg.success("config.team_count", teams), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int configMode(CommandContext<CommandSourceStack> context, SplitMode mode) {
        TeamSessionManager.get().setMode(mode);
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Msg.success("config.mode", mode.displayName()), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int configFriendlyFire(CommandContext<CommandSourceStack> context) {
        boolean value = BoolArgumentType.getBool(context, "value");
        TeamSessionManager.get().setFriendlyFire(value);
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Msg.success("config.friendly_fire",
            Msg.tr(value ? "common.on" : "common.off")), false);
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
            return Msg.success("config.colors", Msg.joinedColors(colors));
        }, false);
        return Command.SINGLE_SUCCESS;
    }

    private static int configColorsReset(CommandContext<CommandSourceStack> context) {
        TeamSessionManager.get().setColors(List.of());
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Msg.success("config.colors_reset"), false);
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
        source.sendSuccess(() -> Msg.success("config.names", Msg.joinedLiterals(names)), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int configNamesReset(CommandContext<CommandSourceStack> context) {
        TeamSessionManager.get().setNames(List.of());
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Msg.success("config.names_reset"), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int configReset(CommandContext<CommandSourceStack> context) {
        TeamSessionManager.get().resetConfig();
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Msg.success("config.reset"), false);
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
            source.sendFailure(Msg.error("error.no_candidates"));
            return 0;
        }
        ServerScoreboard board = source.getServer().getScoreboard();
        if (TeamAssigner.existsManagedTeam(board)) {
            source.sendFailure(Msg.error("error.teams_exist"));
            return 0;
        }
        int teamCount = session.resolveTeamCount(candidates.size());
        if (teamCount > candidates.size()) {
            source.sendFailure(Msg.error("error.too_many_teams", teamCount, candidates.size()));
            return 0;
        }

        List<SplitPlan> plans = TeamAssigner.buildPlan(session, colorOverride, nameOverride);
        TeamAssigner.apply(board, plans, session.isFriendlyFire());
        session.getCreatedTeams().clear();
        for (SplitPlan plan : plans) {
            session.getCreatedTeams().add(plan.teamId());
        }

        MutableComponent summary = Msg.panel("title.split_result")
            .append(Msg.row("result.summary", Msg.tr("result.summary_value", plans.size(), candidates.size())));
        for (int i = 0; i < plans.size(); i++) {
            SplitPlan plan = plans.get(i);
            MutableComponent teamName = plan.displayName().copy().withColor(plan.color().textColor());
            summary.append(Msg.bullet(Msg.tr("result.team", i + 1, teamName,
                plan.members().size(), Msg.joinedLiterals(plan.members()))));
        }
        source.sendSuccess(() -> summary, true);

        PlayerList playerList = source.getServer().getPlayerList();
        for (SplitPlan plan : plans) {
            for (String member : plan.members()) {
                ServerPlayer player = playerList.getPlayerByName(member);
                if (player != null) {
                    player.sendSystemMessage(Msg.success("result.assigned",
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
        source.sendSuccess(() -> Msg.success("team.color",
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
            source.sendFailure(Msg.error("error.empty_name"));
            return 0;
        }
        team.setDisplayName(Component.literal(name));
        TeamAssigner.applyPrefix(team);
        source.sendSuccess(() -> Msg.success("team.name", team.getName(), Component.literal(name)), false);
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
        source.sendSuccess(() -> Msg.success("team.friendly_fire", team.getDisplayName(),
            Msg.tr(value ? "common.on" : "common.off")), false);
        return Command.SINGLE_SUCCESS;
    }

    private static int teamInfo(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        PlayerTeam team = TeamArgument.getTeam(context, "team");
        if (!isManaged(team)) {
            source.sendFailure(notManagedMessage(team));
            return 0;
        }
        MutableComponent info = Msg.panel("title.team_info")
            .append(Msg.row("team.info.id", Component.literal(team.getName())))
            .append(Msg.row("team.info.name", team.getDisplayName()))
            .append(Msg.row("team.info.color", Msg.colorName(team.getColor().orElse(TeamColor.WHITE))))
            .append(Msg.row("team.info.friendly_fire",
                Msg.tr(team.isAllowFriendlyFire() ? "common.on" : "common.off")))
            .append(Msg.row("team.info.members", Msg.tr("team.info.members_value",
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
            source.sendSuccess(() -> Msg.success("clear.none"), false);
        }
        else {
            source.sendSuccess(() -> Msg.success("clear.done", removed), true);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int resetAll(CommandContext<CommandSourceStack> context) {
        int removed = TeamAssigner.clear(context.getSource().getServer().getScoreboard());
        TeamSessionManager.reset();
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> Msg.success("reset.done", removed), true);
        return Command.SINGLE_SUCCESS;
    }

    // ------------------------------------------------------------------
    // helpers
    // ------------------------------------------------------------------

    private static List<String> selectedNames(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, "players");
        LinkedHashSet<String> names = new LinkedHashSet<>();
        for (ServerPlayer player : players) {
            names.add(player.getGameProfile().name());
        }
        return new ArrayList<>(names);
    }

    private static boolean isManaged(PlayerTeam team) {
        return team.getName().startsWith(TeamAssigner.TEAM_ID_PREFIX);
    }

    private static Component notManagedMessage(PlayerTeam team) {
        return Msg.error("error.not_managed", team.getName(), TeamAssigner.TEAM_ID_PREFIX);
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
            throw new LocalizedArgumentException("error.invalid_colors",
                String.join(" ", invalid), valid);
        }
        if (colors.isEmpty()) {
            throw new LocalizedArgumentException("error.no_colors", valid);
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
            throw new LocalizedArgumentException("error.no_names");
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
