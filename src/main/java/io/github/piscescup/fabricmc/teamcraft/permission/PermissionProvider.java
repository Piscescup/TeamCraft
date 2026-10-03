package io.github.piscescup.fabricmc.teamcraft.permission;

/**
 * @author REN YuanTong
 * @since 1.0.0
 */
public interface PermissionProvider {
    /**
     * @return the permission level of the subject
     */
    Permission permission();
}
