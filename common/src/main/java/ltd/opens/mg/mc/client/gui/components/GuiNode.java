package ltd.opens.mg.mc.client.gui.components;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.client.gui.blueprint.Viewport;
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
    public float targetX, targetY;
    public boolean isAnimatingPos = false;
    public int color;
    public float width = 120;
    public float height = 60;
    public float headerHeight = 15;
    public JsonObject inputValues = new JsonObject();
    public boolean isSelected = false;

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
        this.targetX = x;
        this.targetY = y;
        this.color = def.color();
        
        for (NodeDefinition.PortDefinition p : def.inputs()) {
            addInput(p.id(), p.displayName(), p.type(), p.color(), p.hasInput(), p.defaultValue(), p.options());
        }
        for (NodeDefinition.PortDefinition p : def.outputs()) {
            addOutput(p.id(), p.displayName(), p.type(), p.color());
        }
    }

    public void addInput(String id, String displayName, NodeDefinition.PortType type, int color, boolean hasInput, Object defaultValue, String[] options) {
        GuiNodePortManager.addInput(this, id, displayName, type, color, hasInput, defaultValue, options);
    }

    public void addOutput(String id, String displayName, NodeDefinition.PortType type, int color) {
        GuiNodePortManager.addOutput(this, id, displayName, type, color);
    }

    public void markSizeDirty() {
        this.sizeDirty = true;
    }

    public void setSizeDirty(boolean dirty) {
        this.sizeDirty = dirty;
    }

    public float[] getPortPositionByName(String id, boolean isInput) {
        return GuiNodePortManager.getPortPositionByName(this, id, isInput);
    }

    public NodePort getPortByName(String id, boolean isInput) {
        return GuiNodePortManager.getPortByName(this, id, isInput);
    }

    public float[] getPortPosition(int index, boolean isInput) {
        return GuiNodePortManager.getPortPosition(this, index, isInput);
    }

    public void updateConnectedState(List<GuiConnection> connections) {
        GuiNodePortManager.updateConnectedState(this, connections);
    }

    public void render(GuiGraphics guiGraphics, net.minecraft.client.gui.Font font, int mouseX, int mouseY, Viewport viewport, List<GuiConnection> connections, GuiNode focusedNode, String focusedPort, boolean isEditing, int highlightTimer) {
        if (sizeDirty) {
            GuiNodeHelper.updateSize(this, font);
        }
        GuiNodeRenderer.render(this, guiGraphics, font, mouseX, mouseY, viewport, connections, focusedNode, focusedPort, isEditing, highlightTimer);
    }

    public boolean isDynamicPort(NodePort port) {
        return GuiNodeHelper.isDynamicPort(this, port);
    }

    public boolean isMouseOverHeader(double worldMouseX, double worldMouseY) {
        return GuiNodeHelper.isMouseOverHeader(this, worldMouseX, worldMouseY);
    }

    public String getRemovePortAt(double worldMouseX, double worldMouseY, net.minecraft.client.gui.Font font) {
        return GuiNodeHelper.getRemovePortAt(this, worldMouseX, worldMouseY, font);
    }

    public boolean isMouseOverAddButton(double worldMouseX, double worldMouseY) {
        return GuiNodeHelper.isMouseOverAddButton(this, worldMouseX, worldMouseY);
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
