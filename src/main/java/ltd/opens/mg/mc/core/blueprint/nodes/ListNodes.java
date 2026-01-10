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
import java.util.Random;

/**
 * 列表操作节点注册
 */
public class ListNodes {
    
    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onRegister(RegisterMGMCNodesEvent event) {
        // --- 1. 获取元素 (get_list_item) ---
        NodeHelper.setup("get_list_item", "node.mgmc.get_list_item.name")
            .category("node_category.mgmc.variable.list")
            .color(NodeThemes.COLOR_NODE_VARIABLE)
            .input(NodePorts.LIST, "node.mgmc.get_list_item.port.list", NodeDefinition.PortType.LIST, NodeThemes.COLOR_PORT_LIST)
            .input(NodePorts.INDEX, "node.mgmc.get_list_item.port.index", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 0.0)
            .output(NodePorts.VALUE, "node.mgmc.get_list_item.port.value", NodeDefinition.PortType.ANY, NodeThemes.COLOR_PORT_ANY)
            .registerValue((node, portId, ctx) -> {
                try {
                    List<Object> list = TypeConverter.toList(NodeLogicRegistry.evaluateInput(node, NodePorts.LIST, ctx));
                    int index = TypeConverter.toInt(NodeLogicRegistry.evaluateInput(node, NodePorts.INDEX, ctx));
                    if (list != null && index >= 0 && index < list.size()) {
                        return list.get(index);
                    }
                } catch (Exception e) {}
                return null;
            });

        // --- 2. 添加元素 (list_add) ---
        NodeHelper.setup("list_add", "node.mgmc.list_add.name")
            .category("node_category.mgmc.variable.list")
            .color(NodeThemes.COLOR_NODE_VARIABLE)
            .input(NodePorts.LIST, "node.mgmc.port.list", NodeDefinition.PortType.LIST, NodeThemes.COLOR_PORT_LIST)
            .input(NodePorts.ITEM, "node.mgmc.port.value", NodeDefinition.PortType.ANY, NodeThemes.COLOR_PORT_ANY)
            .output(NodePorts.LIST, "node.mgmc.port.list", NodeDefinition.PortType.LIST, NodeThemes.COLOR_PORT_LIST)
            .registerValue((node, portId, ctx) -> {
                List<Object> list = new ArrayList<>(TypeConverter.toList(NodeLogicRegistry.evaluateInput(node, NodePorts.LIST, ctx)));
                Object item = NodeLogicRegistry.evaluateInput(node, NodePorts.ITEM, ctx);
                list.add(item);
                return list;
            });

        // --- 3. 移除元素 (list_remove) ---
        NodeHelper.setup("list_remove", "node.mgmc.list_remove.name")
            .category("node_category.mgmc.variable.list")
            .color(NodeThemes.COLOR_NODE_VARIABLE)
            .input(NodePorts.LIST, "node.mgmc.port.list", NodeDefinition.PortType.LIST, NodeThemes.COLOR_PORT_LIST)
            .input(NodePorts.INDEX, "node.mgmc.port.index", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 0.0)
            .output(NodePorts.LIST, "node.mgmc.port.list", NodeDefinition.PortType.LIST, NodeThemes.COLOR_PORT_LIST)
            .registerValue((node, portId, ctx) -> {
                List<Object> list = new ArrayList<>(TypeConverter.toList(NodeLogicRegistry.evaluateInput(node, NodePorts.LIST, ctx)));
                int index = (int) TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.INDEX, ctx));
                if (index >= 0 && index < list.size()) {
                    list.remove(index);
                }
                return list;
            });

        // --- 4. 列表长度 (list_length) ---
        NodeHelper.setup("list_length", "node.mgmc.list_length.name")
            .category("node_category.mgmc.variable.list")
            .color(NodeThemes.COLOR_NODE_VARIABLE)
            .input(NodePorts.LIST, "node.mgmc.port.list", NodeDefinition.PortType.LIST, NodeThemes.COLOR_PORT_LIST)
            .output(NodePorts.LENGTH, "node.mgmc.port.length", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .registerValue((node, portId, ctx) -> {
                try {
                    List<Object> list = TypeConverter.toList(NodeLogicRegistry.evaluateInput(node, NodePorts.LIST, ctx));
                    return list != null ? (double) list.size() : 0.0;
                } catch (Exception e) {
                    return 0.0;
                }
            });

        // --- 5. 包含元素 (list_contains) ---
        NodeHelper.setup("list_contains", "node.mgmc.list_contains.name")
            .category("node_category.mgmc.variable.list")
            .color(NodeThemes.COLOR_NODE_VARIABLE)
            .input(NodePorts.LIST, "node.mgmc.port.list", NodeDefinition.PortType.LIST, NodeThemes.COLOR_PORT_LIST)
            .input(NodePorts.ITEM, "node.mgmc.port.value", NodeDefinition.PortType.ANY, NodeThemes.COLOR_PORT_ANY)
            .output(NodePorts.RESULT, "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, NodeThemes.COLOR_PORT_BOOLEAN)
            .registerValue((node, portId, ctx) -> {
                List<Object> list = TypeConverter.toList(NodeLogicRegistry.evaluateInput(node, NodePorts.LIST, ctx));
                Object item = NodeLogicRegistry.evaluateInput(node, NodePorts.ITEM, ctx);
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
            .color(NodeThemes.COLOR_NODE_VARIABLE)
            .input(NodePorts.LIST, "node.mgmc.port.list", NodeDefinition.PortType.LIST, NodeThemes.COLOR_PORT_LIST)
            .input(NodePorts.INDEX, "node.mgmc.port.index", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 0.0)
            .input(NodePorts.VALUE, "node.mgmc.port.value", NodeDefinition.PortType.ANY, NodeThemes.COLOR_PORT_ANY)
            .output(NodePorts.LIST, "node.mgmc.port.list", NodeDefinition.PortType.LIST, NodeThemes.COLOR_PORT_LIST)
            .registerValue((node, portId, ctx) -> {
                try {
                    List<Object> list = new ArrayList<>(TypeConverter.toList(NodeLogicRegistry.evaluateInput(node, NodePorts.LIST, ctx)));
                    int index = TypeConverter.toInt(NodeLogicRegistry.evaluateInput(node, NodePorts.INDEX, ctx));
                    Object value = NodeLogicRegistry.evaluateInput(node, NodePorts.VALUE, ctx);
                    
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
            .color(NodeThemes.COLOR_NODE_VARIABLE)
            .input(NodePorts.LIST, "node.mgmc.port.list", NodeDefinition.PortType.LIST, NodeThemes.COLOR_PORT_LIST)
            .input(NodePorts.DELIMITER, "node.mgmc.port.delimiter", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING, ",")
            .output(NodePorts.STRING, "node.mgmc.port.output", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .registerValue((node, portId, ctx) -> {
                List<Object> list = TypeConverter.toList(NodeLogicRegistry.evaluateInput(node, NodePorts.LIST, ctx));
                String delim = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.DELIMITER, ctx));
                if (list == null) return "";
                if (delim == null) delim = "";
                
                return list.stream()
                        .map(TypeConverter::toString)
                        .collect(java.util.stream.Collectors.joining(delim));
            });

        // --- 8. 随机元素 (random_list_item) ---
        NodeHelper.setup("random_list_item", "node.mgmc.random_list_item.name")
            .category("node_category.mgmc.variable.list")
            .color(NodeThemes.COLOR_NODE_VARIABLE)
            .input(NodePorts.LIST, "node.mgmc.port.list", NodeDefinition.PortType.LIST, NodeThemes.COLOR_PORT_LIST)
            .output(NodePorts.ITEM, "node.mgmc.port.value", NodeDefinition.PortType.ANY, NodeThemes.COLOR_PORT_ANY)
            .registerValue((node, portId, ctx) -> {
                List<?> list = TypeConverter.toList(NodeLogicRegistry.evaluateInput(node, NodePorts.LIST, ctx));
                if (list != null && !list.isEmpty()) {
                    return list.get(RANDOM.nextInt(list.size()));
                }
                return null;
            });
    }
}
