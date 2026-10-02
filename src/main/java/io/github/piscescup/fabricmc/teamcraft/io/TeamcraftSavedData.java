package io.github.piscescup.fabricmc.teamcraft.io;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.piscescup.fabricmc.teamcraft.References;
import io.github.piscescup.fabricmc.teamcraft.team.SplitMode;
import io.github.piscescup.fabricmc.teamcraft.team.TeamAssigner;
import io.github.piscescup.fabricmc.teamcraft.team.TeamSession;
import io.github.piscescup.fabricmc.teamcraft.text.TeamcraftColor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.LevelResource;
//#if MC >= 12105
import net.minecraft.world.level.saveddata.SavedDataType;
//#else
//$$ import net.minecraft.core.HolderLookup;
//#endif
//#if MC >= 260102
import net.minecraft.world.level.storage.SavedDataStorage;
//#else
//$$ import net.minecraft.world.level.storage.DimensionDataStorage;
//#endif

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;

/**
 * World-owned persistent storage for TeamCraft's server configuration.
 *
 * <p>The actual {@code PlayerTeam} instances remain in vanilla's scoreboard so
 * Minecraft can enforce membership, colors and friendly fire. This file stores
 * only TeamCraft's workflow state and references those teams by their internal
 * scoreboard ids.</p>
 *
 * <p>The data lives in the running world's {@code data} folder, next to
 * vanilla's own saved data such as the scoreboard: on 26.1.2 and newer at
 * {@code data/teamcraft/session.dat} (server-global storage), on older versions
 * at {@code data/teamcraft_session.dat} (overworld storage). A flat-named file
 * left by an older TeamCraft build is adopted on first load after an upgrade.</p>
 */
public final class TeamcraftSavedData extends SavedData {
    private static final String LEGACY_DATA_ID = "teamcraft_session";

    /**
     * Flat file name used by every build before 26.1.2, when the saved-data id
     * was still a plain string instead of a namespaced identifier.
     */
    private static final String LEGACY_FILE_NAME = LEGACY_DATA_ID + ".dat";
    //#if MC >= 260102
    /**
     * 26.1.2 turned the id into {@code teamcraft:session}, which the storage
     * resolves to {@code <namespace>/<path>.dat} under the world data folder.
     */
    private static final String DATA_FILE_NAME = References.MOD_ID + "/session.dat";
    //#endif

    private static final Codec<TeamcraftColor> COLOR_CODEC = Codec.STRING.xmap(
        name -> {
            TeamcraftColor color = TeamcraftColor.byName(name);
            return color == null ? TeamcraftColor.WHITE : color;
        },
        TeamcraftColor::word
    );

    public static final Codec<TeamcraftSavedData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
        Codec.STRING.listOf().optionalFieldOf("candidates", List.of())
            .forGetter(data -> data.session.getCandidates()),
        Codec.STRING.listOf().optionalFieldOf("created_teams", List.of())
            .forGetter(data -> data.session.getCreatedTeams()),
        Codec.INT.optionalFieldOf("team_size", TeamSession.DEFAULT_TEAM_SIZE)
            .forGetter(data -> data.session.getTeamSize()),
        Codec.INT.optionalFieldOf("team_count")
            .forGetter(data -> Optional.ofNullable(data.session.getTeamCount())),
        SplitMode.CODEC.optionalFieldOf("mode", SplitMode.RANDOM)
            .forGetter(data -> data.session.getMode()),
        Codec.BOOL.optionalFieldOf("friendly_fire", false)
            .forGetter(data -> data.session.isFriendlyFire()),
        COLOR_CODEC.listOf().optionalFieldOf("colors", List.of())
            .forGetter(data -> data.session.getColors()),
        Codec.STRING.listOf().optionalFieldOf("names", List.of())
            .forGetter(data -> data.session.getNames())
    ).apply(instance, TeamcraftSavedData::new));

    //#if MC >= 12105
    //#if MC >= 260102
    public static final SavedDataType<TeamcraftSavedData> TYPE = new SavedDataType<>(
        References.fromPath("session"),
        TeamcraftSavedData::new,
        CODEC,
        null
    );
    //#else
    //$$ public static final SavedDataType<TeamcraftSavedData> TYPE = new SavedDataType<>(
    //$$     LEGACY_DATA_ID,
    //$$     TeamcraftSavedData::new,
    //$$     CODEC,
    //$$     null
    //$$ );
    //#endif
    //#else
    //$$ public static final SavedData.Factory<TeamcraftSavedData> FACTORY = new SavedData.Factory<>(
    //$$     TeamcraftSavedData::new,
    //$$     TeamcraftSavedData::load,
    //$$     null
    //$$ );
    //#endif

    private final TeamSession session;

    private TeamcraftSavedData() {
        this(List.of(), List.of(), TeamSession.DEFAULT_TEAM_SIZE, Optional.empty(),
            SplitMode.RANDOM, false, List.of(), List.of());
    }

    private TeamcraftSavedData(
        List<String> candidates,
        List<String> createdTeams,
        int teamSize,
        Optional<Integer> teamCount,
        SplitMode mode,
        boolean friendlyFire,
        List<TeamcraftColor> colors,
        List<String> names
    ) {
        this.session = new TeamSession();
        this.session.getCandidates().addAll(new LinkedHashSet<>(candidates));
        this.session.getCreatedTeams().addAll(createdTeams.stream()
            .filter(TeamAssigner::isManagedTeamId)
            .distinct()
            .toList());
        this.session.setTeamSize(Math.max(1, teamSize));
        teamCount.filter(value -> value > 0).ifPresent(this.session::setTeamCount);
        this.session.setMode(mode);
        this.session.setFriendlyFire(friendlyFire);
        this.session.setColors(colors);
        this.session.setNames(names);

        // Loading above happens before the listener is attached, so decoding a
        // clean file does not immediately schedule an unnecessary rewrite.
        this.session.setChangeListener(this::setDirty);
    }

    /**
     * Loads or creates the state shared by the entire server save.
     */
    public static TeamcraftSavedData get(MinecraftServer server) {
        Path dataFolder = server.getWorldPath(LevelResource.ROOT).resolve("data");
        //#if MC >= 12105
        //#if MC >= 260102
        SavedDataStorage serverStorage = server.getDataStorage();
        TeamcraftSavedData data = readFromFile(server, dataFolder.resolve(DATA_FILE_NAME));
        if (data == null) {
            // Nothing written under the nested 26.1.2+ name yet; adopt the flat
            // file left by earlier TeamCraft builds so the config survives the
            // upgrade. Marking it dirty makes the storage rewrite it under the
            // new name at the next autosave.
            data = readFromFile(server, dataFolder.resolve(LEGACY_FILE_NAME));
        }
        if (data != null) {
            serverStorage.set(TYPE, data);
            data.setDirty();
            return data;
        }
        return serverStorage.computeIfAbsent(TYPE);
        //#else
        //$$ DimensionDataStorage storage = server.overworld().getDataStorage();
        //$$ TeamcraftSavedData data = readFromFile(server, dataFolder.resolve(LEGACY_FILE_NAME));
        //$$ if (data != null) {
        //$$     storage.set(TYPE, data);
        //$$     data.setDirty();
        //$$     return data;
        //$$ }
        //$$ return storage.computeIfAbsent(TYPE);
        //#endif
        //#else
        //$$ DimensionDataStorage storage = server.overworld().getDataStorage();
        //$$ TeamcraftSavedData data = readFromFile(server, dataFolder.resolve(LEGACY_FILE_NAME));
        //$$ if (data != null) {
        //$$     storage.set(LEGACY_DATA_ID, data);
        //$$     data.setDirty();
        //$$     return data;
        //$$ }
        //$$ return storage.computeIfAbsent(FACTORY, LEGACY_DATA_ID);
        //#endif
    }

    /**
     * Reads a session file written by a previous server run directly from disk.
     *
     * <p>This bypasses the storage's own read path on purpose: on every game
     * version this mod supports, that path passes the registered data fixer
     * type into an unguarded update call, and this mod has no data fixer to
     * register. With a blank type, loading an existing file fails and vanilla
     * silently resets the data to defaults.</p>
     *
     * @param server the running server, providing the codec's serialization context
     * @param file the compressed NBT file written by the storage
     * @return {@code null} when {@code file} does not exist; a fresh instance
     *         when the file exists but cannot be parsed, so the caller overwrites it
     */
    private static TeamcraftSavedData readFromFile(MinecraftServer server, Path file) {
        if (!Files.isRegularFile(file)) {
            return null;
        }
        try {
            CompoundTag root = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
            Tag payload = root.get("data");
            Optional<TeamcraftSavedData> parsed = payload == null
                ? Optional.empty()
                : CODEC.parse(server.registryAccess().createSerializationContext(NbtOps.INSTANCE), payload)
                    .resultOrPartial(error -> References.MOD_LOGGER
                        .error("Corrupt TeamCraft saved data in {}: {}", file, error));
            if (parsed.isPresent()) {
                References.MOD_LOGGER.info("Loaded TeamCraft session data from {}", file);
                return parsed.get();
            }
        }
        catch (Exception exception) {
            References.MOD_LOGGER.error("Could not read TeamCraft saved data from {}", file, exception);
        }
        References.MOD_LOGGER.error("Resetting TeamCraft saved data at {}", file);
        return new TeamcraftSavedData();
    }

    /**
     * @return the live server session backed by this saved-data object
     */
    public TeamSession session() {
        return this.session;
    }

    //#if MC < 12105
    //$$ private static TeamcraftSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
    //$$     return CODEC.parse(NbtOps.INSTANCE, tag).result().orElseGet(TeamcraftSavedData::new);
    //$$ }
    //$$
    //$$ @Override
    //$$ public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
    //$$     Tag encoded = CODEC.encodeStart(NbtOps.INSTANCE, this).result().orElse(tag);
    //$$     return encoded instanceof CompoundTag compound ? compound : tag;
    //$$ }
    //#endif
}
