package ltd.opens.mg.mc.client.gui.components;

import java.util.UUID;

public class GuiRegion {
    public String id;
    public String title;
    public float x, y;
    public float width, height;
    public int color;
    
    // UI State
    public boolean isSelected = false;
    public boolean isResizing = false; // Bottom-right corner
    
    public GuiRegion(float x, float y, float width, float height) {
        this.id = UUID.randomUUID().toString();
        this.title = "Region";
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = 0x44888888; // Default ARGB
    }
}
