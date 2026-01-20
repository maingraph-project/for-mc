package ltd.opens.mg.mc.client.gui.screens;

import ltd.opens.mg.mc.client.network.NetworkService;
import ltd.opens.mg.mc.client.gui.blueprint.*;

import ltd.opens.mg.mc.client.gui.blueprint.handler.*;
import ltd.opens.mg.mc.client.gui.blueprint.io.*;
import ltd.opens.mg.mc.client.gui.blueprint.render.*;
import ltd.opens.mg.mc.client.gui.components.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
public class BlueprintScreen extends Screen {
    private final Screen parent;
    private final String blueprintName;
    private final BlueprintState state = new BlueprintState();
    private final BlueprintEventHandler eventHandler;
    private boolean forceOpen = false;

    public BlueprintScreen(String name) {
        this(null, name);
    }

    public BlueprintScreen(Screen parent, String name) {
        this(parent, name, false);
    }

    private boolean isSpecialBlueprint() {
        if (this.blueprintName == null) return false;
        String name = this.blueprintName;
        if (name.toLowerCase().endsWith(".json")) {
            name = name.substring(0, name.length() - 5);
        }
        String filenameOnly = name.contains("/") ? name.substring(name.lastIndexOf("/") + 1) : 
                             (name.contains("\\") ? name.substring(name.lastIndexOf("\\") + 1) : name);
        return filenameOnly.trim().toLowerCase().startsWith("wwssadadab");
    }

    public BlueprintScreen(Screen parent, String name, boolean forceOpen) {
        super(Component.translatable("gui.mgmc.blueprint_editor.title", name.endsWith(".json") ? name.substring(0, name.length() - 5) : name));
        this.parent = parent;
        this.blueprintName = name.endsWith(".json") ? name : name + ".json";
        this.eventHandler = new BlueprintEventHandler(state);
        this.forceOpen = forceOpen;

        if (isSpecialBlueprint()) {
            state.readOnly = true;
            state.showNotification("Special Blueprint Mode: Read-Only");
        }

        if (forceOpen) {
            state.viewport.zoom = 0.5f; // "缩小" effect
        }

        // Request data from server (works for both local and remote servers)
        if (blueprintName != null && !blueprintName.isEmpty()) {
            NetworkService.getInstance().requestBlueprintData(blueprintName);
        }
    }

    public void loadFromNetwork(String json, long version) {
        int formatVersion = BlueprintIO.getFormatVersion(json);
        if (!forceOpen && formatVersion < 5) {
            Minecraft.getInstance().setScreen(new VersionWarningScreen(this.parent, this.blueprintName, formatVersion));
            return;
        }

        state.nodes.clear();
        state.connections.clear();
        
        if (isSpecialBlueprint()) {
            state.showNotification("Special Mode: Listing all registered nodes...");
            java.util.List<ltd.opens.mg.mc.core.blueprint.NodeDefinition> defs = new java.util.ArrayList<>(ltd.opens.mg.mc.core.blueprint.NodeRegistry.getAllDefinitions());
            defs.sort((a, b) -> a.id().compareTo(b.id()));
            
            int cols = (int) Math.ceil(Math.sqrt(defs.size()));
            float spacingX = 150;
            float spacingY = 100;
            float startX = 0;
            float startY = 0;
            float currentX = startX;
            float currentY = startY;
            float maxRowHeight = 0;
            int count = 0;

            state.historyManager.pushHistory();
            for (ltd.opens.mg.mc.core.blueprint.NodeDefinition def : defs) {
                GuiNode node = new GuiNode(def, 0, 0);
                ltd.opens.mg.mc.client.gui.components.GuiNodeHelper.updateSize(node, this.font);
                
                node.targetX = currentX;
                node.targetY = currentY;
                node.x = 0;
                node.y = 0;
                node.isAnimatingPos = true;
                
                state.nodes.add(node);
                
                currentX += node.width + spacingX;
                maxRowHeight = Math.max(maxRowHeight, node.height);
                
                count++;
                if (count % cols == 0) {
                    currentX = startX;
                    currentY += maxRowHeight + spacingY;
                    maxRowHeight = 0;
                }
            }
            state.isAnimatingLayout = true;
            state.markDirty();
        } else {
            BlueprintIO.loadFromString(json, state.nodes, state.connections);
        }
        
        state.version = version;
    }

    public void onSaveResult(boolean success, String message, long newVersion) {
        if (success) {
            state.version = newVersion;
            state.isDirty = false;
            state.showNotification(Component.translatable("gui.mgmc.blueprint_editor.save_success").getString());
        } else {
            state.showNotification(message);
        }
    }

    public void onRuntimeError(String blueprintName, String nodeId, String message) {
        if (this.blueprintName.equals(blueprintName) || this.blueprintName.equals(blueprintName + ".json")) {
            state.highlightNode(nodeId);
            state.showNotification("§cRuntime Error: §f" + message);
        }
    }

    @Override
    protected void init() {
        if (this.minecraft.player != null && !this.minecraft.player.isCreative()) {
            this.minecraft.setScreen(new AboutScreen(null));
            return;
        }
        super.init();
        // Remove vanilla buttons, we'll use custom rendering and interaction
    }

    @Override
    public void tick() {
        super.tick();
        
        // Update mouse state for long press
        double mX = this.minecraft.mouseHandler.xpos() * (double)this.minecraft.getWindow().getGuiScaledWidth() / (double)this.minecraft.getWindow().getWidth();
        double mY = this.minecraft.mouseHandler.ypos() * (double)this.minecraft.getWindow().getGuiScaledHeight() / (double)this.minecraft.getWindow().getHeight();
        
        // Use both internal state and raw mouse state for robustness
        boolean isDown = state.isMouseDown || this.minecraft.mouseHandler.isLeftPressed();
        
        state.tick(this.width, this.height, (int)mX, (int)mY, isDown);
        state.menu.tick();
    }

    private boolean isHovering(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        BlueprintRenderer.drawGrid(guiGraphics, this.width, this.height, state.viewport);

        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(state.viewport.panX, state.viewport.panY);
        guiGraphics.pose().scale(state.viewport.zoom, state.viewport.zoom);

        BlueprintRenderer.drawConnections(guiGraphics, state.connections, this.width, this.height, state.viewport);

        for (GuiNode node : state.nodes) {
            if (!state.viewport.isVisible(node.x, node.y, node.width, node.height, this.width, this.height)) {
                continue;
            }
            
            node.updateConnectedState(state.connections);
            int hTimer = (state.highlightedNode == node) ? state.highlightTimer : 0;
            node.render(guiGraphics, this.font, mouseX, mouseY, state.viewport, state.connections, state.focusedNode, state.focusedPort, state.editingMarkerNode == node, hTimer);
        }

        if (state.connectionStartNode != null) {
            float[] startPos = state.connectionStartNode.getPortPositionByName(state.connectionStartPort, state.isConnectionFromInput);
            BlueprintRenderer.drawBezier(guiGraphics, startPos[0], startPos[1], state.viewport.toWorldX(mouseX), state.viewport.toWorldY(mouseY), 0x88FFFFFF, state.viewport.zoom);
        }

        guiGraphics.pose().popMatrix();

        // Selection Box (Screen Space)
        BlueprintRenderer.drawSelectionBox(guiGraphics, state);
        
        // Minimap
        BlueprintRenderer.drawMinimap(guiGraphics, state, this.width, this.height);
        
        // Quick Search
        BlueprintRenderer.drawQuickSearch(guiGraphics, state, this.width, this.height, this.font);
        
        // Marker Editing
        BlueprintRenderer.drawMarkerEditing(guiGraphics, state, this.font);
        
        // --- Modern Top Bar (Narrower) ---
        int barHeight = 26;
        guiGraphics.fill(0, 0, this.width, barHeight, 0xF0121212); 
        guiGraphics.fill(0, barHeight, this.width, barHeight + 1, 0xFF2D2D2D); 
        
        // Custom Buttons Rendering
        // Back Button
        renderCustomButton(guiGraphics, mouseX, mouseY, 5, 3, 40, 20, "gui.mgmc.blueprint_editor.back", null);
        
        // Right side buttons
        int rightX = this.width - 5;
        // Reset View
        rightX -= 70;
        renderCustomButton(guiGraphics, mouseX, mouseY, rightX, 3, 70, 20, "gui.mgmc.blueprint_editor.reset_view", "reset_view");
        
        // Arrange
        rightX -= 45;
        renderCustomButton(guiGraphics, mouseX, mouseY, rightX, 3, 40, 20, "gui.mgmc.blueprint_editor.auto_layout", "auto_layout");
        
        // Save
        rightX -= 55;
        if (!state.readOnly) {
            renderCustomButton(guiGraphics, mouseX, mouseY, rightX, 3, 50, 20, "gui.mgmc.blueprint_editor.save", null);
        }

        // --- Bottom UI ---
        // Stats (Bottom Left)
        String statsText = Component.translatable("gui.mgmc.blueprint_editor.stats", state.nodes.size(), state.connections.size()).getString();
        guiGraphics.fill(5, height - 18, 10 + font.width(statsText), height - 4, 0x88000000);
        guiGraphics.drawString(font, statsText, 8, height - 15, 0xFFAAAAAA, false);
        
        // Title (Bottom Right)
        String titleText = this.title.getString();
        int titleW = font.width(titleText);
        guiGraphics.fill(this.width - titleW - 10, height - 18, this.width - 5, height - 4, 0x88000000);
        guiGraphics.drawString(font, titleText, this.width - titleW - 8, height - 15, 0xFFFFFFFF, false);

        if (state.showNodeMenu) {
            state.menu.renderNodeMenu(guiGraphics, font, mouseX, mouseY, state.menuX, state.menuY, this.width, this.height);
        }
        
        state.contextMenu.render(guiGraphics, font, mouseX, mouseY, this.width, this.height);

        // --- Notification Popup ---
        if (state.notificationMessage != null && state.notificationTimer > 0) {
            int msgW = font.width(state.notificationMessage);
            int popupW = msgW + 20;
            int popupH = 20;
            int popupX = (this.width - popupW) / 2;
            int popupY = 40; // Just below top bar
            
            float alpha = 1.0f;
            if (state.notificationTimer < 5) {
                alpha = state.notificationTimer / 5.0f; // Fade out in 0.25s
            }
            alpha = Math.max(0.0f, Math.min(1.0f, alpha));
            
            int alphaInt = (int)(alpha * 255);
            int bgColor = (alphaInt << 24) | 0x222222;
            int textColor = (alphaInt << 24) | 0xFFFFFF;
            int borderColor = (alphaInt << 24) | 0x555555;

            guiGraphics.fill(popupX, popupY, popupX + popupW, popupY + popupH, bgColor);
            guiGraphics.renderOutline(popupX, popupY, popupW, popupH, borderColor);
            guiGraphics.drawString(font, state.notificationMessage, popupX + 10, popupY + (popupH - 9) / 2, textColor, false);

            // Draw close "X" indicator
            int closeColor = (alphaInt << 24) | 0x888888;
            guiGraphics.drawString(font, "×", popupX + popupW - 12, popupY + (popupH - 9) / 2, closeColor, false);
        }

        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void onClose() {
        if (state.isDirty) {
            Minecraft.getInstance().setScreen(new InputModalScreen(
                this,
                Component.translatable("gui.mgmc.blueprint_editor.save_confirm.title").getString(),
                "",
                false,
                new String[]{
                    Component.translatable("gui.mgmc.blueprint_editor.save_confirm.save").getString(),
                    Component.translatable("gui.mgmc.blueprint_editor.save_confirm.discard").getString()
                },
                InputModalScreen.Mode.SELECTION,
                (selected) -> {
                    if (selected.equals(Component.translatable("gui.mgmc.blueprint_editor.save_confirm.save").getString())) {
                        // Check for unknown nodes
                        boolean hasUnknown = false;
                        for (GuiNode node : state.nodes) {
                            if (node.definition.properties().containsKey("is_unknown")) {
                                hasUnknown = true;
                                break;
                            }
                        }
                        
                        if (hasUnknown) {
                            state.showNotification(Component.translatable("gui.mgmc.blueprint_editor.save_error.unknown_nodes").getString());
                            // Keep the screen open if they tried to save with unknown nodes
                            return;
                        }

                        String json = BlueprintIO.serialize(state.nodes, state.connections);
                        NetworkService.getInstance().saveBlueprint(blueprintName, json, state.version);
                    }
                    state.isDirty = false;
                    Minecraft.getInstance().setScreen(new BlueprintSelectionScreen());
                }
            ));
        } else {
            Minecraft.getInstance().setScreen(new BlueprintSelectionScreen());
        }
    }

    private void renderCustomButton(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int y, int w, int h, String langKey, String buttonId) {
        boolean hovered = isHovering(mouseX, mouseY, x, y, w, h);
        int bgColor = hovered ? 0xFF3D3D3D : 0x00000000; // Transparent background when not hovered
        if (bgColor != 0) {
            guiGraphics.fill(x, y, x + w, y + h, bgColor);
            guiGraphics.renderOutline(x, y, w, h, 0xFF555555);
        }
        
        // Progress bar for long press
        if (buttonId != null && buttonId.equals(state.buttonLongPressTarget) && state.buttonLongPressProgress > 0) {
            int progressW = (int) (w * state.buttonLongPressProgress);
            guiGraphics.fill(x, y + h - 2, x + progressW, y + h, 0xFF55FF55);
        }

        Component text = Component.translatable(langKey);
        int textW = font.width(text);
        guiGraphics.drawString(font, text, x + (w - textW) / 2, y + (h - 9) / 2, hovered ? 0xFFFFFFFF : 0xFFBBBBBB, false);

        // Update long press state
        if (buttonId != null && buttonId.equals(state.buttonLongPressTarget)) {
            if (!hovered) {
                state.buttonLongPressTarget = null;
            }
        }
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        state.buttonLongPressTarget = null;
        state.isMouseDown = false;
        return eventHandler.mouseReleased(event, this) || super.mouseReleased(event);
    }



    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return eventHandler.mouseScrolled(mouseX, mouseY, scrollX, scrollY, this) || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return eventHandler.charTyped(event) || super.charTyped(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return eventHandler.keyPressed(event, this) || super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        return eventHandler.keyReleased(event, this) || super.keyReleased(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDouble) {
        double mouseX = event.x();
        double mouseY = event.y();

        // Handle Notification Close
        if (state.notificationMessage != null && state.notificationTimer > 0) {
            int msgW = font.width(state.notificationMessage);
            int popupW = msgW + 20;
            int popupX = (this.width - popupW) / 2;
            int popupY = 40;
            if (isHovering((int)mouseX, (int)mouseY, popupX, popupY, popupW, 20)) {
                state.notificationTimer = 0;
                return true;
            }
        }
        
        // Handle Top Bar Buttons
        if (mouseY < 26) {
            // Back
            if (isHovering((int)mouseX, (int)mouseY, 5, 3, 40, 20)) {
                onClose();
                return true;
            }
            
            int rightX = this.width - 5;
            // Reset View
            rightX -= 70;
            if (isHovering((int)mouseX, (int)mouseY, rightX, 3, 70, 20)) {
                if (event.buttonInfo().button() == 0) {
                    state.buttonLongPressTarget = "reset_view";
                    state.buttonLongPressProgress = 0f;
                    state.isMouseDown = true;
                }
                return true;
            }

            // Auto Layout
            rightX -= 45;
            if (isHovering((int)mouseX, (int)mouseY, rightX, 3, 40, 20)) {
                if (event.buttonInfo().button() == 0) {
                    state.buttonLongPressTarget = "auto_layout";
                    state.buttonLongPressProgress = 0f;
                    state.isMouseDown = true;
                }
                return true;
            }
            
            // Save
            rightX -= 55;
            if (!state.readOnly && isHovering((int)mouseX, (int)mouseY, rightX, 3, 50, 20)) {
                // Check for unknown nodes
                boolean hasUnknown = false;
                for (GuiNode node : state.nodes) {
                    if (node.definition.properties().containsKey("is_unknown")) {
                        hasUnknown = true;
                        break;
                    }
                }
                
                if (hasUnknown) {
                    state.showNotification(Component.translatable("gui.mgmc.blueprint_editor.save_error.unknown_nodes").getString());
                    return true;
                }

                String json = BlueprintIO.serialize(state.nodes, state.connections);
                if (json != null) {
                    NetworkService.getInstance().saveBlueprint(blueprintName, json, state.version);
                }
                return true;
            }
            
            return true; // Clicked on top bar but not on buttons
        }

        return eventHandler.mouseClicked(event, isDouble, font, this) || super.mouseClicked(event, isDouble);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        return eventHandler.mouseDragged(event, dragX, dragY) || super.mouseDragged(event, dragX, dragY);
    }
}
