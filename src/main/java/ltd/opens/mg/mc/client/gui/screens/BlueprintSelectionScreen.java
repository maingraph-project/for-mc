package ltd.opens.mg.mc.client.gui.screens;

import ltd.opens.mg.mc.MaingraphforMC;
import ltd.opens.mg.mc.client.gui.components.GuiContextMenu;
import ltd.opens.mg.mc.client.network.NetworkService;
import ltd.opens.mg.mc.client.gui.screens.InputModalScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;

public class BlueprintSelectionScreen extends Screen {
    private BlueprintList list;
    private EditBox newBlueprintName;
    private Button openButton;
    private Button createButton;
    private final GuiContextMenu contextMenu = new GuiContextMenu();
    private final boolean isGlobalMode;

    public BlueprintSelectionScreen() {
        super(Component.translatable("gui.mgmc.blueprint_selection.title"));
        this.isGlobalMode = Minecraft.getInstance().level == null;
    }

    @Override
    protected void init() {
        if (this.minecraft.player != null && !this.minecraft.player.isCreative()) {
            this.minecraft.setScreen(new AboutScreen(null));
            return;
        }

        // List area: 60px top margin (to accommodate Create box), 60px bottom margin
        this.list = new BlueprintList(this.minecraft, this.width, this.height - 120, 60, 24);
        
        this.addRenderableWidget(this.list);
        this.setFocused(this.list);

        // Create New Blueprint section at the top
        int createWidth = 200;
        int createHeight = 20;
        int createX = (this.width - createWidth - 60) / 2;
        int createY = 35;

        this.newBlueprintName = new EditBox(this.font, createX, createY, createWidth, createHeight, Component.translatable("gui.mgmc.blueprint_selection.new_name_label"));
        this.newBlueprintName.setMaxLength(128);
        this.newBlueprintName.setHint(Component.translatable("gui.mgmc.blueprint_selection.new_name_hint"));
        this.addRenderableWidget(this.newBlueprintName);

        this.createButton = Button.builder(Component.translatable("gui.mgmc.blueprint_selection.create"), b -> {
            String name = this.newBlueprintName.getValue().trim();
            if (!name.isEmpty()) {
                if (!name.endsWith(".json")) name += ".json";
                
                this.setFocused(null);
                if (isGlobalMode) {
                    try {
                        java.nio.file.Path path = ltd.opens.mg.mc.core.blueprint.BlueprintManager.getGlobalBlueprintsDir().resolve(name);
                        java.nio.file.Files.write(path, "{}".getBytes());
                        Minecraft.getInstance().setScreen(new BlueprintScreen(this, name));
                    } catch (java.io.IOException e) {
                        MaingraphforMC.LOGGER.error("Failed to create global blueprint: " + name, e);
                    }
                } else {
                    NetworkService.getInstance().saveBlueprint(name, "{}", -1);
                    Minecraft.getInstance().setScreen(new BlueprintScreen(this, name));
                }
            }
        }).bounds(createX + createWidth + 5, createY, 50, createHeight).build();
        this.addRenderableWidget(this.createButton);

        // Add action buttons at the bottom
        int buttonWidth = 100;
        int buttonHeight = 20;
        int spacing = 10;
        int totalWidth = (buttonWidth * 3) + (spacing * 2);
        int startX = (this.width - totalWidth) / 2;
        int buttonY = this.height - 40;

        this.openButton = Button.builder(Component.translatable("gui.mgmc.blueprint_selection.open"), b -> {
            if (this.list.getSelected() != null) {
                BlueprintEntry entry = this.list.getSelected();
                this.setFocused(null);
                // Version check will happen in BlueprintScreen.loadFromNetwork after receiving data from server
                Minecraft.getInstance().setScreen(new BlueprintScreen(this, entry.fullName));
            }
        }).bounds(startX, buttonY, buttonWidth, buttonHeight).build();
        
        this.addRenderableWidget(this.openButton);
        
        this.addRenderableWidget(Button.builder(Component.translatable("gui.mgmc.blueprint_selection.back"), b -> {
            this.setFocused(null);
            Minecraft.getInstance().setScreen(null);
        }).bounds(startX + buttonWidth + spacing, buttonY, buttonWidth, buttonHeight).build());
        
        this.addRenderableWidget(Button.builder(Component.translatable("gui.mgmc.blueprint_selection.refresh"), b -> {
            refreshFileList();
        }).bounds(startX + (buttonWidth + spacing) * 2, buttonY, buttonWidth, buttonHeight).build());

        // Mapping Button
        this.addRenderableWidget(Button.builder(Component.translatable("gui.mgmc.blueprint_selection.mapping"), b -> {
            this.setFocused(null);
            Minecraft.getInstance().setScreen(new BlueprintMappingScreen(this));
        }).bounds(this.width - 120, 10, 50, 20).build());

        // About Button (Top-Right)
        this.addRenderableWidget(Button.builder(Component.translatable("gui.mgmc.blueprint_selection.about"), b -> {
            this.setFocused(null);
            Minecraft.getInstance().setScreen(new AboutScreen(this));
        }).bounds(this.width - 60, 10, 50, 20).build());

        refreshFileList();
    }

    private void refreshFileList() {
        this.list.clearEntries();
        if (isGlobalMode) {
            try {
                java.nio.file.Path dir = ltd.opens.mg.mc.core.blueprint.BlueprintManager.getGlobalBlueprintsDir();
                java.util.List<String> files = new java.util.ArrayList<>();
                try (java.util.stream.Stream<java.nio.file.Path> stream = java.nio.file.Files.list(dir)) {
                    stream.filter(p -> !java.nio.file.Files.isDirectory(p) && p.toString().endsWith(".json"))
                          .map(p -> p.getFileName().toString())
                          .forEach(files::add);
                }
                java.util.Collections.sort(files);
                updateListFromServer(files);
            } catch (java.io.IOException e) {
                MaingraphforMC.LOGGER.error("Failed to list global blueprints", e);
            }
        } else {
            // Always request from server (works for both local and remote servers)
            NetworkService.getInstance().requestBlueprintList();
        }
    }

    public void updateListFromServer(List<String> blueprints) {
        this.list.clearEntries();
        for (String name : blueprints) {
            this.list.add(new BlueprintEntry(name));
        }
    }

    @Override
    public void tick() {
        this.openButton.active = this.list.getSelected() != null;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        // Draw title
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        
        if (this.list != null && this.list.children().isEmpty()) {
            guiGraphics.drawCenteredString(this.font, Component.translatable("gui.mgmc.blueprint_selection.no_blueprints"), this.width / 2, this.height / 2 - 10, 0xAAAAAA);
        }

        contextMenu.render(guiGraphics, this.font, mouseX, mouseY, this.width, this.height);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDouble) {
        if (contextMenu.mouseClicked(event.x(), event.y(), event.buttonInfo().button())) {
            return true;
        }
        return super.mouseClicked(event, isDouble);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        return super.charTyped(event);
    }

    class BlueprintList extends ObjectSelectionList<BlueprintEntry> {
        public BlueprintList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
            super(minecraft, width, height, y, itemHeight);
        }

        public void add(BlueprintEntry entry) {
            super.addEntry(entry);
        }

        @Override
        public int getRowWidth() {
            return 310;
        }

        @Override
        public int getRowLeft() {
            return BlueprintSelectionScreen.this.width / 2 - 155;
        }
    }

    class BlueprintEntry extends ObjectSelectionList.Entry<BlueprintEntry> {
        final String fullName;
        final String displayName;
        private long lastClickTime;

        public BlueprintEntry(String name) {
            this.fullName = name;
            this.displayName = name.endsWith(".json") ? name.substring(0, name.length() - 5) : name;
        }

        @Override
        public void renderContent(GuiGraphics guiGraphics, int index, int top, boolean isHovered, float partialTick) {
            int entryWidth = this.getWidth();
            int entryLeft = this.getX();
            int entryHeight = this.getHeight();
            
            // Use getY() if it's set, otherwise fallback to top
            int y = this.getY();
            if (y <= 0) y = top;
            
            // If it's still 0 or less, it might be collapsed, but with ObjectSelectionList 
            // the y should be managed by the list.
            
            String nameToRender = this.displayName;
            
            // Render background if selected or hovered
            if (this == BlueprintSelectionScreen.this.list.getSelected()) {
                guiGraphics.fill(entryLeft, y, entryLeft + entryWidth, y + entryHeight, 0x44FFFFFF);
                guiGraphics.renderOutline(entryLeft, y, entryWidth, entryHeight, 0xFFFFCC00);
            } else if (isHovered) {
                guiGraphics.fill(entryLeft, y, entryLeft + entryWidth, y + entryHeight, 0x22FFFFFF);
                guiGraphics.renderOutline(entryLeft, y, entryWidth, entryHeight, 0xFF888888);
            }

            int color = this == BlueprintSelectionScreen.this.list.getSelected() ? 0xFFFFCC00 : (isHovered ? 0xFFFFFFFF : 0xFFAAAAAA);
            guiGraphics.drawString(BlueprintSelectionScreen.this.font, nameToRender, entryLeft + 5, y + (entryHeight - 8) / 2, color);
        }

        @Override
        public Component getNarration() {
            return Component.literal(this.displayName);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean isDouble) {
            BlueprintSelectionScreen.this.list.setSelected(this);
            
            if (event.buttonInfo().button() == 1) { // Right click
                List<GuiContextMenu.MenuItem> menuItems = new ArrayList<>();
                
                menuItems.add(new GuiContextMenu.MenuItem(Component.translatable("gui.mgmc.blueprint_selection.open"), () -> {
                    BlueprintSelectionScreen.this.setFocused(null);
                    Minecraft.getInstance().setScreen(new BlueprintScreen(BlueprintSelectionScreen.this, this.fullName));
                }));

                menuItems.add(new GuiContextMenu.MenuItem(Component.translatable("gui.mgmc.blueprint_selection.rename"), () -> {
                    Minecraft.getInstance().setScreen(new InputModalScreen(
                        BlueprintSelectionScreen.this,
                        Component.translatable("gui.mgmc.blueprint_selection.rename").getString(),
                        this.displayName,
                        false,
                        newName -> {
                            if (!newName.isEmpty() && !newName.equals(this.displayName)) {
                                String finalNewName = newName.endsWith(".json") ? newName : newName + ".json";
                                if (isGlobalMode) {
                                    try {
                                        java.nio.file.Path dir = ltd.opens.mg.mc.core.blueprint.BlueprintManager.getGlobalBlueprintsDir();
                                        java.nio.file.Files.move(dir.resolve(this.fullName), dir.resolve(finalNewName));
                                        BlueprintSelectionScreen.this.refreshFileList();
                                    } catch (java.io.IOException e) {
                                        MaingraphforMC.LOGGER.error("Failed to rename global blueprint", e);
                                    }
                                } else {
                                    NetworkService.getInstance().renameBlueprint(this.fullName, finalNewName);
                                    BlueprintSelectionScreen.this.refreshFileList();
                                }
                            }
                        }
                    ));
                }));

                menuItems.add(new GuiContextMenu.MenuItem(Component.translatable("gui.mgmc.blueprint_selection.duplicate"), () -> {
                    Minecraft.getInstance().setScreen(new InputModalScreen(
                        BlueprintSelectionScreen.this,
                        Component.translatable("gui.mgmc.blueprint_selection.duplicate").getString(),
                        this.displayName + "_copy",
                        false,
                        newName -> {
                            if (!newName.isEmpty()) {
                                String finalNewName = newName.endsWith(".json") ? newName : newName + ".json";
                                if (isGlobalMode) {
                                    try {
                                        java.nio.file.Path dir = ltd.opens.mg.mc.core.blueprint.BlueprintManager.getGlobalBlueprintsDir();
                                        java.nio.file.Files.copy(dir.resolve(this.fullName), dir.resolve(finalNewName));
                                        BlueprintSelectionScreen.this.refreshFileList();
                                    } catch (java.io.IOException e) {
                                        MaingraphforMC.LOGGER.error("Failed to duplicate global blueprint", e);
                                    }
                                } else {
                                    NetworkService.getInstance().duplicateBlueprint(this.fullName, finalNewName);
                                    BlueprintSelectionScreen.this.refreshFileList();
                                }
                            }
                        }
                    ));
                }));

                menuItems.add(new GuiContextMenu.MenuItem(Component.translatable("gui.mgmc.blueprint_selection.delete"), () -> {
                    Minecraft.getInstance().setScreen(new InputModalScreen(
                        BlueprintSelectionScreen.this,
                        Component.translatable("gui.mgmc.blueprint_selection.delete").getString() + ": " + this.displayName,
                        "",
                        false,
                        new String[]{
                            Component.translatable("gui.mgmc.modal.confirm").getString(),
                            Component.translatable("gui.mgmc.modal.cancel").getString()
                        },
                        InputModalScreen.Mode.SELECTION,
                        choice -> {
                            if (choice.equals(Component.translatable("gui.mgmc.modal.confirm").getString())) {
                                if (isGlobalMode) {
                                    try {
                                        java.nio.file.Path dir = ltd.opens.mg.mc.core.blueprint.BlueprintManager.getGlobalBlueprintsDir();
                                        java.nio.file.Files.deleteIfExists(dir.resolve(this.fullName));
                                        BlueprintSelectionScreen.this.refreshFileList();
                                    } catch (java.io.IOException e) {
                                        MaingraphforMC.LOGGER.error("Failed to delete global blueprint", e);
                                    }
                                } else {
                                    NetworkService.getInstance().deleteBlueprint(this.fullName);
                                    BlueprintSelectionScreen.this.refreshFileList();
                                }
                            }
                        }
                    ));
                }, 0xFFFF5555));

                BlueprintSelectionScreen.this.contextMenu.show(event.x(), event.y(), menuItems);
                return true;
            }

            long now = System.currentTimeMillis();
            if (now - lastClickTime < 250L) {
                // Double click
                BlueprintSelectionScreen.this.setFocused(null);
                Minecraft.getInstance().setScreen(new BlueprintScreen(BlueprintSelectionScreen.this, this.fullName));
                return true;
            }
            lastClickTime = now;
            
            return true;
        }
    }
}
