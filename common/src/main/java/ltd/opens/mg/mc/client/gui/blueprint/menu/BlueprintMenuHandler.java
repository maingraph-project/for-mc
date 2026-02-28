package ltd.opens.mg.mc.client.gui.blueprint.menu;

import ltd.opens.mg.mc.client.utils.BlueprintMathHelper;
import ltd.opens.mg.mc.client.gui.blueprint.BlueprintState;

import ltd.opens.mg.mc.client.gui.components.GuiRegion;
import ltd.opens.mg.mc.client.gui.components.GuiNode;
import ltd.opens.mg.mc.client.gui.components.GuiConnection;
import ltd.opens.mg.mc.client.gui.components.GuiContextMenu;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import ltd.opens.mg.mc.client.gui.screens.InputModalScreen;
import ltd.opens.mg.mc.MaingraphforMC;
import net.minecraft.client.gui.screens.Screen;

import java.util.ArrayList;
import java.util.List;

public class BlueprintMenuHandler {
    private final BlueprintState state;

    public BlueprintMenuHandler(BlueprintState state) {
        this.state = state;
    }

    public boolean mouseClicked(MouseButtonEvent event, int screenWidth, int screenHeight) {
        if (state.contextMenu.isVisible()) {
            if (state.contextMenu.mouseClicked(event.x(), event.y(), event.buttonInfo().button())) {
                return true;
            }
            state.contextMenu.hide();
            return true;
        }

        if (state.showNodeMenu) {
            NodeDefinition def = state.menu.onClickNodeMenu(event, state.menuX, state.menuY, screenWidth, screenHeight);
            if (def != null) {
                createNodeAtMenu(def);
                return true;
            }

            if (!state.menu.isClickInsideNodeMenu(event.x(), event.y(), state.menuX, state.menuY, screenWidth, screenHeight)) {
                state.showNodeMenu = false;
                state.menu.reset();
                state.pendingConnectionSourceNode = null;
                state.pendingConnectionSourcePort = null;
                state.pendingConnectionSourceType = null;
                state.pendingConnectionSourceCustomTypeId = null;
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
                        if (canConnect(sourcePort.type, sourcePort.customTypeId, targetPort.type, targetPort.customTypeId)) {
                            state.connections.add(new GuiConnection(node, targetPort.id, state.pendingConnectionSourceNode, state.pendingConnectionSourcePort));
                            break;
                        }
                    }
                } else {
                    // Dragged from output, need input on new node
                    for (GuiNode.NodePort targetPort : node.inputs) {
                        if (canConnect(sourcePort.type, sourcePort.customTypeId, targetPort.type, targetPort.customTypeId)) {
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
            state.pendingConnectionSourceCustomTypeId = null;
        }

        state.markDirty();
        state.showNodeMenu = false;
        state.menu.reset();
    }

    private boolean canConnect(ltd.opens.mg.mc.core.blueprint.NodeDefinition.PortType type1, String customTypeId1, ltd.opens.mg.mc.core.blueprint.NodeDefinition.PortType type2, String customTypeId2) {
        if (type1 == ltd.opens.mg.mc.core.blueprint.NodeDefinition.PortType.EXEC || type2 == ltd.opens.mg.mc.core.blueprint.NodeDefinition.PortType.EXEC) {
            return type1 == type2;
        }
        boolean hasCustom1 = customTypeId1 != null && !customTypeId1.isEmpty();
        boolean hasCustom2 = customTypeId2 != null && !customTypeId2.isEmpty();
        if (hasCustom1 || hasCustom2) {
            if (hasCustom1 && hasCustom2) {
                return customTypeId1.equals(customTypeId2);
            }
            return (hasCustom1 && type2 == ltd.opens.mg.mc.core.blueprint.NodeDefinition.PortType.ANY) || (hasCustom2 && type1 == ltd.opens.mg.mc.core.blueprint.NodeDefinition.PortType.ANY);
        }
        if (type1 == ltd.opens.mg.mc.core.blueprint.NodeDefinition.PortType.ANY || type2 == ltd.opens.mg.mc.core.blueprint.NodeDefinition.PortType.ANY) {
            return true;
        }
        return type1 == type2;
    }

    public boolean keyPressed(KeyEvent event) {
        if (state.showNodeMenu) {
            int keyCode = event.key();
            if (keyCode == 257 || keyCode == 335) { // Enter or Numpad Enter
                NodeDefinition def = state.menu.getSelectedNode();
                if (def != null) {
                    createNodeAtMenu(def);
                    return true;
                }
            }
            return state.menu.keyPressed(event);
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

    public boolean charTyped(CharacterEvent event) {
        if (state.showNodeMenu) {
            return state.menu.charTyped(event);
        }
        return false;
    }

    public boolean mouseReleased(MouseButtonEvent event, int screenWidth, int screenHeight) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.buttonInfo().button();
        if (button == 1) { // Right click
            double worldMouseX = state.viewport.toWorldX(mouseX);
            double worldMouseY = state.viewport.toWorldY(mouseY);
            
            // 1. Check Nodes
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

                    // Check if node is in a region
                    for (GuiRegion region : state.regions) {
                        float cx = node.x + node.width / 2;
                        float cy = node.y + node.height / 2;
                        if (cx >= region.x && cx <= region.x + region.width && cy >= region.y && cy <= region.y + region.height) {
                            items.add(new GuiContextMenu.MenuItem(
                                Component.translatable("gui.mgmc.blueprint_editor.context_menu.auto_layout"),
                                () -> {
                                    state.layoutManager.autoLayoutRegion(region);
                                }
                            ));
                            break; // Only one region per node usually
                        }
                    }

                    state.contextMenu.show(mouseX, mouseY, items);
                    return true;
                }
            }

            // 2. Check Regions
            for (int i = state.regions.size() - 1; i >= 0; i--) {
                GuiRegion region = state.regions.get(i);
                // Check if mouse is on the title bar (height 20)
                if (worldMouseX >= region.x && worldMouseX <= region.x + region.width && worldMouseY >= region.y && worldMouseY <= region.y + 20) {
                    state.contextMenuRegion = region;
                    List<GuiContextMenu.MenuItem> items = new ArrayList<>();
                    
                    // Rename
                    items.add(new GuiContextMenu.MenuItem(
                        Component.translatable("gui.mgmc.blueprint_editor.context_menu.rename"),
                        () -> {
                            if (state.contextMenuRegion != null) {
                                Minecraft.getInstance().setScreen(new InputModalScreen(
                                    Minecraft.getInstance().screen,
                                    Component.translatable("gui.mgmc.blueprint_editor.region.rename_title").getString(),
                                    state.contextMenuRegion.title,
                                    false,
                                    (newName) -> {
                                        state.pushHistory();
                                        state.contextMenuRegion.title = newName;
                                        state.markDirty();
                                    }
                                ));
                            }
                        }
                    ));

                    // Change Color
                    items.add(new GuiContextMenu.MenuItem(
                        Component.translatable("gui.mgmc.blueprint_editor.context_menu.color"),
                        () -> {
                            if (state.contextMenuRegion != null) {
                                // Show preset colors + Custom
                                String[] options = new String[] {
                                    "Default (Gray)", "Red", "Green", "Blue", "Yellow", "Purple", "Cyan", "Custom..."
                                };
                                
                                Minecraft.getInstance().setScreen(new InputModalScreen(
                                    Minecraft.getInstance().screen,
                                    Component.translatable("gui.mgmc.blueprint_editor.region.color_select_title").getString(),
                                    "Default (Gray)", // Initial selection
                                    false,
                                    options,
                                    InputModalScreen.Mode.SELECTION,
                                    (selected) -> {
                                        int newColorInt = 0;
                                        boolean isCustom = false;
                                        
                                        if (selected.equals("Default (Gray)")) newColorInt = 0x44888888;
                                        else if (selected.equals("Red")) newColorInt = 0x44FF0000;
                                        else if (selected.equals("Green")) newColorInt = 0x4400FF00;
                                        else if (selected.equals("Blue")) newColorInt = 0x440000FF;
                                        else if (selected.equals("Yellow")) newColorInt = 0x44FFFF00;
                                        else if (selected.equals("Purple")) newColorInt = 0x44800080;
                                        else if (selected.equals("Cyan")) newColorInt = 0x4400FFFF;
                                        else if (selected.equals("Custom...")) isCustom = true;
                                        
                                        if (isCustom) {
                                            // Show Hex Input
                                            Minecraft.getInstance().setScreen(new InputModalScreen(
                                                Minecraft.getInstance().screen,
                                                Component.translatable("gui.mgmc.blueprint_editor.region.color_title").getString(),
                                                String.format("#%06X", (state.contextMenuRegion.color & 0xFFFFFF)),
                                                false,
                                                (hexColor) -> {
                                                    try {
                                                        String hex = hexColor.replace("#", "");
                                                        int rgb = Integer.parseInt(hex, 16);
                                                        state.pushHistory();
                                                        state.contextMenuRegion.color = (0x44 << 24) | rgb;
                                                        state.markDirty();
                                                    } catch (NumberFormatException e) {
                                                        MaingraphforMC.LOGGER.warn("Invalid hex color input: " + hexColor, e);
                                                    }
                                                }
                                            ));
                                        } else {
                                            state.pushHistory();
                                            state.contextMenuRegion.color = newColorInt;
                                            state.markDirty();
                                            // Return to blueprint screen (InputModalScreen handles this via onClose usually, but we are in callback)
                                            Minecraft.getInstance().setScreen(Minecraft.getInstance().screen); // Close modal
                                        }
                                    }
                                ));
                            }
                        }
                    ));

                    // Delete
                    items.add(new GuiContextMenu.MenuItem(
                        Component.translatable("gui.mgmc.blueprint_editor.context_menu.delete"),
                        () -> {
                            if (state.contextMenuRegion != null) {
                                state.pushHistory();
                                state.regions.remove(state.contextMenuRegion);
                                state.markDirty();
                            }
                        }
                    ));

                    // Auto Layout Region
                    items.add(new GuiContextMenu.MenuItem(
                        Component.translatable("gui.mgmc.blueprint_editor.context_menu.auto_layout"),
                        () -> {
                            if (state.contextMenuRegion != null) {
                                state.layoutManager.autoLayoutRegion(state.contextMenuRegion);
                            }
                        }
                    ));
                    
                    state.contextMenu.show(mouseX, mouseY, items);
                    return true;
                }
            }

            // 3. Check Connections
            GuiConnection hoveredConn = BlueprintMathHelper.getHoveredConnection(worldMouseX, worldMouseY, state);
            if (hoveredConn != null) {
                state.pushHistory();
                state.connections.remove(hoveredConn);
                state.markDirty();
                return true;
            }
            
            // 4. Empty Space - Show Add Node / Add Region menu
            if (Screen.hasControlDown()) {
                // Ctrl + Right Click -> Add Region Directly
                state.pushHistory();
                double rX = state.viewport.toWorldX(mouseX);
                double rY = state.viewport.toWorldY(mouseY);
                state.regions.add(new GuiRegion((float)rX, (float)rY, 300, 200));
                state.markDirty();
                return true;
            } else {
                // Right Click -> Add Node (Original behavior)
                state.showNodeMenu = true;
                state.menuX = mouseX;
                state.menuY = mouseY;
                state.menu.reset();
                
                // Clear pending connection context
                state.pendingConnectionSourceNode = null;
                state.pendingConnectionSourcePort = null;
                state.pendingConnectionSourceType = null;
                state.pendingConnectionSourceCustomTypeId = null;
                return true;
            }
        }
        return false;
    }
}


