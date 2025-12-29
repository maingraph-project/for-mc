package ltd.opens.mg.mc.core.blueprint;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class NodeRegistry {
    private static final Map<String, NodeDefinition> REGISTRY = new HashMap<>();

    public static void register(NodeDefinition definition) {
        REGISTRY.put(definition.id(), definition);
    }

    public static NodeDefinition get(String id) {
        return REGISTRY.get(id);
    }

    public static Collection<NodeDefinition> getAll() {
        return REGISTRY.values();
    }

    static {
        // Colors from web editor
        int colorExec = 0xFFFFFFFF;
        int colorString = 0xFFDA00DA;
        int colorFloat = 0xFF36CF36;
        int colorBoolean = 0xFF920101;
        int colorObject = 0xFF00AAFF;
        int colorList = 0xFFFFCC00; // Yellow-ish for List
        int colorUUID = 0xFF55FF55; // Light Green for UUID
        int colorEnum = 0xFFFFAA00; // Orange for Enum

        // Register default nodes matching web editor
        // Events
        register(new NodeDefinition.Builder("on_mgrun", "On MGRUN")
            .category("Events")
            .color(0xFF880000)
            .addOutput("exec", NodeDefinition.PortType.EXEC, colorExec)
            .addOutput("name", NodeDefinition.PortType.STRING, colorString)
            .addOutput("parameters", NodeDefinition.PortType.LIST, colorList)
            .addOutput("trigger_uuid", NodeDefinition.PortType.UUID, colorUUID)
            .addOutput("trigger_name", NodeDefinition.PortType.STRING, colorString)
            .addOutput("trigger_x", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("trigger_y", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("trigger_z", NodeDefinition.PortType.FLOAT, colorFloat)
            .build());

        // Function
        register(new NodeDefinition.Builder("print_chat", "Print to Chat")
            .category("Function")
            .color(0xFF4488FF)
            .addInput("exec", NodeDefinition.PortType.EXEC, colorExec)
            .addInput("message", NodeDefinition.PortType.STRING, colorString, true, "Hello Chat")
            .addOutput("exec", NodeDefinition.PortType.EXEC, colorExec)
            .build());

        register(new NodeDefinition.Builder("get_list_item", "Get List Item")
            .category("Function")
            .color(0xFF44AA44)
            .addInput("list", NodeDefinition.PortType.LIST, colorList)
            .addInput("index", NodeDefinition.PortType.FLOAT, colorFloat, true, 0)
            .addOutput("value", NodeDefinition.PortType.STRING, colorString)
            .build());

        register(new NodeDefinition.Builder("get_entity_info", "Get Entity Info")
            .category("Function")
            .color(0xFF44AA44)
            .addInput("uuid", NodeDefinition.PortType.UUID, colorUUID)
            .addOutput("name", NodeDefinition.PortType.STRING, colorString)
            .addOutput("type", NodeDefinition.PortType.ENUM, colorEnum)
            .addOutput("registry_name", NodeDefinition.PortType.STRING, colorString)
            .addOutput("pos_x", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("pos_y", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("pos_z", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("health", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("max_health", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("is_living", NodeDefinition.PortType.BOOLEAN, colorBoolean)
            .addOutput("is_player", NodeDefinition.PortType.BOOLEAN, colorBoolean)
            .build());

        // Logic
        register(new NodeDefinition.Builder("branch", "Branch")
            .category("Logic")
            .color(0xFF888888)
            .addInput("exec", NodeDefinition.PortType.EXEC, colorExec)
            .addInput("condition", NodeDefinition.PortType.BOOLEAN, colorBoolean, true, true)
            .addOutput("true", NodeDefinition.PortType.EXEC, colorExec)
            .addOutput("false", NodeDefinition.PortType.EXEC, colorExec)
            .build());

        register(new NodeDefinition.Builder("cast", "Cast (Convert)")
            .category("Logic")
            .color(0xFF888888)
            .addInput("input", NodeDefinition.PortType.OBJECT, colorObject)
            .addInput("to_type", NodeDefinition.PortType.STRING, colorString, true, "STRING")
            .addOutput("output", NodeDefinition.PortType.OBJECT, colorObject)
            .build());
    }
}
