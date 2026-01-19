package ltd.opens.mg.mc.client.gui.screens;

import ltd.opens.mg.mc.client.network.NetworkService;
import ltd.opens.mg.mc.core.blueprint.inventory.BlueprintWorkbenchMenu;
import ltd.opens.mg.mc.core.registry.MGMCRegistries;
import ltd.opens.mg.mc.network.payloads.WorkbenchActionPayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class BlueprintWorkbenchScreen extends AbstractContainerScreen<BlueprintWorkbenchMenu> {
    
    private BlueprintList allBlueprintsList;
    private BoundBlueprintList boundBlueprintsList;

    public BlueprintWorkbenchScreen(BlueprintWorkbenchMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 256;
        this.imageHeight = 150; // 缩小高度，不再需要背包空间
        this.titleLabelX = 8;
        this.titleLabelY = 6;
    }

    @Override
    protected void init() {
        super.init();
        
        int listWidth = 85;
        int listHeight = 90;
        int listY = this.topPos + 35;

        this.boundBlueprintsList = new BoundBlueprintList(this.minecraft, listWidth, listHeight, listY, 20);
        this.boundBlueprintsList.setX(this.leftPos + 55);
        this.addRenderableWidget(this.boundBlueprintsList);

        this.allBlueprintsList = new BlueprintList(this.minecraft, listWidth, listHeight, listY, 20);
        this.allBlueprintsList.setX(this.leftPos + 160);
        this.addRenderableWidget(this.allBlueprintsList);

        this.addRenderableWidget(Button.builder(Component.literal("▶"), b -> {
            BlueprintEntry selected = allBlueprintsList.getSelected();
            if (selected != null) {
                NetworkService.getInstance().sendWorkbenchAction(WorkbenchActionPayload.Action.BIND, selected.path);
            }
        }).bounds(this.leftPos + 142, this.topPos + 65, 16, 20).build());

        NetworkService.getInstance().requestBlueprintList();
    }

    public void updateListFromServer(List<String> blueprints) {
        if (allBlueprintsList != null) {
            allBlueprintsList.clearEntries();
            for (String bp : blueprints) {
                allBlueprintsList.add(new BlueprintEntry(bp));
            }
        }
    }

    private ItemStack lastStack = ItemStack.EMPTY;
    private List<String> lastScripts = null;

    @Override
    public void containerTick() {
        super.containerTick();
        if (this.boundBlueprintsList != null) {
            ItemStack stack = this.menu.getTargetItem();
            List<String> scripts = stack.isEmpty() ? null : stack.get(MGMCRegistries.BLUEPRINT_SCRIPTS.get());
            
            boolean changed = false;
            if (stack.isEmpty() != lastStack.isEmpty()) {
                changed = true;
            } else if (!stack.isEmpty()) {
                if (scripts == null && lastScripts != null) changed = true;
                else if (scripts != null && !scripts.equals(lastScripts)) changed = true;
            }

            if (changed) {
                if (scripts != null) {
                    this.boundBlueprintsList.updateList(scripts);
                } else {
                    this.boundBlueprintsList.clearEntries();
                }
                lastStack = stack.copy();
                lastScripts = scripts == null ? null : List.copyOf(scripts);
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = this.leftPos;
        int y = this.topPos;
        
        // 1. 绘制主体面板
        graphics.fill(x, y, x + this.imageWidth, y + this.imageHeight, 0xFFC6C6C6);
        graphics.fill(x, y, x + this.imageWidth, y + 1, 0xFFFFFFFF); // 顶白
        graphics.fill(x, y, x + 1, y + this.imageHeight, 0xFFFFFFFF); // 左白
        graphics.fill(x + this.imageWidth - 1, y, x + this.imageWidth, y + this.imageHeight, 0xFF555555); // 右黑
        graphics.fill(x, y + this.imageHeight - 1, x + this.imageWidth, y + this.imageHeight, 0xFF555555); // 底黑

        // 2. 绘制手持物品展示框 (左侧)
        int slotX = x + 20;
        int slotY = y + 35;
        // 绘制槽位背景
        graphics.fill(slotX - 1, slotY - 1, slotX + 19, slotY + 19, 0xFF373737);
        graphics.fill(slotX, slotY, slotX + 19, slotY + 19, 0xFFFFFFFF);
        graphics.fill(slotX, slotY, slotX + 18, slotY + 18, 0xFF8B8B8B);
        
        // 渲染当前手持的物品图标
        ItemStack heldItem = this.menu.getTargetItem();
        if (!heldItem.isEmpty()) {
                graphics.renderFakeItem(heldItem, slotX + 1, slotY + 1);
                graphics.renderItemDecorations(this.font, heldItem, slotX + 1, slotY + 1);
            } else {
                // 如果手空，画个淡淡的提示
                graphics.drawString(this.font, Component.translatable("gui.mgmc.workbench.empty"), slotX + 4, slotY + 5, 0xFF888888, false);
            }
            
            graphics.drawString(this.font, Component.translatable("gui.mgmc.workbench.held_item"), x + 10, y + 22, 0xFF404040, false);

            // 3. 绘制蓝图列表
            renderPanel(graphics, x + 55, y + 35, 85, 90, Component.translatable("gui.mgmc.workbench.bound_blueprints").getString());
            renderPanel(graphics, x + 160, y + 35, 85, 90, Component.translatable("gui.mgmc.workbench.blueprint_library").getString());

        graphics.drawString(this.font, this.title, x + 8, y + 8, 0xFF404040, false);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        if (this.boundBlueprintsList.mouseScrolled(mouseX, mouseY, horizontal, vertical)) return true;
        if (this.allBlueprintsList.mouseScrolled(mouseX, mouseY, horizontal, vertical)) return true;
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    private void renderPanel(GuiGraphics g, int x, int y, int w, int h, String label) {
        // 凹陷面板风格
        g.fill(x - 1, y - 1, x + w, y + h, 0xFF373737); // 深色边
        g.fill(x, y, x + w + 1, y + h + 1, 0xFFFFFFFF); // 白色边
        g.fill(x, y, x + w, y + h, 0xFF8B8B8B); // 内部灰色
        
        g.drawString(this.font, Component.literal(label), x, y - 10, 0xFF404040, false);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // 覆盖父类方法，防止重复绘制 "Inventory" 标签
        // 因为我们在 blit 贴图中已经包含了该文字
    }

    private class BlueprintList extends ObjectSelectionList<BlueprintEntry> {
        public BlueprintList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
            super(minecraft, width, height, y, itemHeight);
        }
        public void add(BlueprintEntry entry) {
            this.addEntry(entry);
        }
        @Override
        public int getRowWidth() { return this.width - 4; }
    }

    private class BlueprintEntry extends ObjectSelectionList.Entry<BlueprintEntry> {
        final String path;
        final String displayName;
        public BlueprintEntry(String path) { 
            this.path = path;
            this.displayName = path.endsWith(".json") ? path.substring(0, path.length() - 5) : path;
        }

        @Override
        public void renderContent(GuiGraphics guiGraphics, int index, int top, boolean isHovered, float partialTick) {
            int x = this.getX();
            int y = this.getY();
            if (y <= 0) y = top;
            int width = this.getWidth();
            int height = this.getHeight();

            if (this == allBlueprintsList.getSelected()) {
                guiGraphics.fill(x, y, x + width, y + height, 0xFFFFFFFF); // 选中白色背景
                guiGraphics.drawString(minecraft.font, displayName, x + 4, y + 5, 0xFF404040, false); // 选中时深色文字
            } else {
                if (isHovered) {
                    guiGraphics.fill(x, y, x + width, y + height, 0x44FFFFFF); // 悬停半透明
                }
                guiGraphics.drawString(minecraft.font, displayName, x + 4, y + 5, 0xFFFFFFFF, false); // 普通白色文字
            }
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean isDouble) {
            allBlueprintsList.setSelected(this);
            return true;
        }

        @Override
        public Component getNarration() { return Component.literal(displayName); }
    }

    private class BoundBlueprintList extends ObjectSelectionList<BoundEntry> {
        public BoundBlueprintList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
            super(minecraft, width, height, y, itemHeight);
        }
        public void updateList(List<String> blueprints) {
            String selected = this.getSelected() != null ? this.getSelected().path : null;
            this.clearEntries();
            for (String bp : blueprints) {
                BoundEntry entry = new BoundEntry(bp);
                this.addEntry(entry);
                if (bp.equals(selected)) this.setSelected(entry);
            }
        }
        @Override
        public int getRowWidth() { return this.width - 4; }
    }

    private class BoundEntry extends ObjectSelectionList.Entry<BoundEntry> {
        final String path;
        public BoundEntry(String path) { this.path = path; }

        @Override
        public void renderContent(GuiGraphics guiGraphics, int index, int top, boolean isHovered, float partialTick) {
            int x = this.getX();
            int y = this.getY();
            if (y <= 0) y = top;
            int width = this.getWidth();
            int height = this.getHeight();

            if (isHovered) {
                guiGraphics.fill(x, y, x + width, y + height, 0x44FFFFFF);
            }

            String name = path;
            if (name.endsWith(".json")) name = name.substring(0, name.length() - 5);
            guiGraphics.drawString(minecraft.font, name, x + 4, y + 5, 0xFFFFFFFF, false);
            
            if (isHovered) {
                guiGraphics.drawString(minecraft.font, "✕", x + width - 12, y + 5, 0xFFFF5555, false);
            }
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean isDouble) {
            // 如果点击的是右侧的 X 按钮 (大致范围)
            if (event.x() > this.getX() + this.getWidth() - 15) {
                NetworkService.getInstance().sendWorkbenchAction(WorkbenchActionPayload.Action.UNBIND, path);
                return true;
            }
            return false;
        }

        @Override
        public Component getNarration() { return Component.literal(path); }
    }
}
