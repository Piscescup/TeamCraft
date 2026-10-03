package io.github.piscescup.fabricmc.teamcraft.gui;

import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Loading screen used while Mod Menu requests the server-owned configuration. */
@Environment(EnvType.CLIENT)
final class TeamcraftModMenuScreen extends Screen {
    private final Screen parent;
    private boolean requestSent;

    TeamcraftModMenuScreen(Screen parent) {
        super(Component.translatable(TeamcraftTranslations.GUI_TITLE.key()));
        this.parent = parent;
    }

    @Override
    protected void init() {
        if (!this.requestSent) {
            this.requestSent = true;
            TeamcraftConfigClient.requestOpen(this.parent);
        }

        int buttonWidth = Math.clamp(this.width - 40, 100, 160);
        addRenderableWidget(Button.builder(
            Component.translatable(TeamcraftTranslations.GUI_CLOSE.key()),
            ignored -> onClose()
        ).bounds((this.width - buttonWidth) / 2, this.height / 2 + 28, buttonWidth, 20).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0xD0101318);
        graphics.centeredText(this.font, this.title, this.width / 2, this.height / 2 - 28, 0xFFFFFFFF);
        graphics.centeredText(
            this.font,
            Component.translatable(TeamcraftTranslations.GUI_LOADING.key()),
            this.width / 2,
            this.height / 2 - 4,
            0xFFAAAAAA
        );
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    //#if MC < 12108
    //$$ /** This screen draws its own complete background. */
    //$$ @Override
    //$$ public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    //$$ }
    //#endif

    @Override
    public void onClose() {
        //#if MC >= 260200
        this.minecraft.gui.setScreen(this.parent);
        //#else
        //$$ this.minecraft.setScreen(this.parent);
        //#endif
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
