package net.minecraft.client.input;

public record KeyEvent(int key, int scanCode, int action, int modifiers) {
    public boolean hasControlDown() {
        return (modifiers & 2) != 0;
    }

    public boolean hasShiftDown() {
        return (modifiers & 1) != 0;
    }

    public boolean hasAltDown() {
        return (modifiers & 4) != 0;
    }
}
