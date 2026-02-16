package ltd.opens.mg.mc.client.gui.blueprint.handler;

import ltd.opens.mg.mc.client.gui.blueprint.BlueprintState;
import ltd.opens.mg.mc.client.gui.components.GuiNode;
import ltd.opens.mg.mc.client.gui.components.GuiRegion;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.ArrayList;
import java.util.List;

public class BlueprintRegionHandler {
    private final BlueprintState state;
    private double lastDragX, lastDragY;
    private boolean draggingRecorded = false;

    public BlueprintRegionHandler(BlueprintState state) {
        this.state = state;
    }

    public boolean mouseClicked(MouseButtonEvent event) {
        if (event.buttonInfo().button() != 0) return false; // Only Left Click

        double worldMouseX = state.viewport.toWorldX(event.x());
        double worldMouseY = state.viewport.toWorldY(event.y());

        // Check regions in reverse order (top to bottom)
        for (int i = state.regions.size() - 1; i >= 0; i--) {
            GuiRegion region = state.regions.get(i);
            
            // 1. Check Resize Handle (Bottom-Right 20x20 area)
            if (region.isSelected && 
                worldMouseX >= region.x + region.width - 20 && worldMouseX <= region.x + region.width &&
                worldMouseY >= region.y + region.height - 20 && worldMouseY <= region.y + region.height) {
                
                region.isResizing = true;
                state.draggingRegion = region;
                state.isMouseDown = true;
                state.isBoxSelecting = false; // Cancel optimistic box selection
                return true;
            }

            // 2. Check Hit
            if (worldMouseX >= region.x && worldMouseX <= region.x + region.width &&
                worldMouseY >= region.y && worldMouseY <= region.y + region.height) {
                
                // If clicking inside region, BUT not on header, we should check if we should prioritize selection
                // However, BlueprintEventHandler calls NodeHandler BEFORE this. 
                // So if we are here, it means we did NOT click on a node.
                
                // Select region
                if (!Screen.hasShiftDown() && !Screen.hasControlDown()) {
                    // Deselect others unless modifier
                    for (GuiRegion r : state.regions) r.isSelected = false;
                    state.selectedNodes.clear(); // Clear node selection too? Maybe.
                    for (GuiNode n : state.nodes) n.isSelected = false;
                }
                
                region.isSelected = true;
                state.draggingRegion = region;
                lastDragX = worldMouseX;
                lastDragY = worldMouseY;
                draggingRecorded = false; // Reset for new drag action
                
                // Cancel optimistic box selection from NodeHandler
                state.isBoxSelecting = false;

                // If clicking Header, prepare for drag
                if (worldMouseY <= region.y + 20) {
                    // Clicking header
                } else {
                    // Clicking body
                    // The user said: "if clicking region, move nodes inside"
                    // So dragging body should also move region + nodes
                }
                
                state.isMouseDown = true;
                return true;
            }
        }
        
        // Deselect all regions if clicked on empty space (and not handled by anyone else)
        // But this handler is called after others. If we reach here, we clicked on nothing relevant to regions.
        // We should probably deselect regions if we click outside? 
        // Actually, BlueprintEventHandler handles background clicks for panning. 
        // We can leave deselection to logic that handles "click on nothing".
        
        return false;
    }

    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (state.draggingRegion == null) return false;

        GuiRegion region = state.draggingRegion;
        double worldMouseX = state.viewport.toWorldX(event.x());
        double worldMouseY = state.viewport.toWorldY(event.y());
        
        // Calculate delta in world coordinates
        // dragX/dragY are screen delta? No, usually they are raw delta.
        // Let's use world coordinate delta.
        double dx = worldMouseX - lastDragX;
        double dy = worldMouseY - lastDragY;
        
        if (Math.abs(dx) > 0.0001 || Math.abs(dy) > 0.0001) {
            if (!draggingRecorded) {
                state.pushHistory(); // Save state before modifying
                draggingRecorded = true;
            }
            state.markDirty(); // Mark as modified
        } else {
            return false; // No movement, no update
        }

        if (region.isResizing) {
            region.width = (float) Math.max(100, worldMouseX - region.x);
            region.height = (float) Math.max(100, worldMouseY - region.y);
        } else {
            // Moving region
            region.x += dx;
            region.y += dy;
            
            // Move contained nodes (unless Ctrl is held)
            if (!Screen.hasControlDown()) {
                for (GuiNode node : state.nodes) {
                    // Check if node center was inside region BEFORE move? 
                    // Or check if node is currently "inside" (ignoring that we just moved the region).
                    // Actually, standard behavior: Find nodes that are "contained" and move them.
                    // But we need to know which nodes were contained.
                    // Simple approach: Check if node is inside region area (using previous region position? No, just check if node is strictly inside)
                    // But if we move region, the node moves WITH it, so relative position stays same.
                    
                    // Refined approach: When starting drag (mouseClicked), identify contained nodes.
                    // But here we do it continuously.
                    
                    // Let's assume nodes inside the region should move.
                    // But we need to ensure we don't double move if multiple regions are selected (not supported yet really).
                    
                    // Check if node is inside the region boundaries (using NEW position, wait, if we use new position, we might capture nodes as we sweep over them?)
                    // NO. We should capture nodes at START of drag.
                    
                    // For now, let's just check if node is inside the region's current bounding box (which we just updated).
                    // If we move the region, the node should move by same amount.
                    // We need to check if node WAS inside. 
                    
                    // To avoid complex state, let's just check:
                    // Is node center inside region?
                    float nodeCenterX = node.x + node.width / 2;
                    float nodeCenterY = node.y + node.height / 2;
                    
                    // We just moved region by dx, dy. So previous region pos was x-dx, y-dy.
                    // Check against PREVIOUS position.
                    float prevRegionX = region.x - (float)dx;
                    float prevRegionY = region.y - (float)dy;
                    
                    if (nodeCenterX >= prevRegionX && nodeCenterX <= prevRegionX + region.width &&
                        nodeCenterY >= prevRegionY && nodeCenterY <= prevRegionY + region.height) {
                        
                        node.x += dx;
                        node.y += dy;
                        node.targetX = node.x; // Update target for animation consistency if needed
                        node.targetY = node.y;
                    }
                }
            }
        }
        
        lastDragX = worldMouseX;
        lastDragY = worldMouseY;
        return true;
    }

    public boolean mouseReleased(MouseButtonEvent event) {
        if (state.draggingRegion != null) {
            state.draggingRegion.isResizing = false;
            state.draggingRegion = null;
            return true;
        }
        return false;
    }
}
