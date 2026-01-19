package ltd.opens.mg.mc.client.gui.components;
 
import ltd.opens.mg.mc.client.gui.blueprint.Viewport;
import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodePorts;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import com.google.gson.JsonElement;
import java.util.List;

public class GuiNodeRenderer {

    public static void render(GuiNode node, GuiGraphics guiGraphics, Font font, int mouseX, int mouseY, Viewport viewport, List<GuiConnection> connections, GuiNode focusedNode, String focusedPort, boolean isEditing, int highlightTimer) {
        float zoom = viewport.zoom;
        // LOD 3: Minimal rendering for very far zoom
        if (zoom < 0.15f) {
            guiGraphics.fill((int) node.x, (int) node.y, (int) (node.x + node.width), (int) (node.y + node.height), node.color);
            return;
        }

        // Highlight Effect
        if (highlightTimer > 0) {
            float alpha = Math.min(1.0f, highlightTimer / 10.0f);
            int color = ((int)(alpha * 255) << 24) | 0xFFFFFF;
            int expand = (int) (4 * (1.0f - alpha * 0.5f));
            guiGraphics.renderOutline((int) node.x - expand, (int) node.y - expand, (int) node.width + expand * 2, (int) node.height + expand * 2, color);
            guiGraphics.renderOutline((int) node.x - expand - 1, (int) node.y - expand - 1, (int) node.width + expand * 2 + 2, (int) node.height + expand * 2 + 2, color);
        }

        // Background
        guiGraphics.fill((int) node.x, (int) node.y, (int) (node.x + node.width), (int) (node.y + node.height), 0xEE1A1A1A);
        
        // Border
        double worldMouseX = viewport.toWorldX(mouseX);
        double worldMouseY = viewport.toWorldY(mouseY);
        boolean isHovered = worldMouseX >= node.x && worldMouseX <= node.x + node.width && worldMouseY >= node.y && worldMouseY <= node.y + node.height;
        
        // Simplified border: only draw if hovered or at reasonable zoom
        if (isHovered || node.isSelected || zoom > 0.4f) {
            int borderColor = node.isSelected ? 0xFFFFCC00 : (isHovered ? 0xFFFFFFFF : 0xFF333333);
            guiGraphics.renderOutline((int) node.x, (int) node.y, (int) node.width, (int) node.height, borderColor);
            if (node.isSelected) {
                // Thicker highlight for selection
                guiGraphics.renderOutline((int) node.x - 1, (int) node.y - 1, (int) node.width + 2, (int) node.height + 2, borderColor);
            }
        }
        
        // Header
        guiGraphics.fill((int) node.x + 1, (int) node.y + 1, (int) (node.x + node.width - 1), (int) (node.y + node.headerHeight), node.color);
        
        // Title - hide if too small
        if (zoom > 0.3f) {
            guiGraphics.drawString(font, Component.translatable(node.title), (int) node.x + 5, (int) node.y + 4, 0xFFFFFFFF, false);
        }

        // Marker Special Rendering
        if (node.definition.properties().containsKey("is_marker")) {
            if (zoom > 0.2f && !isEditing) {
                String text = node.inputValues.has(NodePorts.COMMENT) ? 
                           node.inputValues.get(NodePorts.COMMENT).getAsString() : "";
                if (!text.isEmpty()) {
                    int maxWidth = 250;
                    List<FormattedCharSequence> lines = font.split(Component.literal(text), maxWidth - 20);
                    for (int i = 0; i < lines.size(); i++) {
                        guiGraphics.drawString(font, lines.get(i), (int) node.x + 10, (int) (node.y + node.headerHeight + 10 + i * 10), 0xFFAAAAAA, false);
                    }
                }
            }
            return;
        }

        // Render Ports - Skip entirely if very zoomed out
        if (zoom > 0.2f) {
            // Render Inputs
            for (int i = 0; i < node.inputs.size(); i++) {
                renderPort(node, guiGraphics, font, node.inputs.get(i), (int) node.x, (int) (node.y + node.headerHeight + 10 + i * 15), true, connections, focusedNode, focusedPort, zoom);
            }

            // Render Outputs
            for (int i = 0; i < node.outputs.size(); i++) {
                renderPort(node, guiGraphics, font, node.outputs.get(i), (int) (node.x + node.width), (int) (node.y + node.headerHeight + 10 + i * 15), false, connections, focusedNode, focusedPort, zoom);
            }
            
            // Add Branch Button for Switch Node / Add Input for String Combine
            String buttonLabel = (String) node.definition.properties().get("ui_button_label");
            if (buttonLabel != null) {
                int btnX = (int) node.x + 5;
                int btnY = (int) (node.y + node.height - 20);
                int btnW = (int) node.width - 10;
                int btnH = 16;
                
                boolean hovered = worldMouseX >= btnX && worldMouseX <= btnX + btnW && worldMouseY >= btnY && worldMouseY <= btnY + btnH;
                
                guiGraphics.fill(btnX, btnY, btnX + btnW, btnY + btnH, hovered ? 0xFF444444 : 0xFF333333);
                guiGraphics.renderOutline(btnX, btnY, btnW, btnH, 0xFF555555);
                
                Component btnText = Component.translatable(buttonLabel);
                guiGraphics.drawString(font, btnText, btnX + (btnW - font.width(btnText)) / 2, btnY + 4, 0xFFFFFFFF, false);
            }
        }
    }

    private static void renderPort(GuiNode node, GuiGraphics guiGraphics, Font font, GuiNode.NodePort port, int px, int py, boolean isInput, List<GuiConnection> connections, GuiNode focusedNode, String focusedPort, float zoom) {
        int color = getPortColor(port);
        boolean isConnected = port.isConnected;
        
        if (zoom > 0.3f) {
            float size = 4.0f;
            if (port.type == NodeDefinition.PortType.EXEC) {
                if (isConnected) {
                    renderTriangle(guiGraphics, px - size, py - size, px + size, py, px - size, py + size, color);
                } else {
                    renderTriangleOutline(guiGraphics, px - size, py - size, px + size, py, px - size, py + size, color);
                }
            } else {
                if (isConnected) {
                    drawCircle(guiGraphics, px, py, (int)size, color);
                } else {
                    drawCircleOutline(guiGraphics, px, py, (int)size, color);
                }
            }
        } else if (zoom > 0.15f) {
            guiGraphics.fill(px - 1, py - 1, px + 1, py + 1, color);
        }

        if (zoom > 0.4f) {
            if (isInput) {
                guiGraphics.drawString(font, Component.translatable(port.displayName), px + 8, py - 1, 0xFFAAAAAA, false);
                
                if (port.hasInput && !isConnected && zoom > 0.6f) {
                    float inputX = px + 8 + font.width(Component.translatable(port.displayName)) + 2;
                    float inputY = py - 4;
                    float inputWidth = 50;
                    float inputHeight = 10;
                    
                    guiGraphics.fill((int)inputX, (int)inputY, (int)(inputX + inputWidth), (int)(inputY + inputHeight), 0x66000000);
                    
                    if (port.type == NodeDefinition.PortType.BOOLEAN) {
                        JsonElement val = node.inputValues.get(port.id);
                        boolean boolVal = val != null ? val.getAsBoolean() : (port.defaultValue instanceof Boolean ? (Boolean) port.defaultValue : false);
                        
                        int boxColor = boolVal ? 0xFF36CF36 : 0xFF333333;
                        guiGraphics.fill((int)inputX + 2, (int)inputY + 2, (int)inputX + 8, (int)inputY + 8, boxColor);
                        guiGraphics.renderOutline((int)inputX + 1, (int)inputY + 1, 8, 8, 0xFFFFFFFF);
                        
                        Component text = Component.translatable(boolVal ? "gui.mgmc.bool.true" : "gui.mgmc.bool.false");
                        guiGraphics.drawString(font, text, (int)inputX + 12, (int)inputY + 1, 0xFFCCCCCC, false);
                    } else if (port.options != null && port.options.length > 0) {
                        guiGraphics.renderOutline((int)inputX, (int)inputY, (int)inputWidth, (int)inputHeight, 0xFFFFFFFF);
                        
                        JsonElement val = node.inputValues.get(port.id);
                        String text = val != null ? val.getAsString() : (port.defaultValue != null ? port.defaultValue.toString() : port.options[0]);
                        
                        guiGraphics.drawString(font, "v", (int)(inputX + inputWidth - 8), (int)inputY + 1, 0xFFAAAAAA, false);
                        
                        String renderText = text;
                        if (font.width(renderText) > inputWidth - 12) {
                            renderText = font.plainSubstrByWidth(renderText, (int)inputWidth - 15, true) + "..";
                        }
                        guiGraphics.drawString(font, renderText, (int)inputX + 2, (int)inputY + 1, 0xFFCCCCCC, false);
                    } else {
                        boolean isFocused = focusedNode == node && focusedPort != null && focusedPort.equals(port.id);
                        guiGraphics.renderOutline((int)inputX, (int)inputY, (int)inputWidth, (int)inputHeight, isFocused ? 0xFFFFFFFF : 0x33FFFFFF);
                        
                        JsonElement val = node.inputValues.get(port.id);
                        String text = val != null ? val.getAsString() : (port.defaultValue != null ? port.defaultValue.toString() : "");
                        
                        String renderText = text;
                        if (font.width(renderText) > inputWidth - 4) {
                            renderText = "..." + font.plainSubstrByWidth(renderText, (int)inputWidth - 10, true);
                        }
                        guiGraphics.drawString(font, renderText, (int)inputX + 2, (int)inputY + 1, 0xFFCCCCCC, false);
                    }
                }

                if (node.isDynamicPort(port) && zoom > 0.6f) {
                    int rx;
                    if (port.hasInput && !isConnected) {
                        rx = (int) (px + 8 + font.width(Component.translatable(port.displayName)) + 2 + 50 + 4);
                    } else {
                        rx = (int) (px + 8 + font.width(Component.translatable(port.displayName)) + 4);
                    }
                    int ry = py - 4;
                    guiGraphics.drawString(font, "×", rx, ry, 0xFFFF5555, false);
                }
            } else {
                guiGraphics.drawString(font, Component.translatable(port.displayName), px - 8 - font.width(Component.translatable(port.displayName)), py - 1, 0xFFAAAAAA, false);
                
                if (node.isDynamicPort(port) && zoom > 0.6f) {
                    int rx = px - 8 - font.width(Component.translatable(port.displayName)) - 12;
                    int ry = py - 4;
                    guiGraphics.drawString(font, "×", rx, ry, 0xFFFF5555, false);
                }
            }
        }
    }

    private static int getPortColor(GuiNode.NodePort port) {
        switch (port.type) {
            case EXEC: return 0xFFFFFFFF;
            case STRING: return 0xFFFF5555;
            case FLOAT: return 0xFF55FF55;
            case BOOLEAN: return 0xFF5555FF;
            case LIST: return 0xFFFFFF55;
            case UUID: return 0xFFFF55FF;
            case ENTITY: return 0xFF55FFFF;
            case ENUM: return 0xFFFFAA00;
            case ANY: return 0xFFAAAAAA;
            default: return port.color;
        }
    }

    private static void renderTriangle(GuiGraphics guiGraphics, float x1, float y1, float x2, float y2, float x3, float y3, int color) {
        float minY = Math.min(y1, Math.min(y2, y3));
        float maxY = Math.max(y1, Math.max(y2, y3));
        int iMinY = (int) Math.floor(minY);
        int iMaxY = (int) Math.ceil(maxY);
        
        for (int y = iMinY; y <= iMaxY; y++) {
            float minX = Float.MAX_VALUE;
            float maxX = -Float.MAX_VALUE;
            boolean intersected = false;
            float[][] v = {{x1, y1}, {x2, y2}, {x3, y3}, {x1, y1}};
            for (int i = 0; i < 3; i++) {
                float ey1 = v[i][1], ey2 = v[i+1][1];
                if ((ey1 <= y && ey2 > y) || (ey2 <= y && ey1 > y)) {
                    float ex1 = v[i][0], ex2 = v[i+1][0];
                    float ix = ex1 + (y - ey1) * (ex2 - ex1) / (ey2 - ey1);
                    if (ix < minX) minX = ix;
                    if (ix > maxX) maxX = ix;
                    intersected = true;
                }
            }
            if (intersected) {
                guiGraphics.fill((int)minX, y, (int)maxX + 1, y + 1, color);
            }
        }
    }

    private static void renderTriangleOutline(GuiGraphics guiGraphics, float x1, float y1, float x2, float y2, float x3, float y3, int color) {
        drawLine(guiGraphics, x1, y1, x2, y2, color);
        drawLine(guiGraphics, x2, y2, x3, y3, color);
        drawLine(guiGraphics, x3, y3, x1, y1, color);
    }

    private static void drawLine(GuiGraphics guiGraphics, float x1, float y1, float x2, float y2, int color) {
        if (Math.abs(x1 - x2) < 0.5f) {
            guiGraphics.fill((int)x1, (int)Math.min(y1, y2), (int)x1 + 1, (int)Math.max(y1, y2) + 1, color);
            return;
        }
        if (Math.abs(y1 - y2) < 0.5f) {
            guiGraphics.fill((int)Math.min(x1, x2), (int)y1, (int)Math.max(x1, x2) + 1, (int)y1 + 1, color);
            return;
        }
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 0.5f) return;
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(x1, y1);
        float angle = (float) Math.atan2(dy, dx);
        guiGraphics.pose().rotate(angle);
        guiGraphics.fill(0, 0, (int)len, 1, color);
        guiGraphics.pose().popMatrix();
    }

    private static void drawCircle(GuiGraphics guiGraphics, float cx, float cy, int radius, int color) {
        int r2 = radius * radius;
        for (int y = -radius; y <= radius; y++) {
            int xSpan = (int) Math.sqrt(r2 - y * y);
            guiGraphics.fill((int)(cx - xSpan), (int)(cy + y), (int)(cx + xSpan + 1), (int)(cy + y + 1), color);
        }
    }

    private static void drawCircleOutline(GuiGraphics guiGraphics, float cx, float cy, int radius, int color) {
        int segments = radius > 10 ? 16 : 8;
        float lastX = cx + radius;
        float lastY = cy;
        for (int i = 1; i <= segments; i++) {
            float angle = (float) (i * 2 * Math.PI / segments);
            float x = (float) (cx + Math.cos(angle) * radius);
            float y = (float) (cy + Math.sin(angle) * radius);
            drawLine(guiGraphics, lastX, lastY, x, y, color);
            lastX = x;
            lastY = y;
        }
    }
}
