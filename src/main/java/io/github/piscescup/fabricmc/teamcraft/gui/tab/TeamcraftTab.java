package io.github.piscescup.fabricmc.teamcraft.gui.tab;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.piscescup.fabricmc.teamcraft.gui.TeamcraftCandidateEditor;
import io.github.piscescup.fabricmc.teamcraft.gui.TeamcraftConfigResponseReceiver;
import io.github.piscescup.fabricmc.teamcraft.gui.TeamcraftPageHost;
import io.github.piscescup.fabricmc.teamcraft.gui.TeamcraftFeedbackScreen;
import io.github.piscescup.fabricmc.teamcraft.gui.layout.TeamcraftPageLayout;
import io.github.piscescup.fabricmc.teamcraft.gui.render.TeamcraftGuiTheme;
import io.github.piscescup.fabricmc.teamcraft.gui.render.TeamcraftTooltip;
import io.github.piscescup.fabricmc.teamcraft.gui.network.ConfigSyncPayload;
import io.github.piscescup.fabricmc.teamcraft.gui.widget.*;
import io.github.piscescup.fabricmc.teamcraft.hotkey.TeamcraftHotkeys;
import io.github.piscescup.fabricmc.teamcraft.text.Msg;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftColor;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
//#if MC >= 12110
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
//#endif
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * A tab's real Minecraft Screen. Coordinates lifecycle, navigation and input delegation.
 * Content rows belong to TeamcraftContentList, geometry to layout, and pixels to render/widget.
 * Implement build() to describe a page; the host retains the shared configuration draft.
 */
@Environment(EnvType.CLIENT)
public abstract class TeamcraftTab
    extends Screen implements TeamcraftConfigResponseReceiver
{
    protected final TeamcraftPageHost host;
    private final String id;
    private final String labelKey;
    private final String tooltipKey;
    private final List<Button> navigationButtons = new ArrayList<>();
    private final List<AbstractWidget> idleOnlyWidgets = new ArrayList<>();
    private final TeamcraftContentList content;
    private final TeamcraftColorPopup colorPopup = new TeamcraftColorPopup(this::rebuildPage);
    private final TeamcraftTooltip description = new TeamcraftTooltip();
    private TeamcraftPageLayout layout;
    private KeyMapping editingBinding;

    protected TeamcraftTab(TeamcraftPageHost host, String id,
                           String labelKey, String tooltipKey) {
        super(Objects.requireNonNull(host, "host").title());
        this.host = host;
        this.id = Objects.requireNonNull(id, "id");
        this.labelKey = Objects.requireNonNull(labelKey, "labelKey");
        this.tooltipKey = Objects.requireNonNull(tooltipKey, "tooltipKey");
        this.content = new TeamcraftContentList(
            this::addWidget, this.host::waitingForServer,
            () -> this instanceof TeamcraftCandidateEditor, this::candidateNames, this::rebuildPage
        );
    }

    public final String id() {
        return this.id;
    }

    public final Component displayName() {
        return Component.translatable(this.labelKey);
    }

    public final Component navigationTooltip() {
        return Component.translatable(this.tooltipKey);
    }

    /** Local pages can return false to appear in the server-independent Mod Menu view. */
    public boolean requiresServer() {
        return true;
    }

    protected Component headerHint() {
        return null;
    }

    /** Help already renders its explanations inline, so it can disable all hover tooltips. */
    protected boolean showHoverTooltips() {
        return true;
    }

    protected abstract void build();

    /** Pages without special actions automatically receive a Close button. */
    protected List<FooterAction> footerActions() {
        return List.of(closeAction());
    }

    protected final FooterAction closeAction() {
        return new FooterAction(TeamcraftTranslations.GUI_CLOSE.key(), this::onClose, false);
    }

    protected final FooterAction action(TeamcraftTranslations label, Runnable run) {
        return action(label.key(), run);
    }

    protected final FooterAction action(String translationKey, Runnable run) {
        return new FooterAction(translationKey, run, true);
    }

    public final boolean belongsTo(TeamcraftPageHost host) {
        return this.host == host;
    }


    @Override
    protected void init() {
        this.colorPopup.close();
        this.editingBinding = null;
        this.description.reset();
        this.layout = TeamcraftPageLayout.forScreen(this.width, this.height);
        this.navigationButtons.clear();
        this.idleOnlyWidgets.clear();
        addNavigation();
        this.content.begin(this.font, this.layout.content());
        build();
        this.content.finish();
        addFooter();
        updateEnabledState();
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        TeamcraftGuiTheme.screen(graphics, this.font, this.title, this.width, this.height,
            this.layout.footerSeparatorY());
        Component hint = headerHint();
        if (hint != null) {
            int hintX = 24 + this.font.width(this.title);
            var hintLines = this.font.split(hint, Math.max(1, this.width - hintX - 10));
            if (hintX < this.width - 70 && !hintLines.isEmpty()) {
                graphics.text(this.font, hintLines.getFirst(), hintX, 6, TeamcraftGuiTheme.ERROR);
            }
        }

        // Layer order: clipped content, fixed controls, popup, then explanatory tooltip.
        Component hovered = this.content.draw(graphics, mouseX, mouseY, partialTick);
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        this.colorPopup.draw(graphics, this.font, mouseX, mouseY);
        boolean showTooltip = showHoverTooltips() && !this.colorPopup.isOpen() && this.editingBinding == null;
        this.description.draw(graphics, this.font, showTooltip ? hovered : null,
            mouseX, mouseY, this.width, this.height);
    }

    /** The shared theme supplies the translucent background. */
    @Override
    //#if MC >= 260102
    public void extractBackground(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
    //#else
    //$$ public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    //#endif
    }

    //#if MC >= 12110
    @Override
    public boolean keyPressed(@NonNull KeyEvent event) {
        if (this.editingBinding != null) {
            finishKeyBinding(event.key() == InputConstants.KEY_ESCAPE ? InputConstants.UNKNOWN : InputConstants.getKey(event));
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (captureMouseBinding(event.button())) {
            return true;
        }
        if (this.colorPopup.mouseClicked(event.x(), event.y(), event.button())) {
            return true;
        }
        if (this.content.mousePressed(event.x(), event.y(), event.button())) {
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (this.content.mouseDragged(event.x(), event.y(), event.button())) {
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (this.content.mouseReleased(event.button())) {
            return true;
        }
        return super.mouseReleased(event);
    }
    //#else
    //$$ @Override
    //$$ public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    //$$     if (this.editingBinding != null) {
    //$$         finishKeyBinding(keyCode == InputConstants.KEY_ESCAPE ? InputConstants.UNKNOWN : InputConstants.getKey(keyCode, scanCode));
    //$$         return true;
    //$$     }
    //$$     return super.keyPressed(keyCode, scanCode, modifiers);
    //$$ }
    //$$
    //$$ @Override
    //$$ public boolean mouseClicked(double mouseX, double mouseY, int button) {
    //$$     if (captureMouseBinding(button)) {
    //$$         return true;
    //$$     }
    //$$     if (this.colorPopup.mouseClicked(mouseX, mouseY, button)) {
    //$$         return true;
    //$$     }
    //$$     if (this.content.mousePressed(mouseX, mouseY, button)) {
    //$$         return true;
    //$$     }
    //$$     return super.mouseClicked(mouseX, mouseY, button);
    //$$ }
    //$$
    //$$ @Override
    //$$ public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
    //$$     if (this.content.mouseDragged(mouseX, mouseY, button)) {
    //$$         return true;
    //$$     }
    //$$     return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    //$$ }
    //$$
    //$$ @Override
    //$$ public boolean mouseReleased(double mouseX, double mouseY, int button) {
    //$$     if (this.content.mouseReleased(button)) {
    //$$         return true;
    //$$     }
    //$$     return super.mouseReleased(mouseX, mouseY, button);
    //$$ }
    //#endif


    protected final void beginKeyBinding(KeyMapping mapping, Button button) {
        this.editingBinding = mapping;
        button.setMessage(Component.translatable(TeamcraftTranslations.GUI_HOTKEYS_WAITING.key())
            .withStyle(ChatFormatting.YELLOW));
    }

    private boolean captureMouseBinding(int button) {
        if (this.editingBinding == null) {
            return false;
        }
        finishKeyBinding(InputConstants.Type.MOUSE.getOrCreate(button));
        return true;
    }

    private void finishKeyBinding(InputConstants.Key key) {
        TeamcraftHotkeys.bind(this.editingBinding, key);
        this.editingBinding = null;
        rebuildPage();
    }


    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (this.editingBinding != null) { return true; }
        this.colorPopup.close();
        if (super.mouseScrolled(mouseX, mouseY, horizontal, vertical)) { return true; }
        return this.content.mouseScrolled(mouseX, mouseY, vertical);
    }

    @Override
    public void onClose() { this.host.close(); }

    @Override
    public void removed() {
        cancelTransientInput();
        super.removed();
    }

    public final void cancelTransientInput() {
        this.editingBinding = null;
        this.colorPopup.close();
        this.description.reset();
        this.content.cancelDrag();
    }

    private List<String> candidateNames() {
        return this instanceof TeamcraftCandidateEditor editor ? editor.candidateNames() : List.of();
    }

    @Override
    public boolean isPauseScreen() { return false; }

    //#if MC >= 12110
    @Override
    public boolean isInGameUi() { return this.host.isInGameUi(); }
    //#endif

    @Override
    public void handleServerResponse(ConfigSyncPayload payload) {
        if (this.host instanceof TeamcraftConfigResponseReceiver receiver) {
            receiver.handleServerResponse(payload);
        }
    }

    private void addNavigation() {
        var panel = this.layout.panel();
        int x = panel.left();
        int y = TeamcraftPageLayout.TAB_TOP;
        for (TeamcraftTab candidate : this.host.pages()) {
            Component label = candidate.displayName();
            int width = Math.clamp(this.font.width(label) + 16, 54, panel.right() - panel.left());
            if (x > panel.left() && x + width > panel.right()) {
                x = panel.left();
                y += TeamcraftPageLayout.CONTROL_HEIGHT + TeamcraftPageLayout.TAB_GAP;
            }
            TeamcraftButton button = addRenderableWidget(TeamcraftButton.themedBuilder(label, ignored -> this.host.switchPage(candidate.id()))
                .bounds(x, y, width, TeamcraftPageLayout.CONTROL_HEIGHT)
                .tooltip(showHoverTooltips() ? Tooltip.create(candidate.navigationTooltip()) : null)
                .build());
            button.setTooltipDelay(TeamcraftTooltip.DELAY);
            button.selected(candidate == this);
            this.navigationButtons.add(button);
            x += width + TeamcraftPageLayout.TAB_GAP;
        }
        this.layout = this.layout.withTabsBottom(y + TeamcraftPageLayout.CONTROL_HEIGHT);
    }

    private void addFooter() {
        var panel = this.layout.panel();
        List<FooterAction> actions = footerActions();
        if (actions.isEmpty()) {
            return;
        }

        int gap = 4;
        int available = panel.right() - panel.left();
        int maximumWidth = Math.max(1, (available - gap * (actions.size() - 1)) / actions.size());
        int x = panel.left();
        int y = this.layout.footerY();
        for (FooterAction action : actions) {
            int buttonWidth = Math.clamp(this.font.width(Component.translatable(action.translationKey())) + 20, 64, maximumWidth);
            Button button = addRenderableWidget(TeamcraftButton.themedBuilder(
                Component.translatable(action.translationKey()),
                ignored -> action.run().run()
            ).bounds(x, y, buttonWidth, 20)
                .tooltip(showHoverTooltips() ? Tooltip.create(footerTooltip(action.translationKey())) : null)
                .build());
            button.setTooltipDelay(TeamcraftTooltip.DELAY);
            if (action.idleOnly()) {
                this.idleOnlyWidgets.add(button);
            }
            x += buttonWidth + gap;
        }
    }


    protected final void rebuildPage() {
        rebuildWidgets();
    }

    public final void refreshWidgets() {
        rebuildPage();
    }

    public final void resetScroll() {
        this.content.resetScroll();
    }

    public void updateEnabledState() {
        for (Button button : this.navigationButtons) {
            button.active = !this.host.waitingForServer();
        }
        for (AbstractWidget widget : this.idleOnlyWidgets) {
            widget.active = !this.host.waitingForServer() && widget.visible;
        }
        this.content.updateEnabledState();
    }

    protected final TeamcraftButton button(Component label, Button.OnPress onPress) {
        return TeamcraftButton.themedBuilder(label, onPress).build()
            .hoverHint(Component.translatable(TeamcraftTranslations.GUI_BUTTON_ACTION.key(), label));
    }

    protected final <T> Button cycleButton(List<T> values, T initial,
                                          Function<T, Component> label, Consumer<T> onChanged) {
        return new TeamcraftCycleButton<>(values, initial, label, onChanged);
    }

    protected final void registerCandidateRow(String playerName) {
        this.content.registerCandidateRow(playerName);
    }

    protected final void openScreen(Screen screen) {
        //#if MC >= 260200
        this.minecraft.gui.setScreen(screen);
        //#else
        //$$ this.minecraft.setScreen(screen);
        //#endif
    }

    protected final void addHeader(String key) { this.content.addHeader(key); }
    protected final void addSection(String key, Runnable contents) { this.content.addSection(key, contents); }

    protected final void addSection(String id, Component label, String subtitle,
                                    Component tooltip, boolean initiallyExpanded, Runnable contents) {
        this.content.addSection(id, label, subtitle, tooltip, initiallyExpanded, contents);
    }

    protected final void addTextRow(Component text, Component tooltip) {
        this.content.addTextRow(text, tooltip);
    }

    protected final void addValueRow(String key, Component value) {
        this.content.addValueRow(key, value);
    }

    protected final void addFullWidget(AbstractWidget widget, Component tooltip) {
        this.content.addFullWidget(widget, tooltip);
    }

    protected final void addTwoWidgets(AbstractWidget left, AbstractWidget right, Component tooltip) {
        this.content.addTwoWidgets(left, right, tooltip);
    }

    protected final void addLabeledWidget(String key, AbstractWidget widget, Component tooltip) {
        this.content.addLabeledWidget(key, widget, tooltip);
    }

    protected final void addInlineActionRow(Component text, AbstractWidget action, Component tooltip) {
        this.content.addInlineActionRow(text, action, tooltip);
    }

    /** Restore one draft value; the supplied callback must not save or reset other settings. */
    protected final void addResettableWidget(String key, AbstractWidget widget, Component tooltip, Runnable reset) {
        TeamcraftButton resetButton = button(Component.translatable(TeamcraftTranslations.GUI_RESET.key()), ignored -> {
            reset.run();
            rebuildPage();
        });
        resetButton.hoverHint(Component.translatable(TeamcraftTranslations.GUI_RESET_TOOLTIP.key()));
        this.content.addLabeledWidget(key, widget, resetButton, tooltip);
    }

    protected final void addHotkeyRow(String key, TeamcraftButton bind, TeamcraftButton clear,
                                      TeamcraftButton reset, Component tooltip, boolean canClear, boolean canReset) {
        this.content.addHotkeyRow(key, bind, clear, reset, tooltip, canClear, canReset);
    }

    protected final EditBox numberBox(String key, String value, Consumer<String> responder) {
        return textBox(key, TeamcraftTranslations.GUI_PLACEHOLDER_NUMBER.key(), value, 4, responder);
    }

    protected final Button colorDropdownButton(Component label, TeamcraftColor value,
                                                Consumer<TeamcraftColor> onChanged) {
        Component message = label.copy()
            .append(Component.literal(": ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Msg.colorName(value).copy().withColor(value.textColor()))
            .append(Component.literal("  ▼").withStyle(ChatFormatting.GRAY));
        return TeamcraftButton.themedBuilder(message,
            button -> this.colorPopup.open(button, this.content.viewport().bounds(), onChanged)).build()
            .hoverHint(Component.translatable(TeamcraftTranslations.GUI_BUTTON_COLOR.key()));
    }

    protected final EditBox textBox(
        String key,
        String placeholderKey,
        String value,
        int maxLength,
        java.util.function.Consumer<String> responder
    ) {
        EditBox box = new TeamcraftEditBox(this.font, Component.translatable(key));
        box.setMaxLength(maxLength);
        box.setHint(Component.translatable(placeholderKey).withStyle(ChatFormatting.DARK_GRAY));
        box.setValue(value);
        box.setResponder(responder);
        return box;
    }


    protected final void showFeedback(Component message, TeamcraftFeedbackScreen.Type type) {
        TeamcraftFeedbackScreen.open(this, message, type);
    }

    protected final void showFeedback(String translationKey, TeamcraftFeedbackScreen.Type type) {
        showFeedback(Component.translatable(translationKey), type);
    }

    protected final void showOverlay(String translationKey, ChatFormatting color) {
        showOverlay(Component.translatable(translationKey), color);
    }

    public void showOverlay(Component message, ChatFormatting color) {
        if (this.minecraft.player != null) {
            //#if MC >= 260102
            this.minecraft.player.sendOverlayMessage(message.copy().withStyle(color));
            //#else
            //$$ this.minecraft.player.displayClientMessage(message.copy().withStyle(color), true);
            //#endif
        }
    }

    protected static Component tooltip(String key) {
        return Component.translatable(TeamcraftTranslations.GUI_TOOLTIP_FUNCTION.key()).withStyle(ChatFormatting.GOLD)
            .append(Component.translatable(key + ".tooltip").withStyle(ChatFormatting.WHITE))
            .append("\n")
            .append(Component.translatable(TeamcraftTranslations.GUI_TOOLTIP_EXAMPLE.key()).withStyle(ChatFormatting.YELLOW))
            .append(Component.translatable(key + ".example").withStyle(ChatFormatting.AQUA));
    }

    private static Component footerTooltip(String key) {
        TeamcraftTranslations purpose = switch (key) {
            case "teamcraft.gui.close" -> TeamcraftTranslations.GUI_BUTTON_CLOSE;
            case "teamcraft.gui.defaults" -> TeamcraftTranslations.GUI_BUTTON_DEFAULTS;
            case "teamcraft.gui.apply" -> TeamcraftTranslations.GUI_BUTTON_APPLY;
            case "teamcraft.gui.split" -> TeamcraftTranslations.GUI_BUTTON_SPLIT;
            case "teamcraft.gui.refresh" -> TeamcraftTranslations.GUI_BUTTON_REFRESH;
            case "teamcraft.gui.save_team" -> TeamcraftTranslations.GUI_BUTTON_SAVE_TEAM;
            case "teamcraft.gui.leave_team" -> TeamcraftTranslations.GUI_LEAVE_TEAM_TOOLTIP;
            case "teamcraft.gui.all_teams.clear" -> TeamcraftTranslations.GUI_BUTTON_CLEAR_TEAMS;
            default -> null;
        };
        return purpose == null
            ? Component.translatable(TeamcraftTranslations.GUI_BUTTON_ACTION.key(), Component.translatable(key))
            : Component.translatable(purpose.key());
    }

    protected static Component tooltip(String tooltipKey, String exampleKey) {
        return Component.translatable(TeamcraftTranslations.GUI_TOOLTIP_FUNCTION.key()).withStyle(ChatFormatting.GOLD)
            .append(Component.translatable(tooltipKey).withStyle(ChatFormatting.WHITE))
            .append("\n")
            .append(Component.translatable(TeamcraftTranslations.GUI_TOOLTIP_EXAMPLE.key()).withStyle(ChatFormatting.YELLOW))
            .append(Component.translatable(exampleKey).withStyle(ChatFormatting.AQUA));
    }


    /** idleOnly actions are disabled while the screen is waiting for a server response. */
    public record FooterAction(String translationKey, Runnable run, boolean idleOnly) {
        public FooterAction {
            Objects.requireNonNull(translationKey, "translationKey");
            Objects.requireNonNull(run, "run");
        }
    }
}
