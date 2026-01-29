package ltd.opens.mg.mc.client.gui.components;

public class GuiConnection {
    public GuiNode from;
    public String fromPort;
    public GuiNode to;
    public String toPort;

    public GuiConnection(GuiNode from, String fromPort, GuiNode to, String toPort) {
        this.from = from;
        this.fromPort = fromPort;
        this.to = to;
        this.toPort = toPort;
    }
}
