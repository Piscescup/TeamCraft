package io.github.piscescup.fabricmc.teamcraft.permission;

import java.util.function.Predicate;

/**
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public interface PermissionChecker<S extends PermissionProvider>
    extends Predicate<S>
{
    /**
     * @param subject the subject to check
     * @return {@code true} if the subject has sufficient permission, {@code false} otherwise
     */
    boolean hasPermission(S subject);

    @Override
    default boolean test(S subject) {
        return hasPermission(subject);
    }
}
