package io.github.piscescup.fabricmc.teamcraft.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.piscescup.fabricmc.teamcraft.permission.TeamPermissionManager;
import io.github.piscescup.fabricmc.teamcraft.team.TeamSession;
import io.github.piscescup.fabricmc.teamcraft.team.TeamSessionManager;
import io.github.piscescup.fabricmc.teamcraft.text.Msg;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

import static io.github.piscescup.fabricmc.teamcraft.permission.TeamCommandPermission.INIT_KEY;

/**
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
final class InitCommands {
    private static final String PLAYERS_ARG = "players";
    private static final String PLAYER_ARG = "player";

    static final LiteralArgumentBuilder<CommandSourceStack> INIT_COMMAND = Commands.literal("init")
        //#if MC >= 12111
        .requires(source -> Commands.hasPermission(TeamPermissionManager.getPermission(INIT_KEY).toPermission())
            .test(source))
        //#else
        //$$ .requires(source -> source.hasPermission(TeamPermissionManager.getPermission(INIT_KEY).toPermission()))
        //#endif
        .then( Commands.argument(PLAYERS_ARG, EntityArgument.players())
            .executes(InitCommands::initSet)
        )
        .then( Commands.literal("add")
            .then(Commands.argument(PLAYER_ARG, EntityArgument.player())
                .executes(InitCommands::initAdd)
            )
        )
        .then( Commands.literal("remove")
            .then(Commands.argument(PLAYER_ARG, EntityArgument.player())
                .executes(InitCommands::initRemove)
            )
        )
        .then( Commands.literal("list")
            .executes(InitCommands::initList)
        )
        .then( Commands.literal("clear")
            .executes(InitCommands::initClear)
        );

    // ------------------------------------------------------------------
    // init
    // ------------------------------------------------------------------

    private static int initSet(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        List<String> names = selectedNames(context);
        TeamSession session = TeamSessionManager.get();
        session.getCandidates().clear();
        session.getCandidates().addAll(names);
        CommandSourceStack source = context.getSource();
        source.sendSuccess(
            () -> Msg.success(
                TeamcraftTranslations.INIT_SET.key(),
                names.size(), Msg.joinedLiterals(names)
            ),
            false
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int initAdd(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String name = selectedName(context);
        List<String> candidates = TeamSessionManager.get().getCandidates();
        boolean duplicated;
        if (candidates.contains(name)) {
            duplicated = true;
        } else {
            duplicated = false;
            candidates.add(name);
        }
        CommandSourceStack source = context.getSource();
        source.sendSuccess(() -> duplicated
            ? Msg.success(TeamcraftTranslations.INIT_ADD_DUPLICATED.key(), name, candidates.size())
            : Msg.success(TeamcraftTranslations.INIT_ADD.key(), name, candidates.size()),
            false
        );
        return Command.SINGLE_SUCCESS;
    }

    private static String selectedName(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = EntityArgument.getPlayer(context, "player");
        //#if MC >= 12110
        return player.getGameProfile().name();
        //#else
        //$$ return player.getGameProfile().getName();
        //#endif
    }

    private static int initRemove(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        String name = selectedName(context);
        List<String> candidates = TeamSessionManager.get().getCandidates();
        boolean existed;
        existed = candidates.remove(name);
        CommandSourceStack source = context.getSource();

        source.sendSuccess(() -> existed
            ? Msg.success(TeamcraftTranslations.INIT_REMOVE.key(), name, candidates.size())
            : Msg.success(TeamcraftTranslations.INIT_REMOVE_MISSING.key(), name, candidates.size()),
            false
        );
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
                .append(
                    Msg.row(
                        TeamcraftTranslations.STATUS_CANDIDATES.key(),
                        Msg.tr(
                            TeamcraftTranslations.STATUS_CANDIDATES_VALUE.key(),
                            candidates.size(), Msg.joinedLiterals(candidates)
                        )
                    )
                )
            );
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int initClear(CommandContext<CommandSourceStack> context) {
        TeamSessionManager.get().getCandidates().clear();
        context.getSource().sendSuccess(() -> Msg.success(TeamcraftTranslations.INIT_CLEAR.key()), false);
        return Command.SINGLE_SUCCESS;
    }

    static List<String> selectedNames(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        // String players = StringArgumentType.getString(context, "players").trim();
        Collection<ServerPlayer> players = EntityArgument.getPlayers(context, PLAYERS_ARG);
        if (players.isEmpty()) {
            throw EntityArgument.NO_PLAYERS_FOUND.create();
        }
        LinkedHashSet<String> names = new LinkedHashSet<>();
        CommandSourceStack source = context.getSource();
        players.stream()
            .map(player -> {
                //#if MC >= 12110
                return player.getGameProfile().name();
                //#else
                //$$ return player.getGameProfile().getName();
                //#endif
            })
            .forEach(names::add);

        if (names.isEmpty()) {
            throw EntityArgument.NO_PLAYERS_FOUND.create();
        }
        return new ArrayList<>(names);
    }
}
