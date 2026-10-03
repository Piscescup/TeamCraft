package io.github.piscescup.fabricmc.teamcraft.gui;

import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.List;

/** Modal dialog shown when the server rejects a TeamCraft GUI action. */
@Environment(EnvType.CLIENT)
public final class PermissionDeniedScreen extends Screen {
    private static final int MAX_DIALOG_WIDTH = 320;
    private static final int DIALOG_HEIGHT = 124;

    private final Screen parent;
    private final Component message;
    private int dialogLeft;
    private int dialogTop;
    private int dialogRight;
    private int dialogBottom;

    public PermissionDeniedScreen(Screen parent) {
        super(Component.translatable(TeamcraftTranslations.GUI_TITLE.key()));
        this.parent = parent;
        this.message = Component.translatable(TeamcraftTranslations.GUI_ERROR_PERMISSION.key());
    }

    public static void open(Screen parent) {
        Minecraft client = Minecraft.getInstance();
        //#if MC >= 260200
        client.gui.setScreen(new PermissionDeniedScreen(parent));
        //#else
        //$$ client.setScreen(new PermissionDeniedScreen(parent));
        //#endif
    }

    @Override
    protected void init() {
        int dialogWidth = Math.min(MAX_DIALOG_WIDTH, Math.max(200, this.width - 32));
        int dialogHeight = Math.min(DIALOG_HEIGHT, Math.max(96, this.height - 32));
        this.dialogLeft = (this.width - dialogWidth) / 2;
        this.dialogTop = (this.height - dialogHeight) / 2;
        this.dialogRight = this.dialogLeft + dialogWidth;
        this.dialogBottom = this.dialogTop + dialogHeight;

        int buttonWidth = Math.min(100, dialogWidth - 32);
        addRenderableWidget(Button.builder(
            Component.translatable(TeamcraftTranslations.GUI_DONE.key()),
            ignored -> onClose()
        ).bounds(
            (this.width - buttonWidth) / 2,
            this.dialogBottom - 30,
            buttonWidth,
            20
        ).build());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (this.parent != null) {
            this.parent.extractRenderState(graphics, mouseX, mouseY, partialTick);
        }

        graphics.fill(0, 0, this.width, this.height, 0x99000000);
        graphics.fill(
            this.dialogLeft - 2,
            this.dialogTop - 2,
            this.dialogRight + 2,
            this.dialogBottom + 2,
            0xFFAA2020
        );
        graphics.fill(
            this.dialogLeft,
            this.dialogTop,
            this.dialogRight,
            this.dialogBottom,
            0xFF17191F
        );
        graphics.fill(
            this.dialogLeft,
            this.dialogTop + 31,
            this.dialogRight,
            this.dialogTop + 32,
            0xFF6E2A2A
        );
        graphics.centeredText(
            this.font,
            this.title,
            (this.dialogLeft + this.dialogRight) / 2,
            this.dialogTop + 11,
            0xFFFF5555
        );

        List<FormattedCharSequence> lines = this.font.split(this.message, this.dialogRight - this.dialogLeft - 32);
        int firstLineY = this.dialogTop + 45;
        for (int index = 0; index < lines.size() && index < 3; index++) {
            FormattedCharSequence line = lines.get(index);
            int x = (this.width - this.font.width(line)) / 2;
            graphics.text(this.font, line, x, firstLineY + index * 12, 0xFFFF7777);
        }

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    //#if MC < 12108
    //$$ /** The parent screen and modal panel provide the complete background. */
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

    //#if MC >= 12110
    @Override
    public boolean isInGameUi() {
        return true;
    }
    //#endif
}
