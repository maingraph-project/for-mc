package ltd.opens.mg.mc.client.gui.screens;

import ltd.opens.mg.mc.client.utils.IdMetadataHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class AddMappingIdScreen extends Screen {
    private final Screen parent;
    private final Consumer<String> onSelect;
    private final List<String> existingIds;
    
    private EditBox searchBox;
    private SuggestionList suggestionList;
    private List<IdMetadataHelper.IdInfo> allSuggestions;

    public AddMappingIdScreen(Screen parent, List<String> existingIds, Consumer<String> onSelect) {
        super(Component.translatable("gui.mgmc.mapping.add_id.title"));
        this.parent = parent;
        this.onSelect = onSelect;
        this.existingIds = existingIds;
    }

    @Override
    protected void init() {
        if (allSuggestions == null) {
            allSuggestions = IdMetadataHelper.getAllPotentialIds();
        }

        int listWidth = 300;
        int listHeight = 150;
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.searchBox = new EditBox(this.font, centerX - 150, centerY - 100, 300, 20, Component.literal("Search"));
        this.searchBox.setHint(Component.translatable("gui.mgmc.mapping.id_hint"));
        this.searchBox.setResponder(this::onSearchChanged);
        this.addRenderableWidget(this.searchBox);

        this.suggestionList = new SuggestionList(this.minecraft, listWidth, listHeight, centerY - 75, 25);
        this.suggestionList.setX(centerX - 150);
        this.addRenderableWidget(this.suggestionList);

        this.addRenderableWidget(Button.builder(Component.translatable("gui.mgmc.mapping.cancel"), b -> {
            this.onClose();
        }).bounds(centerX - 105, centerY + 85, 100, 20).build());
        
        this.addRenderableWidget(Button.builder(Component.translatable("gui.mgmc.mapping.select"), b -> {
            SuggestionEntry selected = this.suggestionList.getSelected();
            if (selected != null) {
                confirmSelection(selected.info.id);
            }
        }).bounds(centerX + 5, centerY + 85, 100, 20).build());

        updateSuggestions("");
        Minecraft.getInstance().execute(() -> {
            this.setInitialFocus(this.searchBox);
        });
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    private void onSearchChanged(String text) {
        updateSuggestions(text);
    }

    private void updateSuggestions(String text) {
        String query = text.toLowerCase();
        List<IdMetadataHelper.IdInfo> filtered = allSuggestions.stream()
                .filter(info -> info.id.toLowerCase().contains(query) || info.name.getString().toLowerCase().contains(query))
                .limit(50)
                .collect(Collectors.toList());

        this.suggestionList.clearEntries();
        for (IdMetadataHelper.IdInfo info : filtered) {
            this.suggestionList.add(new SuggestionEntry(info));
        }
    }

    private void confirmSelection() {
        SuggestionEntry selected = this.suggestionList.getSelected();
        if (selected != null) {
            confirmSelection(selected.info.id);
        }
    }

    private void confirmSelection(String id) {
        if (existingIds.contains(id)) {
            this.errorMessage = Component.translatable("gui.mgmc.mapping.duplicate_id");
            this.errorTicks = 60;
            return;
        }
        onSelect.accept(id);
        this.onClose();
    }

    private Component errorMessage = null;
    private int errorTicks = 0;

    @Override
    public void tick() {
        if (errorTicks > 0) {
            errorTicks--;
            if (errorTicks == 0) errorMessage = null;
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        
        // 绘制模态框背景
        int centerX = this.width / 2;
        int centerY = this.height / 2;
        guiGraphics.fillGradient(centerX - 160, centerY - 110, centerX + 160, centerY + 115, 0xEE101010, 0xEE101010);
        guiGraphics.renderOutline(centerX - 160, centerY - 110, 320, 225, 0xFF555555);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        
        int centerY = this.height / 2;
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, centerY - 125, 0xFFFFFF);

        if (errorMessage != null) {
            guiGraphics.drawCenteredString(this.font, errorMessage, this.width / 2, centerY + 65, 0xFFFF5555);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            this.confirmSelection();
            return true;
        }
        return super.keyPressed(event);
    }

    class SuggestionList extends ObjectSelectionList<SuggestionEntry> {
        public SuggestionList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
            super(minecraft, width, height, y, itemHeight);
        }

        public void add(SuggestionEntry entry) {
            this.addEntry(entry);
        }

        @Override
        public int getRowWidth() { return this.width - 10; }
        @Override
        public int getRowLeft() { return this.getX() + 5; }
    }

    class SuggestionEntry extends ObjectSelectionList.Entry<SuggestionEntry> {
        final IdMetadataHelper.IdInfo info;

        SuggestionEntry(IdMetadataHelper.IdInfo info) {
            this.info = info;
        }

        @Override
        public void renderContent(GuiGraphics guiGraphics, int index, int top, boolean isHovered, float partialTick) {
            int left = suggestionList.getRowLeft();
            int width = suggestionList.getRowWidth();
            int height = 25; // 使用构造函数中定义的固定行高
            
            boolean isSelected = suggestionList.getSelected() == this;
            boolean isExists = existingIds.contains(info.id);

            if (isSelected) {
                guiGraphics.fill(left, top, left + width, top + height, 0x44FFFFFF);
                guiGraphics.renderOutline(left, top, width, height, 0xFFFFAA00);
            } else if (isHovered) {
                guiGraphics.fill(left, top, left + width, top + height, 0x22FFFFFF);
            }

            // 绘制图标
            if (!info.icon.isEmpty()) {
                guiGraphics.renderItem(info.icon, left + 2, top + 2);
            }

            // 绘制文本
            int textColor = isExists ? 0x888888 : 0xFFFFFF;
            guiGraphics.drawString(font, info.name, left + 25, top + 2, textColor);
            guiGraphics.drawString(font, info.id, left + 25, top + 12, 0x888888);
            
            if (isExists) {
                guiGraphics.drawString(font, "✔", left + width - 15, top + 5, 0x55FF55);
            }
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean isDouble) {
            suggestionList.setSelected(this);
            if (event.buttonInfo().button() == 0) {
                confirmSelection(info.id);
                return true;
            }
            return false;
        }

        @Override
        public Component getNarration() {
            return Component.literal(info.name.getString());
        }
    }
}
