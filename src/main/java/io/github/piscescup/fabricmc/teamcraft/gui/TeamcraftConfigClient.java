package io.github.piscescup.fabricmc.teamcraft.gui;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.piscescup.fabricmc.teamcraft.References;
import io.github.piscescup.fabricmc.teamcraft.gui.network.ConfigRequestPayload;
import io.github.piscescup.fabricmc.teamcraft.gui.network.ConfigSyncPayload;
import io.github.piscescup.fabricmc.teamcraft.gui.network.ConfigUpdatePayload;
import io.github.piscescup.fabricmc.teamcraft.gui.network.TeamUpdatePayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Client-only entry points for opening and saving the configuration screen. */
@Environment(EnvType.CLIENT)
public final class TeamcraftConfigClient {
    private static final KeyMapping.Category KEY_CATEGORY =
        KeyMapping.Category.register(References.fromPath("general"));

    private static Screen pendingParent;

    private TeamcraftConfigClient() {
    }

    public static void register() {
        KeyMapping openConfig = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.teamcraft.open_config",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_O,
            KEY_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openConfig.consumeClick()) {
                if (client.player != null && client.gui.screen() == null) {
                    requestOpen(null);
                }
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(ConfigSyncPayload.TYPE, (payload, context) ->
            handleSync(context.client(), payload)
        );
    }

    /**
     * Requests an authoritative snapshot before creating the screen.
     * This is also suitable for a future Mod Menu integration.
     */
    public static void requestOpen(Screen parent) {
        Minecraft client = Minecraft.getInstance();
        if (!ClientPlayNetworking.canSend(ConfigRequestPayload.TYPE)) {
            notifyPlayer(client, "teamcraft.gui.error.server_unsupported");
            return;
        }

        pendingParent = parent;
        ClientPlayNetworking.send(ConfigRequestPayload.INSTANCE);
        if (client.player != null) {
            client.player.sendOverlayMessage(Component.translatable("teamcraft.gui.loading"));
        }
    }

    public static boolean save(TeamcraftConfigData config, boolean buildTeams) {
        if (!ClientPlayNetworking.canSend(ConfigUpdatePayload.TYPE)) {
            notifyPlayer(Minecraft.getInstance(), "teamcraft.gui.error.server_unsupported");
            return false;
        }
        ClientPlayNetworking.send(new ConfigUpdatePayload(config, buildTeams));
        return true;
    }

    public static boolean saveOwnTeam(String teamId, String displayName, net.minecraft.world.scores.TeamColor color,
                                      boolean friendlyFire) {
        if (!ClientPlayNetworking.canSend(TeamUpdatePayload.TYPE)) {
            notifyPlayer(Minecraft.getInstance(), "teamcraft.gui.error.server_unsupported");
            return false;
        }
        ClientPlayNetworking.send(new TeamUpdatePayload(teamId, displayName, color, friendlyFire));
        return true;
    }

    public static boolean refresh() {
        if (!ClientPlayNetworking.canSend(ConfigRequestPayload.TYPE)) {
            notifyPlayer(Minecraft.getInstance(), "teamcraft.gui.error.server_unsupported");
            return false;
        }
        ClientPlayNetworking.send(ConfigRequestPayload.INSTANCE);
        return true;
    }

    private static void handleSync(Minecraft client, ConfigSyncPayload payload) {
        Screen current = client.gui.screen();
        if (current instanceof TeamcraftConfigScreen configScreen) {
            configScreen.handleServerResponse(payload);
            return;
        }

        if (payload.response() == ConfigSyncPayload.Response.OPENED) {
            client.gui.setScreen(new TeamcraftConfigScreen(
                pendingParent,
                payload.config(),
                payload.ownTeam(),
                payload.teams()
            ));
            return;
        }

        String key = switch (payload.response()) {
            case SAVED -> "teamcraft.gui.saved";
            case BUILT -> "teamcraft.gui.built";
            case TEAM_SAVED -> "teamcraft.gui.team_saved";
            case PERMISSION_DENIED -> "teamcraft.gui.error.permission";
            case NO_CANDIDATES -> "teamcraft.gui.error.no_candidates";
            case TEAMS_EXIST -> "teamcraft.gui.error.teams_exist";
            case TOO_MANY_TEAMS -> "teamcraft.gui.error.too_many_teams";
            case TEAM_NOT_FOUND -> "teamcraft.gui.error.team_not_found";
            case INVALID -> "teamcraft.gui.error.invalid_server";
            case OPENED -> throw new IllegalStateException("Handled above");
        };
        notifyPlayer(client, key);
    }

    private static void notifyPlayer(Minecraft client, String translationKey) {
        if (client.player != null) {
            client.player.sendSystemMessage(Component.translatable(translationKey));
        }
    }
}
