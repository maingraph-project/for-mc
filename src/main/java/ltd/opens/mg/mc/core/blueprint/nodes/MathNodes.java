package ltd.opens.mg.mc.core.blueprint.nodes;

import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeHelper;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

import ltd.opens.mg.mc.core.blueprint.NodePorts;
import ltd.opens.mg.mc.core.blueprint.NodeThemes;
import ltd.opens.mg.mc.core.blueprint.events.RegisterMGMCNodesEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.Random;

/**
 * 数学、比较与逻辑运算节点注册
 */
public class MathNodes {
    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onRegister(RegisterMGMCNodesEvent event) {
        // --- 基础数学运算 ---
        NodeHelper.setup("add_float", "node.mgmc.add_float.name")
            .category("node_category.mgmc.logic.math")
            .color(NodeThemes.COLOR_NODE_MATH)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/math/add_float")
            .registerMathOp((a, b) -> a + b);

        NodeHelper.setup("sub_float", "node.mgmc.sub_float.name")
            .category("node_category.mgmc.logic.math")
            .color(NodeThemes.COLOR_NODE_MATH)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/math/sub_float")
            .registerMathOp((a, b) -> a - b);

        NodeHelper.setup("mul_float", "node.mgmc.mul_float.name")
            .category("node_category.mgmc.logic.math")
            .color(NodeThemes.COLOR_NODE_MATH)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/math/mul_float")
            .mathInputs(1.0, 1.0)
            .registerMathOp((a, b) -> a * b);

        NodeHelper.setup("div_float", "node.mgmc.div_float.name")
            .category("node_category.mgmc.logic.math")
            .color(NodeThemes.COLOR_NODE_MATH)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/math/div_float")
            .mathInputs(1.0, 1.0)
            .registerMathOp((a, b) -> b != 0 ? a / b : 0.0);

        NodeHelper.setup("mod_float", "node.mgmc.mod_float.name")
            .category("node_category.mgmc.logic.math")
            .color(NodeThemes.COLOR_NODE_MATH)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/math/mod_float")
            .mathInputs(1.0, 1.0)
            .registerMathOp((a, b) -> b != 0 ? a % b : 0.0);

        NodeHelper.setup("abs_float", "node.mgmc.abs_float.name")
            .category("node_category.mgmc.logic.math")
            .color(NodeThemes.COLOR_NODE_MATH)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/math/abs_float")
            .registerUnaryMathOp(Math::abs);

        NodeHelper.setup("min_float", "node.mgmc.min_float.name")
            .category("node_category.mgmc.logic.math")
            .color(NodeThemes.COLOR_NODE_MATH)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/math/min_float")
            .registerMathOp(Math::min);

        NodeHelper.setup("max_float", "node.mgmc.max_float.name")
            .category("node_category.mgmc.logic.math")
            .color(NodeThemes.COLOR_NODE_MATH)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/math/max_float")
            .registerMathOp(Math::max);

        NodeHelper.setup("clamp_float", "node.mgmc.clamp_float.name")
            .category("node_category.mgmc.logic.math")
            .color(NodeThemes.COLOR_NODE_MATH)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/math/clamp_float")
            .input(NodePorts.VALUE, "node.mgmc.port.value", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 0.0)
            .input(NodePorts.MIN, "node.mgmc.port.min", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 0.0)
            .input(NodePorts.MAX, "node.mgmc.port.max", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 1.0)
            .mathOutput()
            .registerValue((node, portId, ctx) -> {
                double val = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.VALUE, ctx));
                double min = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.MIN, ctx));
                double max = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.MAX, ctx));
                return Math.max(min, Math.min(max, val));
            });

        NodeHelper.setup("round_float", "node.mgmc.round_float.name")
            .category("node_category.mgmc.logic.math")
            .color(NodeThemes.COLOR_NODE_MATH)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/math/round_float")
            .registerUnaryMathOp(v -> (double) Math.round(v));

        NodeHelper.setup("floor_float", "node.mgmc.floor_float.name")
            .category("node_category.mgmc.logic.math")
            .color(NodeThemes.COLOR_NODE_MATH)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/math/floor_float")
            .registerUnaryMathOp(Math::floor);

        NodeHelper.setup("ceil_float", "node.mgmc.ceil_float.name")
            .category("node_category.mgmc.logic.math")
            .color(NodeThemes.COLOR_NODE_MATH)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/math/ceil_float")
            .registerUnaryMathOp(Math::ceil);

        // --- 随机数 ---
        NodeHelper.setup("random_float", "node.mgmc.random_float.name")
            .category("node_category.mgmc.logic.math")
            .color(NodeThemes.COLOR_NODE_MATH)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/math/random_float")
            .input(NodePorts.MIN, "node.mgmc.port.min", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 0.0)
            .input(NodePorts.MAX, "node.mgmc.port.max", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 1.0)
            .mathOutput()
            .registerValue((node, portId, ctx) -> {
                double min = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.MIN, ctx));
                double max = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.MAX, ctx));
                return min + (max - min) * RANDOM.nextDouble();
            });

        NodeHelper.setup("random_int", "node.mgmc.random_int.name")
            .category("node_category.mgmc.logic.math")
            .color(NodeThemes.COLOR_NODE_MATH)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/math/random_int")
            .input(NodePorts.MIN, "node.mgmc.port.min", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 0.0)
            .input(NodePorts.MAX, "node.mgmc.port.max", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 100.0)
            .mathOutput()
            .registerValue((node, portId, ctx) -> {
                int min = (int) TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.MIN, ctx));
                int max = (int) TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.MAX, ctx));
                if (max <= min) return (double) min;
                return (double) (min + RANDOM.nextInt(max - min + 1));
            });

        NodeHelper.setup("random_bool", "node.mgmc.random_bool.name")
            .category("node_category.mgmc.logic.math")
            .color(NodeThemes.COLOR_NODE_MATH)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/math/random_bool")
            .input(NodePorts.CHANCE, "node.mgmc.random_bool.port.chance", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 0.5)
            .output(NodePorts.RESULT, "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, NodeThemes.COLOR_PORT_BOOLEAN)
            .registerValue((node, portId, ctx) -> {
                double chance = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.CHANCE, ctx));
                return RANDOM.nextDouble() < chance;
            });
    }
}
