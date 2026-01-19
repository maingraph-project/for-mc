package ltd.opens.mg.mc.client.gui.screens;

import ltd.opens.mg.mc.client.network.NetworkService;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.List;

public class BlueprintSelectionForMappingScreen extends Screen {
    private final BlueprintMappingScreen parent;
    private final String targetId;
    private BlueprintList list;

    public BlueprintSelectionForMappingScreen(BlueprintMappingScreen parent, String targetId) {
        super(Component.translatable("gui.mgmc.mapping.select_blueprint.title"));
        this.parent = parent;
        this.targetId = targetId;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    protected void init() {
        this.list = new BlueprintList(this.minecraft, this.width, this.height - 60, 30, 24);
        this.addRenderableWidget(this.list);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.mgmc.mapping.select"), b -> {
            if (this.list.getSelected() != null) {
                parent.addMapping(targetId, this.list.getSelected().name);
                this.onClose();
            }
        }).bounds(this.width / 2 - 105, this.height - 25, 100, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.mgmc.mapping.cancel"), b -> {
            this.onClose();
        }).bounds(this.width / 2 + 5, this.height - 25, 100, 20).build());

        refreshList();
    }

    private void refreshList() {
        this.list.clearEntries();
        NetworkService.getInstance().requestBlueprintList();
    }

    public void updateListFromServer(List<String> blueprints) {
        this.list.clearEntries();
        for (String name : blueprints) {
            this.list.add(new BlueprintEntry(name));
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 10, 0xFFFFFF);
    }

    class BlueprintList extends ObjectSelectionList<BlueprintEntry> {
        public BlueprintList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
            super(minecraft, width, height, y, itemHeight);
        }
        public void add(BlueprintEntry entry) { super.addEntry(entry); }

        @Override
        public int getRowWidth() {
            return 310;
        }
    }

    class BlueprintEntry extends ObjectSelectionList.Entry<BlueprintEntry> {
        final String name;
        final String displayName;
        public BlueprintEntry(String name) { 
            this.name = name; 
            this.displayName = name.endsWith(".json") ? name.substring(0, name.length() - 5) : name;
        }
        
        @Override
        public void renderContent(GuiGraphics guiGraphics, int index, int top, boolean isHovered, float partialTick) {
            int left = this.getX();
            int width = this.getWidth();
            int height = this.getHeight();
            int y = this.getY();
            if (y <= 0) y = top;

            boolean isSelected = BlueprintSelectionForMappingScreen.this.list.getSelected() == this;

            // 渲染背景和边框
            if (isSelected) {
                guiGraphics.fill(left, y, left + width, y + height, 0x44FFFFFF);
                guiGraphics.renderOutline(left, y, width, height, 0xFFFFCC00);
            } else if (isHovered) {
                guiGraphics.fill(left, y, left + width, y + height, 0x22FFFFFF);
                guiGraphics.renderOutline(left, y, width, height, 0xFF888888);
            }

            int color = isSelected ? 0xFFFFCC00 : (isHovered ? 0xFFFFFFFF : 0xFFAAAAAA);
            guiGraphics.drawString(font, displayName, left + 5, y + (height - 8) / 2, color);
        }
        
        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean isDouble) {
            BlueprintSelectionForMappingScreen.this.list.setSelected(this);
            return true;
        }
        
        @Override
        public Component getNarration() { return Component.literal(displayName); }
    }
}
