package io.github.piscescup.fabricmc.teamcraft;

import net.minecraft.resources.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

/**
 * The constant for the {@code Team Craft} mod.
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class References {
    /**
     * The unique namespace of the {@code Team Craft} mod.
     */
    public static final String MOD_ID = "teamcraft";

    /**
     * The visual name of the {@code Team Craft} mod.
     */
    public static final String MOD_NAME = "Team Craft";

    /**
     * The {@link Logger} of the {@code Team Craft} mod.
     */
    public static final Logger MOD_LOGGER = LogManager.getLogger(MOD_NAME);

    @NonNull
    @Contract("_ -> new")
    public static Identifier fromPath(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
