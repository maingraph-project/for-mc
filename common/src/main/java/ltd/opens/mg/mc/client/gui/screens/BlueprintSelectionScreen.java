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

        // Import Button
        this.addRenderableWidget(Button.builder(Component.translatable("gui.mgmc.blueprint_selection.import"), b -> {
            handleImport();
        }).bounds(this.width - 180, 10, 50, 20).build());

        // About Button (Top-Right)
        this.addRenderableWidget(Button.builder(Component.translatable("gui.mgmc.blueprint_selection.about"), b -> {
            this.setFocused(null);
            Minecraft.getInstance().setScreen(new AboutScreen(this));
        }).bounds(this.width - 60, 10, 50, 20).build());

        refreshFileList();
    }

    private void refreshFileList() {
        this.list.clear();
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
        this.list.clear();
        for (String name : blueprints) {
            this.list.add(new BlueprintEntry(name));
        }
    }

    public void handleExportResponse(String name, String data, java.util.Map<String, java.util.Set<String>> mappings) {
        try {
            java.nio.file.Path exportDir = java.nio.file.Paths.get("mgmc_blueprints/exports");
            java.nio.file.Files.createDirectories(exportDir);
            
            com.google.gson.JsonObject exportObj = new com.google.gson.JsonObject();
            exportObj.addProperty("mgmc_bp_version", 1);
            exportObj.addProperty("blueprint_name", name);
            exportObj.addProperty("blueprint_data", data);
            
            com.google.gson.JsonObject mappingsObj = new com.google.gson.JsonObject();
            mappings.forEach((id, bps) -> {
                com.google.gson.JsonArray array = new com.google.gson.JsonArray();
                bps.forEach(array::add);
                mappingsObj.add(id, array);
            });
            exportObj.add("mappings", mappingsObj);
            
            String fileName = (name.endsWith(".json") ? name.substring(0, name.length() - 5) : name) + ".mgmcbp";
            java.nio.file.Path exportFile = exportDir.resolve(fileName);
            java.nio.file.Files.writeString(exportFile, new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(exportObj));
            
            // Notify user
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.displayClientMessage(net.minecraft.network.chat.Component.translatable("gui.mgmc.blueprint_selection.export_success", exportFile.toString()), false);
            } else {
                MaingraphforMC.LOGGER.info("Exported blueprint to: {}", exportFile);
            }
        } catch (Exception e) {
            MaingraphforMC.LOGGER.error("Failed to save export file", e);
        }
    }

    private void handleImport() {
        try {
            java.nio.file.Path importDir = java.nio.file.Paths.get("mgmc_blueprints/imports");
            if (!java.nio.file.Files.exists(importDir)) {
                java.nio.file.Files.createDirectories(importDir);
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.displayClientMessage(net.minecraft.network.chat.Component.translatable("gui.mgmc.blueprint_selection.import_dir_created", importDir.toString()), false);
                }
                return;
            }

            java.util.List<java.nio.file.Path> files = new java.util.ArrayList<>();
            try (java.util.stream.Stream<java.nio.file.Path> stream = java.nio.file.Files.list(importDir)) {
                stream.filter(p -> p.toString().endsWith(".mgmcbp")).forEach(files::add);
            }

            if (files.isEmpty()) {
                if (Minecraft.getInstance().player != null) {
                    Minecraft.getInstance().player.displayClientMessage(net.minecraft.network.chat.Component.translatable("gui.mgmc.blueprint_selection.import_no_files", importDir.toString()), false);
                }
                return;
            }

            // Show a simple list for selection
            List<String> fileNames = files.stream().map(p -> p.getFileName().toString()).toList();
            Minecraft.getInstance().setScreen(new InputModalScreen(
                this,
                Component.translatable("gui.mgmc.blueprint_selection.import").getString(),
                "",
                false,
                fileNames.toArray(new String[0]),
                InputModalScreen.Mode.SELECTION,
                selectedFileName -> {
                    if (selectedFileName != null && !selectedFileName.isEmpty()) {
                        performImport(importDir.resolve(selectedFileName));
                    }
                }
            ));
        } catch (Exception e) {
            MaingraphforMC.LOGGER.error("Failed to list import files", e);
        }
    }

    private void performImport(java.nio.file.Path path) {
        try {
            String content = java.nio.file.Files.readString(path);
            com.google.gson.JsonObject obj = com.google.gson.JsonParser.parseString(content).getAsJsonObject();
            
            String name = obj.get("blueprint_name").getAsString();
            String data = obj.get("blueprint_data").getAsString();
            
            java.util.Map<String, java.util.Set<String>> mappings = new java.util.HashMap<>();
            if (obj.has("mappings")) {
                com.google.gson.JsonObject mappingsObj = obj.getAsJsonObject("mappings");
                mappingsObj.entrySet().forEach(entry -> {
                    java.util.Set<String> set = new java.util.HashSet<>();
                    entry.getValue().getAsJsonArray().forEach(e -> set.add(e.getAsString()));
                    mappings.put(entry.getKey(), set);
                });
            }

            if (isGlobalMode) {
                // Save blueprint
                java.nio.file.Path bpDir = ltd.opens.mg.mc.core.blueprint.BlueprintManager.getGlobalBlueprintsDir();
                java.nio.file.Files.writeString(bpDir.resolve(name), data);
                
                // Save mappings
                java.nio.file.Path mappingsPath = java.nio.file.Paths.get("mgmc_blueprints/.routing/mappings.json");
                com.google.gson.JsonObject currentMappings = new com.google.gson.JsonObject();
                if (java.nio.file.Files.exists(mappingsPath)) {
                    currentMappings = com.google.gson.JsonParser.parseString(java.nio.file.Files.readString(mappingsPath)).getAsJsonObject();
                }
                
                for (java.util.Map.Entry<String, java.util.Set<String>> entry : mappings.entrySet()) {
                    com.google.gson.JsonArray array = currentMappings.has(entry.getKey()) ? currentMappings.getAsJsonArray(entry.getKey()) : new com.google.gson.JsonArray();
                    for (String bp : entry.getValue()) {
                        boolean exists = false;
                        for (com.google.gson.JsonElement e : array) {
                            if (e.getAsString().equals(bp)) { exists = true; break; }
                        }
                        if (!exists) array.add(bp);
                    }
                    currentMappings.add(entry.getKey(), array);
                }
                
                java.nio.file.Files.writeString(mappingsPath, new com.google.gson.GsonBuilder().setPrettyPrinting().create().toJson(currentMappings));
                refreshFileList();
            } else {
                NetworkService.getInstance().importBlueprint(name, data, mappings);
            }
            
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.displayClientMessage(net.minecraft.network.chat.Component.translatable("gui.mgmc.blueprint_selection.import_success", name), false);
            }
        } catch (Exception e) {
            MaingraphforMC.LOGGER.error("Failed to import blueprint", e);
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (contextMenu.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return super.charTyped(codePoint, modifiers);
    }

    class BlueprintList extends ObjectSelectionList<BlueprintEntry> {
        public BlueprintList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
            super(minecraft, width, height, y, itemHeight);
        }

        public void add(BlueprintEntry entry) {
            super.addEntry(entry);
        }

        public void clear() {
            super.clearEntries();
        }

        @Override
        public int getRowWidth() {
            return 310;
        }

        @Override
        public int getRowLeft() {
            return BlueprintSelectionScreen.this.width / 2 - 155;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (contextMenu.mouseClicked(mouseX, mouseY, button)) return true;
            return super.mouseClicked(mouseX, mouseY, button);
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
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isHovered, float partialTick) {
            int entryWidth = width;
            int entryLeft = left;
            int entryHeight = height;
            int y = top;
            
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
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            BlueprintSelectionScreen.this.list.setSelected(this);
            
            if (button == 1) { // Right click
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
                        true,
                        null,
                        InputModalScreen.Mode.INPUT,
                        newName -> {
                            if (newName != null && !newName.isEmpty()) {
                                if (isGlobalMode) {
                                    try {
                                        java.nio.file.Path oldPath = ltd.opens.mg.mc.core.blueprint.BlueprintManager.getGlobalBlueprintsDir().resolve(this.fullName);
                                        java.nio.file.Path newPath = ltd.opens.mg.mc.core.blueprint.BlueprintManager.getGlobalBlueprintsDir().resolve(newName.endsWith(".json") ? newName : newName + ".json");
                                        java.nio.file.Files.move(oldPath, newPath);
                                        refreshFileList();
                                    } catch (java.io.IOException e) {
                                        MaingraphforMC.LOGGER.error("Failed to rename global blueprint", e);
                                    }
                                } else {
                                    NetworkService.getInstance().renameBlueprint(this.fullName, newName.endsWith(".json") ? newName : newName + ".json");
                                }
                            }
                        }
                    ));
                }));

                menuItems.add(new GuiContextMenu.MenuItem(Component.translatable("gui.mgmc.blueprint_selection.delete"), () -> {
                    Minecraft.getInstance().setScreen(new InputModalScreen(
                        BlueprintSelectionScreen.this,
                        Component.translatable("gui.mgmc.blueprint_selection.delete_confirm.title").getString(),
                        Component.translatable("gui.mgmc.blueprint_selection.delete_confirm.message", this.displayName).getString(),
                        false,
                        new String[]{
                            Component.translatable("gui.mgmc.blueprint_selection.delete_confirm.delete").getString(),
                            Component.translatable("gui.mgmc.blueprint_selection.delete_confirm.cancel").getString()
                        },
                        InputModalScreen.Mode.SELECTION,
                        selected -> {
                            if (selected.equals(Component.translatable("gui.mgmc.blueprint_selection.delete_confirm.delete").getString())) {
                                if (isGlobalMode) {
                                    try {
                                        java.nio.file.Path path = ltd.opens.mg.mc.core.blueprint.BlueprintManager.getGlobalBlueprintsDir().resolve(this.fullName);
                                        java.nio.file.Files.deleteIfExists(path);
                                        refreshFileList();
                                    } catch (java.io.IOException e) {
                                        MaingraphforMC.LOGGER.error("Failed to delete global blueprint", e);
                                    }
                                } else {
                                    NetworkService.getInstance().deleteBlueprint(this.fullName);
                                }
                            }
                        }
                    ));
                }));

                menuItems.add(new GuiContextMenu.MenuItem(Component.translatable("gui.mgmc.blueprint_selection.export"), () -> {
                    if (isGlobalMode) {
                        // For global mode, we just copy the file
                        try {
                            java.nio.file.Path src = ltd.opens.mg.mc.core.blueprint.BlueprintManager.getGlobalBlueprintsDir().resolve(this.fullName);
                            String data = java.nio.file.Files.readString(src);
                            // We don't have mappings in global mode easily accessible here, but we can try to find them
                            java.util.Map<String, java.util.Set<String>> mappings = new java.util.HashMap<>();
                            java.nio.file.Path mappingsPath = java.nio.file.Paths.get("mgmc_blueprints/.routing/mappings.json");
                            if (java.nio.file.Files.exists(mappingsPath)) {
                                com.google.gson.JsonObject allMappings = com.google.gson.JsonParser.parseString(java.nio.file.Files.readString(mappingsPath)).getAsJsonObject();
                                allMappings.entrySet().forEach(entry -> {
                                    com.google.gson.JsonArray array = entry.getValue().getAsJsonArray();
                                    java.util.Set<String> bps = new java.util.HashSet<>();
                                    array.forEach(e -> {
                                        if (e.getAsString().equals(this.fullName)) bps.add(this.fullName);
                                    });
                                    if (!bps.isEmpty()) mappings.put(entry.getKey(), bps);
                                });
                            }
                            handleExportResponse(this.fullName, data, mappings);
                        } catch (Exception e) {
                            MaingraphforMC.LOGGER.error("Failed to export global blueprint", e);
                        }
                    } else {
                        NetworkService.getInstance().requestExport(this.fullName);
                    }
                }));

                contextMenu.show(mouseX, mouseY, menuItems);
                return true;
            } else if (button == 0) { // Left click
                long now = System.currentTimeMillis();
                if (now - lastClickTime < 250) { // Double click
                    BlueprintSelectionScreen.this.setFocused(null);
                    Minecraft.getInstance().setScreen(new BlueprintScreen(BlueprintSelectionScreen.this, this.fullName));
                    return true;
                }
                lastClickTime = now;
            }
            return true;
        }
    }
}
