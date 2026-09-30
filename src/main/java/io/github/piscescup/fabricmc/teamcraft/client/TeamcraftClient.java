package io.github.piscescup.fabricmc.teamcraft.client;

import io.github.piscescup.fabricmc.teamcraft.gui.TeamcraftConfigClient;
import net.fabricmc.api.ClientModInitializer;

/**
 * Clien entry.
 */
public class TeamcraftClient
    implements ClientModInitializer
{
    @Override
    public void onInitializeClient() {
        TeamcraftConfigClient.register();
    }
}
