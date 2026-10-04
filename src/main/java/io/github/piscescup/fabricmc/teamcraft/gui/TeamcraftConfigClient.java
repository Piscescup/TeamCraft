package io.github.piscescup.fabricmc.teamcraft.gui;

import io.github.piscescup.fabricmc.teamcraft.config.TeamcraftConfigData;
import io.github.piscescup.fabricmc.teamcraft.gui.network.ConfigRequestPayload;
import io.github.piscescup.fabricmc.teamcraft.gui.network.ConfigSyncPayload;
import io.github.piscescup.fabricmc.teamcraft.gui.network.ConfigUpdatePayload;
import io.github.piscescup.fabricmc.teamcraft.gui.network.TeamDeletePayload;
import io.github.piscescup.fabricmc.teamcraft.gui.network.TeamUpdatePayload;
import io.github.piscescup.fabricmc.teamcraft.gui.network.TeamLeavePayload;
import io.github.piscescup.fabricmc.teamcraft.hotkey.TeamcraftHotkeys;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftColor;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

/** Client-only entry points for opening and saving the configuration screen. */
@Environment(EnvType.CLIENT)
public final class TeamcraftConfigClient {
    private static Screen pendingParent;
    private static String pendingPage = TeamcraftPages.TEAM_CONFIG;

    private TeamcraftConfigClient() {
    }

    public static void register() {
        TeamcraftInvitationClient.register();
        TeamcraftHotkeys.register();
        ClientPlayNetworking.registerGlobalReceiver(ConfigSyncPayload.TYPE, (payload, context) ->
            handleSync(context.client(), payload)
        );
    }

    /**
     * Requests an authoritative snapshot before creating the screen.
     */
    public static void requestOpen(Screen parent) {
        requestOpen(parent, TeamcraftPages.TEAM_CONFIG);
    }

    public static void requestOpen(Screen parent, String page) {
        Minecraft client = Minecraft.getInstance();
        if (!ClientPlayNetworking.canSend(ConfigRequestPayload.TYPE)) {
            notifyPlayer(client, TeamcraftTranslations.GUI_ERROR_SERVER_UNSUPPORTED.key(), ChatFormatting.RED);
            return;
        }

        pendingParent = parent;
        pendingPage = page;
        ClientPlayNetworking.send(ConfigRequestPayload.INSTANCE);
        if (client.player != null) {
            //#if MC >= 260102
            client.player.sendOverlayMessage(Component.translatable(TeamcraftTranslations.GUI_LOADING.key()));
            //#else
            //$$ client.player.displayClientMessage(Component.translatable(TeamcraftTranslations.GUI_LOADING.key()), true);
            //#endif
        }
    }

    /**
     * Creates the screen exposed through Mod Menu.
     *
     * <p>Changing the configuration requires an active server connection, so
     * an in-world client first receives a small loading screen while the
     * authoritative snapshot is requested. From the title screen, or when the
     * connected server does not have TeamCraft, the read-only Help page is
     * still available.</p>
     */
    public static Screen createModMenuScreen(Screen parent) {
        return ClientPlayNetworking.canSend(ConfigRequestPayload.TYPE)
            ? new TeamcraftModMenuScreen(parent)
            : TeamcraftPageContext.help(parent).screen();
    }

    /** Send helpers return false when unavailable; GUI callers own the failure dialog. */
    public static boolean save(TeamcraftConfigData config, List<String> candidates, boolean buildTeams) {
        if (!ClientPlayNetworking.canSend(ConfigUpdatePayload.TYPE)) {
            return false;
        }
        ClientPlayNetworking.send(new ConfigUpdatePayload(config, candidates, buildTeams));
        return true;
    }

    public static boolean saveOwnTeam(String teamId, String displayName, TeamcraftColor color,
                                      boolean friendlyFire) {
        if (!ClientPlayNetworking.canSend(TeamUpdatePayload.TYPE)) {
            return false;
        }
        ClientPlayNetworking.send(new TeamUpdatePayload(teamId, displayName, color, friendlyFire));
        return true;
    }

    public static boolean disbandTeam(String teamId) {
        if (!ClientPlayNetworking.canSend(TeamDeletePayload.TYPE)) {
            return false;
        }
        ClientPlayNetworking.send(new TeamDeletePayload(TeamDeletePayload.Target.ONE, teamId));
        return true;
    }

    public static boolean clearAllTeams() {
        if (!ClientPlayNetworking.canSend(TeamDeletePayload.TYPE)) {
            return false;
        }
        ClientPlayNetworking.send(new TeamDeletePayload(TeamDeletePayload.Target.ALL));
        return true;
    }

    public static boolean leaveTeam(String teamId) {
        if (!ClientPlayNetworking.canSend(TeamLeavePayload.TYPE)) {
            return false;
        }
        ClientPlayNetworking.send(new TeamLeavePayload(teamId));
        return true;
    }

    public static boolean refresh() {
        if (!ClientPlayNetworking.canSend(ConfigRequestPayload.TYPE)) {
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
        if (current instanceof TeamcraftConfigResponseReceiver receiver) {
            receiver.handleServerResponse(payload);
            return;
        }

        if (payload.response() == ConfigSyncPayload.Response.OPENED) {
            TeamcraftPageContext opened = new TeamcraftPageContext(
                pendingParent,
                payload.config(),
                payload.candidates(),
                payload.onlinePlayers(),
                payload.ownTeam(),
                payload.teams()
            );
            opened.selectPage(pendingPage);
            pendingParent = null;
            pendingPage = TeamcraftPages.TEAM_CONFIG;
            //#if MC >= 260200
            client.gui.setScreen(opened.screen());
            //#else
            //$$ client.setScreen(opened.screen());
            //#endif
            return;
        }

        if (payload.response() == ConfigSyncPayload.Response.PERMISSION_DENIED) {
            PermissionDeniedScreen.open(pendingParent);
            pendingParent = null;
            pendingPage = TeamcraftPages.TEAM_CONFIG;
            return;
        }

        String key = switch (payload.response()) {
            case SAVED -> TeamcraftTranslations.GUI_SAVED.key();
            case BUILT -> TeamcraftTranslations.GUI_BUILT.key();
            case TEAM_SAVED -> TeamcraftTranslations.GUI_TEAM_SAVED.key();
            case TEAM_LEFT -> TeamcraftTranslations.GUI_TEAM_LEFT.key();
            case NOT_IN_TEAM -> TeamcraftTranslations.ERROR_NOT_IN_TEAM.key();
            case PERMISSION_DENIED, OPENED -> throw new IllegalStateException("Handled above");
            case NO_CANDIDATES -> TeamcraftTranslations.GUI_ERROR_NO_CANDIDATES.key();
            case TEAMS_EXIST -> TeamcraftTranslations.GUI_ERROR_TEAMS_EXIST.key();
            case TOO_MANY_TEAMS -> TeamcraftTranslations.GUI_ERROR_TOO_MANY_TEAMS.key();
            case TEAM_NOT_FOUND -> TeamcraftTranslations.GUI_ERROR_TEAM_NOT_FOUND.key();
            case TEAM_DISBANDED -> TeamcraftTranslations.GUI_DETAILS_DISBANDED.key();
            case ALL_TEAMS_CLEARED -> TeamcraftTranslations.GUI_ALL_TEAMS_CLEARED.key();
            case INVALID -> TeamcraftTranslations.GUI_ERROR_INVALID_SERVER.key();
        };
        ChatFormatting color = switch (payload.response()) {
            case SAVED, BUILT, TEAM_SAVED, TEAM_DISBANDED, ALL_TEAMS_CLEARED, TEAM_LEFT -> ChatFormatting.GREEN;
            default -> ChatFormatting.RED;
        };
        notifyPlayer(client, key, color);
    }

    private static void notifyPlayer(Minecraft client, String translationKey, ChatFormatting color) {
        if (client.player != null) {
            //#if MC >= 260102
            client.player.sendSystemMessage(Component.translatable(translationKey).withStyle(color));
            //#else
            //$$ client.player.displayClientMessage(
            //$$     Component.translatable(translationKey).withStyle(color), false);
            //#endif
        }
    }
}
