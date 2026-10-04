package io.github.piscescup.fabricmc.teamcraft.gui.widget;

import io.github.piscescup.fabricmc.teamcraft.gui.layout.TeamcraftBounds;
import io.github.piscescup.fabricmc.teamcraft.gui.layout.TeamcraftScrollViewport;
import io.github.piscescup.fabricmc.teamcraft.gui.render.TeamcraftGuiTheme;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A page's content area: builds rows, positions controls, clips drawing and owns scrolling.
 * Shared configuration values remain outside this component; callbacks edit the page's draft.
 */
@Environment(EnvType.CLIENT)
public final class TeamcraftContentList {
    public static final int ROW_HEIGHT = 26;
    private static final int ROW_GAP = 2;
    private static final int HEADER_HEIGHT = 22;
    private static final int WIDGET_HEIGHT = 20;

    private final TeamcraftScrollViewport viewport = new TeamcraftScrollViewport();
    private final List<PositionedWidget> contentWidgets = new ArrayList<>();
    private final List<ContentRow> contentRows = new ArrayList<>();
    private final List<ContentHeader> contentHeaders = new ArrayList<>();
    private final List<CandidateRow> candidateRows = new ArrayList<>();
    private final Map<String, Boolean> expandedSections = new HashMap<>();
    private final Consumer<AbstractWidget> registerWidget;
    private final BooleanSupplier waitingForServer;
    private final BooleanSupplier canEditCandidates;
    private final Supplier<List<String>> candidates;
    private final Runnable rebuild;
    private Font font;
    private int cursorY;
    private int sectionDepth;
    private String draggedCandidate;
    private double candidateDragStartX;
    private double candidateDragStartY;
    private boolean candidateDragMoved;

    public TeamcraftContentList(Consumer<AbstractWidget> registerWidget, BooleanSupplier waitingForServer,
                               BooleanSupplier canEditCandidates, Supplier<List<String>> candidates, Runnable rebuild) {
        this.registerWidget = registerWidget;
        this.waitingForServer = waitingForServer;
        this.canEditCandidates = canEditCandidates;
        this.candidates = candidates;
        this.rebuild = rebuild;
    }

    /** Clears generated rows, but retains scroll, expanded sections and an ongoing drag. */
    public void begin(Font font, TeamcraftBounds bounds) {
        this.font = font;
        this.viewport.setBounds(bounds);
        this.cursorY = 0;
        this.sectionDepth = 0;
        this.contentWidgets.clear();
        this.contentRows.clear();
        this.contentHeaders.clear();
        this.candidateRows.clear();
    }

    public void finish() {
        this.viewport.setContentHeight(Math.max(0, this.cursorY - ROW_GAP));
        positionContentWidgets();
    }

    public TeamcraftScrollViewport viewport() { return this.viewport; }

    public void resetScroll() { this.viewport.reset(); }

    public void cancelDrag() {
        this.draggedCandidate = null;
        this.candidateDragMoved = false;
    }

    public void updateEnabledState() {
        positionContentWidgets();
    }

    public boolean mouseScrolled(double x, double y, double amount) {
        if (!this.viewport.bounds().contains(x, y) || this.viewport.maximum() <= 0 || amount == 0) {
            return false;
        }
        this.viewport.scrollBy(-amount * (ROW_HEIGHT + ROW_GAP));
        positionContentWidgets();
        return true;
    }

    public void registerCandidateRow(String playerName) {
        this.candidateRows.add(new CandidateRow(playerName, this.cursorY));
    }

    public void addHeader(String key) {
        this.contentHeaders.add(new ContentHeader(this.cursorY, Component.translatable(key)));
        this.cursorY += HEADER_HEIGHT;
    }

    public void addSection(String key, Runnable contents) {
        addSection(key, Component.translatable(key).withStyle(ChatFormatting.WHITE),
            key.substring(key.lastIndexOf('.') + 1), null, true, contents);
    }

    /** A dynamic section whose state follows its stable ID, not its displayed name. */
    public void addSection(String sectionKey, Component label, String subtitle,
                                    Component tooltip, boolean initiallyExpanded, Runnable contents) {
        boolean expanded = this.expandedSections.getOrDefault(sectionKey, initiallyExpanded);
        TeamcraftButton toggle = TeamcraftButton.themedBuilder(
            Component.literal(expanded ? "[-] " : "[+] ")
                .withStyle(expanded ? ChatFormatting.GRAY : ChatFormatting.RED)
                .append(label), ignored -> {
                    this.expandedSections.put(sectionKey, !expanded);
                    this.rebuild.run();
                }
        ).build().plain().subtitle(subtitle);
        toggle.hoverHint(Component.translatable((expanded
            ? TeamcraftTranslations.GUI_BUTTON_COLLAPSE : TeamcraftTranslations.GUI_BUTTON_EXPAND).key()));
        int indent = this.sectionDepth * 20;
        addContentWidget(toggle, indent, this.cursorY, contentWidth() - indent - 8, ROW_HEIGHT, true);
        if (tooltip != null) {
            this.contentRows.add(new ContentRow(this.cursorY, ROW_HEIGHT, List.of(), tooltip));
        }
        this.cursorY += ROW_HEIGHT + ROW_GAP;
        if (expanded) {
            this.sectionDepth++;
            try {
                contents.run();
            }
            finally {
                this.sectionDepth--;
            }
        }
    }

    public void addTextRow(Component text, Component tooltip) {
        int indent = this.sectionDepth * 20;
        List<FormattedCharSequence> lines = this.font.split(text, Math.max(40, contentWidth() - indent - 16));
        int height = Math.max(ROW_HEIGHT, lines.size() * (this.font.lineHeight + 1) + 10);
        this.contentRows.add(new ContentRow(this.cursorY, height, lines, tooltip, null, indent));
        this.cursorY += height + ROW_GAP;
    }

    public void addValueRow(String labelKey, Component value) {
        Component text = Component.translatable(labelKey).withStyle(ChatFormatting.GRAY)
            .append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
            .append(value);
        addTextRow(text, text);
    }

    public void addFullWidget(AbstractWidget widget, Component tooltip) {
        int indent = this.sectionDepth * 20;
        int width = contentWidth() - indent - 12;
        addContentWidget(widget, indent + 4, this.cursorY + 3, width, WIDGET_HEIGHT, true);
        this.contentRows.add(new ContentRow(this.cursorY, ROW_HEIGHT, List.of(), tooltip));
        this.cursorY += ROW_HEIGHT + ROW_GAP;
    }

    public void addTwoWidgets(AbstractWidget left, AbstractWidget right, Component tooltip) {
        int indent = this.sectionDepth * 20;
        int gap = 6;
        int width = (contentWidth() - indent - 12 - gap) / 2;
        addContentWidget(left, indent + 4, this.cursorY + 3, width, WIDGET_HEIGHT, true);
        addContentWidget(right, indent + 4 + width + gap, this.cursorY + 3, width, WIDGET_HEIGHT, true);
        this.contentRows.add(new ContentRow(this.cursorY, ROW_HEIGHT, List.of(), tooltip));
        this.cursorY += ROW_HEIGHT + ROW_GAP;
    }

    public void addLabeledWidget(String labelKey, AbstractWidget widget, Component tooltip) {
        addLabeledWidget(labelKey, widget, null, tooltip);
    }

    /** A compact action immediately after literal/dynamic text, not in the value column. */
    public void addInlineActionRow(Component text, AbstractWidget action, Component tooltip) {
        int indent = this.sectionDepth * 20;
        int available = Math.max(1, contentWidth() - indent - 16);
        int gap = 6;
        int actionWidth = Math.clamp(this.font.width(action.getMessage()) + 20, 1, Math.max(1, available * 2 / 3));
        int labelWidth = Math.max(1, Math.min(this.font.width(text), available - actionWidth - gap));
        List<FormattedCharSequence> lines = this.font.split(text, labelWidth);
        int height = Math.max(ROW_HEIGHT, lines.size() * (this.font.lineHeight + 1) + 10);
        addContentWidget(action, indent + 8 + labelWidth + gap, this.cursorY + (height - WIDGET_HEIGHT) / 2,
            actionWidth, WIDGET_HEIGHT, true);
        this.contentRows.add(new ContentRow(this.cursorY, height, lines, tooltip, null, indent));
        this.cursorY += height + ROW_GAP;
    }

    /** Optional trailing action (such as Reset), kept inside the same scrollable row. */
    public void addLabeledWidget(String labelKey, AbstractWidget widget, AbstractWidget trailing, Component tooltip) {
        if (widget instanceof TeamcraftCycleButton<?> cycle) {
            cycle.hoverHint(Component.translatable(TeamcraftTranslations.GUI_BUTTON_CHANGE.key(),
                Component.translatable(labelKey)));
        }
        int indent = this.sectionDepth * 20;
        int labelWidth = Math.clamp((contentWidth() - indent - 14) * 2L / 5, 60, 260);
        int widgetX = labelWidth + 6;
        int available = Math.max(1, contentWidth() - indent - widgetX - 8);
        int trailingWidth = trailing == null ? 0 : Math.clamp((available - 6) / 3, 1, 56);
        int gap = trailing == null ? 0 : 6;
        int widgetWidth = Math.clamp(available - trailingWidth - gap, 1, 240);
        addContentWidget(widget, indent + widgetX, this.cursorY + 3, widgetWidth, WIDGET_HEIGHT, true);
        if (trailing != null) {
            addContentWidget(trailing, indent + widgetX + widgetWidth + gap, this.cursorY + 3,
                trailingWidth, WIDGET_HEIGHT, true);
        }
        this.contentRows.add(new ContentRow(
            this.cursorY,
            ROW_HEIGHT,
            List.of(this.font.split(Component.translatable(labelKey), labelWidth - 8).getFirst()),
            tooltip,
            labelKey.substring(labelKey.lastIndexOf('.') + 1),
            indent
        ));
        this.cursorY += ROW_HEIGHT + ROW_GAP;
    }

    public void addHotkeyRow(String key, TeamcraftButton bind, TeamcraftButton clear,
                      TeamcraftButton reset, Component tooltip, boolean canClear, boolean canReset) {
        int available = contentWidth() - 12;
        int labelWidth = Math.min(260, available * 2 / 5);
        int resetWidth = Math.min(56, available / 6);
        int clearWidth = 20;
        int bindX = labelWidth + 6;
        int bindWidth = Math.max(1, available - bindX - clearWidth - resetWidth - 6);
        addContentWidget(bind, bindX, this.cursorY + 3, bindWidth, WIDGET_HEIGHT, true);
        addContentWidget(clear, bindX + bindWidth + 3, this.cursorY + 3, clearWidth, WIDGET_HEIGHT, canClear);
        addContentWidget(reset, bindX + bindWidth + clearWidth + 6, this.cursorY + 3, resetWidth, WIDGET_HEIGHT, canReset);
        this.contentRows.add(new ContentRow(this.cursorY, ROW_HEIGHT,
            List.of(this.font.split(Component.translatable(key), Math.max(1, labelWidth - 8)).getFirst()),
            tooltip, key.substring(key.lastIndexOf('.') + 1), 0));
        this.cursorY += ROW_HEIGHT + ROW_GAP;
    }

    private void addContentWidget(
        AbstractWidget widget,
        int xOffset,
        int baseY,
        int width,
        int height,
        boolean enabled
    ) {
        if (widget instanceof EditBox) {
            widget.setSize(Math.max(1, width - 8), height - 8);
            xOffset += 4;
            baseY += 4;
        }
        else {
            widget.setSize(Math.max(1, width), height);
        }
        this.registerWidget.accept(widget);
        this.contentWidgets.add(new PositionedWidget(widget, xOffset, baseY, enabled));
    }


    private void positionContentWidgets() {
        for (PositionedWidget positioned : this.contentWidgets) {
            AbstractWidget widget = positioned.widget;
            int y = this.viewport.toScreenY(positioned.baseY);
            widget.setPosition(this.viewport.bounds().left() + positioned.xOffset, y);
            int inset = widget instanceof EditBox ? 4 : 0;
            boolean fullyVisible = this.viewport.fullyVisible(positioned.baseY - inset, widget.getHeight() + inset * 2)
                && widget.getX() - inset >= this.viewport.bounds().left()
                && widget.getRight() + inset <= this.viewport.bounds().right();
            widget.visible = fullyVisible;
            widget.active = fullyVisible && positioned.enabled && !this.waitingForServer.getAsBoolean();
        }
    }


    public Component draw(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (this.viewport.bounds().width() <= 0 || this.viewport.bounds().height() <= 0) {
            return null;
        }
        Component hoveredTooltip = null;
        graphics.enableScissor(this.viewport.bounds().left(), this.viewport.bounds().top(),
            this.viewport.bounds().right(), this.viewport.bounds().bottom());
        try {
            for (ContentHeader header : this.contentHeaders) {
                int y = this.viewport.toScreenY(header.baseY);
                if (!this.viewport.intersects(header.baseY, HEADER_HEIGHT)) {
                    continue;
                }
                TeamcraftGuiTheme.header(graphics, this.font, header.text,
                    this.viewport.bounds().left(), y, this.viewport.bounds().right(), HEADER_HEIGHT);
            }

            for (ContentRow row : this.contentRows) {
                int y = this.viewport.toScreenY(row.baseY);
                if (!this.viewport.intersects(row.baseY, row.height)) {
                    continue;
                }
                boolean hovered = this.viewport.bounds().contains(mouseX, mouseY)
                    && mouseY >= y && mouseY < y + row.height;
                TeamcraftGuiTheme.row(graphics, this.viewport.bounds().left(), y, this.viewport.bounds().right() - 8,
                    y + row.height, hovered);
                if (!row.textLines.isEmpty()) {
                    int textY = y + (row.identifier != null ? 3
                        : row.textLines.size() == 1 ? (row.height - this.font.lineHeight) / 2 : 5);
                    int textX = this.viewport.bounds().left() + 8 + row.indent();
                    for (FormattedCharSequence line : row.textLines) {
                        graphics.text(this.font, line, textX, textY, TeamcraftGuiTheme.ROW_TEXT);
                        textY += this.font.lineHeight + 1;
                    }
                    if (row.identifier != null) {
                        TeamcraftGuiTheme.smallText(graphics, this.font, row.identifier,
                            textX, y + 15, contentWidth() * 2 / 5 - 16);
                    }
                }
                if (hovered && row.tooltip != null) {
                    hoveredTooltip = row.tooltip;
                }
            }

            for (PositionedWidget positioned : this.contentWidgets) {
                if (positioned.widget.visible) {
                    //#if MC >= 260102
                    positioned.widget.extractRenderState(graphics, mouseX, mouseY, partialTick);
                    //#else
                    //$$ positioned.widget.render(graphics, mouseX, mouseY, partialTick);
                    //#endif
                    AbstractWidget widget = positioned.widget;
                    if (widget instanceof Button && this.viewport.bounds().contains(mouseX, mouseY)
                        && mouseX >= widget.getX() && mouseX < widget.getRight()
                        && mouseY >= widget.getY() && mouseY < widget.getBottom()) {
                        // Even disabled buttons suppress the complete row description.
                        hoveredTooltip = widget instanceof TeamcraftButton button ? button.hoverHint() : null;
                    }
                }
            }

            if (this.candidateDragMoved && this.draggedCandidate != null) {
                for (CandidateRow row : this.candidateRows) {
                    if (!row.playerName.equals(this.draggedCandidate)) {
                        continue;
                    }
                    int y = this.viewport.toScreenY(row.baseY);
                    if (this.viewport.intersects(row.baseY, ROW_HEIGHT)) {
                        graphics.fill(this.viewport.bounds().left() + 1, y, this.viewport.bounds().left() + 4,
                            y + ROW_HEIGHT, TeamcraftGuiTheme.DRAG_MARKER);
                    }
                    break;
                }
            }
        }
        finally {
            graphics.disableScissor();
        }
        TeamcraftGuiTheme.scrollbar(graphics, this.viewport.bounds().right(), this.viewport.bounds().top(),
            this.viewport.bounds().bottom(), this.viewport.contentHeight(), this.viewport.offset());
        return hoveredTooltip;
    }


    public boolean mousePressed(double mouseX, double mouseY, int button) {
        if (button != 0 || !this.canEditCandidates.getAsBoolean()
            || this.candidateRows.isEmpty() || this.waitingForServer.getAsBoolean()) {
            return false;
        }
        CandidateRow row = candidateRowAt(mouseX, mouseY);
        if (row == null) {
            return false;
        }
        this.draggedCandidate = row.playerName;
        this.candidateDragStartX = mouseX;
        this.candidateDragStartY = mouseY;
        this.candidateDragMoved = false;
        return true;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button) {
        if (button != 0 || this.draggedCandidate == null) {
            return false;
        }
        double deltaX = mouseX - this.candidateDragStartX;
        double deltaY = mouseY - this.candidateDragStartY;
        if (!this.candidateDragMoved && deltaX * deltaX + deltaY * deltaY < 9.0) {
            return true;
        }
        this.candidateDragMoved = true;

        if (mouseY < this.viewport.bounds().top() + 12 && this.viewport.offset() > 0) {
            this.viewport.scrollBy(-6);
            positionContentWidgets();
        }
        else if (mouseY > this.viewport.bounds().bottom() - 12 && this.viewport.offset() < maxScroll()) {
            this.viewport.scrollBy(6);
            positionContentWidgets();
        }

        int targetIndex = nearestCandidateIndex(mouseY);
        int currentIndex = this.candidates.get().indexOf(this.draggedCandidate);
        if (targetIndex >= 0 && currentIndex >= 0 && targetIndex != currentIndex) {
            String candidate = this.candidates.get().remove(currentIndex);
            this.candidates.get().add(targetIndex, candidate);
            this.rebuild.run();
        }
        return true;
    }

    public boolean mouseReleased(int button) {
        if (button != 0 || this.draggedCandidate == null) {
            return false;
        }
        String candidate = this.draggedCandidate;
        boolean moved = this.candidateDragMoved;
        this.draggedCandidate = null;
        this.candidateDragMoved = false;
        if (!moved) {
            this.candidates.get().remove(candidate);
        }
        this.rebuild.run();
        return true;
    }

    private CandidateRow candidateRowAt(double mouseX, double mouseY) {
        if (mouseX < this.viewport.bounds().left() + 4 || mouseX >= this.viewport.bounds().right() - 4
            || mouseY < this.viewport.bounds().top() || mouseY >= this.viewport.bounds().bottom()) {
            return null;
        }
        for (CandidateRow row : this.candidateRows) {
            int y = this.viewport.toScreenY(row.baseY);
            if (this.viewport.fullyVisible(row.baseY, ROW_HEIGHT)
                && mouseY >= y && mouseY < y + ROW_HEIGHT) {
                return row;
            }
        }
        return null;
    }

    private int nearestCandidateIndex(double mouseY) {
        int nearest = -1;
        double nearestDistance = Double.MAX_VALUE;
        for (int i = 0; i < this.candidateRows.size(); i++) {
            CandidateRow row = this.candidateRows.get(i);
            double centerY = this.viewport.bounds().top() + row.baseY - this.viewport.offset() + ROW_HEIGHT / 2.0;
            double distance = Math.abs(mouseY - centerY);
            if (distance < nearestDistance) {
                nearest = i;
                nearestDistance = distance;
            }
        }
        return nearest;
    }


    private int maxScroll() { return this.viewport.maximum(); }
    private int contentWidth() { return this.viewport.bounds().width(); }

    private record PositionedWidget(AbstractWidget widget, int xOffset, int baseY, boolean enabled) {
    }

    private record ContentRow(int baseY, int height, List<FormattedCharSequence> textLines,
                              Component tooltip, String identifier, int indent) {
        private ContentRow(int baseY, int height, List<FormattedCharSequence> textLines, Component tooltip) {
            this(baseY, height, textLines, tooltip, null, 0);
        }
    }

    private record ContentHeader(int baseY, Component text) {
    }

    private record CandidateRow(String playerName, int baseY) {
    }

}
