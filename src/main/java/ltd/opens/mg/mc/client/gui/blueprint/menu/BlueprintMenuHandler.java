package ltd.opens.mg.mc.client.gui.blueprint.menu;

import ltd.opens.mg.mc.client.gui.blueprint.BlueprintState;

import ltd.opens.mg.mc.client.gui.components.*;
import net.minecraft.client.input.CharacterEvent;
import ltd.opens.mg.mc.core.blueprint.NodeDefinition;

public class BlueprintMenuHandler {
    private final BlueprintState state;

    public BlueprintMenuHandler(BlueprintState state) {
        this.state = state;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int screenWidth, int screenHeight) {
        if (state.showNodeContextMenu) {
            BlueprintMenu.ContextMenuResult result = state.menu.onClickContextMenu(mouseX, mouseY, state.menuX, state.menuY);
            if (result == BlueprintMenu.ContextMenuResult.DELETE) {
                if (state.contextMenuNode != null) {
                    if (state.focusedNode == state.contextMenuNode) {
                        state.focusedNode = null;
                        state.focusedPort = null;
                    }
                    final GuiNode finalNode = state.contextMenuNode;
                    state.nodes.remove(state.contextMenuNode);
                    state.connections.removeIf(c -> c.from == finalNode || c.to == finalNode);
                    state.markDirty();
                }
                state.showNodeContextMenu = false;
                state.contextMenuNode = null;
                return true;
            } else if (result == BlueprintMenu.ContextMenuResult.BREAK_LINKS) {
                if (state.contextMenuNode != null) {
                    final GuiNode finalNode = state.contextMenuNode;
                    state.connections.removeIf(c -> c.from == finalNode || c.to == finalNode);
                    state.markDirty();
                }
                state.showNodeContextMenu = false;
                state.contextMenuNode = null;
                return true;
            }
            
            state.showNodeContextMenu = false;
            state.contextMenuNode = null;
            return true;
        }

        if (state.showNodeMenu) {
            NodeDefinition def = state.menu.onClickNodeMenu(mouseX, mouseY, state.menuX, state.menuY, screenWidth, screenHeight);
            if (def != null) {
                createNodeAtMenu(def);
                return true;
            }

            if (!state.menu.isClickInsideNodeMenu(mouseX, mouseY, state.menuX, state.menuY, screenWidth, screenHeight)) {
                state.showNodeMenu = false;
                state.menu.reset();
            }
            return true;
        }

        return false;
    }

    private void createNodeAtMenu(NodeDefinition def) {
        float worldX = (float) ((state.menuX - state.panX) / state.zoom);
        float worldY = (float) ((state.menuY - state.panY) / state.zoom);
        GuiNode node = new GuiNode(def, worldX, worldY);
        state.nodes.add(node);
        state.markDirty();
        state.showNodeMenu = false;
        state.menu.reset();
    }

    public boolean keyPressed(int key) {
        if (state.showNodeMenu) {
            if (key == 257) { // Enter
                NodeDefinition def = state.menu.getSelectedNode();
                if (def != null) {
                    createNodeAtMenu(def);
                    return true;
                }
            }
            return state.menu.keyPressed(key);
        }
        return false;
    }

    public boolean charTyped(CharacterEvent event) {
        if (state.showNodeMenu) {
            return state.menu.charTyped((char) event.codepoint());
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, int screenWidth, int screenHeight, double amount) {
        if (state.showNodeMenu) {
            state.menu.mouseScrolled(mouseX, mouseY, state.menuX, state.menuY, screenWidth, screenHeight, amount);
            return true;
        }
        return false;
    }

    public boolean mouseReleased(double mouseX, double mouseY, int button, int screenWidth, int screenHeight) {
        if (button == 1) { // Right click
            double worldMouseX = (mouseX - state.panX) / state.zoom;
            double worldMouseY = (mouseY - state.panY) / state.zoom;
            
            for (int i = state.nodes.size() - 1; i >= 0; i--) {
                GuiNode node = state.nodes.get(i);
                if (worldMouseX >= node.x && worldMouseX <= node.x + node.width && worldMouseY >= node.y && worldMouseY <= node.y + node.height) {
                    state.showNodeContextMenu = true;
                    state.contextMenuNode = node;
                    state.menuX = mouseX;
                    state.menuY = mouseY;
                    return true;
                }
            }
            
            state.showNodeMenu = true;
            state.menuX = mouseX;
            state.menuY = mouseY;
            state.menu.reset(); // Reset search when opening
            return true;
        }
        return false;
    }
}


