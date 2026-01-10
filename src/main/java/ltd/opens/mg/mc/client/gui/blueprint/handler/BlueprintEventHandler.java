package ltd.opens.mg.mc.client.gui.blueprint.handler;

import ltd.opens.mg.mc.client.gui.blueprint.BlueprintState;
import ltd.opens.mg.mc.client.gui.blueprint.menu.*;


import ltd.opens.mg.mc.client.gui.screens.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;

public class BlueprintEventHandler {
    private final BlueprintState state;
    private final BlueprintViewHandler viewHandler;
    private final BlueprintMenuHandler menuHandler;
    private final BlueprintConnectionHandler connectionHandler;
    private final BlueprintNodeHandler nodeHandler;

    public BlueprintEventHandler(BlueprintState state) {
        this.state = state;
        this.viewHandler = new BlueprintViewHandler(state);
        this.menuHandler = new BlueprintMenuHandler(state);
        this.connectionHandler = new BlueprintConnectionHandler(state);
        this.nodeHandler = new BlueprintNodeHandler(state);
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean isDouble, Font font, BlueprintScreen screen) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.buttonInfo().button();

        // Block all blueprint interactions if clicking the top bar
        if (mouseY < 26) return false;

        // Block all modifications if in read-only mode
        if (state.readOnly) {
            // Only allow panning (middle mouse or right click drag) and zooming
            if (button != 2 && button != 1) {
                // Allow left click for panning start if it's the only way, but usually it's button 2 or 1
                // For now, let's just allow panning logic to run
            }
        }

        // 1. Menu interactions (context menu or creation menu)
        if (state.readOnly) {
            // Skip node menu and context menu in read-only
        } else {
            if (menuHandler.mouseClicked(mouseX, mouseY, button, screen.width, screen.height)) return true;
        }

        // 2. View interactions (panning start)
        if (viewHandler.mouseClicked(mouseX, mouseY, button)) return true;

        // World coordinates for other interactions
        double worldMouseX = (mouseX - state.panX) / state.zoom;
        double worldMouseY = (mouseY - state.panY) / state.zoom;

        if (button == 0) { // Left click
            state.focusedNode = null;
            state.focusedPort = null;
            
            if (!state.readOnly) {
                // 3. Connection interactions (port click start)
                if (connectionHandler.mouseClicked(worldMouseX, worldMouseY)) return true;

                // 4. Node interactions (input box or header drag start)
                if (nodeHandler.mouseClicked(worldMouseX, worldMouseY, font, screen)) return true;
            }
        }

        return false;
    }

    public boolean mouseReleased(MouseButtonEvent event, BlueprintScreen screen) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.buttonInfo().button();

        // 1. View interactions (panning end)
        if (viewHandler.mouseReleased(mouseX, mouseY, button)) return true;

        if (state.readOnly) return false;

        // 2. Menu interactions (open context menu on right click release)
        if (menuHandler.mouseReleased(mouseX, mouseY, button, screen.width, screen.height)) return true;

        // World coordinates for other interactions
        double worldMouseX = (mouseX - state.panX) / state.zoom;
        double worldMouseY = (mouseY - state.panY) / state.zoom;

        // 2. Connection interactions (link creation)
        if (connectionHandler.mouseReleased(worldMouseX, worldMouseY)) return true;

        // 3. Node interactions (drag end)
        if (nodeHandler.mouseReleased()) return true;

        return false;
    }

    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        double mouseX = event.x();
        double mouseY = event.y();

        // 1. View interactions (panning drag)
        if (viewHandler.mouseDragged(mouseX, mouseY)) return true;

        if (state.readOnly) return false;

        // World coordinates for other interactions
        double worldMouseX = (mouseX - state.panX) / state.zoom;
        double worldMouseY = (mouseY - state.panY) / state.zoom;

        // 2. Node interactions (node drag)
        if (nodeHandler.mouseDragged(worldMouseX, worldMouseY)) return true;

        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY, BlueprintScreen screen) {
        if (menuHandler.mouseScrolled(mouseX, mouseY, screen.width, screen.height, scrollY)) return true;
        return viewHandler.mouseScrolled(mouseX, mouseY, scrollY);
    }

    public boolean keyPressed(KeyEvent event) {
        if (state.readOnly) return false;
        if (menuHandler.keyPressed(event.key())) return true;
        return nodeHandler.keyPressed(event.key());
    }

    public boolean charTyped(CharacterEvent event) {
        if (state.readOnly) return false;
        return menuHandler.charTyped(event);
    }
}


