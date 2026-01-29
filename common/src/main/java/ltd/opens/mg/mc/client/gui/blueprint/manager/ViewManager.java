package ltd.opens.mg.mc.client.gui.blueprint.manager;

import ltd.opens.mg.mc.client.gui.blueprint.BlueprintState;
import ltd.opens.mg.mc.client.gui.components.GuiNode;

public class ViewManager {
    private final BlueprintState state;
    
    public float targetPanX = 0;
    public float targetPanY = 0;
    public float targetZoom = 1.0f;
    
    private static final float PAN_SMOOTHING = 0.2f;
    private static final float ZOOM_SMOOTHING = 0.15f;

    public ViewManager(BlueprintState state) {
        this.state = state;
    }

    public void tick() {
        if (state.isAnimatingView) {
            float dx = targetPanX - state.viewport.panX;
            float dy = targetPanY - state.viewport.panY;
            float dz = targetZoom - state.viewport.zoom;
            
            if (Math.abs(dx) < 0.1f && Math.abs(dy) < 0.1f && Math.abs(dz) < 0.005f) {
                state.viewport.set(targetPanX, targetPanY, targetZoom);
                state.isAnimatingView = false;
            } else {
                state.viewport.panX += dx * PAN_SMOOTHING;
                state.viewport.panY += dy * PAN_SMOOTHING;
                state.viewport.zoom += dz * ZOOM_SMOOTHING;
            }
        }
    }

    public void jumpToNode(GuiNode node, int screenWidth, int screenHeight) {
        targetZoom = 1.0f;
        targetPanX = screenWidth / 2f - (node.x + node.width / 2f) * targetZoom;
        targetPanY = screenHeight / 2f - (node.y + node.height / 2f) * targetZoom;
        state.isAnimatingView = true;
        state.highlightedNode = node;
        state.highlightTimer = 40;
        state.addToHistory(node);
    }

    public void centerOnNode(GuiNode node) {
        targetZoom = 1.0f;
        targetPanX = -node.x; 
        targetPanY = -node.y;
        state.isAnimatingView = true;
    }

    public void resetView() {
        targetPanX = 0;
        targetPanY = 0;
        targetZoom = 1.0f;
        state.isAnimatingView = true;
    }
}
