package ltd.opens.mg.mc.client.gui.screens;

import ltd.opens.mg.mc.MaingraphforMC;
import ltd.opens.mg.mc.client.network.NetworkService;
import ltd.opens.mg.mc.client.gui.blueprint.*;
import ltd.opens.mg.mc.client.gui.blueprint.BlueprintSettingsPanel;

import ltd.opens.mg.mc.client.gui.blueprint.handler.*;
import ltd.opens.mg.mc.client.gui.blueprint.io.*;
import ltd.opens.mg.mc.client.gui.blueprint.render.*;
import ltd.opens.mg.mc.client.gui.components.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.*;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class BlueprintScreen extends Screen {
    private final Screen parent;
    private final String blueprintName;
    private final BlueprintState state = new BlueprintState();
    private final BlueprintEventHandler eventHandler;
    private boolean forceOpen = false;
    private long lastClickTime = 0;

    private final boolean isGlobalMode;

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
        this.isGlobalMode = Minecraft.getInstance().level == null;

        if (isSpecialBlueprint()) {
            state.readOnly = true;
            state.showNotification("Special Blueprint Mode: Read-Only");
        }

        if (forceOpen) {
            state.viewport.zoom = 0.5f; // "缩小" effect
        }

        // Request data
        if (blueprintName != null && !blueprintName.isEmpty()) {
            if (isGlobalMode) {
                try {
                    java.nio.file.Path path = ltd.opens.mg.mc.core.blueprint.BlueprintManager.getGlobalBlueprintsDir().resolve(blueprintName);
                    if (java.nio.file.Files.exists(path)) {
                        String json = new String(java.nio.file.Files.readAllBytes(path), java.nio.charset.StandardCharsets.UTF_8);
                        loadFromNetwork(json, -1);
                    }
                } catch (java.io.IOException e) {
                    MaingraphforMC.LOGGER.error("Failed to load global blueprint data: " + blueprintName, e);
                }
            } else {
                NetworkService.getInstance().requestBlueprintData(blueprintName);
            }
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
            BlueprintIO.loadFromString(json, state.nodes, state.connections, state.regions);
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
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // Do nothing to prevent default background blur/darkening in 1.21.1
        // We draw our own background in render() via BlueprintRenderer.drawGrid()
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        BlueprintRenderer.drawGrid(guiGraphics, this.width, this.height, state.viewport);

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(state.viewport.panX, state.viewport.panY, 0);
        guiGraphics.pose().scale(state.viewport.zoom, state.viewport.zoom, 1.0f);

        BlueprintRenderer.drawRegions(guiGraphics, state.regions, this.font, state.viewport);
        BlueprintRenderer.drawConnections(guiGraphics, state.connections, this.width, this.height, state.viewport);

        BlueprintRenderer.drawSnapGuides(guiGraphics, state);

        for (GuiNode node : state.nodes) {
            if (!state.viewport.isVisible(node.x, node.y, node.width, node.height, this.width, this.height)) {
                continue;
            }
            
            node.updateConnectedState(state.connections);
            int hTimer = (state.highlightedNode == node) ? state.highlightTimer : 0;
            node.render(guiGraphics, this.font, mouseX, mouseY, state.viewport, state.connections, state.focusedNode, state.focusedPort, state.editingMarkerNode == node, hTimer);

            // 如果该节点是当前 W 长按的目标节点，渲染进度条
            if (state.wPressProgress > 0 && state.selectedNodes.size() == 1 && state.selectedNodes.contains(node)) {
                BlueprintRenderer.drawNodeWProgressBar(guiGraphics, node, state.wPressProgress);
            }
        }

        if (state.connectionStartNode != null) {
            float[] startPos = state.connectionStartNode.getPortPositionByName(state.connectionStartPort, state.isConnectionFromInput);
            BlueprintRenderer.drawBezier(guiGraphics, startPos[0], startPos[1], state.viewport.toWorldX(mouseX), state.viewport.toWorldY(mouseY), 0x88FFFFFF, state.viewport.zoom);
        }

        guiGraphics.pose().popPose();

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
        renderCustomButton(guiGraphics, mouseX, mouseY, 5, 3, 40, 20, Component.translatable("gui.mgmc.blueprint_editor.back"), null);
        
        // Right side buttons
        int rightX = this.width - 5;
        
        // Help
        rightX -= 20;
        renderCustomButton(guiGraphics, mouseX, mouseY, rightX, 3, 20, 20, Component.literal("?"), "help");
        
        // Settings
        rightX -= 25;
        renderCustomButton(guiGraphics, mouseX, mouseY, rightX, 3, 20, 20, Component.literal("⚙"), "settings");

        // Reset View
        rightX -= 75;
        renderCustomButton(guiGraphics, mouseX, mouseY, rightX, 3, 70, 20, Component.translatable("gui.mgmc.blueprint_editor.reset_view"), "reset_view");
        
        // Arrange
        rightX -= 45;
        renderCustomButton(guiGraphics, mouseX, mouseY, rightX, 3, 40, 20, Component.translatable("gui.mgmc.blueprint_editor.auto_layout"), "auto_layout");
        
        // Save
        rightX -= 55;
        if (!state.readOnly) {
            renderCustomButton(guiGraphics, mouseX, mouseY, rightX, 3, 50, 20, Component.translatable("gui.mgmc.blueprint_editor.save"), "save");
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
            // ... (existing notification code)
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

        // --- Settings Menu ---
        BlueprintSettingsPanel.render(guiGraphics, this, state, font, mouseX, mouseY);
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

                        String json = BlueprintIO.serialize(state.nodes, state.connections, state.regions);
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

    private void renderCustomButton(GuiGraphics guiGraphics, int mouseX, int mouseY, int x, int y, int w, int h, Component label, String buttonId) {
        boolean hovered = isHovering(mouseX, mouseY, x, y, w, h);
        int bgColor = hovered ? 0xFF3D3D3D : 0x00000000; // Transparent background when not hovered
        int borderColor = 0xFF555555;
        int textColor = hovered ? 0xFFFFFFFF : 0xFFBBBBBB;

        // Save button highlight logic
        if ("save".equals(buttonId) && state.isDirty) {
            float pulse = (float)(Math.sin(System.currentTimeMillis() / 200.0) + 1.0) * 0.5f; // 0.0 to 1.0
            int alpha = 100 + (int)(pulse * 100); // 100 to 200
            
            // Greenish pulsing background
            bgColor = (alpha << 24) | 0x00AA00;
            if (hovered) {
                bgColor = (Math.min(255, alpha + 30) << 24) | 0x00CC00;
            }
            
            // Bright border
            borderColor = 0xFF00FF00;
            textColor = 0xFFFFFFFF;
        }

        if (bgColor != 0) {
            guiGraphics.fill(x, y, x + w, y + h, bgColor);
            guiGraphics.renderOutline(x, y, w, h, borderColor);
        }
        
        // Progress bar for long press
        if (buttonId != null && buttonId.equals(state.buttonLongPressTarget) && state.buttonLongPressProgress > 0) {
            int progressW = (int) (w * state.buttonLongPressProgress);
            guiGraphics.fill(x, y + h - 2, x + progressW, y + h, 0xFF55FF55);
        }

        Component text = label;
        int textW = font.width(text);
        guiGraphics.drawString(font, text, x + (w - textW) / 2, y + (h - 9) / 2, textColor, false);

        // Update long press state
        if (buttonId != null && buttonId.equals(state.buttonLongPressTarget)) {
            if (!hovered) {
                state.buttonLongPressTarget = null;
            }
        }
    }

    private void saveBlueprint() {
        if (state.readOnly) return;

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
            return;
        }

        String json = BlueprintIO.serialize(state.nodes, state.connections, state.regions);
        if (json != null) {
            if (isGlobalMode) {
                try {
                    java.nio.file.Path path = ltd.opens.mg.mc.core.blueprint.BlueprintManager.getGlobalBlueprintsDir().resolve(blueprintName);
                    java.nio.file.Files.write(path, json.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    state.showNotification(Component.translatable("gui.mgmc.notification.saved").getString());
                } catch (java.io.IOException e) {
                    MaingraphforMC.LOGGER.error("Failed to save global blueprint: " + blueprintName, e);
                }
            } else {
                NetworkService.getInstance().saveBlueprint(blueprintName, json, state.version);
            }
        }
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        state.buttonLongPressTarget = null;
        state.isMouseDown = false;
        int modifiers = (hasControlDown() ? 2 : 0) | (hasShiftDown() ? 1 : 0) | (hasAltDown() ? 4 : 0);
        MouseButtonEvent event = new MouseButtonEvent(mouseX, mouseY, new ButtonInfo(button, 0, modifiers));
        return eventHandler.mouseReleased(event, this) || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        CharacterEvent event = new CharacterEvent(codePoint, modifiers);
        return eventHandler.charTyped(event) || super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (hasControlDown()) {
            if (keyCode == GLFW.GLFW_KEY_S) {
                saveBlueprint();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_A) {
                state.selectedNodes.clear();
                for (GuiNode node : state.nodes) {
                    node.isSelected = true;
                    state.selectedNodes.add(node);
                }
                return true;
            }
        }
        KeyEvent event = new KeyEvent(keyCode, scanCode, 1, modifiers);
        return eventHandler.keyPressed(event, this) || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        KeyEvent event = new KeyEvent(keyCode, scanCode, 0, modifiers);
        return eventHandler.keyReleased(event, this) || super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        long currentTime = System.currentTimeMillis();
        boolean isDouble = (currentTime - lastClickTime) < 300;
        lastClickTime = currentTime;

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
        
        // Handle Settings Menu
        if (BlueprintSettingsPanel.mouseClicked(this, state, mouseX, mouseY, button)) {
            return true;
        }

        // Handle Top Bar Buttons
        if (mouseY < 26) {
            // Back
            if (isHovering((int)mouseX, (int)mouseY, 5, 3, 40, 20)) {
                onClose();
                return true;
            }
            
            int rightX = this.width - 5;
            
            // Help
            rightX -= 20;
            if (isHovering((int)mouseX, (int)mouseY, rightX, 3, 20, 20)) {
                if (button == 0) {
                    Minecraft.getInstance().setScreen(new BlueprintHelpScreen(this));
                }
                return true;
            }
            
            // Settings
            rightX -= 25;
            if (isHovering((int)mouseX, (int)mouseY, rightX, 3, 20, 20)) {
                state.showSettings = !state.showSettings;
                return true;
            }

            // Reset View
            rightX -= 75;
            if (isHovering((int)mouseX, (int)mouseY, rightX, 3, 70, 20)) {
                if (button == 0) {
                    state.buttonLongPressTarget = "reset_view";
                    state.buttonLongPressProgress = 0f;
                    state.isMouseDown = true;
                }
                return true;
            }

            // Auto Layout
            rightX -= 45;
            if (isHovering((int)mouseX, (int)mouseY, rightX, 3, 40, 20)) {
                if (button == 0) {
                    state.buttonLongPressTarget = "auto_layout";
                    state.buttonLongPressProgress = 0f;
                    state.isMouseDown = true;
                }
                return true;
            }
            
            // Save
            rightX -= 55;
            if (!state.readOnly && isHovering((int)mouseX, (int)mouseY, rightX, 3, 50, 20)) {
                saveBlueprint();
                return true;
            }
            
            return true; // Clicked on top bar but not on buttons
        }

        int modifiers = (hasControlDown() ? 2 : 0) | (hasShiftDown() ? 1 : 0) | (hasAltDown() ? 4 : 0);
        MouseButtonEvent event = new MouseButtonEvent(mouseX, mouseY, new ButtonInfo(button, 1, modifiers));
        return eventHandler.mouseClicked(event, isDouble, font, this) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        int modifiers = (hasControlDown() ? 2 : 0) | (hasShiftDown() ? 1 : 0) | (hasAltDown() ? 4 : 0);
        MouseButtonEvent event = new MouseButtonEvent(mouseX, mouseY, new ButtonInfo(button, 2, modifiers));
        return eventHandler.mouseDragged(event, dragX, dragY) || super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollHorizontal, double scrollVertical) {
        if (BlueprintSettingsPanel.mouseScrolled(this, state, mouseX, mouseY, scrollVertical)) {
            return true;
        }
        return eventHandler.mouseScrolled(mouseX, mouseY, scrollHorizontal, scrollVertical, this) || super.mouseScrolled(mouseX, mouseY, scrollHorizontal, scrollVertical);
    }
}
