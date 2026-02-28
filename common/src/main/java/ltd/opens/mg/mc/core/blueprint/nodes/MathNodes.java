package ltd.opens.mg.mc.core.blueprint.nodes;

import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeHelper;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

import ltd.opens.mg.mc.core.blueprint.NodePorts;
import ltd.opens.mg.mc.core.blueprint.NodeThemes;
import ltd.opens.mg.mc.core.blueprint.data.XYZ;
import net.minecraft.util.Mth;

import java.util.List;
import java.util.Random;

/**
 * 数学、比较与逻辑运算节点注册
 */
public class MathNodes {
    private static final Random RANDOM = new Random();

    public static void register() {
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

        NodeHelper.setup("pow_float", "node.mgmc.pow_float.name")
            .category("node_category.mgmc.logic.math")
            .color(NodeThemes.COLOR_NODE_MATH)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/math/pow_float")
            .mathInputs(1.0, 2.0)
            .registerMathOp(Math::pow);

        NodeHelper.setup("eval_expr", "node.mgmc.eval_expr.name")
            .category("node_category.mgmc.logic.math")
            .color(NodeThemes.COLOR_NODE_MATH)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/logic/math/eval_expr")
            .input(NodePorts.STRING, "node.mgmc.eval_expr.port.expression", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING, "")
            .output(NodePorts.RESULT, "node.mgmc.port.output", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .registerValue((node, portId, ctx) -> {
                String expr = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.STRING, ctx), ctx);
                try {
                    return evalExpression(expr);
                } catch (IllegalArgumentException e) {
                    ltd.opens.mg.mc.MaingraphforMC.LOGGER.warn("Invalid expression '{}': {}", expr, e.getMessage());
                    return 0.0;
                }
            });

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
            .input(NodePorts.MIN, "node.mgmc.port.min", NodeDefinition.PortType.INT, NodeThemes.COLOR_PORT_INT, 0)
            .input(NodePorts.MAX, "node.mgmc.port.max", NodeDefinition.PortType.INT, NodeThemes.COLOR_PORT_INT, 100)
            .output(NodePorts.OUTPUT, "node.mgmc.port.output", NodeDefinition.PortType.INT, NodeThemes.COLOR_PORT_INT)
            .registerValue((node, portId, ctx) -> {
                int min = TypeConverter.toInt(NodeLogicRegistry.evaluateInput(node, NodePorts.MIN, ctx));
                int max = TypeConverter.toInt(NodeLogicRegistry.evaluateInput(node, NodePorts.MAX, ctx));
                if (max <= min) return min;
                return min + RANDOM.nextInt(max - min + 1);
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

    private static double evalExpression(String expr) {
        if (expr == null || expr.trim().isEmpty()) return 0.0;
        List<String> output = new java.util.ArrayList<>();
        java.util.ArrayDeque<String> ops = new java.util.ArrayDeque<>();

        int i = 0;
        boolean expectOperand = true;
        while (i < expr.length()) {
            char c = expr.charAt(i);
            if (Character.isWhitespace(c)) {
                i++;
                continue;
            }
            if (c == '(') {
                ops.push("(");
                i++;
                expectOperand = true;
                continue;
            }
            if (c == ')') {
                while (!ops.isEmpty() && !"(".equals(ops.peek())) {
                    output.add(ops.pop());
                }
                if (ops.isEmpty()) throw new IllegalArgumentException("Mismatched parentheses");
                ops.pop();
                i++;
                expectOperand = false;
                continue;
            }

            if (isOperatorChar(c)) {
                String op = String.valueOf(c);
                if ((c == '+' || c == '-') && expectOperand) {
                    if (c == '-') {
                        output.add("0");
                        op = "-";
                    } else {
                        i++;
                        continue;
                    }
                }
                while (!ops.isEmpty() && isOperator(ops.peek())) {
                    String top = ops.peek();
                    if ((isRightAssociative(op) && precedence(op) < precedence(top))
                        || (!isRightAssociative(op) && precedence(op) <= precedence(top))) {
                        output.add(ops.pop());
                    } else {
                        break;
                    }
                }
                ops.push(op);
                i++;
                expectOperand = true;
                continue;
            }

            if (Character.isDigit(c) || c == '.') {
                int start = i;
                i++;
                while (i < expr.length()) {
                    char ch = expr.charAt(i);
                    if (Character.isDigit(ch) || ch == '.') {
                        i++;
                        continue;
                    }
                    break;
                }
                output.add(expr.substring(start, i));
                expectOperand = false;
                continue;
            }

            throw new IllegalArgumentException("Unexpected character: " + c);
        }

        while (!ops.isEmpty()) {
            String op = ops.pop();
            if ("(".equals(op) || ")".equals(op)) throw new IllegalArgumentException("Mismatched parentheses");
            output.add(op);
        }

        java.util.ArrayDeque<Double> stack = new java.util.ArrayDeque<>();
        for (String token : output) {
            if (isOperator(token)) {
                if (stack.size() < 2) throw new IllegalArgumentException("Invalid expression");
                double b = stack.pop();
                double a = stack.pop();
                stack.push(applyOperator(token, a, b));
            } else {
                try {
                    stack.push(Double.parseDouble(token));
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("Invalid number: " + token);
                }
            }
        }
        if (stack.size() != 1) throw new IllegalArgumentException("Invalid expression");
        return stack.pop();
    }

    private static boolean isOperatorChar(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/' || c == '%' || c == '^';
    }

    private static boolean isOperator(String s) {
        return "+".equals(s) || "-".equals(s) || "*".equals(s) || "/".equals(s) || "%".equals(s) || "^".equals(s);
    }

    private static int precedence(String op) {
        return switch (op) {
            case "^" -> 4;
            case "*", "/", "%" -> 3;
            case "+", "-" -> 2;
            default -> 0;
        };
    }

    private static boolean isRightAssociative(String op) {
        return "^".equals(op);
    }

    private static double applyOperator(String op, double a, double b) {
        return switch (op) {
            case "+" -> a + b;
            case "-" -> a - b;
            case "*" -> a * b;
            case "/" -> b != 0 ? a / b : 0.0;
            case "%" -> b != 0 ? a % b : 0.0;
            case "^" -> Math.pow(a, b);
            default -> 0.0;
        };
    }
}
