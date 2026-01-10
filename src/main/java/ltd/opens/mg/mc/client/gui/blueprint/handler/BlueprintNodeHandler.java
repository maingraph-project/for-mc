package ltd.opens.mg.mc.client.gui.blueprint.handler;

import ltd.opens.mg.mc.client.gui.blueprint.BlueprintState;


import ltd.opens.mg.mc.client.gui.screens.*;
import ltd.opens.mg.mc.client.gui.components.*;
import com.google.gson.JsonElement;
import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class BlueprintNodeHandler {
    private final BlueprintState state;

    public BlueprintNodeHandler(BlueprintState state) {
        this.state = state;
    }

    public boolean mouseClicked(double worldMouseX, double worldMouseY, Font font, BlueprintScreen screen) {
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
                if (node.typeId.equals("switch")) {
                    // Add new branch to switch node
                    Minecraft.getInstance().setScreen(new InputModalScreen(
                        screen,
                        Component.translatable("gui.mgmc.modal.enter_value", Component.translatable("node.mgmc.switch.name")).getString(),
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
                } else if (node.typeId.equals("string_combine")) {
                    // Add new input to string combine node
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
                state.draggingNode = node;
                state.dragOffsetX = (float) (worldMouseX - node.x);
                state.dragOffsetY = (float) (worldMouseY - node.y);
                state.nodes.remove(i);
                state.nodes.add(node);
                return true;
            }
        }
        return false;
    }

    public boolean mouseReleased() {
        if (state.draggingNode != null) {
            state.draggingNode = null;
            return true;
        }
        return false;
    }

    public boolean mouseDragged(double worldMouseX, double worldMouseY) {
        if (state.draggingNode != null) {
            float dx = (float) (worldMouseX - state.dragOffsetX) - state.draggingNode.x;
            float dy = (float) (worldMouseY - state.dragOffsetY) - state.draggingNode.y;
            
            if (Math.abs(dx) > 0.1 || Math.abs(dy) > 0.1) {
                state.draggingNode.x = (float) (worldMouseX - state.dragOffsetX);
                state.draggingNode.y = (float) (worldMouseY - state.dragOffsetY);
                state.markDirty();
            }
            return true;
        }
        return false;
    }

    public boolean keyPressed(int keyCode) {
        if (keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            double mouseX = Minecraft.getInstance().mouseHandler.xpos() * (double)Minecraft.getInstance().getWindow().getGuiScaledWidth() / (double)Minecraft.getInstance().getWindow().getWidth();
            double mouseY = Minecraft.getInstance().mouseHandler.ypos() * (double)Minecraft.getInstance().getWindow().getGuiScaledHeight() / (double)Minecraft.getInstance().getWindow().getHeight();
            
            double worldMouseX = (mouseX - state.panX) / state.zoom;
            double worldMouseY = (mouseY - state.panY) / state.zoom;
            
            GuiNode toRemove = null;
            for (int i = state.nodes.size() - 1; i >= 0; i--) {
                GuiNode node = state.nodes.get(i);
                if (worldMouseX >= node.x && worldMouseX <= node.x + node.width && worldMouseY >= node.y && worldMouseY <= node.y + node.height) {
                    toRemove = node;
                    break;
                }
            }
            
            if (toRemove != null) {
                if (state.focusedNode == toRemove) {
                    state.focusedNode = null;
                    state.focusedPort = null;
                }
                final GuiNode finalToRemove = toRemove;
                state.nodes.remove(toRemove);
                state.connections.removeIf(c -> c.from == finalToRemove || c.to == finalToRemove);
                return true;
            }
        }
        return false;
    }
}


