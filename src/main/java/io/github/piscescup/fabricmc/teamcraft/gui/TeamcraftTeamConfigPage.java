package io.github.piscescup.fabricmc.teamcraft.gui;

import io.github.piscescup.fabricmc.teamcraft.team.SplitMode;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;

/** Builds the candidate, split-rule, name, and color controls. */
final class TeamcraftTeamConfigPage {
    private TeamcraftTeamConfigPage() {
    }

    static void build(TeamcraftConfigScreen screen) {
        addCandidates(screen);
        addSplitSettings(screen);
        addAppearanceSettings(screen);
        screen.addTextRow(
            Component.translatable(TeamcraftTranslations.GUI_SESSION_NOTE.key()).withStyle(ChatFormatting.DARK_GRAY),
            Component.translatable(TeamcraftTranslations.GUI_SESSION_NOTE.key())
        );
    }

    private static void addCandidates(TeamcraftConfigScreen screen) {
        screen.addHeader(TeamcraftTranslations.GUI_CATEGORY_CANDIDATES.key());
        screen.addTextRow(
            Component.translatable(
                TeamcraftTranslations.GUI_CANDIDATES_SUMMARY.key(),
                screen.candidates.size(),
                screen.onlinePlayers.size()
            ).withStyle(ChatFormatting.GRAY),
            TeamcraftConfigScreen.tooltip(TeamcraftTranslations.GUI_CATEGORY_CANDIDATES.key())
        );

        Button selectAll = Button.builder(Component.translatable(
            TeamcraftTranslations.GUI_CANDIDATES_SELECT_ALL.key()), ignored -> {
                LinkedHashSet<String> selected = new LinkedHashSet<>(screen.candidates);
                for (String playerName : screen.onlinePlayers) {
                    if (selected.size() >= TeamcraftConfigData.MAX_CANDIDATES) {
                        break;
                    }
                    selected.add(playerName);
                }
                screen.candidates = new ArrayList<>(selected);
                screen.rebuildPage();
            }).build();
        Button clear = Button.builder(Component.translatable(
            TeamcraftTranslations.GUI_CANDIDATES_CLEAR.key()), ignored -> {
                screen.candidates.clear();
                screen.rebuildPage();
            }).build();
        screen.addTwoWidgets(
            selectAll,
            clear,
            TeamcraftConfigScreen.tooltip(TeamcraftTranslations.GUI_CATEGORY_CANDIDATES.key())
        );

        LinkedHashSet<String> displayedPlayers = new LinkedHashSet<>(screen.candidates);
        displayedPlayers.addAll(screen.onlinePlayers);
        if (displayedPlayers.isEmpty()) {
            screen.addTextRow(
                Component.translatable(TeamcraftTranslations.GUI_CANDIDATES_NONE_ONLINE.key())
                    .withStyle(ChatFormatting.GRAY),
                TeamcraftConfigScreen.tooltip(TeamcraftTranslations.GUI_CATEGORY_CANDIDATES.key())
            );
        }
        for (String playerName : displayedPlayers) {
            addCandidate(screen, playerName);
        }
    }

    private static void addCandidate(TeamcraftConfigScreen screen, String playerName) {
        boolean selected = screen.candidates.contains(playerName);
        boolean online = screen.onlinePlayers.contains(playerName);
        Component message = Component.literal(selected ? "↕  ☑ " : "☐ ")
            .append(Component.literal(playerName).withStyle(selected ? ChatFormatting.WHITE : ChatFormatting.GRAY))
            .append(Component.literal("  •  ").withStyle(ChatFormatting.DARK_GRAY))
            .append(Component.translatable(
                online
                    ? TeamcraftTranslations.GUI_CANDIDATES_ONLINE.key()
                    : TeamcraftTranslations.GUI_CANDIDATES_OFFLINE.key()
            ).withStyle(online ? ChatFormatting.GREEN : ChatFormatting.DARK_GRAY));
        Button player = Button.builder(message, ignored -> {
            if (screen.candidates.remove(playerName)) {
                screen.rebuildPage();
                return;
            }
            if (screen.candidates.size() < TeamcraftConfigData.MAX_CANDIDATES) {
                screen.candidates.add(playerName);
                screen.rebuildPage();
            }
        }).build();
        if (selected) {
            screen.registerCandidateRow(playerName);
        }
        screen.addFullWidget(player, TeamcraftConfigScreen.tooltip(
            TeamcraftTranslations.GUI_CANDIDATES_PLAYER_TOOLTIP.key(),
            TeamcraftTranslations.GUI_CANDIDATES_PLAYER_EXAMPLE.key()
        ));
    }

    private static void addSplitSettings(TeamcraftConfigScreen screen) {
        screen.addHeader(TeamcraftTranslations.GUI_CATEGORY_SPLIT.key());

        //#if MC >= 12111
        CycleButton<TeamcraftSplitRule> rule = CycleButton.builder(
                TeamcraftSplitRule::displayName,
                screen.currentRule()
            )
            .withValues(TeamcraftSplitRule.values())
            .create(Component.translatable(TeamcraftTranslations.GUI_OPTION_RULE.key()), (button, value) -> {
                screen.fixedTeamCount = value == TeamcraftSplitRule.TEAM_COUNT;
                screen.resetScroll();
                screen.rebuildPage();
            });
        //#else
        //$$ CycleButton<TeamcraftSplitRule> rule = CycleButton.builder(TeamcraftSplitRule::displayName)
        //$$     .withValues(TeamcraftSplitRule.values())
        //$$     .withInitialValue(screen.currentRule())
        //$$     .create(Component.translatable(TeamcraftTranslations.GUI_OPTION_RULE.key()), (button, value) -> {
        //$$         screen.fixedTeamCount = value == TeamcraftSplitRule.TEAM_COUNT;
        //$$         screen.resetScroll();
        //$$         screen.rebuildPage();
        //$$     });
        //#endif
        screen.addFullWidget(rule, TeamcraftConfigScreen.tooltip(TeamcraftTranslations.GUI_OPTION_RULE.key()));

        if (screen.fixedTeamCount) {
            EditBox count = screen.numberBox(
                TeamcraftTranslations.GUI_OPTION_TEAM_COUNT.key(),
                screen.teamCountText,
                value -> screen.teamCountText = value
            );
            screen.addLabeledWidget(
                TeamcraftTranslations.GUI_OPTION_TEAM_COUNT.key(),
                count,
                TeamcraftConfigScreen.tooltip(TeamcraftTranslations.GUI_OPTION_TEAM_COUNT.key())
            );
        }
        else {
            EditBox size = screen.numberBox(
                TeamcraftTranslations.GUI_OPTION_PLAYERS_PER_TEAM.key(),
                screen.playersPerTeamText,
                value -> screen.playersPerTeamText = value
            );
            screen.addLabeledWidget(
                TeamcraftTranslations.GUI_OPTION_PLAYERS_PER_TEAM.key(),
                size,
                TeamcraftConfigScreen.tooltip(TeamcraftTranslations.GUI_OPTION_PLAYERS_PER_TEAM.key())
            );
        }

        //#if MC >= 12111
        CycleButton<SplitMode> modeButton = CycleButton.builder(SplitMode::displayName, screen.mode)
            .withValues(SplitMode.values())
            .create(
                Component.translatable(TeamcraftTranslations.GUI_OPTION_MODE.key()),
                (button, value) -> screen.mode = value
            );
        //#else
        //$$ CycleButton<SplitMode> modeButton = CycleButton.builder(SplitMode::displayName)
        //$$     .withValues(SplitMode.values())
        //$$     .withInitialValue(screen.mode)
        //$$     .create(
        //$$         Component.translatable(TeamcraftTranslations.GUI_OPTION_MODE.key()),
        //$$         (button, value) -> screen.mode = value
        //$$     );
        //#endif
        screen.addFullWidget(
            modeButton,
            TeamcraftConfigScreen.tooltip(TeamcraftTranslations.GUI_OPTION_MODE.key())
        );

        CycleButton<Boolean> friendlyFireButton = CycleButton.onOffBuilder(screen.friendlyFire)
            .create(
                Component.translatable(TeamcraftTranslations.GUI_OPTION_FRIENDLY_FIRE.key()),
                (button, value) -> screen.friendlyFire = value
            );
        screen.addFullWidget(
            friendlyFireButton,
            TeamcraftConfigScreen.tooltip(TeamcraftTranslations.GUI_OPTION_FRIENDLY_FIRE.key())
        );
    }

    private static void addAppearanceSettings(TeamcraftConfigScreen screen) {
        screen.addHeader(TeamcraftTranslations.GUI_CATEGORY_APPEARANCE.key());
        addNames(screen);
        addColors(screen);
    }

    private static void addNames(TeamcraftConfigScreen screen) {
        screen.addHeader(TeamcraftTranslations.GUI_OPTION_NAMES.key());
        if (screen.configuredNames.isEmpty()) {
            screen.addTextRow(
                Component.translatable(TeamcraftTranslations.GUI_NAMES_DEFAULT.key())
                    .withStyle(ChatFormatting.GRAY),
                TeamcraftConfigScreen.tooltip(TeamcraftTranslations.GUI_OPTION_NAMES.key())
            );
        }
        for (int index = 0; index < screen.configuredNames.size(); index++) {
            int nameIndex = index;
            Button name = Button.builder(Component.translatable(
                TeamcraftTranslations.GUI_NAMES_SLOT.key(),
                index + 1,
                screen.configuredNames.get(index)
            ), ignored -> { }).build();
            Button remove = Button.builder(Component.translatable(
                TeamcraftTranslations.GUI_NAMES_REMOVE.key()), ignored -> {
                    screen.configuredNames.remove(nameIndex);
                    screen.rebuildPage();
                }).build();
            screen.addTwoWidgets(
                name,
                remove,
                TeamcraftConfigScreen.tooltip(TeamcraftTranslations.GUI_OPTION_NAMES.key())
            );
        }

        EditBox nameInput = screen.textBox(
            TeamcraftTranslations.GUI_OPTION_NAMES.key(),
            TeamcraftTranslations.GUI_PLACEHOLDER_NAMES.key(),
            screen.pendingName,
            TeamcraftConfigData.MAX_NAME_LENGTH,
            value -> screen.pendingName = value
        );
        Button addName = Button.builder(Component.translatable(
            TeamcraftTranslations.GUI_NAMES_ADD.key()), ignored -> {
                String value = screen.pendingName.trim();
                if (!value.isEmpty() && screen.configuredNames.size() < TeamcraftConfigData.MAX_LIST_SIZE) {
                    screen.configuredNames.add(value);
                    screen.pendingName = "";
                    screen.rebuildPage();
                }
            }).build();
        screen.addTwoWidgets(
            nameInput,
            addName,
            TeamcraftConfigScreen.tooltip(TeamcraftTranslations.GUI_OPTION_NAMES.key())
        );
    }

    private static void addColors(TeamcraftConfigScreen screen) {
        screen.addHeader(TeamcraftTranslations.GUI_OPTION_COLORS.key());
        if (screen.configuredColors.isEmpty()) {
            screen.addTextRow(
                Component.translatable(TeamcraftTranslations.GUI_COLORS_DEFAULT_PALETTE.key())
                    .withStyle(ChatFormatting.GRAY),
                TeamcraftConfigScreen.tooltip(TeamcraftTranslations.GUI_OPTION_COLORS.key())
            );
        }
        for (int index = 0; index < screen.configuredColors.size(); index++) {
            int colorIndex = index;
            Button color = screen.colorDropdownButton(
                Component.translatable(TeamcraftTranslations.GUI_COLORS_SLOT.key(), index + 1),
                screen.configuredColors.get(index),
                value -> screen.configuredColors.set(colorIndex, value)
            );
            Button remove = Button.builder(Component.translatable(
                TeamcraftTranslations.GUI_COLORS_REMOVE.key()), ignored -> {
                    screen.configuredColors.remove(colorIndex);
                    screen.rebuildPage();
                }).build();
            screen.addTwoWidgets(
                color,
                remove,
                TeamcraftConfigScreen.tooltip(TeamcraftTranslations.GUI_OPTION_COLORS.key())
            );
        }

        Button picker = screen.colorDropdownButton(
            Component.translatable(TeamcraftTranslations.GUI_COLORS_PICKER.key()),
            screen.selectedColor,
            value -> screen.selectedColor = value
        );
        Button addColor = Button.builder(Component.translatable(
            TeamcraftTranslations.GUI_COLORS_ADD.key()), ignored -> {
                if (screen.configuredColors.size() < TeamcraftConfigData.MAX_LIST_SIZE) {
                    screen.configuredColors.add(screen.selectedColor);
                    screen.rebuildPage();
                }
            }).build();
        screen.addTwoWidgets(
            picker,
            addColor,
            TeamcraftConfigScreen.tooltip(TeamcraftTranslations.GUI_OPTION_COLORS.key())
        );

        if (!screen.configuredColors.isEmpty()) {
            Button useDefaults = Button.builder(Component.translatable(
                TeamcraftTranslations.GUI_COLORS_USE_DEFAULTS.key()), ignored -> {
                    screen.configuredColors.clear();
                    screen.rebuildPage();
                }).build();
            screen.addFullWidget(
                useDefaults,
                TeamcraftConfigScreen.tooltip(TeamcraftTranslations.GUI_OPTION_COLORS.key())
            );
        }
    }
}
