package io.github.piscescup.fabricmc.teamcraft;

import io.github.piscescup.fabricmc.teamcraft.command.TeamcraftCommand;
import io.github.piscescup.fabricmc.teamcraft.gui.network.TeamcraftConfigNetworking;
import io.github.piscescup.fabricmc.teamcraft.team.TeamSessionManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;

import static io.github.piscescup.fabricmc.teamcraft.References.MOD_LOGGER;
import static io.github.piscescup.fabricmc.teamcraft.References.MOD_NAME;

public class Teamcraft
    implements ModInitializer
{
    @Override
    public void onInitialize() {
        MOD_LOGGER.info("Hello, {}", MOD_NAME);

        TeamcraftConfigNetworking.register();
        CommandRegistrationCallback.EVENT.register(TeamcraftCommand::register);
        ServerLifecycleEvents.SERVER_STOPPING.register(_ -> TeamSessionManager.reset());
    }
}
