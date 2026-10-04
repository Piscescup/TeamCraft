package io.github.piscescup.fabricmc.teamcraft.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.piscescup.fabricmc.teamcraft.permission.TeamPermissionManager;
import io.github.piscescup.fabricmc.teamcraft.team.TeamAssigner;
import io.github.piscescup.fabricmc.teamcraft.team.TeamSessionManager;
import io.github.piscescup.fabricmc.teamcraft.text.Msg;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import static io.github.piscescup.fabricmc.teamcraft.permission.TeamCommandPermission.CLEAR_KEY;
import static io.github.piscescup.fabricmc.teamcraft.permission.TeamCommandPermission.RESET_KEY;

/**
 *
 * @author REN YuanTong
 * @since
 */
final class ClearResetCommands {
    static final LiteralArgumentBuilder<CommandSourceStack> CLEAR_COMMAND = Commands.literal("clear")
        //#if MC >= 12111
        .requires(source -> Commands.hasPermission(TeamPermissionManager.getPermission(CLEAR_KEY).toPermission())
            .test(source))
        //#else
        //$$ .requires(source -> source.hasPermission(TeamPermissionManager.getPermission(CLEAR_KEY).toPermission()))
        //#endif
        .executes(ClearResetCommands::clearTeams);

    public static final LiteralArgumentBuilder<CommandSourceStack> RESET_COMMAND = Commands.literal("reset")
        //#if MC >= 12111
        .requires(source -> Commands.hasPermission(TeamPermissionManager.getPermission(RESET_KEY).toPermission())
            .test(source))
        //#else
        //$$ .requires(source -> source.hasPermission(TeamPermissionManager.getPermission(RESET_KEY).toPermission()))
        //#endif
        .executes(ClearResetCommands::resetAll);

    private static int clearTeams(CommandContext<CommandSourceStack> context) {
        int removed = TeamAssigner.clear(context.getSource().getServer().getScoreboard());
        TeamSessionManager.get().getCreatedTeams().clear();
        CommandSourceStack source = context.getSource();
        if (removed == 0) {
            source.sendSuccess(
                () -> Msg.success(TeamcraftTranslations.CLEAR_NONE.key()),
                false
            );
        }
        else {
            source.sendSuccess(
                () -> Msg.success(TeamcraftTranslations.CLEAR_DONE.key(), removed),
                true
            );
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int resetAll(CommandContext<CommandSourceStack> context) {
        int removed = TeamAssigner.clear(context.getSource().getServer().getScoreboard());
        TeamSessionManager.reset();
        CommandSourceStack source = context.getSource();
        source.sendSuccess(
            () -> Msg.success(TeamcraftTranslations.RESET_DONE.key(), removed),
            true
        );
        return Command.SINGLE_SUCCESS;
    }
}
