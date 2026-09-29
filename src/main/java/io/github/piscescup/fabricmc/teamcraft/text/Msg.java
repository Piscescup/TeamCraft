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
    private static final String KEY_PREFIX = "teamcraft.";
    private static final Map<String, TeamColor> BY_WORD = new HashMap<>();

    static {
        for (TeamColor color : TeamColor.values()) {
            BY_WORD.put(color.getSerializedName().toLowerCase(Locale.ROOT), color);
        }
    }

    private Msg() {
    }

    /**
     * Creates a translated component in the mod namespace.
     *
     * @param key  the part after {@code teamcraft.}
     * @param args translation arguments
     * @return a mutable translated component
     */
    public static MutableComponent tr(String key, Object... args) {
        return Component.translatable(KEY_PREFIX + key, args);
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
        return tr("color." + color.getSerializedName()).withColor(color.textColor());
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
