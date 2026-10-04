package io.github.piscescup.fabricmc.teamcraft.gui.tab;

import io.github.piscescup.fabricmc.teamcraft.gui.TeamcraftCandidateEditor;
import io.github.piscescup.fabricmc.teamcraft.gui.TeamcraftConfigActions;
import io.github.piscescup.fabricmc.teamcraft.gui.TeamcraftPageContext;
import io.github.piscescup.fabricmc.teamcraft.gui.TeamcraftPages;

import io.github.piscescup.fabricmc.teamcraft.config.TeamcraftConfigData;
import io.github.piscescup.fabricmc.teamcraft.config.TeamcraftSplitRule;
import io.github.piscescup.fabricmc.teamcraft.gui.widget.TeamcraftButton;
import io.github.piscescup.fabricmc.teamcraft.team.SplitMode;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import io.github.piscescup.fabricmc.teamcraft.text.Msg;

/** Builds the candidate, split-rule, name, and color controls. */
public final class TeamcraftTeamConfigTab
    extends TeamcraftTab
    implements TeamcraftCandidateEditor
{
    private final TeamcraftPageContext context;

    public TeamcraftTeamConfigTab(TeamcraftPageContext context) {
        super(context, TeamcraftPages.TEAM_CONFIG, TeamcraftTranslations.GUI_NAV_TEAM_CONFIG.key(),
            TeamcraftTranslations.GUI_NAV_TEAM_CONFIG_TOOLTIP.key());
        this.context = context;
    }

    @Override
    public List<String> candidateNames() {
        return this.context.candidates;
    }

    @Override
    protected Component headerHint() {
        return Component.translatable(TeamcraftTranslations.GUI_EXPAND_HINT.key());
    }

    @Override
    protected List<FooterAction> footerActions() {
        return List.of(
            action(TeamcraftTranslations.GUI_DEFAULTS, () -> TeamcraftConfigActions.restoreDefaults(this.context)),
            closeAction(),
            action(TeamcraftTranslations.GUI_APPLY, () -> TeamcraftConfigActions.submitConfig(this.context, false)),
            action(TeamcraftTranslations.GUI_SPLIT, () -> TeamcraftConfigActions.submitConfig(this.context, true))
        );
    }

    @Override
    protected void build() {
        addHeader(TeamcraftTranslations.GUI_CATEGORY_SPLIT_CONFIG.key());
        addSection(TeamcraftTranslations.GUI_CATEGORY_CANDIDATES.key(), this::addCandidates);
        addSection(TeamcraftTranslations.GUI_CATEGORY_SPLIT.key(), this::addSplitSettings);
        addAppearanceSettings();
        addTextRow(
            Component.translatable(TeamcraftTranslations.GUI_SESSION_NOTE.key()).withStyle(ChatFormatting.DARK_GRAY),
            Component.translatable(TeamcraftTranslations.GUI_SESSION_NOTE.key())
        );
    }

    private void addCandidates() {
        addTextRow(
            Component.translatable(
                TeamcraftTranslations.GUI_CANDIDATES_SUMMARY.key(),
                context.candidates.size(),
                context.onlinePlayers.size()
            ).withStyle(ChatFormatting.GRAY),
            tooltip(TeamcraftTranslations.GUI_CATEGORY_CANDIDATES.key())
        );

        Button selectAll = TeamcraftButton.themedBuilder(Component.translatable(
            TeamcraftTranslations.GUI_CANDIDATES_SELECT_ALL.key()), ignored -> {
                LinkedHashSet<String> selected = new LinkedHashSet<>(context.candidates);
                for (String playerName : context.onlinePlayers) {
                    if (selected.size() >= TeamcraftConfigData.MAX_CANDIDATES) {
                        break;
                    }
                    selected.add(playerName);
                }
                context.candidates = new ArrayList<>(selected);
                rebuildPage();
            }).build().hoverHint(Component.translatable(TeamcraftTranslations.GUI_BUTTON_SELECT_ALL.key()));
        Button clear = TeamcraftButton.themedBuilder(Component.translatable(
            TeamcraftTranslations.GUI_CANDIDATES_CLEAR.key()), ignored -> {
                context.candidates.clear();
                rebuildPage();
            }).build().hoverHint(Component.translatable(TeamcraftTranslations.GUI_BUTTON_CLEAR_CANDIDATES.key()));
        addTwoWidgets(
            selectAll,
            clear,
            tooltip(TeamcraftTranslations.GUI_CATEGORY_CANDIDATES.key())
        );

        LinkedHashSet<String> displayedPlayers = new LinkedHashSet<>(context.candidates);
        displayedPlayers.addAll(context.onlinePlayers);
        if (displayedPlayers.isEmpty()) {
            addTextRow(
                Component.translatable(TeamcraftTranslations.GUI_CANDIDATES_NONE_ONLINE.key())
                    .withStyle(ChatFormatting.GRAY),
                tooltip(TeamcraftTranslations.GUI_CATEGORY_CANDIDATES.key())
            );
        }
        for (String playerName : displayedPlayers) {
            addCandidate(playerName);
        }
    }

    private void addCandidate(String playerName) {
        boolean selected = context.candidates.contains(playerName);
        boolean online = context.onlinePlayers.contains(playerName);
        Component message = Component.literal(selected ? "↕  ☑ " : "☐ ")
            .append(Component.literal(playerName).withStyle(selected ? ChatFormatting.WHITE : ChatFormatting.GRAY))
            .append(Component.literal("  •  ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.translatable(
                online
                    ? TeamcraftTranslations.GUI_CANDIDATES_ONLINE.key()
                    : TeamcraftTranslations.GUI_CANDIDATES_OFFLINE.key()
            ).withStyle(online ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
        TeamcraftButton player = TeamcraftButton.themedBuilder(message, ignored -> {
            if (context.candidates.remove(playerName)) {
                rebuildPage();
                return;
            }
            if (context.candidates.size() < TeamcraftConfigData.MAX_CANDIDATES) {
                context.candidates.add(playerName);
                rebuildPage();
            }
        }).build();
        player.hoverHint(Component.translatable((selected
            ? TeamcraftTranslations.GUI_BUTTON_REMOVE_PLAYER : TeamcraftTranslations.GUI_BUTTON_SELECT_PLAYER).key(), playerName));
        if (selected) {
            registerCandidateRow(playerName);
        }
        addFullWidget(player, tooltip(
            TeamcraftTranslations.GUI_CANDIDATES_PLAYER_TOOLTIP.key(),
            TeamcraftTranslations.GUI_CANDIDATES_PLAYER_EXAMPLE.key()
        ));
    }

    private void addSplitSettings() {
        TeamcraftConfigData defaults = TeamcraftConfigData.defaults();
        Button rule = cycleButton(
            List.of(TeamcraftSplitRule.values()), context.currentRule(), TeamcraftSplitRule::displayName, value -> {
                context.fixedTeamCount = value == TeamcraftSplitRule.TEAM_COUNT;
                rebuildPage();
            });
        addResettableWidget(TeamcraftTranslations.GUI_OPTION_RULE.key(), rule,
            tooltip(TeamcraftTranslations.GUI_OPTION_RULE.key()),
            () -> context.fixedTeamCount = defaults.fixedTeamCount());

        if (context.fixedTeamCount) {
            EditBox count = numberBox(
                TeamcraftTranslations.GUI_OPTION_TEAM_COUNT.key(),
                context.teamCountText,
                value -> context.teamCountText = value
            );
            addResettableWidget(
                TeamcraftTranslations.GUI_OPTION_TEAM_COUNT.key(),
                count,
                tooltip(TeamcraftTranslations.GUI_OPTION_TEAM_COUNT.key()),
                () -> context.teamCountText = Integer.toString(defaults.teamCount())
            );
        }
        else {
            EditBox size = numberBox(
                TeamcraftTranslations.GUI_OPTION_PLAYERS_PER_TEAM.key(),
                context.playersPerTeamText,
                value -> context.playersPerTeamText = value
            );
            addResettableWidget(
                TeamcraftTranslations.GUI_OPTION_PLAYERS_PER_TEAM.key(),
                size,
                tooltip(TeamcraftTranslations.GUI_OPTION_PLAYERS_PER_TEAM.key()),
                () -> context.playersPerTeamText = Integer.toString(defaults.playersPerTeam())
            );
        }

        Button modeButton = cycleButton(
            List.of(SplitMode.values()), context.mode, SplitMode::displayName, value -> context.mode = value);
        addResettableWidget(
            TeamcraftTranslations.GUI_OPTION_MODE.key(),
            modeButton,
            tooltip(TeamcraftTranslations.GUI_OPTION_MODE.key()),
            () -> context.mode = defaults.mode()
        );

        Button friendlyFireButton = cycleButton(
            List.of(false, true), context.friendlyFire,
            value -> Msg.tr(value ? TeamcraftTranslations.COMMON_ON.key() : TeamcraftTranslations.COMMON_OFF.key())
                .withStyle(value ? ChatFormatting.GREEN : ChatFormatting.RED),
            value -> context.friendlyFire = value);
        addResettableWidget(
            TeamcraftTranslations.GUI_OPTION_FRIENDLY_FIRE.key(),
            friendlyFireButton,
            tooltip(TeamcraftTranslations.GUI_OPTION_FRIENDLY_FIRE.key()),
            () -> context.friendlyFire = defaults.friendlyFire()
        );
    }

    private void addAppearanceSettings() {
        addHeader(TeamcraftTranslations.GUI_CATEGORY_APPEARANCE.key());
        addSection(TeamcraftTranslations.GUI_OPTION_NAMES.key(), this::addNames);
        addSection(TeamcraftTranslations.GUI_OPTION_COLORS.key(), this::addColors);
    }

    private void addNames() {
        if (context.configuredNames.isEmpty()) {
            addTextRow(
                Component.translatable(TeamcraftTranslations.GUI_NAMES_DEFAULT.key())
                    .withStyle(ChatFormatting.GRAY),
                tooltip(TeamcraftTranslations.GUI_OPTION_NAMES.key())
            );
        }
        for (int index = 0; index < context.configuredNames.size(); index++) {
            int nameIndex = index;
            Button name = TeamcraftButton.themedBuilder(Component.translatable(
                TeamcraftTranslations.GUI_NAMES_SLOT.key(),
                index + 1,
                context.configuredNames.get(index)
            ), ignored -> { }).build().hoverHint(Component.translatable(TeamcraftTranslations.GUI_BUTTON_NAME_SLOT.key()));
            Button remove = TeamcraftButton.themedBuilder(Component.translatable(
                TeamcraftTranslations.GUI_NAMES_REMOVE.key()), ignored -> {
                    context.configuredNames.remove(nameIndex);
                    rebuildPage();
                }).build().hoverHint(Component.translatable(TeamcraftTranslations.GUI_BUTTON_REMOVE_NAME.key()));
            addTwoWidgets(
                name,
                remove,
                tooltip(TeamcraftTranslations.GUI_OPTION_NAMES.key())
            );
        }

        EditBox nameInput = textBox(
            TeamcraftTranslations.GUI_OPTION_NAMES.key(),
            TeamcraftTranslations.GUI_PLACEHOLDER_NAMES.key(),
            context.pendingName,
            TeamcraftConfigData.MAX_NAME_LENGTH,
            value -> context.pendingName = value
        );
        Button addName = TeamcraftButton.themedBuilder(Component.translatable(
            TeamcraftTranslations.GUI_NAMES_ADD.key()), ignored -> {
                String value = context.pendingName.trim();
                if (!value.isEmpty() && context.configuredNames.size() < TeamcraftConfigData.MAX_LIST_SIZE) {
                    context.configuredNames.add(value);
                    context.pendingName = "";
                    rebuildPage();
                }
            }).build().hoverHint(Component.translatable(TeamcraftTranslations.GUI_BUTTON_ADD_NAME.key()));
        addTwoWidgets(
            nameInput,
            addName,
            tooltip(TeamcraftTranslations.GUI_OPTION_NAMES.key())
        );
    }

    private void addColors() {
        if (context.configuredColors.isEmpty()) {
            addTextRow(
                Component.translatable(TeamcraftTranslations.GUI_COLORS_DEFAULT_PALETTE.key())
                    .withStyle(ChatFormatting.GRAY),
                tooltip(TeamcraftTranslations.GUI_OPTION_COLORS.key())
            );
        }
        for (int index = 0; index < context.configuredColors.size(); index++) {
            int colorIndex = index;
            Button color = colorDropdownButton(
                Component.translatable(TeamcraftTranslations.GUI_COLORS_SLOT.key(), index + 1),
                context.configuredColors.get(index),
                value -> context.configuredColors.set(colorIndex, value)
            );
            Button remove = TeamcraftButton.themedBuilder(Component.translatable(
                TeamcraftTranslations.GUI_COLORS_REMOVE.key()), ignored -> {
                    context.configuredColors.remove(colorIndex);
                    rebuildPage();
                }).build().hoverHint(Component.translatable(TeamcraftTranslations.GUI_BUTTON_REMOVE_COLOR.key()));
            addTwoWidgets(
                color,
                remove,
                tooltip(TeamcraftTranslations.GUI_OPTION_COLORS.key())
            );
        }

        Button picker = colorDropdownButton(
            Component.translatable(TeamcraftTranslations.GUI_COLORS_PICKER.key()),
            context.selectedColor,
            value -> context.selectedColor = value
        );
        Button addColor = TeamcraftButton.themedBuilder(Component.translatable(
            TeamcraftTranslations.GUI_COLORS_ADD.key()), ignored -> {
                if (context.configuredColors.size() < TeamcraftConfigData.MAX_LIST_SIZE) {
                    context.configuredColors.add(context.selectedColor);
                    rebuildPage();
                }
            }).build().hoverHint(Component.translatable(TeamcraftTranslations.GUI_BUTTON_ADD_COLOR.key()));
        addTwoWidgets(
            picker,
            addColor,
            tooltip(TeamcraftTranslations.GUI_OPTION_COLORS.key())
        );

        if (!context.configuredColors.isEmpty()) {
            Button useDefaults = TeamcraftButton.themedBuilder(Component.translatable(
                TeamcraftTranslations.GUI_COLORS_USE_DEFAULTS.key()), ignored -> {
                    context.configuredColors.clear();
                    rebuildPage();
                }).build().hoverHint(Component.translatable(TeamcraftTranslations.GUI_BUTTON_DEFAULT_COLORS.key()));
            addFullWidget(
                useDefaults,
                tooltip(TeamcraftTranslations.GUI_OPTION_COLORS.key())
            );
        }
    }
}
