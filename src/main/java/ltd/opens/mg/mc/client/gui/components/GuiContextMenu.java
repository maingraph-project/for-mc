package ltd.opens.mg.mc.client.gui.components;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import java.util.ArrayList;
import java.util.List;

/**
 * 通用的右键菜单组件，用于统一各个页面的右键操作视觉和逻辑。
 */
public class GuiContextMenu {
    public static class MenuItem {
        public final Component label;
        public final Runnable action;
        public final int color;

        public MenuItem(Component label, Runnable action) {
            this(label, action, 0xFFFFFFFF);
        }

        public MenuItem(Component label, Runnable action, int color) {
            this.label = label;
            this.action = action;
            this.color = color;
        }
    }

    private final List<MenuItem> items = new ArrayList<>();
    private double x, y;
    private int width = 100;
    private int itemHeight = 20;
    private boolean visible = false;
    
    // 缓存最后的渲染位置，用于点击判定
    private int lastRenderX, lastRenderY, lastRenderW, lastRenderH;

    public void show(double x, double y, List<MenuItem> items) {
        this.x = x;
        this.y = y;
        this.items.clear();
        this.items.addAll(items);
        this.visible = true;
    }

    public void hide() {
        this.visible = false;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void render(GuiGraphics graphics, Font font, int mouseX, int mouseY, int screenWidth, int screenHeight) {
        if (!visible || items.isEmpty()) return;

        int menuX = (int) x;
        int menuY = (int) y;
        int menuHeight = items.size() * itemHeight;

        // 边界检查，防止菜单超出屏幕
        if (menuX + width > screenWidth) menuX -= width;
        if (menuY + menuHeight > screenHeight) menuY -= menuHeight;
        
        // 确保不会超出左侧或上方
        if (menuX < 0) menuX = 0;
        if (menuY < 0) menuY = 0;

        lastRenderX = menuX;
        lastRenderY = menuY;
        lastRenderW = width;
        lastRenderH = menuHeight;

        // 绘制背景和边框
        graphics.fill(menuX, menuY, menuX + width, menuY + menuHeight, 0xFF202020);
        graphics.renderOutline(menuX, menuY, width, menuHeight, 0xFFFFFFFF);

        for (int i = 0; i < items.size(); i++) {
            MenuItem item = items.get(i);
            int itemY = menuY + i * itemHeight;
            boolean hovered = mouseX >= menuX && mouseX <= menuX + width && mouseY >= itemY && mouseY <= itemY + itemHeight;

            if (hovered) {
                graphics.fill(menuX + 1, itemY + 1, menuX + width - 1, itemY + itemHeight - 1, 0xFF404040);
            }

            graphics.drawString(font, item.label, menuX + 10, itemY + (itemHeight - 8) / 2, item.color, false);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return false;

        if (mouseX >= lastRenderX && mouseX <= lastRenderX + lastRenderW &&
            mouseY >= lastRenderY && mouseY <= lastRenderY + lastRenderH) {
            
            int index = (int)((mouseY - lastRenderY) / itemHeight);
            if (index >= 0 && index < items.size()) {
                items.get(index).action.run();
            }
            visible = false;
            return true;
        }

        visible = false;
        return false; // 点击菜单外部，关闭但不一定消费事件（视情况而定，这里返回 false 让底层处理）
    }
}
