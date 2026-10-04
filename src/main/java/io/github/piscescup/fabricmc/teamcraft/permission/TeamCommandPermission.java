package io.github.piscescup.fabricmc.teamcraft.permission;


import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Map;

/**
 * @author REN YuanTong
 * @since 1.0.0
 */
public class TeamCommandPermission {

    public static final String ROOT_KEY = "root";
    public static final String INIT_KEY = "init";
    public static final String CONFIG_KEY = "config";
    public static final String BUILD_KEY = "build";
    public static final String MANAGE_KEY = "manage";
    public static final String CLEAR_KEY = "clear";
    public static final String RESET_KEY = "reset";
    public static final String STATUS_KEY = "status";
    public static final String HELP_KEY = "help";
    public static final String PERMISSION_KEY = "permission";
    public static final String INVITE_KEY = "invite";
    public static final String LEAVE_TEAM_KEY = "leaveteam";

    public static final TeamCommandPermission DEFAULT_PERMISSION_CONFIG = Builder.defaultConfig()
        .build();

    private final Map<String, Permission> commandPermissions;

    public static final List<String> KEYS = List.of(
        ROOT_KEY,
        INIT_KEY,
        CONFIG_KEY,
        BUILD_KEY,
        MANAGE_KEY,
        CLEAR_KEY,
        RESET_KEY,
        STATUS_KEY,
        HELP_KEY,
        PERMISSION_KEY,
        INVITE_KEY,
        LEAVE_TEAM_KEY
    );

    @Contract(pure = true)
    private TeamCommandPermission(@NonNull Builder builder) {
        this.commandPermissions = builder.commandPermissions;
    }

    public Permission getPermission(String key) {
        return this.commandPermissions
            .getOrDefault(key, Permission.LEVEL_ALL);
    }

    public Permission updatePermission(String key, Permission newPermission) {
        return this.commandPermissions.put(key, newPermission);
    }

    public static
    class Builder {
        private final Map<String, Permission> commandPermissions;

        private Builder() {
            this.commandPermissions = new java.util.HashMap<>();
        }

        @NonNull
        @Contract(value = " -> new", pure = true)
        public static Builder create() {
            return new Builder();
        }

        public static Builder defaultConfig() {
            return create()
                .root(Permission.LEVEL_ALL)
                .init(Permission.LEVEL_ALL)
                .config(Permission.LEVEL_ALL)
                .buildTeam(Permission.LEVEL_ALL)
                .manage(Permission.LEVEL_GAMEMASTERS)
                .clear(Permission.LEVEL_GAMEMASTERS)
                .reset(Permission.LEVEL_GAMEMASTERS)
                .status(Permission.LEVEL_ALL)
                .help(Permission.LEVEL_ALL)
                .invite(Permission.LEVEL_ALL)
                .leaveTeam(Permission.LEVEL_ALL)
                .permission(Permission.LEVEL_GAMEMASTERS);
        }

        public Builder setPermission(String commandKey, Permission permission) {
            this.commandPermissions.put(commandKey, permission);
            return this;
        }

        public Builder root(Permission permission) {
            return setPermission(ROOT_KEY, permission);
        }

        public Builder init(Permission permission) {
            return setPermission(INIT_KEY, permission);
        }

        public Builder config(Permission permission) {
            return setPermission(CONFIG_KEY, permission);
        }

        public Builder buildTeam(Permission permission) {
            return setPermission(BUILD_KEY, permission);
        }

        public Builder manage(Permission permission) {
            return setPermission(MANAGE_KEY, permission);
        }

        public Builder clear(Permission permission) {
            return setPermission(CLEAR_KEY, permission);
        }

        public Builder reset(Permission permission) {
            return setPermission(RESET_KEY, permission);
        }

        public Builder status(Permission permission) {
            return setPermission(STATUS_KEY, permission);
        }

        public Builder help(Permission permission) {
            return setPermission(HELP_KEY, permission);
        }

        public Builder permission(Permission permission) {
            return setPermission(PERMISSION_KEY, permission);
        }

        public Builder invite(Permission permission) {
            return setPermission(INVITE_KEY, permission);
        }

        public Builder leaveTeam(Permission permission) {
            return setPermission(LEAVE_TEAM_KEY, permission);
        }

        public TeamCommandPermission build() {
            return new TeamCommandPermission(this);
        }
    }
}
