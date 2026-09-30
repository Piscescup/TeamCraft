package io.github.piscescup.fabricmc.teamcraft.text;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.scores.TeamColor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Shared translatable and styled text helpers.
 *
 * <p>Every user-facing sentence is backed by a {@code teamcraft.*} translation
 * key. Literal components are only used for player input, command syntax and
 * visual separators.</p>
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public final class Msg
{
    private static final Map<String, TeamColor> BY_WORD = new HashMap<>();
    private static final Map<String, String> EN_US_FALLBACK_BY_KEY = new HashMap<>();

    static {
        for (TeamColor color : TeamColor.values()) {
            BY_WORD.put(color.getSerializedName().toLowerCase(Locale.ROOT), color);
        }
        for (TeamcraftTranslations translation : TeamcraftTranslations.values()) {
            EN_US_FALLBACK_BY_KEY.put(translation.key(), translation.enUsTranslation());
        }
    }

    private Msg() {
    }

    /**
     * Creates a translated component from a complete translation key. TeamCraft
     * keys include an English fallback so scoreboard data and server messages
     * remain readable on clients that do not have the mod's language files.
     *
     * @param key  the complete key returned by {@link TeamcraftTranslations#key()}
     * @param args translation arguments
     * @return a mutable translated component
     */
    public static MutableComponent tr(String key, Object... args) {
        String fallback = EN_US_FALLBACK_BY_KEY.get(key);
        return fallback == null
            ? Component.translatable(key, args)
            : Component.translatableWithFallback(key, fallback, args);
    }

    /**
     * Creates the consistent gold mod tag used by command feedback.
     */
    public static MutableComponent prefix() {
        return Component.empty()
            .append(Component.literal("[TeamCraft] ").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
    }

    /**
     * Creates a compact success message with a consistent visual marker.
     */
    public static MutableComponent success(String key, Object... args) {
        return prefix()
            .append(Component.literal("✔ ").withStyle(ChatFormatting.GREEN))
            .append(tr(key, args).withStyle(ChatFormatting.WHITE));
    }

    /**
     * Creates a compact error message with a consistent visual marker.
     */
    public static MutableComponent error(String key, Object... args) {
        return prefix()
            .append(Component.literal("✖ ").withStyle(ChatFormatting.RED))
            .append(tr(key, args).withStyle(ChatFormatting.RED));
    }

    /**
     * Starts a multi-line panel.
     */
    public static MutableComponent panel(String titleKey) {
        return prefix()
            .append(tr(titleKey).withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD))
            .append(Component.literal("\n  ─────────────────────────").withStyle(ChatFormatting.DARK_GRAY));
    }

    /**
     * Creates one aligned-looking label/value row for a panel.
     */
    public static MutableComponent row(String labelKey, Component value) {
        return Component.literal("\n  ")
            .append(tr(labelKey).withStyle(ChatFormatting.GRAY))
            .append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.empty().withStyle(ChatFormatting.WHITE).append(value));
    }

    /**
     * Creates an indented bullet row.
     */
    public static MutableComponent bullet(Component value) {
        return Component.literal("\n    • ").withStyle(ChatFormatting.DARK_GRAY)
            .append(Component.empty().withStyle(ChatFormatting.WHITE).append(value));
    }

    /**
     * Renders a command and its localized help text as one bullet row.
     */
    public static MutableComponent helpLine(String command, String descriptionKey) {
        return Component.literal("\n  • ").withStyle(ChatFormatting.DARK_GRAY)
            .append(Component.literal(command).withStyle(ChatFormatting.YELLOW))
            .append(Component.literal(" — ").withStyle(ChatFormatting.DARK_GRAY))
            .append(tr(descriptionKey).withStyle(ChatFormatting.GRAY));
    }

    /**
     * Parses a color word accepted by the command line, e.g. {@code dark_blue}.
     */
    public static TeamColor parseColor(String word) {
        return BY_WORD.get(word.trim().toLowerCase(Locale.ROOT));
    }

    /**
     * @return every accepted color word, in palette order
     */
    public static List<String> validColorWords() {
        List<String> words = new ArrayList<>();
        for (TeamColor color : TeamColor.values()) {
            words.add(color.getSerializedName());
        }
        return words;
    }

    /**
     * Renders the command-line color token in its own color.
     */
    public static MutableComponent colorWord(TeamColor color) {
        return Component.literal(color.getSerializedName()).withColor(color.textColor());
    }

    /**
     * Renders the localized color name in its own color.
     */
    public static MutableComponent colorName(TeamColor color) {
        TeamcraftTranslations translation = switch (color) {
            case BLACK -> TeamcraftTranslations.COLOR_BLACK;
            case DARK_BLUE -> TeamcraftTranslations.COLOR_DARK_BLUE;
            case DARK_GREEN -> TeamcraftTranslations.COLOR_DARK_GREEN;
            case DARK_AQUA -> TeamcraftTranslations.COLOR_DARK_AQUA;
            case DARK_RED -> TeamcraftTranslations.COLOR_DARK_RED;
            case DARK_PURPLE -> TeamcraftTranslations.COLOR_DARK_PURPLE;
            case GOLD -> TeamcraftTranslations.COLOR_GOLD;
            case GRAY -> TeamcraftTranslations.COLOR_GRAY;
            case DARK_GRAY -> TeamcraftTranslations.COLOR_DARK_GRAY;
            case BLUE -> TeamcraftTranslations.COLOR_BLUE;
            case GREEN -> TeamcraftTranslations.COLOR_GREEN;
            case AQUA -> TeamcraftTranslations.COLOR_AQUA;
            case RED -> TeamcraftTranslations.COLOR_RED;
            case LIGHT_PURPLE -> TeamcraftTranslations.COLOR_LIGHT_PURPLE;
            case YELLOW -> TeamcraftTranslations.COLOR_YELLOW;
            case WHITE -> TeamcraftTranslations.COLOR_WHITE;
        };
        return tr(translation.key()).withColor(color.textColor());
    }

    /**
     * Joins untrusted/user-controlled text without converting it into a
     * translation key or formatting sequence.
     */
    public static MutableComponent joinedLiterals(Collection<String> values) {
        MutableComponent result = Component.empty();
        int index = 0;
        for (String value : values) {
            if (index++ > 0) {
                result.append(Component.literal(", ").withStyle(ChatFormatting.DARK_GRAY));
            }
            result.append(Component.literal(value).withStyle(ChatFormatting.WHITE));
        }
        return result;
    }

    /**
     * Joins colored command-line color tokens.
     */
    public static MutableComponent joinedColors(Collection<TeamColor> colors) {
        MutableComponent result = Component.empty();
        int index = 0;
        for (TeamColor color : colors) {
            if (index++ > 0) {
                result.append("  ");
            }
            result.append(colorWord(color));
        }
        return result;
    }
}
