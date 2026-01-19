package ltd.opens.mg.mc.core.blueprint.nodes;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.MaingraphforMC;
import ltd.opens.mg.mc.core.blueprint.EntityVariableManager;
import ltd.opens.mg.mc.core.blueprint.GlobalVariableManager;
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
            .output(NodePorts.VALUE, "node.mgmc.port.value", NodeDefinition.PortType.ANY, NodeThemes.COLOR_PORT_ANY)
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
            .input(NodePorts.VALUE, "node.mgmc.port.value", NodeDefinition.PortType.ANY, NodeThemes.COLOR_PORT_ANY)
            .output(NodePorts.VALUE, "node.mgmc.port.value", NodeDefinition.PortType.ANY, NodeThemes.COLOR_PORT_ANY)
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

        // --- 全局持久化变量 ---
        NodeHelper.setup("get_global_variable", "node.mgmc.get_global_variable.name")
            .category("node_category.mgmc.variable")
            .color(NodeThemes.COLOR_NODE_VARIABLE)
            .input(NodePorts.NAME, "node.mgmc.port.name", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING, "")
            .output(NodePorts.VALUE, "node.mgmc.port.value", NodeDefinition.PortType.ANY, NodeThemes.COLOR_PORT_ANY)
            .registerValue((node, portId, ctx) -> {
                String name = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.NAME, ctx));
                if (name == null || name.trim().isEmpty()) return null;
                GlobalVariableManager manager = MaingraphforMC.getGlobalVariableManager();
                return manager != null ? manager.get(name.trim()) : null;
            });

        NodeHelper.setup("set_global_variable", "node.mgmc.set_global_variable.name")
            .category("node_category.mgmc.variable")
            .color(NodeThemes.COLOR_NODE_VARIABLE)
            .execIn()
            .execOut()
            .input(NodePorts.NAME, "node.mgmc.port.name", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING, "")
            .input(NodePorts.VALUE, "node.mgmc.port.value", NodeDefinition.PortType.ANY, NodeThemes.COLOR_PORT_ANY)
            .output(NodePorts.VALUE, "node.mgmc.port.value", NodeDefinition.PortType.ANY, NodeThemes.COLOR_PORT_ANY)
            .register(new NodeHelper.NodeHandlerAdapter() {
                @Override
                public void execute(JsonObject node, NodeContext ctx) {
                    String name = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.NAME, ctx));
                    Object value = NodeLogicRegistry.evaluateInput(node, NodePorts.VALUE, ctx);
                    if (name != null && !name.trim().isEmpty()) {
                        GlobalVariableManager manager = MaingraphforMC.getGlobalVariableManager();
                        if (manager != null) {
                            manager.set(name.trim(), value);
                        }
                    }
                    NodeLogicRegistry.triggerExec(node, NodePorts.EXEC, ctx);
                }

                @Override
                public Object getValue(JsonObject node, String portId, NodeContext ctx) {
                    return NodeLogicRegistry.evaluateInput(node, NodePorts.VALUE, ctx);
                }
            });

        // --- 实体持久化变量 ---
        NodeHelper.setup("get_entity_variable", "node.mgmc.get_entity_variable.name")
            .category("node_category.mgmc.variable")
            .color(NodeThemes.COLOR_NODE_VARIABLE)
            .input(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .input(NodePorts.NAME, "node.mgmc.port.name", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING, "")
            .output(NodePorts.VALUE, "node.mgmc.port.value", NodeDefinition.PortType.ANY, NodeThemes.COLOR_PORT_ANY)
            .registerValue((node, portId, ctx) -> {
                Object entityObj = NodeLogicRegistry.evaluateInput(node, NodePorts.ENTITY, ctx);
                String uuid = "";
                if (entityObj instanceof net.minecraft.world.entity.Entity entity) {
                    uuid = entity.getUUID().toString();
                } else {
                    uuid = TypeConverter.toString(entityObj);
                }
                
                String name = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.NAME, ctx));
                if (uuid.isEmpty() || name == null || name.trim().isEmpty()) return null;
                
                EntityVariableManager manager = MaingraphforMC.getEntityVariableManager();
                return manager != null ? manager.get(uuid, name.trim()) : null;
            });

        NodeHelper.setup("set_entity_variable", "node.mgmc.set_entity_variable.name")
            .category("node_category.mgmc.variable")
            .color(NodeThemes.COLOR_NODE_VARIABLE)
            .execIn()
            .execOut()
            .input(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .input(NodePorts.NAME, "node.mgmc.port.name", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING, "")
            .input(NodePorts.VALUE, "node.mgmc.port.value", NodeDefinition.PortType.ANY, NodeThemes.COLOR_PORT_ANY)
            .output(NodePorts.VALUE, "node.mgmc.port.value", NodeDefinition.PortType.ANY, NodeThemes.COLOR_PORT_ANY)
            .register(new NodeHelper.NodeHandlerAdapter() {
                @Override
                public void execute(JsonObject node, NodeContext ctx) {
                    Object entityObj = NodeLogicRegistry.evaluateInput(node, NodePorts.ENTITY, ctx);
                    String uuid = "";
                    if (entityObj instanceof net.minecraft.world.entity.Entity entity) {
                        uuid = entity.getUUID().toString();
                    } else {
                        uuid = TypeConverter.toString(entityObj);
                    }
                    
                    String name = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.NAME, ctx));
                    Object value = NodeLogicRegistry.evaluateInput(node, NodePorts.VALUE, ctx);
                    
                    if (!uuid.isEmpty() && name != null && !name.trim().isEmpty()) {
                        EntityVariableManager manager = MaingraphforMC.getEntityVariableManager();
                        if (manager != null) {
                            manager.set(uuid, name.trim(), value);
                        }
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
