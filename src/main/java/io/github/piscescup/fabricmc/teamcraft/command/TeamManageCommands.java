package io.github.piscescup.fabricmc.teamcraft.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.piscescup.fabricmc.teamcraft.permission.TeamPermissionManager;
import io.github.piscescup.fabricmc.teamcraft.team.TeamAssigner;
import io.github.piscescup.fabricmc.teamcraft.team.TeamMembershipService;
import io.github.piscescup.fabricmc.teamcraft.text.Msg;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftColor;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
//#if MC >= 260200
import net.minecraft.commands.arguments.TeamColorArgument;
//#else
//$$ import net.minecraft.commands.arguments.ColorArgument;
//#endif
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.scores.PlayerTeam;


import static io.github.piscescup.fabricmc.teamcraft.permission.TeamCommandPermission.MANAGE_KEY;

/**
 *
 * @author REN YuanTong
 * @since
 */
final class TeamManageCommands {
    static final LiteralArgumentBuilder<CommandSourceStack> TEAM_MANAGE_COMMAND = Commands.literal("manage")
        //#if MC >= 12111
        .requires(source -> Commands.hasPermission(TeamPermissionManager.getPermission(MANAGE_KEY).toPermission())
            .test(source))
        //#else
        //$$ .requires(source -> source.hasPermission(TeamPermissionManager.getPermission(MANAGE_KEY).toPermission()))
        //#endif
        .then( Commands.literal("team")
            .then(Commands.argument("team", StringArgumentType.string())
                .suggests(CommandUtils::suggestOwnTeam)
                .then(Commands.literal("color")
                    .then(Commands.argument("color",
                            //#if MC >= 260200
                            TeamColorArgument.teamColor()
                            //#else
                            //$$ ColorArgument.color()
                            //#endif
                        )
                        .executes(TeamManageCommands::teamColor))
                )
                .then(Commands.literal("name")
                    .then(Commands.argument("name", StringArgumentType.string())
                        .executes(TeamManageCommands::teamName))
                )
                .then(Commands.literal("friendlyfire")
                    .then(Commands.argument("value", BoolArgumentType.bool())
                        .executes(TeamManageCommands::teamFriendlyFire))
                )
                .then(Commands.literal("info")
                    .executes(TeamManageCommands::teamInfo)
                )
            )
        )
        .then( Commands.literal("leave")
            .executes(TeamManageCommands::teamLeave)
        );

    private static int teamLeave(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        TeamMembershipService.Result result = TeamMembershipService.leave(source.getPlayerOrException(), null);
        if (result.outcome() != TeamMembershipService.Outcome.LEFT) {
            source.sendFailure(result.commandMessage());
            return 0;
        }
        source.sendSuccess(result::commandMessage, false);
        return Command.SINGLE_SUCCESS;
    }

    private static int teamColor(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        PlayerTeam team = resolveManagedTeam(context);
        if (team == null) {
            return 0;
        }
        //#if MC >= 260200
        TeamcraftColor color = TeamcraftColor.of(TeamColorArgument.getTeamColor(context, "color"));
        //#else
        //$$ TeamcraftColor color = TeamcraftColor.of(ColorArgument.getColor(context, "color"));
        //#endif
        color.applyTo(team);
        TeamAssigner.applyPrefix(team);
        source.sendSuccess(
            () -> Msg.success(
                TeamcraftTranslations.TEAM_COLOR.key(),
                team.getDisplayName().copy().withColor(color.textColor()), Msg.colorName(color)
            ),
            false
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int teamName(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        PlayerTeam team = resolveManagedTeam(context);
        if (team == null) {
            return 0;
        }
        String name = StringArgumentType.getString(context, "name");
        if (name.isBlank()) {
            source.sendFailure(Msg.error(TeamcraftTranslations.ERROR_EMPTY_NAME.key()));
            return 0;
        }
        team.setDisplayName(Component.literal(name));
        TeamAssigner.applyPrefix(team);
        source.sendSuccess(
            () -> Msg.success(
                TeamcraftTranslations.TEAM_NAME.key(),
                team.getName(), Component.literal(name)
            ),
            false
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int teamFriendlyFire(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        PlayerTeam team = resolveManagedTeam(context);
        if (team == null) {
            return 0;
        }
        boolean value = BoolArgumentType.getBool(context, "value");
        team.setAllowFriendlyFire(value);
        source.sendSuccess(
            () -> Msg.success(TeamcraftTranslations.TEAM_FRIENDLY_FIRE.key(), team.getDisplayName(),
                Msg.tr(value ?
                    TeamcraftTranslations.COMMON_ON.key() :
                    TeamcraftTranslations.COMMON_OFF.key()
                )
            ),
            false
        );
        return Command.SINGLE_SUCCESS;
    }

    private static int teamInfo(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        PlayerTeam team = resolveManagedTeam(context);
        if (team == null) {
            return 0;
        }
        MutableComponent info = Msg.panel(TeamcraftTranslations.TITLE_TEAM_INFO.key())
            .append(Msg.row(TeamcraftTranslations.TEAM_INFO_ID.key(), Component.literal(team.getName())))
            .append(Msg.row(TeamcraftTranslations.TEAM_INFO_NAME.key(), team.getDisplayName()))
            .append(Msg.row(TeamcraftTranslations.TEAM_INFO_COLOR.key(), Msg.colorName(TeamcraftColor.ofTeam(team))))
            .append(
                Msg.row(
                    TeamcraftTranslations.TEAM_INFO_FRIENDLY_FIRE.key(),
                    Msg.tr(team.isAllowFriendlyFire() ?
                        TeamcraftTranslations.COMMON_ON.key() :
                        TeamcraftTranslations.COMMON_OFF.key()
                    )
                )
            )
            .append(
                Msg.row(
                    TeamcraftTranslations.TEAM_INFO_MEMBERS.key(),
                    Msg.tr(
                        TeamcraftTranslations.TEAM_INFO_MEMBERS_VALUE.key(),
                        team.getPlayers().size(), Msg.joinedLiterals(team.getPlayers())
                    )
                )
            );
        source.sendSystemMessage(info);
        return Command.SINGLE_SUCCESS;
    }

    private static PlayerTeam resolveManagedTeam(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        source.getPlayerOrException();
        PlayerTeam own = CommandUtils.ownManagedTeam(source);
        if (own == null) {
            source.sendFailure(Msg.error(TeamcraftTranslations.ERROR_NOT_IN_TEAM.key()));
            return null;
        }
        String requested = StringArgumentType.getString(context, "team");
        if (own.getName().equals(requested) || own.getDisplayName().getString().equalsIgnoreCase(requested)) {
            return own;
        }
        source.sendFailure(Msg.error(TeamcraftTranslations.ERROR_NOT_OWN_TEAM.key()));
        return null;
    }

}
