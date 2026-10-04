package io.github.piscescup.fabricmc.teamcraft.gui.widget;

import io.github.piscescup.fabricmc.teamcraft.gui.render.TeamcraftGuiTheme;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

/** Beveled gray button; vanilla input, focus, narration and tooltips are retained. */
@Environment(EnvType.CLIENT)
public class TeamcraftButton extends Button {
    private boolean selected;
    private boolean plain;
    private String subtitle;
    private Component hoverHint;

    protected TeamcraftButton(int x, int y, int width, int height, Component message, OnPress onPress) {
        super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
    }

    public static Builder themedBuilder(Component message, OnPress onPress) {
        return new Builder(message, onPress);
    }

    public TeamcraftButton selected(boolean selected) {
        this.selected = selected;
        return this;
    }

    public TeamcraftButton plain() {
        this.plain = true;
        return this;
    }

    public TeamcraftButton subtitle(String subtitle) {
        this.subtitle = subtitle;
        return this;
    }

    /** Short action hint, distinct from the content row's complete explanation. */
    public TeamcraftButton hoverHint(Component hint) {
        this.hoverHint = hint;
        return this;
    }

    public Component hoverHint() {
        return this.hoverHint;
    }

    @Override
    //#if MC >= 260102
    protected void extractContents(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    //#elseif MC >= 12111
    //$$ protected void renderContents(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    //#else
    //$$ protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    //#endif
        boolean highlighted = this.active && isHoveredOrFocused();
        if (!this.plain) {
            TeamcraftGuiTheme.button(graphics, getX(), getY(), getRight(), getBottom(),
                this.active, highlighted, this.selected);
        }
        var font = Minecraft.getInstance().font;
        Component label = getMessage();
        int textWidth = Math.max(1, getWidth() - 10);
        // Keep long names within their own column on small GUI scales.
        var lines = font.split(label, textWidth);
        if (lines.isEmpty()) {
            return;
        }
        var text = lines.getFirst();
        int x = this.plain ? getX() + 5 : getX() + (getWidth() - font.width(text)) / 2;
        graphics.text(font, text, x, this.subtitle == null ? getY() + (getHeight() - font.lineHeight) / 2 : getY() + 3,
            !this.active || this.selected ? TeamcraftGuiTheme.MUTED : TeamcraftGuiTheme.TEXT);
        if (this.subtitle != null) {
            TeamcraftGuiTheme.smallText(graphics, font, this.subtitle, getX() + 26, getY() + 15, getWidth() - 30);
        }
    }

    public static final class Builder {
        private final Component message;
        private final OnPress onPress;
        private int x;
        private int y;
        private int width = 150;
        private int height = 20;
        private Tooltip tooltip;

        private Builder(Component message, OnPress onPress) {
            this.message = message;
            this.onPress = onPress;
        }

        public Builder bounds(int x, int y, int width, int height) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            return this;
        }

        public Builder tooltip(Tooltip tooltip) {
            this.tooltip = tooltip;
            return this;
        }

        public TeamcraftButton build() {
            TeamcraftButton button = new TeamcraftButton(this.x, this.y, this.width, this.height,
                this.message, this.onPress);
            button.setTooltip(this.tooltip);
            return button;
        }
    }
}
