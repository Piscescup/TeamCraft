package io.github.piscescup.fabricmc.teamcraft.gui.widget;

import io.github.piscescup.fabricmc.teamcraft.gui.TeamcraftPageContext;
import io.github.piscescup.fabricmc.teamcraft.gui.TeamcraftPages;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.piscescup.fabricmc.teamcraft.gui.tab.TeamcraftTab;
import io.github.piscescup.fabricmc.teamcraft.hotkey.TeamcraftHotkeys;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;

/** Only frequently used page shortcuts are bindable; configuration values have no hotkeys. */
public final class TeamcraftHotkeysTab
    extends TeamcraftTab
{
    public TeamcraftHotkeysTab(TeamcraftPageContext context) {
        super(context, TeamcraftPages.HOTKEYS, TeamcraftTranslations.GUI_NAV_HOTKEYS.key(),
            TeamcraftTranslations.GUI_NAV_HOTKEYS_TOOLTIP.key());
    }

    @Override
    public boolean requiresServer() {
        return false;
    }

    @Override
    protected void build() {
        addTextRow(Component.translatable(TeamcraftTranslations.GUI_HOTKEYS_HINT.key())
            .withStyle(ChatFormatting.GRAY), null);
        for (TeamcraftHotkeys.Entry entry : TeamcraftHotkeys.entries()) {
            KeyMapping mapping = entry.mapping();
            KeyMapping conflict = TeamcraftHotkeys.conflict(mapping);
            Component key = mapping.getTranslatedKeyMessage().copy()
                .withStyle(conflict == null ? ChatFormatting.WHITE : ChatFormatting.RED);
            Component tooltip = Component.translatable(entry.tooltip().key());
            if (conflict != null) {
                tooltip = tooltip.copy().append("\n").append(Component.translatable(
                    TeamcraftTranslations.GUI_HOTKEYS_CONFLICT.key(), Component.translatable(conflict.getName()))
                    .withStyle(ChatFormatting.RED));
            }
            TeamcraftButton bind = TeamcraftButton.themedBuilder(key,
                button -> beginKeyBinding(mapping, button)).build()
                .hoverHint(Component.translatable(TeamcraftTranslations.GUI_BUTTON_BIND_KEY.key()));
            TeamcraftButton clear = TeamcraftButton.themedBuilder(Component.literal("×"), ignored -> {
                TeamcraftHotkeys.bind(mapping, InputConstants.UNKNOWN);
                rebuildPage();
            }).build().hoverHint(Component.translatable(TeamcraftTranslations.GUI_BUTTON_CLEAR_KEY.key()));
            TeamcraftButton reset = TeamcraftButton.themedBuilder(
                Component.translatable(TeamcraftTranslations.GUI_HOTKEYS_RESET.key()), ignored -> {
                    TeamcraftHotkeys.bind(mapping, mapping.getDefaultKey());
                    rebuildPage();
                }).build().hoverHint(Component.translatable(TeamcraftTranslations.GUI_BUTTON_RESET_KEY.key()));
            addHotkeyRow(mapping.getName(), bind, clear, reset, tooltip,
                !mapping.isUnbound(), !mapping.isDefault());
        }
    }
}
