package net.minecraft.client.input;

public record MouseButtonEvent(double x, double y, ButtonInfo buttonInfo) {
    public boolean hasControlDown() {
        return (buttonInfo.modifiers() & 2) != 0;
    }

    public boolean hasShiftDown() {
        return (buttonInfo.modifiers() & 1) != 0;
    }

    public boolean hasAltDown() {
        return (buttonInfo.modifiers() & 4) != 0;
    }
}
