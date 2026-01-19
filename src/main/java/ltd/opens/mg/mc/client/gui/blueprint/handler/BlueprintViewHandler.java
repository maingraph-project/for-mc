package ltd.opens.mg.mc.client.gui.blueprint.handler;

import ltd.opens.mg.mc.client.gui.blueprint.BlueprintState;


public class BlueprintViewHandler {
    private final BlueprintState state;

    public BlueprintViewHandler(BlueprintState state) {
        this.state = state;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
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

    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (state.isPanning && (button == 2 || button == 1)) {
            state.isPanning = false;
            
            // 濡傛灉鏄彸閿紝涓斾綅绉诲緢灏忥紝杩斿洖 false锛岃澶栧眰閫昏緫瑙﹀彂鑿滃崟
            if (button == 1) {
                double dist = Math.sqrt(Math.pow(mouseX - state.startMouseX, 2) + Math.pow(mouseY - state.startMouseY, 2));
                if (dist < 5.0) {
                    return false; // 涓嶆秷璐逛簨浠讹紝浜ょ粰鑿滃崟澶勭悊鍣?
                }
            }
            return true;
        }
        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY) {
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


