package ltd.opens.mg.mc.core.blueprint.nodes;

import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeHelper;
import ltd.opens.mg.mc.core.blueprint.NodePorts;
import ltd.opens.mg.mc.core.blueprint.NodeThemes;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

import ltd.opens.mg.mc.core.blueprint.events.RegisterMGMCNodesEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * 字符串操作相关节点
 */
public class StringNodes {

    @SubscribeEvent
    public static void onRegister(RegisterMGMCNodesEvent event) {
        // 字符串连接 (Concat)
        NodeHelper.setup("string_concat", "node.mgmc.string_concat.name")
            .category("node_category.mgmc.variable.string")
            .color(NodeThemes.COLOR_NODE_VARIABLE)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/variable/string/string_concat")
            .input(NodePorts.A, "node.mgmc.string_concat.port.a", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .input(NodePorts.B, "node.mgmc.string_concat.port.b", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.OUTPUT, "node.mgmc.port.output", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .registerValue((node, portId, ctx) -> {
                String a = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.A, ctx));
                String b = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.B, ctx));
                return (a == null ? "" : a) + (b == null ? "" : b);
            });

        // 字符串组合 (Combine) - 修复硬编码，支持动态输入
        NodeHelper.setup("string_combine", "node.mgmc.string_combine.name")
            .category("node_category.mgmc.variable.string")
            .color(NodeThemes.COLOR_NODE_VARIABLE)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/variable/string/string_combine")
            .property("dynamic_inputs", true) // 允许 UI 动态添加输入
            .button("node.mgmc.string_combine.add_input", "add_input_indexed")
            .output(NodePorts.OUTPUT, "node.mgmc.port.output", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
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
                        if (key.equals(NodePorts.EXEC)) continue;
                        String val = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, key, ctx));
                        if (val != null) sb.append(val);
                    }
                }
                return sb.toString();
            });

        // 字符串长度 (Length)
        NodeHelper.setup("string_length", "node.mgmc.string_length.name")
            .category("node_category.mgmc.variable.string")
            .color(NodeThemes.COLOR_NODE_VARIABLE)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/variable/string/string_length")
            .input(NodePorts.STRING, "node.mgmc.port.input", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.LENGTH, "node.mgmc.port.length", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .registerValue((node, portId, ctx) -> {
                String s = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.STRING, ctx));
                return s == null ? 0.0 : (double) s.length();
            });

        // 字符串包含 (Contains)
        NodeHelper.setup("string_contains", "node.mgmc.string_contains.name")
            .category("node_category.mgmc.variable.string")
            .color(NodeThemes.COLOR_NODE_VARIABLE)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/variable/string/string_contains")
            .input(NodePorts.STRING, "node.mgmc.port.input", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .input(NodePorts.SUBSTRING, "node.mgmc.string_contains.port.substring", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.RESULT, "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, NodeThemes.COLOR_PORT_BOOLEAN)
            .registerValue((node, portId, ctx) -> {
                String s = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.STRING, ctx));
                String sub = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.SUBSTRING, ctx));
                return s != null && sub != null && s.contains(sub);
            });

        // 字符串替换 (Replace)
        NodeHelper.setup("string_replace", "node.mgmc.string_replace.name")
            .category("node_category.mgmc.variable.string")
            .color(NodeThemes.COLOR_NODE_VARIABLE)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/variable/string/string_replace")
            .input(NodePorts.STRING, "node.mgmc.port.input", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .input(NodePorts.OLD, "node.mgmc.string_replace.port.old", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .input(NodePorts.NEW, "node.mgmc.string_replace.port.new", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.OUTPUT, "node.mgmc.port.output", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .registerValue((node, portId, ctx) -> {
                String s = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.STRING, ctx));
                String oldS = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.OLD, ctx));
                String newS = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.NEW, ctx));
                if (s == null || oldS == null || newS == null) return s;
                return s.replace(oldS, newS);
            });

        // 字符串截取 (Substring)
        NodeHelper.setup("string_substring", "node.mgmc.string_substring.name")
            .category("node_category.mgmc.variable.string")
            .color(NodeThemes.COLOR_NODE_VARIABLE)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/variable/string/string_substring")
            .input(NodePorts.STRING, "node.mgmc.port.input", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .input(NodePorts.START, "node.mgmc.string_substring.port.start", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, true, 0.0)
            .input(NodePorts.END, "node.mgmc.string_substring.port.end", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, true, 5.0)
            .output(NodePorts.OUTPUT, "node.mgmc.port.output", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .registerValue((node, portId, ctx) -> {
                String s = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.STRING, ctx));
                int start = TypeConverter.toInt(NodeLogicRegistry.evaluateInput(node, NodePorts.START, ctx));
                int end = TypeConverter.toInt(NodeLogicRegistry.evaluateInput(node, NodePorts.END, ctx));
                if (s == null) return "";
                start = Math.max(0, Math.min(start, s.length()));
                end = Math.max(start, Math.min(end, s.length()));
                return s.substring(start, end);
            });

        // 字符串大小写转换 (Case)
        NodeHelper.setup("string_case", "node.mgmc.string_case.name")
            .category("node_category.mgmc.variable.string")
            .color(NodeThemes.COLOR_NODE_VARIABLE)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/variable/string/string_case")
            .input(NodePorts.STRING, "node.mgmc.port.input", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .input(NodePorts.MODE, "node.mgmc.string_case.port.mode", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING, true, "UPPER",
                new String[]{"UPPER", "LOWER", "TRIM"})
            .output(NodePorts.OUTPUT, "node.mgmc.port.output", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .registerValue((node, portId, ctx) -> {
                String s = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.STRING, ctx));
                String mode = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.MODE, ctx));
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
            .color(NodeThemes.COLOR_NODE_VARIABLE)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/variable/list/string_split")
            .input(NodePorts.STRING, "node.mgmc.port.input", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .input(NodePorts.DELIMITER, "node.mgmc.port.delimiter", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING, true, ",")
            .output(NodePorts.LIST, "node.mgmc.port.list", NodeDefinition.PortType.LIST, NodeThemes.COLOR_PORT_LIST)
            .registerValue((node, portId, ctx) -> {
                String s = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.STRING, ctx));
                String delim = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.DELIMITER, ctx));
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
