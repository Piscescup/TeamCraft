package io.github.piscescup.fabricmc.teamcraft.gui.widget;

import io.github.piscescup.fabricmc.teamcraft.gui.render.TeamcraftGuiTheme;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

/** Black input with a one-pixel focus border, sharing the screen's theme. */
@Environment(EnvType.CLIENT)
public final class TeamcraftEditBox extends EditBox {
    public TeamcraftEditBox(Font font, Component label) {
        //#if MC >= 260200
        super(font, label);
        //#else
        //$$ super(font, 0, 0, 20, 20, label);
        //#endif
        setBordered(false);
        setTextColor(TeamcraftGuiTheme.TEXT);
        setTextColorUneditable(TeamcraftGuiTheme.MUTED);
    }

    @Override
    //#if MC >= 260102
    public void extractWidgetRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    //#else
    //$$ public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    //#endif
        TeamcraftGuiTheme.frame(graphics, getX() - 4, getY() - 4, getRight() + 4, getBottom() + 4,
            TeamcraftGuiTheme.INPUT_BACKGROUND, isFocused() && this.active ? TeamcraftGuiTheme.ACCENT : TeamcraftGuiTheme.BORDER);
        //#if MC >= 260102
        super.extractWidgetRenderState(graphics, mouseX, mouseY, partialTick);
        //#else
        //$$ super.renderWidget(graphics, mouseX, mouseY, partialTick);
        //#endif
    }
}
