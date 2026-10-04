package io.github.piscescup.fabricmc.teamcraft.gui;

import io.github.piscescup.fabricmc.teamcraft.gui.layout.TeamcraftBounds;
import io.github.piscescup.fabricmc.teamcraft.gui.layout.TeamcraftScrollViewport;
import io.github.piscescup.fabricmc.teamcraft.gui.network.ConfigSyncPayload;
import io.github.piscescup.fabricmc.teamcraft.gui.render.TeamcraftGuiTheme;
import io.github.piscescup.fabricmc.teamcraft.gui.render.TeamcraftGuiLayers;
import io.github.piscescup.fabricmc.teamcraft.gui.widget.TeamcraftButton;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Objects;

/**
 * Shared modal Screen: dimmed parent, centered frame, wrapped message and Done/Escape return.
 * Subclasses supply the title, message and accent color, not layout or navigation code.
 */
@Environment(EnvType.CLIENT)
public abstract class TeamcraftDialogScreen extends Screen implements TeamcraftConfigResponseReceiver {
    protected final Screen parent;
    private final Component message;
    private final TeamcraftScrollViewport content = new TeamcraftScrollViewport();
    private TeamcraftBounds dialog;
    private List<FormattedCharSequence> titleLines = List.of();
    private List<FormattedCharSequence> messageLines = List.of();
    private int separatorY;

    protected TeamcraftDialogScreen(Screen parent, Component title, Component message) {
        super(Objects.requireNonNull(title, "title"));
        // Replacing a result must not create a stack of old result dialogs.
        while (parent instanceof TeamcraftDialogScreen previous) {
            parent = previous.parent;
        }
        this.parent = parent;
        this.message = Objects.requireNonNull(message, "message").copy();
    }

    /** ARGB color used for the border, title and message. */
    protected abstract int accentColor();

    protected Component doneLabel() {
        return Component.translatable(TeamcraftTranslations.GUI_DONE.key());
    }

    /** Allows asynchronous page results to replace their own modal without reopening a closed page. */
    public final boolean hasParent(Screen screen) {
        return this.parent == screen;
    }

    protected static void display(Screen screen) {
        Minecraft client = Minecraft.getInstance();
        //#if MC >= 260200
        client.gui.setScreen(screen);
        //#else
        //$$ client.setScreen(screen);
        //#endif
    }

    @Override
    protected void init() {
        if (this.parent != null && (this.parent.width != this.width || this.parent.height != this.height)) {
            //#if MC >= 12111
            this.parent.resize(this.width, this.height);
            //#else
            //$$ this.parent.resize(this.minecraft, this.width, this.height);
            //#endif
        }
        int dialogWidth = Math.clamp(this.width - 32, 1, 360);
        int textWidth = Math.max(1, dialogWidth - 32);
        this.titleLines = this.font.split(this.title, textWidth);
        this.messageLines = this.font.split(this.message, textWidth);
        int headerHeight = 20 + this.titleLines.size() * lineHeight();
        int dialogHeight = Math.max(1, Math.clamp(headerHeight + 56 + (long) this.messageLines.size() * lineHeight(), 124, this.height - 32));
        int left = (this.width - dialogWidth) / 2;
        int top = (this.height - dialogHeight) / 2;
        this.dialog = new TeamcraftBounds(left, top, left + dialogWidth, top + dialogHeight);

        int buttonWidth = Math.clamp(dialogWidth - 32, 1, 100);
        int buttonHeight = Math.min(20, dialogHeight);
        int bottomPadding = Math.min(10, (dialogHeight - buttonHeight) / 2);
        int buttonY = this.dialog.bottom() - bottomPadding - buttonHeight;
        this.separatorY = Math.min(top + headerHeight, buttonY);
        int contentTop = Math.min(this.separatorY + 12, buttonY);
        int contentBottom = Math.max(contentTop, buttonY - 10);
        int horizontalPadding = Math.min(16, dialogWidth / 2);
        this.content.setBounds(new TeamcraftBounds(left + horizontalPadding, contentTop,
            this.dialog.right() - horizontalPadding, contentBottom));
        this.content.setContentHeight(this.messageLines.size() * lineHeight());

        addRenderableWidget(TeamcraftButton.themedBuilder(doneLabel(), ignored -> onClose())
            .bounds((this.width - buttonWidth) / 2, buttonY, buttonWidth, buttonHeight).build());
    }

    private int lineHeight() {
        return this.font.lineHeight + 3;
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (this.parent != null) {
            this.parent.extractRenderState(graphics, -1, -1, partialTick);
        }
        TeamcraftGuiLayers.draw(graphics, TeamcraftGuiLayers.Layer.DIALOG,
            () -> drawDialog(graphics, mouseX, mouseY, partialTick));
    }

    private void drawDialog(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0x99000000);
        int color = accentColor();
        TeamcraftGuiTheme.frame(graphics, this.dialog.left(), this.dialog.top(), this.dialog.right(),
            this.dialog.bottom(), TeamcraftGuiTheme.PANEL, color);
        graphics.fill(this.dialog.left(), this.separatorY, this.dialog.right(),
            Math.min(this.dialog.bottom(), this.separatorY + 1), TeamcraftGuiTheme.BORDER);

        graphics.enableScissor(this.dialog.left(), this.dialog.top(), this.dialog.right(), this.separatorY);
        try {
            for (int index = 0; index < this.titleLines.size(); index++) {
                FormattedCharSequence line = this.titleLines.get(index);
                int x = (this.width - this.font.width(line)) / 2;
                int y = this.dialog.top() + 10 + index * lineHeight();
                graphics.text(this.font, line, x, y, color);
            }
        } finally {
            graphics.disableScissor();
        }
        var bounds = this.content.bounds();
        graphics.enableScissor(bounds.left(), bounds.top(), bounds.right(), bounds.bottom());
        try {
            int padding = Math.max(0, (bounds.height() - this.content.contentHeight()) / 2);
            for (int index = 0; index < this.messageLines.size(); index++) {
                FormattedCharSequence line = this.messageLines.get(index);
                int x = (this.width - this.font.width(line)) / 2;
                int y = this.content.toScreenY(index * lineHeight()) + padding;
                graphics.text(this.font, line, x, y, color);
            }
        } finally {
            graphics.disableScissor();
        }
        TeamcraftGuiTheme.scrollbar(graphics, this.dialog.right() - 4, bounds.top(), bounds.bottom(),
            this.content.contentHeight(), this.content.offset());
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    //#if MC >= 260102
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    //#else
    //$$ public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    //#endif
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (this.content.bounds().contains(mouseX, mouseY) && this.content.maximum() > 0) {
            this.content.scrollBy(-vertical * lineHeight());
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    @Override
    public void onClose() {
        display(this.parent);
    }

    @NonNull
    @Override
    public Component getNarrationMessage() {
        return this.title.copy().append("\n").append(this.message);
    }

    /** A late response still reaches the originating session instead of being lost in the modal. */
    @Override
    public void handleServerResponse(ConfigSyncPayload payload) {
        if (this.parent instanceof TeamcraftConfigResponseReceiver receiver) {
            display(this.parent);
            receiver.handleServerResponse(payload);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    //#if MC >= 12110
    @Override
    public boolean isInGameUi() {
        return this.parent != null && this.parent.isInGameUi();
    }
    //#endif
}
