package ltd.opens.mg.mc.client.gui.blueprint.render;


import ltd.opens.mg.mc.client.gui.components.*;
import net.minecraft.client.gui.GuiGraphics;
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

        // Dynamic segments based on distance and zoom
        float pixelDist = (float) Math.sqrt((x2-x1)*(x2-x1) + (y2-y1)*(y2-y1));
        int maxSegments = zoom > 0.4f ? 30 : 12;
        int segments = Math.max(4, Math.min(maxSegments, (int)(pixelDist / (zoom > 0.4f ? 10 : 25))));
        
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
}


