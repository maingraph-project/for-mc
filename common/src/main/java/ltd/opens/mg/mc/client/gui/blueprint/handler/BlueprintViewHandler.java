package ltd.opens.mg.mc.client.gui.blueprint.handler;

import ltd.opens.mg.mc.client.gui.blueprint.BlueprintState;


import net.minecraft.client.input.MouseButtonEvent;

public class BlueprintViewHandler {
    private final BlueprintState state;

    public BlueprintViewHandler(BlueprintState state) {
        this.state = state;
    }

    public boolean mouseClicked(MouseButtonEvent event) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.buttonInfo().button();
        if (button == 2 || button == 1) { // Middle click or Right click for panning
            state.isPanning = true;
            state.isAnimatingView = false; // Stop animation if user starts manual panning
            state.lastMouseX = mouseX;
            state.lastMouseY = mouseY;
            state.startMouseX = mouseX;
            state.startMouseY = mouseY;
            return true;
        }
        return false;
    }

    public boolean mouseReleased(MouseButtonEvent event) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.buttonInfo().button();
        if (state.isPanning && (button == 2 || button == 1)) {
            state.isPanning = false;
            
            // 如果是右键，且位移很小，返回 false，让外层逻辑触发菜单
            if (button == 1) {
                double dist = Math.sqrt(Math.pow(mouseX - state.startMouseX, 2) + Math.pow(mouseY - state.startMouseY, 2));
                if (dist < 5.0) {
                    return false; // 不消费事件，交给菜单处理器
                }
            }
            return true;
        }
        return false;
    }

    public boolean mouseDragged(MouseButtonEvent event) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (state.isPanning) {
            state.viewport.pan(mouseX - state.lastMouseX, mouseY - state.lastMouseY);
            state.lastMouseX = mouseX;
            state.lastMouseY = mouseY;
            return true;
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        state.isAnimatingView = false; // Stop animation if user starts zooming
        float zoomSensitivity = 1.1f;
        float factor = scrollY > 0 ? zoomSensitivity : 1.0f / zoomSensitivity;
        
        state.viewport.zoom(factor, mouseX, mouseY);
        
        return true;
    }
}


