package io.github.piscescup.fabricmc.teamcraft.permission;

import net.minecraft.commands.Commands;
//#if MC >= 12111
import net.minecraft.server.permissions.PermissionCheck;
//#endif
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;


/**
 * @author REN YuanTong
 * @since 1.0.0
 */
public enum Permission
    implements StringRepresentable
{
    LEVEL_ALL("all"),
    LEVEL_MODERATORS("moderators"),
    LEVEL_GAMEMASTERS("gamemasters"),
    LEVEL_ADMINS("admins"),
    LEVEL_OWNERS("owners"),
    ;

    public static final StringRepresentable.EnumCodec<Permission> CODEC = StringRepresentable.fromEnum(Permission::values);

    private static final String PERMISSION_PREFIX = "teamcraft.permission.level.";

    public static Permission fromName(String name) {
        return CODEC.byName(name);
    }

    private final String permission;

    Permission(String permission) {
        this.permission = permission;
    }

    @NonNull
    @Contract(pure = true)
    public String translationKey() {
        return PERMISSION_PREFIX + this.permission;
    }

    @NonNull
    @Override
    public String getSerializedName() {
        return this.permission;
    }

    //#if MC >= 12111
    public PermissionCheck toPermission() {
        //#else
        //$$ public int toPermission() {
        //#endif
        return switch (this) {
            case LEVEL_ALL -> Commands.LEVEL_ALL;
            case LEVEL_MODERATORS -> Commands.LEVEL_MODERATORS;
            case LEVEL_GAMEMASTERS -> Commands.LEVEL_GAMEMASTERS;
            case LEVEL_ADMINS -> Commands.LEVEL_ADMINS;
            case LEVEL_OWNERS -> Commands.LEVEL_OWNERS;
        };
    }
}
