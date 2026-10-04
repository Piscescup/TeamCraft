package io.github.piscescup.fabricmc.teamcraft.gui;

import io.github.piscescup.fabricmc.teamcraft.gui.network.ConfigSyncPayload;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/** Optional capability used to dispatch authoritative configuration responses. */
@Environment(EnvType.CLIENT)
public interface TeamcraftConfigResponseReceiver {
    void handleServerResponse(ConfigSyncPayload payload);
}
