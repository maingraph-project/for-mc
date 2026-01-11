package ltd.opens.mg.mc.client.gui.blueprint.handler;

import ltd.opens.mg.mc.client.gui.blueprint.BlueprintState;


import ltd.opens.mg.mc.client.gui.screens.*;
import ltd.opens.mg.mc.client.gui.components.*;
import com.google.gson.JsonElement;
import ltd.opens.mg.mc.client.gui.blueprint.io.BlueprintIO;
import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class BlueprintNodeHandler {
    private final BlueprintState state;

    public BlueprintNodeHandler(BlueprintState state) {
        this.state = state;
    }

    public boolean mouseClicked(MouseButtonEvent event, double worldMouseX, double worldMouseY, Font font, BlueprintScreen screen) {
        boolean isShiftDown = event.hasShiftDown();
        boolean isCtrlDown = event.hasControlDown();

        // Check for remove port click
        for (GuiNode node : state.nodes) {
            String portId = node.getRemovePortAt(worldMouseX, worldMouseY, font);
            if (portId != null) {
                // Determine if it's an input or output port
                boolean isInput = node.getPortByName(portId, true) != null;
                if (isInput) {
                    node.inputs.removeIf(p -> p.id.equals(portId));
                    state.connections.removeIf(c -> c.to == node && c.toPort.equals(portId));
                } else {
                    node.outputs.removeIf(p -> p.id.equals(portId));
                    state.connections.removeIf(c -> c.from == node && c.fromPort.equals(portId));
                }
                state.markDirty();
                return true;
            }
        }

        // Check for input box click check first
        for (GuiNode node : state.nodes) {
            for (int i = 0; i < node.inputs.size(); i++) {
                GuiNode.NodePort port = node.inputs.get(i);
                float[] pos = node.getPortPosition(i, true);
                
                if (port.hasInput) {
                    float inputX = pos[0] + 8 + font.width(Component.translatable(port.displayName)) + 2;
                    float inputY = pos[1] - 4;
                    float inputWidth = 50;
                    float inputHeight = 10;
                    if (worldMouseX >= inputX && worldMouseX <= inputX + inputWidth && worldMouseY >= inputY && worldMouseY <= inputY + inputHeight) {
                        // Only allow editing if not connected
                        boolean isConnected = false;
                        for (GuiConnection conn : state.connections) {
                            if (conn.to == node && conn.toPort.equals(port.id)) {
                                isConnected = true;
                                break;
                            }
                        }
                        if (!isConnected) {
                            if (port.type == NodeDefinition.PortType.BOOLEAN) {
                                JsonElement val = node.inputValues.get(port.id);
                                boolean current = val != null ? val.getAsBoolean() : (port.defaultValue instanceof Boolean ? (Boolean) port.defaultValue : false);
                                boolean nextVal = !current;
                                node.inputValues.addProperty(port.id, nextVal);
                                state.markDirty();
                            } else if (port.options != null && port.options.length > 0) {
                                // Open selection modal instead of cycling
                                JsonElement val = node.inputValues.get(port.id);
                                String current = val != null ? val.getAsString() : (port.defaultValue != null ? port.defaultValue.toString() : port.options[0]);
                                
                                final GuiNode targetNode = node;
                                final String targetPort = port.id;
                                
                                Minecraft.getInstance().setScreen(new InputModalScreen(
                                    screen, 
                                    Component.translatable("gui.mgmc.blueprint_editor.modal.select_type").getString(), 
                                    current, 
                                    false, 
                                    port.options,
                                    InputModalScreen.Mode.SELECTION,
                                    (selected) -> {
                                        JsonElement oldVal = targetNode.inputValues.get(targetPort);
                                        String oldStr = oldVal != null ? oldVal.getAsString() : "";
                                        
                                        if (!selected.equals(oldStr)) {
                                            targetNode.inputValues.addProperty(targetPort, selected);
                                            state.markDirty();
                                            // Update output port type based on selection
                                            NodeDefinition.PortType newType = NodeDefinition.PortType.valueOf(selected.toUpperCase());
                                            targetNode.getPortByName("output", false).type = newType;
                                            
                                            // Disconnect all output connections since the type changed
                                            state.connections.removeIf(conn -> conn.from == targetNode && conn.fromPort.equals("output"));
                                        }
                                    }
                                ));
                            } else {
                                JsonElement val = node.inputValues.get(port.id);
                                String initialText = val != null ? val.getAsString() : (port.defaultValue != null ? port.defaultValue.toString() : "");
                                boolean isNumeric = (port.type == NodeDefinition.PortType.FLOAT);
                                
                                final GuiNode targetNode = node;
                                final String targetPort = port.id;
                                
                                Minecraft.getInstance().setScreen(new InputModalScreen(
                                    screen, 
                                    Component.translatable("gui.mgmc.blueprint_editor.modal.enter_value", Component.translatable(port.displayName)).getString(), 
                                    initialText, 
                                    isNumeric,
                                    (newText) -> {
                                        targetNode.inputValues.addProperty(targetPort, newText);
                                        state.markDirty();
                                    }
                                ));
                            }
                            return true;
                        }
                    }
                }
            }
        }

        // Check for node header click
        for (int i = state.nodes.size() - 1; i >= 0; i--) {
            GuiNode node = state.nodes.get(i);
            if (node.isMouseOverAddButton(worldMouseX, worldMouseY)) {
                String action = (String) node.definition.properties().get("ui_button_action");
                if ("add_output_modal".equals(action)) {
                    // Add new branch/output via modal
                    Minecraft.getInstance().setScreen(new InputModalScreen(
                        screen,
                        Component.translatable("gui.mgmc.modal.enter_value", Component.translatable(node.title)).getString(),
                        "",
                        false,
                        (newText) -> {
                             if (newText != null && !newText.isEmpty()) {
                                 if (node.getPortByName(newText, false) == null) {
                                     node.addOutput(newText, newText, NodeDefinition.PortType.EXEC, 0xFFFFFFFF);
                                     state.markDirty();
                                 }
                             }
                         }
                    ));
                } else if ("add_input_indexed".equals(action)) {
                    // Add new indexed input (like string_combine)
                    int maxIndex = -1;
                    for (GuiNode.NodePort port : node.inputs) {
                        if (port.id.startsWith("input_")) {
                            try {
                                int idx = Integer.parseInt(port.id.substring(6));
                                if (idx > maxIndex) maxIndex = idx;
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                    int nextIndex = maxIndex + 1;
                    String portId = "input_" + nextIndex;
                    String displayName = "input " + nextIndex;
                    node.addInput(portId, displayName, NodeDefinition.PortType.STRING, 0xFFBBBBBB, true, "", null);
                    state.markDirty();
                }
                return true;
            }
            if (node.isMouseOverHeader(worldMouseX, worldMouseY)) {
                if (isShiftDown || isCtrlDown) {
                    if (node.isSelected) {
                        node.isSelected = false;
                        state.selectedNodes.remove(node);
                    } else {
                        node.isSelected = true;
                        state.selectedNodes.add(node);
                    }
                } else {
                    if (!node.isSelected) {
                        // Clear previous selection if not clicking a selected node
                        for (GuiNode n : state.selectedNodes) n.isSelected = false;
                        state.selectedNodes.clear();
                        node.isSelected = true;
                        state.selectedNodes.add(node);
                    }
                }

                state.draggingNode = node;
                state.dragOffsetX = (float) (worldMouseX - node.x);
                state.dragOffsetY = (float) (worldMouseY - node.y);
                state.nodes.remove(i);
                state.nodes.add(node);
                return true;
            }
        }

        // Clicked empty space
        if (!isShiftDown && !isCtrlDown) {
            for (GuiNode n : state.selectedNodes) n.isSelected = false;
            state.selectedNodes.clear();
        }
        
        // Start box selection
        state.isBoxSelecting = true;
        state.boxSelectStartX = worldMouseX * state.zoom + state.panX;
        state.boxSelectStartY = worldMouseY * state.zoom + state.panY;
        state.boxSelectEndX = state.boxSelectStartX;
        state.boxSelectEndY = state.boxSelectStartY;

        return false;
    }

    public boolean mouseReleased(MouseButtonEvent event) {
        if (state.isBoxSelecting) {
            state.isBoxSelecting = false;
            
            double worldX1 = (state.boxSelectStartX - state.panX) / state.zoom;
            double worldY1 = (state.boxSelectStartY - state.panY) / state.zoom;
            double worldX2 = (state.boxSelectEndX - state.panX) / state.zoom;
            double worldY2 = (state.boxSelectEndY - state.panY) / state.zoom;
            
            double minX = Math.min(worldX1, worldX2);
            double minY = Math.min(worldY1, worldY2);
            double maxX = Math.max(worldX1, worldX2);
            double maxY = Math.max(worldY1, worldY2);
            
            boolean isShiftDown = event.hasShiftDown();
            boolean isCtrlDown = event.hasControlDown();

            if (!isShiftDown && !isCtrlDown) {
                for (GuiNode n : state.selectedNodes) n.isSelected = false;
                state.selectedNodes.clear();
            }

            for (GuiNode node : state.nodes) {
                if (node.x + node.width >= minX && node.x <= maxX && node.y + node.height >= minY && node.y <= maxY) {
                    if (!node.isSelected) {
                        node.isSelected = true;
                        state.selectedNodes.add(node);
                    }
                }
            }
            return true;
        }
        if (state.draggingNode != null) {
            state.draggingNode = null;
            return true;
        }
        return false;
    }

    public boolean mouseDragged(double worldMouseX, double worldMouseY, double mouseX, double mouseY) {
        if (state.isBoxSelecting) {
            state.boxSelectEndX = mouseX;
            state.boxSelectEndY = mouseY;
            return true;
        }
        if (state.draggingNode != null) {
            float dx = (float) (worldMouseX - state.dragOffsetX) - state.draggingNode.x;
            float dy = (float) (worldMouseY - state.dragOffsetY) - state.draggingNode.y;
            
            if (Math.abs(dx) > 0.1 || Math.abs(dy) > 0.1) {
                // Move all selected nodes
                for (GuiNode node : state.selectedNodes) {
                    node.x += dx;
                    node.y += dy;
                }
                // If dragging node is not in selection (should not happen with new logic but just in case)
                if (!state.selectedNodes.contains(state.draggingNode)) {
                    state.draggingNode.x += dx;
                    state.draggingNode.y += dy;
                }
                state.markDirty();
            }
            return true;
        }
        return false;
    }

    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        boolean isCtrlDown = event.hasControlDown();

        if (isCtrlDown && keyCode == GLFW.GLFW_KEY_C) {
            if (!state.selectedNodes.isEmpty()) {
                List<GuiConnection> selectedConnections = new ArrayList<>();
                for (GuiConnection conn : state.connections) {
                    if (state.selectedNodes.contains(conn.from) && state.selectedNodes.contains(conn.to)) {
                        selectedConnections.add(conn);
                    }
                }
                BlueprintState.clipboardJson = BlueprintIO.serialize(state.selectedNodes, selectedConnections);
                state.showNotification(Component.translatable("gui.mgmc.notification.copied", state.selectedNodes.size()).getString());
            }
            return true;
        }

        if (isCtrlDown && keyCode == GLFW.GLFW_KEY_V) {
            if (BlueprintState.clipboardJson != null && !BlueprintState.clipboardJson.isEmpty()) {
                List<GuiNode> pastedNodes = new ArrayList<>();
                List<GuiConnection> pastedConnections = new ArrayList<>();
                BlueprintIO.loadFromString(BlueprintState.clipboardJson, pastedNodes, pastedConnections, false);

                if (!pastedNodes.isEmpty()) {
                    // Offset pasted nodes and assign new IDs
                    for (GuiNode node : pastedNodes) {
                        node.x += 20;
                        node.y += 20;
                        node.id = java.util.UUID.randomUUID().toString();
                    }

                    // Clear current selection and select pasted nodes
                    for (GuiNode n : state.selectedNodes) n.isSelected = false;
                    state.selectedNodes.clear();

                    for (GuiNode node : pastedNodes) {
                        node.isSelected = true;
                        state.selectedNodes.add(node);
                        state.nodes.add(node);
                    }
                    state.connections.addAll(pastedConnections);
                    state.markDirty();
                    state.showNotification(Component.translatable("gui.mgmc.notification.pasted", pastedNodes.size()).getString());
                }
            }
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            if (!state.selectedNodes.isEmpty()) {
                List<GuiNode> toRemove = new ArrayList<>(state.selectedNodes);
                for (GuiNode node : toRemove) {
                    if (state.focusedNode == node) {
                        state.focusedNode = null;
                        state.focusedPort = null;
                    }
                    state.nodes.remove(node);
                    state.connections.removeIf(c -> c.from == node || c.to == node);
                }
                state.selectedNodes.clear();
                state.markDirty();
                return true;
            }

            double mouseX = Minecraft.getInstance().mouseHandler.xpos() * (double)Minecraft.getInstance().getWindow().getGuiScaledWidth() / (double)Minecraft.getInstance().getWindow().getWidth();
            double mouseY = Minecraft.getInstance().mouseHandler.ypos() * (double)Minecraft.getInstance().getWindow().getGuiScaledHeight() / (double)Minecraft.getInstance().getWindow().getHeight();
            
            double worldMouseX = (mouseX - state.panX) / state.zoom;
            double worldMouseY = (mouseY - state.panY) / state.zoom;
            
            GuiNode hoveredToRemove = null;
            for (int i = state.nodes.size() - 1; i >= 0; i--) {
                GuiNode node = state.nodes.get(i);
                if (worldMouseX >= node.x && worldMouseX <= node.x + node.width && worldMouseY >= node.y && worldMouseY <= node.y + node.height) {
                    hoveredToRemove = node;
                    break;
                }
            }
            
            if (hoveredToRemove != null) {
                if (state.focusedNode == hoveredToRemove) {
                    state.focusedNode = null;
                    state.focusedPort = null;
                }
                final GuiNode finalToRemove = hoveredToRemove;
                state.nodes.remove(hoveredToRemove);
                state.connections.removeIf(c -> c.from == finalToRemove || c.to == finalToRemove);
                state.markDirty();
                return true;
            }
        }
        return false;
    }
}


