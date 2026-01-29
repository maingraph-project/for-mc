package ltd.opens.mg.mc.client.gui.blueprint.handler;

import ltd.opens.mg.mc.client.gui.blueprint.BlueprintState;
import ltd.opens.mg.mc.client.gui.blueprint.menu.*;
import ltd.opens.mg.mc.client.gui.components.GuiNode;

import java.util.List;


import ltd.opens.mg.mc.client.gui.screens.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
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

    public boolean mouseClicked(double mouseX, double mouseY, int button, boolean isDouble, Font font, BlueprintScreen screen) {
        // 0. Quick Search interactions
        if (state.showQuickSearch) {
            if (state.quickSearchEditBox != null) {
                if (state.quickSearchEditBox.mouseClicked(mouseX, mouseY, button)) {
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
                double worldMouseX = state.viewport.toWorldX(mouseX);
                double worldMouseY = state.viewport.toWorldY(mouseY);
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
            if (menuHandler.mouseClicked(mouseX, mouseY, button, screen.width, screen.height)) return true;
        }

        // 2. View interactions (panning start)
        if (viewHandler.mouseClicked(mouseX, mouseY, button)) return true;

        // World coordinates for other interactions
        double worldMouseX = state.viewport.toWorldX(mouseX);
        double worldMouseY = state.viewport.toWorldY(mouseY);

        if (button == 0) { // Left click
            state.focusedNode = null;
            state.focusedPort = null;
            
            if (!state.readOnly) {
                // 3. Connection interactions (port click start)
                if (connectionHandler.mouseClicked(worldMouseX, worldMouseY)) return true;

                // 4. Node interactions (input box or header drag start)
                if (nodeHandler.mouseClicked(mouseX, mouseY, button, isDouble, worldMouseX, worldMouseY, font, screen)) return true;
            }
        }

        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button, BlueprintScreen screen) {
        if (state.showQuickSearch) {
            state.isMouseDown = false;
        }

        // 1. View interactions (panning end)
        if (viewHandler.mouseReleased(mouseX, mouseY, button)) return true;

        if (state.readOnly) return false;

        // 2. Menu interactions (open context menu on right click release)
        if (menuHandler.mouseReleased(mouseX, mouseY, button, screen.width, screen.height)) return true;

        // World coordinates for other interactions
        double worldMouseX = state.viewport.toWorldX(mouseX);
        double worldMouseY = state.viewport.toWorldY(mouseY);

        // 2. Connection interactions (link creation)
        if (connectionHandler.mouseReleased(worldMouseX, worldMouseY)) return true;

        // 3. Node interactions (drag end)
        if (nodeHandler.mouseReleased(mouseX, mouseY, button)) return true;

        return false;
    }

    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        // 1. View interactions (panning drag)
        if (viewHandler.mouseDragged(mouseX, mouseY)) return true;

        if (state.readOnly) return false;

        // World coordinates for other interactions
        double worldMouseX = state.viewport.toWorldX(mouseX);
        double worldMouseY = state.viewport.toWorldY(mouseY);

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

    public boolean keyReleased(int keyCode, int scanCode, int modifiers, BlueprintScreen screen) {
        if (keyCode == GLFW.GLFW_KEY_ENTER) {
            state.isEnterDown = false;
        }
        if (keyCode == GLFW.GLFW_KEY_W) {
            state.isWDown = false;
        }
        return false;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers, BlueprintScreen screen) {
        if (keyCode == GLFW.GLFW_KEY_W && !state.showQuickSearch && state.editingMarkerNode == null) {
            state.isWDown = true;
            // No return true here to allow other controls (like WASD panning if implemented) 
            // but currently W is not used for panning in this editor.
        }
        if (keyCode == GLFW.GLFW_KEY_M) {
            state.showMinimap = !state.showMinimap;
            return true;
        }
        
        // Ctrl + P: Quick Search Markers
        if (keyCode == GLFW.GLFW_KEY_P && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
            state.showQuickSearch = !state.showQuickSearch;
            if (state.showQuickSearch) {
                if (state.quickSearchEditBox == null) {
                    state.quickSearchEditBox = new EditBox(Minecraft.getInstance().font, 0, 0, 180, 12, Component.empty());
                    state.quickSearchEditBox.setBordered(false);
                    state.quickSearchEditBox.setMaxLength(100);
                    state.quickSearchEditBox.setTextColor(0xFFFFFFFF);
                }
                state.quickSearchEditBox.setValue("");
                state.quickSearchEditBox.setFocused(true);
                state.updateQuickSearchMatches();
                state.showNodeMenu = false;
                state.contextMenu.hide();
            }
            return true;
        }

        if (state.showQuickSearch) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                state.showQuickSearch = false;
                return true;
            }
            
            if (state.quickSearchEditBox != null) {
                if (keyCode == GLFW.GLFW_KEY_ENTER) {
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
                
                if (keyCode == GLFW.GLFW_KEY_UP) {
                    List<GuiNode> currentList = state.quickSearchEditBox.getValue().isEmpty() ? state.searchHistory : state.quickSearchMatches;
                    if (!currentList.isEmpty()) {
                        state.quickSearchSelectedIndex = (state.quickSearchSelectedIndex - 1 + currentList.size()) % currentList.size();
                        state.searchConfirmProgress = 0f; // Reset on selection change
                        ensureQuickSearchSelectionVisible();
                    }
                    return true;
                }
                
                if (keyCode == GLFW.GLFW_KEY_DOWN) {
                    List<GuiNode> currentList = state.quickSearchEditBox.getValue().isEmpty() ? state.searchHistory : state.quickSearchMatches;
                    if (!currentList.isEmpty()) {
                        state.quickSearchSelectedIndex = (state.quickSearchSelectedIndex + 1) % currentList.size();
                        state.searchConfirmProgress = 0f; // Reset on selection change
                        ensureQuickSearchSelectionVisible();
                    }
                    return true;
                }
                
                String oldVal = state.quickSearchEditBox.getValue();
                if (state.quickSearchEditBox.keyPressed(keyCode, scanCode, modifiers)) {
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
            if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_ENTER) {
                finishMarkerEditing();
                return true;
            }
            return state.markerEditBox.keyPressed(keyCode, scanCode, modifiers);
        }

        if (state.readOnly) return false;
        return nodeHandler.keyPressed(keyCode, scanCode, modifiers);
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (state.showQuickSearch) {
            if (state.quickSearchEditBox != null) {
                String oldVal = state.quickSearchEditBox.getValue();
                boolean handled = state.quickSearchEditBox.charTyped(codePoint, modifiers);
                if (handled && !state.quickSearchEditBox.getValue().equals(oldVal)) {
                    state.updateQuickSearchMatches();
                }
                return handled;
            }
            return true;
        }

        if (state.editingMarkerNode != null && state.markerEditBox != null) {
            boolean handled = state.markerEditBox.charTyped(codePoint, modifiers);
            if (handled) {
                state.editingMarkerNode.inputValues.addProperty(ltd.opens.mg.mc.core.blueprint.NodePorts.COMMENT, state.markerEditBox.getValue());
                state.editingMarkerNode.setSizeDirty(true);
            }
            return handled;
        }
        if (state.readOnly) return false;
        return menuHandler.charTyped(codePoint, modifiers);
    }
}


