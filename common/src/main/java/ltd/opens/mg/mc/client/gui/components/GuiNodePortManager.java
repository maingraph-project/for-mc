package ltd.opens.mg.mc.client.gui.components;

import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import java.util.List;

public class GuiNodePortManager {

    public static void addInput(GuiNode node, String id, String displayName, NodeDefinition.PortType type, int color, boolean hasInput, Object defaultValue, String[] options) {
        node.inputs.add(new GuiNode.NodePort(id, displayName, type, color, true, hasInput, defaultValue, options));
        node.markSizeDirty();
    }

    public static void addOutput(GuiNode node, String id, String displayName, NodeDefinition.PortType type, int color) {
        node.outputs.add(new GuiNode.NodePort(id, displayName, type, color, false, false, null, null));
        node.markSizeDirty();
    }

    public static GuiNode.NodePort getPortByName(GuiNode node, String id, boolean isInput) {
        List<GuiNode.NodePort> ports = isInput ? node.inputs : node.outputs;
        for (GuiNode.NodePort p : ports) {
            if (p.id.equals(id)) return p;
        }
        return null;
    }

    public static float[] getPortPosition(GuiNode node, int index, boolean isInput) {
        float py = node.y + node.headerHeight + 10 + index * 15; // Exact center of port
        float px = isInput ? node.x : node.x + node.width;
        return new float[]{px, py};
    }

    public static float[] getPortPositionByName(GuiNode node, String id, boolean isInput) {
        List<GuiNode.NodePort> ports = isInput ? node.inputs : node.outputs;
        for (int i = 0; i < ports.size(); i++) {
            if (ports.get(i).id.equals(id)) {
                return getPortPosition(node, i, isInput);
            }
        }
        return new float[]{node.x, node.y};
    }

    public static void updateConnectedState(GuiNode node, List<GuiConnection> connections) {
        for (GuiNode.NodePort p : node.inputs) {
            p.isConnected = false;
            for (GuiConnection c : connections) {
                if (c.to == node && c.toPort.equals(p.id)) {
                    p.isConnected = true;
                    break;
                }
            }
        }
        for (GuiNode.NodePort p : node.outputs) {
            p.isConnected = false;
            for (GuiConnection c : connections) {
                if (c.from == node && c.fromPort.equals(p.id)) {
                    p.isConnected = true;
                    break;
                }
            }
        }
    }
}
