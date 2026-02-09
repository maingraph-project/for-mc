package ltd.opens.mg.mc.client.gui.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;

public class BlueprintHelpScreen extends Screen {
    private final Screen parent;
    private int selectedCategory = 0;
    private final List<Component> categories = new ArrayList<>();
    private final int sidebarWidth = 100;

    public BlueprintHelpScreen(Screen parent) {
        super(Component.translatable("gui.mgmc.help.title"));
        this.parent = parent;
        categories.add(Component.translatable("gui.mgmc.help.category.shortcuts"));
    }

    @Override
    protected void init() {
        // Back Button
        this.addRenderableWidget(Button.builder(Component.translatable("gui.mgmc.blueprint_selection.back"), b -> {
            Minecraft.getInstance().setScreen(this.parent);
        }).bounds(5, this.height - 25, 90, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 1. 首先绘制背景（模糊/暗化）
        this.renderTransparentBackground(guiGraphics);

        // 2. 绘制侧边栏背景
        guiGraphics.fill(0, 0, sidebarWidth, this.height, 0xAA222222);
        guiGraphics.fill(sidebarWidth - 1, 0, sidebarWidth, this.height, 0xFF555555);

        // 3. 绘制分类文字
        for (int i = 0; i < categories.size(); i++) {
            boolean isSelected = i == selectedCategory;
            boolean isHovered = mouseX >= 5 && mouseX <= sidebarWidth - 5 && mouseY >= 10 + i * 25 && mouseY <= 10 + i * 25 + 20;
            
            int color = isSelected ? 0xFFFFFFFF : (isHovered ? 0xFFCCCCCC : 0xFFAAAAAA);
            if (isSelected) {
                guiGraphics.fill(5, 10 + i * 25, sidebarWidth - 5, 10 + i * 25 + 20, 0x44FFFFFF);
            }
            
            guiGraphics.drawString(this.font, categories.get(i), 10, 15 + i * 25, color);
        }

        // 4. 绘制主内容区域
        int contentX = sidebarWidth + 20;
        int contentY = 20;
        int contentWidth = this.width - sidebarWidth - 40;

        if (selectedCategory == 0) {
            renderShortcuts(guiGraphics, contentX, contentY, contentWidth);
        }

        // 5. 最后绘制按钮等组件，确保它们在最上层
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        // 覆盖此方法并留空，防止 super.render 再次绘制背景导致变暗或遮挡
    }

    private void renderShortcuts(GuiGraphics guiGraphics, int x, int y, int width) {
        int yOffset = y;
        guiGraphics.drawString(this.font, Component.translatable("gui.mgmc.help.shortcuts.title"), x, yOffset, 0xFFFFAA00);
        yOffset += 20;

        Object[][] shortcuts = {
            {"Ctrl + C", Component.translatable("gui.mgmc.help.shortcuts.copy")},
            {"Ctrl + V", Component.translatable("gui.mgmc.help.shortcuts.paste")},
            {"Del / Backspace", Component.translatable("gui.mgmc.help.shortcuts.delete")},
            {"Ctrl + Z", Component.translatable("gui.mgmc.help.shortcuts.undo")},
            {"Ctrl + Y / Ctrl+Shift+Z", Component.translatable("gui.mgmc.help.shortcuts.redo")},
            {"M", Component.translatable("gui.mgmc.help.shortcuts.minimap")},
            {"Ctrl + P", Component.translatable("gui.mgmc.help.shortcuts.search")},
            {"长按 W", Component.translatable("gui.mgmc.help.shortcuts.web")},
            {"双击左键", Component.translatable("gui.mgmc.help.shortcuts.edit_marker")},
            {"右键/中键拖拽", Component.translatable("gui.mgmc.help.shortcuts.pan")},
            {"滚轮", Component.translatable("gui.mgmc.help.shortcuts.zoom")},
            {"左键框选", Component.translatable("gui.mgmc.help.shortcuts.box_select")},
            {"Shift/Ctrl + 左键", Component.translatable("gui.mgmc.help.shortcuts.multi_select")}
        };

        for (Object[] shortcut : shortcuts) {
            guiGraphics.drawString(this.font, (String)shortcut[0], x, yOffset, 0xFFFFFFFF);
            guiGraphics.drawString(this.font, (Component)shortcut[1], x + 150, yOffset, 0xFFAAAAAA);
            yOffset += 14;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            for (int i = 0; i < categories.size(); i++) {
                if (mouseX >= 5 && mouseX <= sidebarWidth - 5 && mouseY >= 10 + i * 25 && mouseY <= 10 + i * 25 + 20) {
                    selectedCategory = i;
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(this.parent);
    }
}
