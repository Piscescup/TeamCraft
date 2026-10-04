package io.github.piscescup.fabricmc.teamcraft.gui;

import io.github.piscescup.fabricmc.teamcraft.gui.widget.TeamcraftButton;
import io.github.piscescup.fabricmc.teamcraft.gui.render.TeamcraftGuiTheme;

import io.github.piscescup.fabricmc.teamcraft.config.TeamInfoData;
import io.github.piscescup.fabricmc.teamcraft.gui.network.ConfigSyncPayload;
import io.github.piscescup.fabricmc.teamcraft.gui.tab.TeamcraftTab;
import io.github.piscescup.fabricmc.teamcraft.text.Msg;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

/** Read-only details for one managed team, with an action to disband it. */
@Environment(EnvType.CLIENT)
public final class TeamDetailsScreen extends Screen implements TeamcraftConfigResponseReceiver {
    private static final int ROW_HEIGHT = 22;

    private final TeamcraftTab parent;
    private final TeamInfoData team;
    private double scrollOffset;
    private boolean waitingForServer;
    private int panelLeft;
    private int panelTop;
    private int panelRight;
    private int panelBottom;
    private int contentTop;
    private int contentBottom;

    public TeamDetailsScreen(TeamcraftTab parent, TeamInfoData team) {
        super(Component.translatable(TeamcraftTranslations.GUI_DETAILS_TITLE.key()));
        this.parent = parent;
        this.team = team;
    }

    @Override
    protected void init() {
        this.panelLeft = 10;
        this.panelTop = 8;
        this.panelRight = this.width - 10;
        this.panelBottom = this.height - 8;
        this.contentTop = 36;
        this.contentBottom = this.panelBottom - 35;

        int gap = 6;
        int width = Math.min(150, (this.panelRight - this.panelLeft - gap) / 2);
        int y = this.panelBottom - 22;
        addRenderableWidget(TeamcraftButton.themedBuilder(Component.translatable(TeamcraftTranslations.GUI_DETAILS_BACK.key()), ignored -> onClose())
            .bounds(this.panelLeft, y, width, 20)
            .build());
        Button disband = addRenderableWidget(TeamcraftButton.themedBuilder(
            Component.translatable(TeamcraftTranslations.GUI_DETAILS_DISBAND.key()),
            ignored -> disbandTeam()
        ).bounds(this.panelLeft + width + gap, y, width, 20).build());
        disband.active = !this.waitingForServer;
        clampScroll();
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        TeamcraftGuiTheme.screen(graphics, this.font, this.title, this.width, this.height,
            this.panelBottom - 29);

        graphics.enableScissor(this.panelLeft + 10, this.contentTop, this.panelRight - 10, this.contentBottom);
        int y = this.contentTop - (int) this.scrollOffset;
        drawRow(graphics, y, Component.translatable(TeamcraftTranslations.GUI_DETAILS_NAME.key()),
            this.team.displayName().copy().withColor(this.team.color().textColor()));
        y += ROW_HEIGHT + 4;
        drawRow(graphics, y, Component.translatable(TeamcraftTranslations.GUI_DETAILS_COLOR.key()),
            Msg.colorName(this.team.color()).copy().withColor(this.team.color().textColor()));
        y += ROW_HEIGHT + 10;
        graphics.text(this.font, Component.translatable(TeamcraftTranslations.GUI_DETAILS_MEMBERS.key(), this.team.members().size())
            .withStyle(ChatFormatting.BOLD).getVisualOrderText(), this.panelLeft + 18, y + 6, 0xFFFFFFFF);
        y += ROW_HEIGHT + 4;
        for (int i = 0; i < this.team.members().size(); i++) {
            int rowY = y + i * (ROW_HEIGHT + 2);
            TeamcraftGuiTheme.row(graphics, this.panelLeft + 10, rowY, this.panelRight - 10,
                rowY + ROW_HEIGHT, mouseX >= this.panelLeft + 10 && mouseX < this.panelRight - 10
                    && mouseY >= rowY && mouseY < rowY + ROW_HEIGHT);
            graphics.text(this.font, Component.translatable(
                TeamcraftTranslations.GUI_DETAILS_MEMBER.key(), i + 1, this.team.members().get(i)
            ).getVisualOrderText(), this.panelLeft + 22, rowY + 7, 0xFFE6E6E6);
        }
        graphics.disableScissor();
        TeamcraftGuiTheme.scrollbar(graphics, this.panelRight, this.contentTop,
            this.contentBottom, contentHeight(), this.scrollOffset);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    //#if MC >= 260102
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    //#else
    //$$ public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    //#endif
    }

    private void drawRow(GuiGraphicsExtractor graphics, int y, Component label, Component value) {
        TeamcraftGuiTheme.row(graphics, this.panelLeft + 10, y, this.panelRight - 10,
            y + ROW_HEIGHT, false);
        graphics.text(this.font, label.copy().withStyle(ChatFormatting.GRAY), this.panelLeft + 22, y + 7, 0xFFE6E6E6);
        int valueX = this.panelLeft + (this.panelRight - this.panelLeft) / 2;
        var lines = this.font.split(value, Math.max(1, this.panelRight - valueX - 14));
        if (!lines.isEmpty()) {
            graphics.text(this.font, lines.getFirst(), valueX, y + 7, 0xFFFFFFFF);
        }
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

    @Override
    public void handleServerResponse(ConfigSyncPayload payload) {
        this.waitingForServer = false;
        //#if MC >= 260200
        this.minecraft.gui.setScreen(this.parent);
        //#else
        //$$ this.minecraft.setScreen(this.parent);
        //#endif
        this.parent.handleServerResponse(payload);
    }

    private void disbandTeam() {
        this.waitingForServer = true;
        rebuildWidgets();
        if (!TeamcraftConfigClient.disbandTeam(this.team.id())) {
            this.waitingForServer = false;
            rebuildWidgets();
            TeamcraftFeedbackScreen.open(this,
                Component.translatable(TeamcraftTranslations.GUI_ERROR_SERVER_UNSUPPORTED.key()),
                TeamcraftFeedbackScreen.Type.ERROR);
        }
    }

    private int contentHeight() {
        return ROW_HEIGHT * 3 + 18 + this.team.members().size() * (ROW_HEIGHT + 2);
    }

    private int maxScroll() {
        return Math.max(0, contentHeight() - (this.contentBottom - this.contentTop));
    }

    private void clampScroll() {
        this.scrollOffset = Math.clamp(this.scrollOffset, 0, maxScroll());
    }
}
