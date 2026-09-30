package io.github.piscescup.fabricmc.teamcraft.gui;

import io.github.piscescup.fabricmc.teamcraft.gui.network.ConfigSyncPayload;
import io.github.piscescup.fabricmc.teamcraft.text.Msg;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

/** Read-only details for one managed team, with an action to disband it. */
@Environment(EnvType.CLIENT)
public final class TeamDetailsScreen extends Screen {
    private static final int ROW_HEIGHT = 22;

    private final TeamcraftConfigScreen parent;
    private final TeamInfoData team;
    private double scrollOffset;
    private boolean waitingForServer;
    private int panelLeft;
    private int panelTop;
    private int panelRight;
    private int panelBottom;
    private int contentTop;
    private int contentBottom;

    public TeamDetailsScreen(TeamcraftConfigScreen parent, TeamInfoData team) {
        super(Component.translatable(TeamcraftTranslations.GUI_DETAILS_TITLE.key()));
        this.parent = parent;
        this.team = team;
    }

    @Override
    protected void init() {
        this.panelLeft = 8;
        this.panelTop = 8;
        this.panelRight = this.width - 8;
        this.panelBottom = this.height - 8;
        this.contentTop = this.panelTop + 42;
        this.contentBottom = this.panelBottom - 46;

        int gap = 6;
        int width = (this.panelRight - this.panelLeft - 16 - gap) / 2;
        int y = this.panelBottom - 29;
        addRenderableWidget(Button.builder(Component.translatable(TeamcraftTranslations.GUI_DETAILS_BACK.key()), ignored -> onClose())
            .bounds(this.panelLeft + 8, y, width, 20)
            .build());
        Button disband = addRenderableWidget(Button.builder(
            Component.translatable(TeamcraftTranslations.GUI_DETAILS_DISBAND.key()),
            ignored -> disbandTeam()
        ).bounds(this.panelLeft + 8 + width + gap, y, width, 20).build());
        disband.active = !this.waitingForServer;
        clampScroll();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(this.panelLeft, this.panelTop, this.panelRight, this.panelBottom, 0xF0101318);
        graphics.fill(this.panelLeft, this.panelTop, this.panelRight, this.panelTop + 34, 0xFF1B2128);
        graphics.horizontalLine(this.panelLeft, this.panelRight, this.panelBottom - 38, 0xFF39424C);
        graphics.centeredText(this.font, this.title, (this.panelLeft + this.panelRight) / 2,
            this.panelTop + 12, 0xFFFFFFFF);

        graphics.enableScissor(this.panelLeft + 10, this.contentTop, this.panelRight - 10, this.contentBottom);
        int y = this.contentTop - (int) this.scrollOffset;
        drawRow(graphics, y, Component.translatable(TeamcraftTranslations.GUI_DETAILS_NAME.key()),
            this.team.displayName().copy().withColor(this.team.color().textColor()));
        y += ROW_HEIGHT + 4;
        drawRow(graphics, y, Component.translatable(TeamcraftTranslations.GUI_DETAILS_COLOR.key()),
            Msg.colorName(this.team.color()).copy().withColor(this.team.color().textColor()));
        y += ROW_HEIGHT + 10;
        graphics.text(this.font, Component.translatable(TeamcraftTranslations.GUI_DETAILS_MEMBERS.key(), this.team.members().size())
            .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD), this.panelLeft + 18, y + 6, 0xFFFFFFFF);
        y += ROW_HEIGHT + 4;
        for (int i = 0; i < this.team.members().size(); i++) {
            int rowY = y + i * (ROW_HEIGHT + 2);
            graphics.fill(this.panelLeft + 14, rowY, this.panelRight - 14, rowY + ROW_HEIGHT, 0x8020272E);
            graphics.text(this.font, Component.translatable(
                TeamcraftTranslations.GUI_DETAILS_MEMBER.key(), i + 1, this.team.members().get(i)
            ), this.panelLeft + 22, rowY + 7, 0xFFE6E6E6);
        }
        graphics.disableScissor();
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    private void drawRow(GuiGraphicsExtractor graphics, int y, Component label, Component value) {
        graphics.fill(this.panelLeft + 14, y, this.panelRight - 14, y + ROW_HEIGHT, 0x8020272E);
        graphics.text(this.font, label.copy().withStyle(ChatFormatting.GRAY), this.panelLeft + 22, y + 7, 0xFFE6E6E6);
        int valueX = this.panelLeft + Math.max(130, (this.panelRight - this.panelLeft) / 3);
        graphics.text(this.font, value, valueX, y + 7, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (mouseX < this.panelLeft || mouseX >= this.panelRight
            || mouseY < this.contentTop || mouseY >= this.contentBottom || maxScroll() <= 0) {
            return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
        }
        this.scrollOffset -= vertical * (ROW_HEIGHT + 2);
        clampScroll();
        return true;
    }

    @Override
    public void onClose() {
        this.minecraft.gui.setScreen(this.parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean isInGameUi() {
        return true;
    }

    public void handleServerResponse(ConfigSyncPayload payload) {
        this.waitingForServer = false;
        this.minecraft.gui.setScreen(this.parent);
        this.parent.handleServerResponse(payload);
    }

    private void disbandTeam() {
        this.waitingForServer = true;
        rebuildWidgets();
        if (!TeamcraftConfigClient.disbandTeam(this.team.id())) {
            this.waitingForServer = false;
            rebuildWidgets();
        }
    }

    private int contentHeight() {
        return (ROW_HEIGHT + 4) * 2 + ROW_HEIGHT + 4 + this.team.members().size() * (ROW_HEIGHT + 2);
    }

    private int maxScroll() {
        return Math.max(0, contentHeight() - (this.contentBottom - this.contentTop));
    }

    private void clampScroll() {
        this.scrollOffset = Math.clamp(this.scrollOffset, 0, maxScroll());
    }
}
