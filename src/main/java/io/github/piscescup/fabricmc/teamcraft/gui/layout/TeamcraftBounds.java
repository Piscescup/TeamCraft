package io.github.piscescup.fabricmc.teamcraft.gui.layout;

/** A rectangle in GUI-scaled pixels; right and bottom edges are exclusive. */
public record TeamcraftBounds(int left, int top, int right, int bottom) {
    public TeamcraftBounds {
        if (right < left || bottom < top) {
            throw new IllegalArgumentException("Inverted GUI bounds");
        }
    }

    public int width() { return this.right - this.left; }
    public int height() { return this.bottom - this.top; }

    public boolean contains(double x, double y) {
        return x >= this.left && x < this.right && y >= this.top && y < this.bottom;
    }
}
