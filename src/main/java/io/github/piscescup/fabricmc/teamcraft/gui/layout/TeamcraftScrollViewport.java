package io.github.piscescup.fabricmc.teamcraft.gui.layout;

/**
 * Scroll state and coordinate conversion, without Minecraft or rendering dependencies.
 * Row coordinates are local to the content; toScreenY applies viewport origin and scrolling once.
 */
public final class TeamcraftScrollViewport {
    private TeamcraftBounds bounds = new TeamcraftBounds(0, 0, 0, 0);
    private int contentHeight;
    private double offset;

    public TeamcraftBounds bounds() { return this.bounds; }
    public int contentHeight() { return this.contentHeight; }
    public double offset() { return this.offset; }
    public int maximum() { return Math.max(0, this.contentHeight - this.bounds.height()); }

    /** Keep the offset until finish-build supplies the new content height. */
    public void setBounds(TeamcraftBounds bounds) { this.bounds = bounds; }

    public void setContentHeight(int height) {
        this.contentHeight = Math.max(0, height);
        clamp();
    }

    public void reset() { this.offset = 0; }

    public void scrollBy(double amount) {
        this.offset += amount;
        clamp();
    }

    public void clamp() { this.offset = Math.clamp(this.offset, 0, maximum()); }
    public int toScreenY(int localY) { return this.bounds.top() + localY - (int) this.offset; }

    public boolean intersects(int localY, int height) {
        int y = toScreenY(localY);
        return y + height > this.bounds.top() && y < this.bounds.bottom();
    }

    public boolean fullyVisible(int localY, int height) {
        int y = toScreenY(localY);
        return y >= this.bounds.top() && y + height <= this.bounds.bottom();
    }
}
