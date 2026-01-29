package ltd.opens.mg.mc.client.gui.blueprint.menu;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class BlueprintMenuRenderer {

    public static void renderScrollbar(GuiGraphics guiGraphics, int x, int y, int width, int height, double scroll, int totalHeight) {
        if (totalHeight <= height) return;
        guiGraphics.fill(x, y, x + width, y + height, 0x22FFFFFF);
        int barHeight = (int) ((height / (float) totalHeight) * height);
        int barY = y + (int) ((scroll / (float) totalHeight) * height);
        guiGraphics.fill(x, barY, x + width, barY + barHeight, 0x88FFFFFF);
    }

    public static void renderHighlightedString(GuiGraphics guiGraphics, Font font, String text, int x, int y, int color, String query) {
        if (query.isEmpty()) {
            guiGraphics.drawString(font, text, x, y, color, false);
            return;
        }

        String activeQuery = query;
        int matchStart = -1;
        int matchEnd = -1;

        // Helper to perform the match
        java.util.function.BiFunction<String, String, int[]> performMatch = (t, q) -> {
            if (q.contains("*") || q.contains("?")) {
                String regex = q.replace(".", "\\.").replace("?", ".").replace("*", ".*");
                try {
                    java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex, java.util.regex.Pattern.CASE_INSENSITIVE);
                    java.util.regex.Matcher matcher = pattern.matcher(t);
                    if (matcher.find()) return new int[]{matcher.start(), matcher.end()};
                } catch (Exception ignored) {
                }
            } else {
                int start = t.toLowerCase().indexOf(q.toLowerCase());
                if (start != -1) return new int[]{start, start + q.length()};
            }
            return null;
        };

        int[] match = performMatch.apply(text, activeQuery);
        if (match != null) {
            matchStart = match[0];
            matchEnd = match[1];
        } else if (query.contains("/")) {
            // Try matching parts of the path
            String[] parts = query.split("/");
            for (int i = parts.length - 1; i >= 0; i--) {
                if (parts[i].isEmpty()) continue;
                match = performMatch.apply(text, parts[i]);
                if (match != null) {
                    matchStart = match[0];
                    matchEnd = match[1];
                    activeQuery = parts[i];
                    break;
                }
            }
        }

        if (matchStart == -1) {
            guiGraphics.drawString(font, text, x, y, color, false);
            return;
        }

        // Before match
        String before = text.substring(0, matchStart);
        guiGraphics.drawString(font, before, x, y, color, false);
        int curX = x + font.width(before);

        // Match (Highlight with yellow)
        String matchText = text.substring(matchStart, matchEnd);
        guiGraphics.drawString(font, matchText, curX, y, 0xFFFFFF00, false);
        curX += font.width(matchText);

        // After match
        String after = text.substring(matchEnd);
        guiGraphics.drawString(font, after, curX, y, color, false);
    }

    public static void renderBackground(GuiGraphics guiGraphics, int x, int y, int width, int height) {
        guiGraphics.fill(x, y, x + width, y + height, 0xF01E1E1E);
        guiGraphics.renderOutline(x, y, width, height, 0xFF444444);
    }

    public static void renderSearchBox(GuiGraphics guiGraphics, Font font, int x, int y, int width, int height, BlueprintMenu menu, Component hint) {
        guiGraphics.fill(x, y, x + width, y + height, 0xF0121212);
        guiGraphics.renderOutline(x, y, width, height, 0xFF555555);
        
        EditBox editBox = menu.getSearchEditBox();
        if (editBox == null) return;

        // 设置 EditBox 的实际位置和大小，以便正确渲染和处理点击
        editBox.setX(x + 8);
        editBox.setY(y + (height - 9) / 2);
        editBox.setWidth(width - 16);
        // editBox.setHeight(height); // EditBox height usually fixed by font

        if (editBox.getValue().isEmpty()) {
            guiGraphics.drawString(font, hint, x + 8, y + (height - 9) / 2, 0xFF888888, false);
        }
        
        // 使用 EditBox 原生的渲染逻辑，它会处理光标和选择高亮
        editBox.render(guiGraphics, 0, 0, 0);
    }
}


