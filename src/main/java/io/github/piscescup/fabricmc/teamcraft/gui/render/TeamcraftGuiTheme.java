package io.github.piscescup.fabricmc.teamcraft.gui.render;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

/** TeamCraft's own drawing primitives for compact, tabbed configuration screens. */
@Environment(EnvType.CLIENT)
public final class TeamcraftGuiTheme {
    public static final int BACKGROUND = 0x70000000;
    public static final int PANEL = 0xEE101010;
    public static final int BORDER = 0xFF606060;
    public static final int TEXT = 0xFFE0E0E0;
    public static final int MUTED = 0xFFA0A0A0;
    public static final int ACCENT = 0xFFFFD060;
    public static final int ERROR = 0xFFFF6666;
    public static final int ROW_TEXT = 0xFFE6E6E6;
    public static final int INPUT_BACKGROUND = 0xE0000000;
    public static final int COLOR_HOVER = 0xFF404040;
    public static final int DRAG_MARKER = 0xFFFFAA00;
    public static final int TOOLTIP_BACKGROUND = 0xF0100010;
    public static final int TOOLTIP_BORDER = 0xFF500060;

    private TeamcraftGuiTheme() {
    }

    public static void frame(GuiGraphicsExtractor graphics, int left, int top, int right, int bottom,
                      int background, int border) {
        if (right <= left || bottom <= top) {
            return;
        }
        graphics.fill(left, top, right, bottom, border);
        graphics.fill(left + 1, top + 1, right - 1, bottom - 1, background);
    }

    public static void screen(GuiGraphicsExtractor graphics, Font font, Component title,
                       int width, int height, int footerTop) {
        graphics.fill(0, 0, width, height, BACKGROUND);
        graphics.text(font, title, 20, 6, 0xFFFFFFFF);
        graphics.fill(10, footerTop, width - 10, footerTop + 1, BORDER);
    }

    public static void button(GuiGraphicsExtractor graphics, int left, int top, int right, int bottom,
                       boolean active, boolean highlighted, boolean selected) {
        frame(graphics, left, top, right, bottom,
            selected || !active ? 0xFF303030 : highlighted ? 0xFF808080 : 0xFF686868, 0xFF080808);
        int light = selected || !active ? 0xFF505050 : highlighted ? 0xFFFFFFFF : 0xFFC0C0C0;
        graphics.fill(left + 1, top + 1, right - 1, top + 2, light);
        graphics.fill(left + 1, top + 1, left + 2, bottom - 1, light);
        graphics.fill(left + 2, bottom - 2, right - 1, bottom - 1, 0xFF303030);
        graphics.fill(right - 2, top + 2, right - 1, bottom - 1, 0xFF303030);
    }

    public static void smallText(GuiGraphicsExtractor graphics, Font font, String text, int x, int y, int width) {
        float scale = 0.75F;
        var lines = font.split(Component.literal(text), Math.max(1, (int) (width / scale)));
        if (lines.isEmpty()) {
            return;
        }
        //#if MC >= 12108
        graphics.pose().pushMatrix();
        graphics.pose().scale(scale, scale);
        //#else
        //$$ graphics.pose().pushPose();
        //$$ graphics.pose().scale(scale, scale, 1.0F);
        //#endif
        graphics.text(font, lines.getFirst(), (int) (x / scale), (int) (y / scale), MUTED, false);
        //#if MC >= 12108
        graphics.pose().popMatrix();
        //#else
        //$$ graphics.pose().popPose();
        //#endif
    }

    /** Section heading, including the shared underline. */
    public static void header(GuiGraphicsExtractor graphics, Font font, Component label,
                              int left, int y, int right, int height) {
        graphics.fill(left + 4, y + height - 2, right - 8, y + height - 1, BORDER);
        graphics.text(font, label.copy().withStyle(net.minecraft.ChatFormatting.BOLD).getVisualOrderText(),
            left + 5, y + 6, 0xFFFFFFFF);
    }

    public static void row(GuiGraphicsExtractor graphics, int left, int top, int right, int bottom,
                    boolean hovered) {
        if (hovered) {
            graphics.fill(left, top, right, bottom, 0x18000000);
        }
    }

    public static void scrollbar(GuiGraphicsExtractor graphics, int right, int top, int bottom,
                          int contentHeight, double offset) {
        int viewport = bottom - top;
        int maximum = Math.max(0, contentHeight - viewport);
        if (maximum == 0 || viewport <= 0) {
            return;
        }
        int thumb = Math.min(viewport, Math.max(12, viewport * viewport / contentHeight));
        int y = top + (int) ((viewport - thumb) * offset / maximum);
        frame(graphics, right - 6, top, right, bottom, 0xC0000000, BORDER);
        graphics.fill(right - 5, y, right - 1, y + thumb, 0xFFC0C0C0);
    }
}
