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
            state.panX += (float) (mouseX - state.lastMouseX);
            state.panY += (float) (mouseY - state.lastMouseY);
            state.lastMouseX = mouseX;
            state.lastMouseY = mouseY;
            return true;
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollY) {
        float zoomSensitivity = 0.1f;
        float oldZoom = state.zoom;
        if (scrollY > 0) {
            state.zoom *= (1 + zoomSensitivity);
        } else {
            state.zoom /= (1 + zoomSensitivity);
        }
        
        // Zoom limits
        state.zoom = Math.max(0.1f, Math.min(3.0f, state.zoom));
        
        if (state.zoom != oldZoom) {
            // Adjust pan to zoom towards mouse position
            double worldMouseX = (mouseX - state.panX) / oldZoom;
            double worldMouseY = (mouseY - state.panY) / oldZoom;
            state.panX = (float) (mouseX - worldMouseX * state.zoom);
            state.panY = (float) (mouseY - worldMouseY * state.zoom);
        }
        return true;
    }
}


