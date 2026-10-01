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
import net.minecraft.client.gui.components.CycleButton;
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
import java.util.LinkedHashSet;
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
    private List<String> candidates;
    private List<String> onlinePlayers;
    private List<TeamcraftColor> configuredColors;
    private TeamcraftColor selectedColor = TeamcraftColor.RED;
    private List<String> configuredNames;
    private String pendingName = "";

    private TeamInfoData ownTeam;
    private List<TeamInfoData> teams;
    private String ownTeamName;
    private TeamcraftColor ownTeamcraftColor = TeamcraftColor.WHITE;
    private boolean ownTeamFriendlyFire;
    private boolean waitingForServer;
    private ColorMenu colorMenu;

    public TeamcraftConfigScreen(
        Screen parent,
        TeamcraftConfigData config,
        List<String> candidates,
        List<String> onlinePlayers,
        TeamInfoData ownTeam,
        List<TeamInfoData> teams
    ) {
        super(Component.translatable(TeamcraftTranslations.GUI_TITLE.key()));
        this.parent = parent;
        loadConfig(config);
        loadCandidates(candidates, onlinePlayers);
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
            case HELP -> addHelpPage();
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
        return super.mouseClicked(event, doubleClick);
    }
    //#else
    //$$ @Override
    //$$ public boolean mouseClicked(double mouseX, double mouseY, int button) {
    //$$     if (this.handleColorMenuClick(mouseX, mouseY, button)) {
    //$$         return true;
    //$$     }
    //$$     return super.mouseClicked(mouseX, mouseY, button);
    //$$ }
    //#endif

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
            if (menu.anchorContains(x, y)) {
                return true;
            }
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
        return true;
    }
    //#endif

    public void handleServerResponse(ConfigSyncPayload payload) {
        this.waitingForServer = false;

        switch (payload.response()) {
            case OPENED -> {
                loadConfig(payload.config());
                loadCandidates(payload.candidates(), payload.onlinePlayers());
                loadTeams(payload.ownTeam(), payload.teams());
                showOverlay(TeamcraftTranslations.GUI_REFRESHED.key(), ChatFormatting.GREEN);
            }
            case SAVED -> {
                loadConfig(payload.config());
                loadCandidates(payload.candidates(), payload.onlinePlayers());
                loadTeams(payload.ownTeam(), payload.teams());
                showOverlay(TeamcraftTranslations.GUI_SAVED.key(), ChatFormatting.GREEN);
            }
            case BUILT -> {
                loadConfig(payload.config());
                loadCandidates(payload.candidates(), payload.onlinePlayers());
                loadTeams(payload.ownTeam(), payload.teams());
                this.page = Page.ALL_TEAMS;
                this.scrollOffset = 0;
                showOverlay(TeamcraftTranslations.GUI_BUILT.key(), ChatFormatting.GREEN);
            }
            case TEAM_SAVED -> {
                loadTeams(payload.ownTeam(), payload.teams());
                showOverlay(TeamcraftTranslations.GUI_TEAM_SAVED.key(), ChatFormatting.GREEN);
            }
            case INVALID -> showOverlay(TeamcraftTranslations.GUI_ERROR_INVALID_SERVER.key(), ChatFormatting.RED);
            case PERMISSION_DENIED -> {
                showOverlay(TeamcraftTranslations.GUI_ERROR_PERMISSION.key(), ChatFormatting.RED);
                onClose();
                return;
            }
            case NO_CANDIDATES -> {
                loadConfig(payload.config());
                loadCandidates(payload.candidates(), payload.onlinePlayers());
                showOverlay(TeamcraftTranslations.GUI_ERROR_NO_CANDIDATES.key(), ChatFormatting.RED);
            }
            case TEAMS_EXIST -> {
                loadConfig(payload.config());
                loadCandidates(payload.candidates(), payload.onlinePlayers());
                loadTeams(payload.ownTeam(), payload.teams());
                showOverlay(TeamcraftTranslations.GUI_ERROR_TEAMS_EXIST.key(), ChatFormatting.RED);
            }
            case TOO_MANY_TEAMS -> {
                loadConfig(payload.config());
                loadCandidates(payload.candidates(), payload.onlinePlayers());
                showOverlay(TeamcraftTranslations.GUI_ERROR_TOO_MANY_TEAMS.key(), ChatFormatting.RED);
            }
            case TEAM_NOT_FOUND -> {
                loadTeams(payload.ownTeam(), payload.teams());
                showOverlay(TeamcraftTranslations.GUI_ERROR_TEAM_NOT_FOUND.key(), ChatFormatting.RED);
            }
            case TEAM_DISBANDED -> {
                loadTeams(payload.ownTeam(), payload.teams());
                this.page = Page.ALL_TEAMS;
                this.scrollOffset = 0;
                showOverlay(TeamcraftTranslations.GUI_DETAILS_DISBANDED.key(), ChatFormatting.GREEN);
            }
            case ALL_TEAMS_CLEARED -> {
                loadTeams(payload.ownTeam(), payload.teams());
                this.page = Page.ALL_TEAMS;
                this.scrollOffset = 0;
                showOverlay(TeamcraftTranslations.GUI_ALL_TEAMS_CLEARED.key(), ChatFormatting.GREEN);
            }
        }
        rebuildWidgets();
    }

    private void calculateBounds() {
        int panelWidth = Math.max(1, this.width - 16);
        int panelHeight = Math.max(1, this.height - 16);
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
        addHeader(TeamcraftTranslations.GUI_CATEGORY_CANDIDATES.key());
        addTextRow(
            Component.translatable(
                TeamcraftTranslations.GUI_CANDIDATES_SUMMARY.key(),
                this.candidates.size(),
                this.onlinePlayers.size()
            ).withStyle(ChatFormatting.GRAY),
            tooltip(TeamcraftTranslations.GUI_CATEGORY_CANDIDATES.key())
        );

        Button selectAll = Button.builder(Component.translatable(TeamcraftTranslations.GUI_CANDIDATES_SELECT_ALL.key()), ignored -> {
            LinkedHashSet<String> selected = new LinkedHashSet<>(this.candidates);
            for (String playerName : this.onlinePlayers) {
                if (selected.size() >= TeamcraftConfigData.MAX_CANDIDATES) {
                    break;
                }
                selected.add(playerName);
            }
            this.candidates = new ArrayList<>(selected);
            rebuildWidgets();
        }).build();
        Button clear = Button.builder(Component.translatable(TeamcraftTranslations.GUI_CANDIDATES_CLEAR.key()), ignored -> {
            this.candidates.clear();
            rebuildWidgets();
        }).build();
        addTwoWidgets(selectAll, clear, tooltip(TeamcraftTranslations.GUI_CATEGORY_CANDIDATES.key()));

        LinkedHashSet<String> displayedPlayers = new LinkedHashSet<>(this.candidates);
        displayedPlayers.addAll(this.onlinePlayers);
        if (displayedPlayers.isEmpty()) {
            addTextRow(
                Component.translatable(TeamcraftTranslations.GUI_CANDIDATES_NONE_ONLINE.key()).withStyle(ChatFormatting.GRAY),
                tooltip(TeamcraftTranslations.GUI_CATEGORY_CANDIDATES.key())
            );
        }
        for (String playerName : displayedPlayers) {
            boolean selected = this.candidates.contains(playerName);
            boolean online = this.onlinePlayers.contains(playerName);
            Component message = Component.literal(selected ? "☑ " : "☐ ")
                .append(Component.literal(playerName).withStyle(selected ? ChatFormatting.WHITE : ChatFormatting.GRAY))
                .append(Component.literal("  •  ").withStyle(ChatFormatting.DARK_GRAY))
                .append(Component.translatable(
                    online ? TeamcraftTranslations.GUI_CANDIDATES_ONLINE.key() : TeamcraftTranslations.GUI_CANDIDATES_OFFLINE.key()
                ).withStyle(online ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
            Button player = Button.builder(message, ignored -> {
                if (this.candidates.remove(playerName)) {
                    rebuildWidgets();
                    return;
                }
                if (this.candidates.size() < TeamcraftConfigData.MAX_CANDIDATES) {
                    this.candidates.add(playerName);
                    rebuildWidgets();
                }
            }).build();
            addFullWidget(player, tooltip(
                TeamcraftTranslations.GUI_CANDIDATES_PLAYER_TOOLTIP.key(),
                TeamcraftTranslations.GUI_CANDIDATES_PLAYER_EXAMPLE.key()
            ));
        }

        addHeader(TeamcraftTranslations.GUI_CATEGORY_SPLIT.key());

        //#if MC >= 12111
        CycleButton<SplitRule> rule = CycleButton.builder(SplitRule::displayName, currentRule())
            .withValues(SplitRule.values())
            .create(Component.translatable(TeamcraftTranslations.GUI_OPTION_RULE.key()), (button, value) -> {
                this.fixedTeamCount = value == SplitRule.TEAM_COUNT;
                this.scrollOffset = 0;
                rebuildWidgets();
            });
        //#else
        //$$ CycleButton<SplitRule> rule = CycleButton.builder(SplitRule::displayName)
        //$$     .withValues(SplitRule.values())
        //$$     .withInitialValue(currentRule())
        //$$     .create(Component.translatable(TeamcraftTranslations.GUI_OPTION_RULE.key()), (button, value) -> {
        //$$         this.fixedTeamCount = value == SplitRule.TEAM_COUNT;
        //$$         this.scrollOffset = 0;
        //$$         rebuildWidgets();
        //$$     });
        //#endif
        addFullWidget(rule, tooltip(TeamcraftTranslations.GUI_OPTION_RULE.key()));

        if (this.fixedTeamCount) {
            EditBox count = numberBox(
                TeamcraftTranslations.GUI_OPTION_TEAM_COUNT.key(),
                this.teamCountText,
                value -> this.teamCountText = value
            );
            addLabeledWidget(TeamcraftTranslations.GUI_OPTION_TEAM_COUNT.key(), count, tooltip(TeamcraftTranslations.GUI_OPTION_TEAM_COUNT.key()));
        }
        else {
            EditBox size = numberBox(
                TeamcraftTranslations.GUI_OPTION_PLAYERS_PER_TEAM.key(),
                this.playersPerTeamText,
                value -> this.playersPerTeamText = value
            );
            addLabeledWidget(
                TeamcraftTranslations.GUI_OPTION_PLAYERS_PER_TEAM.key(),
                size,
                tooltip(TeamcraftTranslations.GUI_OPTION_PLAYERS_PER_TEAM.key())
            );
        }

        //#if MC >= 12111
        CycleButton<SplitMode> modeButton = CycleButton.builder(SplitMode::displayName, this.mode)
            .withValues(SplitMode.values())
            .create(Component.translatable(TeamcraftTranslations.GUI_OPTION_MODE.key()), (button, value) -> this.mode = value);
        //#else
        //$$ CycleButton<SplitMode> modeButton = CycleButton.builder(SplitMode::displayName)
        //$$     .withValues(SplitMode.values())
        //$$     .withInitialValue(this.mode)
        //$$     .create(Component.translatable(TeamcraftTranslations.GUI_OPTION_MODE.key()), (button, value) -> this.mode = value);
        //#endif
        addFullWidget(modeButton, tooltip(TeamcraftTranslations.GUI_OPTION_MODE.key()));

        CycleButton<Boolean> friendlyFireButton = CycleButton.onOffBuilder(this.friendlyFire)
            .create(
                Component.translatable(TeamcraftTranslations.GUI_OPTION_FRIENDLY_FIRE.key()),
                (button, value) -> this.friendlyFire = value
            );
        addFullWidget(friendlyFireButton, tooltip(TeamcraftTranslations.GUI_OPTION_FRIENDLY_FIRE.key()));

        addHeader(TeamcraftTranslations.GUI_CATEGORY_APPEARANCE.key());
        addHeader(TeamcraftTranslations.GUI_OPTION_NAMES.key());
        if (this.configuredNames.isEmpty()) {
            addTextRow(
                Component.translatable(TeamcraftTranslations.GUI_NAMES_DEFAULT.key()).withStyle(ChatFormatting.GRAY),
                tooltip(TeamcraftTranslations.GUI_OPTION_NAMES.key())
            );
        }
        for (int i = 0; i < this.configuredNames.size(); i++) {
            int index = i;
            Button name = Button.builder(Component.translatable(
                TeamcraftTranslations.GUI_NAMES_SLOT.key(), i + 1, this.configuredNames.get(i)
            ), ignored -> { }).build();
            Button remove = Button.builder(Component.translatable(TeamcraftTranslations.GUI_NAMES_REMOVE.key()), ignored -> {
                this.configuredNames.remove(index);
                rebuildWidgets();
            }).build();
            addTwoWidgets(name, remove, tooltip(TeamcraftTranslations.GUI_OPTION_NAMES.key()));
        }

        EditBox nameInput = textBox(
            TeamcraftTranslations.GUI_OPTION_NAMES.key(),
            TeamcraftTranslations.GUI_PLACEHOLDER_NAMES.key(),
            this.pendingName,
            TeamcraftConfigData.MAX_NAME_LENGTH,
            value -> this.pendingName = value
        );
        Button addName = Button.builder(Component.translatable(TeamcraftTranslations.GUI_NAMES_ADD.key()), ignored -> {
            String value = this.pendingName.trim();
            if (!value.isEmpty() && this.configuredNames.size() < TeamcraftConfigData.MAX_LIST_SIZE) {
                this.configuredNames.add(value);
                this.pendingName = "";
                rebuildWidgets();
            }
        }).build();
        addTwoWidgets(nameInput, addName, tooltip(TeamcraftTranslations.GUI_OPTION_NAMES.key()));

        addHeader(TeamcraftTranslations.GUI_OPTION_COLORS.key());
        if (this.configuredColors.isEmpty()) {
            addTextRow(
                Component.translatable(TeamcraftTranslations.GUI_COLORS_DEFAULT_PALETTE.key()).withStyle(ChatFormatting.GRAY),
                tooltip(TeamcraftTranslations.GUI_OPTION_COLORS.key())
            );
        }
        for (int i = 0; i < this.configuredColors.size(); i++) {
            int index = i;
            Button color = colorDropdownButton(
                Component.translatable(TeamcraftTranslations.GUI_COLORS_SLOT.key(), i + 1),
                this.configuredColors.get(i),
                value -> this.configuredColors.set(index, value)
            );
            Button remove = Button.builder(Component.translatable(TeamcraftTranslations.GUI_COLORS_REMOVE.key()), ignored -> {
                this.configuredColors.remove(index);
                rebuildWidgets();
            }).build();
            addTwoWidgets(color, remove, tooltip(TeamcraftTranslations.GUI_OPTION_COLORS.key()));
        }

        Button picker = colorDropdownButton(
            Component.translatable(TeamcraftTranslations.GUI_COLORS_PICKER.key()),
            this.selectedColor,
            value -> this.selectedColor = value
        );
        Button addColor = Button.builder(Component.translatable(TeamcraftTranslations.GUI_COLORS_ADD.key()), ignored -> {
            if (this.configuredColors.size() < TeamcraftConfigData.MAX_LIST_SIZE) {
                this.configuredColors.add(this.selectedColor);
                rebuildWidgets();
            }
        }).build();
        addTwoWidgets(picker, addColor, tooltip(TeamcraftTranslations.GUI_OPTION_COLORS.key()));

        if (!this.configuredColors.isEmpty()) {
            Button useDefaults = Button.builder(Component.translatable(TeamcraftTranslations.GUI_COLORS_USE_DEFAULTS.key()), ignored -> {
                this.configuredColors.clear();
                rebuildWidgets();
            }).build();
            addFullWidget(useDefaults, tooltip(TeamcraftTranslations.GUI_OPTION_COLORS.key()));
        }

        addTextRow(
            Component.translatable(TeamcraftTranslations.GUI_SESSION_NOTE.key()).withStyle(ChatFormatting.DARK_GRAY),
            Component.translatable(TeamcraftTranslations.GUI_SESSION_NOTE.key())
        );
    }

    private void addOwnTeamPage() {
        addHeader(TeamcraftTranslations.GUI_CATEGORY_OWN_TEAM.key());
        if (this.ownTeam == null) {
            addTextRow(
                Component.translatable(TeamcraftTranslations.GUI_OWN_TEAM_NONE.key()).withStyle(ChatFormatting.GRAY),
                Component.translatable(TeamcraftTranslations.GUI_OWN_TEAM_NONE_TOOLTIP.key())
            );
            return;
        }

        addValueRow(TeamcraftTranslations.GUI_OWN_TEAM_ID.key(), Component.literal(this.ownTeam.id()).withStyle(ChatFormatting.GRAY));

        EditBox name = textBox(
            TeamcraftTranslations.GUI_OWN_TEAM_NAME.key(),
            TeamcraftTranslations.GUI_PLACEHOLDER_TEAM_NAME.key(),
            this.ownTeamName,
            TeamInfoData.MAX_DISPLAY_NAME_LENGTH,
            value -> this.ownTeamName = value
        );
        addLabeledWidget(TeamcraftTranslations.GUI_OWN_TEAM_NAME.key(), name, tooltip(TeamcraftTranslations.GUI_OWN_TEAM_NAME.key()));

        Button color = colorDropdownButton(
            Component.translatable(TeamcraftTranslations.GUI_OWN_TEAM_COLOR.key()),
            this.ownTeamcraftColor,
            value -> this.ownTeamcraftColor = value
        );
        addFullWidget(color, tooltip(TeamcraftTranslations.GUI_OWN_TEAM_COLOR.key()));

        CycleButton<Boolean> friendly = CycleButton.onOffBuilder(this.ownTeamFriendlyFire)
            .create(Component.translatable(TeamcraftTranslations.GUI_OWN_TEAM_FRIENDLY_FIRE.key()),
                (button, value) -> this.ownTeamFriendlyFire = value);
        addFullWidget(friendly, tooltip(TeamcraftTranslations.GUI_OWN_TEAM_FRIENDLY_FIRE.key()));

        addValueRow(
            TeamcraftTranslations.GUI_OWN_TEAM_MEMBERS.key(),
            Component.literal(String.join(", ", this.ownTeam.members())).withStyle(ChatFormatting.WHITE)
        );
    }

    private void addAllTeamsPage() {
        addHeader(TeamcraftTranslations.GUI_CATEGORY_ALL_TEAMS.key());
        if (this.teams.isEmpty()) {
            addTextRow(
                Component.translatable(TeamcraftTranslations.GUI_ALL_TEAMS_NONE.key()).withStyle(ChatFormatting.GRAY),
                Component.translatable(TeamcraftTranslations.GUI_ALL_TEAMS_NONE.key())
            );
            return;
        }

        for (TeamInfoData team : this.teams) {
            Component label = team.displayName().copy()
                .withColor(team.color().textColor())
                .append(Component.literal("  •  " + team.members().size())
                    .withStyle(ChatFormatting.GRAY));
            Component details = Component.translatable(
                TeamcraftTranslations.GUI_ALL_TEAMS_TOOLTIP.key(),
                team.id(),
                Msg.colorName(team.color()),
                Component.translatable(team.friendlyFire() ? TeamcraftTranslations.COMMON_ON.key() : TeamcraftTranslations.COMMON_OFF.key()),
                String.join(", ", team.members())
            );
            Button teamRow = Button.builder(label, ignored ->
                //#if MC >= 260200
                this.minecraft.gui.setScreen(new TeamDetailsScreen(this, team))
                //#else
                //$$ this.minecraft.setScreen(new TeamDetailsScreen(this, team))
                //#endif
            ).build();
            addFullWidget(teamRow, details);
        }
    }

    private void addHelpPage() {
        addHeader(TeamcraftTranslations.GUI_HELP_QUICK_START.key());
        addTextRow(
            Component.translatable(TeamcraftTranslations.GUI_HELP_CANDIDATES.key()),
            Component.translatable(TeamcraftTranslations.GUI_HELP_CANDIDATES.key())
        );
        addTextRow(
            Component.translatable(TeamcraftTranslations.GUI_HELP_SPLIT.key()),
            Component.translatable(TeamcraftTranslations.GUI_HELP_SPLIT.key())
        );
        addTextRow(
            Component.translatable(TeamcraftTranslations.GUI_HELP_APPEARANCE.key()),
            Component.translatable(TeamcraftTranslations.GUI_HELP_APPEARANCE.key())
        );
        addTextRow(
            Component.translatable(TeamcraftTranslations.GUI_HELP_BUILD.key()),
            Component.translatable(TeamcraftTranslations.GUI_HELP_BUILD.key())
        );

        addHeader(TeamcraftTranslations.GUI_HELP_MANAGEMENT_TITLE.key());
        addTextRow(
            Component.translatable(TeamcraftTranslations.GUI_HELP_MANAGE_TEAMS.key()),
            Component.translatable(TeamcraftTranslations.GUI_HELP_MANAGE_TEAMS.key())
        );
        addTextRow(
            Component.translatable(TeamcraftTranslations.GUI_HELP_REMOVE_TEAMS.key()),
            Component.translatable(TeamcraftTranslations.GUI_HELP_REMOVE_TEAMS.key())
        );

        addHeader(TeamcraftTranslations.GUI_HELP_COMMAND_TITLE.key());
        addTextRow(
            Component.translatable(TeamcraftTranslations.GUI_HELP_COMMAND_INTRO.key()),
            null
        );

        addHeader(TeamcraftTranslations.GUI_HELP_COMMAND_GENERAL_TITLE.key());
        addCommandHelp(
            "/teamcraft  |  /teamcraft help",
            TeamcraftTranslations.GUI_HELP_CMD_HELP
        );
        addCommandHelp("/teamcraft status", TeamcraftTranslations.GUI_HELP_CMD_STATUS);

        addHeader(TeamcraftTranslations.GUI_HELP_COMMAND_CANDIDATES_TITLE.key());
        addCommandHelp("/teamcraft init <players...>", TeamcraftTranslations.GUI_HELP_CMD_INIT);
        addCommandHelp("/teamcraft init add <players...>", TeamcraftTranslations.GUI_HELP_CMD_INIT_ADD);
        addCommandHelp("/teamcraft init remove <players...>", TeamcraftTranslations.GUI_HELP_CMD_INIT_REMOVE);
        addCommandHelp("/teamcraft init list", TeamcraftTranslations.GUI_HELP_CMD_INIT_LIST);
        addCommandHelp("/teamcraft init clear", TeamcraftTranslations.GUI_HELP_CMD_INIT_CLEAR);

        addHeader(TeamcraftTranslations.GUI_HELP_COMMAND_CONFIG_TITLE.key());
        addCommandHelp(
            "/teamcraft config players-per-team <players>",
            TeamcraftTranslations.GUI_HELP_CMD_CONFIG_PLAYERS_PER_TEAM
        );
        addCommandHelp(
            "/teamcraft config team-count <teams>",
            TeamcraftTranslations.GUI_HELP_CMD_CONFIG_TEAM_COUNT
        );
        addCommandHelp(
            "/teamcraft config mode <fixed|random>",
            TeamcraftTranslations.GUI_HELP_CMD_CONFIG_MODE
        );
        addCommandHelp(
            "/teamcraft config friendlyfire <true|false>",
            TeamcraftTranslations.GUI_HELP_CMD_CONFIG_FRIENDLY_FIRE
        );
        addCommandHelp(
            "/teamcraft config colors <colors...>",
            TeamcraftTranslations.GUI_HELP_CMD_CONFIG_COLORS
        );
        addCommandHelp(
            "/teamcraft config colors reset",
            TeamcraftTranslations.GUI_HELP_CMD_CONFIG_COLORS_RESET
        );
        addCommandHelp(
            "/teamcraft config names <names...>",
            TeamcraftTranslations.GUI_HELP_CMD_CONFIG_NAMES
        );
        addCommandHelp(
            "/teamcraft config names reset",
            TeamcraftTranslations.GUI_HELP_CMD_CONFIG_NAMES_RESET
        );
        addCommandHelp("/teamcraft config reset", TeamcraftTranslations.GUI_HELP_CMD_CONFIG_RESET);

        addHeader(TeamcraftTranslations.GUI_HELP_COMMAND_BUILD_TITLE.key());
        addCommandHelp("/teamcraft build-teams", TeamcraftTranslations.GUI_HELP_CMD_BUILD);
        addCommandHelp(
            "/teamcraft build-teams colors <colors...>",
            TeamcraftTranslations.GUI_HELP_CMD_BUILD_COLORS
        );
        addCommandHelp(
            "/teamcraft build-teams names <names...>",
            TeamcraftTranslations.GUI_HELP_CMD_BUILD_NAMES
        );

        addHeader(TeamcraftTranslations.GUI_HELP_COMMAND_TEAM_TITLE.key());
        addCommandHelp(
            "/teamcraft team-manage <team> color <color>",
            TeamcraftTranslations.GUI_HELP_CMD_TEAM_COLOR
        );
        addCommandHelp(
            "/teamcraft team-manage <team> name <name>",
            TeamcraftTranslations.GUI_HELP_CMD_TEAM_NAME
        );
        addCommandHelp(
            "/teamcraft team-manage <team> friendlyfire <true|false>",
            TeamcraftTranslations.GUI_HELP_CMD_TEAM_FRIENDLY_FIRE
        );
        addCommandHelp(
            "/teamcraft team-manage <team> info",
            TeamcraftTranslations.GUI_HELP_CMD_TEAM_INFO
        );

        addHeader(TeamcraftTranslations.GUI_HELP_COMMAND_CLEANUP_TITLE.key());
        addCommandHelp("/teamcraft clear", TeamcraftTranslations.GUI_HELP_CMD_CLEAR);
        addCommandHelp("/teamcraft reset", TeamcraftTranslations.GUI_HELP_CMD_RESET);
    }

    private void addCommandHelp(String command, TeamcraftTranslations details) {
        Component text = Component.literal(command)
            .withStyle(ChatFormatting.YELLOW, ChatFormatting.BOLD)
            .append("\n")
            .append(Component.translatable(details.key()).withStyle(ChatFormatting.GRAY));
        addTextRow(text, null);
    }

    private void addFooter() {
        List<FooterAction> actions = switch (this.page) {
            case TEAM_CONFIG -> List.of(
                new FooterAction(TeamcraftTranslations.GUI_DEFAULTS.key(), this::restoreDefaults, true),
                new FooterAction(TeamcraftTranslations.GUI_CLOSE.key(), this::onClose, false),
                new FooterAction(TeamcraftTranslations.GUI_APPLY.key(), () -> submitConfig(false), true),
                new FooterAction(TeamcraftTranslations.GUI_SPLIT.key(), () -> submitConfig(true), true)
            );
            case OWN_TEAM -> List.of(
                new FooterAction(TeamcraftTranslations.GUI_CLOSE.key(), this::onClose, false),
                new FooterAction(TeamcraftTranslations.GUI_REFRESH.key(), this::refresh, true),
                new FooterAction(TeamcraftTranslations.GUI_SAVE_TEAM.key(), this::submitOwnTeam, true)
            );
            case ALL_TEAMS -> List.of(
                new FooterAction(TeamcraftTranslations.GUI_CLOSE.key(), this::onClose, false),
                new FooterAction(TeamcraftTranslations.GUI_REFRESH.key(), this::refresh, true),
                new FooterAction(TeamcraftTranslations.GUI_ALL_TEAMS_CLEAR.key(), this::clearAllTeams, true)
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

    private void submitConfig(boolean buildTeams) {
        DraftResult draft = createConfigDraft();
        if (draft.error != null) {
            showOverlay(draft.error, ChatFormatting.RED);
            return;
        }
        this.waitingForServer = true;
        updateEnabledState();
        if (!TeamcraftConfigClient.save(draft.config, this.candidates, buildTeams)) {
            this.waitingForServer = false;
            updateEnabledState();
        }
    }

    private void submitOwnTeam() {
        if (this.ownTeam == null) {
            showOverlay(TeamcraftTranslations.GUI_ERROR_TEAM_NOT_FOUND.key(), ChatFormatting.RED);
            return;
        }
        String name = this.ownTeamName.trim();
        if (name.isEmpty()) {
            showOverlay(TeamcraftTranslations.GUI_ERROR_EMPTY_TEAM_NAME.key(), ChatFormatting.RED);
            return;
        }
        this.waitingForServer = true;
        updateEnabledState();
        if (!TeamcraftConfigClient.saveOwnTeam(
            this.ownTeam.id(),
            name,
            this.ownTeamcraftColor,
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

    private void clearAllTeams() {
        this.waitingForServer = true;
        updateEnabledState();
        if (!TeamcraftConfigClient.clearAllTeams()) {
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
                TeamcraftTranslations.GUI_ERROR_PLAYERS_PER_TEAM.key(),
                TeamcraftConfigData.MIN_PLAYERS_PER_TEAM,
                TeamcraftConfigData.MAX_PLAYERS_PER_TEAM
            ));
        }
        if (this.fixedTeamCount && teamCount == null) {
            return DraftResult.error(Component.translatable(
                TeamcraftTranslations.GUI_ERROR_TEAM_COUNT.key(),
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

        if (this.configuredNames.size() > TeamcraftConfigData.MAX_LIST_SIZE) {
            return DraftResult.error(Component.translatable(
                TeamcraftTranslations.GUI_ERROR_TOO_MANY_VALUES.key(),
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
            this.configuredNames
        ), null);
    }

    private void restoreDefaults() {
        loadConfig(TeamcraftConfigData.defaults());
        this.scrollOffset = 0;
        rebuildWidgets();
        showOverlay(TeamcraftTranslations.GUI_DEFAULTS_READY.key(), ChatFormatting.YELLOW);
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
        this.configuredNames = new ArrayList<>(config.names());
        this.pendingName = "";
    }

    private void loadCandidates(List<String> candidates, List<String> onlinePlayers) {
        this.candidates = new ArrayList<>(candidates);
        this.onlinePlayers = List.copyOf(onlinePlayers);
    }

    private void loadTeams(TeamInfoData ownTeam, List<TeamInfoData> teams) {
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

    private void addHeader(String key) {
        this.contentHeaders.add(new ContentHeader(this.cursorY, Component.translatable(key)));
        this.cursorY += HEADER_HEIGHT;
    }

    private void addTextRow(Component text, Component tooltip) {
        List<FormattedCharSequence> lines = this.font.split(text, Math.max(40, contentWidth() - 16));
        int height = Math.max(ROW_HEIGHT, lines.size() * (this.font.lineHeight + 1) + 10);
        this.contentRows.add(new ContentRow(this.cursorY, height, lines, tooltip));
        this.cursorY += height + ROW_GAP;
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
        this.contentRows.add(new ContentRow(this.cursorY, ROW_HEIGHT, List.of(), tooltip));
        this.cursorY += ROW_HEIGHT + ROW_GAP;
    }

    private void addTwoWidgets(AbstractWidget left, AbstractWidget right, Component tooltip) {
        configureTooltip(left, tooltip);
        configureTooltip(right, tooltip);
        int gap = 6;
        int width = (contentWidth() - 8 - gap) / 2;
        addContentWidget(left, 4, this.cursorY + 2, width, WIDGET_HEIGHT, true);
        addContentWidget(right, 4 + width + gap, this.cursorY + 2, width, WIDGET_HEIGHT, true);
        this.contentRows.add(new ContentRow(this.cursorY, ROW_HEIGHT, List.of(), tooltip));
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

    private EditBox numberBox(String key, String value, java.util.function.Consumer<String> responder) {
        return textBox(key, TeamcraftTranslations.GUI_PLACEHOLDER_NUMBER.key(), value, 4, responder);
    }

    private Button colorDropdownButton(Component label, TeamcraftColor value, Consumer<TeamcraftColor> onChanged) {
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
                int tooltipWidth = Math.max(120, Math.min(TOOLTIP_MAX_WIDTH, this.width - 32));
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
            //#if MC >= 260102
            this.minecraft.player.sendOverlayMessage(message.copy().withStyle(color));
            //#else
            //$$ this.minecraft.player.displayClientMessage(message.copy().withStyle(color), true);
            //#endif
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

    private static Component tooltip(String tooltipKey, String exampleKey) {
        return Component.translatable(tooltipKey)
            .append("\n")
            .append(Component.translatable(exampleKey).withStyle(ChatFormatting.GRAY));
    }

    private static void configureTooltip(AbstractWidget widget, Component tooltip) {
        widget.setTooltip(Tooltip.create(tooltip));
        widget.setTooltipDelay(TOOLTIP_DELAY);
    }

    private enum Page {
        TEAM_CONFIG(TeamcraftTranslations.GUI_NAV_TEAM_CONFIG.key(), TeamcraftTranslations.GUI_NAV_TEAM_CONFIG_TOOLTIP.key()),
        OWN_TEAM(TeamcraftTranslations.GUI_NAV_OWN_TEAM.key(), TeamcraftTranslations.GUI_NAV_OWN_TEAM_TOOLTIP.key()),
        ALL_TEAMS(TeamcraftTranslations.GUI_NAV_ALL_TEAMS.key(), TeamcraftTranslations.GUI_NAV_ALL_TEAMS_TOOLTIP.key()),
        HELP(TeamcraftTranslations.GUI_NAV_HELP.key(), TeamcraftTranslations.GUI_NAV_HELP_TOOLTIP.key());

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
        PLAYERS_PER_TEAM(TeamcraftTranslations.GUI_RULE_PLAYERS_PER_TEAM.key()),
        TEAM_COUNT(TeamcraftTranslations.GUI_RULE_TEAM_COUNT.key());

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

    private record ContentRow(int baseY, int height, List<FormattedCharSequence> textLines, Component tooltip) {
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

    private record DraftResult(TeamcraftConfigData config, Component error) {
        private static DraftResult error(Component error) {
            return new DraftResult(null, error);
        }
    }
}
