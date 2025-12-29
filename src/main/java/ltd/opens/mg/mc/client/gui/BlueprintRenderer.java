package ltd.opens.mg.mc.client.gui;

import net.minecraft.client.gui.GuiGraphics;
import java.util.List;

public class BlueprintRenderer {

    public static void drawGrid(GuiGraphics guiGraphics, int width, int height, float panX, float panY, float zoom) {
        float scaledGridSize = 20 * zoom;
        int color = 0xFF262626;

        // Fill background first
        guiGraphics.fill(0, 0, width, height, 0xFF121212);

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
        
        // Draw larger grid lines every 5 small grid squares
        float largeGridSize = scaledGridSize * 5;
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

    public static void drawConnections(GuiGraphics guiGraphics, List<GuiConnection> connections) {
        for (GuiConnection conn : connections) {
            float[] outPos = conn.from.getPortPositionByName(conn.fromPort, false);
            float[] inPos = conn.to.getPortPositionByName(conn.toPort, true);
            drawBezier(guiGraphics, outPos[0], outPos[1], inPos[0], inPos[1], 0xFFFFFFFF);
        }
    }

    public static void drawBezier(GuiGraphics guiGraphics, float x1, float y1, float x2, float y2, int color) {
        float dist = Math.max(Math.abs(x2 - x1) * 0.5f, 30);
        float cp1x = x1 + dist;
        float cp1y = y1;
        float cp2x = x2 - dist;
        float cp2y = y2;

        int segments = 30;
        float lastX = x1;
        float lastY = y1;

        for (int i = 1; i <= segments; i++) {
            float t = i / (float) segments;
            float invT = 1.0f - t;
            float x = invT * invT * invT * x1 + 3 * invT * invT * t * cp1x + 3 * invT * t * t * cp2x + t * t * t * x2;
            float y = invT * invT * invT * y1 + 3 * invT * invT * t * cp1y + 3 * invT * t * t * cp2y + t * t * t * y2;
            
            drawLine(guiGraphics, lastX, lastY, x, y, color);
            lastX = x;
            lastY = y;
        }
    }

    public static void drawLine(GuiGraphics guiGraphics, float x1, float y1, float x2, float y2, int color) {
        int ix1 = (int) x1;
        int iy1 = (int) y1;
        int ix2 = (int) x2;
        int iy2 = (int) y2;
        
        if (ix1 == ix2 && iy1 == iy2) return;
        
        if (Math.abs(ix1 - ix2) > Math.abs(iy1 - iy2)) {
            if (ix1 > ix2) {
                int tmp = ix1; ix1 = ix2; ix2 = tmp;
                tmp = iy1; iy1 = iy2; iy2 = tmp;
            }
            for (int x = ix1; x <= ix2; x++) {
                float t = (ix2 == ix1) ? 0 : (x - ix1) / (float) (ix2 - ix1);
                int y = (int) (iy1 + t * (iy2 - iy1));
                guiGraphics.fill(x, y, x + 1, y + 2, color);
            }
        } else {
            if (iy1 > iy2) {
                int tmp = ix1; ix1 = ix2; ix2 = tmp;
                tmp = iy1; iy1 = iy2; iy2 = tmp;
            }
            for (int y = iy1; y <= iy2; y++) {
                float t = (iy2 == iy1) ? 0 : (y - iy1) / (float) (iy2 - iy1);
                int x = (int) (ix1 + t * (ix2 - ix1));
                guiGraphics.fill(x, y, x + 2, y + 1, color);
            }
        }
    }
}
