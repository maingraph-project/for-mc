package ltd.opens.mg.mc.client.gui.blueprint.handler;

import ltd.opens.mg.mc.client.gui.blueprint.BlueprintState;
import ltd.opens.mg.mc.client.gui.blueprint.menu.*;
import ltd.opens.mg.mc.client.gui.components.GuiNode;

import java.util.List;


import ltd.opens.mg.mc.client.gui.screens.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;

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

        // 0. Quick Search interactions
        if (state.showQuickSearch) {
            if (state.quickSearchEditBox != null) {
                if (state.quickSearchEditBox.mouseClicked(event, false)) {
                    return true;
                }
                
                // Check if clicked on a candidate
                int searchW = 200;
                int x = (screen.width - searchW) / 2;
                int y = screen.height / 4;
                int itemHeight = 18;
                int listY = y + 42; // Match BlueprintRenderer listY
                List<GuiNode> displayList = state.quickSearchEditBox.getValue().isEmpty() ? state.searchHistory : state.quickSearchMatches;
                
                if (mouseX >= x && mouseX <= x + searchW && !displayList.isEmpty()) {
                    int clickedVisibleIdx = (int) ((mouseY - (listY + 3)) / itemHeight);
                    int clickedIdx = clickedVisibleIdx + state.quickSearchScrollOffset;
                    if (clickedVisibleIdx >= 0 && clickedVisibleIdx < Math.min(displayList.size() - state.quickSearchScrollOffset, BlueprintState.MAX_QUICK_SEARCH_VISIBLE)) {
                        state.quickSearchSelectedIndex = clickedIdx;
                        if (state.quickSearchEditBox.getValue().isEmpty()) {
                            state.isMouseDown = true;
                        } else {
                            state.jumpToNode(displayList.get(clickedIdx), screen.width, screen.height);
                            state.showQuickSearch = false;
                        }
                        return true;
                    }
                }
            }
            // Clicked outside, close search
            state.showQuickSearch = false;
            return true;
        }

        // 0.1 Marker Editing interactions
        if (state.editingMarkerNode != null) {
            if (state.markerEditBox != null) {
                // If clicked outside the node, finish editing
                double worldMouseX = (mouseX - state.panX) / state.zoom;
                double worldMouseY = (mouseY - state.panY) / state.zoom;
                GuiNode node = state.editingMarkerNode;
                if (!(worldMouseX >= node.x && worldMouseX <= node.x + node.width && worldMouseY >= node.y && worldMouseY <= node.y + node.height)) {
                    finishMarkerEditing();
                } else {
                    return true; // Clicked inside, keep focus
                }
            }
        }

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
            if (menuHandler.mouseClicked(event, screen.width, screen.height)) return true;
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
                if (nodeHandler.mouseClicked(event, isDouble, worldMouseX, worldMouseY, font, screen)) return true;
            }
        }

        return false;
    }

    public boolean mouseReleased(MouseButtonEvent event, BlueprintScreen screen) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.buttonInfo().button();

        if (state.showQuickSearch) {
            state.isMouseDown = false;
        }

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
        if (nodeHandler.mouseReleased(event)) return true;

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
        if (nodeHandler.mouseDragged(worldMouseX, worldMouseY, mouseX, mouseY)) return true;

        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY, BlueprintScreen screen) {
        if (state.showQuickSearch) {
            List<GuiNode> currentList = state.quickSearchEditBox.getValue().isEmpty() ? state.searchHistory : state.quickSearchMatches;
            if (!currentList.isEmpty() && currentList.size() > BlueprintState.MAX_QUICK_SEARCH_VISIBLE) {
                state.quickSearchScrollOffset = Math.max(0, Math.min(currentList.size() - BlueprintState.MAX_QUICK_SEARCH_VISIBLE, state.quickSearchScrollOffset - (int) scrollY));
                return true;
            }
        }
        if (menuHandler.mouseScrolled(mouseX, mouseY, screen.width, screen.height, scrollY)) return true;
        return viewHandler.mouseScrolled(mouseX, mouseY, scrollY);
    }

    private void ensureQuickSearchSelectionVisible() {
        List<GuiNode> currentList = state.quickSearchEditBox.getValue().isEmpty() ? state.searchHistory : state.quickSearchMatches;
        if (currentList.isEmpty()) return;
        
        if (state.quickSearchSelectedIndex < state.quickSearchScrollOffset) {
            state.quickSearchScrollOffset = state.quickSearchSelectedIndex;
        } else if (state.quickSearchSelectedIndex >= state.quickSearchScrollOffset + BlueprintState.MAX_QUICK_SEARCH_VISIBLE) {
            state.quickSearchScrollOffset = state.quickSearchSelectedIndex - BlueprintState.MAX_QUICK_SEARCH_VISIBLE + 1;
        }
    }

    private void finishMarkerEditing() {
        if (state.editingMarkerNode != null && state.markerEditBox != null) {
            String newVal = state.markerEditBox.getValue();
            String oldVal = state.editingMarkerNode.inputValues.has(ltd.opens.mg.mc.core.blueprint.NodePorts.COMMENT) ? 
                             state.editingMarkerNode.inputValues.get(ltd.opens.mg.mc.core.blueprint.NodePorts.COMMENT).getAsString() : "";
            
            if (!newVal.equals(oldVal)) {
                state.pushHistory();
                state.editingMarkerNode.inputValues.addProperty(ltd.opens.mg.mc.core.blueprint.NodePorts.COMMENT, newVal);
                state.editingMarkerNode.setSizeDirty(true);
                state.markDirty();
            }
            state.editingMarkerNode = null;
            state.markerEditBox.setFocused(false);
        }
    }

    public boolean keyReleased(KeyEvent event, BlueprintScreen screen) {
        if (event.key() == GLFW.GLFW_KEY_ENTER) {
            state.isEnterDown = false;
        }
        return false;
    }

    public boolean keyPressed(KeyEvent event, BlueprintScreen screen) {
        if (event.key() == GLFW.GLFW_KEY_M) {
            state.showMinimap = !state.showMinimap;
            return true;
        }
        
        // Ctrl + P: Quick Search Markers
        if (event.key() == GLFW.GLFW_KEY_P && (event.modifiers() & GLFW.GLFW_MOD_CONTROL) != 0) {
            state.showQuickSearch = !state.showQuickSearch;
            if (state.showQuickSearch) {
                if (state.quickSearchEditBox == null) {
                    state.quickSearchEditBox = new EditBox(screen.getFont(), 0, 0, 180, 12, Component.empty());
                    state.quickSearchEditBox.setBordered(false);
                    state.quickSearchEditBox.setMaxLength(100);
                    state.quickSearchEditBox.setTextColor(0xFFFFFFFF);
                }
                state.quickSearchEditBox.setValue("");
                state.quickSearchEditBox.setFocused(true);
                state.updateQuickSearchMatches();
                state.showNodeMenu = false;
                state.showNodeContextMenu = false;
            }
            return true;
        }

        if (state.showQuickSearch) {
            if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
                state.showQuickSearch = false;
                return true;
            }
            
            if (state.quickSearchEditBox != null) {
                if (event.key() == GLFW.GLFW_KEY_ENTER) {
                    if (state.quickSearchEditBox.getValue().isEmpty()) {
                        state.isEnterDown = true;
                    } else {
                        List<GuiNode> currentList = state.quickSearchMatches;
                        if (state.quickSearchSelectedIndex >= 0 && state.quickSearchSelectedIndex < currentList.size()) {
                            state.jumpToNode(currentList.get(state.quickSearchSelectedIndex), screen.width, screen.height);
                            state.showQuickSearch = false;
                        }
                    }
                    return true;
                }
                
                if (event.key() == GLFW.GLFW_KEY_UP) {
                    List<GuiNode> currentList = state.quickSearchEditBox.getValue().isEmpty() ? state.searchHistory : state.quickSearchMatches;
                    if (!currentList.isEmpty()) {
                        state.quickSearchSelectedIndex = (state.quickSearchSelectedIndex - 1 + currentList.size()) % currentList.size();
                        state.searchConfirmProgress = 0f; // Reset on selection change
                        ensureQuickSearchSelectionVisible();
                    }
                    return true;
                }
                
                if (event.key() == GLFW.GLFW_KEY_DOWN) {
                    List<GuiNode> currentList = state.quickSearchEditBox.getValue().isEmpty() ? state.searchHistory : state.quickSearchMatches;
                    if (!currentList.isEmpty()) {
                        state.quickSearchSelectedIndex = (state.quickSearchSelectedIndex + 1) % currentList.size();
                        state.searchConfirmProgress = 0f; // Reset on selection change
                        ensureQuickSearchSelectionVisible();
                    }
                    return true;
                }
                
                String oldVal = state.quickSearchEditBox.getValue();
                if (state.quickSearchEditBox.keyPressed(event)) {
                    if (!state.quickSearchEditBox.getValue().equals(oldVal)) {
                        state.updateQuickSearchMatches();
                    }
                    return true;
                }
            }
            return true; // Block other keys when search is open
        }

        // Marker Editing keys
        if (state.editingMarkerNode != null && state.markerEditBox != null) {
            if (event.key() == GLFW.GLFW_KEY_ESCAPE || event.key() == GLFW.GLFW_KEY_ENTER) {
                finishMarkerEditing();
                return true;
            }
            if (state.markerEditBox.keyPressed(event)) {
                // Real-time size update
                state.editingMarkerNode.inputValues.addProperty(ltd.opens.mg.mc.core.blueprint.NodePorts.COMMENT, state.markerEditBox.getValue());
                state.editingMarkerNode.setSizeDirty(true);
                return true;
            }
            return true;
        }

        if (state.readOnly) return false;
        if (menuHandler.keyPressed(event)) return true;
        return nodeHandler.keyPressed(event);
    }

    public boolean charTyped(CharacterEvent event) {
        if (state.showQuickSearch) {
            if (state.quickSearchEditBox != null) {
                String oldVal = state.quickSearchEditBox.getValue();
                boolean handled = state.quickSearchEditBox.charTyped(event);
                if (handled && !state.quickSearchEditBox.getValue().equals(oldVal)) {
                    state.updateQuickSearchMatches();
                }
                return handled;
            }
            return true;
        }

        if (state.editingMarkerNode != null && state.markerEditBox != null) {
            boolean handled = state.markerEditBox.charTyped(event);
            if (handled) {
                state.editingMarkerNode.inputValues.addProperty(ltd.opens.mg.mc.core.blueprint.NodePorts.COMMENT, state.markerEditBox.getValue());
                state.editingMarkerNode.setSizeDirty(true);
            }
            return handled;
        }
        if (state.readOnly) return false;
        return menuHandler.charTyped(event);
    }
}


