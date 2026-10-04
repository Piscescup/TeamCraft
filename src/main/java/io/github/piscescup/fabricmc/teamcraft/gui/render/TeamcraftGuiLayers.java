package io.github.piscescup.fabricmc.teamcraft.gui.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.Objects;

/** Separates overlay batches on old GUI renderers and strata on newer versions. */
@Environment(EnvType.CLIENT)
public final class TeamcraftGuiLayers {
    public enum Layer {
        POPUP(200.0F),
        TOOLTIP(400.0F),
        DIALOG(600.0F);

        private final float depth;

        Layer(float depth) {
            this.depth = depth;
        }
    }

    private TeamcraftGuiLayers() {
    }

    /** The caller still owns its scissor region; this method always restores the legacy pose. */
    public static void draw(GuiGraphicsExtractor graphics, Layer layer, Runnable contents) {
        Objects.requireNonNull(layer, "layer");
        Objects.requireNonNull(contents, "contents");
        //#if MC >= 12108
        graphics.nextStratum();
        contents.run();
        //#else
        //$$ // Fonts and rectangles use different batches. Submit underlying text before the overlay.
        //$$ graphics.flush();
        //$$ graphics.pose().pushPose();
        //$$ graphics.pose().translate(0.0F, 0.0F, layer.depth);
        //$$ try {
        //$$     contents.run();
        //$$ } finally {
        //$$     try {
        //$$         graphics.flush();
        //$$     } finally {
        //$$         graphics.pose().popPose();
        //$$     }
        //$$ }
        //#endif
    }
}
