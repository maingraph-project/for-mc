package ltd.opens.mg.mc.core.blueprint.nodes;

import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeHelper;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

import java.util.ArrayList;
import java.util.List;

/**
 * 字符串操作相关节点
 */
public class StringNodes {
    private static final int COLOR_STRING = 0xFF66AAFF;
    private static final int COLOR_BOOLEAN = 0xFF4444FF;
    private static final int COLOR_FLOAT = 0xFFFFCC00;
    private static final int COLOR_LIST = 0xFF44AA44;

    public static void register() {
        // 字符串连接 (Concat)
        NodeHelper.setup("string_concat", "node.mgmc.string_concat.name")
            .category("node_category.mgmc.variable.string")
            .color(COLOR_STRING)
            .input("a", "node.mgmc.string_concat.port.a", NodeDefinition.PortType.STRING, COLOR_STRING)
            .input("b", "node.mgmc.string_concat.port.b", NodeDefinition.PortType.STRING, COLOR_STRING)
            .output("output", "node.mgmc.port.output", NodeDefinition.PortType.STRING, COLOR_STRING)
            .registerValue((node, portId, ctx) -> {
                String a = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "a", ctx));
                String b = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "b", ctx));
                return (a == null ? "" : a) + (b == null ? "" : b);
            });

        // 字符串组合 (Combine) - 修复硬编码，支持动态输入
        NodeHelper.setup("string_combine", "node.mgmc.string_combine.name")
            .category("node_category.mgmc.variable.string")
            .color(COLOR_STRING)
            .property("dynamic_inputs", true) // 允许 UI 动态添加输入
            .output("output", "node.mgmc.port.output", NodeDefinition.PortType.STRING, COLOR_STRING)
            .registerValue((node, portId, ctx) -> {
                StringBuilder sb = new StringBuilder();
                if (node.has("inputs")) {
                    var inputs = node.getAsJsonObject("inputs");
                    List<String> keys = new ArrayList<>(inputs.keySet());
                    // 按照端口 ID 排序以保证合并顺序
                    keys.sort((k1, k2) -> {
                        try {
                            int numA = extractNumber(k1);
                            int numB = extractNumber(k2);
                            return Integer.compare(numA, numB);
                        } catch (Exception e) {
                            return k1.compareTo(k2);
                        }
                    });

                    for (String key : keys) {
                        if (key.equals("exec")) continue;
                        String val = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, key, ctx));
                        if (val != null) sb.append(val);
                    }
                }
                return sb.toString();
            });

        // 字符串长度 (Length)
        NodeHelper.setup("string_length", "node.mgmc.string_length.name")
            .category("node_category.mgmc.variable.string")
            .color(COLOR_STRING)
            .input("string", "node.mgmc.port.input", NodeDefinition.PortType.STRING, COLOR_STRING)
            .output("length", "node.mgmc.port.length", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .registerValue((node, portId, ctx) -> {
                String s = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "string", ctx));
                return s == null ? 0.0 : (double) s.length();
            });

        // 字符串包含 (Contains)
        NodeHelper.setup("string_contains", "node.mgmc.string_contains.name")
            .category("node_category.mgmc.variable.string")
            .color(COLOR_STRING)
            .input("string", "node.mgmc.port.input", NodeDefinition.PortType.STRING, COLOR_STRING)
            .input("substring", "node.mgmc.string_contains.port.substring", NodeDefinition.PortType.STRING, COLOR_STRING)
            .output("result", "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN)
            .registerValue((node, portId, ctx) -> {
                String s = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "string", ctx));
                String sub = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "substring", ctx));
                return s != null && sub != null && s.contains(sub);
            });

        // 字符串替换 (Replace)
        NodeHelper.setup("string_replace", "node.mgmc.string_replace.name")
            .category("node_category.mgmc.variable.string")
            .color(COLOR_STRING)
            .input("string", "node.mgmc.port.input", NodeDefinition.PortType.STRING, COLOR_STRING)
            .input("old", "node.mgmc.string_replace.port.old", NodeDefinition.PortType.STRING, COLOR_STRING)
            .input("new", "node.mgmc.string_replace.port.new", NodeDefinition.PortType.STRING, COLOR_STRING)
            .output("output", "node.mgmc.port.output", NodeDefinition.PortType.STRING, COLOR_STRING)
            .registerValue((node, portId, ctx) -> {
                String s = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "string", ctx));
                String oldS = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "old", ctx));
                String newS = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "new", ctx));
                if (s == null || oldS == null || newS == null) return s;
                return s.replace(oldS, newS);
            });

        // 字符串截取 (Substring)
        NodeHelper.setup("string_substring", "node.mgmc.string_substring.name")
            .category("node_category.mgmc.variable.string")
            .color(COLOR_STRING)
            .input("string", "node.mgmc.port.input", NodeDefinition.PortType.STRING, COLOR_STRING)
            .input("start", "node.mgmc.string_substring.port.start", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, true, 0.0)
            .input("end", "node.mgmc.string_substring.port.end", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, true, 5.0)
            .output("output", "node.mgmc.port.output", NodeDefinition.PortType.STRING, COLOR_STRING)
            .registerValue((node, portId, ctx) -> {
                String s = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "string", ctx));
                int start = TypeConverter.toInt(NodeLogicRegistry.evaluateInput(node, "start", ctx));
                int end = TypeConverter.toInt(NodeLogicRegistry.evaluateInput(node, "end", ctx));
                if (s == null) return "";
                start = Math.max(0, Math.min(start, s.length()));
                end = Math.max(start, Math.min(end, s.length()));
                return s.substring(start, end);
            });

        // 字符串大小写转换 (Case)
        NodeHelper.setup("string_case", "node.mgmc.string_case.name")
            .category("node_category.mgmc.variable.string")
            .color(COLOR_STRING)
            .input("string", "node.mgmc.port.input", NodeDefinition.PortType.STRING, COLOR_STRING)
            .input("mode", "node.mgmc.string_case.port.mode", NodeDefinition.PortType.STRING, COLOR_STRING, true, "UPPER",
                new String[]{"UPPER", "LOWER", "TRIM"})
            .output("output", "node.mgmc.port.output", NodeDefinition.PortType.STRING, COLOR_STRING)
            .registerValue((node, portId, ctx) -> {
                String s = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "string", ctx));
                String mode = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "mode", ctx));
                if (s == null) return "";
                return switch (mode != null ? mode : "UPPER") {
                    case "LOWER" -> s.toLowerCase();
                    case "TRIM" -> s.trim();
                    default -> s.toUpperCase();
                };
            });

        // 字符串分割 (Split) - 归类到列表，因为输出是列表
        NodeHelper.setup("string_split", "node.mgmc.string_split.name")
            .category("node_category.mgmc.variable.list")
            .color(COLOR_LIST)
            .input("string", "node.mgmc.port.input", NodeDefinition.PortType.STRING, COLOR_STRING)
            .input("delimiter", "node.mgmc.port.delimiter", NodeDefinition.PortType.STRING, COLOR_STRING, true, ",")
            .output("list", "node.mgmc.port.list", NodeDefinition.PortType.LIST, COLOR_LIST)
            .registerValue((node, portId, ctx) -> {
                String s = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "string", ctx));
                String delim = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "delimiter", ctx));
                if (s == null) return List.of();
                if (delim == null || delim.isEmpty()) return List.of(s);
                return List.of(s.split(java.util.regex.Pattern.quote(delim)));
            });
    }

    private static int extractNumber(String s) {
        String num = s.replaceAll("\\D", "");
        return num.isEmpty() ? 0 : Integer.parseInt(num);
    }
}
