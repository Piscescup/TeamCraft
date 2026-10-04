package io.github.piscescup.fabricmc.teamcraft.gui.widget;

import io.github.piscescup.fabricmc.teamcraft.gui.layout.TeamcraftBounds;
import io.github.piscescup.fabricmc.teamcraft.gui.render.TeamcraftGuiTheme;
import io.github.piscescup.fabricmc.teamcraft.gui.render.TeamcraftGuiLayers;
import io.github.piscescup.fabricmc.teamcraft.text.Msg;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftColor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
//#if MC < 12103
//$$ import net.minecraft.client.resources.sounds.SimpleSoundInstance;
//$$ import net.minecraft.sounds.SoundEvents;
//#endif

import java.util.function.Consumer;

/** Owns the color menu's bounds, selection and overlay drawing, not configuration data. */
@Environment(EnvType.CLIENT)
public final class TeamcraftColorPopup {
    private Menu menu;
    private TeamcraftBounds viewport;
    private final Runnable rebuild;

    public TeamcraftColorPopup(Runnable rebuild) { this.rebuild = rebuild; }
    public boolean isOpen() { return this.menu != null; }
    public void close() { this.menu = null; }

    public void open(AbstractWidget anchor, TeamcraftBounds viewport, Consumer<TeamcraftColor> onChanged) {
        this.viewport = viewport;
        int rowHeight = 16;
        int columns = viewport.height() >= ((TeamcraftColor.values().length + 1) / 2) * rowHeight ? 2 : 4;
        int rows = (TeamcraftColor.values().length + columns - 1) / columns;
        int availableWidth = Math.max(columns, viewport.width() - 8);
        int desiredWidth = columns == 2 ? Math.max(anchor.getWidth(), 220) : availableWidth;
        int columnWidth = Math.max(1, Math.min(desiredWidth, availableWidth) / columns);
        int width = columnWidth * columns;
        int height = rows * rowHeight;
        int minimumX = viewport.left() + 4;
        int x = Math.clamp(anchor.getX(), minimumX, Math.max(minimumX, viewport.right() - width - 4));
        int below = anchor.getBottom() + 1;
        int y = below + height <= viewport.bottom()
            ? below : Math.max(viewport.top(), anchor.getY() - height - 1);
        this.menu = new Menu(x, y, columnWidth, rowHeight, columns,
            anchor.getX(), anchor.getY(), anchor.getWidth(), anchor.getHeight(), onChanged);
    }

    /** Clicking outside dismisses the popup; the anchor click is consumed. */
    public boolean mouseClicked(double x, double y, int button) {
        if (this.menu == null) { return false; }
        Menu selected = this.menu;
        close();
        if (button == 0 && this.viewport.contains(x, y) && selected.contains(x, y)) {
            int column = (int) ((x - selected.x) / selected.columnWidth);
            int row = (int) ((y - selected.y) / selected.rowHeight);
            int index = row * selected.columns + column;
            TeamcraftColor[] colors = TeamcraftColor.values();
            if (index >= 0 && index < colors.length) {
                Minecraft client = Minecraft.getInstance();
                //#if MC >= 12103
                AbstractWidget.playButtonClickSound(client.getSoundManager());
                //#else
                //$$ client.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                //#endif
                selected.onChanged.accept(colors[index]);
                this.rebuild.run();
                return true;
            }
        }
        return selected.anchorContains(x, y);
    }

    public void draw(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
        if (this.menu == null) {
            return;
        }
        TeamcraftGuiLayers.draw(graphics, TeamcraftGuiLayers.Layer.POPUP,
            () -> drawMenu(graphics, font, mouseX, mouseY));
    }

    private void drawMenu(GuiGraphicsExtractor graphics, Font font, int mouseX, int mouseY) {
        Menu menu = this.menu;
        TeamcraftColor[] colors = TeamcraftColor.values();
        int rows = (colors.length + menu.columns - 1) / menu.columns;
        int width = menu.columnWidth * menu.columns;
        int height = rows * menu.rowHeight;

        graphics.enableScissor(this.viewport.left(), this.viewport.top(), this.viewport.right(), this.viewport.bottom());
        try {
            TeamcraftGuiTheme.frame(graphics, menu.x - 1, menu.y - 1, menu.x + width + 1,
                menu.y + height + 1, TeamcraftGuiTheme.PANEL, TeamcraftGuiTheme.BORDER);
            for (int i = 0; i < colors.length; i++) {
                int column = i % menu.columns;
                int row = i / menu.columns;
                int x = menu.x + column * menu.columnWidth;
                int y = menu.y + row * menu.rowHeight;
                boolean hovered = mouseX >= x && mouseX < x + menu.columnWidth
                    && mouseY >= y && mouseY < y + menu.rowHeight;
                if (hovered) {
                    graphics.fill(x, y, x + menu.columnWidth, y + menu.rowHeight, TeamcraftGuiTheme.COLOR_HOVER);
                }
                TeamcraftColor color = colors[i];
                graphics.fill(x + 4, y + 5, x + 11, y + 12, 0xFF000000 | color.rgb());
                graphics.text(
                    font,
                    Msg.colorName(color).copy().withColor(color.textColor()).getVisualOrderText(),
                    x + 15,
                    y + 5,
                    0xFFFFFFFF
                );
            }
        }
        finally {
            graphics.disableScissor();
        }
    }

    private record Menu(
        int x,
        int y,
        int columnWidth,
        int rowHeight,
        int columns,
        int anchorX,
        int anchorY,
        int anchorWidth,
        int anchorHeight,
        Consumer<TeamcraftColor> onChanged
    ) {
        private boolean contains(double mouseX, double mouseY) {
            int rows = (TeamcraftColor.values().length + this.columns - 1) / this.columns;
            return mouseX >= this.x && mouseX < this.x + this.columnWidth * this.columns
                && mouseY >= this.y && mouseY < this.y + this.rowHeight * rows;
        }

        private boolean anchorContains(double mouseX, double mouseY) {
            return mouseX >= this.anchorX && mouseX < this.anchorX + this.anchorWidth
                && mouseY >= this.anchorY && mouseY < this.anchorY + this.anchorHeight;
        }
    }

}
