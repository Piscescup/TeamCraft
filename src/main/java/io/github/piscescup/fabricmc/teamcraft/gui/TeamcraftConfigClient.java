package io.github.piscescup.fabricmc.teamcraft.gui;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.piscescup.fabricmc.teamcraft.References;
import io.github.piscescup.fabricmc.teamcraft.gui.network.ConfigRequestPayload;
import io.github.piscescup.fabricmc.teamcraft.gui.network.ConfigSyncPayload;
import io.github.piscescup.fabricmc.teamcraft.gui.network.ConfigUpdatePayload;
import io.github.piscescup.fabricmc.teamcraft.gui.network.TeamDeletePayload;
import io.github.piscescup.fabricmc.teamcraft.gui.network.TeamUpdatePayload;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftColor;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//#if MC >= 260102
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
//#else
//$$ import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
//#endif
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/** Client-only entry points for opening and saving the configuration screen. */
@Environment(EnvType.CLIENT)
public final class TeamcraftConfigClient {
    //#if MC >= 12110
    private static final KeyMapping.Category KEY_CATEGORY =
        KeyMapping.Category.register(References.fromPath("general"));
    //#else
    //$$ private static final String KEY_CATEGORY = "key.categories.teamcraft";
    //#endif

    private static Screen pendingParent;

    private TeamcraftConfigClient() {
    }

    public static void register() {
        //#if MC >= 260102
        KeyMapping openConfig = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            //#else
            //$$ KeyMapping openConfig = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            //#endif
            TeamcraftTranslations.KEY_TEAMCRAFT_OPEN_CONFIG.key(),
            //#if MC >= 260300
            //$$ InputConstants.Type.KEYBOARD,
            //#else
            InputConstants.Type.KEYSYM,
            //#endif
            InputConstants.KEY_BACKSPACE,
            KEY_CATEGORY
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openConfig.consumeClick()) {
                //#if MC >= 260200
                if (client.player != null && client.gui.screen() == null) {
                    //#else
                    //$$ if (client.player != null && client.screen == null) {
                    //#endif
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
            notifyPlayer(client, TeamcraftTranslations.GUI_ERROR_SERVER_UNSUPPORTED.key());
            return;
        }

        pendingParent = parent;
        ClientPlayNetworking.send(ConfigRequestPayload.INSTANCE);
        if (client.player != null) {
            //#if MC >= 260102
            client.player.sendOverlayMessage(Component.translatable(TeamcraftTranslations.GUI_LOADING.key()));
            //#else
            //$$ client.player.displayClientMessage(Component.translatable(TeamcraftTranslations.GUI_LOADING.key()), true);
            //#endif
        }
    }

    public static boolean save(TeamcraftConfigData config, List<String> candidates, boolean buildTeams) {
        if (!ClientPlayNetworking.canSend(ConfigUpdatePayload.TYPE)) {
            notifyPlayer(Minecraft.getInstance(), TeamcraftTranslations.GUI_ERROR_SERVER_UNSUPPORTED.key());
            return false;
        }
        ClientPlayNetworking.send(new ConfigUpdatePayload(config, candidates, buildTeams));
        return true;
    }

    public static boolean saveOwnTeam(String teamId, String displayName, TeamcraftColor color,
                                      boolean friendlyFire) {
        if (!ClientPlayNetworking.canSend(TeamUpdatePayload.TYPE)) {
            notifyPlayer(Minecraft.getInstance(), TeamcraftTranslations.GUI_ERROR_SERVER_UNSUPPORTED.key());
            return false;
        }
        ClientPlayNetworking.send(new TeamUpdatePayload(teamId, displayName, color, friendlyFire));
        return true;
    }

    public static boolean disbandTeam(String teamId) {
        if (!ClientPlayNetworking.canSend(TeamDeletePayload.TYPE)) {
            notifyPlayer(Minecraft.getInstance(), TeamcraftTranslations.GUI_ERROR_SERVER_UNSUPPORTED.key());
            return false;
        }
        ClientPlayNetworking.send(new TeamDeletePayload(TeamDeletePayload.Target.ONE, teamId));
        return true;
    }

    public static boolean clearAllTeams() {
        if (!ClientPlayNetworking.canSend(TeamDeletePayload.TYPE)) {
            notifyPlayer(Minecraft.getInstance(), TeamcraftTranslations.GUI_ERROR_SERVER_UNSUPPORTED.key());
            return false;
        }
        ClientPlayNetworking.send(new TeamDeletePayload(TeamDeletePayload.Target.ALL));
        return true;
    }

    public static boolean refresh() {
        if (!ClientPlayNetworking.canSend(ConfigRequestPayload.TYPE)) {
            notifyPlayer(Minecraft.getInstance(), TeamcraftTranslations.GUI_ERROR_SERVER_UNSUPPORTED.key());
            return false;
        }
        ClientPlayNetworking.send(ConfigRequestPayload.INSTANCE);
        return true;
    }

    private static void handleSync(Minecraft client, ConfigSyncPayload payload) {
        //#if MC >= 260200
        Screen current = client.gui.screen();
        //#else
        //$$ Screen current = client.screen;
        //#endif
        if (current instanceof TeamcraftConfigScreen configScreen) {
            configScreen.handleServerResponse(payload);
            return;
        }
        if (current instanceof TeamDetailsScreen detailsScreen) {
            detailsScreen.handleServerResponse(payload);
            return;
        }

        if (payload.response() == ConfigSyncPayload.Response.OPENED) {
            //#if MC >= 260200
            client.gui.setScreen(new TeamcraftConfigScreen(
                pendingParent,
                payload.config(),
                payload.candidates(),
                payload.onlinePlayers(),
                payload.ownTeam(),
                payload.teams()
            ));
            //#else
            //$$ client.setScreen(new TeamcraftConfigScreen(
            //$$     pendingParent,
            //$$     payload.config(),
            //$$     payload.candidates(),
            //$$     payload.onlinePlayers(),
            //$$     payload.ownTeam(),
            //$$     payload.teams()
            //$$ ));
            //#endif
            return;
        }

        String key = switch (payload.response()) {
            case SAVED -> TeamcraftTranslations.GUI_SAVED.key();
            case BUILT -> TeamcraftTranslations.GUI_BUILT.key();
            case TEAM_SAVED -> TeamcraftTranslations.GUI_TEAM_SAVED.key();
            case PERMISSION_DENIED -> TeamcraftTranslations.GUI_ERROR_PERMISSION.key();
            case NO_CANDIDATES -> TeamcraftTranslations.GUI_ERROR_NO_CANDIDATES.key();
            case TEAMS_EXIST -> TeamcraftTranslations.GUI_ERROR_TEAMS_EXIST.key();
            case TOO_MANY_TEAMS -> TeamcraftTranslations.GUI_ERROR_TOO_MANY_TEAMS.key();
            case TEAM_NOT_FOUND -> TeamcraftTranslations.GUI_ERROR_TEAM_NOT_FOUND.key();
            case TEAM_DISBANDED -> TeamcraftTranslations.GUI_DETAILS_DISBANDED.key();
            case ALL_TEAMS_CLEARED -> TeamcraftTranslations.GUI_ALL_TEAMS_CLEARED.key();
            case INVALID -> TeamcraftTranslations.GUI_ERROR_INVALID_SERVER.key();
            case OPENED -> throw new IllegalStateException("Handled above");
        };
        notifyPlayer(client, key);
    }

    private static void notifyPlayer(Minecraft client, String translationKey) {
        if (client.player != null) {
            //#if MC >= 260102
            client.player.sendSystemMessage(Component.translatable(translationKey));
            //#else
            //$$ client.player.displayClientMessage(Component.translatable(translationKey), false);
            //#endif
        }
    }
}
