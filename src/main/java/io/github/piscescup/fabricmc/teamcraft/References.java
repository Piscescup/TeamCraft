package io.github.piscescup.fabricmc.teamcraft;

//#if MC >= 12111
import net.minecraft.resources.Identifier;
//#else
//$$ import net.minecraft.resources.ResourceLocation;
//#endif
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Contract;
//#if MC >= 12111
import org.jspecify.annotations.NonNull;
//#endif

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

    @Contract("_ -> new")
    //#if MC >= 12111
    @NonNull
    //#endif
    //#if MC >= 12111
    public static Identifier fromPath(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
    //#else
    //$$ public static ResourceLocation fromPath(String path) {
    //$$     return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    //$$ }
    //#endif
}
