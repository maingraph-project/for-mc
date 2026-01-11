package ltd.opens.mg.mc.client.gui.blueprint.render;


import ltd.opens.mg.mc.client.gui.blueprint.BlueprintState;
import ltd.opens.mg.mc.client.gui.components.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import java.util.List;

public class BlueprintRenderer {

    public static void drawGrid(GuiGraphics guiGraphics, int width, int height, float panX, float panY, float zoom) {
        // Fill background first
        guiGraphics.fill(0, 0, width, height, 0xFF121212);

        float scaledGridSize = 20 * zoom;
        // Optimized grid: Only draw if zoom is large enough, and use fewer lines
        if (zoom > 0.5f) {
            int color = 0xFF262626;
            float startX = panX % scaledGridSize;
            if (startX > 0) startX -= scaledGridSize;
            float startY = panY % scaledGridSize;
            if (startY > 0) startY -= scaledGridSize;

            for (float x = startX; x < width; x += scaledGridSize) {
                guiGraphics.fill((int) x, 0, (int) x + 1, height, color);
            }
            for (float y = startY; y < height; y += scaledGridSize) {
                guiGraphics.fill(0, (int) y, width, (int) y + 1, color);
            }
        }
        
        // Draw larger grid lines every 5 small grid squares
        float largeGridSize = 100 * zoom;
        if (largeGridSize > 10) { // Only draw if visible
            float largeStartX = panX % largeGridSize;
            if (largeStartX > 0) largeStartX -= largeGridSize;
            float largeStartY = panY % largeGridSize;
            if (largeStartY > 0) largeStartY -= largeGridSize;
            
            int largeColor = 0xFF333333;
            for (float x = largeStartX; x < width; x += largeGridSize) {
                guiGraphics.fill((int) x, 0, (int) x + 1, height, largeColor);
            }
            for (float y = largeStartY; y < height; y += largeGridSize) {
                guiGraphics.fill(0, (int) y, width, (int) y + 1, largeColor);
            }
        }
    }

    public static void drawConnections(GuiGraphics guiGraphics, List<GuiConnection> connections, int screenWidth, int screenHeight, float panX, float panY, float zoom) {
        for (GuiConnection conn : connections) {
            float[] outPos = conn.from.getPortPositionByName(conn.fromPort, false);
            float[] inPos = conn.to.getPortPositionByName(conn.toPort, true);
            
            // Screen-space coordinates
            float sX1 = outPos[0] * zoom + panX;
            float sY1 = outPos[1] * zoom + panY;
            float sX2 = inPos[0] * zoom + panX;
            float sY2 = inPos[1] * zoom + panY;

            // Simple culling for connections
            if ((sX1 < 0 && sX2 < 0) || (sX1 > screenWidth && sX2 > screenWidth) ||
                (sY1 < 0 && sY2 < 0) || (sY1 > screenHeight && sY2 > screenHeight)) {
                continue;
            }

            drawBezier(guiGraphics, outPos[0], outPos[1], inPos[0], inPos[1], 0xAAFFFFFF, zoom); // Slightly transparent for better look
        }
    }

    public static void drawBezier(GuiGraphics guiGraphics, float x1, float y1, float x2, float y2, int color, float zoom) {
        // LOD for connections
        if (zoom < 0.15f) {
            // Straight line for very far zoom
            drawLine(guiGraphics, x1, y1, x2, y2, color);
            return;
        }

        float dist = Math.abs(x2 - x1) * 0.5f;
        if (dist < 20) dist = 20;
        
        float cp1x = x1 + dist;
        float cp1y = y1;
        float cp2x = x2 - dist;
        float cp2y = y2;

        // Dynamic segments based on length and zoom (LOD)
        float pixelDist = (float) Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
        int segments = (int) Math.max(4, Math.min(40, pixelDist * zoom / 7.0f));
        
        float lastX = x1;
        float lastY = y1;

        for (int i = 1; i <= segments; i++) {
            float t = i / (float) segments;
            float invT = 1.0f - t;
            
            // Cubic Bezier formula
            float b0 = invT * invT * invT;
            float b1 = 3 * invT * invT * t;
            float b2 = 3 * invT * t * t;
            float b3 = t * t * t;
            
            float x = b0 * x1 + b1 * cp1x + b2 * cp2x + b3 * x2;
            float y = b0 * y1 + b1 * cp1y + b2 * cp2y + b3 * y2;
            
            drawLine(guiGraphics, lastX, lastY, x, y, color);
            lastX = x;
            lastY = y;
        }
    }

    public static void drawLine(GuiGraphics guiGraphics, float x1, float y1, float x2, float y2, int color) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        float len = (float) Math.sqrt(dx * dx + dy * dy);
        if (len < 0.5f) return;

        // Use PoseStack for efficient rotated rectangle drawing
        guiGraphics.pose().pushMatrix();
        guiGraphics.pose().translate(x1, y1);
        
        float angle = (float) Math.atan2(dy, dx);
        guiGraphics.pose().rotate(angle);
        
        // Draw the line as a thin rectangle
        // Thickness is 2.0f
        guiGraphics.fill(0, -1, (int)len, 1, color);
        
        guiGraphics.pose().popMatrix();
    }

    public static void drawSelectionBox(GuiGraphics guiGraphics, BlueprintState state) {
        if (!state.isBoxSelecting) return;

        int x1 = (int) state.boxSelectStartX;
        int y1 = (int) state.boxSelectStartY;
        int x2 = (int) state.boxSelectEndX;
        int y2 = (int) state.boxSelectEndY;

        int minX = Math.min(x1, x2);
        int minY = Math.min(y1, y2);
        int maxX = Math.max(x1, x2);
        int maxY = Math.max(y1, y2);

        // Draw translucent fill
        guiGraphics.fill(minX, minY, maxX, maxY, 0x334488FF);
        // Draw outline
        guiGraphics.renderOutline(minX, minY, maxX - minX, maxY - minY, 0xFF4488FF);
    }

    public static void drawMinimap(GuiGraphics guiGraphics, BlueprintState state, int screenWidth, int screenHeight) {
        if (!state.showMinimap || state.nodes.isEmpty()) return;

        // Minimap settings
        int minimapWidth = 120;
        int minimapHeight = 80;
        int margin = 10;
        int x = screenWidth - minimapWidth - margin;
        int y = screenHeight - minimapHeight - margin;

        // Draw background
        guiGraphics.fill(x, y, x + minimapWidth, y + minimapHeight, 0xAA121212);
        guiGraphics.renderOutline(x, y, minimapWidth, minimapHeight, 0xFF444444);

        // Find bounds of all nodes
        float minX = Float.MAX_VALUE, minY = Float.MAX_VALUE;
        float maxX = Float.MIN_VALUE, maxY = Float.MIN_VALUE;
        for (GuiNode node : state.nodes) {
            minX = Math.min(minX, node.x);
            minY = Math.min(minY, node.y);
            maxX = Math.max(maxX, node.x + node.width);
            maxY = Math.max(maxY, node.y + node.height);
        }

        // Add some padding to bounds
        float padding = 100;
        minX -= padding; minY -= padding;
        maxX += padding; maxY += padding;

        float contentWidth = maxX - minX;
        float contentHeight = maxY - minY;
        float scale = Math.min(minimapWidth / contentWidth, minimapHeight / contentHeight);

        // Center content in minimap
        float offsetX = x + (minimapWidth - contentWidth * scale) / 2f - minX * scale;
        float offsetY = y + (minimapHeight - contentHeight * scale) / 2f - minY * scale;

        // Draw nodes as small dots/rects
        for (GuiNode node : state.nodes) {
            int nx = (int) (node.x * scale + offsetX);
            int ny = (int) (node.y * scale + offsetY);
            int nw = (int) Math.max(1, node.width * scale);
            int nh = (int) Math.max(1, node.height * scale);
            
            int color = 0xCC888888;
            if (node.definition.properties().containsKey("is_marker")) {
                color = 0xCC448844; // Marker color in minimap
            }
            guiGraphics.fill(nx, ny, nx + nw, ny + nh, color);
        }

        // Draw current view area
        float viewX = (-state.panX / state.zoom);
        float viewY = (-state.panY / state.zoom);
        float viewW = (screenWidth / state.zoom);
        float viewH = (screenHeight / state.zoom);

        int vx = (int) (viewX * scale + offsetX);
        int vy = (int) (viewY * scale + offsetY);
        int vw = (int) (viewW * scale);
        int vh = (int) (viewH * scale);

        // Clip view area to minimap bounds
        int cvx = Math.max(x, vx);
        int cvy = Math.max(y, vy);
        int cvw = Math.min(x + minimapWidth, vx + vw) - cvx;
        int cvh = Math.min(y + minimapHeight, vy + vh) - cvy;

        if (cvw > 0 && cvh > 0) {
            guiGraphics.fill(cvx, cvy, cvx + cvw, cvy + cvh, 0x22FFFFFF);
            guiGraphics.renderOutline(cvx, cvy, cvw, cvh, 0x66FFFFFF);
        }
    }

    public static void drawMarkerEditing(GuiGraphics guiGraphics, BlueprintState state, net.minecraft.client.gui.Font font) {
        if (state.editingMarkerNode == null || state.markerEditBox == null) return;

        GuiNode node = state.editingMarkerNode;
        // Project node to screen space
        int sx = (int) (node.x * state.zoom + state.panX);
        int sy = (int) (node.y * state.zoom + state.panY);
        int sw = (int) (node.width * state.zoom);

        // Header height in screen space
        int headerH = (int) (node.headerHeight * state.zoom);

        // Position EditBox in the content area of the node
        state.markerEditBox.setX(sx + (int)(10 * state.zoom));
        state.markerEditBox.setY(sy + headerH + (int)(10 * state.zoom));
        state.markerEditBox.setWidth(sw - (int)(20 * state.zoom));
        // EditBox height is fixed for single line, but we might want it to look okay
        // Minecraft's EditBox doesn't natively support multiline well, but for input it's fine.
        // If we want multiline editing, we might need a custom component, 
        // but let's stick with EditBox for now as it's better than a modal.
        
        state.markerEditBox.render(guiGraphics, 0, 0, 0);
    }

    public static void drawQuickSearch(GuiGraphics guiGraphics, BlueprintState state, int width, int height, net.minecraft.client.gui.Font font) {
        if (!state.showQuickSearch) return;

        int searchW = 200;
        int searchH = 40;
        int x = (width - searchW) / 2;
        int y = height / 4;

        // Overlay
        guiGraphics.fill(0, 0, width, height, 0x44000000);

        // Background
        guiGraphics.fill(x, y, x + searchW, y + searchH, 0xF01A1A1A);
        guiGraphics.renderOutline(x, y, searchW, searchH, 0xFFFFFFFF);

        // Label
        String label = Component.translatable("gui.mgmc.quick_search.label").getString();
        guiGraphics.drawString(font, label, x + 10, y + 8, 0xFFAAAAAA, false);

        // Search text
        if (state.quickSearchEditBox != null) {
            state.quickSearchEditBox.setX(x + 10);
            state.quickSearchEditBox.setY(y + 22);
            state.quickSearchEditBox.render(guiGraphics, 0, 0, 0);
        }

        // Candidates list or History
        List<GuiNode> displayList = (state.quickSearchEditBox != null && state.quickSearchEditBox.getValue().isEmpty()) ? state.searchHistory : state.quickSearchMatches;
        
        if (!displayList.isEmpty()) {
            int itemHeight = 18;
            int listY = y + searchH + 2;
            int visibleCount = Math.min(displayList.size(), BlueprintState.MAX_QUICK_SEARCH_VISIBLE);
            int listHeight = visibleCount * itemHeight + 6;
            
            // List Background
            guiGraphics.fill(x, listY, x + searchW, listY + listHeight, 0xF01A1A1A);
            guiGraphics.renderOutline(x, listY, searchW, listHeight, 0xFF555555);
            
            // Draw Items
            for (int i = 0; i < visibleCount; i++) {
                int actualIdx = i + state.quickSearchScrollOffset;
                if (actualIdx >= displayList.size()) break;

                GuiNode node = displayList.get(actualIdx);
                int itemTop = listY + 3 + i * itemHeight;
                boolean selected = actualIdx == state.quickSearchSelectedIndex;
                
                // Selection Highlight
                if (selected) {
                    guiGraphics.fill(x + 2, itemTop, x + searchW - 2, itemTop + itemHeight, 0x44FFFFFF);
                    
                    // History Confirm Progress Bar (Golden)
                    if (state.quickSearchEditBox != null && state.quickSearchEditBox.getValue().isEmpty() && state.searchConfirmProgress > 0) {
                        int barW = (int) ((searchW - 4) * state.searchConfirmProgress);
                        guiGraphics.fill(x + 2, itemTop + itemHeight - 2, x + 2 + barW, itemTop + itemHeight, 0xFFFFD700);
                    }
                }
                
                String comment = node.inputValues.has(ltd.opens.mg.mc.core.blueprint.NodePorts.COMMENT) ? 
                                 node.inputValues.get(ltd.opens.mg.mc.core.blueprint.NodePorts.COMMENT).getAsString() : "Marker";
                
                int textOffset = 8;
                // Draw Clock Icon for History (MC Style)
                if (state.quickSearchEditBox != null && state.quickSearchEditBox.getValue().isEmpty()) {
                    guiGraphics.drawString(font, "§6\u231B", x + 8, itemTop + 4, 0xFFFFFFFF, false); // Unicode hourglass as clock placeholder
                    textOffset = 20;
                }
                
                // Truncate comment if too long
                int availableW = searchW - textOffset - 10;
                if (font.width(comment) > availableW) {
                    comment = font.plainSubstrByWidth(comment, availableW - 10) + "...";
                }
                
                guiGraphics.drawString(font, comment, x + textOffset, itemTop + 4, 0xFFFFFFFF, false);

                // Highlight matched text (only for non-history)
                if (state.quickSearchEditBox != null && !state.quickSearchEditBox.getValue().isEmpty()) {
                    String query = state.quickSearchEditBox.getValue().toLowerCase();
                    if (comment.toLowerCase().contains(query)) {
                        int startIdx = comment.toLowerCase().indexOf(query);
                        if (startIdx >= 0) {
                            String prefix = comment.substring(0, startIdx);
                            String match = comment.substring(startIdx, startIdx + query.length());
                            int prefixW = font.width(prefix);
                            int matchW = font.width(match);
                            guiGraphics.fill(x + textOffset + prefixW, itemTop + 14, x + textOffset + prefixW + matchW, itemTop + 15, 0xFF55FF55);
                        }
                    }
                }
            }

            // Scrollbar
            if (displayList.size() > BlueprintState.MAX_QUICK_SEARCH_VISIBLE) {
                int scrollbarX = x + searchW - 4;
                int scrollbarY = listY + 3;
                int scrollbarHeight = listHeight - 6;
                int thumbHeight = (int) ((float) BlueprintState.MAX_QUICK_SEARCH_VISIBLE / displayList.size() * scrollbarHeight);
                int thumbY = scrollbarY + (int) ((float) state.quickSearchScrollOffset / (displayList.size() - BlueprintState.MAX_QUICK_SEARCH_VISIBLE) * (scrollbarHeight - thumbHeight));
                
                guiGraphics.fill(scrollbarX, scrollbarY, scrollbarX + 2, scrollbarY + scrollbarHeight, 0x44000000);
                guiGraphics.fill(scrollbarX, thumbY, scrollbarX + 2, thumbY + thumbHeight, 0xFFAAAAAA);
            }
        } else if (state.quickSearchEditBox != null && !state.quickSearchEditBox.getValue().isEmpty()) {
            // No matches hint
            int listY = y + searchH + 2;
            int listHeight = 24;
            guiGraphics.fill(x, listY, x + searchW, listY + listHeight, 0xF01A1A1A);
            guiGraphics.renderOutline(x, listY, searchW, listHeight, 0xFF555555);
            guiGraphics.drawString(font, Component.translatable("gui.mgmc.blueprint_editor.no_nodes_found"), x + 8, listY + 8, 0xFF888888, false);
        }

        // Results summary
        int count = state.quickSearchMatches.size();
        String query = state.quickSearchEditBox != null ? state.quickSearchEditBox.getValue() : "";
        
        if (!query.isEmpty()) {
            String results = Component.translatable("gui.mgmc.quick_search.results", count).getString();
            guiGraphics.drawString(font, results, x + searchW - font.width(results) - 10, y + 8, 0xFF55FF55, false);
        }
    }
}


