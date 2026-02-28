package ltd.opens.mg.mc.core.blueprint.nodes;

import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeHelper;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;
import ltd.opens.mg.mc.core.blueprint.NodePorts;
import ltd.opens.mg.mc.core.blueprint.NodeThemes;

import java.util.Objects;

/**
 * 比较与布尔逻辑运算节点注册
 */
public class LogicNodes {

    public static void register() {
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

        NodeHelper.setup("compare_any", "node.mgmc.compare_any.name")
            .category("node_category.mgmc.logic.comparison")
            .color(NodeThemes.COLOR_NODE_LOGIC)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/comparison/compare_any")
            .input(NodePorts.A, "node.mgmc.port.a", NodeDefinition.PortType.ANY, NodeThemes.COLOR_PORT_ANY)
            .input(NodePorts.B, "node.mgmc.port.b", NodeDefinition.PortType.ANY, NodeThemes.COLOR_PORT_ANY)
            .output(NodePorts.IGNORE_TYPE, "node.mgmc.compare_any.port.ignore_type", NodeDefinition.PortType.BOOLEAN, NodeThemes.COLOR_PORT_BOOLEAN)
            .output(NodePorts.STRICT_TYPE, "node.mgmc.compare_any.port.strict_type", NodeDefinition.PortType.BOOLEAN, NodeThemes.COLOR_PORT_BOOLEAN)
            .output(NodePorts.LOOSE_TYPE, "node.mgmc.compare_any.port.loose_type", NodeDefinition.PortType.BOOLEAN, NodeThemes.COLOR_PORT_BOOLEAN)
            .registerValue((node, portId, ctx) -> {
                Object a = NodeLogicRegistry.evaluateInput(node, NodePorts.A, ctx);
                Object b = NodeLogicRegistry.evaluateInput(node, NodePorts.B, ctx);
                if (NodePorts.STRICT_TYPE.equals(portId)) {
                    return strictEquals(a, b);
                }
                if (NodePorts.LOOSE_TYPE.equals(portId)) {
                    return looseEquals(a, b);
                }
                return ignoreEquals(a, b);
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

    private static boolean ignoreEquals(Object a, Object b) {
        if (a instanceof Number && b instanceof Number) {
            return Double.compare(((Number) a).doubleValue(), ((Number) b).doubleValue()) == 0;
        }
        return Objects.equals(a, b);
    }

    private static boolean looseEquals(Object a, Object b) {
        if (a instanceof Number && b instanceof Number) {
            return Double.compare(((Number) a).doubleValue(), ((Number) b).doubleValue()) == 0;
        }
        if ((a instanceof Number && b instanceof String) || (a instanceof String && b instanceof Number)) {
            return Double.compare(TypeConverter.toDouble(a), TypeConverter.toDouble(b)) == 0;
        }
        if ((a instanceof Boolean && b instanceof String) || (a instanceof String && b instanceof Boolean)) {
            return TypeConverter.toBoolean(a) == TypeConverter.toBoolean(b);
        }
        return Objects.equals(TypeConverter.toString(a), TypeConverter.toString(b));
    }

    private static boolean strictEquals(Object a, Object b) {
        if (a instanceof Number && b instanceof Number) {
            if (!a.getClass().equals(b.getClass())) return false;
        }
        return Objects.equals(a, b);
    }
}
