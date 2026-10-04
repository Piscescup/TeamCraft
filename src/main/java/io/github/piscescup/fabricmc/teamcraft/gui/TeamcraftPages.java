package io.github.piscescup.fabricmc.teamcraft.gui;

import io.github.piscescup.fabricmc.teamcraft.gui.tab.*;
import io.github.piscescup.fabricmc.teamcraft.gui.widget.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

/** Ordered client-side page registry. Register new tabs before opening a context. */
@Environment(EnvType.CLIENT)
public final class TeamcraftPages {
    public static final String TEAM_CONFIG = "team_config";
    public static final String OWN_TEAM = "own_team";
    public static final String ALL_TEAMS = "all_teams";
    public static final String INVITATIONS = "invitations";
    public static final String HOTKEYS = "hotkeys";
    public static final String HELP = "help";

    private static final Map<String, Function<TeamcraftPageContext, ? extends TeamcraftTab>> FACTORIES =
        new LinkedHashMap<>();

    static {
        register(TEAM_CONFIG, TeamcraftTeamConfigTab::new);
        register(OWN_TEAM, TeamcraftOwnTeamTab::new);
        register(ALL_TEAMS, TeamcraftAllTeamsTab::new);
        register(INVITATIONS, TeamcraftInvitationsTab::new);
        register(HOTKEYS, TeamcraftHotkeysTab::new);
        register(HELP, TeamcraftHelpTab::new);
    }

    private TeamcraftPages() {
    }

    /** Tab order follows registration order. Existing IDs cannot be replaced. */
    public static void register(String id, Function<TeamcraftPageContext, ? extends TeamcraftTab> factory) {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(factory, "factory");
        if (id.isBlank()) {
            throw new IllegalArgumentException("A page ID cannot be blank");
        }
        if (FACTORIES.putIfAbsent(id, factory) != null) {
            throw new IllegalArgumentException("Duplicate TeamCraft page ID: " + id);
        }
    }

    static List<TeamcraftTab> createPages(TeamcraftPageContext context) {
        List<TeamcraftTab> pages = new ArrayList<>();
        for (var entry : List.copyOf(FACTORIES.entrySet())) {
            TeamcraftTab page = Objects.requireNonNull(entry.getValue().apply(context), "page");
            if (!entry.getKey().equals(page.id()) || !page.belongsTo(context)) {
                throw new IllegalArgumentException("Invalid TeamCraft page factory for: " + entry.getKey());
            }
            pages.add(page);
        }
        return List.copyOf(pages);
    }
}
