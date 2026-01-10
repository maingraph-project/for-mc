package ltd.opens.mg.mc.core.blueprint.nodes;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeHelper;
import ltd.opens.mg.mc.core.blueprint.NodePorts;
import ltd.opens.mg.mc.core.blueprint.NodeThemes;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

import ltd.opens.mg.mc.core.blueprint.events.RegisterMGMCNodesEvent;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * 变量与常量类节点注册
 */
public class VariableNodes {

    @SubscribeEvent
    public static void onRegister(RegisterMGMCNodesEvent event) {
        // --- 常量节点 ---
        NodeHelper.setup("float", "node.mgmc.float.name")
            .category("node_category.mgmc.variable.float")
            .color(NodeThemes.COLOR_NODE_VARIABLE)
            .property("input_type", "float")
            .input(NodePorts.VALUE, "node.mgmc.port.value", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 0.0)
            .output(NodePorts.VALUE, "node.mgmc.port.output", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .registerValue((node, portId, ctx) -> {
                return TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.VALUE, ctx));
            });

        NodeHelper.setup("boolean", "node.mgmc.boolean.name")
            .category("node_category.mgmc.variable.boolean")
            .color(NodeThemes.COLOR_NODE_VARIABLE)
            .property("input_type", "boolean")
            .input(NodePorts.VALUE, "node.mgmc.port.value", NodeDefinition.PortType.BOOLEAN, NodeThemes.COLOR_PORT_BOOLEAN, false)
            .output(NodePorts.VALUE, "node.mgmc.port.output", NodeDefinition.PortType.BOOLEAN, NodeThemes.COLOR_PORT_BOOLEAN)
            .registerValue((node, portId, ctx) -> {
                return TypeConverter.toBoolean(NodeLogicRegistry.evaluateInput(node, NodePorts.VALUE, ctx));
            });

        NodeHelper.setup("string", "node.mgmc.string.name")
            .category("node_category.mgmc.variable.string")
            .color(NodeThemes.COLOR_NODE_VARIABLE)
            .property("input_type", "multiline_text")
            .input(NodePorts.VALUE, "node.mgmc.port.value", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING, "")
            .output(NodePorts.VALUE, "node.mgmc.port.output", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .registerValue((node, portId, ctx) -> {
                return TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.VALUE, ctx));
            });

        // --- 变量操作 ---
        NodeHelper.setup("get_variable", "node.mgmc.get_variable.name")
            .category("node_category.mgmc.variable")
            .color(NodeThemes.COLOR_NODE_VARIABLE)
            .input(NodePorts.NAME, "node.mgmc.port.name", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING, "")
            .output(NodePorts.VALUE, "node.mgmc.port.value", NodeDefinition.PortType.OBJECT, NodeThemes.COLOR_PORT_ANY)
            .registerValue((node, portId, ctx) -> {
                String name = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.NAME, ctx));
                if (name == null || name.trim().isEmpty()) return null;
                return ctx.variables.get(name.trim());
            });

        NodeHelper.setup("set_variable", "node.mgmc.set_variable.name")
            .category("node_category.mgmc.variable")
            .color(NodeThemes.COLOR_NODE_VARIABLE)
            .execIn()
            .execOut()
            .input(NodePorts.NAME, "node.mgmc.port.name", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING, "")
            .input(NodePorts.VALUE, "node.mgmc.port.value", NodeDefinition.PortType.OBJECT, NodeThemes.COLOR_PORT_ANY)
            .output(NodePorts.VALUE, "node.mgmc.port.value", NodeDefinition.PortType.OBJECT, NodeThemes.COLOR_PORT_ANY)
            .register(new NodeHelper.NodeHandlerAdapter() {
                @Override
                public void execute(JsonObject node, NodeContext ctx) {
                    String name = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.NAME, ctx));
                    Object value = NodeLogicRegistry.evaluateInput(node, NodePorts.VALUE, ctx);
                    if (name != null && !name.trim().isEmpty()) {
                        ctx.variables.put(name.trim(), value);
                    }
                    NodeLogicRegistry.triggerExec(node, NodePorts.EXEC, ctx);
                }

                @Override
                public Object getValue(JsonObject node, String portId, NodeContext ctx) {
                    return NodeLogicRegistry.evaluateInput(node, NodePorts.VALUE, ctx);
                }
            });
    }
}
