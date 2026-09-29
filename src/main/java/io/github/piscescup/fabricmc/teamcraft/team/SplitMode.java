package io.github.piscescup.fabricmc.teamcraft.team;

import io.github.piscescup.fabricmc.teamcraft.text.Msg;
import net.minecraft.network.chat.MutableComponent;

/**
 * The strategy used to distribute candidates into teams.
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public enum SplitMode
{
    /**
     * Candidates are assigned in the exact order they appear in the session list.
     */
    FIXED("mode.fixed"),

    /**
     * Candidates are shuffled before being assigned.
     */
    RANDOM("mode.random");

    private final String translationKey;

    SplitMode(String translationKey) {
        this.translationKey = translationKey;
    }

    /**
     * @return the localized display name of this mode
     */
    public MutableComponent displayName() {
        return Msg.tr(this.translationKey);
    }
}
