package ltd.opens.mg.mc.client.gui.screens;

import ltd.opens.mg.mc.client.network.NetworkService;
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
import java.util.List;

public class BlueprintSelectionScreen extends Screen {
    private BlueprintList list;
    private EditBox newBlueprintName;
    private Button openButton;
    private Button createButton;
    
    // Context Menu State
    private BlueprintEntry contextMenuEntry;
    private double menuX, menuY;
    private boolean showMenu = false;
    private EditBox renameBox;
    private boolean isRenaming = false;

    public BlueprintSelectionScreen() {
        super(Component.translatable("gui.mgmc.blueprint_selection.title"));
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
        this.newBlueprintName.setHint(Component.translatable("gui.mgmc.blueprint_selection.new_name_hint"));
        this.addRenderableWidget(this.newBlueprintName);

        this.createButton = Button.builder(Component.translatable("gui.mgmc.blueprint_selection.create"), b -> {
            String name = this.newBlueprintName.getValue().trim();
            if (!name.isEmpty()) {
                if (!name.endsWith(".json")) name += ".json";
                
                this.setFocused(null);
                NetworkService.getInstance().saveBlueprint(name, "{}", -1);
                Minecraft.getInstance().setScreen(new BlueprintScreen(this, name));
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
                Minecraft.getInstance().setScreen(new BlueprintScreen(this, entry.name));
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

        // Rename box (initially hidden)
        this.renameBox = new EditBox(this.font, 0, 0, 100, 20, Component.translatable("gui.mgmc.blueprint_selection.rename"));
        this.renameBox.setVisible(false);
        this.addRenderableWidget(this.renameBox);
    }

    private void refreshFileList() {
        this.list.clearEntries();
        // Always request from server (works for both local and remote servers)
        NetworkService.getInstance().requestBlueprintList();
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
        
        // Draw list area background if it's not being drawn by the list
        // This helps to see the bounds
        // guiGraphics.fill(0, 40, this.width, this.height - 60, 0x44000000);
        
        if (this.list != null && this.list.children().isEmpty()) {
            guiGraphics.drawCenteredString(this.font, Component.translatable("gui.mgmc.blueprint_selection.no_blueprints"), this.width / 2, this.height / 2 - 10, 0xAAAAAA);
        }

        if (showMenu) {
            renderContextMenu(guiGraphics, mouseX, mouseY);
        }
    }

    private void renderContextMenu(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = (int)menuX;
        int y = (int)menuY;
        int w = 100;
        int h = 60;

        if (x + w > this.width) x -= w;
        if (y + h > this.height) y -= h;

        guiGraphics.fill(x, y, x + w, y + h, 0xFF202020);
        guiGraphics.renderOutline(x, y, w, h, 0xFFFFFFFF);

        // Rename Option
        boolean hoverRename = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + 20;
        if (hoverRename) guiGraphics.fill(x + 1, y + 1, x + w - 1, y + 19, 0xFF404040);
        guiGraphics.drawString(font, Component.translatable("gui.mgmc.blueprint_selection.rename"), x + 10, y + 6, 0xFFFFFFFF);

        // Duplicate Option
        boolean hoverDuplicate = mouseX >= x && mouseX <= x + w && mouseY >= y + 20 && mouseY <= y + 40;
        if (hoverDuplicate) guiGraphics.fill(x + 1, y + 21, x + w - 1, y + 39, 0xFF404040);
        guiGraphics.drawString(font, Component.translatable("gui.mgmc.blueprint_selection.duplicate"), x + 10, y + 26, 0xFFFFFFFF);

        // Delete Option
        boolean hoverDelete = mouseX >= x && mouseX <= x + w && mouseY >= y + 40 && mouseY <= y + 60;
        if (hoverDelete) guiGraphics.fill(x + 1, y + 41, x + w - 1, y + 59, 0xFF404040);
        guiGraphics.drawString(font, Component.translatable("gui.mgmc.blueprint_selection.delete"), x + 10, y + 46, 0xFFFF5555);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDouble) {
        double mouseX = event.x();
        double mouseY = event.y();

        if (showMenu) {
            int x = (int)menuX;
            int y = (int)menuY;
            int w = 100;
            int h = 60;
            if (x + w > this.width) x -= w;
            if (y + h > this.height) y -= h;

            if (mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + 20) {
                startRename();
                showMenu = false;
                return true;
            } else if (mouseX >= x && mouseX <= x + w && mouseY >= y + 20 && mouseY <= y + 40) {
                duplicateBlueprint();
                showMenu = false;
                return true;
            } else if (mouseX >= x && mouseX <= x + w && mouseY >= y + 40 && mouseY <= y + 60) {
                deleteBlueprint();
                showMenu = false;
                return true;
            }
            showMenu = false;
        }

        if (isRenaming) {
            if (renameBox.isMouseOver(mouseX, mouseY)) {
                return renameBox.mouseClicked(event, isDouble);
            } else {
                finishRename(false);
            }
        }

        return super.mouseClicked(event, isDouble);
    }

    private void startRename() {
        isRenaming = true;
        renameBox.setVisible(true);
        renameBox.setX((int)menuX);
        renameBox.setY((int)menuY);
        String fileName = contextMenuEntry.name;
        if (fileName.endsWith(".json")) {
            fileName = fileName.substring(0, fileName.length() - 5);
        }
        renameBox.setValue(fileName);
        setFocused(renameBox);
    }

    private void finishRename(boolean save) {
        if (save && contextMenuEntry != null) {
            String newName = renameBox.getValue().trim();
            if (!newName.isEmpty()) {
                if (!newName.endsWith(".json")) newName += ".json";
                NetworkService.getInstance().renameBlueprint(contextMenuEntry.name, newName);
            }
        }
        isRenaming = false;
        renameBox.setVisible(false);
        if (this.getFocused() == this.renameBox) {
            this.setFocused(null);
        }
    }

    private void deleteBlueprint() {
        if (contextMenuEntry != null) {
            NetworkService.getInstance().deleteBlueprint(contextMenuEntry.name);
        }
    }

    private void duplicateBlueprint() {
        if (contextMenuEntry != null) {
            String baseName = contextMenuEntry.name;
            if (baseName.endsWith(".json")) {
                baseName = baseName.substring(0, baseName.length() - 5);
            }
            
            // Generate a random 3-character suffix (alphanumeric)
            String chars = "abcdefghijklmnopqrstuvwxyz0123456789";
            StringBuilder suffix = new StringBuilder();
            java.util.Random rnd = new java.util.Random();
            for (int i = 0; i < 3; i++) {
                suffix.append(chars.charAt(rnd.nextInt(chars.length())));
            }
            
            String newName = baseName + "_copy_" + suffix.toString() + ".json";
            NetworkService.getInstance().duplicateBlueprint(contextMenuEntry.name, newName);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (isRenaming) {
            int keyCode = event.key();
            if (keyCode == 257 || keyCode == 335) { // ENTER or NUMPAD_ENTER
                finishRename(true);
                return true;
            } else if (keyCode == 256) { // ESCAPE
                finishRename(false);
                return true;
            }
            
            if (renameBox.keyPressed(event)) {
                return true;
            }
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (isRenaming) {
            return renameBox.charTyped(event);
        }
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
        final String name;
        private long lastClickTime;

        public BlueprintEntry(String name) {
            this.name = name.endsWith(".json") ? name.substring(0, name.length() - 5) : name;
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
            
            String nameToRender = this.name;
            
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
            return Component.literal(this.name);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean isDouble) {
            BlueprintSelectionScreen.this.list.setSelected(this);
            
            if (event.buttonInfo().button() == 1) { // Right click
                contextMenuEntry = this;
                menuX = event.x();
                menuY = event.y();
                showMenu = true;
                return true;
            }
            
            long now = System.currentTimeMillis();
            if (now - lastClickTime < 250L) {
                // Double click
                BlueprintSelectionScreen.this.setFocused(null);
                Minecraft.getInstance().setScreen(new BlueprintScreen(BlueprintSelectionScreen.this, this.name));
                return true;
            }
            lastClickTime = now;
            
            return true;
        }
    }
}
