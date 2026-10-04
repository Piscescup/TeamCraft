package io.github.piscescup.fabricmc.teamcraft.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.github.piscescup.fabricmc.teamcraft.permission.TeamPermissionManager;
import io.github.piscescup.fabricmc.teamcraft.text.Msg;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.MutableComponent;

import static io.github.piscescup.fabricmc.teamcraft.permission.TeamCommandPermission.HELP_KEY;

/**
 *
 * @author REN YuanTong
 * @since
 */
final class HelpCommands {
    static final LiteralArgumentBuilder<CommandSourceStack> HELP_COMMAND = Commands.literal("help")
        //#if MC >= 12111
        .requires(source -> Commands.hasPermission(TeamPermissionManager.getPermission(HELP_KEY).toPermission())
            .test(source))
        //#else
        //$$ .requires(source -> source.hasPermission(TeamPermissionManager.getPermission(HELP_KEY).toPermission()))
        //#endif
        .executes(HelpCommands::help);


    static int help(CommandContext<CommandSourceStack> context) {
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
            .append(Msg.helpLine("/teamcraft manage team <team> color|name|friendlyfire|info", TeamcraftTranslations.HELP_TEAM.key()))
            .append(Msg.helpLine("/teamcraft invite player <player>", TeamcraftTranslations.HELP_INVITE.key()))
            .append(Msg.helpLine("/teamcraft invite accept", TeamcraftTranslations.HELP_INVITE_ACCEPT.key()))
            .append(Msg.helpLine("/teamcraft invite decline", TeamcraftTranslations.HELP_INVITE_DECLINE.key()))
            .append(Msg.helpLine("/teamcraft manage leave", TeamcraftTranslations.HELP_LEAVE_TEAM.key()))
            .append(Msg.helpLine("/teamcraft status", TeamcraftTranslations.HELP_STATUS.key()))
            .append(Msg.helpLine("/teamcraft clear", TeamcraftTranslations.HELP_CLEAR.key()))
            .append(Msg.helpLine("/teamcraft reset", TeamcraftTranslations.HELP_RESET.key()));
        context.getSource().sendSystemMessage(help);
        return Command.SINGLE_SUCCESS;
    }
}
