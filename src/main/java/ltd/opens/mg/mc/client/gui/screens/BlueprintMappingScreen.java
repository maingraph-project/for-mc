package ltd.opens.mg.mc.client.gui.screens;

import ltd.opens.mg.mc.client.utils.IdMetadataHelper;
import ltd.opens.mg.mc.core.blueprint.routing.BlueprintRouter;
import ltd.opens.mg.mc.network.payloads.RequestMappingsPayload;
import ltd.opens.mg.mc.network.payloads.SaveMappingsPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BlueprintMappingScreen extends Screen {
    private final Screen parent;
    private final Map<String, Set<String>> workingMappings = new HashMap<>();
    private IdList idList;
    private BlueprintSelectionList blueprintList;
    private String selectedId = null;

    // 右键菜单状态
    private boolean showMenu = false;
    private double menuX, menuY;
    private String contextMenuId = null;
    private String contextMenuBlueprint = null;

    public BlueprintMappingScreen(Screen parent) {
        super(Component.translatable("gui.mgmc.mapping.title"));
        this.parent = parent;
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
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
        this.idList = new IdList(this.minecraft, sidePanelWidth, listHeight, 40, 25);
        this.idList.setX(10);
        this.addRenderableWidget(this.idList);

        // 添加 ID 按钮 (+)
        this.addRenderableWidget(Button.builder(Component.literal("+"), b -> {
            Minecraft.getInstance().execute(() -> {
                Minecraft.getInstance().setScreen(new AddMappingIdScreen(this, new ArrayList<>(workingMappings.keySet()), id -> {
                    if (!workingMappings.containsKey(id)) {
                        workingMappings.put(id, new HashSet<>());
                        refreshIdList();
                        selectId(id);
                    }
                }));
            });
        }).bounds(10 + sidePanelWidth - 25, 15, 20, 20).build());

        // 蓝图列表 (右侧)
        this.blueprintList = new BlueprintSelectionList(this.minecraft, mainPanelWidth, listHeight, 40, 20);
        this.blueprintList.setX(sidePanelWidth + 20);
        this.addRenderableWidget(this.blueprintList);

        // 底部控制区起始 Y
        int controlsY = this.height - 65;

        // 添加蓝图按钮 (放在右侧列表下方，也就是 sidePanelWidth + 20 处)
        this.addRenderableWidget(Button.builder(Component.translatable("gui.mgmc.mapping.add_blueprint"), b -> {
            if (selectedId != null) {
                this.setFocused(null); // 清除当前焦点
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

        // 渲染右键菜单
        if (showMenu) {
            renderContextMenu(guiGraphics, mouseX, mouseY);
        }
    }

    private void renderContextMenu(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        int x = (int)menuX;
        int y = (int)menuY;
        int w = 120; // 稍微加宽一点
        int h = 20;

        if (x + w > this.width) x -= w;
        if (y + h > this.height) y -= h;

        guiGraphics.fill(x, y, x + w, y + h, 0xFF202020);
        guiGraphics.renderOutline(x, y, w, h, 0xFFFFFFFF);

        // 删除/移除 选项
        boolean isBuiltIn = contextMenuId != null && (contextMenuId.equals(BlueprintRouter.GLOBAL_ID) || contextMenuId.equals(BlueprintRouter.PLAYERS_ID));
        boolean hover = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        
        if (hover && !isBuiltIn) {
            guiGraphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, 0xFF404040);
        }
        
        String label = contextMenuId != null ? "gui.mgmc.mapping.delete_id" : "gui.mgmc.mapping.remove_mapping";
        int textColor = isBuiltIn ? 0x888888 : 0xFFFF5555;
        guiGraphics.drawString(font, Component.translatable(label), x + 10, y + 6, textColor, false);
        
        if (isBuiltIn && hover) {
            Component text = Component.translatable("gui.mgmc.mapping.built_in_no_delete");
            int textWidth = font.width(text);
            guiGraphics.fill(mouseX + 10, mouseY - 15, mouseX + 10 + textWidth + 10, mouseY + 5, 0xAA000000);
            guiGraphics.renderOutline(mouseX + 10, mouseY - 15, textWidth + 10, 20, 0xFFFFFFFF);
            guiGraphics.drawString(font, text, mouseX + 15, mouseY - 10, 0xFFFFFFFF);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDouble) {
        if (showMenu) {
            int x = (int)menuX;
            int y = (int)menuY;
            int w = 120;
            if (x + w > this.width) x -= w;
            if (y + 20 > this.height) y -= 20;

            if (event.x() >= x && event.x() <= x + w && event.y() >= y && event.y() <= y + 20) {
                if (contextMenuId != null) {
                    // 删除 ID (不允许删除内置 ID)
                    if (!contextMenuId.equals(BlueprintRouter.GLOBAL_ID) && !contextMenuId.equals(BlueprintRouter.PLAYERS_ID)) {
                        workingMappings.remove(contextMenuId);
                        if (contextMenuId.equals(selectedId)) {
                            selectedId = BlueprintRouter.GLOBAL_ID;
                        }
                        refreshIdList();
                    }
                } else if (contextMenuBlueprint != null && selectedId != null) {
                    // 移除蓝图绑定
                    workingMappings.get(selectedId).remove(contextMenuBlueprint);
                    selectId(selectedId);
                }
                showMenu = false;
                return true;
            }
            showMenu = false;
        }
        return super.mouseClicked(event, isDouble);
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
            int left = this.getX();
            int width = this.getWidth();
            int height = this.getHeight();
            int y = this.getY();
            if (y <= 0) y = top;

            boolean isSelected = selectedId != null && selectedId.equals(id);
            IdMetadataHelper.IdInfo info = IdMetadataHelper.getInfo(id);

            // 渲染背景和边框
            if (isSelected) {
                guiGraphics.fill(left, y, left + width, y + height, 0x44FFFFFF);
                guiGraphics.renderOutline(left, y, width, height, 0xFFFFCC00);
            } else if (isHovered) {
                guiGraphics.fill(left, y, left + width, y + height, 0x22FFFFFF);
                guiGraphics.renderOutline(left, y, width, height, 0xFF888888);
            }

            // 渲染图标
            if (!info.icon.isEmpty()) {
                guiGraphics.renderItem(info.icon, left + 5, y + 4);
            }

            int color = isSelected ? 0xFFFFCC00 : (isHovered ? 0xFFFFFFFF : (info.isBuiltIn ? 0xFF888888 : 0xFFAAAAAA));
            
            // 渲染显示名称
            guiGraphics.drawString(font, info.name, left + 25, y + 4, color);
            // 渲染 ID
            guiGraphics.drawString(font, info.id, left + 25, y + 14, 0x888888);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean isDouble) {
            selectId(id);
            if (event.buttonInfo().button() == 1) { // 右键
                contextMenuId = id;
                contextMenuBlueprint = null;
                menuX = event.x();
                menuY = event.y();
                showMenu = true;
                return true;
            }
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
            int left = this.getX();
            int width = this.getWidth();
            int height = this.getHeight();
            int y = this.getY();
            if (y <= 0) y = top;

            // 渲染背景
            if (isHovered) {
                guiGraphics.fill(left, y, left + width, y + height, 0x22FFFFFF);
                guiGraphics.renderOutline(left, y, width, height, 0xFF888888);
            }

            int color = isHovered ? 0xFFFFFFFF : 0xFFAAAAAA;
            guiGraphics.drawString(font, blueprintPath, left + 5, y + (height - 8) / 2, color);
            
            // 删除按钮 (X)
            int xBtnWidth = 20;
            int xBtnX = left + width - xBtnWidth;
            // 简化悬停显示：只要条目被悬停，就显示浅红色的 X
            guiGraphics.drawString(font, "X", xBtnX + 5, y + (height - 8) / 2, isHovered ? 0xFFFF5555 : 0x44FF5555);
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean isDouble) {
            int entryWidth = this.getWidth();
            int entryLeft = this.getX();
            int xBtnWidth = 20;
            int xBtnX = entryLeft + entryWidth - xBtnWidth;
            
            double mouseX = event.x();
            double mouseY = event.y();

            if (event.buttonInfo().button() == 1) { // 右键
                contextMenuId = null;
                contextMenuBlueprint = blueprintPath;
                menuX = mouseX;
                menuY = mouseY;
                showMenu = true;
                return true;
            }

            if (mouseX >= xBtnX) {
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
