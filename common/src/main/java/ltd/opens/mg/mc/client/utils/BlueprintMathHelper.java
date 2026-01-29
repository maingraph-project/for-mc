package ltd.opens.mg.mc.client.utils;

import ltd.opens.mg.mc.client.gui.blueprint.BlueprintState;
import ltd.opens.mg.mc.client.gui.components.GuiConnection;

public class BlueprintMathHelper {

    public static GuiConnection getHoveredConnection(double worldMouseX, double worldMouseY, BlueprintState state) {
        float threshold = 3.0f; // Distance threshold in world coordinates

        for (GuiConnection conn : state.connections) {
            float[] outPos = conn.from.getPortPositionByName(conn.fromPort, false);
            float[] inPos = conn.to.getPortPositionByName(conn.toPort, true);

            if (isPointNearBezier(worldMouseX, worldMouseY, outPos[0], outPos[1], inPos[0], inPos[1], threshold)) {
                return conn;
            }
        }
        return null;
    }

    private static boolean isPointNearBezier(double px, double py, float x1, float y1, float x2, float y2, float threshold) {
        float dist = Math.abs(x2 - x1) * 0.5f;
        if (dist < 20) dist = 20;

        float cp1x = x1 + dist;
        float cp1y = y1;
        float cp2x = x2 - dist;
        float cp2y = y2;

        int segments = 20;
        float lastX = x1;
        float lastY = y1;

        for (int i = 1; i <= segments; i++) {
            float t = i / (float) segments;
            float invT = 1.0f - t;

            float b0 = invT * invT * invT;
            float b1 = 3 * invT * invT * t;
            float b2 = 3 * invT * t * t;
            float b3 = t * t * t;

            float x = b0 * x1 + b1 * cp1x + b2 * cp2x + b3 * x2;
            float y = b0 * y1 + b1 * cp1y + b2 * cp2y + b3 * y2;

            if (distToSegment(px, py, lastX, lastY, x, y) < threshold) {
                return true;
            }
            lastX = x;
            lastY = y;
        }
        return false;
    }

    private static double distToSegment(double px, double py, float x1, float y1, float x2, float y2) {
        double dx = x2 - x1;
        double dy = y2 - y1;
        double l2 = dx * dx + dy * dy;
        if (l2 == 0) return Math.sqrt(Math.pow(px - x1, 2) + Math.pow(py - y1, 2));

        double t = ((px - x1) * dx + (py - y1) * dy) / l2;
        t = Math.max(0, Math.min(1, t));
        return Math.sqrt(Math.pow(px - (x1 + t * dx), 2) + Math.pow(py - (y1 + t * dy), 2));
    }
}
