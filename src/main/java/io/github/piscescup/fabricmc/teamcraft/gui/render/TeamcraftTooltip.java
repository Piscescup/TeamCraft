package io.github.piscescup.fabricmc.teamcraft.gui.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.time.Duration;
import java.util.List;
import java.util.Objects;

/** Tracks the hover delay and draws explanations after content and popups. */
@Environment(EnvType.CLIENT)
public final class TeamcraftTooltip {
    public static final Duration DELAY = Duration.ofMillis(300);
    private Component previous;
    private long hoverSince;

    public void reset() { this.previous = null; }

    public void draw(GuiGraphicsExtractor graphics, Font font, Component message,
                     int mouseX, int mouseY, int screenWidth, int screenHeight) {
        if (!Objects.equals(message, this.previous)) {
            this.previous = message;
            this.hoverSince = System.nanoTime();
        }
        if (message != null && System.nanoTime() - this.hoverSince >= DELAY.toNanos()) {
            drawBox(graphics, font, message, mouseX, mouseY, screenWidth, screenHeight);
        }
    }

    private static void drawBox(GuiGraphicsExtractor graphics, Font font, Component text,
                        int mouseX, int mouseY, int screenWidth, int screenHeight) {
        int wrapWidth = Math.clamp(screenWidth - 28, 1, 600);
        List<FormattedCharSequence> lines = font.split(text, wrapWidth);
        if (lines.isEmpty()) {
            return;
        }
        int lineHeight = font.lineHeight + 2;
        int count = Math.clamp((screenHeight - 24) / lineHeight, 1, lines.size());
        int width = 0;
        for (int i = 0; i < count; i++) {
            width = Math.max(width, font.width(lines.get(i)));
        }
        int height = count * lineHeight + 10;
        int x = Math.clamp(mouseX + 12, 6, Math.max(6, screenWidth - width - 16));
        int y = mouseY - height - 8;
        if (y < 6) {
            y = mouseY + 16;
        }
        y = Math.clamp(y, 6, Math.max(6, screenHeight - height - 6));
        int boxTop = y;
        int boxWidth = width;
        TeamcraftGuiLayers.draw(graphics, TeamcraftGuiLayers.Layer.TOOLTIP, () -> {
            TeamcraftGuiTheme.frame(graphics, x, boxTop, x + boxWidth + 12, boxTop + height,
                TeamcraftGuiTheme.TOOLTIP_BACKGROUND, TeamcraftGuiTheme.TOOLTIP_BORDER);
            for (int i = 0; i < count; i++) {
                int textX = x + 6;
                int textY = boxTop + 5 + i * lineHeight;
                graphics.text(font, lines.get(i), textX, textY, 0xFFFFFFFF);
            }
        });
    }


}
