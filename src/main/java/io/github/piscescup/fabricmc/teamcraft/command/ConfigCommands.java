package io.github.piscescup.fabricmc.teamcraft.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.piscescup.fabricmc.teamcraft.permission.TeamPermissionManager;
import io.github.piscescup.fabricmc.teamcraft.team.SplitMode;
import io.github.piscescup.fabricmc.teamcraft.team.TeamSessionManager;
import io.github.piscescup.fabricmc.teamcraft.text.Msg;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftColor;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.piscescup.fabricmc.teamcraft.permission.TeamCommandPermission.CONFIG_KEY;

/**
 *
 * @author REN YuanTong
 * @since
 */
final class ConfigCommands {
    private static final Pattern NAME_TOKEN = Pattern.compile("\"([^\"]*)\"|(\\S+)");

    static final LiteralArgumentBuilder<CommandSourceStack> CONFIG_COMMANDS = Commands.literal("config")
        //#if MC >= 12111
        .requires(
            source -> Commands.hasPermission(TeamPermissionManager.getPermission(CONFIG_KEY).toPermission())
                .test(source)
        )
        //#else
        //$$ .requires(source -> source.hasPermission(TeamPermissionManager.getPermission(CONFIG_KEY).toPermission()))
        //#endif
        .then(Commands.literal("players-per-team")
            .then( Commands.argument("players", IntegerArgumentType.integer(1, 1000))
                .executes(ConfigCommands::configPlayersPerTeam)
            )
        )
        .then(Commands.literal("team-count")
            .then(Commands.argument("teams", IntegerArgumentType.integer(1, 100))
                .executes(ConfigCommands::configTeamCount)
            )
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
                .executes(ConfigCommands::configFriendlyFire)
            )
        )
        .then(Commands.literal("colors")
            .then(Commands.literal("reset")
                .executes(ConfigCommands::configColorsReset)
            )
            .then(Commands.argument("colors", StringArgumentType.greedyString())
                .suggests(CommandUtils::suggestColors)
                .executes(ConfigCommands::configColors)
            )
        )
        .then(Commands.literal("names")
            .then(Commands.literal("reset")
                .executes(ConfigCommands::configNamesReset)
            )
            .then(Commands.argument("names", StringArgumentType.greedyString())
                .executes(ConfigCommands::configNames)
            )
        )
        .then(Commands.literal("reset")
            .executes(ConfigCommands::configReset)
        );



    private static int configPlayersPerTeam(CommandContext<CommandSourceStack> context) {
        int players = IntegerArgumentType.getInteger(context, "players");
        TeamSessionManager.get().setTeamSize(players);
        CommandSourceStack source = context.getSource();
        source.sendSuccess(
            () -> Msg.success(TeamcraftTranslations.CONFIG_PLAYERS_PER_TEAM.key(), players),
            false
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int configTeamCount(CommandContext<CommandSourceStack> context) {
        int teams = IntegerArgumentType.getInteger(context, "teams");
        TeamSessionManager.get().setTeamCount(teams);
        CommandSourceStack source = context.getSource();
        source.sendSuccess(
            () -> Msg.success(TeamcraftTranslations.CONFIG_TEAM_COUNT.key(), teams),
            false
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int configMode(CommandContext<CommandSourceStack> context, SplitMode mode) {
        TeamSessionManager.get().setMode(mode);
        CommandSourceStack source = context.getSource();
        source.sendSuccess(
            () -> Msg.success(TeamcraftTranslations.CONFIG_MODE.key(), mode.displayName()),
            false
        );

        return Command.SINGLE_SUCCESS;
    }

    private static int configFriendlyFire(CommandContext<CommandSourceStack> context) {
        boolean value = BoolArgumentType.getBool(context, "value");
        TeamSessionManager.get().setFriendlyFire(value);
        CommandSourceStack source = context.getSource();
        source.sendSuccess(
            () -> Msg.success(
                TeamcraftTranslations.CONFIG_FRIENDLY_FIRE.key(),
                Msg.tr(value ?
                    TeamcraftTranslations.COMMON_ON.key() :
                    TeamcraftTranslations.COMMON_OFF.key()
                )
            ),
            false
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int configColors(CommandContext<CommandSourceStack> context) {
        String raw = StringArgumentType.getString(context, "colors").trim();
        // the greedy argument can swallow the word meant for the reset literal; accept it here
        if (raw.equalsIgnoreCase("reset")) {
            return configColorsReset(context);
        }
        List<TeamcraftColor> colors;
        try {
            colors = parseColorList(raw);
        }
        catch (LocalizedArgumentException e) {
            context.getSource().sendFailure(e.component());
            return 0;
        }
        TeamSessionManager.get().setColors(colors);
        CommandSourceStack source = context.getSource();
        source.sendSuccess(
            () -> Msg.success(
                TeamcraftTranslations.CONFIG_COLORS.key(),
                Msg.joinedColors(colors)
            ),
            false
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int configColorsReset(CommandContext<CommandSourceStack> context) {
        TeamSessionManager.get().setColors(List.of());
        CommandSourceStack source = context.getSource();
        source.sendSuccess(
            () -> Msg.success(TeamcraftTranslations.CONFIG_COLORS_RESET.key()),
            false
        );
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
        source.sendSuccess(
            () -> Msg.success(TeamcraftTranslations.CONFIG_NAMES.key(), Msg.joinedLiterals(names)),
            false
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int configNamesReset(CommandContext<CommandSourceStack> context) {
        TeamSessionManager.get().setNames(List.of());
        CommandSourceStack source = context.getSource();
        source.sendSuccess(
            () -> Msg.success(TeamcraftTranslations.CONFIG_NAMES_RESET.key()),
            false
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int configReset(CommandContext<CommandSourceStack> context) {
        TeamSessionManager.get().resetConfig();
        CommandSourceStack source = context.getSource();
        source.sendSuccess(
            () -> Msg.success(TeamcraftTranslations.CONFIG_RESET.key()),
            false
        );
        return Command.SINGLE_SUCCESS;
    }

    static List<TeamcraftColor> parseColorList(String input) {
        List<TeamcraftColor> colors = new ArrayList<>();
        List<String> invalid = new ArrayList<>();
        for (String word : input.trim().split("\\s+")) {
            TeamcraftColor color = Msg.parseColor(word);
            if (color == null) {
                invalid.add(word);
            }
            else {
                colors.add(color);
            }
        }
        String valid = String.join(" ", Msg.validColorWords());
        if (!invalid.isEmpty()) {
            throw new LocalizedArgumentException(
                TeamcraftTranslations.ERROR_INVALID_COLORS.key(),
                String.join(" ", invalid), valid
            );
        }
        if (colors.isEmpty()) {
            throw new LocalizedArgumentException(
                TeamcraftTranslations.ERROR_NO_COLORS.key(), valid
            );
        }
        return colors;
    }

    static List<String> parseNameList(String input) {
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

}
