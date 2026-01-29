package ltd.opens.mg.mc.client.gui.blueprint.menu;

import ltd.opens.mg.mc.client.utils.BlueprintMathHelper;
import ltd.opens.mg.mc.client.gui.blueprint.BlueprintState;

import ltd.opens.mg.mc.client.gui.components.*;
import ltd.opens.mg.mc.client.gui.components.GuiContextMenu;
import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

public class BlueprintMenuHandler {
    private final BlueprintState state;

    public BlueprintMenuHandler(BlueprintState state) {
        this.state = state;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, int screenWidth, int screenHeight) {
        if (state.contextMenu.isVisible()) {
            if (state.contextMenu.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
            state.contextMenu.hide();
            return true;
        }

        if (state.showNodeMenu) {
            NodeDefinition def = state.menu.onClickNodeMenu(mouseX, mouseY, button, state.menuX, state.menuY, screenWidth, screenHeight);
            if (def != null) {
                createNodeAtMenu(def);
                return true;
            }

            if (!state.menu.isClickInsideNodeMenu(mouseX, mouseY, state.menuX, state.menuY, screenWidth, screenHeight)) {
                state.showNodeMenu = false;
                state.menu.reset();
                state.pendingConnectionSourceNode = null;
                state.pendingConnectionSourcePort = null;
                state.pendingConnectionSourceType = null;
            }
            return true;
        }

        return false;
    }

    private void createNodeAtMenu(NodeDefinition def) {
        state.pushHistory();
        float worldX = state.viewport.toWorldX(state.menuX);
        float worldY = state.viewport.toWorldY(state.menuY);
        GuiNode node = new GuiNode(def, worldX, worldY);
        state.nodes.add(node);
        
        // Handle pending connection (UE style)
        if (state.pendingConnectionSourceNode != null) {
            GuiNode.NodePort sourcePort = state.pendingConnectionSourceNode.getPortByName(state.pendingConnectionSourcePort, state.pendingConnectionFromInput);
            if (sourcePort != null) {
                // Find a compatible port on the new node
                if (state.pendingConnectionFromInput) {
                    // Dragged from input, need output on new node
                    for (GuiNode.NodePort targetPort : node.outputs) {
                        if (canConnect(sourcePort.type, targetPort.type)) {
                            state.connections.add(new GuiConnection(node, targetPort.id, state.pendingConnectionSourceNode, state.pendingConnectionSourcePort));
                            break;
                        }
                    }
                } else {
                    // Dragged from output, need input on new node
                    for (GuiNode.NodePort targetPort : node.inputs) {
                        if (canConnect(sourcePort.type, targetPort.type)) {
                            state.connections.add(new GuiConnection(state.pendingConnectionSourceNode, state.pendingConnectionSourcePort, node, targetPort.id));
                            break;
                        }
                    }
                }
            }
            // Clear context
            state.pendingConnectionSourceNode = null;
            state.pendingConnectionSourcePort = null;
            state.pendingConnectionSourceType = null;
        }

        state.markDirty();
        state.showNodeMenu = false;
        state.menu.reset();
    }

    private boolean canConnect(ltd.opens.mg.mc.core.blueprint.NodeDefinition.PortType type1, ltd.opens.mg.mc.core.blueprint.NodeDefinition.PortType type2) {
        if (type1 == ltd.opens.mg.mc.core.blueprint.NodeDefinition.PortType.EXEC || type2 == ltd.opens.mg.mc.core.blueprint.NodeDefinition.PortType.EXEC) {
            return type1 == type2;
        }
        if (type1 == ltd.opens.mg.mc.core.blueprint.NodeDefinition.PortType.ANY || type2 == ltd.opens.mg.mc.core.blueprint.NodeDefinition.PortType.ANY) {
            return true;
        }
        return type1 == type2;
    }

    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (state.showNodeMenu) {
            if (keyCode == 257 || keyCode == 335) { // Enter or Numpad Enter
                NodeDefinition def = state.menu.getSelectedNode();
                if (def != null) {
                    createNodeAtMenu(def);
                    return true;
                }
            }
            return state.menu.keyPressed(keyCode, scanCode, modifiers);
        }
        return false;
    }

    public boolean charTyped(char codePoint, int modifiers) {
        if (state.showNodeMenu) {
            return state.menu.charTyped(codePoint, modifiers);
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
            double worldMouseX = state.viewport.toWorldX(mouseX);
            double worldMouseY = state.viewport.toWorldY(mouseY);
            
            for (int i = state.nodes.size() - 1; i >= 0; i--) {
                GuiNode node = state.nodes.get(i);
                if (worldMouseX >= node.x && worldMouseX <= node.x + node.width && worldMouseY >= node.y && worldMouseY <= node.y + node.height) {
                    state.contextMenuNode = node;
                    List<GuiContextMenu.MenuItem> items = new ArrayList<>();
                    items.add(new GuiContextMenu.MenuItem(
                        Component.translatable("gui.mgmc.blueprint_editor.context_menu.delete"),
                        () -> {
                            if (state.contextMenuNode != null) {
                                state.pushHistory();
                                if (state.focusedNode == state.contextMenuNode) {
                                    state.focusedNode = null;
                                    state.focusedPort = null;
                                }
                                final GuiNode finalNode = state.contextMenuNode;
                                state.nodes.remove(state.contextMenuNode);
                                state.connections.removeIf(c -> c.from == finalNode || c.to == finalNode);
                                state.markDirty();
                            }
                        }
                    ));
                    items.add(new GuiContextMenu.MenuItem(
                        Component.translatable("gui.mgmc.blueprint_editor.context_menu.break_links"),
                        () -> {
                            if (state.contextMenuNode != null) {
                                state.pushHistory();
                                final GuiNode finalNode = state.contextMenuNode;
                                state.connections.removeIf(c -> c.from == finalNode || c.to == finalNode);
                                state.markDirty();
                            }
                        }
                    ));
                    state.contextMenu.show(mouseX, mouseY, items);
                    return true;
                }
            }

            // Check if right-clicked a connection
            GuiConnection hoveredConn = BlueprintMathHelper.getHoveredConnection(worldMouseX, worldMouseY, state);
            if (hoveredConn != null) {
                state.pushHistory();
                state.connections.remove(hoveredConn);
                state.markDirty();
                return true;
            }
            
            state.showNodeMenu = true;
            state.menuX = mouseX;
            state.menuY = mouseY;
            state.menu.reset(); // Reset search when opening
            
            // Clear pending connection context when opening menu normally via right-click
            state.pendingConnectionSourceNode = null;
            state.pendingConnectionSourcePort = null;
            state.pendingConnectionSourceType = null;
            
            return true;
        }
        return false;
    }
}


