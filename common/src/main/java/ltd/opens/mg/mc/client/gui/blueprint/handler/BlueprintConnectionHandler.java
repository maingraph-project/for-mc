package ltd.opens.mg.mc.client.gui.blueprint.handler;

import ltd.opens.mg.mc.client.gui.blueprint.BlueprintState;


import ltd.opens.mg.mc.client.gui.components.*;
import ltd.opens.mg.mc.core.blueprint.NodeDefinition;

public class BlueprintConnectionHandler {
    private final BlueprintState state;

    public BlueprintConnectionHandler(BlueprintState state) {
        this.state = state;
    }

    public boolean mouseClicked(double worldMouseX, double worldMouseY) {
        for (int i = state.nodes.size() - 1; i >= 0; i--) {
            GuiNode node = state.nodes.get(i);
            // Check inputs
            for (int j = 0; j < node.inputs.size(); j++) {
                float[] pos = node.getPortPosition(j, true);
                if (Math.abs(worldMouseX - pos[0]) < 5 && Math.abs(worldMouseY - pos[1]) < 5) {
                    state.connectionStartNode = node;
                    state.connectionStartPort = node.inputs.get(j).id;
                    state.isConnectionFromInput = true;
                    state.isAnimatingView = false;
                    return true;
                }
            }
            // Check outputs
            for (int j = 0; j < node.outputs.size(); j++) {
                float[] pos = node.getPortPosition(j, false);
                if (Math.abs(worldMouseX - pos[0]) < 5 && Math.abs(worldMouseY - pos[1]) < 5) {
                    state.connectionStartNode = node;
                    state.connectionStartPort = node.outputs.get(j).id;
                    state.isConnectionFromInput = false;
                    state.isAnimatingView = false;
                    return true;
                }
            }
            
            // If the mouse is anywhere over this node, stop checking ports for nodes underneath
            if (worldMouseX >= node.x && worldMouseX <= node.x + node.width &&
                worldMouseY >= node.y && worldMouseY <= node.y + node.height) {
                return false; // Let BlueprintNodeHandler handle the node click
            }
        }
        return false;
    }

    private boolean canConnect(NodeDefinition.PortType type1, NodeDefinition.PortType type2) {
        if (type1 == NodeDefinition.PortType.EXEC || type2 == NodeDefinition.PortType.EXEC) {
            return type1 == type2;
        }
        if (type1 == NodeDefinition.PortType.ANY || type2 == NodeDefinition.PortType.ANY) {
            return true;
        }
        return type1 == type2;
    }

    public boolean mouseReleased(double worldMouseX, double worldMouseY) {
        if (state.connectionStartNode != null) {
            boolean connected = false;
            for (GuiNode node : state.nodes) {
                if (node == state.connectionStartNode) continue;
                
                // If started from output, look for input
                if (!state.isConnectionFromInput) {
                    GuiNode.NodePort startPort = state.connectionStartNode.getPortByName(state.connectionStartPort, false);
                    for (int i = 0; i < node.inputs.size(); i++) {
                        GuiNode.NodePort targetPort = node.inputs.get(i);
                        float[] pos = node.getPortPosition(i, true);
                        if (Math.abs(worldMouseX - pos[0]) < 10 && Math.abs(worldMouseY - pos[1]) < 10) {
                            if (startPort != null && canConnect(startPort.type, targetPort.type)) {
                                state.pushHistory();
                                // Remove existing connections to this input if it's not EXEC
                                if (targetPort.type != NodeDefinition.PortType.EXEC) {
                                    state.connections.removeIf(c -> c.to == node && c.toPort.equals(targetPort.id));
                                } else {
                                    // For EXEC, if a connection already exists between the same ports, remove it first to "cover" it
                                    state.connections.removeIf(c -> c.from == state.connectionStartNode && c.fromPort.equals(state.connectionStartPort) 
                                        && c.to == node && c.toPort.equals(targetPort.id));
                                }
                                state.connections.add(new GuiConnection(state.connectionStartNode, state.connectionStartPort, node, targetPort.id));
                                state.markDirty();
                                connected = true;
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
                            if (startPort != null && canConnect(startPort.type, targetPort.type)) {
                                state.pushHistory();
                                // Remove existing connections to the start input if it's not EXEC
                                if (startPort.type != NodeDefinition.PortType.EXEC) {
                                    state.connections.removeIf(c -> c.to == state.connectionStartNode && c.toPort.equals(startPort.id));
                                } else {
                                    // For EXEC, if a connection already exists between the same ports, remove it first to "cover" it
                                    state.connections.removeIf(c -> c.from == node && c.fromPort.equals(targetPort.id) 
                                        && c.to == state.connectionStartNode && c.toPort.equals(startPort.id));
                                }
                                state.connections.add(new GuiConnection(node, targetPort.id, state.connectionStartNode, state.connectionStartPort));
                                state.markDirty();
                                connected = true;
                            }
                            break;
                        }
                    }
                }
                if (connected) break;
            }

            if (!connected) {
                // Open node menu if released in empty space
                state.pendingConnectionSourceNode = state.connectionStartNode;
                state.pendingConnectionSourcePort = state.connectionStartPort;
                state.pendingConnectionFromInput = state.isConnectionFromInput;
                
                GuiNode.NodePort port = state.connectionStartNode.getPortByName(state.connectionStartPort, state.isConnectionFromInput);
                if (port != null) {
                    state.pendingConnectionSourceType = port.type;
                    state.showNodeMenu = true;
                    state.menuX = state.viewport.toScreenX((float)worldMouseX);
                    state.menuY = state.viewport.toScreenY((float)worldMouseY);
                    state.menu.reset();
                    state.menu.setFilter(port.type, !state.isConnectionFromInput);
                }
            }

            state.connectionStartNode = null;
            state.connectionStartPort = null;
            return true;
        }
        return false;
    }
}


