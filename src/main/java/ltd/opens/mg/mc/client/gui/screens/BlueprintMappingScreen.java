package ltd.opens.mg.mc.client.gui.screens;

import ltd.opens.mg.mc.core.blueprint.routing.BlueprintRouter;
import ltd.opens.mg.mc.network.payloads.RequestMappingsPayload;
import ltd.opens.mg.mc.network.payloads.SaveMappingsPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;

import java.util.*;

public class BlueprintMappingScreen extends Screen {
    private final Map<String, Set<String>> workingMappings = new HashMap<>();
    private IdList idList;
    private BlueprintSelectionList blueprintList;
    private EditBox idInput;
    private String selectedId = null;

    public BlueprintMappingScreen() {
        super(Component.translatable("gui.mgmc.mapping.title"));
    }

    @Override
    protected void init() {
        // 只有在没有数据时才请求，避免覆盖本地未保存的修改
        if (this.workingMappings.isEmpty() && Minecraft.getInstance().getConnection() != null) {
            Minecraft.getInstance().getConnection().send(new ServerboundCustomPayloadPacket(new RequestMappingsPayload()));
        }

        int sidePanelWidth = this.width / 3;
        int mainPanelWidth = this.width - sidePanelWidth - 40;
        int listHeight = this.height - 120;

        // ID 列表 (左侧)
        this.idList = new IdList(this.minecraft, sidePanelWidth, listHeight, 40, 20);
        this.idList.setX(10);
        this.addRenderableWidget(this.idList);

        // 蓝图列表 (右侧)
        this.blueprintList = new BlueprintSelectionList(this.minecraft, mainPanelWidth, listHeight, 40, 20);
        this.blueprintList.setX(sidePanelWidth + 20);
        this.addRenderableWidget(this.blueprintList);

        // 底部控制区起始 Y
        int controlsY = this.height - 65;

        // ID 输入框
        this.idInput = new EditBox(this.font, 10, controlsY, sidePanelWidth, 20, Component.literal("ID"));
        this.idInput.setHint(Component.translatable("gui.mgmc.mapping.id_hint"));
        this.addRenderableWidget(this.idInput);

        // 添加 ID 按钮
        this.addRenderableWidget(Button.builder(Component.translatable("gui.mgmc.mapping.add_id"), b -> {
            String id = this.idInput.getValue().trim();
            if (!id.isEmpty() && !workingMappings.containsKey(id)) {
                workingMappings.put(id, new HashSet<>());
                refreshIdList();
                this.idInput.setValue("");
            }
        }).bounds(10 + sidePanelWidth + 5, controlsY, 60, 20).build());

        // 添加蓝图按钮 (放在右侧列表下方)
        this.addRenderableWidget(Button.builder(Component.translatable("gui.mgmc.mapping.add_blueprint"), b -> {
            if (selectedId != null) {
                Minecraft.getInstance().setScreen(new BlueprintSelectionForMappingScreen(this, selectedId));
            }
        }).bounds(sidePanelWidth + 20, controlsY, 100, 20).build());

        // 保存和返回按钮 (最底部)
        int footerY = this.height - 30;
        this.addRenderableWidget(Button.builder(Component.translatable("gui.mgmc.mapping.save"), b -> {
            if (Minecraft.getInstance().getConnection() != null) {
                Minecraft.getInstance().getConnection().send(new ServerboundCustomPayloadPacket(new SaveMappingsPayload(new HashMap<>(workingMappings))));
            }
            this.onClose();
        }).bounds(this.width - 110, footerY, 100, 20).build());

        this.addRenderableWidget(Button.builder(Component.translatable("gui.mgmc.mapping.back"), b -> {
            this.onClose();
        }).bounds(this.width - 220, footerY, 100, 20).build());

        refreshIdList();
        
        // 默认选中 GLOBAL_ID
        if (selectedId == null) {
            selectId(BlueprintRouter.GLOBAL_ID);
        }
    }

    public void updateMappingsFromServer(Map<String, Set<String>> mappings) {
        this.workingMappings.clear();
        mappings.forEach((k, v) -> this.workingMappings.put(k, new HashSet<>(v)));
        refreshIdList();
    }

    private void refreshIdList() {
        String prevSelected = selectedId;
        this.idList.clearEntries();
        
        // 确保内置 ID 存在
        workingMappings.putIfAbsent(BlueprintRouter.GLOBAL_ID, new HashSet<>());
        workingMappings.putIfAbsent(BlueprintRouter.PLAYERS_ID, new HashSet<>());

        workingMappings.keySet().stream().sorted().forEach(id -> {
            IdEntry entry = new IdEntry(id);
            this.idList.add(entry);
            if (id.equals(prevSelected)) {
                this.idList.setSelected(entry);
            }
        });

        if (prevSelected != null && workingMappings.containsKey(prevSelected)) {
            selectId(prevSelected);
        }
    }

    public void addMapping(String id, String path) {
        workingMappings.computeIfAbsent(id, k -> new HashSet<>()).add(path);
        // 如果当前选中的正是这个 ID，立即刷新右侧列表
        if (id.equals(selectedId)) {
            selectId(id);
        }
    }

    private void selectId(String id) {
        this.selectedId = id;
        
        // 确保右侧列表存在
        if (this.blueprintList != null) {
            this.blueprintList.clearEntries();
            if (id != null && workingMappings.containsKey(id)) {
                workingMappings.get(id).stream().sorted().forEach(bp -> {
                    this.blueprintList.add(new BlueprintMappingEntry(bp));
                });
            }
        }
        
        // 更新左侧列表的视觉选中状态
        if (this.idList != null) {
            for (IdEntry entry : this.idList.children()) {
                if (entry.id.equals(id)) {
                    this.idList.setSelected(entry);
                    break;
                }
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
        
        guiGraphics.drawString(this.font, Component.translatable("gui.mgmc.mapping.ids"), 10, 30, 0xAAAAAA);
        guiGraphics.drawString(this.font, Component.translatable("gui.mgmc.mapping.blueprints"), this.width / 3 + 20, 30, 0xAAAAAA);
    }

    // --- 内部类：ID 列表 ---
    class IdList extends ObjectSelectionList<IdEntry> {
        public IdList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
            super(minecraft, width, height, y, itemHeight);
        }

        public void add(IdEntry entry) {
            super.addEntry(entry);
        }

        @Override
        public int getRowWidth() {
            return this.width - 10;
        }

        @Override
        public int getRowLeft() {
            return this.getX() + 5;
        }
    }

    class IdEntry extends ObjectSelectionList.Entry<IdEntry> {
        private final String id;

        public IdEntry(String id) {
            this.id = id;
        }

        @Override
        public void renderContent(GuiGraphics guiGraphics, int index, int top, boolean isHovered, float partialTick) {
            int entryWidth = this.getWidth();
            int entryLeft = this.getX();
            int entryHeight = this.getHeight();
            int y = this.getY();
            if (y <= 0) y = top;

            boolean isSelected = selectedId != null && selectedId.equals(id);

            // 渲染背景和边框
            if (isSelected) {
                guiGraphics.fill(entryLeft, y, entryLeft + entryWidth, y + entryHeight, 0x44FFFFFF);
                guiGraphics.renderOutline(entryLeft, y, entryWidth, entryHeight, 0xFFFFCC00);
            } else if (isHovered) {
                guiGraphics.fill(entryLeft, y, entryLeft + entryWidth, y + entryHeight, 0x22FFFFFF);
                guiGraphics.renderOutline(entryLeft, y, entryWidth, entryHeight, 0xFF888888);
            }

            int color = isSelected ? 0xFFFFCC00 : (isHovered ? 0xFFFFFFFF : 0xFFAAAAAA);
            guiGraphics.drawString(font, id, entryLeft + 5, y + (entryHeight - 8) / 2, color);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean isDouble) {
            selectId(id);
            return true;
        }

        @Override
        public Component getNarration() {
            return Component.literal(id);
        }
    }

    // --- 内部类：蓝图列表 ---
    class BlueprintSelectionList extends ObjectSelectionList<BlueprintMappingEntry> {
        public BlueprintSelectionList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
            super(minecraft, width, height, y, itemHeight);
        }

        public void add(BlueprintMappingEntry entry) {
            super.addEntry(entry);
        }

        @Override
        public int getRowWidth() {
            return this.width - 10;
        }

        @Override
        public int getRowLeft() {
            return this.getX() + 5;
        }
    }

    class BlueprintMappingEntry extends ObjectSelectionList.Entry<BlueprintMappingEntry> {
        private final String blueprintPath;

        public BlueprintMappingEntry(String path) {
            this.blueprintPath = path;
        }

        @Override
        public void renderContent(GuiGraphics guiGraphics, int index, int top, boolean isHovered, float partialTick) {
            int entryWidth = this.getWidth();
            int entryLeft = this.getX();
            int entryHeight = this.getHeight();
            int y = this.getY();
            if (y <= 0) y = top;

            // 渲染背景
            if (isHovered) {
                guiGraphics.fill(entryLeft, y, entryLeft + entryWidth, y + entryHeight, 0x22FFFFFF);
                guiGraphics.renderOutline(entryLeft, y, entryWidth, entryHeight, 0xFF888888);
            }

            int color = isHovered ? 0xFFFFFFFF : 0xFFAAAAAA;
            guiGraphics.drawString(font, blueprintPath, entryLeft + 5, y + (entryHeight - 8) / 2, color);
            
            // 删除按钮 (X)
            int xBtnWidth = 20;
            int xBtnX = entryLeft + entryWidth - xBtnWidth;
            // 简化悬停显示：只要条目被悬停，就显示浅红色的 X
            guiGraphics.drawString(font, "X", xBtnX + 5, y + (entryHeight - 8) / 2, isHovered ? 0xFFFF5555 : 0x44FF5555);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean isDouble) {
            int entryWidth = this.getWidth();
            int entryLeft = this.getX();
            int xBtnWidth = 20;
            int xBtnX = entryLeft + entryWidth - xBtnWidth;
            
            if (event.x() >= xBtnX) {
                if (selectedId != null && workingMappings.containsKey(selectedId)) {
                    workingMappings.get(selectedId).remove(blueprintPath);
                    selectId(selectedId);
                }
                return true;
            }
            return false;
        }

        @Override
        public Component getNarration() {
            return Component.literal(blueprintPath);
        }
    }
}
