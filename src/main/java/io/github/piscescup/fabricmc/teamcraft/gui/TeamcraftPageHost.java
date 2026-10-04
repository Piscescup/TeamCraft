package io.github.piscescup.fabricmc.teamcraft.gui;

import io.github.piscescup.fabricmc.teamcraft.gui.tab.TeamcraftTab;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

import java.util.List;

/** Navigation and session state needed by a page, independent of its rendering. */
@Environment(EnvType.CLIENT)
public interface TeamcraftPageHost {
    Component title();
    List<TeamcraftTab> pages();
    void switchPage(String id);
    void close();
    boolean waitingForServer();
    boolean isInGameUi();
}
