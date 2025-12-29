package ltd.opens.mg.mc.client.gui;

import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.input.MouseButtonEvent;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.input.KeyEvent;
import com.google.gson.JsonElement;

public class BlueprintEventHandler {
    private final BlueprintState state;

    public BlueprintEventHandler(BlueprintState state) {
        this.state = state;
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean isDouble, Font font, BlueprintScreen screen) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.buttonInfo().button();

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
                }
                state.showNodeContextMenu = false;
                state.contextMenuNode = null;
                return true;
            } else if (result == BlueprintMenu.ContextMenuResult.BREAK_LINKS) {
                if (state.contextMenuNode != null) {
                    final GuiNode finalNode = state.contextMenuNode;
                    state.connections.removeIf(c -> c.from == finalNode || c.to == finalNode);
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
            NodeDefinition def = state.menu.onClickNodeMenu(mouseX, mouseY, state.menuX, state.menuY);
            if (def != null) {
                float worldX = (float) ((state.menuX - state.panX) / state.zoom);
                float worldY = (float) ((state.menuY - state.panY) / state.zoom);
                GuiNode node = new GuiNode(def, worldX, worldY);
                state.nodes.add(node);
                state.showNodeMenu = false;
                state.menu.reset();
                return true;
            }

            if (!state.menu.isClickInsideNodeMenu(mouseX, mouseY, state.menuX, state.menuY)) {
                state.showNodeMenu = false;
                state.menu.reset();
            }
            return true;
        }

        if (button == 2) { // Middle click for panning
            state.isPanning = true;
            state.lastMouseX = mouseX;
            state.lastMouseY = mouseY;
            return true;
        }

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
            return true;
        }
        
        double worldMouseX = (mouseX - state.panX) / state.zoom;
        double worldMouseY = (mouseY - state.panY) / state.zoom;

        if (button == 0) { // Left click
            state.focusedNode = null;
            state.focusedPort = null;
            
            // Check for port clicks first
            for (GuiNode node : state.nodes) {
                // Check inputs
                for (int i = 0; i < node.inputs.size(); i++) {
                    GuiNode.NodePort port = node.inputs.get(i);
                    float[] pos = node.getPortPosition(i, true);
                    
                    // Input box click check
                    if (port.hasInput) {
                        float inputX = pos[0] + 8 + font.width(port.name) + 2;
                        float inputY = pos[1] - 4;
                        float inputWidth = 50;
                        float inputHeight = 10;
                        if (worldMouseX >= inputX && worldMouseX <= inputX + inputWidth && worldMouseY >= inputY && worldMouseY <= inputY + inputHeight) {
                            // Only allow editing if not connected
                            boolean isConnected = false;
                            for (GuiConnection conn : state.connections) {
                                if (conn.to == node && conn.toPort.equals(port.name)) {
                                    isConnected = true;
                                    break;
                                }
                            }
                            if (!isConnected) {
                                if (port.type == NodeDefinition.PortType.BOOLEAN) {
                                    JsonElement val = node.inputValues.get(port.name);
                                    boolean current = val != null ? val.getAsBoolean() : (port.defaultValue instanceof Boolean ? (Boolean) port.defaultValue : false);
                                    node.inputValues.addProperty(port.name, !current);
                                } else {
                                    // Open separate input modal
                                    JsonElement val = node.inputValues.get(port.name);
                                    String initialText = val != null ? val.getAsString() : (port.defaultValue != null ? port.defaultValue.toString() : "");
                                    boolean isNumeric = (port.type == NodeDefinition.PortType.FLOAT);
                                    
                                    final GuiNode targetNode = node;
                                    final String targetPort = port.name;
                                    
                                    Minecraft.getInstance().setScreen(new InputModalScreen(
                                        screen, 
                                        "Enter " + port.name, 
                                        initialText, 
                                        isNumeric,
                                        (newText) -> {
                                            targetNode.inputValues.addProperty(targetPort, newText);
                                        }
                                    ));
                                }
                                return true;
                            }
                        }
                    }

                    if (Math.abs(worldMouseX - pos[0]) < 5 && Math.abs(worldMouseY - pos[1]) < 5) {
                        state.connectionStartNode = node;
                        state.connectionStartPort = node.inputs.get(i).name;
                        state.isConnectionFromInput = true;
                        return true;
                    }
                }
                // Check outputs
                for (int i = 0; i < node.outputs.size(); i++) {
                    float[] pos = node.getPortPosition(i, false);
                    if (Math.abs(worldMouseX - pos[0]) < 5 && Math.abs(worldMouseY - pos[1]) < 5) {
                        state.connectionStartNode = node;
                        state.connectionStartPort = node.outputs.get(i).name;
                        state.isConnectionFromInput = false;
                        return true;
                    }
                }
            }

            // Check for node header click
            for (int i = state.nodes.size() - 1; i >= 0; i--) {
                GuiNode node = state.nodes.get(i);
                if (node.isMouseOverHeader(worldMouseX, worldMouseY)) {
                    state.draggingNode = node;
                    state.dragOffsetX = (float) (worldMouseX - node.x);
                    state.dragOffsetY = (float) (worldMouseY - node.y);
                    state.nodes.remove(i);
                    state.nodes.add(node);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean mouseReleased(MouseButtonEvent event) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.buttonInfo().button();
        if (button == 2) {
            state.isPanning = false;
            return true;
        }
        if (state.connectionStartNode != null) {
            double worldMouseX = (mouseX - state.panX) / state.zoom;
            double worldMouseY = (mouseY - state.panY) / state.zoom;

            for (GuiNode node : state.nodes) {
                if (node == state.connectionStartNode) continue;
                
                // If started from output, look for input
                if (!state.isConnectionFromInput) {
                    GuiNode.NodePort startPort = state.connectionStartNode.getPortByName(state.connectionStartPort, false);
                    for (int i = 0; i < node.inputs.size(); i++) {
                        GuiNode.NodePort targetPort = node.inputs.get(i);
                        float[] pos = node.getPortPosition(i, true);
                        if (Math.abs(worldMouseX - pos[0]) < 10 && Math.abs(worldMouseY - pos[1]) < 10) {
                            if (startPort != null && startPort.type == targetPort.type) {
                                // Remove existing connections to this input if it's not EXEC
                                if (targetPort.type != NodeDefinition.PortType.EXEC) {
                                    state.connections.removeIf(c -> c.to == node && c.toPort.equals(targetPort.name));
                                }
                                state.connections.add(new GuiConnection(state.connectionStartNode, state.connectionStartPort, node, targetPort.name));
                            }
                            break;
                        }
                    }
                } else {
                    // Started from input, look for output
                    GuiNode.NodePort startPort = state.connectionStartNode.getPortByName(state.connectionStartPort, true);
                    for (int i = 0; i < node.outputs.size(); i++) {
                        GuiNode.NodePort targetPort = node.outputs.get(i);
                        float[] pos = node.getPortPosition(i, false);
                        if (Math.abs(worldMouseX - pos[0]) < 10 && Math.abs(worldMouseY - pos[1]) < 10) {
                            if (startPort != null && startPort.type == targetPort.type) {
                                // Remove existing connections to the start input if it's not EXEC
                                if (startPort.type != NodeDefinition.PortType.EXEC) {
                                    state.connections.removeIf(c -> c.to == state.connectionStartNode && c.toPort.equals(startPort.name));
                                }
                                state.connections.add(new GuiConnection(node, targetPort.name, state.connectionStartNode, state.connectionStartPort));
                            }
                            break;
                        }
                    }
                }
            }
            state.connectionStartNode = null;
            state.connectionStartPort = null;
        }

        state.draggingNode = null;
        state.isPanning = false;
        return false;
    }

    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        double mouseX = event.x();
        double mouseY = event.y();
        if (state.isPanning) {
            state.panX += (float) (mouseX - state.lastMouseX);
            state.panY += (float) (mouseY - state.lastMouseY);
            state.lastMouseX = mouseX;
            state.lastMouseY = mouseY;
            return true;
        }

        if (state.draggingNode != null) {
            double worldMouseX = (mouseX - state.panX) / state.zoom;
            double worldMouseY = (mouseY - state.panY) / state.zoom;
            state.draggingNode.x = (float) (worldMouseX - state.dragOffsetX);
            state.draggingNode.y = (float) (worldMouseY - state.dragOffsetY);
            return true;
        }
        return false;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
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

    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();

        if (keyCode == GLFW.GLFW_KEY_DELETE || keyCode == GLFW.GLFW_KEY_BACKSPACE) {
            // Delete selected node (last one mouse was over)
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
