package io.github.piscescup.fabricmc.teamcraft.text;

import java.util.Optional;

//#if MC >= 260200
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.TeamColor;
//#else
//$$ import net.minecraft.ChatFormatting;
//$$ import net.minecraft.world.scores.PlayerTeam;
//#endif

/**
 * Version-independent mirror of the scoreboard color palette. Minecraft moved
 * team colors from {@code ChatFormatting} to the dedicated {@code TeamColor}
 * class in 26.2; this enum keeps the rest of the mod on one API surface.
 *
 * <p>The constant names match both vanilla types, and the wire format is the
 * serialized name, so payloads stay byte-compatible across versions.</p>
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public enum TeamcraftColor
{
    BLACK,
    DARK_BLUE,
    DARK_GREEN,
    DARK_AQUA,
    DARK_RED,
    DARK_PURPLE,
    GOLD,
    GRAY,
    DARK_GRAY,
    BLUE,
    GREEN,
    AQUA,
    RED,
    LIGHT_PURPLE,
    YELLOW,
    WHITE;

    //#if MC >= 260200
    private final TeamColor vanilla = TeamColor.valueOf(this.name());

    /**
     * @return the matching vanilla {@link TeamColor} constant
     */
    public TeamColor vanilla() {
        return this.vanilla;
    }

    /**
     * @param color a vanilla team color
     * @return the TeamCraft constant with the same name
     */
    public static TeamcraftColor of(TeamColor color) {
        return valueOf(color.name());
    }

    /**
     * Reads the team's current color, defaulting to white like vanilla display.
     */
    public static TeamcraftColor ofTeam(PlayerTeam team) {
        return of(team.getColor().orElse(TeamColor.WHITE));
    }

    /**
     * Applies this color to the scoreboard team.
     */
    public void applyTo(PlayerTeam team) {
        team.setColor(Optional.of(this.vanilla));
    }

    /**
     * @param name a serialized color name, e.g. {@code dark_aqua}
     * @return the constant, or {@code null} when unknown
     */
    public static TeamcraftColor byName(String name) {
        TeamColor color = TeamColor.byName(name);
        return color == null ? null : of(color);
    }

    /**
     * @return the vanilla text color for glyphs, for {@link net.minecraft.network.chat.MutableComponent#withColor}
     */
    public TextColor textColor() {
        return this.vanilla.textColor();
    }

    /**
     * @return the base color value, e.g. for swatch rectangles
     */
    public int rgb() {
        return this.vanilla.rgb();
    }
    //#else
    //$$ private final ChatFormatting vanilla = ChatFormatting.valueOf(this.name());
    //$$
    //$$ /**
    //$$  * @return the matching vanilla {@code ChatFormatting} constant
    //$$  */
    //$$ public ChatFormatting vanilla() {
    //$$     return this.vanilla;
    //$$ }
    //$$
    //$$ /**
    //$$  * @param color a vanilla formatting color
    //$$  * @return the TeamCraft constant with the same name
    //$$  */
    //$$ public static TeamcraftColor of(ChatFormatting color) {
    //$$     return valueOf(color.name());
    //$$ }
    //$$
    //$$ /**
    //$$  * Reads the team's current color, defaulting to white like vanilla display.
    //$$  */
    //$$ public static TeamcraftColor ofTeam(PlayerTeam team) {
    //$$     return of(team.getColor());
    //$$ }
    //$$
    //$$ /**
    //$$  * Applies this color to the scoreboard team.
    //$$  */
    //$$ public void applyTo(PlayerTeam team) {
    //$$     team.setColor(this.vanilla);
    //$$ }
    //$$
    //$$ /**
    //$$  * @param name a serialized color name, e.g. {@code dark_aqua}
    //$$  * @return the constant, or {@code null} when unknown
    //$$  */
    //$$ public static TeamcraftColor byName(String name) {
    //$$     ChatFormatting color = ChatFormatting.getByName(name);
    //$$     return color == null ? null : of(color);
    //$$ }
    //$$
    //$$ /**
    //$$  * @return the color used for text glyphs, as an ARGB-ish int
    //$$  */
    //$$ public int textColor() {
    //$$     return this.vanilla.getColor();
    //$$ }
    //$$
    //$$ /**
    //$$  * @return the base color value, e.g. for swatch rectangles
    //$$  */
    //$$ public int rgb() {
    //$$     return this.vanilla.getColor();
    //$$ }
    //#endif

    /**
     * @return the lowercase command-line word for this color
     */
    public String word() {
        return this.vanilla.getSerializedName();
    }
}
