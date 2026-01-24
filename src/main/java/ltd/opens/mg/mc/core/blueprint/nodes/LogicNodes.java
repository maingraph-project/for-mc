package ltd.opens.mg.mc.core.blueprint.nodes;

import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeHelper;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;
import ltd.opens.mg.mc.core.blueprint.NodePorts;
import ltd.opens.mg.mc.core.blueprint.NodeThemes;
import ltd.opens.mg.mc.core.blueprint.events.RegisterMGMCNodesEvent;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * 比较与布尔逻辑运算节点注册
 */
public class LogicNodes {

    @SubscribeEvent
    public static void onRegister(RegisterMGMCNodesEvent event) {
        // --- 比较运算 ---
        NodeHelper.setup("compare_eq", "node.mgmc.compare_eq.name")
            .category("node_category.mgmc.logic.comparison")
            .color(NodeThemes.COLOR_NODE_LOGIC)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/comparison/compare_eq")
            .input(NodePorts.A, "node.mgmc.port.a", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 0.0)
            .input(NodePorts.B, "node.mgmc.port.b", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 0.0)
            .output(NodePorts.RESULT, "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, NodeThemes.COLOR_PORT_BOOLEAN)
            .registerValue((node, portId, ctx) -> {
                double a = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.A, ctx));
                double b = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.B, ctx));
                return a == b;
            });

        NodeHelper.setup("compare_neq", "node.mgmc.compare_neq.name")
            .category("node_category.mgmc.logic.comparison")
            .color(NodeThemes.COLOR_NODE_LOGIC)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/comparison/compare_neq")
            .input(NodePorts.A, "node.mgmc.port.a", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 0.0)
            .input(NodePorts.B, "node.mgmc.port.b", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 0.0)
            .output(NodePorts.RESULT, "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, NodeThemes.COLOR_PORT_BOOLEAN)
            .registerValue((node, portId, ctx) -> {
                double a = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.A, ctx));
                double b = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.B, ctx));
                return a != b;
            });

        NodeHelper.setup("compare_gt", "node.mgmc.compare_gt.name")
            .category("node_category.mgmc.logic.comparison")
            .color(NodeThemes.COLOR_NODE_LOGIC)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/comparison/compare_gt")
            .input(NodePorts.A, "node.mgmc.port.a", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 0.0)
            .input(NodePorts.B, "node.mgmc.port.b", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 0.0)
            .output(NodePorts.RESULT, "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, NodeThemes.COLOR_PORT_BOOLEAN)
            .registerValue((node, portId, ctx) -> {
                double a = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.A, ctx));
                double b = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.B, ctx));
                return a > b;
            });

        NodeHelper.setup("compare_gte", "node.mgmc.compare_gte.name")
            .category("node_category.mgmc.logic.comparison")
            .color(NodeThemes.COLOR_NODE_LOGIC)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/comparison/compare_gte")
            .input(NodePorts.A, "node.mgmc.port.a", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 0.0)
            .input(NodePorts.B, "node.mgmc.port.b", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 0.0)
            .output(NodePorts.RESULT, "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, NodeThemes.COLOR_PORT_BOOLEAN)
            .registerValue((node, portId, ctx) -> {
                double a = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.A, ctx));
                double b = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.B, ctx));
                return a >= b;
            });

        NodeHelper.setup("compare_lt", "node.mgmc.compare_lt.name")
            .category("node_category.mgmc.logic.comparison")
            .color(NodeThemes.COLOR_NODE_LOGIC)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/comparison/compare_lt")
            .input(NodePorts.A, "node.mgmc.port.a", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 0.0)
            .input(NodePorts.B, "node.mgmc.port.b", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 0.0)
            .output(NodePorts.RESULT, "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, NodeThemes.COLOR_PORT_BOOLEAN)
            .registerValue((node, portId, ctx) -> {
                double a = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.A, ctx));
                double b = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.B, ctx));
                return a < b;
            });

        NodeHelper.setup("compare_lte", "node.mgmc.compare_lte.name")
            .category("node_category.mgmc.logic.comparison")
            .color(NodeThemes.COLOR_NODE_LOGIC)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/comparison/compare_lte")
            .input(NodePorts.A, "node.mgmc.port.a", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 0.0)
            .input(NodePorts.B, "node.mgmc.port.b", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 0.0)
            .output(NodePorts.RESULT, "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, NodeThemes.COLOR_PORT_BOOLEAN)
            .registerValue((node, portId, ctx) -> {
                double a = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.A, ctx));
                double b = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.B, ctx));
                return a <= b;
            });

        // --- 布尔逻辑 ---
        NodeHelper.setup("logic_and", "node.mgmc.logic_and.name")
            .category("node_category.mgmc.logic.boolean")
            .color(NodeThemes.COLOR_NODE_LOGIC)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/boolean/logic_and")
            .input(NodePorts.A, "node.mgmc.port.a", NodeDefinition.PortType.BOOLEAN, NodeThemes.COLOR_PORT_BOOLEAN, true)
            .input(NodePorts.B, "node.mgmc.port.b", NodeDefinition.PortType.BOOLEAN, NodeThemes.COLOR_PORT_BOOLEAN, true)
            .output(NodePorts.RESULT, "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, NodeThemes.COLOR_PORT_BOOLEAN)
            .registerValue((node, portId, ctx) -> {
                boolean a = TypeConverter.toBoolean(NodeLogicRegistry.evaluateInput(node, NodePorts.A, ctx));
                boolean b = TypeConverter.toBoolean(NodeLogicRegistry.evaluateInput(node, NodePorts.B, ctx));
                return a && b;
            });

        NodeHelper.setup("logic_or", "node.mgmc.logic_or.name")
            .category("node_category.mgmc.logic.boolean")
            .color(NodeThemes.COLOR_NODE_LOGIC)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/boolean/logic_or")
            .input(NodePorts.A, "node.mgmc.port.a", NodeDefinition.PortType.BOOLEAN, NodeThemes.COLOR_PORT_BOOLEAN, true)
            .input(NodePorts.B, "node.mgmc.port.b", NodeDefinition.PortType.BOOLEAN, NodeThemes.COLOR_PORT_BOOLEAN, true)
            .output(NodePorts.RESULT, "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, NodeThemes.COLOR_PORT_BOOLEAN)
            .registerValue((node, portId, ctx) -> {
                boolean a = TypeConverter.toBoolean(NodeLogicRegistry.evaluateInput(node, NodePorts.A, ctx));
                boolean b = TypeConverter.toBoolean(NodeLogicRegistry.evaluateInput(node, NodePorts.B, ctx));
                return a || b;
            });

        NodeHelper.setup("logic_not", "node.mgmc.logic_not.name")
            .category("node_category.mgmc.logic.boolean")
            .color(NodeThemes.COLOR_NODE_LOGIC)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/boolean/logic_not")
            .input(NodePorts.A, "node.mgmc.port.a", NodeDefinition.PortType.BOOLEAN, NodeThemes.COLOR_PORT_BOOLEAN, false)
            .output(NodePorts.RESULT, "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, NodeThemes.COLOR_PORT_BOOLEAN)
            .registerValue((node, portId, ctx) -> !TypeConverter.toBoolean(NodeLogicRegistry.evaluateInput(node, NodePorts.A, ctx)));

        NodeHelper.setup("logic_xor", "node.mgmc.logic_xor.name")
            .category("node_category.mgmc.logic.boolean")
            .color(NodeThemes.COLOR_NODE_LOGIC)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/boolean/logic_xor")
            .input(NodePorts.A, "node.mgmc.port.a", NodeDefinition.PortType.BOOLEAN, NodeThemes.COLOR_PORT_BOOLEAN, false)
            .input(NodePorts.B, "node.mgmc.port.b", NodeDefinition.PortType.BOOLEAN, NodeThemes.COLOR_PORT_BOOLEAN, false)
            .output(NodePorts.RESULT, "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, NodeThemes.COLOR_PORT_BOOLEAN)
            .registerValue((node, portId, ctx) -> {
                boolean a = TypeConverter.toBoolean(NodeLogicRegistry.evaluateInput(node, NodePorts.A, ctx));
                boolean b = TypeConverter.toBoolean(NodeLogicRegistry.evaluateInput(node, NodePorts.B, ctx));
                return a ^ b;
            });
    }
}
