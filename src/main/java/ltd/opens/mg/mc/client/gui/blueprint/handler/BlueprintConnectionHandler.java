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
        for (GuiNode node : state.nodes) {
            // Check inputs
            for (int i = 0; i < node.inputs.size(); i++) {
                float[] pos = node.getPortPosition(i, true);
                if (Math.abs(worldMouseX - pos[0]) < 5 && Math.abs(worldMouseY - pos[1]) < 5) {
                    state.connectionStartNode = node;
                    state.connectionStartPort = node.inputs.get(i).id;
                    state.isConnectionFromInput = true;
                    return true;
                }
            }
            // Check outputs
            for (int i = 0; i < node.outputs.size(); i++) {
                float[] pos = node.getPortPosition(i, false);
                if (Math.abs(worldMouseX - pos[0]) < 5 && Math.abs(worldMouseY - pos[1]) < 5) {
                    state.connectionStartNode = node;
                    state.connectionStartPort = node.outputs.get(i).id;
                    state.isConnectionFromInput = false;
                    return true;
                }
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
                                // Remove existing connections to this input if it's not EXEC
                                if (targetPort.type != NodeDefinition.PortType.EXEC) {
                                    state.connections.removeIf(c -> c.to == node && c.toPort.equals(targetPort.id));
                                }
                                state.connections.add(new GuiConnection(state.connectionStartNode, state.connectionStartPort, node, targetPort.id));
                                state.markDirty();
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
                                // Remove existing connections to the start input if it's not EXEC
                                if (startPort.type != NodeDefinition.PortType.EXEC) {
                                    state.connections.removeIf(c -> c.to == state.connectionStartNode && c.toPort.equals(startPort.id));
                                }
                                state.connections.add(new GuiConnection(node, targetPort.id, state.connectionStartNode, state.connectionStartPort));
                                state.markDirty();
                            }
                            break;
                        }
                    }
                }
            }
            state.connectionStartNode = null;
            state.connectionStartPort = null;
            return true;
        }
        return false;
    }
}


