package io.github.piscescup.fabricmc.teamcraft.compat.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import io.github.piscescup.fabricmc.teamcraft.gui.TeamcraftConfigClient;

/**
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public class TeamCraftModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return TeamcraftConfigClient::createModMenuScreen;
    }
}
