package io.github.piscescup.fabricmc.teamcraft.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.piscescup.fabricmc.teamcraft.permission.TeamPermissionManager;
import io.github.piscescup.fabricmc.teamcraft.team.*;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import java.util.*;

import static io.github.piscescup.fabricmc.teamcraft.command.BuildCommands.BUILD_COMMAND;
import static io.github.piscescup.fabricmc.teamcraft.command.ClearResetCommands.CLEAR_COMMAND;
import static io.github.piscescup.fabricmc.teamcraft.command.ClearResetCommands.RESET_COMMAND;
import static io.github.piscescup.fabricmc.teamcraft.command.ConfigCommands.CONFIG_COMMANDS;
import static io.github.piscescup.fabricmc.teamcraft.command.HelpCommands.HELP_COMMAND;
import static io.github.piscescup.fabricmc.teamcraft.command.InitCommands.INIT_COMMAND;
import static io.github.piscescup.fabricmc.teamcraft.command.InviteCommands.INVITE_COMMAND;
import static io.github.piscescup.fabricmc.teamcraft.command.PermissionCommands.PERMISSION_COMMAND;
import static io.github.piscescup.fabricmc.teamcraft.command.StatusCommands.STATUS_COMMAND;
import static io.github.piscescup.fabricmc.teamcraft.command.TeamManageCommands.TEAM_MANAGE_COMMAND;
import static io.github.piscescup.fabricmc.teamcraft.permission.TeamCommandPermission.*;

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
public final class TeamcraftCommand {
    public static final LiteralArgumentBuilder<CommandSourceStack> ROOT_COMMAND = Commands.literal("teamcraft")
        //#if MC >= 12111
        .requires(source -> Commands.hasPermission(TeamPermissionManager.getPermission(ROOT_KEY).toPermission())
            .test(source))
        //#else
        //$$ .requires(source -> source.hasPermission(TeamPermissionManager.getPermission(ROOT_KEY).toPermission()))
        //#endif
        .executes(HelpCommands::help);

    private TeamcraftCommand() {}

    /**
     * Registers the command tree. Called once from the mod initializer.
     */
    public static void register(
        CommandDispatcher<CommandSourceStack> dispatcher,
        CommandBuildContext context,
        Commands.CommandSelection selection
    ) {
        dispatcher.register(ROOT_COMMAND
            .then(HELP_COMMAND)
            .then(STATUS_COMMAND)
            .then(INIT_COMMAND)
            .then(INVITE_COMMAND)
            .then(CONFIG_COMMANDS)
            .then(BUILD_COMMAND)
            .then(TEAM_MANAGE_COMMAND)
            .then(CLEAR_COMMAND)
            .then(RESET_COMMAND)
            .then(PERMISSION_COMMAND)
        );
    }
}
