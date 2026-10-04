package io.github.piscescup.fabricmc.teamcraft.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.piscescup.fabricmc.teamcraft.permission.Permission;
import io.github.piscescup.fabricmc.teamcraft.permission.TeamPermissionManager;
import io.github.piscescup.fabricmc.teamcraft.text.Msg;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import static io.github.piscescup.fabricmc.teamcraft.permission.TeamCommandPermission.KEYS;
import static io.github.piscescup.fabricmc.teamcraft.permission.TeamCommandPermission.PERMISSION_KEY;

/**
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
final class PermissionCommands {
    static final LiteralArgumentBuilder<CommandSourceStack> PERMISSION_COMMAND;

    static {
        LiteralArgumentBuilder<CommandSourceStack> permission = Commands.literal("permission");

        KEYS.stream()
            .map(PermissionCommands::permissionFor)
            .forEach(permission::then);

        PERMISSION_COMMAND = permission;
    }

    private static LiteralArgumentBuilder<CommandSourceStack> permissionFor(String permissionKey) {
        return Commands.literal(permissionKey)
            .then(Commands.literal("get")
                .executes(context -> {
                    CommandSourceStack source = context.getSource();
                    Permission permission = TeamPermissionManager.getPermission(permissionKey);
                    source.sendSuccess(
                        () -> Msg.success(
                            TeamcraftTranslations.PERMISSION_GET.key(),
                            permissionKey, permission.getSerializedName()
                        ),
                        true
                    );
                    return Command.SINGLE_SUCCESS;
                })
            )
            .then(Commands.literal("set")
                //#if MC >= 12111
                .requires(source -> Commands.hasPermission(TeamPermissionManager.getPermission(PERMISSION_KEY).toPermission())
                    .test(source))
                //#else
                //$$ .requires(source -> source.hasPermission(TeamPermissionManager.getPermission(PERMISSION_KEY).toPermission()))
                //#endif
                .then(Commands.argument("level", StringArgumentType.word())
                    .suggests((context, builder) -> {
                        for (Permission level : Permission.values()) {
                            builder.suggest(level.getSerializedName());
                        }
                        return builder.buildFuture();
                    })
                    .executes(context -> {
                        CommandSourceStack source = context.getSource();
                        String levelStr = StringArgumentType.getString(context, "level");
                        Permission level = Permission.fromName(levelStr);
                        TeamPermissionManager.updatePermission(permissionKey, level);
                        source.getServer().getPlayerList().getPlayers().forEach(
                            player -> source.getServer().getCommands().sendCommands(player)
                        );
                        source.sendSuccess(() -> Msg.success(TeamcraftTranslations.PERMISSION_SET.key(),
                            permissionKey, level.getSerializedName()), true
                        );
                        return Command.SINGLE_SUCCESS;
                    })
                )
            );
    }
}
