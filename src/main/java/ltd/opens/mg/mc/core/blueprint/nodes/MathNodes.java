package ltd.opens.mg.mc.core.blueprint.nodes;

import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeHelper;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

import java.util.Random;

/**
 * 数学、比较与逻辑运算节点注册
 */
public class MathNodes {
    private static final int COLOR_MATH = 0xFF888888;
    private static final int COLOR_FLOAT = 0xFF00FF00;
    private static final int COLOR_BOOLEAN = 0xFF920101;
    
    private static final Random RANDOM = new Random();

    public static void register() {
        // --- 基础数学运算 ---
        NodeHelper.setup("add_float", "node.mgmc.add_float.name")
            .category("node_category.mgmc.logic.math")
            .color(COLOR_MATH)
            .input("a", "node.mgmc.port.a", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .input("b", "node.mgmc.port.b", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .output("result", "node.mgmc.port.output", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .register((node, portId, ctx) -> {
                double a = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "a", ctx));
                double b = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "b", ctx));
                return a + b;
            });

        NodeHelper.setup("sub_float", "node.mgmc.sub_float.name")
            .category("node_category.mgmc.logic.math")
            .color(COLOR_MATH)
            .input("a", "node.mgmc.port.a", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .input("b", "node.mgmc.port.b", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .output("result", "node.mgmc.port.output", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .register((node, portId, ctx) -> {
                double a = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "a", ctx));
                double b = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "b", ctx));
                return a - b;
            });

        NodeHelper.setup("mul_float", "node.mgmc.mul_float.name")
            .category("node_category.mgmc.logic.math")
            .color(COLOR_MATH)
            .input("a", "node.mgmc.port.a", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 1.0)
            .input("b", "node.mgmc.port.b", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 1.0)
            .output("result", "node.mgmc.port.output", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .register((node, portId, ctx) -> {
                double a = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "a", ctx));
                double b = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "b", ctx));
                return a * b;
            });

        NodeHelper.setup("div_float", "node.mgmc.div_float.name")
            .category("node_category.mgmc.logic.math")
            .color(COLOR_MATH)
            .input("a", "node.mgmc.port.a", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 1.0)
            .input("b", "node.mgmc.port.b", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 1.0)
            .output("result", "node.mgmc.port.output", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .register((node, portId, ctx) -> {
                double a = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "a", ctx));
                double b = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "b", ctx));
                return b != 0 ? a / b : 0.0;
            });

        NodeHelper.setup("mod_float", "node.mgmc.mod_float.name")
            .category("node_category.mgmc.logic.math")
            .color(COLOR_MATH)
            .input("a", "node.mgmc.port.a", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 1.0)
            .input("b", "node.mgmc.port.b", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 1.0)
            .output("result", "node.mgmc.port.output", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .register((node, portId, ctx) -> {
                double a = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "a", ctx));
                double b = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "b", ctx));
                return b != 0 ? a % b : 0.0;
            });

        NodeHelper.setup("abs_float", "node.mgmc.abs_float.name")
            .category("node_category.mgmc.logic.math")
            .color(COLOR_MATH)
            .input("input", "node.mgmc.port.input", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .output("result", "node.mgmc.port.output", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .register((node, portId, ctx) -> Math.abs(TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "input", ctx))));

        NodeHelper.setup("min_float", "node.mgmc.min_float.name")
            .category("node_category.mgmc.logic.math")
            .color(COLOR_MATH)
            .input("a", "node.mgmc.port.a", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .input("b", "node.mgmc.port.b", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .output("result", "node.mgmc.port.output", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .register((node, portId, ctx) -> {
                double a = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "a", ctx));
                double b = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "b", ctx));
                return Math.min(a, b);
            });

        NodeHelper.setup("max_float", "node.mgmc.max_float.name")
            .category("node_category.mgmc.logic.math")
            .color(COLOR_MATH)
            .input("a", "node.mgmc.port.a", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .input("b", "node.mgmc.port.b", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .output("result", "node.mgmc.port.output", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .register((node, portId, ctx) -> {
                double a = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "a", ctx));
                double b = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "b", ctx));
                return Math.max(a, b);
            });

        NodeHelper.setup("clamp_float", "node.mgmc.clamp_float.name")
            .category("node_category.mgmc.logic.math")
            .color(COLOR_MATH)
            .input("value", "node.mgmc.port.value", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .input("min", "node.mgmc.port.min", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .input("max", "node.mgmc.port.max", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 1.0)
            .output("result", "node.mgmc.port.output", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .register((node, portId, ctx) -> {
                double val = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "value", ctx));
                double min = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "min", ctx));
                double max = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "max", ctx));
                return Math.max(min, Math.min(max, val));
            });

        NodeHelper.setup("round_float", "node.mgmc.round_float.name")
            .category("node_category.mgmc.logic.math")
            .color(COLOR_MATH)
            .input("input", "node.mgmc.port.input", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .output("result", "node.mgmc.port.output", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .register((node, portId, ctx) -> (double) Math.round(TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "input", ctx))));

        NodeHelper.setup("floor_float", "node.mgmc.floor_float.name")
            .category("node_category.mgmc.logic.math")
            .color(COLOR_MATH)
            .input("input", "node.mgmc.port.input", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .output("result", "node.mgmc.port.output", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .register((node, portId, ctx) -> Math.floor(TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "input", ctx))));

        NodeHelper.setup("ceil_float", "node.mgmc.ceil_float.name")
            .category("node_category.mgmc.logic.math")
            .color(COLOR_MATH)
            .input("input", "node.mgmc.port.input", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .output("result", "node.mgmc.port.output", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .register((node, portId, ctx) -> Math.ceil(TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "input", ctx))));

        // --- 随机数 ---
        NodeHelper.setup("random_float", "node.mgmc.random_float.name")
            .category("node_category.mgmc.logic.math")
            .color(COLOR_MATH)
            .input("min", "node.mgmc.port.min", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .input("max", "node.mgmc.port.max", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 1.0)
            .output("result", "node.mgmc.port.output", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .register((node, portId, ctx) -> {
                double min = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "min", ctx));
                double max = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "max", ctx));
                return min + (max - min) * RANDOM.nextDouble();
            });

        NodeHelper.setup("random_int", "node.mgmc.random_int.name")
            .category("node_category.mgmc.logic.math")
            .color(COLOR_MATH)
            .input("min", "node.mgmc.port.min", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .input("max", "node.mgmc.port.max", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 100.0)
            .output("result", "node.mgmc.port.output", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .register((node, portId, ctx) -> {
                int min = (int) TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "min", ctx));
                int max = (int) TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "max", ctx));
                if (max <= min) return (double) min;
                return (double) (min + RANDOM.nextInt(max - min + 1));
            });

        NodeHelper.setup("random_bool", "node.mgmc.random_bool.name")
            .category("node_category.mgmc.logic.math")
            .color(COLOR_MATH)
            .input("chance", "node.mgmc.random_bool.port.chance", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.5)
            .output("result", "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN)
            .register((node, portId, ctx) -> {
                double chance = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "chance", ctx));
                return RANDOM.nextDouble() < chance;
            });

        // --- 比较运算 ---
        NodeHelper.setup("compare_eq", "node.mgmc.compare_eq.name")
            .category("node_category.mgmc.logic.comparison")
            .color(COLOR_MATH)
            .input("a", "node.mgmc.port.a", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .input("b", "node.mgmc.port.b", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .output("result", "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN)
            .register((node, portId, ctx) -> {
                double a = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "a", ctx));
                double b = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "b", ctx));
                return a == b;
            });

        NodeHelper.setup("compare_neq", "node.mgmc.compare_neq.name")
            .category("node_category.mgmc.logic.comparison")
            .color(COLOR_MATH)
            .input("a", "node.mgmc.port.a", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .input("b", "node.mgmc.port.b", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .output("result", "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN)
            .register((node, portId, ctx) -> {
                double a = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "a", ctx));
                double b = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "b", ctx));
                return a != b;
            });

        NodeHelper.setup("compare_gt", "node.mgmc.compare_gt.name")
            .category("node_category.mgmc.logic.comparison")
            .color(COLOR_MATH)
            .input("a", "node.mgmc.port.a", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .input("b", "node.mgmc.port.b", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .output("result", "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN)
            .register((node, portId, ctx) -> {
                double a = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "a", ctx));
                double b = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "b", ctx));
                return a > b;
            });

        NodeHelper.setup("compare_gte", "node.mgmc.compare_gte.name")
            .category("node_category.mgmc.logic.comparison")
            .color(COLOR_MATH)
            .input("a", "node.mgmc.port.a", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .input("b", "node.mgmc.port.b", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .output("result", "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN)
            .register((node, portId, ctx) -> {
                double a = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "a", ctx));
                double b = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "b", ctx));
                return a >= b;
            });

        NodeHelper.setup("compare_lt", "node.mgmc.compare_lt.name")
            .category("node_category.mgmc.logic.comparison")
            .color(COLOR_MATH)
            .input("a", "node.mgmc.port.a", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .input("b", "node.mgmc.port.b", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .output("result", "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN)
            .register((node, portId, ctx) -> {
                double a = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "a", ctx));
                double b = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "b", ctx));
                return a < b;
            });

        NodeHelper.setup("compare_lte", "node.mgmc.compare_lte.name")
            .category("node_category.mgmc.logic.comparison")
            .color(COLOR_MATH)
            .input("a", "node.mgmc.port.a", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .input("b", "node.mgmc.port.b", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .output("result", "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN)
            .register((node, portId, ctx) -> {
                double a = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "a", ctx));
                double b = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "b", ctx));
                return a <= b;
            });

        // --- 布尔逻辑 ---
        NodeHelper.setup("logic_and", "node.mgmc.logic_and.name")
            .category("node_category.mgmc.logic.boolean")
            .color(COLOR_MATH)
            .input("a", "node.mgmc.port.a", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN, true)
            .input("b", "node.mgmc.port.b", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN, true)
            .output("result", "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN)
            .register((node, portId, ctx) -> {
                boolean a = TypeConverter.toBoolean(NodeLogicRegistry.evaluateInput(node, "a", ctx));
                boolean b = TypeConverter.toBoolean(NodeLogicRegistry.evaluateInput(node, "b", ctx));
                return a && b;
            });

        NodeHelper.setup("logic_or", "node.mgmc.logic_or.name")
            .category("node_category.mgmc.logic.boolean")
            .color(COLOR_MATH)
            .input("a", "node.mgmc.port.a", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN, false)
            .input("b", "node.mgmc.port.b", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN, false)
            .output("result", "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN)
            .register((node, portId, ctx) -> {
                boolean a = TypeConverter.toBoolean(NodeLogicRegistry.evaluateInput(node, "a", ctx));
                boolean b = TypeConverter.toBoolean(NodeLogicRegistry.evaluateInput(node, "b", ctx));
                return a || b;
            });

        NodeHelper.setup("logic_not", "node.mgmc.logic_not.name")
            .category("node_category.mgmc.logic.boolean")
            .color(COLOR_MATH)
            .input("a", "node.mgmc.port.a", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN, false)
            .output("result", "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN)
            .register((node, portId, ctx) -> !TypeConverter.toBoolean(NodeLogicRegistry.evaluateInput(node, "a", ctx)));

        NodeHelper.setup("logic_xor", "node.mgmc.logic_xor.name")
            .category("node_category.mgmc.logic.boolean")
            .color(COLOR_MATH)
            .input("a", "node.mgmc.port.a", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN, false)
            .input("b", "node.mgmc.port.b", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN, false)
            .output("result", "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN)
            .register((node, portId, ctx) -> {
                boolean a = TypeConverter.toBoolean(NodeLogicRegistry.evaluateInput(node, "a", ctx));
                boolean b = TypeConverter.toBoolean(NodeLogicRegistry.evaluateInput(node, "b", ctx));
                return a ^ b;
            });
    }
}
