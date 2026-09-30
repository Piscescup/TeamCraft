package io.github.piscescup.fabricmc.teamcraft.gui;

import io.github.piscescup.fabricmc.teamcraft.gui.network.ConfigSyncPayload;
import io.github.piscescup.fabricmc.teamcraft.team.SplitMode;
import io.github.piscescup.fabricmc.teamcraft.text.Msg;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.scores.TeamColor;

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
    private static final int ROW_HEIGHT = 24;
    private static final int ROW_GAP = 4;
    private static final int HEADER_HEIGHT = 22;
    private static final int WIDGET_HEIGHT = 20;

    private final Screen parent;
    private final List<PositionedWidget> contentWidgets = new ArrayList<>();
    private final List<ContentRow> contentRows = new ArrayList<>();
    private final List<ContentHeader> contentHeaders = new ArrayList<>();
    private final List<Button> navigationButtons = new ArrayList<>();
    private final List<AbstractWidget> idleOnlyWidgets = new ArrayList<>();

    private Page page = Page.TEAM_CONFIG;
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

    private boolean fixedTeamCount;
    private String playersPerTeamText;
    private String teamCountText;
    private SplitMode mode;
    private boolean friendlyFire;
    private List<TeamColor> configuredColors;
    private TeamColor selectedColor = TeamColor.RED;
    private String namesText;

    private TeamInfoData ownTeam;
    private List<TeamInfoData> teams;
    private String ownTeamName;
    private TeamColor ownTeamColor = TeamColor.WHITE;
    private boolean ownTeamFriendlyFire;
    private boolean waitingForServer;
    private ColorMenu colorMenu;

    public TeamcraftConfigScreen(
        Screen parent,
        TeamcraftConfigData config,
        TeamInfoData ownTeam,
        List<TeamInfoData> teams
    ) {
        super(Component.translatable("teamcraft.gui.title"));
        this.parent = parent;
        loadConfig(config);
        loadTeams(ownTeam, teams);
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
        this.cursorY = 0;

        addNavigation();
        switch (this.page) {
            case TEAM_CONFIG -> addTeamConfigPage();
            case OWN_TEAM -> addOwnTeamPage();
            case ALL_TEAMS -> addAllTeamsPage();
        }
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
        graphics.verticalLine(
            this.panelLeft + this.navigationWidth,
            this.panelTop + 34,
            this.panelBottom - 38,
            0xFF39424C
        );
        graphics.horizontalLine(this.panelLeft, this.panelRight, this.panelBottom - 38, 0xFF39424C);
        graphics.centeredText(this.font, this.title, (this.panelLeft + this.panelRight) / 2, this.panelTop + 12, 0xFFFFFFFF);

        graphics.enableScissor(this.contentLeft, this.contentTop, this.contentRight, this.contentBottom);
        extractContent(graphics, mouseX, mouseY, partialTick);
        graphics.disableScissor();
        extractScrollbar(graphics);

        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        extractColorMenu(graphics, mouseX, mouseY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.colorMenu != null) {
            ColorMenu menu = this.colorMenu;
            this.colorMenu = null;
            if (event.button() == 0 && menu.contains(event.x(), event.y())) {
                int column = (int) ((event.x() - menu.x) / menu.columnWidth);
                int row = (int) ((event.y() - menu.y) / menu.rowHeight);
                int index = row * menu.columns + column;
                TeamColor[] colors = TeamColor.values();
                if (index >= 0 && index < colors.length) {
                    AbstractWidget.playButtonClickSound(this.minecraft.getSoundManager());
                    menu.onChanged.accept(colors[index]);
                    rebuildWidgets();
                    return true;
                }
            }
            if (menu.anchorContains(event.x(), event.y())) {
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
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

        switch (payload.response()) {
            case OPENED -> {
                loadConfig(payload.config());
                loadTeams(payload.ownTeam(), payload.teams());
                showOverlay("teamcraft.gui.refreshed", ChatFormatting.GREEN);
            }
            case SAVED -> {
                loadConfig(payload.config());
                loadTeams(payload.ownTeam(), payload.teams());
                showOverlay("teamcraft.gui.saved", ChatFormatting.GREEN);
            }
            case BUILT -> {
                loadConfig(payload.config());
                loadTeams(payload.ownTeam(), payload.teams());
                this.page = Page.ALL_TEAMS;
                this.scrollOffset = 0;
                showOverlay("teamcraft.gui.built", ChatFormatting.GREEN);
            }
            case TEAM_SAVED -> {
                loadTeams(payload.ownTeam(), payload.teams());
                showOverlay("teamcraft.gui.team_saved", ChatFormatting.GREEN);
            }
            case INVALID -> showOverlay("teamcraft.gui.error.invalid_server", ChatFormatting.RED);
            case PERMISSION_DENIED -> {
                showOverlay("teamcraft.gui.error.permission", ChatFormatting.RED);
                onClose();
                return;
            }
            case NO_CANDIDATES -> {
                loadConfig(payload.config());
                showOverlay("teamcraft.gui.error.no_candidates", ChatFormatting.RED);
            }
            case TEAMS_EXIST -> {
                loadConfig(payload.config());
                loadTeams(payload.ownTeam(), payload.teams());
                showOverlay("teamcraft.gui.error.teams_exist", ChatFormatting.RED);
            }
            case TOO_MANY_TEAMS -> {
                loadConfig(payload.config());
                showOverlay("teamcraft.gui.error.too_many_teams", ChatFormatting.RED);
            }
            case TEAM_NOT_FOUND -> {
                loadTeams(payload.ownTeam(), payload.teams());
                showOverlay("teamcraft.gui.error.team_not_found", ChatFormatting.RED);
            }
        }
        rebuildWidgets();
    }

    private void calculateBounds() {
        int panelWidth = Math.min(640, this.width - 16);
        int panelHeight = Math.min(380, this.height - 16);
        this.panelLeft = (this.width - panelWidth) / 2;
        this.panelTop = (this.height - panelHeight) / 2;
        this.panelRight = this.panelLeft + panelWidth;
        this.panelBottom = this.panelTop + panelHeight;
        this.navigationWidth = Math.max(94, Math.min(142, panelWidth / 4));
        this.contentLeft = this.panelLeft + this.navigationWidth + 10;
        this.contentRight = this.panelRight - 10;
        this.contentTop = this.panelTop + 42;
        this.contentBottom = this.panelBottom - 46;
    }

    private void addNavigation() {
        int x = this.panelLeft + 8;
        int y = this.panelTop + 43;
        int width = this.navigationWidth - 16;
        for (Page candidate : Page.values()) {
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

    private void addTeamConfigPage() {
        addHeader("teamcraft.gui.category.split");

        CycleButton<SplitRule> rule = CycleButton.builder(SplitRule::displayName, currentRule())
            .withValues(SplitRule.values())
            .create(Component.translatable("teamcraft.gui.option.rule"), (button, value) -> {
                this.fixedTeamCount = value == SplitRule.TEAM_COUNT;
                this.scrollOffset = 0;
                rebuildWidgets();
            });
        addFullWidget(rule, tooltip("teamcraft.gui.option.rule"));

        if (this.fixedTeamCount) {
            EditBox count = numberBox(
                "teamcraft.gui.option.team_count",
                this.teamCountText,
                value -> this.teamCountText = value
            );
            addLabeledWidget("teamcraft.gui.option.team_count", count, tooltip("teamcraft.gui.option.team_count"));
        }
        else {
            EditBox size = numberBox(
                "teamcraft.gui.option.players_per_team",
                this.playersPerTeamText,
                value -> this.playersPerTeamText = value
            );
            addLabeledWidget(
                "teamcraft.gui.option.players_per_team",
                size,
                tooltip("teamcraft.gui.option.players_per_team")
            );
        }

        CycleButton<SplitMode> modeButton = CycleButton.builder(SplitMode::displayName, this.mode)
            .withValues(SplitMode.values())
            .create(Component.translatable("teamcraft.gui.option.mode"), (button, value) -> this.mode = value);
        addFullWidget(modeButton, tooltip("teamcraft.gui.option.mode"));

        CycleButton<Boolean> friendlyFireButton = CycleButton.onOffBuilder(this.friendlyFire)
            .create(
                Component.translatable("teamcraft.gui.option.friendly_fire"),
                (button, value) -> this.friendlyFire = value
            );
        addFullWidget(friendlyFireButton, tooltip("teamcraft.gui.option.friendly_fire"));

        addHeader("teamcraft.gui.category.appearance");
        EditBox names = textBox(
            "teamcraft.gui.option.names",
            "teamcraft.gui.placeholder.names",
            this.namesText,
            TeamcraftConfigData.MAX_LIST_SIZE * (TeamcraftConfigData.MAX_NAME_LENGTH + 2),
            value -> this.namesText = value
        );
        addLabeledWidget("teamcraft.gui.option.names", names, tooltip("teamcraft.gui.option.names"));

        addHeader("teamcraft.gui.option.colors");
        if (this.configuredColors.isEmpty()) {
            addTextRow(
                Component.translatable("teamcraft.gui.colors.default_palette").withStyle(ChatFormatting.GRAY),
                tooltip("teamcraft.gui.option.colors")
            );
        }
        for (int i = 0; i < this.configuredColors.size(); i++) {
            int index = i;
            Button color = colorDropdownButton(
                Component.translatable("teamcraft.gui.colors.slot", i + 1),
                this.configuredColors.get(i),
                value -> this.configuredColors.set(index, value)
            );
            Button remove = Button.builder(Component.translatable("teamcraft.gui.colors.remove"), ignored -> {
                this.configuredColors.remove(index);
                rebuildWidgets();
            }).build();
            addTwoWidgets(color, remove, tooltip("teamcraft.gui.option.colors"));
        }

        Button picker = colorDropdownButton(
            Component.translatable("teamcraft.gui.colors.picker"),
            this.selectedColor,
            value -> this.selectedColor = value
        );
        Button addColor = Button.builder(Component.translatable("teamcraft.gui.colors.add"), ignored -> {
            if (this.configuredColors.size() < TeamcraftConfigData.MAX_LIST_SIZE) {
                this.configuredColors.add(this.selectedColor);
                rebuildWidgets();
            }
        }).build();
        addTwoWidgets(picker, addColor, tooltip("teamcraft.gui.option.colors"));

        if (!this.configuredColors.isEmpty()) {
            Button useDefaults = Button.builder(Component.translatable("teamcraft.gui.colors.use_defaults"), ignored -> {
                this.configuredColors.clear();
                rebuildWidgets();
            }).build();
            addFullWidget(useDefaults, tooltip("teamcraft.gui.option.colors"));
        }

        addTextRow(
            Component.translatable("teamcraft.gui.session_note").withStyle(ChatFormatting.DARK_GRAY),
            Component.translatable("teamcraft.gui.session_note")
        );
    }

    private void addOwnTeamPage() {
        addHeader("teamcraft.gui.category.own_team");
        if (this.ownTeam == null) {
            addTextRow(
                Component.translatable("teamcraft.gui.own_team.none").withStyle(ChatFormatting.GRAY),
                Component.translatable("teamcraft.gui.own_team.none.tooltip")
            );
            return;
        }

        addValueRow("teamcraft.gui.own_team.id", Component.literal(this.ownTeam.id()).withStyle(ChatFormatting.GRAY));

        EditBox name = textBox(
            "teamcraft.gui.own_team.name",
            "teamcraft.gui.placeholder.team_name",
            this.ownTeamName,
            TeamInfoData.MAX_DISPLAY_NAME_LENGTH,
            value -> this.ownTeamName = value
        );
        addLabeledWidget("teamcraft.gui.own_team.name", name, tooltip("teamcraft.gui.own_team.name"));

        Button color = colorDropdownButton(
            Component.translatable("teamcraft.gui.own_team.color"),
            this.ownTeamColor,
            value -> this.ownTeamColor = value
        );
        addFullWidget(color, tooltip("teamcraft.gui.own_team.color"));

        CycleButton<Boolean> friendly = CycleButton.onOffBuilder(this.ownTeamFriendlyFire)
            .create(Component.translatable("teamcraft.gui.own_team.friendly_fire"),
                (button, value) -> this.ownTeamFriendlyFire = value);
        addFullWidget(friendly, tooltip("teamcraft.gui.own_team.friendly_fire"));

        addValueRow(
            "teamcraft.gui.own_team.members",
            Component.literal(String.join(", ", this.ownTeam.members())).withStyle(ChatFormatting.WHITE)
        );
    }

    private void addAllTeamsPage() {
        addHeader("teamcraft.gui.category.all_teams");
        if (this.teams.isEmpty()) {
            addTextRow(
                Component.translatable("teamcraft.gui.all_teams.none").withStyle(ChatFormatting.GRAY),
                Component.translatable("teamcraft.gui.all_teams.none")
            );
            return;
        }

        for (TeamInfoData team : this.teams) {
            Component label = team.displayName().copy()
                .withColor(team.color().textColor())
                .append(Component.literal("  •  " + team.members().size())
                    .withStyle(ChatFormatting.GRAY));
            Component details = Component.translatable(
                "teamcraft.gui.all_teams.tooltip",
                team.id(),
                Msg.colorName(team.color()),
                Component.translatable(team.friendlyFire() ? "teamcraft.common.on" : "teamcraft.common.off"),
                String.join(", ", team.members())
            );
            Button teamRow = Button.builder(label, ignored -> { }).build();
            addFullWidget(teamRow, details);
        }
    }

    private void addFooter() {
        List<FooterAction> actions = switch (this.page) {
            case TEAM_CONFIG -> List.of(
                new FooterAction("teamcraft.gui.defaults", this::restoreDefaults, true),
                new FooterAction("teamcraft.gui.close", this::onClose, false),
                new FooterAction("teamcraft.gui.apply", () -> submitConfig(false), true),
                new FooterAction("teamcraft.gui.split", () -> submitConfig(true), true)
            );
            case OWN_TEAM -> List.of(
                new FooterAction("teamcraft.gui.close", this::onClose, false),
                new FooterAction("teamcraft.gui.refresh", this::refresh, true),
                new FooterAction("teamcraft.gui.save_team", this::submitOwnTeam, true)
            );
            case ALL_TEAMS -> List.of(
                new FooterAction("teamcraft.gui.close", this::onClose, false),
                new FooterAction("teamcraft.gui.refresh", this::refresh, true)
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

    private void submitConfig(boolean buildTeams) {
        DraftResult draft = createConfigDraft();
        if (draft.error != null) {
            showOverlay(draft.error, ChatFormatting.RED);
            return;
        }
        this.waitingForServer = true;
        updateEnabledState();
        if (!TeamcraftConfigClient.save(draft.config, buildTeams)) {
            this.waitingForServer = false;
            updateEnabledState();
        }
    }

    private void submitOwnTeam() {
        if (this.ownTeam == null) {
            showOverlay("teamcraft.gui.error.team_not_found", ChatFormatting.RED);
            return;
        }
        String name = this.ownTeamName.trim();
        if (name.isEmpty()) {
            showOverlay("teamcraft.gui.error.empty_team_name", ChatFormatting.RED);
            return;
        }
        this.waitingForServer = true;
        updateEnabledState();
        if (!TeamcraftConfigClient.saveOwnTeam(
            this.ownTeam.id(),
            name,
            this.ownTeamColor,
            this.ownTeamFriendlyFire
        )) {
            this.waitingForServer = false;
            updateEnabledState();
        }
    }

    private void refresh() {
        this.waitingForServer = true;
        updateEnabledState();
        if (!TeamcraftConfigClient.refresh()) {
            this.waitingForServer = false;
            updateEnabledState();
        }
    }

    private DraftResult createConfigDraft() {
        TeamcraftConfigData defaults = TeamcraftConfigData.defaults();
        Integer playersPerTeam = parseBoundedInteger(
            this.playersPerTeamText,
            TeamcraftConfigData.MIN_PLAYERS_PER_TEAM,
            TeamcraftConfigData.MAX_PLAYERS_PER_TEAM
        );
        Integer teamCount = parseBoundedInteger(
            this.teamCountText,
            TeamcraftConfigData.MIN_TEAM_COUNT,
            TeamcraftConfigData.MAX_TEAM_COUNT
        );

        // Only the value belonging to the selected split rule is user-facing.
        // A stale invalid value from the hidden rule must not block saving.
        if (!this.fixedTeamCount && playersPerTeam == null) {
            return DraftResult.error(Component.translatable(
                "teamcraft.gui.error.players_per_team",
                TeamcraftConfigData.MIN_PLAYERS_PER_TEAM,
                TeamcraftConfigData.MAX_PLAYERS_PER_TEAM
            ));
        }
        if (this.fixedTeamCount && teamCount == null) {
            return DraftResult.error(Component.translatable(
                "teamcraft.gui.error.team_count",
                TeamcraftConfigData.MIN_TEAM_COUNT,
                TeamcraftConfigData.MAX_TEAM_COUNT
            ));
        }
        if (playersPerTeam == null) {
            playersPerTeam = defaults.playersPerTeam();
        }
        if (teamCount == null) {
            teamCount = defaults.teamCount();
        }

        List<String> names = new ArrayList<>();
        if (!this.namesText.isBlank()) {
            for (String value : this.namesText.split("[|,，;；]")) {
                String name = value.trim();
                if (!name.isEmpty()) {
                    if (name.length() > TeamcraftConfigData.MAX_NAME_LENGTH) {
                        return DraftResult.error(Component.translatable(
                            "teamcraft.gui.error.name_too_long",
                            TeamcraftConfigData.MAX_NAME_LENGTH
                        ));
                    }
                    names.add(name);
                }
            }
        }
        if (names.size() > TeamcraftConfigData.MAX_LIST_SIZE) {
            return DraftResult.error(Component.translatable(
                "teamcraft.gui.error.too_many_values",
                TeamcraftConfigData.MAX_LIST_SIZE
            ));
        }

        return new DraftResult(new TeamcraftConfigData(
            this.fixedTeamCount,
            playersPerTeam,
            teamCount,
            this.mode,
            this.friendlyFire,
            this.configuredColors,
            names
        ), null);
    }

    private void restoreDefaults() {
        loadConfig(TeamcraftConfigData.defaults());
        this.scrollOffset = 0;
        rebuildWidgets();
        showOverlay("teamcraft.gui.defaults_ready", ChatFormatting.YELLOW);
    }

    private void switchPage(Page target) {
        if (this.waitingForServer || this.page == target) {
            return;
        }
        this.page = target;
        this.scrollOffset = 0;
        rebuildWidgets();
    }

    private void loadConfig(TeamcraftConfigData config) {
        this.fixedTeamCount = config.fixedTeamCount();
        this.playersPerTeamText = Integer.toString(config.playersPerTeam());
        this.teamCountText = Integer.toString(config.teamCount());
        this.mode = config.mode();
        this.friendlyFire = config.friendlyFire();
        this.configuredColors = new ArrayList<>(config.colors());
        this.namesText = String.join(", ", config.names());
    }

    private void loadTeams(TeamInfoData ownTeam, List<TeamInfoData> teams) {
        this.ownTeam = ownTeam;
        this.teams = List.copyOf(teams);
        if (ownTeam != null) {
            this.ownTeamName = ownTeam.displayName().getString();
            this.ownTeamColor = ownTeam.color();
            this.ownTeamFriendlyFire = ownTeam.friendlyFire();
        }
        else {
            this.ownTeamName = "";
            this.ownTeamColor = TeamColor.WHITE;
            this.ownTeamFriendlyFire = false;
        }
    }

    private void addHeader(String key) {
        this.contentHeaders.add(new ContentHeader(this.cursorY, Component.translatable(key)));
        this.cursorY += HEADER_HEIGHT;
    }

    private void addTextRow(Component text, Component tooltip) {
        this.contentRows.add(new ContentRow(this.cursorY, ROW_HEIGHT, text, tooltip));
        this.cursorY += ROW_HEIGHT + ROW_GAP;
    }

    private void addValueRow(String labelKey, Component value) {
        Component text = Component.translatable(labelKey).withStyle(ChatFormatting.GRAY)
            .append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
            .append(value);
        addTextRow(text, text);
    }

    private void addFullWidget(AbstractWidget widget, Component tooltip) {
        configureTooltip(widget, tooltip);
        int width = contentWidth() - 8;
        addContentWidget(widget, 4, this.cursorY + 2, width, WIDGET_HEIGHT, true);
        this.contentRows.add(new ContentRow(this.cursorY, ROW_HEIGHT, null, tooltip));
        this.cursorY += ROW_HEIGHT + ROW_GAP;
    }

    private void addTwoWidgets(AbstractWidget left, AbstractWidget right, Component tooltip) {
        configureTooltip(left, tooltip);
        configureTooltip(right, tooltip);
        int gap = 6;
        int width = (contentWidth() - 8 - gap) / 2;
        addContentWidget(left, 4, this.cursorY + 2, width, WIDGET_HEIGHT, true);
        addContentWidget(right, 4 + width + gap, this.cursorY + 2, width, WIDGET_HEIGHT, true);
        this.contentRows.add(new ContentRow(this.cursorY, ROW_HEIGHT, null, tooltip));
        this.cursorY += ROW_HEIGHT + ROW_GAP;
    }

    private void addLabeledWidget(String labelKey, AbstractWidget widget, Component tooltip) {
        configureTooltip(widget, tooltip);
        int labelWidth = Math.max(72, Math.min(130, contentWidth() / 3));
        int widgetX = labelWidth + 6;
        int widgetWidth = contentWidth() - widgetX - 4;
        addContentWidget(widget, widgetX, this.cursorY + 2, widgetWidth, WIDGET_HEIGHT, true);
        this.contentRows.add(new ContentRow(
            this.cursorY,
            ROW_HEIGHT,
            Component.translatable(labelKey),
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

    private EditBox numberBox(String key, String value, java.util.function.Consumer<String> responder) {
        return textBox(key, "teamcraft.gui.placeholder.number", value, 4, responder);
    }

    private Button colorDropdownButton(Component label, TeamColor value, Consumer<TeamColor> onChanged) {
        Component message = label.copy()
            .append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Msg.colorName(value).copy().withColor(value.textColor()))
            .append(Component.literal("  ▼").withStyle(ChatFormatting.GRAY));
        return Button.builder(message, button -> openColorMenu(button, onChanged)).build();
    }

    private void openColorMenu(AbstractWidget anchor, Consumer<TeamColor> onChanged) {
        int rowHeight = 16;
        int availableHeight = this.contentBottom - this.contentTop;
        int columns = availableHeight >= ((TeamColor.values().length + 1) / 2) * rowHeight ? 2 : 4;
        int rows = (TeamColor.values().length + columns - 1) / columns;
        int width = columns == 2
            ? Math.min(Math.max(anchor.getWidth(), 220), contentWidth() - 8)
            : contentWidth() - 8;
        int height = rows * rowHeight;
        int x = Math.max(this.contentLeft + 4, Math.min(anchor.getX(), this.contentRight - width - 4));
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

    private EditBox textBox(
        String key,
        String placeholderKey,
        String value,
        int maxLength,
        java.util.function.Consumer<String> responder
    ) {
        EditBox box = new EditBox(this.font, Component.translatable(key));
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

    private void updateEnabledState() {
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
            graphics.text(this.font, header.text.copy().withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
                this.contentLeft + 5, y + 6, 0xFFFFFFFF);
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
            if (row.text != null) {
                graphics.text(this.font, row.text, this.contentLeft + 8, y + 8, 0xFFE6E6E6);
            }
            if (hovered && row.tooltip != null) {
                graphics.setTooltipForNextFrame(row.tooltip, mouseX, mouseY);
            }
        }

        for (PositionedWidget positioned : this.contentWidgets) {
            if (positioned.widget.visible) {
                positioned.widget.extractRenderState(graphics, mouseX, mouseY, partialTick);
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
        TeamColor[] colors = TeamColor.values();
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
            TeamColor color = colors[i];
            graphics.fill(x + 4, y + 5, x + 11, y + 12, 0xFF000000 | color.rgb());
            graphics.text(
                this.font,
                Msg.colorName(color).copy().withColor(color.textColor()),
                x + 15,
                y + 5,
                0xFFFFFFFF
            );
        }
        graphics.disableScissor();
    }

    private void clampScroll() {
        this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, maxScroll()));
    }

    private int maxScroll() {
        return Math.max(0, this.contentHeight - (this.contentBottom - this.contentTop));
    }

    private int contentWidth() {
        return this.contentRight - this.contentLeft;
    }

    private SplitRule currentRule() {
        return this.fixedTeamCount ? SplitRule.TEAM_COUNT : SplitRule.PLAYERS_PER_TEAM;
    }

    private void showOverlay(String translationKey, ChatFormatting color) {
        showOverlay(Component.translatable(translationKey), color);
    }

    private void showOverlay(Component message, ChatFormatting color) {
        if (this.minecraft.player != null) {
            this.minecraft.player.sendOverlayMessage(message.copy().withStyle(color));
        }
    }

    private static Integer parseBoundedInteger(String value, int minimum, int maximum) {
        try {
            int parsed = Integer.parseInt(value.trim());
            return parsed >= minimum && parsed <= maximum ? parsed : null;
        }
        catch (NumberFormatException ignored) {
            return null;
        }
    }

    private static Component tooltip(String key) {
        return Component.translatable(key + ".tooltip")
            .append("\n")
            .append(Component.translatable(key + ".example").withStyle(ChatFormatting.GRAY));
    }

    private static void configureTooltip(AbstractWidget widget, Component tooltip) {
        widget.setTooltip(Tooltip.create(tooltip));
        widget.setTooltipDelay(TOOLTIP_DELAY);
    }

    private enum Page {
        TEAM_CONFIG("teamcraft.gui.nav.team_config", "teamcraft.gui.nav.team_config.tooltip"),
        OWN_TEAM("teamcraft.gui.nav.own_team", "teamcraft.gui.nav.own_team.tooltip"),
        ALL_TEAMS("teamcraft.gui.nav.all_teams", "teamcraft.gui.nav.all_teams.tooltip");

        private final String labelKey;
        private final String tooltipKey;

        Page(String labelKey, String tooltipKey) {
            this.labelKey = labelKey;
            this.tooltipKey = tooltipKey;
        }

        private Component displayName() {
            return Component.translatable(this.labelKey);
        }
    }

    private enum SplitRule {
        PLAYERS_PER_TEAM("teamcraft.gui.rule.players_per_team"),
        TEAM_COUNT("teamcraft.gui.rule.team_count");

        private final String translationKey;

        SplitRule(String translationKey) {
            this.translationKey = translationKey;
        }

        private Component displayName() {
            return Component.translatable(this.translationKey);
        }
    }

    private record PositionedWidget(AbstractWidget widget, int xOffset, int baseY, boolean enabled) {
    }

    private record ContentRow(int baseY, int height, Component text, Component tooltip) {
    }

    private record ContentHeader(int baseY, Component text) {
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
        Consumer<TeamColor> onChanged
    ) {
        private boolean contains(double mouseX, double mouseY) {
            int rows = (TeamColor.values().length + this.columns - 1) / this.columns;
            return mouseX >= this.x && mouseX < this.x + this.columnWidth * this.columns
                && mouseY >= this.y && mouseY < this.y + this.rowHeight * rows;
        }

        private boolean anchorContains(double mouseX, double mouseY) {
            return mouseX >= this.anchorX && mouseX < this.anchorX + this.anchorWidth
                && mouseY >= this.anchorY && mouseY < this.anchorY + this.anchorHeight;
        }
    }

    private record DraftResult(TeamcraftConfigData config, Component error) {
        private static DraftResult error(Component error) {
            return new DraftResult(null, error);
        }
    }
}
