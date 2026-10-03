package io.github.piscescup.fabricmc.teamcraft.gui;

import io.github.piscescup.fabricmc.teamcraft.gui.network.ConfigSyncPayload;
import io.github.piscescup.fabricmc.teamcraft.team.SplitMode;
import io.github.piscescup.fabricmc.teamcraft.text.Msg;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
//#if MC >= 12110
import net.minecraft.client.input.MouseButtonEvent;
//#endif
import net.minecraft.network.chat.Component;
//#if MC < 12103
//$$ import net.minecraft.client.resources.sounds.SimpleSoundInstance;
//$$ import net.minecraft.sounds.SoundEvents;
//#endif
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftColor;
import net.minecraft.util.FormattedCharSequence;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Jade-inspired TeamCraft control center: categories stay on the left while
 * the selected category's scrollable controls are shown on the right.
 */
@Environment(EnvType.CLIENT)
public final class TeamcraftConfigScreen extends Screen {
    private static final Duration TOOLTIP_DELAY = Duration.ofMillis(300);
    private static final int TOOLTIP_MAX_WIDTH = 220;
    private static final int ROW_HEIGHT = 24;
    private static final int ROW_GAP = 4;
    private static final int HEADER_HEIGHT = 22;
    private static final int WIDGET_HEIGHT = 20;

    private final Screen parent;
    private final boolean helpOnly;
    private final List<PositionedWidget> contentWidgets = new ArrayList<>();
    private final List<ContentRow> contentRows = new ArrayList<>();
    private final List<ContentHeader> contentHeaders = new ArrayList<>();
    private final List<Button> navigationButtons = new ArrayList<>();
    private final List<AbstractWidget> idleOnlyWidgets = new ArrayList<>();
    private final List<CandidateRow> candidateRows = new ArrayList<>();

    TeamcraftConfigPage page = TeamcraftConfigPage.TEAM_CONFIG;
    private double scrollOffset;
    private int contentHeight;
    private int cursorY;
    private int panelLeft;
    private int panelTop;
    private int panelRight;
    private int panelBottom;
    private int navigationWidth;
    private int contentLeft;
    private int contentRight;
    private int contentTop;
    private int contentBottom;

    boolean fixedTeamCount;
    String playersPerTeamText;
    String teamCountText;
    SplitMode mode;
    boolean friendlyFire;
    List<String> candidates;
    List<String> onlinePlayers;
    List<TeamcraftColor> configuredColors;
    TeamcraftColor selectedColor = TeamcraftColor.RED;
    List<String> configuredNames;
    String pendingName = "";

    TeamInfoData ownTeam;
    List<TeamInfoData> teams;
    String ownTeamName;
    TeamcraftColor ownTeamcraftColor = TeamcraftColor.WHITE;
    boolean ownTeamFriendlyFire;
    boolean waitingForServer;
    private ColorMenu colorMenu;
    private String draggedCandidate;
    private double candidateDragStartX;
    private double candidateDragStartY;
    private boolean candidateDragMoved;

    public TeamcraftConfigScreen(
        Screen parent,
        TeamcraftConfigData config,
        List<String> candidates,
        List<String> onlinePlayers,
        TeamInfoData ownTeam,
        List<TeamInfoData> teams
    ) {
        this(parent, config, candidates, onlinePlayers, ownTeam, teams, false);
    }

    private TeamcraftConfigScreen(
        Screen parent,
        TeamcraftConfigData config,
        List<String> candidates,
        List<String> onlinePlayers,
        TeamInfoData ownTeam,
        List<TeamInfoData> teams,
        boolean helpOnly
    ) {
        super(Component.translatable(
            helpOnly ? TeamcraftTranslations.TITLE_HELP.key() : TeamcraftTranslations.GUI_TITLE.key()
        ));
        this.parent = parent;
        this.helpOnly = helpOnly;
        loadConfig(config);
        loadCandidates(candidates, onlinePlayers);
        loadTeams(ownTeam, teams);
        if (helpOnly) {
            this.page = TeamcraftConfigPage.HELP;
        }
    }

    /** Creates a server-independent, read-only Help view for Mod Menu. */
    public static TeamcraftConfigScreen help(Screen parent) {
        return new TeamcraftConfigScreen(
            parent,
            TeamcraftConfigData.defaults(),
            List.of(),
            List.of(),
            null,
            List.of(),
            true
        );
    }

    @Override
    protected void init() {
        this.colorMenu = null;
        calculateBounds();
        this.contentWidgets.clear();
        this.contentRows.clear();
        this.contentHeaders.clear();
        this.navigationButtons.clear();
        this.idleOnlyWidgets.clear();
        this.candidateRows.clear();
        this.cursorY = 0;

        addNavigation();
        this.page.build(this);
        this.contentHeight = Math.max(0, this.cursorY - ROW_GAP);
        addFooter();
        clampScroll();
        positionContentWidgets();
        updateEnabledState();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(this.panelLeft, this.panelTop, this.panelRight, this.panelBottom, 0xF0101318);
        graphics.fill(this.panelLeft, this.panelTop, this.panelRight, this.panelTop + 34, 0xFF1B2128);
        graphics.fill(
            this.panelLeft,
            this.panelTop + 34,
            this.panelLeft + this.navigationWidth,
            this.panelBottom - 38,
            0xFF151A20
        );
        graphics.fill(
            this.panelLeft + this.navigationWidth,
            this.panelTop + 34,
            this.panelLeft + this.navigationWidth + 1,
            this.panelBottom - 38,
            0xFF39424C
        );
        graphics.fill(this.panelLeft, this.panelBottom - 38, this.panelRight, this.panelBottom - 37, 0xFF39424C);
        graphics.centeredText(this.font, this.title, (this.panelLeft + this.panelRight) / 2, this.panelTop + 12, 0xFFFFFFFF);

        graphics.enableScissor(this.contentLeft, this.contentTop, this.contentRight, this.contentBottom);
        extractContent(graphics, mouseX, mouseY, partialTick);
        graphics.disableScissor();
        extractScrollbar(graphics);

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        extractColorMenu(graphics, mouseX, mouseY);
    }

    //#if MC < 12108
    //$$ /**
    //$$  * Before 1.21.8, {@link Screen#render} draws the screen background before
    //$$  * its widgets. This screen draws its own translucent panel before calling
    //$$  * the superclass, so the vanilla blur would otherwise be applied to the
    //$$  * panel and its contents instead of only to the world behind it.
    //$$  */
    //$$ @Override
    //$$ public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    //$$     // The custom panel is the complete background for this in-game screen.
    //$$ }
    //#endif

    //#if MC >= 12110
    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.handleColorMenuClick(event.x(), event.y(), event.button())) {
            return true;
        }
        if (this.handleCandidatePress(event.x(), event.y(), event.button())) {
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (this.handleCandidateDrag(event.x(), event.y(), event.button())) {
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (this.handleCandidateRelease(event.button())) {
            return true;
        }
        return super.mouseReleased(event);
    }
    //#else
    //$$ @Override
    //$$ public boolean mouseClicked(double mouseX, double mouseY, int button) {
    //$$     if (this.handleColorMenuClick(mouseX, mouseY, button)) {
    //$$         return true;
    //$$     }
    //$$     if (this.handleCandidatePress(mouseX, mouseY, button)) {
    //$$         return true;
    //$$     }
    //$$     return super.mouseClicked(mouseX, mouseY, button);
    //$$ }
    //$$
    //$$ @Override
    //$$ public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
    //$$     if (this.handleCandidateDrag(mouseX, mouseY, button)) {
    //$$         return true;
    //$$     }
    //$$     return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    //$$ }
    //$$
    //$$ @Override
    //$$ public boolean mouseReleased(double mouseX, double mouseY, int button) {
    //$$     if (this.handleCandidateRelease(button)) {
    //$$         return true;
    //$$     }
    //$$     return super.mouseReleased(mouseX, mouseY, button);
    //$$ }
    //#endif

    private boolean handleCandidatePress(double mouseX, double mouseY, int button) {
        if (button != 0 || this.page != TeamcraftConfigPage.TEAM_CONFIG || this.waitingForServer) {
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

    private boolean handleCandidateDrag(double mouseX, double mouseY, int button) {
        if (button != 0 || this.draggedCandidate == null) {
            return false;
        }
        double deltaX = mouseX - this.candidateDragStartX;
        double deltaY = mouseY - this.candidateDragStartY;
        if (!this.candidateDragMoved && deltaX * deltaX + deltaY * deltaY < 9.0) {
            return true;
        }
        this.candidateDragMoved = true;

        if (mouseY < this.contentTop + 12 && this.scrollOffset > 0) {
            this.scrollOffset -= 6;
            clampScroll();
            positionContentWidgets();
        }
        else if (mouseY > this.contentBottom - 12 && this.scrollOffset < maxScroll()) {
            this.scrollOffset += 6;
            clampScroll();
            positionContentWidgets();
        }

        int targetIndex = nearestCandidateIndex(mouseY);
        int currentIndex = this.candidates.indexOf(this.draggedCandidate);
        if (targetIndex >= 0 && currentIndex >= 0 && targetIndex != currentIndex) {
            String candidate = this.candidates.remove(currentIndex);
            this.candidates.add(targetIndex, candidate);
            rebuildWidgets();
        }
        return true;
    }

    private boolean handleCandidateRelease(int button) {
        if (button != 0 || this.draggedCandidate == null) {
            return false;
        }
        String candidate = this.draggedCandidate;
        boolean moved = this.candidateDragMoved;
        this.draggedCandidate = null;
        this.candidateDragMoved = false;
        if (!moved) {
            this.candidates.remove(candidate);
        }
        rebuildWidgets();
        return true;
    }

    private CandidateRow candidateRowAt(double mouseX, double mouseY) {
        if (mouseX < this.contentLeft + 4 || mouseX >= this.contentRight - 4
            || mouseY < this.contentTop || mouseY >= this.contentBottom) {
            return null;
        }
        for (CandidateRow row : this.candidateRows) {
            int y = this.contentTop + row.baseY - (int) this.scrollOffset;
            if (y >= this.contentTop && y + ROW_HEIGHT <= this.contentBottom
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
            double centerY = this.contentTop + row.baseY - this.scrollOffset + ROW_HEIGHT / 2.0;
            double distance = Math.abs(mouseY - centerY);
            if (distance < nearestDistance) {
                nearest = i;
                nearestDistance = distance;
            }
        }
        return nearest;
    }

    /**
     * Handles one click against the open color menu.
     *
     * @return whether the click was consumed by the menu
     */
    private boolean handleColorMenuClick(double x, double y, int button) {
        if (this.colorMenu != null) {
            ColorMenu menu = this.colorMenu;
            this.colorMenu = null;
            if (button == 0 && menu.contains(x, y)) {
                int column = (int) ((x - menu.x) / menu.columnWidth);
                int row = (int) ((y - menu.y) / menu.rowHeight);
                int index = row * menu.columns + column;
                TeamcraftColor[] colors = TeamcraftColor.values();
                if (index >= 0 && index < colors.length) {
                    //#if MC >= 12103
                    AbstractWidget.playButtonClickSound(this.minecraft.getSoundManager());
                    //#else
                    //$$ this.minecraft.getSoundManager()
                    //$$     .play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    //#endif
                    menu.onChanged.accept(colors[index]);
                    rebuildWidgets();
                    return true;
                }
            }
            return menu.anchorContains(x, y);
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        this.colorMenu = null;
        if (super.mouseScrolled(mouseX, mouseY, horizontal, vertical)) {
            return true;
        }
        if (mouseX < this.contentLeft || mouseX >= this.contentRight
            || mouseY < this.contentTop || mouseY >= this.contentBottom
            || maxScroll() <= 0) {
            return false;
        }
        this.scrollOffset -= vertical * (ROW_HEIGHT + ROW_GAP);
        clampScroll();
        positionContentWidgets();
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
        return !this.helpOnly;
    }
    //#endif

    public void handleServerResponse(ConfigSyncPayload payload) {
        TeamcraftConfigResponseHandler.handle(this, payload);
    }
    private void calculateBounds() {
        int panelWidth = Math.max(1, this.width - 16);
        int panelHeight = Math.max(1, this.height - 16);
        this.panelLeft = (this.width - panelWidth) / 2;
        this.panelTop = (this.height - panelHeight) / 2;
        this.panelRight = this.panelLeft + panelWidth;
        this.panelBottom = this.panelTop + panelHeight;
        this.navigationWidth = Math.clamp(panelWidth / 4, 94, 142);
        this.contentLeft = this.panelLeft + this.navigationWidth + 10;
        this.contentRight = this.panelRight - 10;
        this.contentTop = this.panelTop + 42;
        this.contentBottom = this.panelBottom - 46;
    }

    private void addNavigation() {
        int x = this.panelLeft + 8;
        int y = this.panelTop + 43;
        int width = this.navigationWidth - 16;
        TeamcraftConfigPage[] pages = this.helpOnly
            ? new TeamcraftConfigPage[]{TeamcraftConfigPage.HELP}
            : TeamcraftConfigPage.values();
        for (TeamcraftConfigPage candidate : pages) {
            Component label = candidate == this.page
                ? Component.literal("▶ ").append(candidate.displayName()).withStyle(ChatFormatting.AQUA)
                : candidate.displayName();
            Button button = addRenderableWidget(Button.builder(label, ignored -> switchPage(candidate))
                .bounds(x, y, width, 22)
                .tooltip(Tooltip.create(Component.translatable(candidate.tooltipKey)))
                .build());
            button.setTooltipDelay(TOOLTIP_DELAY);
            this.navigationButtons.add(button);
            y += 27;
        }
    }

    private void addFooter() {
        List<FooterAction> actions = switch (this.page) {
            case TEAM_CONFIG -> List.of(
                new FooterAction(TeamcraftTranslations.GUI_DEFAULTS.key(), () -> TeamcraftConfigActions.restoreDefaults(this), true),
                new FooterAction(TeamcraftTranslations.GUI_CLOSE.key(), this::onClose, false),
                new FooterAction(TeamcraftTranslations.GUI_APPLY.key(), () -> TeamcraftConfigActions.submitConfig(this, false), true),
                new FooterAction(TeamcraftTranslations.GUI_SPLIT.key(), () -> TeamcraftConfigActions.submitConfig(this, true), true)
            );
            case OWN_TEAM -> List.of(
                new FooterAction(TeamcraftTranslations.GUI_CLOSE.key(), this::onClose, false),
                new FooterAction(TeamcraftTranslations.GUI_REFRESH.key(), () -> TeamcraftConfigActions.refresh(this), true),
                new FooterAction(TeamcraftTranslations.GUI_SAVE_TEAM.key(), () -> TeamcraftConfigActions.submitOwnTeam(this), true)
            );
            case ALL_TEAMS -> List.of(
                new FooterAction(TeamcraftTranslations.GUI_CLOSE.key(), this::onClose, false),
                new FooterAction(TeamcraftTranslations.GUI_REFRESH.key(), () -> TeamcraftConfigActions.refresh(this), true),
                new FooterAction(TeamcraftTranslations.GUI_ALL_TEAMS_CLEAR.key(), () -> TeamcraftConfigActions.clearAllTeams(this), true)
            );
            case HELP -> List.of(
                new FooterAction(TeamcraftTranslations.GUI_CLOSE.key(), this::onClose, false)
            );
        };

        int gap = 5;
        int available = this.panelRight - this.panelLeft - 16;
        int buttonWidth = (available - gap * (actions.size() - 1)) / actions.size();
        int x = this.panelLeft + 8;
        int y = this.panelBottom - 29;
        for (FooterAction action : actions) {
            Button button = addRenderableWidget(Button.builder(
                Component.translatable(action.translationKey),
                ignored -> action.run.run()
            ).bounds(x, y, buttonWidth, 20).build());
            if (action.idleOnly) {
                this.idleOnlyWidgets.add(button);
            }
            x += buttonWidth + gap;
        }
    }

    private void switchPage(TeamcraftConfigPage target) {
        if (this.waitingForServer || this.page == target) {
            return;
        }
        this.page = target;
        this.scrollOffset = 0;
        rebuildWidgets();
    }

    void rebuildPage() {
        rebuildWidgets();
    }

    void resetScroll() {
        this.scrollOffset = 0;
    }

    void registerCandidateRow(String playerName) {
        this.candidateRows.add(new CandidateRow(playerName, this.cursorY));
    }

    void openTeamDetails(TeamInfoData team) {
        //#if MC >= 260200
        this.minecraft.gui.setScreen(new TeamDetailsScreen(this, team));
        //#else
        //$$ this.minecraft.setScreen(new TeamDetailsScreen(this, team));
        //#endif
    }

    void loadConfig(TeamcraftConfigData config) {
        this.fixedTeamCount = config.fixedTeamCount();
        this.playersPerTeamText = Integer.toString(config.playersPerTeam());
        this.teamCountText = Integer.toString(config.teamCount());
        this.mode = config.mode();
        this.friendlyFire = config.friendlyFire();
        this.configuredColors = new ArrayList<>(config.colors());
        this.configuredNames = new ArrayList<>(config.names());
        this.pendingName = "";
    }

    void loadCandidates(List<String> candidates, List<String> onlinePlayers) {
        this.candidates = new ArrayList<>(candidates);
        this.onlinePlayers = List.copyOf(onlinePlayers);
        this.draggedCandidate = null;
        this.candidateDragMoved = false;
    }

    void loadTeams(TeamInfoData ownTeam, List<TeamInfoData> teams) {
        this.ownTeam = ownTeam;
        this.teams = List.copyOf(teams);
        if (ownTeam != null) {
            this.ownTeamName = ownTeam.displayName().getString();
            this.ownTeamcraftColor = ownTeam.color();
            this.ownTeamFriendlyFire = ownTeam.friendlyFire();
        }
        else {
            this.ownTeamName = "";
            this.ownTeamcraftColor = TeamcraftColor.WHITE;
            this.ownTeamFriendlyFire = false;
        }
    }

    void addHeader(String key) {
        this.contentHeaders.add(new ContentHeader(this.cursorY, Component.translatable(key)));
        this.cursorY += HEADER_HEIGHT;
    }

    void addTextRow(Component text, Component tooltip) {
        List<FormattedCharSequence> lines = this.font.split(text, Math.max(40, contentWidth() - 16));
        int height = Math.max(ROW_HEIGHT, lines.size() * (this.font.lineHeight + 1) + 10);
        this.contentRows.add(new ContentRow(this.cursorY, height, lines, tooltip));
        this.cursorY += height + ROW_GAP;
    }

    void addValueRow(String labelKey, Component value) {
        Component text = Component.translatable(labelKey).withStyle(ChatFormatting.GRAY)
            .append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
            .append(value);
        addTextRow(text, text);
    }

    void addFullWidget(AbstractWidget widget, Component tooltip) {
        configureTooltip(widget, tooltip);
        int width = contentWidth() - 8;
        addContentWidget(widget, 4, this.cursorY + 2, width, WIDGET_HEIGHT, true);
        this.contentRows.add(new ContentRow(this.cursorY, ROW_HEIGHT, List.of(), tooltip));
        this.cursorY += ROW_HEIGHT + ROW_GAP;
    }

    void addTwoWidgets(AbstractWidget left, AbstractWidget right, Component tooltip) {
        configureTooltip(left, tooltip);
        configureTooltip(right, tooltip);
        int gap = 6;
        int width = (contentWidth() - 8 - gap) / 2;
        addContentWidget(left, 4, this.cursorY + 2, width, WIDGET_HEIGHT, true);
        addContentWidget(right, 4 + width + gap, this.cursorY + 2, width, WIDGET_HEIGHT, true);
        this.contentRows.add(new ContentRow(this.cursorY, ROW_HEIGHT, List.of(), tooltip));
        this.cursorY += ROW_HEIGHT + ROW_GAP;
    }

    void addLabeledWidget(String labelKey, AbstractWidget widget, Component tooltip) {
        configureTooltip(widget, tooltip);
        int labelWidth = Math.max(72, Math.min(130, contentWidth() / 3));
        int widgetX = labelWidth + 6;
        int widgetWidth = contentWidth() - widgetX - 4;
        addContentWidget(widget, widgetX, this.cursorY + 2, widgetWidth, WIDGET_HEIGHT, true);
        this.contentRows.add(new ContentRow(
            this.cursorY,
            ROW_HEIGHT,
            List.of(Component.translatable(labelKey).getVisualOrderText()),
            tooltip
        ));
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
        widget.setSize(width, height);
        addWidget(widget);
        this.contentWidgets.add(new PositionedWidget(widget, xOffset, baseY, enabled));
        this.idleOnlyWidgets.add(widget);
    }

    EditBox numberBox(String key, String value, java.util.function.Consumer<String> responder) {
        return textBox(key, TeamcraftTranslations.GUI_PLACEHOLDER_NUMBER.key(), value, 4, responder);
    }

    Button colorDropdownButton(Component label, TeamcraftColor value, Consumer<TeamcraftColor> onChanged) {
        Component message = label.copy()
            .append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Msg.colorName(value).copy().withColor(value.textColor()))
            .append(Component.literal("  ▼").withStyle(ChatFormatting.GRAY));
        return Button.builder(message, button -> openColorMenu(button, onChanged)).build();
    }

    private void openColorMenu(AbstractWidget anchor, Consumer<TeamcraftColor> onChanged) {
        int rowHeight = 16;
        int availableHeight = this.contentBottom - this.contentTop;
        int columns = availableHeight >= ((TeamcraftColor.values().length + 1) / 2) * rowHeight ? 2 : 4;
        int rows = (TeamcraftColor.values().length + columns - 1) / columns;
        int width = columns == 2
            ? Math.clamp(anchor.getWidth(), 220, contentWidth() - 8)
            : contentWidth() - 8;
        int height = rows * rowHeight;
        int x = Math.clamp(anchor.getX(), this.contentLeft + 4, this.contentRight - width - 4);
        int below = anchor.getBottom() + 1;
        int y = below + height <= this.contentBottom
            ? below
            : Math.max(this.contentTop, anchor.getY() - height - 1);
        this.colorMenu = new ColorMenu(
            x,
            y,
            width / columns,
            rowHeight,
            columns,
            anchor.getX(),
            anchor.getY(),
            anchor.getWidth(),
            anchor.getHeight(),
            onChanged
        );
    }

    EditBox textBox(
        String key,
        String placeholderKey,
        String value,
        int maxLength,
        java.util.function.Consumer<String> responder
    ) {
        //#if MC >= 260200
        EditBox box = new EditBox(this.font, Component.translatable(key));
        //#else
        //$$ EditBox box = new EditBox(this.font, 0, 0, 20, 20, Component.translatable(key));
        //#endif
        box.setMaxLength(maxLength);
        box.setHint(Component.translatable(placeholderKey).withStyle(ChatFormatting.DARK_GRAY));
        box.setValue(value);
        box.setResponder(responder);
        return box;
    }

    private void positionContentWidgets() {
        for (PositionedWidget positioned : this.contentWidgets) {
            AbstractWidget widget = positioned.widget;
            int y = this.contentTop + positioned.baseY - (int) this.scrollOffset;
            widget.setPosition(this.contentLeft + positioned.xOffset, y);
            boolean fullyVisible = y >= this.contentTop && y + widget.getHeight() <= this.contentBottom;
            widget.visible = fullyVisible;
            widget.active = fullyVisible && positioned.enabled && !this.waitingForServer;
        }
    }

    void updateEnabledState() {
        for (Button button : this.navigationButtons) {
            button.active = !this.waitingForServer;
        }
        for (AbstractWidget widget : this.idleOnlyWidgets) {
            widget.active = !this.waitingForServer && widget.visible;
        }
        positionContentWidgets();
    }

    private void extractContent(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        for (ContentHeader header : this.contentHeaders) {
            int y = this.contentTop + header.baseY - (int) this.scrollOffset;
            if (y + HEADER_HEIGHT <= this.contentTop || y >= this.contentBottom) {
                continue;
            }
            FormattedCharSequence headerText = header.text.copy()
                .withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD)
                .getVisualOrderText();
            graphics.text(this.font, headerText, this.contentLeft + 5, y + 6, 0xFFFFFFFF);
        }

        for (ContentRow row : this.contentRows) {
            int y = this.contentTop + row.baseY - (int) this.scrollOffset;
            if (y + row.height <= this.contentTop || y >= this.contentBottom) {
                continue;
            }
            boolean hovered = mouseX >= this.contentLeft && mouseX < this.contentRight
                && mouseY >= y && mouseY < y + row.height;
            graphics.fill(
                this.contentLeft,
                y,
                this.contentRight - 5,
                y + row.height,
                hovered ? 0xB02A323B : 0x8020272E
            );
            if (!row.textLines.isEmpty()) {
                int textY = y + (row.textLines.size() == 1 ? 8 : 5);
                for (FormattedCharSequence line : row.textLines) {
                    graphics.text(this.font, line, this.contentLeft + 8, textY, 0xFFE6E6E6);
                    textY += this.font.lineHeight + 1;
                }
            }
            if (hovered && row.tooltip != null) {
                int tooltipWidth = Math.clamp(this.width - 32, 120, TOOLTIP_MAX_WIDTH);
                graphics.setTooltipForNextFrame(
                    this.font,
                    this.font.split(row.tooltip, tooltipWidth),
                    mouseX,
                    mouseY
                );
            }
        }

        for (PositionedWidget positioned : this.contentWidgets) {
            if (positioned.widget.visible) {
                //#if MC >= 260102
                positioned.widget.extractRenderState(graphics, mouseX, mouseY, partialTick);
                //#else
                //$$ positioned.widget.render(graphics, mouseX, mouseY, partialTick);
                //#endif
            }
        }

        if (this.candidateDragMoved && this.draggedCandidate != null) {
            for (CandidateRow row : this.candidateRows) {
                if (!row.playerName.equals(this.draggedCandidate)) {
                    continue;
                }
                int y = this.contentTop + row.baseY - (int) this.scrollOffset;
                if (y + ROW_HEIGHT > this.contentTop && y < this.contentBottom) {
                    graphics.fill(this.contentLeft + 1, y, this.contentLeft + 4, y + ROW_HEIGHT, 0xFFFFAA00);
                }
                break;
            }
        }
    }

    private void extractScrollbar(GuiGraphicsExtractor graphics) {
        int maxScroll = maxScroll();
        if (maxScroll <= 0) {
            return;
        }
        int trackHeight = this.contentBottom - this.contentTop;
        int thumbHeight = Math.max(20, trackHeight * trackHeight / this.contentHeight);
        int travel = trackHeight - thumbHeight;
        int thumbY = this.contentTop + (int) (travel * this.scrollOffset / maxScroll);
        graphics.fill(this.contentRight - 3, this.contentTop, this.contentRight, this.contentBottom, 0x602A3037);
        graphics.fill(this.contentRight - 3, thumbY, this.contentRight, thumbY + thumbHeight, 0xFF6D7C89);
    }

    private void extractColorMenu(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        if (this.colorMenu == null) {
            return;
        }
        ColorMenu menu = this.colorMenu;
        TeamcraftColor[] colors = TeamcraftColor.values();
        int rows = (colors.length + menu.columns - 1) / menu.columns;
        int width = menu.columnWidth * menu.columns;
        int height = rows * menu.rowHeight;

        graphics.enableScissor(this.contentLeft, this.contentTop, this.contentRight, this.contentBottom);
        graphics.fill(menu.x - 1, menu.y - 1, menu.x + width + 1, menu.y + height + 1, 0xFF7F8C98);
        graphics.fill(menu.x, menu.y, menu.x + width, menu.y + height, 0xFF11161B);
        for (int i = 0; i < colors.length; i++) {
            int column = i % menu.columns;
            int row = i / menu.columns;
            int x = menu.x + column * menu.columnWidth;
            int y = menu.y + row * menu.rowHeight;
            boolean hovered = mouseX >= x && mouseX < x + menu.columnWidth
                && mouseY >= y && mouseY < y + menu.rowHeight;
            if (hovered) {
                graphics.fill(x, y, x + menu.columnWidth, y + menu.rowHeight, 0xFF34414D);
            }
            TeamcraftColor color = colors[i];
            graphics.fill(x + 4, y + 5, x + 11, y + 12, 0xFF000000 | color.rgb());
            graphics.text(
                this.font,
                Msg.colorName(color).copy().withColor(color.textColor()).getVisualOrderText(),
                x + 15,
                y + 5,
                0xFFFFFFFF
            );
        }
        graphics.disableScissor();
    }

    private void clampScroll() {
        this.scrollOffset = Math.clamp(this.scrollOffset, 0, maxScroll());
    }

    private int maxScroll() {
        return Math.max(0, this.contentHeight - (this.contentBottom - this.contentTop));
    }

    private int contentWidth() {
        return this.contentRight - this.contentLeft;
    }

    TeamcraftSplitRule currentRule() {
        return this.fixedTeamCount ? TeamcraftSplitRule.TEAM_COUNT : TeamcraftSplitRule.PLAYERS_PER_TEAM;
    }

    void showOverlay(String translationKey, ChatFormatting color) {
        showOverlay(Component.translatable(translationKey), color);
    }

    void showOverlay(Component message, ChatFormatting color) {
        if (this.minecraft.player != null) {
            //#if MC >= 260102
            this.minecraft.player.sendOverlayMessage(message.copy().withStyle(color));
            //#else
            //$$ this.minecraft.player.displayClientMessage(message.copy().withStyle(color), true);
            //#endif
        }
    }

    static Component tooltip(String key) {
        return Component.translatable(key + ".tooltip")
            .append("\n")
            .append(Component.translatable(key + ".example").withStyle(ChatFormatting.GRAY));
    }

    static Component tooltip(String tooltipKey, String exampleKey) {
        return Component.translatable(tooltipKey)
            .append("\n")
            .append(Component.translatable(exampleKey).withStyle(ChatFormatting.GRAY));
    }

    private static void configureTooltip(AbstractWidget widget, Component tooltip) {
        widget.setTooltip(Tooltip.create(tooltip));
        widget.setTooltipDelay(TOOLTIP_DELAY);
    }

    private record PositionedWidget(AbstractWidget widget, int xOffset, int baseY, boolean enabled) {
    }

    private record ContentRow(int baseY, int height, List<FormattedCharSequence> textLines, Component tooltip) {
    }

    private record ContentHeader(int baseY, Component text) {
    }

    private record CandidateRow(String playerName, int baseY) {
    }

    private record FooterAction(String translationKey, Runnable run, boolean idleOnly) {
    }

    private record ColorMenu(
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
