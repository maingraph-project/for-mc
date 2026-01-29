package ltd.opens.mg.mc.client.gui.components;

import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodePorts;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;

import java.util.List;

public class GuiNodeHelper {

    public static void updateSize(GuiNode node, Font font) {
        // 标记节点特殊处理
        if (node.definition.properties().containsKey("is_marker")) {
            String text = node.inputValues.has(NodePorts.COMMENT) ? node.inputValues.get(NodePorts.COMMENT).getAsString() : "";
            if (text.isEmpty()) {
                node.width = 100;
                node.height = 40;
            } else {
                int maxWidth = 250;
                List<net.minecraft.util.FormattedCharSequence> lines = font.split(Component.literal(text), maxWidth - 20);
                int textWidth = 0;
                for (net.minecraft.util.FormattedCharSequence line : lines) {
                    textWidth = Math.max(textWidth, font.width(line));
                }
                node.width = Math.max(100, textWidth + 20);
                node.height = node.headerHeight + 10 + lines.size() * 10 + 10;
            }
            node.setSizeDirty(false);
            return;
        }

        // Calculate height
        int maxPorts = Math.max(node.inputs.size(), node.outputs.size());
        
        // 检查是否有配置底部交互按钮
        String buttonLabel = (String) node.definition.properties().get("ui_button_label");
        boolean hasAddButton = buttonLabel != null;
        
        node.height = Math.max(40, node.headerHeight + 10 + maxPorts * 15 + (hasAddButton ? 25 : 5));

        // Calculate width
        float minWidth = 100;
        float titleW = font.width(Component.translatable(node.title)) + 20;

        float maxInputW = 0;
        for (GuiNode.NodePort p : node.inputs) {
            float w = 10 + font.width(Component.translatable(p.displayName));
            if (p.hasInput) {
                w += 55; // Space for input field
            }
            if (isDynamicPort(node, p)) {
                w += 15; // Space for remove button
            }
            maxInputW = Math.max(maxInputW, w);
        }

        float maxOutputW = 0;
        for (GuiNode.NodePort p : node.outputs) {
            float w = 10 + font.width(Component.translatable(p.displayName));
            if (isDynamicPort(node, p)) {
                w += 15; // Space for remove button
            }
            maxOutputW = Math.max(maxOutputW, w);
        }

        if (hasAddButton) {
            float btnW = font.width(Component.translatable(buttonLabel)) + 20;
            minWidth = Math.max(minWidth, btnW);
        }

        node.width = Math.max(minWidth, Math.max(titleW, maxInputW + maxOutputW + 20));
        node.setSizeDirty(false);
    }

    public static boolean isDynamicPort(GuiNode node, GuiNode.NodePort port) {
        if (port.isInput) {
            for (NodeDefinition.PortDefinition defPort : node.definition.inputs()) {
                if (defPort.id().equals(port.id)) return false;
            }
            return true;
        } else {
            for (NodeDefinition.PortDefinition defPort : node.definition.outputs()) {
                if (defPort.id().equals(port.id)) return false;
            }
            return true;
        }
    }

    public static String getRemovePortAt(GuiNode node, double worldMouseX, double worldMouseY, Font font) {
        // Check outputs
        for (int i = 0; i < node.outputs.size(); i++) {
            GuiNode.NodePort port = node.outputs.get(i);
            if (isDynamicPort(node, port)) {
                float[] pos = node.getPortPosition(i, false);
                int rx = (int)pos[0] - 8 - font.width(Component.translatable(port.displayName)) - 12;
                int ry = (int)pos[1] - 4;
                if (worldMouseX >= rx && worldMouseX <= rx + 8 && worldMouseY >= ry && worldMouseY <= ry + 8) {
                    return port.id;
                }
            }
        }
        // Check inputs
        for (int i = 0; i < node.inputs.size(); i++) {
            GuiNode.NodePort port = node.inputs.get(i);
            if (isDynamicPort(node, port)) {
                float[] pos = node.getPortPosition(i, true);
                int rx;
                if (port.hasInput && !port.isConnected) {
                    rx = (int) (pos[0] + 8 + font.width(Component.translatable(port.displayName)) + 2 + 50 + 4);
                } else {
                    rx = (int) (pos[0] + 8 + font.width(Component.translatable(port.displayName)) + 4);
                }
                int ry = (int)pos[1] - 4;
                if (worldMouseX >= rx && worldMouseX <= rx + 8 && worldMouseY >= ry && worldMouseY <= ry + 8) {
                    return port.id;
                }
            }
        }
        return null;
    }

    public static boolean isMouseOverHeader(GuiNode node, double worldMouseX, double worldMouseY) {
        return worldMouseX >= node.x && worldMouseX <= node.x + node.width && worldMouseY >= node.y && worldMouseY <= node.y + node.headerHeight;
    }

    public static boolean isMouseOverAddButton(GuiNode node, double worldMouseX, double worldMouseY) {
        String buttonLabel = (String) node.definition.properties().get("ui_button_label");
        if (buttonLabel == null) return false;
        
        int btnX = (int) node.x + 5;
        int btnY = (int) (node.y + node.height - 20);
        int btnW = (int) node.width - 10;
        int btnH = 16;
        return worldMouseX >= btnX && worldMouseX <= btnX + btnW && worldMouseY >= btnY && worldMouseY <= btnY + btnH;
    }
}
