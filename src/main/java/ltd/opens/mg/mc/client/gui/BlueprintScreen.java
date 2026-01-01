package ltd.opens.mg.mc.client.gui;

import ltd.opens.mg.mc.MaingraphforMCClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import java.nio.file.Path;

public class BlueprintScreen extends Screen {
    private final Path dataFile;
    private final BlueprintState state = new BlueprintState();
    private final BlueprintEventHandler eventHandler;

    public BlueprintScreen() {
        super(Component.literal("Blueprint Editor"));
        this.dataFile = MaingraphforMCClient.getBlueprintPath();
        this.eventHandler = new BlueprintEventHandler(state);
        BlueprintIO.load(this.dataFile, state.nodes, state.connections);
    }

    @Override
    protected void init() {
        super.init();
        this.addRenderableWidget(Button.builder(Component.literal("Save"), (btn) -> BlueprintIO.save(this.dataFile, state.nodes, state.connections))
            .bounds(5, 5, 50, 20).build());
        this.addRenderableWidget(Button.builder(Component.literal("Reset View"), (btn) -> state.resetView()).bounds(60, 5, 80, 20).build());
    }

    @Override
    public void tick() {
        super.tick();
        state.cursorTick++;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        BlueprintRenderer.drawGrid(guiGraphics, this.width, this.height, state.panX, state.panY, state.zoom);

        // Pre-calculate connection states only once if needed, or just less frequently
        // For now, let's just make sure we don't do it inside the scaled pose if not needed
        // but it's currently inside the node loop which is fine if culling works.

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(state.panX, state.panY);
        guiGraphics.pose().scale(state.zoom, state.zoom);

        BlueprintRenderer.drawConnections(guiGraphics, state.connections, this.width, this.height, state.panX, state.panY, state.zoom);

        for (GuiNode node : state.nodes) {
            // Culling for nodes
            float sX = node.x * state.zoom + state.panX;
            float sY = node.y * state.zoom + state.panY;
            float sW = node.width * state.zoom;
            float sH = node.height * state.zoom;
            
            if (sX + sW < 0 || sX > this.width || sY + sH < 0 || sY > this.height) {
                continue;
            }
            
            // Only update if something changed? For now, the bottleneck was drawLine.
            // But let's keep it optimized.
            node.updateConnectedState(state.connections);
            node.render(guiGraphics, this.font, mouseX, mouseY, state.panX, state.panY, state.zoom, state.connections, state.focusedNode, state.focusedPort);
        }

        if (state.connectionStartNode != null) {
            float[] startPos = state.connectionStartNode.getPortPositionByName(state.connectionStartPort, state.isConnectionFromInput);
            BlueprintRenderer.drawBezier(guiGraphics, startPos[0], startPos[1], (float) ((mouseX - state.panX) / state.zoom), (float) ((mouseY - state.panY) / state.zoom), 0x88FFFFFF, state.zoom);
        }

        guiGraphics.pose().popMatrix();

        // UI Overlay
        guiGraphics.drawString(font, "Nodes: " + state.nodes.size() + " | Connections: " + state.connections.size(), 5, height - 15, 0xFFAAAAAA, false);
        guiGraphics.drawString(font, "Right click to add. Drag ports to connect. DEL to delete.", 150, 10, 0xFF888888, false);

        if (state.showNodeMenu) {
            state.menu.renderNodeMenu(guiGraphics, font, mouseX, mouseY, state.menuX, state.menuY);
        }
        
        if (state.showNodeContextMenu) {
            state.menu.renderNodeContextMenu(guiGraphics, font, mouseX, mouseY, state.menuX, state.menuY);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return eventHandler.mouseScrolled(mouseX, mouseY, scrollX, scrollY) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return super.charTyped(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return eventHandler.keyPressed(event) || super.keyPressed(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDouble) {
        return eventHandler.mouseClicked(event, isDouble, font, this) || super.mouseClicked(event, isDouble);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        return eventHandler.mouseReleased(event) || super.mouseReleased(event);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        return eventHandler.mouseDragged(event, dragX, dragY) || super.mouseDragged(event, dragX, dragY);
    }
}
