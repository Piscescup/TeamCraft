package io.github.piscescup.fabricmc.teamcraft.hotkey;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.piscescup.fabricmc.teamcraft.References;
import io.github.piscescup.fabricmc.teamcraft.gui.TeamcraftConfigClient;
import io.github.piscescup.fabricmc.teamcraft.gui.TeamcraftPages;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
//#if MC >= 260102
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
//#else
//$$ import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
//#endif
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.List;

/** Local, vanilla-persisted shortcuts for page navigation, never for privileged actions. */
@Environment(EnvType.CLIENT)
public final class TeamcraftHotkeys {
    //#if MC >= 12110
    private static final KeyMapping.Category CATEGORY =
        KeyMapping.Category.register(References.fromPath("general"));
    //#else
    //$$ private static final String CATEGORY = TeamcraftTranslations.KEY_CATEGORY_TEAMCRAFT_GENERAL.key();
    //#endif
    private static final List<Entry> ENTRIES = new ArrayList<>();

    private TeamcraftHotkeys() {
    }

    public static void register() {
        add(TeamcraftTranslations.KEY_TEAMCRAFT_OPEN_CONFIG, InputConstants.KEY_BACKSPACE,
            TeamcraftPages.TEAM_CONFIG, TeamcraftTranslations.GUI_HOTKEYS_OPEN_CONFIG_TOOLTIP);
        add(TeamcraftTranslations.KEY_TEAMCRAFT_OPEN_TEAMS, -1,
            TeamcraftPages.ALL_TEAMS, TeamcraftTranslations.GUI_HOTKEYS_OPEN_TEAMS_TOOLTIP);
        add(TeamcraftTranslations.KEY_TEAMCRAFT_OPEN_OWN_TEAM, -1,
            TeamcraftPages.OWN_TEAM, TeamcraftTranslations.GUI_HOTKEYS_OPEN_OWN_TEAM_TOOLTIP);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            Entry pressed = null;
            for (Entry entry : ENTRIES) {
                while (entry.mapping.consumeClick()) {
                    if (pressed == null) {
                        pressed = entry;
                    }
                }
            }
            //#if MC >= 260200
            boolean inWorld = client.player != null && client.gui.screen() == null;
            //#else
            //$$ boolean inWorld = client.player != null && client.screen == null;
            //#endif
            if (pressed != null && inWorld) {
                TeamcraftConfigClient.requestOpen(null, pressed.page);
            }
        });
    }

    private static void add(TeamcraftTranslations name, int defaultKey,
                            String page, TeamcraftTranslations tooltip) {
        KeyMapping mapping = new KeyMapping(name.key(),
            //#if MC >= 260300
            //$$ InputConstants.Type.KEYBOARD,
            //#else
            InputConstants.Type.KEYSYM,
            //#endif
            defaultKey, CATEGORY);
        //#if MC >= 260102
        KeyMappingHelper.registerKeyMapping(mapping);
        //#else
        //$$ KeyBindingHelper.registerKeyBinding(mapping);
        //#endif
        ENTRIES.add(new Entry(mapping, page, tooltip));
    }

    public static List<Entry> entries() {
        return List.copyOf(ENTRIES);
    }

    public static void bind(KeyMapping mapping, InputConstants.Key key) {
        mapping.setKey(key);
        KeyMapping.resetMapping();
        Minecraft.getInstance().options.save();
    }

    public static KeyMapping conflict(KeyMapping mapping) {
        if (!mapping.isUnbound()) {
            for (KeyMapping other : Minecraft.getInstance().options.keyMappings) {
                if (other != mapping && mapping.same(other)) {
                    return other;
                }
            }
        }
        return null;
    }

    public record Entry(KeyMapping mapping, String page, TeamcraftTranslations tooltip) {
    }
}
