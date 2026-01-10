package ltd.opens.mg.mc.core.blueprint.nodes;

import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeHelper;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;
import ltd.opens.mg.mc.core.blueprint.NodePorts;
import ltd.opens.mg.mc.core.blueprint.NodeThemes;
import ltd.opens.mg.mc.core.blueprint.events.RegisterMGMCNodesEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.Arrays;

/**
 * 类型转换相关节点
 */
public class ConversionNodes {

    @SubscribeEvent
    public static void onRegister(RegisterMGMCNodesEvent event) {
        // 获取所有 PortType 名称作为转换选项，排除 EXEC 和 ANY
        String[] typeOptions = Arrays.stream(NodeDefinition.PortType.values())
            .filter(t -> t != NodeDefinition.PortType.EXEC && t != NodeDefinition.PortType.ANY)
            .map(Enum::name)
            .toArray(String[]::new);

        // 强制转换节点 (Cast)
        NodeHelper.setup("cast", "node.mgmc.cast.name")
            .category("node_category.mgmc.logic.math")
            .color(NodeThemes.COLOR_NODE_CONVERSION)
            .input(NodePorts.INPUT, "node.mgmc.cast.port.input", NodeDefinition.PortType.ANY, NodeThemes.COLOR_PORT_ANY)
            .input(NodePorts.TO_TYPE, "node.mgmc.cast.port.to_type", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING, true, "STRING", typeOptions)
            .output(NodePorts.OUTPUT, "node.mgmc.cast.port.output", NodeDefinition.PortType.ANY, NodeThemes.COLOR_PORT_ANY)
            .registerValue((node, portId, ctx) -> {
                Object input = NodeLogicRegistry.evaluateInput(node, NodePorts.INPUT, ctx);
                String targetType = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.TO_TYPE, ctx));
                return TypeConverter.cast(input, targetType);
            });
    }
}
