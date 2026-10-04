package io.github.piscescup.fabricmc.teamcraft.gui.widget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

/** Themed option selector with keyboard activation and mouse-wheel cycling. */
@Environment(EnvType.CLIENT)
public final class TeamcraftCycleButton<T> extends TeamcraftButton {
    private final List<T> values;
    private final Function<T, Component> label;
    private final Consumer<T> onChanged;
    private int index;

    public TeamcraftCycleButton(List<T> values, T initial, Function<T, Component> label, Consumer<T> onChanged) {
        super(0, 0, 150, 20, label.apply(initial), button -> ((TeamcraftCycleButton<?>) button).cycle(1));
        this.values = List.copyOf(values);
        this.label = label;
        this.onChanged = onChanged;
        this.index = Math.max(0, this.values.indexOf(initial));
    }

    private void cycle(int direction) {
        this.index = Math.floorMod(this.index + direction, this.values.size());
        T value = this.values.get(this.index);
        setMessage(this.label.apply(value));
        this.onChanged.accept(value);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (!this.active || !this.visible || !isMouseOver(mouseX, mouseY) || vertical == 0) {
            return false;
        }
        cycle(vertical > 0 ? -1 : 1);
        return true;
    }
}
