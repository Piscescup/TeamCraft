package io.github.piscescup.fabricmc.teamcraft.io;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.piscescup.fabricmc.teamcraft.References;
import io.github.piscescup.fabricmc.teamcraft.permission.Permission;
import io.github.piscescup.fabricmc.teamcraft.permission.TeamCommandPermission;
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
import java.util.Optional;

import static io.github.piscescup.fabricmc.teamcraft.permission.TeamCommandPermission.*;


/**
 *
 * @author REN YuanTong
 * @since 1.0.0
 */
public class TeamcraftPermissionSavedData
    extends SavedData
{
    private static final String TEAMCRAFT_PERMISSION_ID = "teamcraft_permission";
    /**
     * Flat file name used by every build before 26.1.2, when the saved-data id
     * was still a plain string instead of a namespaced identifier.
     */
    private static final String TEAMCRAFT_PERMISSION_NAME = TEAMCRAFT_PERMISSION_ID + ".dat";

    //#if MC >= 260102
    /**
     * 26.1.2 turned the id into {@code teamcraft:session}, which the storage
     * resolves to {@code <namespace>/<path>.dat} under the world data folder.
     */
    private static final String DATA_FILE_NAME = References.MOD_ID + "/permission.dat";
    //#endif


    public static final Codec<TeamcraftPermissionSavedData> CODEC = RecordCodecBuilder.create(
        instance -> instance.group(
                Permission.CODEC.fieldOf(ROOT_KEY)
                    .forGetter(data -> data.permissions.getPermission(ROOT_KEY)),
                Permission.CODEC.fieldOf(INIT_KEY)
                    .forGetter(data -> data.permissions.getPermission(INIT_KEY)),
                Permission.CODEC.fieldOf(CONFIG_KEY)
                    .forGetter(data -> data.permissions.getPermission(CONFIG_KEY)),
                Permission.CODEC.fieldOf(BUILD_KEY)
                    .forGetter(data -> data.permissions.getPermission(BUILD_KEY)),
                Permission.CODEC.fieldOf(MANAGE_KEY)
                    .forGetter(data -> data.permissions.getPermission(MANAGE_KEY)),
                Permission.CODEC.fieldOf(CLEAR_KEY)
                    .forGetter(data -> data.permissions.getPermission(CLEAR_KEY)),
                Permission.CODEC.fieldOf(RESET_KEY)
                    .forGetter(data -> data.permissions.getPermission(RESET_KEY)),
                Permission.CODEC.fieldOf(STATUS_KEY)
                    .forGetter(data -> data.permissions.getPermission(STATUS_KEY)),
                Permission.CODEC.fieldOf(HELP_KEY)
                    .forGetter(data -> data.permissions.getPermission(HELP_KEY)),
                Permission.CODEC.optionalFieldOf(PERMISSION_KEY, Permission.LEVEL_GAMEMASTERS)
                    .forGetter(data -> data.permissions.getPermission(PERMISSION_KEY))
            )
                .apply(instance, TeamcraftPermissionSavedData::new)
    );

    //#if MC >= 12105
    //#if MC >= 260102
    public static final SavedDataType<TeamcraftPermissionSavedData> TYPE = new SavedDataType<>(
        References.fromPath("permission"),
        TeamcraftPermissionSavedData::new,
        CODEC,
        null
    );
    //#else
    //$$ public static final SavedDataType<TeamcraftPermissionSavedData> TYPE = new SavedDataType<>(
    //$$     TEAMCRAFT_PERMISSION_NAME,
    //$$     TeamcraftPermissionSavedData::new,
    //$$     CODEC,
    //$$     null
    //$$ );
    //#endif
    //#else
    //$$ public static final SavedData.Factory<TeamcraftPermissionSavedData> FACTORY = new SavedData.Factory<>(
    //$$     TeamcraftPermissionSavedData::new,
    //$$     TeamcraftPermissionSavedData::load,
    //$$     null
    //$$ );
    //#endif

    private final TeamCommandPermission permissions;

    private TeamcraftPermissionSavedData() {
        this.permissions = TeamCommandPermission.Builder.defaultConfig().build();
    }

    private TeamcraftPermissionSavedData(
        Permission rootPermission,
        Permission initPermission,
        Permission configPermission,
        Permission buildPermission,
        Permission managePermission,
        Permission clearPermission,
        Permission resetPermission,
        Permission statusPermission,
        Permission helpPermission,
        Permission permissionPermission
    )
    {
        this.permissions = TeamCommandPermission.Builder.create()
            .root(rootPermission)
            .init(initPermission)
            .config(configPermission)
            .buildTeam(buildPermission)
            .manage(managePermission)
            .clear(clearPermission)
            .reset(resetPermission)
            .status(statusPermission)
            .help(helpPermission)
            .permission(permissionPermission)
            .build();
    }

    public TeamCommandPermission permissions() {
        return this.permissions;
    }


    /**
     * Loads or creates the state shared by the entire server save.
     */
    public static TeamcraftPermissionSavedData get(MinecraftServer server) {
        Path dataFolder = server.getWorldPath(LevelResource.ROOT).resolve("data");
        //#if MC >= 12105
        //#if MC >= 260102
        SavedDataStorage serverStorage = server.getDataStorage();
        TeamcraftPermissionSavedData data = readFromFile(server, dataFolder.resolve(DATA_FILE_NAME));
        if (data == null) {
            // Nothing written under the nested 26.1.2+ name yet; adopt the flat
            // file left by earlier TeamCraft builds so the config survives the
            // upgrade. Marking it dirty makes the storage rewrite it under the
            // new name at the next autosave.
            data = readFromFile(server, dataFolder.resolve(TEAMCRAFT_PERMISSION_NAME));
        }
        if (data != null) {
            serverStorage.set(TYPE, data);
            data.setDirty();
            return data;
        }
        return serverStorage.computeIfAbsent(TYPE);
        //#else
        //$$ DimensionDataStorage storage = server.overworld().getDataStorage();
        //$$ TeamcraftPermissionSavedData data = readFromFile(server, dataFolder.resolve(TEAMCRAFT_PERMISSION_NAME));
        //$$ if (data != null) {
        //$$     storage.set(TYPE, data);
        //$$     data.setDirty();
        //$$     return data;
        //$$ }
        //$$ return storage.computeIfAbsent(TYPE);
        //#endif
        //#else
        //$$ DimensionDataStorage storage = server.overworld().getDataStorage();
        //$$ TeamcraftPermissionSavedData data = readFromFile(server, dataFolder.resolve(TEAMCRAFT_PERMISSION_NAME));
        //$$ if (data != null) {
        //$$     storage.set(TEAMCRAFT_PERMISSION_NAME, data);
        //$$     data.setDirty();
        //$$     return data;
        //$$ }
        //$$ return storage.computeIfAbsent(FACTORY, TEAMCRAFT_PERMISSION_NAME);
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
    private static TeamcraftPermissionSavedData readFromFile(MinecraftServer server, Path file) {
        if (!Files.isRegularFile(file)) {
            return null;
        }
        try {
            CompoundTag root = NbtIo.readCompressed(file, NbtAccounter.unlimitedHeap());
            Tag payload = root.get("data");
            Optional<TeamcraftPermissionSavedData> parsed = payload == null
                ? Optional.empty()
                : CODEC.parse(server.registryAccess().createSerializationContext(NbtOps.INSTANCE), payload)
                    .resultOrPartial(error -> References.MOD_LOGGER
                        .error("Corrupt TeamCraft Permission saved data in {}: {}", file, error));
            if (parsed.isPresent()) {
                References.MOD_LOGGER.info("Loaded TeamCraft Permission saved data from {}", file);
                return parsed.get();
            }
        }
        catch (Exception exception) {
            References.MOD_LOGGER.error("Could not read TeamCraft Permission saved data from {}", file, exception);
        }
        References.MOD_LOGGER.error("Resetting TeamCraft Permission saved data at {}", file);
        return new TeamcraftPermissionSavedData();
    }

    //#if MC < 12105
    //$$ private static TeamcraftPermissionSavedData load(CompoundTag tag, HolderLookup.Provider registries) {
    //$$     return CODEC.parse(NbtOps.INSTANCE, tag).result().orElseGet(TeamcraftPermissionSavedData::new);
    //$$ }
    //$$
    //$$ @Override
    //$$ public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
    //$$     Tag encoded = CODEC.encodeStart(NbtOps.INSTANCE, this).result().orElse(tag);
    //$$     return encoded instanceof CompoundTag compound ? compound : tag;
    //$$ }
    //#endif
}
