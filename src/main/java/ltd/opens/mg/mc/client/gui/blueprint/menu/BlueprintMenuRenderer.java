package ltd.opens.mg.mc.client.gui.blueprint.menu;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class BlueprintMenuRenderer {

    public static void renderNodeContextMenu(GuiGraphics guiGraphics, Font font, int mouseX, int mouseY, double menuX, double menuY) {
        int x = (int) menuX;
        int y = (int) menuY;
        int width = 120;
        int height = 46;

        // Shadow/Glow
        guiGraphics.fill(x + 2, y + 2, x + width + 2, y + height + 2, 0x44000000);
        // Background
        guiGraphics.fill(x, y, x + width, y + height, 0xF01E1E1E);
        // Border
        guiGraphics.renderOutline(x, y, width, height, 0xFF444444);

        // Delete Node
        boolean hoverDelete = mouseX >= x && mouseX <= x + width && mouseY >= y + 3 && mouseY <= y + 23;
        if (hoverDelete) guiGraphics.fill(x + 1, y + 3, x + width - 1, y + 23, 0x44FFFFFF);
        guiGraphics.drawString(font, Component.translatable("gui.mgmc.blueprint_editor.context_menu.delete"), x + 8, y + 8, 0xFFFFFFFF, false);

        // Break Links
        boolean hoverBreak = mouseX >= x && mouseX <= x + width && mouseY >= y + 23 && mouseY <= y + 43;
        if (hoverBreak) guiGraphics.fill(x + 1, y + 23, x + width - 1, y + 43, 0x44FFFFFF);
        guiGraphics.drawString(font, Component.translatable("gui.mgmc.blueprint_editor.context_menu.break_links"), x + 8, y + 28, 0xFFFFFFFF, false);
    }

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

    public static void renderSearchBox(GuiGraphics guiGraphics, Font font, int x, int y, int width, int height, String query, Component hint) {
        guiGraphics.fill(x, y, x + width, y + height, 0xF0121212);
        guiGraphics.renderOutline(x, y, width, height, 0xFF555555);
        
        String displaySearch = query.isEmpty() ? hint.getString() : query;
        int searchColor = query.isEmpty() ? 0xFF888888 : 0xFFFFFFFF;
        guiGraphics.drawString(font, displaySearch + (System.currentTimeMillis() / 500 % 2 == 0 ? "_" : ""), x + 8, y + (height - 9) / 2, searchColor, false);
    }
}


