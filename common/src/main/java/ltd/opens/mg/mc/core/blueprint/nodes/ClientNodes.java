package ltd.opens.mg.mc.core.blueprint.nodes;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.NodeHelper;
import ltd.opens.mg.mc.core.blueprint.NodePorts;
import ltd.opens.mg.mc.core.blueprint.NodeThemes;
import ltd.opens.mg.mc.core.blueprint.NodeDefinition.PortType;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

/**
 * 客户端专用节点
 */
public class ClientNodes {

    public static void register() {
        // 客户端 Toast 提示 (Show Toast)
        NodeHelper.setup("client_show_toast", "node.mgmc.client_show_toast.name")
            .category("node_category.mgmc_client.hud")
            .color(NodeThemes.COLOR_NODE_ACTION)
            .input(NodePorts.EXEC, "node.mgmc.port.exec_in", PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .input(NodePorts.TITLE, "node.mgmc.port.title", PortType.STRING, NodeThemes.COLOR_PORT_STRING, "Maingraph")
            .input(NodePorts.MESSAGE, "node.mgmc.port.message", PortType.STRING, NodeThemes.COLOR_PORT_STRING, "Hello from Server!")
            .output(NodePorts.EXEC, "node.mgmc.port.exec_out", PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .registerClientAction("show_toast", (node, ctx) -> {
                JsonObject params = new JsonObject();
                params.addProperty("title", TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.TITLE, ctx)));
                params.addProperty("message", TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.MESSAGE, ctx)));
                return params;
            }, () -> (params, ctx) -> {
                String title = params.has("title") ? params.get("title").getAsString() : "Maingraph";
                String message = params.has("message") ? params.get("message").getAsString() : "";
                
                net.minecraft.client.Minecraft.getInstance().execute(() -> {
                    net.minecraft.client.gui.components.toasts.SystemToast.add(
                        net.minecraft.client.Minecraft.getInstance().getToasts(),
                        net.minecraft.client.gui.components.toasts.SystemToast.SystemToastId.NARRATOR_TOGGLE,
                        net.minecraft.network.chat.Component.literal(title),
                        net.minecraft.network.chat.Component.literal(message)
                    );
                });
            });

        // 客户端打开网页 (Open URL)
        NodeHelper.setup("client_open_url", "node.mgmc.client_open_url.name")
            .category("node_category.mgmc_client.system")
            .color(NodeThemes.COLOR_NODE_ACTION)
            .input(NodePorts.EXEC, "node.mgmc.port.exec_in", PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .input(NodePorts.URL, "node.mgmc.port.url", PortType.STRING, NodeThemes.COLOR_PORT_STRING, "")
            .output(NodePorts.EXEC, "node.mgmc.port.exec_out", PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .registerClientAction("open_url", (node, ctx) -> {
                JsonObject params = new JsonObject();
                params.addProperty("url", TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.URL, ctx)));
                return params;
            }, () -> (params, ctx) -> {
                String url = params.has("url") ? params.get("url").getAsString() : "";
                if (!url.isEmpty()) {
                    net.minecraft.Util.getPlatform().openUri(url);
                }
            });
    }
}
