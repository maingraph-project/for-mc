package ltd.opens.mg.mc.core.blueprint.nodes;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeHelper;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 列表操作节点注册
 */
public class ListNodes {
    private static final int COLOR_LIST = 0xFFFFCC00;
    private static final int COLOR_FLOAT = 0xFF00FF00;
    private static final int COLOR_STRING = 0xFFDA00DA;
    private static final int COLOR_BOOLEAN = 0xFF920101;
    private static final int COLOR_ANY = 0xFFAAAAAA;
    
    private static final Random RANDOM = new Random();

    public static void register() {
        // --- 1. 获取元素 (get_list_item) ---
        NodeHelper.setup("get_list_item", "node.mgmc.get_list_item.name")
            .category("node_category.mgmc.variable.list")
            .color(COLOR_LIST)
            .input("list", "node.mgmc.get_list_item.port.list", NodeDefinition.PortType.LIST, COLOR_LIST)
            .input("index", "node.mgmc.get_list_item.port.index", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .output("value", "node.mgmc.get_list_item.port.value", NodeDefinition.PortType.ANY, COLOR_ANY)
            .registerValue((node, portId, ctx) -> {
                try {
                    List<Object> list = TypeConverter.toList(NodeLogicRegistry.evaluateInput(node, "list", ctx));
                    int index = TypeConverter.toInt(NodeLogicRegistry.evaluateInput(node, "index", ctx));
                    if (list != null && index >= 0 && index < list.size()) {
                        return list.get(index);
                    }
                } catch (Exception e) {}
                return null;
            });

        // --- 2. 添加元素 (list_add) ---
        NodeHelper.setup("list_add", "node.mgmc.list_add.name")
            .category("node_category.mgmc.variable.list")
            .color(COLOR_LIST)
            .input("list", "node.mgmc.port.list", NodeDefinition.PortType.LIST, COLOR_LIST)
            .input("item", "node.mgmc.port.value", NodeDefinition.PortType.ANY, COLOR_ANY)
            .output("list", "node.mgmc.port.list", NodeDefinition.PortType.LIST, COLOR_LIST)
            .registerValue((node, portId, ctx) -> {
                List<Object> list = new ArrayList<>(TypeConverter.toList(NodeLogicRegistry.evaluateInput(node, "list", ctx)));
                Object item = NodeLogicRegistry.evaluateInput(node, "item", ctx);
                list.add(item);
                return list;
            });

        // --- 3. 移除元素 (list_remove) ---
        NodeHelper.setup("list_remove", "node.mgmc.list_remove.name")
            .category("node_category.mgmc.variable.list")
            .color(COLOR_LIST)
            .input("list", "node.mgmc.port.list", NodeDefinition.PortType.LIST, COLOR_LIST)
            .input("index", "node.mgmc.port.index", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .output("list", "node.mgmc.port.list", NodeDefinition.PortType.LIST, COLOR_LIST)
            .registerValue((node, portId, ctx) -> {
                List<Object> list = new ArrayList<>(TypeConverter.toList(NodeLogicRegistry.evaluateInput(node, "list", ctx)));
                int index = (int) TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "index", ctx));
                if (index >= 0 && index < list.size()) {
                    list.remove(index);
                }
                return list;
            });

        // --- 4. 列表长度 (list_length) ---
        NodeHelper.setup("list_length", "node.mgmc.list_length.name")
            .category("node_category.mgmc.variable.list")
            .color(COLOR_LIST)
            .input("list", "node.mgmc.port.list", NodeDefinition.PortType.LIST, COLOR_LIST)
            .output("length", "node.mgmc.port.length", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .registerValue((node, portId, ctx) -> {
                try {
                    List<Object> list = TypeConverter.toList(NodeLogicRegistry.evaluateInput(node, "list", ctx));
                    return list != null ? (double) list.size() : 0.0;
                } catch (Exception e) {
                    return 0.0;
                }
            });

        // --- 5. 包含元素 (list_contains) ---
        NodeHelper.setup("list_contains", "node.mgmc.list_contains.name")
            .category("node_category.mgmc.variable.list")
            .color(COLOR_LIST)
            .input("list", "node.mgmc.port.list", NodeDefinition.PortType.LIST, COLOR_LIST)
            .input("item", "node.mgmc.port.value", NodeDefinition.PortType.ANY, COLOR_ANY)
            .output("result", "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN)
            .registerValue((node, portId, ctx) -> {
                List<Object> list = TypeConverter.toList(NodeLogicRegistry.evaluateInput(node, "list", ctx));
                Object item = NodeLogicRegistry.evaluateInput(node, "item", ctx);
                if (list == null) return false;
                
                if (list.contains(item)) return true;

                // 兜底逻辑：如果对象引用不同，尝试数值比较或字符串比较
                if (item instanceof Number num) {
                    double dv = num.doubleValue();
                    for (Object o : list) {
                        if (o instanceof Number on && on.doubleValue() == dv) return true;
                    }
                }

                String itemStr = TypeConverter.toString(item);
                for (Object o : list) {
                    if (TypeConverter.toString(o).equals(itemStr)) return true;
                }
                return false;
            });

        // --- 6. 设置元素 (list_set_item) ---
        NodeHelper.setup("list_set_item", "node.mgmc.list_set_item.name")
            .category("node_category.mgmc.variable.list")
            .color(COLOR_LIST)
            .input("list", "node.mgmc.port.list", NodeDefinition.PortType.LIST, COLOR_LIST)
            .input("index", "node.mgmc.port.index", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .input("value", "node.mgmc.port.value", NodeDefinition.PortType.ANY, COLOR_ANY)
            .output("list", "node.mgmc.port.list", NodeDefinition.PortType.LIST, COLOR_LIST)
            .registerValue((node, portId, ctx) -> {
                try {
                    List<Object> list = new ArrayList<>(TypeConverter.toList(NodeLogicRegistry.evaluateInput(node, "list_in", ctx)));
                    int index = TypeConverter.toInt(NodeLogicRegistry.evaluateInput(node, "index", ctx));
                    Object value = NodeLogicRegistry.evaluateInput(node, "value", ctx);
                    
                    if (index >= 0 && index < list.size()) {
                        list.set(index, value);
                    } else if (index == list.size()) {
                        list.add(value);
                    }
                    return list;
                } catch (Exception e) {
                    return null;
                }
            });

        // --- 7. 合并/转字符串 (list_join) ---
        NodeHelper.setup("list_join", "node.mgmc.list_join.name")
            .category("node_category.mgmc.variable.list")
            .color(COLOR_LIST)
            .input("list", "node.mgmc.port.list", NodeDefinition.PortType.LIST, COLOR_LIST)
            .input("delimiter", "node.mgmc.port.delimiter", NodeDefinition.PortType.STRING, COLOR_STRING, ",")
            .output("string", "node.mgmc.port.output", NodeDefinition.PortType.STRING, COLOR_STRING)
            .registerValue((node, portId, ctx) -> {
                List<Object> list = TypeConverter.toList(NodeLogicRegistry.evaluateInput(node, "list", ctx));
                String delim = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "delimiter", ctx));
                if (list == null) return "";
                if (delim == null) delim = "";
                
                return list.stream()
                        .map(TypeConverter::toString)
                        .collect(java.util.stream.Collectors.joining(delim));
            });

        // --- 8. 随机元素 (random_list_item) ---
        NodeHelper.setup("random_list_item", "node.mgmc.random_list_item.name")
            .category("node_category.mgmc.variable.list")
            .color(COLOR_LIST)
            .input("list", "node.mgmc.port.list", NodeDefinition.PortType.LIST, COLOR_LIST)
            .output("item", "node.mgmc.port.value", NodeDefinition.PortType.ANY, COLOR_ANY)
            .registerValue((node, portId, ctx) -> {
                List<?> list = TypeConverter.toList(NodeLogicRegistry.evaluateInput(node, "list", ctx));
                if (list != null && !list.isEmpty()) {
                    return list.get(RANDOM.nextInt(list.size()));
                }
                return null;
            });
    }
}
