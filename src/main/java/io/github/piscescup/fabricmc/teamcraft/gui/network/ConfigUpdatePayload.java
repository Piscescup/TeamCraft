package io.github.piscescup.fabricmc.teamcraft.gui.network;

import io.github.piscescup.fabricmc.teamcraft.References;
import io.github.piscescup.fabricmc.teamcraft.config.TeamcraftConfigData;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

/** Sends one complete configuration draft and optionally starts the split. */
public record ConfigUpdatePayload(
    TeamcraftConfigData config,
    List<String> candidates,
    boolean buildTeams
) implements CustomPacketPayload {
    public static final Type<ConfigUpdatePayload> TYPE = new Type<>(References.fromPath("config_update"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigUpdatePayload> CODEC =
        CustomPacketPayload.codec(ConfigUpdatePayload::write, ConfigUpdatePayload::new);

    public ConfigUpdatePayload {
        candidates = List.copyOf(candidates);
    }

    private ConfigUpdatePayload(RegistryFriendlyByteBuf buffer) {
        this(TeamcraftConfigData.read(buffer), readCandidates(buffer), buffer.readBoolean());
    }

    private void write(RegistryFriendlyByteBuf buffer) {
        this.config.write(buffer);
        buffer.writeVarInt(this.candidates.size());
        for (String candidate : this.candidates) {
            buffer.writeUtf(candidate, TeamcraftConfigData.MAX_NAME_LENGTH);
        }
        buffer.writeBoolean(this.buildTeams);
    }

    @NonNull
    @Override
    public Type<ConfigUpdatePayload> type() {
        return TYPE;
    }

    private static List<String> readCandidates(RegistryFriendlyByteBuf buffer) {
        int size = buffer.readVarInt();
        if (size < 0 || size > TeamcraftConfigData.MAX_CANDIDATES) {
            throw new IllegalArgumentException("Invalid TeamCraft candidate list size: " + size);
        }
        List<String> candidates = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            candidates.add(buffer.readUtf(TeamcraftConfigData.MAX_NAME_LENGTH));
        }
        return candidates;
    }
}
