package ltd.opens.mg.mc.client.gui.blueprint;

/**
 * Handles coordinate transformations between screen space and blueprint world space.
 */
public class Viewport {
    public float panX = 0;
    public float panY = 0;
    public float zoom = 1.0f;

    public float toScreenX(float worldX) {
        return worldX * zoom + panX;
    }

    public float toScreenY(float worldY) {
        return worldY * zoom + panY;
    }

    public float toWorldX(double screenX) {
        return (float) ((screenX - panX) / zoom);
    }

    public float toWorldY(double screenY) {
        return (float) ((screenY - panY) / zoom);
    }

    public void pan(double dx, double dy) {
        panX += (float) dx;
        panY += (float) dy;
    }

    public void zoom(float factor, double mouseX, double mouseY) {
        float oldZoom = zoom;
        zoom *= factor;
        zoom = Math.max(0.1f, Math.min(zoom, 2.5f));

        float actualFactor = zoom / oldZoom;
        panX = (float) (mouseX - (mouseX - panX) * actualFactor);
        panY = (float) (mouseY - (mouseY - panY) * actualFactor);
    }

    public void set(float panX, float panY, float zoom) {
        this.panX = panX;
        this.panY = panY;
        this.zoom = zoom;
    }

    public boolean isVisible(float worldX, float worldY, float width, float height, int screenWidth, int screenHeight) {
        float sX = toScreenX(worldX);
        float sY = toScreenY(worldY);
        float sW = width * zoom;
        float sH = height * zoom;
        return sX + sW >= 0 && sX <= screenWidth && sY + sH >= 0 && sY <= screenHeight;
    }

    public float getWorldWidth(int screenWidth) {
        return screenWidth / zoom;
    }

    public float getWorldHeight(int screenHeight) {
        return screenHeight / zoom;
    }
}
