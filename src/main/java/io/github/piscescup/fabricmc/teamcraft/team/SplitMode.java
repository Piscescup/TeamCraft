package io.github.piscescup.fabricmc.teamcraft.team;

import com.mojang.serialization.Codec;
import io.github.piscescup.fabricmc.teamcraft.text.Msg;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.StringRepresentable;
//#if MC >= 12111
import org.jspecify.annotations.NonNull;
//#endif

/**
 * The strategy used to distribute candidates into teams.
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public enum SplitMode implements StringRepresentable {
    /**
     * Candidates are assigned in the exact order they appear in the session list.
     */
    FIXED("fixed"),

    /**
     * Candidates are shuffled before being assigned.
     */
    RANDOM("random");

    /**
     * The {@link Codec} of the {@link SplitMode}.
     */
    public static final StringRepresentable.EnumCodec<SplitMode> CODEC = StringRepresentable.fromEnum(
        SplitMode::values
    );

    /**
     * Convert a name to the {@link SplitMode}.
     * @param name the string name of {@link SplitMode}.
     * @return the {@link SplitMode}.
     */
    public static SplitMode fromName(String name) {
        return CODEC.byName(name);
    }

    private final String name;

    SplitMode(String name) {
        this.name = name;
    }

    /**
     * @return the localized display name of this mode
     */
    public MutableComponent displayName() {
        return Msg.tr(switch (this) {
            case FIXED -> TeamcraftTranslations.MODE_FIXED.key();
            case RANDOM -> TeamcraftTranslations.MODE_RANDOM.key();
        });
    }

    //#if MC >= 12111
    @NonNull
    //#endif
    @Override
    public String getSerializedName() {
        return name;
    }
}
