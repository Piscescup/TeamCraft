package io.github.piscescup.fabricmc.teamcraft.gui;

import io.github.piscescup.fabricmc.teamcraft.gui.render.TeamcraftGuiTheme;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Modal dialog shown when the server rejects a TeamCraft GUI action. */
@Environment(EnvType.CLIENT)
public final class PermissionDeniedScreen extends TeamcraftDialogScreen {
    public PermissionDeniedScreen(Screen parent) {
        super(parent, Component.translatable(TeamcraftTranslations.GUI_TITLE.key()),
            Component.translatable(TeamcraftTranslations.GUI_ERROR_PERMISSION.key()));
    }

    public static void open(Screen parent) {
        display(new PermissionDeniedScreen(parent));
    }

    @Override
    protected int accentColor() {
        return TeamcraftGuiTheme.ERROR;
    }
}
