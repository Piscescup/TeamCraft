package io.github.piscescup.fabricmc.teamcraft.testing;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.piscescup.fabricmc.teamcraft.permission.Permission;
import io.github.piscescup.fabricmc.teamcraft.permission.TeamPermissionManager;
import io.github.piscescup.fabricmc.teamcraft.gui.network.*;
import io.github.piscescup.fabricmc.teamcraft.team.*;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftColor;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftTranslations;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.buffer.Unpooled;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.fabric.impl.networking.server.ServerNetworkingImpl;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.Connection;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.common.ClientboundCustomPayloadPacket;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.network.protocol.game.ClientboundSetPlayerTeamPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ClientInformation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.level.GameType;
import net.minecraft.world.scores.PlayerTeam;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static io.github.piscescup.fabricmc.teamcraft.permission.TeamCommandPermission.*;

/** Real server and player list, in-memory connections. NOT a graphical-client/E2E test. */
public final class MultiplayerChecks implements ModInitializer {
    private final List<String> results = new ArrayList<>();
    private final List<EmbeddedChannel> channels = new ArrayList<>();
    private final Map<UUID, EmbeddedChannel> playerChannels = new HashMap<>();
    private MinecraftServer server;
    private int startupTicks;

    @Override
    public void onInitialize() {
        if (!Boolean.getBoolean("teamcraft.multiplayer.test")) {
            throw new IllegalStateException("Test mod must only run in its isolated test configuration");
        }
        ServerLifecycleEvents.SERVER_STARTED.register(started -> server = started);
        ServerTickEvents.END_SERVER_TICK.register(ticking -> {
            // execute() may run inline on the server thread during SERVER_STARTED.
            // Wait for all startup listeners and initial chunk/entity ticks first.
            if (ticking == server && ++startupTicks == 5) { run(); }
        });
    }

    private void run() {
        String status = "FAIL";
        try {
            checks();
            status = "PASS";
        } catch (Throwable failure) {
            results.add("FAIL: " + failure);
            failure.printStackTrace();
        } finally {
            try {
                Files.writeString(Path.of("test-result.txt"), status + "\nversion="
                    + System.getProperty("teamcraft.multiplayer.version") + "\n"
                    + String.join("\n", results) + "\n", StandardCharsets.UTF_8);
            } catch (Exception failure) {
                throw new RuntimeException("Cannot write test results", failure);
            } finally {
                server.halt(false);
            }
        }
    }

    private void checks() throws Exception {
        ServerPlayer alice = player("MultiAlice");
        ServerPlayer bob = player("MultiBob");
        ServerPlayer cara = player("MultiCara");
        ServerPlayer dave = player("MultiDave");
        check(server.getPlayerList().getPlayers().size() == 4, "Four distinct server players are online");
        check(alice.gameMode.getGameModeForPlayer() == GameType.SURVIVAL, "Test players are in Survival mode");

        ServerScoreboard board = server.getScoreboard();
        PlayerTeam own = board.addPlayerTeam("teamcraft_1");
        own.setDisplayName(Component.literal("Test"));
        TeamcraftColor.RED.applyTo(own);
        TeamAssigner.applyPrefix(own);
        PlayerTeam other = board.addPlayerTeam("teamcraft_2");
        other.setDisplayName(Component.literal("Other"));
        other.setAllowFriendlyFire(false);
        board.addPlayerToTeam(alice.getScoreboardName(), own);
        board.addPlayerToTeam(bob.getScoreboardName(), own);
        board.addPlayerToTeam(cara.getScoreboardName(), other);
        drainAll();

        check(rejected(alice, "teamcraft manage team teamcraft_1 name Forbidden"), "Default manage permission rejects a non-operator");
        check(own.getDisplayName().getString().equals("Test"), "Permission rejection does not mutate the team");
        TeamPermissionManager.updatePermission(MANAGE_KEY, Permission.LEVEL_ALL);
        check(suggestions(alice).equals(Set.of("Test", "teamcraft_1")), "Alice sees only her team's name and ID");
        check(suggestions(cara).equals(Set.of("Other", "teamcraft_2")), "Cara sees only her team's name and ID");
        check(suggestions(dave).isEmpty(), "A player with no team receives no team candidates");
        check(command(alice, "teamcraft manage team Test name Hello") == 1, "Own team can be renamed by its current name");
        check(suggestions(alice).equals(Set.of("Hello", "teamcraft_1")), "Renaming refreshes the name but preserves the ID");
        check(suggestions(bob).equals(Set.of("Hello", "teamcraft_1")), "Teammate suggestions see the new name");
        assertTeamPacketsEverywhere(own.getName(), "Rename broadcasts a team update to all four connections");
        check(command(alice, "teamcraft manage team Test name Stale") == 0, "The previous name no longer resolves");
        check(command(alice, "teamcraft manage team teamcraft_2 name Intrusion") == 0, "Another team's ID is rejected");
        check(command(alice, "teamcraft manage team Other friendlyfire true") == 0, "Another team's name is rejected");
        check(other.getDisplayName().getString().equals("Other") && !other.isAllowFriendlyFire(), "Rejected commands leave the other team unchanged");
        check(command(alice, "teamcraft manage team teamcraft_1 name \"Hello World\"") == 1, "Names containing spaces are accepted when quoted");
        check(suggestions(alice).equals(Set.of("\"Hello World\"", "teamcraft_1")), "Spaced name suggestions are quoted");
        other.setDisplayName(Component.literal("Hello World"));
        check(command(alice, "teamcraft manage team \"Hello World\" friendlyfire true") == 1
            && own.isAllowFriendlyFire() && !other.isAllowFriendlyFire(), "Duplicate display names still resolve exclusively to the sender's team");
        drainAll();
        check(command(alice, "teamcraft manage team teamcraft_1 color gold") == 1, "Own team color can be changed by stable ID");
        check(own.getDisplayName().getString().equals("Hello World") && TeamcraftColor.ofTeam(own) == TeamcraftColor.GOLD,
            "Changing color preserves the team name");
        check(own.getPlayerPrefix().getStyle().getColor().getValue() == TeamcraftColor.GOLD.rgb(), "Player prefix uses the new color");
        assertTeamPacketsEverywhere(own.getName(), "Color update broadcasts to all four connections");
        check(command(alice, "teamcraft manage team teamcraft_1 info") == 1, "Own team information is available");
        check(command(dave, "teamcraft manage team teamcraft_1 info") == 0, "An unassigned player cannot manage another team");

        TeamPermissionManager.updatePermission(ROOT_KEY, Permission.LEVEL_OWNERS);
        check(rejected(alice, "teamcraft manage team teamcraft_1 color blue"), "Root permission blocks manage commands");
        check(TeamInvitationService.send(server, alice, dave).status() == TeamInvitationService.Status.PERMISSION_DENIED,
            "Root permission also blocks the invitation service used by the GUI");
        TeamPermissionManager.updatePermission(ROOT_KEY, Permission.LEVEL_ALL);
        TeamPermissionManager.updatePermission(INVITE_KEY, Permission.LEVEL_OWNERS);
        check(TeamInvitationService.send(server, alice, dave).status() == TeamInvitationService.Status.PERMISSION_DENIED,
            "Invite permission is enforced independently");
        TeamPermissionManager.updatePermission(INVITE_KEY, Permission.LEVEL_ALL);
        check(command(alice, "teamcraft invite player MultiDave") == 1, "Invite command locates another online player");
        var pending = TeamInvitationManager.get(server).find(dave.getUUID());
        check(pending != null, "Invite creates a pending invitation for the target UUID");
        check(TeamInvitationService.send(server, bob, dave).translation() == TeamcraftTranslations.INVITE_ERROR_PENDING,
            "A teammate cannot overwrite an existing invitation");
        check(TeamInvitationService.accept(server, dave, UUID.randomUUID()).status() == TeamInvitationService.Status.ERROR,
            "A stale GUI invitation ID is rejected");
        check(TeamInvitationManager.get(server).find(dave.getUUID()).id().equals(pending.id()), "A stale GUI action preserves the live invitation");
        drainAll();
        check(command(dave, "teamcraft invite accept") == 1 && board.getPlayersTeam(dave.getScoreboardName()) == own,
            "Accept joins the inviter's existing team");
        assertTeamPacketsEverywhere(own.getName(), "Joining broadcasts membership to all four connections");
        check(command(dave, "teamcraft invite accept") == 0, "Accept cannot be replayed");
        check(suggestions(dave).equals(suggestions(alice)), "New teammate immediately sees own team candidates");
        check(TeamInvitationService.send(server, alice, cara).translation() == TeamcraftTranslations.INVITE_ERROR_HAS_TEAM,
            "An invitation cannot pull a player out of a different team");

        TeamSession session = TeamSessionManager.get();
        session.getCandidates().addAll(List.of("MultiAlice", "MultiBob", "MultiCara", "MultiDave"));
        List<String> candidates = List.copyOf(session.getCandidates());
        TeamPermissionManager.updatePermission(LEAVE_TEAM_KEY, Permission.LEVEL_OWNERS);
        check(TeamMembershipService.leave(dave, own.getName()).outcome() == TeamMembershipService.Outcome.PERMISSION_DENIED,
            "Leave permission blocks GUI leave requests");
        TeamPermissionManager.updatePermission(LEAVE_TEAM_KEY, Permission.LEVEL_ALL);
        check(TeamMembershipService.leave(dave, other.getName()).outcome() == TeamMembershipService.Outcome.TEAM_CHANGED,
            "A stale or forged GUI leave team ID is rejected");
        drainAll();
        check(command(dave, "teamcraft manage leave") == 1 && board.getPlayersTeam(dave.getScoreboardName()) == null,
            "Leave command removes only the invoking player");
        check(board.getPlayerTeam(own.getName()) == own && own.getPlayers().containsAll(List.of("MultiAlice", "MultiBob")),
            "Leaving preserves the team and its other members");
        check(session.getCandidates().equals(candidates), "Leaving preserves split candidates");
        assertTeamPacketsEverywhere(own.getName(), "Leaving broadcasts membership to all four connections");
        check(suggestions(dave).isEmpty(), "Leaving removes the former team's suggestions");
        check(command(dave, "teamcraft manage leave") == 0, "Leaving twice produces a controlled rejection");

        check(command(alice, "teamcraft invite player MultiDave") == 1, "A player who has left can be invited again");
        check(command(dave, "teamcraft invite decline") == 1, "Decline consumes the pending invitation");
        check(command(dave, "teamcraft invite decline") == 0, "Decline cannot be replayed");
        check(command(alice, "teamcraft invite player MultiDave") == 1, "Fresh invitation is created after declining");
        check(command(alice, "teamcraft manage leave") == 1, "Inviter can leave while another teammate remains");
        check(TeamInvitationManager.get(server).find(dave.getUUID()) == null, "Inviter leaving cancels outgoing invitations");
        check(board.getPlayersTeam(bob.getScoreboardName()) == own, "Inviter leaving does not remove the teammate");
        check(command(bob, "teamcraft invite player MultiDave") == 1, "Remaining teammate can still invite");
        UUID obsolete = TeamInvitationManager.get(server).find(dave.getUUID()).id();
        TeamAssigner.disband(board, own.getName());
        board.addPlayerTeam(own.getName());
        check(TeamInvitationService.accept(server, dave, obsolete).status() == TeamInvitationService.Status.ERROR,
            "Clearing and recreating the same team ID cannot hijack an old invitation");
        check(board.getPlayersTeam(dave.getScoreboardName()) == null, "Invalid invitation does not change target membership");

        check(command(server.createCommandSourceStack(), "teamcraft reset") == 1, "Server operator can reset the test workflow");
        check(command(server.createCommandSourceStack(), "teamcraft init @a") == 1, "Candidate selection includes all four online players");
        check(command(server.createCommandSourceStack(), "teamcraft config players-per-team 2") == 1, "Players per team can be configured");
        check(command(server.createCommandSourceStack(), "teamcraft config mode fixed") == 1, "Fixed split mode can be configured");
        check(command(server.createCommandSourceStack(), "teamcraft build-teams") == 1, "Four-player split command succeeds");
        check(board.getPlayerTeams().stream().filter(t -> TeamAssigner.isManagedTeamId(t.getName())).count() == 2,
            "Split creates two teams for four players");
        check(List.of(alice, bob, cara, dave).stream().allMatch(p -> board.getPlayersTeam(p.getScoreboardName()) != null),
            "All four players receive team assignments");
        check(command(server.createCommandSourceStack(), "teamcraft build-teams") == 0, "A repeated split is rejected while teams exist");
        check(command(server.createCommandSourceStack(), "teamcraft clear") == 1, "Clear disbands the existing teams");
        check(command(server.createCommandSourceStack(), "teamcraft build-teams") == 1, "Splitting can be retried after clearing");
        guiChecks(alice, bob, cara, dave);
    }

    private void guiChecks(ServerPlayer alice, ServerPlayer bob, ServerPlayer cara, ServerPlayer dave) throws Exception {
        ServerScoreboard board = server.getScoreboard();
        PlayerTeam aliceTeam = board.getPlayersTeam(alice.getScoreboardName());
        PlayerTeam daveTeam = board.getPlayersTeam(dave.getScoreboardName());
        check(aliceTeam != daveTeam, "GUI test begins with two separate teams");
        TeamPermissionManager.updatePermission(ROOT_KEY, Permission.LEVEL_OWNERS);
        receive(alice, ConfigRequestPayload.INSTANCE);
        check(response(alice, ConfigSyncPayload.class).response() == ConfigSyncPayload.Response.PERMISSION_DENIED,
            "GUI configuration request checks root permission");
        InvitationSyncPayload hidden = inviteGui(alice, InvitationActionPayload.Action.REFRESH, "", null);
        check(hidden.response() == InvitationSyncPayload.Response.PERMISSION_DENIED && hidden.ownTeam() == null
            && hidden.teams().isEmpty() && hidden.eligiblePlayers().isEmpty() && !hidden.canInvite(),
            "Denied invitation GUI snapshot exposes no team or eligible-player data");
        TeamPermissionManager.updatePermission(ROOT_KEY, Permission.LEVEL_ALL);
        receive(alice, ConfigRequestPayload.INSTANCE);
        ConfigSyncPayload snapshot = response(alice, ConfigSyncPayload.class);
        check(snapshot.response() == ConfigSyncPayload.Response.OPENED && snapshot.teams().size() == 2
            && snapshot.ownTeam().id().equals(aliceTeam.getName()) && snapshot.onlinePlayers().size() == 4,
            "GUI configuration snapshot includes all online players and the sender's own team");
        check(roundTrip(ConfigSyncPayload.CODEC, snapshot).equals(snapshot), "GUI configuration response codec round-trips with live server registries");

        String previous = aliceTeam.getDisplayName().getString();
        TeamPermissionManager.updatePermission(MANAGE_KEY, Permission.LEVEL_OWNERS);
        TeamUpdatePayload ownUpdate = new TeamUpdatePayload(aliceTeam.getName(), "Gui Team", TeamcraftColor.AQUA, false);
        check(roundTrip(TeamUpdatePayload.CODEC, ownUpdate).equals(ownUpdate), "GUI team update request codec round-trips");
        receive(alice, ownUpdate);
        check(response(alice, ConfigSyncPayload.class).response() == ConfigSyncPayload.Response.PERMISSION_DENIED
            && aliceTeam.getDisplayName().getString().equals(previous), "GUI team update rejects insufficient manage permission without mutation");
        TeamPermissionManager.updatePermission(MANAGE_KEY, Permission.LEVEL_ALL);
        String otherName = daveTeam.getDisplayName().getString();
        receive(alice, new TeamUpdatePayload(daveTeam.getName(), "Forged", TeamcraftColor.BLACK, true));
        check(response(alice, ConfigSyncPayload.class).response() == ConfigSyncPayload.Response.TEAM_NOT_FOUND
            && daveTeam.getDisplayName().getString().equals(otherName), "GUI rejects a forged other-team update");
        drainAll();
        receive(alice, ownUpdate);
        ConfigSyncPayload saved = response(alice, ConfigSyncPayload.class);
        check(saved.response() == ConfigSyncPayload.Response.TEAM_SAVED && aliceTeam.getDisplayName().getString().equals("Gui Team")
            && TeamcraftColor.ofTeam(aliceTeam) == TeamcraftColor.AQUA && !aliceTeam.isAllowFriendlyFire(),
            "GUI updates the sender's own name, color and friendly fire");
        // response() consumes Alice's scoreboard packets; other players must still receive the update.
        check(teamPacketReceived(playerChannels.get(bob.getUUID()), aliceTeam.getName())
            && teamPacketReceived(playerChannels.get(cara.getUUID()), aliceTeam.getName())
            && teamPacketReceived(playerChannels.get(dave.getUUID()), aliceTeam.getName()),
            "GUI team update broadcasts to the teammate and both other-team players");
        check(suggestions(bob).equals(Set.of("\"Gui Team\"", aliceTeam.getName())), "GUI renaming also updates teammate command suggestions");

        TeamLeavePayload leave = new TeamLeavePayload(daveTeam.getName());
        check(roundTrip(TeamLeavePayload.CODEC, leave).equals(leave), "GUI leave request codec round-trips");
        TeamPermissionManager.updatePermission(LEAVE_TEAM_KEY, Permission.LEVEL_OWNERS);
        receive(dave, leave);
        check(response(dave, ConfigSyncPayload.class).response() == ConfigSyncPayload.Response.PERMISSION_DENIED
            && board.getPlayersTeam(dave.getScoreboardName()) == daveTeam, "GUI leave handler enforces leave permission");
        TeamPermissionManager.updatePermission(LEAVE_TEAM_KEY, Permission.LEVEL_ALL);
        receive(dave, new TeamLeavePayload(aliceTeam.getName()));
        check(response(dave, ConfigSyncPayload.class).response() == ConfigSyncPayload.Response.TEAM_NOT_FOUND
            && board.getPlayersTeam(dave.getScoreboardName()) == daveTeam, "GUI rejects another team's leave ID");
        receive(dave, leave);
        ConfigSyncPayload left = response(dave, ConfigSyncPayload.class);
        check(left.response() == ConfigSyncPayload.Response.TEAM_LEFT && left.ownTeam() == null
            && board.getPlayersTeam(cara.getScoreboardName()) == daveTeam, "GUI leave updates own-team snapshot and preserves the former teammate");
        receive(dave, leave);
        check(response(dave, ConfigSyncPayload.class).response() == ConfigSyncPayload.Response.NOT_IN_TEAM,
            "GUI repeated leave returns a controlled no-team response");

        InvitationSyncPayload eligible = inviteGui(alice, InvitationActionPayload.Action.REFRESH, "", null);
        check(eligible.canInvite() && eligible.eligiblePlayers().equals(List.of("MultiDave")), "Invitation GUI lists only online players without any team");
        TeamPermissionManager.updatePermission(INVITE_KEY, Permission.LEVEL_OWNERS);
        check(inviteGui(alice, InvitationActionPayload.Action.SEND, "MultiDave", null).response()
            == InvitationSyncPayload.Response.PERMISSION_DENIED, "GUI invitation send handler enforces invite permission");
        TeamPermissionManager.updatePermission(INVITE_KEY, Permission.LEVEL_ALL);
        check(inviteGui(alice, InvitationActionPayload.Action.SEND, "MultiDave", null).response()
            == InvitationSyncPayload.Response.SUCCESS, "GUI invitation send succeeds for an eligible online target");
        InvitationSyncPayload received = inviteGui(dave, InvitationActionPayload.Action.REFRESH, "", null);
        check(received.invitation() != null && received.invitation().senderName().equals("MultiAlice")
            && received.invitation().teamId().equals(aliceTeam.getName()), "Recipient GUI receives inviter, team and invitation identity");
        check(roundTrip(InvitationSyncPayload.CODEC, received).equals(received), "Invitation snapshot codec round-trips pending identity and lifetime");
        UUID invitationId = received.invitation().id();
        check(inviteGui(dave, InvitationActionPayload.Action.ACCEPT, "", UUID.randomUUID()).response()
            == InvitationSyncPayload.Response.ERROR, "GUI handler rejects an obsolete invitation identity");
        check(TeamInvitationManager.get(server).find(dave.getUUID()).id().equals(invitationId), "GUI stale action leaves the live invitation available");
        InvitationSyncPayload joined = inviteGui(dave, InvitationActionPayload.Action.ACCEPT, "", invitationId);
        check(joined.response() == InvitationSyncPayload.Response.SUCCESS && joined.ownTeam().id().equals(aliceTeam.getName())
            && joined.teams().size() == 2 && joined.invitation() == null
            && board.getPlayersTeam(dave.getScoreboardName()) == aliceTeam, "GUI acceptance joins the inviter's team and refreshes team tabs");
        check(inviteGui(dave, InvitationActionPayload.Action.ACCEPT, "", invitationId).response()
            == InvitationSyncPayload.Response.ERROR, "GUI acceptance cannot be replayed");
        receive(dave, new TeamLeavePayload(aliceTeam.getName()));
        check(response(dave, ConfigSyncPayload.class).response() == ConfigSyncPayload.Response.TEAM_LEFT, "Joined player can leave through the GUI");
        check(inviteGui(bob, InvitationActionPayload.Action.SEND, "MultiDave", null).response()
            == InvitationSyncPayload.Response.SUCCESS, "Another teammate can send a GUI invitation");
        UUID declineId = TeamInvitationManager.get(server).find(dave.getUUID()).id();
        check(inviteGui(dave, InvitationActionPayload.Action.DECLINE, "", declineId).response()
            == InvitationSyncPayload.Response.SUCCESS && TeamInvitationManager.get(server).find(dave.getUUID()) == null,
            "GUI decline consumes exactly the pending invitation");
        check(inviteGui(dave, InvitationActionPayload.Action.DECLINE, "", declineId).response()
            == InvitationSyncPayload.Response.ERROR, "GUI decline cannot be replayed");
    }

    @SuppressWarnings("unchecked")
    private <T extends CustomPacketPayload> void receive(ServerPlayer player, T payload) {
        // Invoke the actual registered server-side handler. No socket handshake/client rendering is claimed.
        var handler = (ServerPlayNetworking.PlayPayloadHandler<T>) ServerNetworkingImpl.PLAY.getHandler(payload.type().id());
        if (handler == null) { throw new AssertionError("Missing registered handler: " + payload.type().id()); }
        handler.receive(payload, new ServerPlayNetworking.Context() {
            public MinecraftServer server() { return server; }
            public ServerPlayer player() { return player; }
            public PacketSender responseSender() { return ServerPlayNetworking.getSender(player); }
        });
    }

    private <T extends CustomPacketPayload> T response(ServerPlayer player, Class<T> type) {
        EmbeddedChannel channel = playerChannels.get(player.getUUID());
        channel.runPendingTasks();
        T found = null;
        Object packet;
        while ((packet = channel.readOutbound()) != null) {
            if (packet instanceof ClientboundCustomPayloadPacket custom && type.isInstance(custom.payload())) {
                found = type.cast(custom.payload());
            }
        }
        if (found == null) { throw new AssertionError("Missing GUI response: " + type.getSimpleName()); }
        return found;
    }

    private InvitationSyncPayload inviteGui(ServerPlayer player, InvitationActionPayload.Action action, String target, UUID invitation) {
        UUID requestId = UUID.randomUUID();
        InvitationActionPayload request = new InvitationActionPayload(requestId, action, target, invitation);
        if (!roundTrip(InvitationActionPayload.CODEC, request).equals(request)) { throw new AssertionError("Invitation request codec"); }
        receive(player, request);
        InvitationSyncPayload response = response(player, InvitationSyncPayload.class);
        if (!response.requestId().equals(requestId)) { throw new AssertionError("GUI response request correlation"); }
        return response;
    }

    private <T> T roundTrip(StreamCodec<RegistryFriendlyByteBuf, T> codec, T input) {
        RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), server.registryAccess());
        try {
            codec.encode(buffer, input);
            T decoded = codec.decode(buffer);
            if (buffer.readableBytes() != 0) { throw new AssertionError("Codec left unread bytes"); }
            return decoded;
        } finally {
            buffer.release();
        }
    }

    private ServerPlayer player(String name) throws Exception {
        // Authlib changed GameProfile's constructor; keep the test source version-neutral.
        GameProfile profile;
        UUID id = UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(StandardCharsets.UTF_8));
        try {
            profile = GameProfile.class.getConstructor(UUID.class, String.class).newInstance(id, name);
        } catch (NoSuchMethodException changed) {
            Class<?> properties = Class.forName("com.mojang.authlib.properties.PropertyMap");
            profile = (GameProfile) GameProfile.class.getConstructors()[0].newInstance(id, name, properties.getField("EMPTY").get(null));
        }
        ServerPlayer player = new ServerPlayer(server, server.overworld(), profile, ClientInformation.createDefault());
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        EmbeddedChannel channel = new EmbeddedChannel(connection);
        channels.add(channel);
        playerChannels.put(id, channel);
        server.getPlayerList().placeNewPlayer(connection, player, CommonListenerCookie.createInitial(profile, false));
        player.setGameMode(GameType.SURVIVAL);
        return player;
    }

    private Set<String> suggestions(ServerPlayer player) throws Exception {
        var dispatcher = server.getCommands().getDispatcher();
        var parsed = dispatcher.parse("teamcraft manage team ", player.createCommandSourceStack());
        var suggestions = dispatcher.getCompletionSuggestions(parsed).get();
        Set<String> tokens = new HashSet<>();
        for (var suggestion : suggestions.getList()) { tokens.add(suggestion.getText()); }
        return tokens;
    }

    private int command(ServerPlayer player, String command) throws CommandSyntaxException {
        return command(player.createCommandSourceStack(), command);
    }

    private int command(CommandSourceStack source, String command) throws CommandSyntaxException {
        return server.getCommands().getDispatcher().execute(command, source);
    }

    private boolean rejected(ServerPlayer player, String command) {
        try { return command(player, command) == 0; }
        catch (CommandSyntaxException expected) { return true; }
    }

    private void drainAll() {
        for (EmbeddedChannel channel : channels) {
            channel.runPendingTasks();
            while (channel.readOutbound() != null) { }
        }
    }

    private void assertTeamPacketsEverywhere(String team, String label) {
        for (EmbeddedChannel channel : channels) {
            if (!teamPacketReceived(channel, team)) { throw new AssertionError(label + ": one player connection received no update"); }
        }
        check(true, label);
    }

    private boolean teamPacketReceived(EmbeddedChannel channel, String team) {
        channel.runPendingTasks();
        boolean received = false;
        Object packet;
        while ((packet = channel.readOutbound()) != null) {
            if (packet instanceof ClientboundSetPlayerTeamPacket update && update.getName().equals(team)) { received = true; }
        }
        return received;
    }

    private void check(boolean condition, String label) {
        if (!condition) { throw new AssertionError(label); }
        results.add("PASS: " + label);
        System.out.println("[TeamCraft multiplayer checks] " + label);
    }
}
