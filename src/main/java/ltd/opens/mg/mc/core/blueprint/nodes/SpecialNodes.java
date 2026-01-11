package ltd.opens.mg.mc.core.blueprint.nodes;

import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeHelper;
import ltd.opens.mg.mc.core.blueprint.NodePorts;
import ltd.opens.mg.mc.core.blueprint.NodeThemes;
import ltd.opens.mg.mc.core.blueprint.events.RegisterMGMCNodesEvent;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * 特殊节点注册（如标记、注释等）
 */
public class SpecialNodes {

    @SubscribeEvent
    public static void onRegister(RegisterMGMCNodesEvent event) {
        // --- 标记节点 (Marker Node) ---
        // 标记节点没有逻辑执行，仅用于在编辑器中做标注和快速跳转
        NodeHelper.setup("marker", "node.mgmc.marker.name")
            .category("node_category.mgmc.special")
            .color(NodeThemes.COLOR_NODE_COMMENT)
            .input(NodePorts.COMMENT, "node.mgmc.port.comment", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING, "")
            .property("is_marker", true)
            .property("ui_multiline", true)
            .registerMetadataOnly();
    }
}
