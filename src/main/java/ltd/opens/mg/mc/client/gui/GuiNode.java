package ltd.opens.mg.mc.client.gui;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import net.minecraft.client.gui.GuiGraphics;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GuiNode {
    public String id;
    public String typeId;
    public NodeDefinition definition;
    public String title;
    public float x, y;
    public int color;
    public float width = 120;
    public float height = 60;
    public float headerHeight = 15;
    public JsonObject inputValues = new JsonObject();

    public List<NodePort> inputs = new ArrayList<>();
    public List<NodePort> outputs = new ArrayList<>();

    private boolean sizeDirty = true;

    public GuiNode(NodeDefinition def, float x, float y) {
        this.id = UUID.randomUUID().toString();
        this.typeId = def.id();
        this.definition = def;
        this.title = def.name();
        this.x = x;
        this.y = y;
        this.color = def.color();
        
        for (NodeDefinition.PortDefinition p : def.inputs()) {
            addInput(p.id(), p.displayName(), p.type(), p.color(), p.hasInput(), p.defaultValue(), p.options());
        }
        for (NodeDefinition.PortDefinition p : def.outputs()) {
            addOutput(p.id(), p.displayName(), p.type(), p.color());
        }
    }

    public void addInput(String id, String displayName, NodeDefinition.PortType type, int color, boolean hasInput, Object defaultValue, String[] options) {
        inputs.add(new NodePort(id, displayName, type, color, true, hasInput, defaultValue, options));
        sizeDirty = true;
    }

    public void addOutput(String id, String displayName, NodeDefinition.PortType type, int color) {
        outputs.add(new NodePort(id, displayName, type, color, false, false, null, null));
        sizeDirty = true;
    }

    private void updateSize(net.minecraft.client.gui.Font font) {
        // Calculate height
        int maxPorts = Math.max(inputs.size(), outputs.size());
        this.height = Math.max(40, headerHeight + 10 + maxPorts * 15 + 5);

        // Calculate width
        float minWidth = 100;
        float titleW = font.width(title) + 20;

        float maxInputW = 0;
        for (NodePort p : inputs) {
            float w = 10 + font.width(p.displayName);
            if (p.hasInput) {
                w += 55; // Space for input field
            }
            maxInputW = Math.max(maxInputW, w);
        }

        float maxOutputW = 0;
        for (NodePort p : outputs) {
            float w = 10 + font.width(p.displayName);
            maxOutputW = Math.max(maxOutputW, w);
        }

        this.width = Math.max(minWidth, Math.max(titleW, maxInputW + maxOutputW + 20));
        sizeDirty = false;
    }

    public float[] getPortPositionByName(String id, boolean isInput) {
        List<NodePort> ports = isInput ? inputs : outputs;
        for (int i = 0; i < ports.size(); i++) {
            if (ports.get(i).id.equals(id)) {
                return getPortPosition(i, isInput);
            }
        }
        return new float[]{x, y};
    }

    public NodePort getPortByName(String id, boolean isInput) {
        List<NodePort> ports = isInput ? inputs : outputs;
        for (NodePort p : ports) {
            if (p.id.equals(id)) return p;
        }
        return null;
    }

    public float[] getPortPosition(int index, boolean isInput) {
        float py = y + headerHeight + 10 + index * 15; // Exact center of port
        float px = isInput ? x : x + width;
        return new float[]{px, py};
    }

    public void updateConnectedState(List<GuiConnection> connections) {
        for (NodePort p : inputs) {
            p.isConnected = false;
            for (GuiConnection c : connections) {
                if (c.to == this && c.toPort.equals(p.id)) {
                    p.isConnected = true;
                    break;
                }
            }
        }
        for (NodePort p : outputs) {
            p.isConnected = false;
            for (GuiConnection c : connections) {
                if (c.from == this && c.fromPort.equals(p.id)) {
                    p.isConnected = true;
                    break;
                }
            }
        }
    }

    public void render(GuiGraphics guiGraphics, net.minecraft.client.gui.Font font, int mouseX, int mouseY, float panX, float panY, float zoom, List<GuiConnection> connections, GuiNode focusedNode, String focusedPort) {
        if (sizeDirty) {
            updateSize(font);
        }

        // LOD 3: Minimal rendering for very far zoom
        if (zoom < 0.15f) {
            guiGraphics.fill((int) x, (int) y, (int) (x + width), (int) (y + height), color);
            return;
        }

        // Background
        guiGraphics.fill((int) x, (int) y, (int) (x + width), (int) (y + height), 0xEE1A1A1A);
        
        // Border
        double worldMouseX = (mouseX - panX) / zoom;
        double worldMouseY = (mouseY - panY) / zoom;
        boolean isHovered = worldMouseX >= x && worldMouseX <= x + width && worldMouseY >= y && worldMouseY <= y + height;
        
        // Simplified border: only draw if hovered or at reasonable zoom
        if (isHovered || zoom > 0.4f) {
            int borderColor = isHovered ? 0xFFFFFFFF : 0xFF333333;
            guiGraphics.renderOutline((int) x, (int) y, (int) width, (int) height, borderColor);
        }
        
        // Header
        guiGraphics.fill((int) x + 1, (int) y + 1, (int) (x + width - 1), (int) (y + headerHeight), color);
        
        // Title - hide if too small
        if (zoom > 0.3f) {
            guiGraphics.drawString(font, title, (int) x + 5, (int) y + 4, 0xFFFFFFFF, false); // Disabled shadow for performance
        }

        // Render Ports - Skip entirely if very zoomed out
        if (zoom > 0.2f) {
            // Render Inputs
            for (int i = 0; i < inputs.size(); i++) {
                renderPort(guiGraphics, font, inputs.get(i), (int) x, (int) (y + headerHeight + 10 + i * 15), true, connections, focusedNode, focusedPort, zoom);
            }

            // Render Outputs
            for (int i = 0; i < outputs.size(); i++) {
                renderPort(guiGraphics, font, outputs.get(i), (int) (x + width), (int) (y + headerHeight + 10 + i * 15), false, connections, focusedNode, focusedPort, zoom);
            }
        }
    }

    private int getPortColor(NodePort port) {
        switch (port.type) {
            case EXEC: return 0xFFFFFFFF;
            case STRING: return 0xFFFF5555;
            case FLOAT: return 0xFF55FF55;
            case BOOLEAN: return 0xFF5555FF;
            case LIST: return 0xFFFFFF55;
            case UUID: return 0xFFFF55FF;
            case ENUM: return 0xFFFFAA00;
            case ANY: return 0xFFAAAAAA;
            default: return port.color;
        }
    }

    private void renderPort(GuiGraphics guiGraphics, net.minecraft.client.gui.Font font, NodePort port, int px, int py, boolean isInput, List<GuiConnection> connections, GuiNode focusedNode, String focusedPort, float zoom) {
        int color = getPortColor(port);
        boolean isConnected = port.isConnected;
        
        // Port rendering (more "Unreal Engine" style)
        if (zoom > 0.3f) {
            float size = 4.0f;
            if (port.type == NodeDefinition.PortType.EXEC) {
                // Execution port: Arrow shape
                if (isConnected) {
                    // Filled arrow
                    renderTriangle(guiGraphics, px - size, py - size, px + size, py, px - size, py + size, color);
                } else {
                    // Outline arrow
                    renderTriangleOutline(guiGraphics, px - size, py - size, px + size, py, px - size, py + size, color);
                }
            } else {
                // Data port: Circle
                if (isConnected) {
                    // Filled circle
                    drawCircle(guiGraphics, px, py, (int)size, color);
                } else {
                    // Hollow circle
                    drawCircleOutline(guiGraphics, px, py, (int)size, color);
                }
            }
        } else if (zoom > 0.15f) {
            // Very simplified port: just a 2x2 colored square
            guiGraphics.fill(px - 1, py - 1, px + 1, py + 1, color);
        }

        // Port label - hide if zoomed out
        if (zoom > 0.4f) {
            if (isInput) {
                guiGraphics.drawString(font, port.displayName, px + 8, py - 1, 0xFFAAAAAA, false);
                
                if (port.hasInput && !isConnected && zoom > 0.6f) {
                    float inputX = px + 8 + font.width(port.displayName) + 2;
                    float inputY = py - 4;
                    float inputWidth = 50;
                    float inputHeight = 10;
                    
                    // Background
                    guiGraphics.fill((int)inputX, (int)inputY, (int)(inputX + inputWidth), (int)(inputY + inputHeight), 0x66000000);
                    
                    if (port.type == NodeDefinition.PortType.BOOLEAN) {
                        JsonElement val = inputValues.get(port.id);
                        boolean boolVal = val != null ? val.getAsBoolean() : (port.defaultValue instanceof Boolean ? (Boolean) port.defaultValue : false);
                        
                        // Checkbox style
                        int boxColor = boolVal ? 0xFF36CF36 : 0xFF333333;
                        guiGraphics.fill((int)inputX + 2, (int)inputY + 2, (int)inputX + 8, (int)inputY + 8, boxColor);
                        guiGraphics.renderOutline((int)inputX + 1, (int)inputY + 1, 8, 8, 0xFFFFFFFF);
                        
                        String text = boolVal ? "True" : "False";
                        guiGraphics.drawString(font, text, (int)inputX + 12, (int)inputY + 1, 0xFFCCCCCC, false);
                    } else if (port.options != null && port.options.length > 0) {
                        // Selection box style
                        guiGraphics.renderOutline((int)inputX, (int)inputY, (int)inputWidth, (int)inputHeight, 0xFFFFFFFF);
                        
                        JsonElement val = inputValues.get(port.id);
                        String text = val != null ? val.getAsString() : (port.defaultValue != null ? port.defaultValue.toString() : port.options[0]);
                        
                        // Draw selection arrow
                        guiGraphics.drawString(font, "v", (int)(inputX + inputWidth - 8), (int)inputY + 1, 0xFFAAAAAA, false);
                        
                        String renderText = text;
                        if (font.width(renderText) > inputWidth - 12) {
                            renderText = font.plainSubstrByWidth(renderText, (int)inputWidth - 15, true) + "..";
                        }
                        guiGraphics.drawString(font, renderText, (int)inputX + 2, (int)inputY + 1, 0xFFCCCCCC, false);
                    } else {
                        // Border if focused
                        boolean isFocused = focusedNode == this && focusedPort != null && focusedPort.equals(port.id);
                        guiGraphics.renderOutline((int)inputX, (int)inputY, (int)inputWidth, (int)inputHeight, isFocused ? 0xFFFFFFFF : 0x33FFFFFF);
                        
                        // Text
                        JsonElement val = inputValues.get(port.id);
                        String text = val != null ? val.getAsString() : (port.defaultValue != null ? port.defaultValue.toString() : "");
                        
                        String renderText = text;
                        // Truncate text if too long
                        if (font.width(renderText) > inputWidth - 4) {
                            renderText = "..." + font.plainSubstrByWidth(renderText, (int)inputWidth - 10, true);
                        }
                        
                        guiGraphics.drawString(font, renderText, (int)inputX + 2, (int)inputY + 1, 0xFFCCCCCC, false);
                    }
                }
            } else {
                guiGraphics.drawString(font, port.displayName, px - 8 - font.width(port.displayName), py - 1, 0xFFAAAAAA, false);
            }
        }
    }

    public boolean isMouseOverHeader(double worldMouseX, double worldMouseY) {
        return worldMouseX >= x && worldMouseX <= x + width && worldMouseY >= y && worldMouseY <= y + headerHeight;
    }

    private void renderTriangle(GuiGraphics guiGraphics, float x1, float y1, float x2, float y2, float x3, float y3, int color) {
        // For very small triangles (like ports), we can just use 3 lines or a few fills
        // But for filled triangles, scanline is correct. Let's keep it but optimize the edge calculation.
        float minY = Math.min(y1, Math.min(y2, y3));
        float maxY = Math.max(y1, Math.max(y2, y3));
        
        int iMinY = (int) Math.floor(minY);
        int iMaxY = (int) Math.ceil(maxY);
        
        for (int y = iMinY; y <= iMaxY; y++) {
            float minX = Float.MAX_VALUE;
            float maxX = -Float.MAX_VALUE;
            boolean intersected = false;

            // Manual edge intersection for speed
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

    private void renderTriangleOutline(GuiGraphics guiGraphics, float x1, float y1, float x2, float y2, float x3, float y3, int color) {
        drawLine(guiGraphics, x1, y1, x2, y2, color);
        drawLine(guiGraphics, x2, y2, x3, y3, color);
        drawLine(guiGraphics, x3, y3, x1, y1, color);
    }

    private void drawLine(GuiGraphics guiGraphics, float x1, float y1, float x2, float y2, int color) {
        // Use a single fill if it's almost horizontal or vertical
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

        // Use matrix transformation for rotated line
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(x1, y1);
        float angle = (float) Math.atan2(dy, dx);
        guiGraphics.pose().rotate(angle);
        guiGraphics.fill(0, 0, (int)len, 1, color);
        guiGraphics.pose().popMatrix();
    }

    private void drawCircle(GuiGraphics guiGraphics, float cx, float cy, int radius, int color) {
        // Optimization: For small radii, we can pre-calculate or use a very simple loop
        int r2 = radius * radius;
        for (int y = -radius; y <= radius; y++) {
            int xSpan = (int) Math.sqrt(r2 - y * y);
            guiGraphics.fill((int)(cx - xSpan), (int)(cy + y), (int)(cx + xSpan + 1), (int)(cy + y + 1), color);
        }
    }

    private void drawCircleOutline(GuiGraphics guiGraphics, float cx, float cy, int radius, int color) {
        // Optimization: Draw as a polygon with fewer segments
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

    public static class NodePort {
        public String id;
        public String displayName;
        public NodeDefinition.PortType type;
        public int color;
        public boolean isInput;
        public boolean hasInput;
        public Object defaultValue;
        public String[] options;
        public boolean isConnected = false;

        public NodePort(String id, String displayName, NodeDefinition.PortType type, int color, boolean isInput, boolean hasInput, Object defaultValue, String[] options) {
            this.id = id;
            this.displayName = displayName;
            this.type = type;
            this.color = color;
            this.isInput = isInput;
            this.hasInput = hasInput;
            this.defaultValue = defaultValue;
            this.options = options;
        }
    }
}
