package ltd.opens.mg.mc.core.blueprint.nodes;

import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeHelper;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;
import ltd.opens.mg.mc.core.blueprint.NodePorts;
import ltd.opens.mg.mc.core.blueprint.NodeThemes;
import ltd.opens.mg.mc.core.blueprint.data.XYZ;

import java.util.Arrays;

/**
 * 类型转换相关节点
 */
public class ConversionNodes {

    public static void register() {
        // 获取所有 PortType 名称作为转换选项，排除 EXEC 和 ANY
        String[] typeOptions = Arrays.stream(NodeDefinition.PortType.values())
            .filter(t -> t != NodeDefinition.PortType.EXEC && t != NodeDefinition.PortType.ANY)
            .map(Enum::name)
            .toArray(String[]::new);

        // 强制转换节点 (Cast)
        NodeHelper.setup("cast", "node.mgmc.cast.name")
            .category("node_category.mgmc.logic.math")
            .color(NodeThemes.COLOR_NODE_CONVERSION)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/math/cast")
            .input(NodePorts.INPUT, "node.mgmc.cast.port.input", NodeDefinition.PortType.ANY, NodeThemes.COLOR_PORT_ANY)
            .input(NodePorts.TO_TYPE, "node.mgmc.cast.port.to_type", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING, true, "STRING", typeOptions)
            .output(NodePorts.OUTPUT, "node.mgmc.cast.port.output", NodeDefinition.PortType.ANY, NodeThemes.COLOR_PORT_ANY)
            .registerValue((node, portId, ctx) -> {
                Object input = NodeLogicRegistry.evaluateInput(node, NodePorts.INPUT, ctx);
                String targetType = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.TO_TYPE, ctx));
                return TypeConverter.cast(input, targetType);
            });

        // to_int (浮点数转整数)
        NodeHelper.setup("to_int", "node.mgmc.to_int.name")
            .category("node_category.mgmc.logic.math")
            .color(NodeThemes.COLOR_NODE_CONVERSION)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/math/to_int")
            .input(NodePorts.INPUT, "node.mgmc.port.value", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 0.0)
            .output(NodePorts.OUTPUT, "node.mgmc.port.output", NodeDefinition.PortType.INT, NodeThemes.COLOR_PORT_INT)
            .registerValue((node, portId, ctx) -> {
                Object input = NodeLogicRegistry.evaluateInput(node, NodePorts.INPUT, ctx);
                return (double) TypeConverter.toInt(input); // 虽然内部是 INT，但为了兼容性返回 double
            });

        // make_xyz (浮点数到XYZ)
        NodeHelper.setup("make_xyz", "node.mgmc.make_xyz.name")
            .category("node_category.mgmc.logic.math")
            .color(NodeThemes.COLOR_NODE_CONVERSION)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/math/make_xyz")
            .input(NodePorts.X, "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 0.0)
            .input(NodePorts.Y, "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 0.0)
            .input(NodePorts.Z, "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 0.0)
            .output(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .registerValue((node, portId, ctx) -> {
                double x = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.X, ctx));
                double y = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.Y, ctx));
                double z = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.Z, ctx));
                return new XYZ(x, y, z);
            });

        // break_xyz (XYZ到浮点数)
        NodeHelper.setup("break_xyz", "node.mgmc.break_xyz.name")
            .category("node_category.mgmc.logic.math")
            .color(NodeThemes.COLOR_NODE_CONVERSION)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/math/break_xyz")
            .input(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.X, "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.Y, "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.Z, "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .registerValue((node, portId, ctx) -> {
                XYZ xyz = TypeConverter.toXYZ(NodeLogicRegistry.evaluateInput(node, NodePorts.XYZ, ctx));
                return switch (portId) {
                    case NodePorts.X -> xyz.x();
                    case NodePorts.Y -> xyz.y();
                    case NodePorts.Z -> xyz.z();
                    default -> 0.0;
                };
            });
    }
}
