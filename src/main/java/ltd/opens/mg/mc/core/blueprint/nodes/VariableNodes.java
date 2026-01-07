package ltd.opens.mg.mc.core.blueprint.nodes;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeHelper;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

/**
 * 变量与常量节点注册
 */
public class VariableNodes {
    private static final int COLOR_VARIABLE = 0xFFFFAA00;
    private static final int COLOR_STRING = 0xFFDA00DA;
    private static final int COLOR_FLOAT = 0xFF00FF00;
    private static final int COLOR_BOOLEAN = 0xFF920101;
    private static final int COLOR_EXEC = 0xFFFFFFFF;

    public static void register() {
        // --- 常量节点 ---
        NodeHelper.setup("float", "node.mgmc.float.name")
            .category("node_category.mgmc.variable.float")
            .color(COLOR_FLOAT)
            .property("input_type", "float")
            .input("value", "node.mgmc.port.value", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .output("value", "node.mgmc.port.output", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .registerValue((node, portId, ctx) -> {
                return TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "value", ctx));
            });

        NodeHelper.setup("boolean", "node.mgmc.boolean.name")
            .category("node_category.mgmc.variable.boolean")
            .color(COLOR_BOOLEAN)
            .property("input_type", "boolean")
            .input("value", "node.mgmc.port.value", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN, false)
            .output("value", "node.mgmc.port.output", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN)
            .registerValue((node, portId, ctx) -> {
                return TypeConverter.toBoolean(NodeLogicRegistry.evaluateInput(node, "value", ctx));
            });

        NodeHelper.setup("string", "node.mgmc.string.name")
            .category("node_category.mgmc.variable.string")
            .color(COLOR_STRING)
            .property("input_type", "multiline_text")
            .input("value", "node.mgmc.port.value", NodeDefinition.PortType.STRING, COLOR_STRING, "")
            .output("value", "node.mgmc.port.output", NodeDefinition.PortType.STRING, COLOR_STRING)
            .registerValue((node, portId, ctx) -> {
                return TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "value", ctx));
            });

        // --- 变量操作 ---
        NodeHelper.setup("get_variable", "node.mgmc.get_variable.name")
            .category("node_category.mgmc.variable")
            .color(COLOR_VARIABLE)
            .input("name", "node.mgmc.port.name", NodeDefinition.PortType.STRING, COLOR_STRING, "")
            .output("value", "node.mgmc.port.value", NodeDefinition.PortType.OBJECT, 0xFFFFFFFF)
            .registerValue((node, portId, ctx) -> {
                String name = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "name", ctx));
                if (name == null || name.trim().isEmpty()) return null;
                return ctx.variables.get(name.trim());
            });

        NodeHelper.setup("set_variable", "node.mgmc.set_variable.name")
            .category("node_category.mgmc.variable")
            .color(COLOR_VARIABLE)
            .execIn()
            .execOut()
            .input("name", "node.mgmc.port.name", NodeDefinition.PortType.STRING, COLOR_STRING, "")
            .input("value", "node.mgmc.port.value", NodeDefinition.PortType.OBJECT, 0xFFFFFFFF)
            .output("value", "node.mgmc.port.value", NodeDefinition.PortType.OBJECT, 0xFFFFFFFF)
            .register(new NodeHelper.NodeHandlerAdapter() {
                @Override
                public void execute(JsonObject node, NodeContext ctx) {
                    String name = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "name", ctx));
                    Object value = NodeLogicRegistry.evaluateInput(node, "value", ctx);
                    if (name != null && !name.trim().isEmpty()) {
                        ctx.variables.put(name.trim(), value);
                    }
                    NodeLogicRegistry.triggerExec(node, "exec", ctx);
                }

                @Override
                public Object getValue(JsonObject node, String portId, NodeContext ctx) {
                    return NodeLogicRegistry.evaluateInput(node, "value", ctx);
                }
            });
    }
}
