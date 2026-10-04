package io.github.piscescup.fabricmc.teamcraft.gui;

import io.github.piscescup.fabricmc.teamcraft.config.TeamInfoData;
import io.github.piscescup.fabricmc.teamcraft.config.TeamcraftConfigData;
import io.github.piscescup.fabricmc.teamcraft.config.TeamcraftSplitRule;
import io.github.piscescup.fabricmc.teamcraft.gui.network.ConfigSyncPayload;
import io.github.piscescup.fabricmc.teamcraft.gui.tab.TeamcraftTab;
import io.github.piscescup.fabricmc.teamcraft.team.SplitMode;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftColor;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * One configuration session, shared by its real Screen pages.
 * Drafts survive tab switches; rendering and input belong to the selected page.
 */
@Environment(EnvType.CLIENT)
public final class TeamcraftPageContext implements TeamcraftPageHost, TeamcraftConfigResponseReceiver {
    private final Screen parent;
    private final boolean helpOnly;
    private List<TeamcraftTab> pages = List.of();
    private TeamcraftTab page;

    // Shared mutable drafts are also used by tabs in the gui.tab subpackage.
    public boolean fixedTeamCount;
    public String playersPerTeamText;
    public String teamCountText;
    public SplitMode mode;
    public boolean friendlyFire;
    public List<String> candidates;
    public List<String> onlinePlayers;
    public List<TeamcraftColor> configuredColors;
    public TeamcraftColor selectedColor = TeamcraftColor.RED;
    public List<String> configuredNames;
    public String pendingName = "";

    public TeamInfoData ownTeam;
    public List<TeamInfoData> teams;
    public String ownTeamName;
    public TeamcraftColor ownTeamcraftColor = TeamcraftColor.WHITE;
    public boolean ownTeamFriendlyFire;
    public boolean waitingForServer;

    public TeamcraftPageContext(
        Screen parent,
        TeamcraftConfigData config,
        List<String> candidates,
        List<String> onlinePlayers,
        TeamInfoData ownTeam,
        List<TeamInfoData> teams
    ) {
        this(parent, config, candidates, onlinePlayers, ownTeam, teams, false);
    }

    private TeamcraftPageContext(
        Screen parent,
        TeamcraftConfigData config,
        List<String> candidates,
        List<String> onlinePlayers,
        TeamInfoData ownTeam,
        List<TeamInfoData> teams,
        boolean helpOnly
    ) {
        this.parent = parent;
        this.helpOnly = helpOnly;
        loadConfig(config);
        loadCandidates(candidates, onlinePlayers);
        loadTeams(ownTeam, teams);
        this.pages = TeamcraftPages.createPages(this).stream()
            .filter(candidate -> !helpOnly || !candidate.requiresServer())
            .toList();
        selectPage(helpOnly ? TeamcraftPages.HELP : TeamcraftPages.TEAM_CONFIG);
    }

    /** Creates a server-independent, read-only Help view for Mod Menu. */
    public static TeamcraftPageContext help(Screen parent) {
        return new TeamcraftPageContext(
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
    public Component title() {
        return Component.translatable(
            this.helpOnly ? TeamcraftTranslations.TITLE_HELP.key() : TeamcraftTranslations.GUI_TITLE.key()
        );
    }

    @Override
    public List<TeamcraftTab> pages() {
        return this.pages;
    }

    /** The actual Minecraft Screen to display, not a wrapper around a page. */
    public TeamcraftTab screen() {
        return this.page;
    }

    @Override
    public boolean waitingForServer() {
        return this.waitingForServer;
    }

    @Override
    public boolean isInGameUi() {
        return !this.helpOnly;
    }

    @Override
    public void close() {
        showScreen(this.parent);
    }

    @Override
    public void switchPage(String id) {
        TeamcraftTab target = findPage(id);
        if (this.waitingForServer || this.page == target) {
            return;
        }
        selectPage(id);
        showScreen(this.page);
    }

    /** Select without displaying, used before initialization and during response handling. */
    void selectPage(String id) {
        this.page = findPage(id);
        this.page.resetScroll();
    }

    private TeamcraftTab findPage(String id) {
        return this.pages.stream().filter(candidate -> candidate.id().equals(id)).findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Unknown or unavailable TeamCraft page: " + id));
    }

    public String currentPageId() {
        return this.page.id();
    }

    void rebuildPage() {
        Minecraft client = Minecraft.getInstance();
        //#if MC >= 260200
        Screen current = client.gui.screen();
        //#else
        //$$ Screen current = client.screen;
        //#endif
        if (current == this.page) {
            this.page.refreshWidgets();
        }
        else if (current instanceof TeamcraftTab previous && previous.belongsTo(this)) {
            showScreen(this.page);
        }
    }

    void resetScroll() {
        this.page.resetScroll();
    }

    void updateEnabledState() {
        this.page.updateEnabledState();
    }

    @Override
    public void handleServerResponse(ConfigSyncPayload payload) {
        TeamcraftConfigResponseHandler.handle(this, payload);
    }

    private static void showScreen(Screen screen) {
        Minecraft client = Minecraft.getInstance();
        //#if MC >= 260200
        client.gui.setScreen(screen);
        //#else
        //$$ client.setScreen(screen);
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
        this.pages.forEach(TeamcraftTab::cancelTransientInput);
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

    /** Joining from the invitation page updates team views without discarding split-configuration drafts. */
    public void loadInvitationTeams(TeamInfoData ownTeam, List<TeamInfoData> teams) {
        loadTeams(ownTeam, teams);
    }


    public TeamcraftSplitRule currentRule() {
        return this.fixedTeamCount ? TeamcraftSplitRule.TEAM_COUNT : TeamcraftSplitRule.PLAYERS_PER_TEAM;
    }

    public void showFeedback(String translationKey, TeamcraftFeedbackScreen.Type type) {
        showFeedback(Component.translatable(translationKey), type);
    }

    public void showFeedback(Component message, TeamcraftFeedbackScreen.Type type) {
        TeamcraftFeedbackScreen.open(this.page, message, type);
    }

    void showOverlay(String translationKey, ChatFormatting color) {
        showOverlay(Component.translatable(translationKey), color);
    }

    public void showOverlay(Component message, ChatFormatting color) {
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            //#if MC >= 260102
            client.player.sendOverlayMessage(message.copy().withStyle(color));
            //#else
            //$$ client.player.displayClientMessage(message.copy().withStyle(color), true);
            //#endif
        }
    }
}
