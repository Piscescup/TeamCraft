package io.github.piscescup.fabricmc.teamcraft.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import java.util.List;

/** Optional capability for pages using the base's candidate click/drag controls. */
@Environment(EnvType.CLIENT)
public interface TeamcraftCandidateEditor {
    /** Returns the current mutable draft list; dragging reorders this list in place. */
    List<String> candidateNames();
}
