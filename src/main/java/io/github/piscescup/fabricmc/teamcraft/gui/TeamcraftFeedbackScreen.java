package io.github.piscescup.fabricmc.teamcraft.gui;

import io.github.piscescup.fabricmc.teamcraft.gui.render.TeamcraftGuiTheme;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Objects;

/** A reusable result dialog for successful actions, notices and errors. */
@Environment(EnvType.CLIENT)
public final class TeamcraftFeedbackScreen extends TeamcraftDialogScreen {
    public enum Type {
        SUCCESS(0xFF55FF55),
        INFO(TeamcraftGuiTheme.ACCENT),
        ERROR(TeamcraftGuiTheme.ERROR);

        private final int color;

        Type(int color) {
            this.color = color;
        }
    }

    private final Type type;

    public TeamcraftFeedbackScreen(Screen parent, Component message, Type type) {
        this(parent, Component.translatable(TeamcraftTranslations.GUI_TITLE.key()), message, type);
    }

    public TeamcraftFeedbackScreen(Screen parent, Component title, Component message, Type type) {
        super(parent, title, message);
        this.type = Objects.requireNonNull(type, "type");
    }

    public static void open(Screen parent, Component message, Type type) {
        display(new TeamcraftFeedbackScreen(parent, message, type));
    }

    @Override
    protected int accentColor() {
        return this.type.color;
    }
}
