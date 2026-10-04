package io.github.piscescup.fabricmc.teamcraft.gui.layout;

/** Outer page geometry only: title, wrapped tabs, content viewport and footer. */
public record TeamcraftPageLayout(TeamcraftBounds panel, TeamcraftBounds content) {
    public static final int TAB_TOP = 26;
    public static final int CONTROL_HEIGHT = 20;
    public static final int TAB_GAP = 2;

    public static TeamcraftPageLayout forScreen(int width, int height) {
        int panelWidth = Math.max(1, width - 20);
        int panelHeight = Math.max(1, height - 16);
        int left = (width - panelWidth) / 2;
        int top = (height - panelHeight) / 2;
        TeamcraftBounds panel = new TeamcraftBounds(left, top, left + panelWidth, top + panelHeight);
        return new TeamcraftPageLayout(panel, panel).withTabsBottom(TAB_TOP + CONTROL_HEIGHT);
    }

    public TeamcraftPageLayout withTabsBottom(int tabsBottom) {
        int top = Math.clamp(tabsBottom + 10, this.panel.top(), this.panel.bottom());
        int bottom = Math.max(top, this.panel.bottom() - 35);
        return new TeamcraftPageLayout(this.panel,
            new TeamcraftBounds(this.panel.left(), top, this.panel.right(), bottom));
    }

    public int footerY() { return Math.max(this.panel.top(), this.panel.bottom() - 22); }
    public int footerSeparatorY() { return Math.max(this.panel.top(), this.panel.bottom() - 29); }
}
