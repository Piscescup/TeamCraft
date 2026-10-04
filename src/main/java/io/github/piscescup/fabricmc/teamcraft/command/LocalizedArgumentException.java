package io.github.piscescup.fabricmc.teamcraft.command;

import io.github.piscescup.fabricmc.teamcraft.text.Msg;
import net.minecraft.network.chat.Component;

/**
 * Keeps parser failures localizable without leaking already-rendered server
 * strings into player-facing messages.
 */
class LocalizedArgumentException
    extends IllegalArgumentException
{
    private final String key;
    private final Object[] args;

    LocalizedArgumentException(String key, Object... args) {
        this.key = key;
        this.args = args;
    }

    Component component() {
        return Msg.error(this.key, this.args);
    }
}
