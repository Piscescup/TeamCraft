package io.github.piscescup.fabricmc.teamcraft.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.piscescup.fabricmc.teamcraft.permission.TeamPermissionManager;
import io.github.piscescup.fabricmc.teamcraft.team.TeamInvitationService;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import static io.github.piscescup.fabricmc.teamcraft.permission.TeamCommandPermission.INVITE_KEY;

/** Player command entry points; invitation rules are shared with the GUI. */
final class InviteCommands {
    static final LiteralArgumentBuilder<CommandSourceStack> INVITE_COMMAND = Commands.literal("invite")
        .requires(source -> source.getEntity() instanceof ServerPlayer)
        .then(Commands.literal("player")
            //#if MC >= 12111
            .requires(source -> Commands.hasPermission(TeamPermissionManager.getPermission(INVITE_KEY).toPermission())
                .test(source))
            //#else
            //$$ .requires(source -> source.hasPermission(TeamPermissionManager.getPermission(INVITE_KEY).toPermission()))
            //#endif
            .then(Commands.argument("player", EntityArgument.player()).executes(InviteCommands::invitePlayer)))
        .then(Commands.literal("accept").executes(InviteCommands::acceptInvite))
        .then(Commands.literal("decline").executes(InviteCommands::declineInvite));

    private static int invitePlayer(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        return feedback(source, TeamInvitationService.send(source.getServer(),
            source.getPlayerOrException(), EntityArgument.getPlayer(context, "player")));
    }

    private static int acceptInvite(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        return feedback(source, TeamInvitationService.accept(source.getServer(), source.getPlayerOrException(), null));
    }

    private static int declineInvite(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        return feedback(source, TeamInvitationService.decline(source.getServer(), source.getPlayerOrException(), null));
    }

    private static int feedback(CommandSourceStack source, TeamInvitationService.Result result) {
        if (result.status() != TeamInvitationService.Status.SUCCESS) {
            source.sendFailure(result.commandMessage());
            return 0;
        }
        source.sendSuccess(result::commandMessage, false);
        return Command.SINGLE_SUCCESS;
    }
}
