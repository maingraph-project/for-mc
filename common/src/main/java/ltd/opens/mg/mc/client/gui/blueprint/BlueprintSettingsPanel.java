package ltd.opens.mg.mc.client.gui.blueprint;

import com.mojang.blaze3d.systems.RenderSystem;
import ltd.opens.mg.mc.client.gui.screens.BlueprintScreen;
import ltd.opens.mg.mc.client.gui.blueprint.settings.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlueprintSettingsPanel {
    
    static {
        // Initialize settings
        SettingsHelper.setup("snap_guides", "gui.mgmc.settings.snap_guides")
            .description("gui.mgmc.settings.snap_guides.desc")
            .type(SettingType.BOOLEAN)
            .defaultValue(true)
            .register();

        SettingsHelper.setup("save_button_highlight", "gui.mgmc.settings.save_button_highlight")
            .description("gui.mgmc.settings.save_button_highlight.desc")
            .type(SettingType.BOOLEAN)
            .defaultValue(true)
            .register();

        SettingsHelper.setup("snap_threshold", "gui.mgmc.settings.snap_threshold")
            .description("gui.mgmc.settings.snap_threshold.desc")
            .type(SettingType.FLOAT)
            .defaultValue(10.0f)
            .range(1.0f, 50.0f)
            .register();
    }

    // Slider geometry cache: id -> {x, y, w, h} (screen coords, updated each render frame)
    private static final Map<String, int[]> sliderRects = new HashMap<>();
    private static String draggingSettingId = null;

    public static void render(GuiGraphics guiGraphics, BlueprintScreen screen, BlueprintState state, Font font, int mouseX, int mouseY) {
        // Animation
        float target = state.showSettings ? 1.0f : 0.0f;
        state.settingsAnimProgress += (target - state.settingsAnimProgress) * 0.2f; // Smooth transition
        
        if (state.settingsAnimProgress > 0.01f) {
            float scale = state.settingsAnimProgress;
            int width = screen.width;
            int height = screen.height;
            int menuW = 200;
            int menuH = 150;
            int menuX = (width - menuW) / 2;
            int menuY = (height - menuH) / 2;
            
            // Apply scaling effect from center
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(width / 2.0, height / 2.0, 0);
            guiGraphics.pose().scale(scale, scale, 1.0f);
            guiGraphics.pose().translate(-width / 2.0, -height / 2.0, 0);

            // Background
            guiGraphics.fill(0, 0, width, height, ((int)(0x88 * scale) << 24)); // Dim background with fade
            guiGraphics.fill(menuX, menuY, menuX + menuW, menuY + menuH, 0xFF2D2D2D);
            guiGraphics.renderOutline(menuX, menuY, menuW, menuH, 0xFF555555);

            // Title
            String settingsTitle = Component.translatable("gui.mgmc.settings.title").getString(); // "Editor Settings"
            guiGraphics.drawString(font, settingsTitle, menuX + 10, menuY + 10, 0xFFFFFFFF, false);
            guiGraphics.fill(menuX, menuY + 25, menuX + menuW, menuY + 26, 0xFF555555);

            // Close Button (X)
            guiGraphics.drawString(font, "×", menuX + menuW - 15, menuY + 10, 0xFFAAAAAA, false);

            // Scissor for scrolling content
            guiGraphics.enableScissor(menuX, menuY + 26, menuX + menuW, menuY + menuH);

            int currentY = (int)(menuY + 35 - state.settingsScrollY);
            Collection<SettingsDefinition> settings = SettingsRegistry.getDefinitions();
            sliderRects.clear();

            for (SettingsDefinition def : settings) {
                if (def.type == SettingType.BOOLEAN) {
                    // Title
                    String title = Component.translatable(def.labelKey).getString();
                    boolean isEnabled = SettingsRegistry.getBoolean(def.id);
                    
                    guiGraphics.drawString(font, title, menuX + 10, currentY, 0xFFEEEEEE, false);
                    
                    // Toggle Switch
                    int toggleX = menuX + menuW - 30;
                    int toggleY = currentY;
                    int toggleW = 20;
                    int toggleH = 10;
                    
                    guiGraphics.fill(toggleX, toggleY, toggleX + toggleW, toggleY + toggleH, isEnabled ? 0xFF44AA44 : 0xFF884444);
                    guiGraphics.renderOutline(toggleX, toggleY, toggleW, toggleH, 0xFF000000);
                    if (isEnabled) {
                        guiGraphics.fill(toggleX + toggleW - 8, toggleY + 1, toggleX + toggleW - 1, toggleY + toggleH - 1, 0xFFFFFFFF);
                    } else {
                        guiGraphics.fill(toggleX + 1, toggleY + 1, toggleX + 8, toggleY + toggleH - 1, 0xFFFFFFFF);
                    }

                    // Description
                    if (def.descriptionKey != null) {
                        Component desc = Component.translatable(def.descriptionKey);
                        List<FormattedCharSequence> lines = font.split(desc, menuW - 20);
                        int lineY = currentY + 15;
                        for (FormattedCharSequence line : lines) {
                            guiGraphics.drawString(font, line, menuX + 10, lineY, 0xFFAAAAAA, false);
                            lineY += 10;
                        }
                        currentY = lineY + 10; // Spacing after item
                    } else {
                        currentY += 20;
                    }
                } else if (def.type == SettingType.FLOAT) {
                    float value = SettingsRegistry.getFloat(def.id);
                    float min = def.min;
                    float max = def.max;
                    float t = (max > min) ? (value - min) / (max - min) : 0.0f;
                    t = Math.max(0.0f, Math.min(1.0f, t));

                    // Title
                    String title = Component.translatable(def.labelKey).getString();
                    guiGraphics.drawString(font, title, menuX + 10, currentY, 0xFFEEEEEE, false);

                    // Slider track
                    int sliderX = menuX + 10;
                    int sliderY = currentY + 15;
                    int sliderW = menuW - 20;
                    int sliderH = 6;
                    guiGraphics.fill(sliderX, sliderY, sliderX + sliderW, sliderY + sliderH, 0xFF555555);
                    int knobX = sliderX + (int)(sliderW * t) - 3;
                    guiGraphics.fill(knobX, sliderY - 2, knobX + 6, sliderY + sliderH + 2, 0xFF44AAFF);
                    sliderRects.put(def.id, new int[]{sliderX, sliderY, sliderW, sliderH});

                    // Value text
                    String valStr = String.format("%.1f", value);
                    int valW = font.width(valStr);
                    guiGraphics.drawString(font, valStr, menuX + menuW - 10 - valW, currentY, 0xFFAAAAAA, false);

                    // Description
                    int lineY = sliderY + sliderH + 4;
                    if (def.descriptionKey != null) {
                        Component desc = Component.translatable(def.descriptionKey);
                        List<FormattedCharSequence> lines = font.split(desc, menuW - 20);
                        for (FormattedCharSequence line : lines) {
                            guiGraphics.drawString(font, line, menuX + 10, lineY, 0xFFAAAAAA, false);
                            lineY += 10;
                        }
                    }
                    currentY = lineY + 10; // Spacing after item
                }
                // Handle other types...
            }
            
            // Update content height for scrolling
            float contentHeight = currentY - (menuY + 35 - state.settingsScrollY); // Total height
            if (contentHeight < 100) contentHeight = 100;
            state.settingsContentHeight = contentHeight;

            guiGraphics.disableScissor();
            
            // Scrollbar
            float maxScroll = Math.max(0, state.settingsContentHeight - (menuH - 30));
            if (maxScroll > 0) {
                int scrollBarH = (int)((menuH - 30) * ((menuH - 30) / state.settingsContentHeight));
                if (scrollBarH < 20) scrollBarH = 20;
                int scrollBarY = menuY + 26 + (int)((state.settingsScrollY / maxScroll) * (menuH - 30 - scrollBarH));
                guiGraphics.fill(menuX + menuW - 6, scrollBarY, menuX + menuW - 2, scrollBarY + scrollBarH, 0xFF888888);
            }

            guiGraphics.pose().popPose();
        }
    }

    public static boolean mouseClicked(BlueprintScreen screen, BlueprintState state, double mouseX, double mouseY, int button) {
        if (state.showSettings || state.settingsAnimProgress > 0.01f) {
            int menuW = 200;
            int menuH = 150;
            int menuX = (screen.width - menuW) / 2;
            int menuY = (screen.height - menuH) / 2;
            
            // Close button (X)
            if (state.showSettings && isHovering((int)mouseX, (int)mouseY, menuX + menuW - 15, menuY, 15, 25)) {
                state.showSettings = false;
                return true;
            }
            
            // Iterate settings to check clicks
            int currentY = (int)(menuY + 35 - state.settingsScrollY);
            Collection<SettingsDefinition> settings = SettingsRegistry.getDefinitions();
            
            for (SettingsDefinition def : settings) {
                if (def.type == SettingType.BOOLEAN) {
                    int itemHeight = 20;
                    if (def.descriptionKey != null) {
                        // Calculate height (approximate or re-calculate)
                        // For simplicity, assume click area is the toggle button
                        Component desc = Component.translatable(def.descriptionKey);
                        List<FormattedCharSequence> lines = Minecraft.getInstance().font.split(desc, menuW - 20);
                        itemHeight = 15 + lines.size() * 10 + 10;
                    }

                    // Check Toggle Switch
                    // Only if within visible area
                    if (currentY > menuY + 25 && currentY < menuY + menuH) {
                        int toggleX = menuX + menuW - 30;
                        int toggleY = currentY;
                        if (state.showSettings && isHovering((int)mouseX, (int)mouseY, toggleX, toggleY, 20, 10)) {
                            boolean current = SettingsRegistry.getBoolean(def.id);
                            SettingsRegistry.set(def.id, !current);
                            return true;
                        }
                    }
                    currentY += itemHeight;
                } else if (def.type == SettingType.FLOAT) {
                    int sliderY = currentY + 15;
                    int lineY = sliderY + 6 + 4;
                    if (def.descriptionKey != null) {
                        Component desc = Component.translatable(def.descriptionKey);
                        List<FormattedCharSequence> lines = Minecraft.getInstance().font.split(desc, menuW - 20);
                        lineY += lines.size() * 10;
                    }
                    currentY = lineY + 10;

                    // Check slider drag start
                    int[] rect = sliderRects.get(def.id);
                    if (state.showSettings && rect != null
                            && isHovering((int)mouseX, (int)mouseY, rect[0] - 4, rect[1] - 4, rect[2] + 8, rect[3] + 8)) {
                        applyFloat(def, rect, (int)mouseX);
                        draggingSettingId = def.id;
                        return true;
                    }
                }
            }
            
            // Consume clicks inside menu
            if (state.showSettings && isHovering((int)mouseX, (int)mouseY, menuX, menuY, menuW, menuH)) {
                return true;
            }
            
            // Click outside closes menu
            if (state.showSettings) {
                state.showSettings = false;
                return true;
            }
        }
        return false;
    }

    public static boolean mouseScrolled(BlueprintScreen screen, BlueprintState state, double mouseX, double mouseY, double scrollVertical) {
        if (state.showSettings) {
            int menuW = 200;
            int menuH = 150;
            int menuX = (screen.width - menuW) / 2;
            int menuY = (screen.height - menuH) / 2;
            
            if (isHovering((int)mouseX, (int)mouseY, menuX, menuY, menuW, menuH)) {
                state.settingsScrollY -= scrollVertical * 10;
                float maxScroll = Math.max(0, state.settingsContentHeight - (menuH - 30));
                state.settingsScrollY = Math.max(0, Math.min(state.settingsScrollY, maxScroll));
                return true;
            }
        }
        return false;
    }
    
    public static boolean mouseDragged(BlueprintScreen screen, BlueprintState state, double mouseX, double mouseY, int button) {
        if (draggingSettingId != null) {
            int[] rect = sliderRects.get(draggingSettingId);
            SettingsDefinition def = findDef(draggingSettingId);
            if (rect != null && def != null) {
                applyFloat(def, rect, (int) mouseX);
                return true;
            }
            draggingSettingId = null;
        }
        return false;
    }

    public static boolean mouseReleased(BlueprintScreen screen, BlueprintState state, double mouseX, double mouseY, int button) {
        if (draggingSettingId != null) {
            draggingSettingId = null;
            return true;
        }
        return false;
    }

    private static void applyFloat(SettingsDefinition def, int[] rect, int mouseX) {
        float t = (mouseX - rect[0]) / (float) rect[2];
        t = Math.max(0.0f, Math.min(1.0f, t));
        float value = def.min + t * (def.max - def.min);
        value = Math.round(value * 10.0f) / 10.0f; // snap to 1 decimal place
        SettingsRegistry.set(def.id, value);
    }

    private static SettingsDefinition findDef(String id) {
        for (SettingsDefinition def : SettingsRegistry.getDefinitions()) {
            if (def.id.equals(id)) {
                return def;
            }
        }
        return null;
    }

    private static boolean isHovering(int mouseX, int mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX < x + w && mouseY >= y && mouseY < y + h;
    }
}
