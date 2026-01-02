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
        register(new NodeDefinition.Builder("on_mgrun", "node.mgmc.on_mgrun.name")
            .category("node_category.mgmc.events")
            .color(0xFF880000)
            .addOutput("exec", "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, colorExec)
            .addOutput("name", "node.mgmc.port.name", NodeDefinition.PortType.STRING, colorString)
            .addOutput("parameters", "node.mgmc.on_mgrun.port.parameters", NodeDefinition.PortType.LIST, colorList)
            .addOutput("trigger_uuid", "node.mgmc.on_mgrun.port.trigger_uuid", NodeDefinition.PortType.UUID, colorUUID)
            .addOutput("trigger_name", "node.mgmc.on_mgrun.port.trigger_name", NodeDefinition.PortType.STRING, colorString)
            .addOutput("trigger_x", "node.mgmc.on_mgrun.port.trigger_x", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("trigger_y", "node.mgmc.on_mgrun.port.trigger_y", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("trigger_z", "node.mgmc.on_mgrun.port.trigger_z", NodeDefinition.PortType.FLOAT, colorFloat)
            .build());

        register(new NodeDefinition.Builder("on_player_move", "node.mgmc.on_player_move.name")
            .category("node_category.mgmc.events")
            .color(0xFF0088FF)
            .addOutput("exec", "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, colorExec)
            .addOutput("x", "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("y", "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("z", "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("speed", "node.mgmc.on_player_move.port.speed", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("uuid", "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, colorUUID)
            .build());

        // Action -> Player
        register(new NodeDefinition.Builder("print_chat", "node.mgmc.print_chat.name")
            .category("node_category.mgmc.action.player")
            .color(0xFF4488FF)
            .addInput("exec", "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, colorExec)
            .addInput("message", "node.mgmc.port.message", NodeDefinition.PortType.STRING, colorString, true, "Hello Chat")
            .addOutput("exec", "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, colorExec)
            .build());

        register(new NodeDefinition.Builder("run_command_as_player", "node.mgmc.run_command_as_player.name")
            .category("node_category.mgmc.action.player")
            .color(0xFF4488FF)
            .addInput("exec", "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, colorExec)
            .addInput("uuid", "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, colorUUID)
            .addInput("command", "node.mgmc.run_command_as_player.port.command", NodeDefinition.PortType.STRING, colorString, true, "say hello")
            .addOutput("exec", "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, colorExec)
            .build());

        // Action -> World
        register(new NodeDefinition.Builder("play_effect", "node.mgmc.play_effect.name")
            .category("node_category.mgmc.action.world")
            .color(0xFF4488FF)
            .addInput("exec", "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, colorExec)
            .addInput("effect", "node.mgmc.play_effect.port.effect", NodeDefinition.PortType.STRING, colorString, true, "minecraft:happy_villager")
            .addInput("x", "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, colorFloat, true, 0.0)
            .addInput("y", "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, colorFloat, true, 0.0)
            .addInput("z", "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, colorFloat, true, 0.0)
            .addOutput("exec", "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, colorExec)
            .build());

        register(new NodeDefinition.Builder("explosion", "node.mgmc.explosion.name")
            .category("node_category.mgmc.action.world")
            .color(0xFFFF4444)
            .addInput("exec", "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, colorExec)
            .addInput("x", "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, colorFloat, true, 0.0)
            .addInput("y", "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, colorFloat, true, 0.0)
            .addInput("z", "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, colorFloat, true, 0.0)
            .addInput("radius", "node.mgmc.explosion.port.radius", NodeDefinition.PortType.FLOAT, colorFloat, true, 3.0)
            .addOutput("exec", "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, colorExec)
            .build());

        // Variable -> Entity
        register(new NodeDefinition.Builder("get_entity_info", "node.mgmc.get_entity_info.name")
            .category("node_category.mgmc.variable.entity")
            .color(0xFF44AA44)
            .addInput("uuid", "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, colorUUID)
            .addOutput("name", "node.mgmc.port.name", NodeDefinition.PortType.STRING, colorString)
            .addOutput("type", "node.mgmc.get_entity_info.port.type", NodeDefinition.PortType.ENUM, colorEnum)
            .addOutput("registry_name", "node.mgmc.get_entity_info.port.registry_name", NodeDefinition.PortType.STRING, colorString)
            .addOutput("pos_x", "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("pos_y", "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("pos_z", "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("health", "node.mgmc.get_entity_info.port.health", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("max_health", "node.mgmc.get_entity_info.port.max_health", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("is_living", "node.mgmc.get_entity_info.port.is_living", NodeDefinition.PortType.BOOLEAN, colorBoolean)
            .addOutput("is_player", "node.mgmc.get_entity_info.port.is_player", NodeDefinition.PortType.BOOLEAN, colorBoolean)
            .build());

        // Variable -> List
        register(new NodeDefinition.Builder("get_list_item", "node.mgmc.get_list_item.name")
            .category("node_category.mgmc.variable.list")
            .color(0xFF44AA44)
            .addInput("list", "node.mgmc.get_list_item.port.list", NodeDefinition.PortType.LIST, colorList)
            .addInput("index", "node.mgmc.get_list_item.port.index", NodeDefinition.PortType.FLOAT, colorFloat, true, 0)
            .addOutput("value", "node.mgmc.get_list_item.port.value", NodeDefinition.PortType.STRING, colorString)
            .build());

        register(new NodeDefinition.Builder("list_add", "node.mgmc.list_add.name")
            .category("node_category.mgmc.variable.list")
            .color(0xFF44AA44)
            .addInput("list_in", "node.mgmc.port.list", NodeDefinition.PortType.LIST, colorList)
            .addInput("item", "node.mgmc.port.value", NodeDefinition.PortType.ANY, 0xFFAAAAAA)
            .addOutput("list_out", "node.mgmc.port.list", NodeDefinition.PortType.LIST, colorList)
            .build());

        register(new NodeDefinition.Builder("list_remove", "node.mgmc.list_remove.name")
            .category("node_category.mgmc.variable.list")
            .color(0xFF44AA44)
            .addInput("list_in", "node.mgmc.port.list", NodeDefinition.PortType.LIST, colorList)
            .addInput("index", "node.mgmc.port.index", NodeDefinition.PortType.FLOAT, colorFloat, true, 0)
            .addOutput("list_out", "node.mgmc.port.list", NodeDefinition.PortType.LIST, colorList)
            .build());

        register(new NodeDefinition.Builder("list_length", "node.mgmc.list_length.name")
            .category("node_category.mgmc.variable.list")
            .color(0xFF44AA44)
            .addInput("list", "node.mgmc.port.list", NodeDefinition.PortType.LIST, colorList)
            .addOutput("length", "node.mgmc.port.length", NodeDefinition.PortType.FLOAT, colorFloat)
            .build());

        register(new NodeDefinition.Builder("list_contains", "node.mgmc.list_contains.name")
            .category("node_category.mgmc.variable.list")
            .color(0xFF44AA44)
            .addInput("list", "node.mgmc.port.list", NodeDefinition.PortType.LIST, colorList)
            .addInput("item", "node.mgmc.port.value", NodeDefinition.PortType.ANY, 0xFFAAAAAA)
            .addOutput("result", "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, colorBoolean)
            .build());

        register(new NodeDefinition.Builder("list_set_item", "node.mgmc.list_set_item.name")
            .category("node_category.mgmc.variable.list")
            .color(0xFF44AA44)
            .addInput("list_in", "node.mgmc.port.list", NodeDefinition.PortType.LIST, colorList)
            .addInput("index", "node.mgmc.port.index", NodeDefinition.PortType.FLOAT, colorFloat, true, 0)
            .addInput("value", "node.mgmc.port.value", NodeDefinition.PortType.ANY, 0xFFAAAAAA)
            .addOutput("list_out", "node.mgmc.port.list", NodeDefinition.PortType.LIST, colorList)
            .build());

        register(new NodeDefinition.Builder("list_join", "node.mgmc.list_join.name")
            .category("node_category.mgmc.variable.list")
            .color(0xFF44AA44)
            .addInput("list", "node.mgmc.port.list", NodeDefinition.PortType.LIST, colorList)
            .addInput("delimiter", "node.mgmc.port.delimiter", NodeDefinition.PortType.STRING, colorString, true, ",")
            .addOutput("string", "node.mgmc.port.output", NodeDefinition.PortType.STRING, colorString)
            .build());

        register(new NodeDefinition.Builder("string_split", "node.mgmc.string_split.name")
            .category("node_category.mgmc.variable.list")
            .color(0xFF44AA44)
            .addInput("string", "node.mgmc.port.input", NodeDefinition.PortType.STRING, colorString)
            .addInput("delimiter", "node.mgmc.port.delimiter", NodeDefinition.PortType.STRING, colorString, true, ",")
            .addOutput("list", "node.mgmc.port.list", NodeDefinition.PortType.LIST, colorList)
            .build());

        // Logic -> Control
        register(new NodeDefinition.Builder("branch", "node.mgmc.branch.name")
            .category("node_category.mgmc.logic.control")
            .color(0xFF888888)
            .addInput("exec", "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, colorExec)
            .addInput("condition", "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, colorBoolean, true, true)
            .addOutput("true", "node.mgmc.port.true", NodeDefinition.PortType.EXEC, colorExec)
            .addOutput("false", "node.mgmc.port.false", NodeDefinition.PortType.EXEC, colorExec)
            .build());

        register(new NodeDefinition.Builder("switch", "node.mgmc.switch.name")
            .category("node_category.mgmc.logic.control")
            .color(0xFF888888)
            .addInput("exec", "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, colorExec)
            .addInput("control", "node.mgmc.switch.port.control", NodeDefinition.PortType.ANY, 0xFFAAAAAA)
            .addOutput("default", "node.mgmc.switch.port.default", NodeDefinition.PortType.EXEC, colorExec)
            .build());

        register(new NodeDefinition.Builder("for_loop", "node.mgmc.for_loop.name")
            .category("node_category.mgmc.logic.control")
            .color(0xFF888888)
            .addInput("exec", "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, colorExec)
            .addInput("start", "node.mgmc.for_loop.port.start", NodeDefinition.PortType.FLOAT, colorFloat, true, 0)
            .addInput("end", "node.mgmc.for_loop.port.end", NodeDefinition.PortType.FLOAT, colorFloat, true, 10)
            .addInput("break", "node.mgmc.for_loop.port.break", NodeDefinition.PortType.EXEC, colorExec)
            .addOutput("loop_body", "node.mgmc.for_loop.port.loop_body", NodeDefinition.PortType.EXEC, colorExec)
            .addOutput("completed", "node.mgmc.for_loop.port.completed", NodeDefinition.PortType.EXEC, colorExec)
            .addOutput("index", "node.mgmc.for_loop.port.index", NodeDefinition.PortType.FLOAT, colorFloat)
            .build());

        register(new NodeDefinition.Builder("break_loop", "node.mgmc.break_loop.name")
            .category("node_category.mgmc.logic.control")
            .color(0xFF888888)
            .addInput("exec", "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, colorExec)
            .addOutput("break", "node.mgmc.break_loop.port.break", NodeDefinition.PortType.EXEC, colorExec)
            .build());

        // Logic -> Math/Convert
        register(new NodeDefinition.Builder("cast", "node.mgmc.cast.name")
            .category("node_category.mgmc.logic.math")
            .color(0xFF888888)
            .addInput("input", "node.mgmc.cast.port.input", NodeDefinition.PortType.ANY, 0xFFAAAAAA)
            .addInput("to_type", "node.mgmc.cast.port.to_type", NodeDefinition.PortType.STRING, colorString, true, "STRING", 
                new String[]{"STRING", "FLOAT", "BOOLEAN", "UUID", "INT", "LIST"})
            .addOutput("output", "node.mgmc.cast.port.output", NodeDefinition.PortType.ANY, 0xFFAAAAAA)
            .build());

        // Variable -> String
        register(new NodeDefinition.Builder("string", "node.mgmc.string.name")
            .category("node_category.mgmc.variable.string")
            .color(colorString)
            .addInput("value", "node.mgmc.port.value", NodeDefinition.PortType.STRING, colorString, true, "")
            .addOutput("output", "node.mgmc.port.output", NodeDefinition.PortType.STRING, colorString)
            .build());

        register(new NodeDefinition.Builder("string_concat", "node.mgmc.string_concat.name")
            .category("node_category.mgmc.variable.string")
            .color(colorString)
            .addInput("a", "node.mgmc.string_concat.port.a", NodeDefinition.PortType.STRING, colorString)
            .addInput("b", "node.mgmc.string_concat.port.b", NodeDefinition.PortType.STRING, colorString)
            .addOutput("output", "node.mgmc.port.output", NodeDefinition.PortType.STRING, colorString)
            .build());

        register(new NodeDefinition.Builder("string_length", "node.mgmc.string_length.name")
            .category("node_category.mgmc.variable.string")
            .color(colorString)
            .addInput("string", "node.mgmc.port.input", NodeDefinition.PortType.STRING, colorString)
            .addOutput("length", "node.mgmc.port.length", NodeDefinition.PortType.FLOAT, colorFloat)
            .build());

        register(new NodeDefinition.Builder("string_contains", "node.mgmc.string_contains.name")
            .category("node_category.mgmc.variable.string")
            .color(colorString)
            .addInput("string", "node.mgmc.port.input", NodeDefinition.PortType.STRING, colorString)
            .addInput("substring", "node.mgmc.string_contains.port.substring", NodeDefinition.PortType.STRING, colorString)
            .addOutput("result", "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, colorBoolean)
            .build());

        register(new NodeDefinition.Builder("string_replace", "node.mgmc.string_replace.name")
            .category("node_category.mgmc.variable.string")
            .color(colorString)
            .addInput("string", "node.mgmc.port.input", NodeDefinition.PortType.STRING, colorString)
            .addInput("old", "node.mgmc.string_replace.port.old", NodeDefinition.PortType.STRING, colorString)
            .addInput("new", "node.mgmc.string_replace.port.new", NodeDefinition.PortType.STRING, colorString)
            .addOutput("output", "node.mgmc.port.output", NodeDefinition.PortType.STRING, colorString)
            .build());

        register(new NodeDefinition.Builder("string_substring", "node.mgmc.string_substring.name")
            .category("node_category.mgmc.variable.string")
            .color(colorString)
            .addInput("string", "node.mgmc.port.input", NodeDefinition.PortType.STRING, colorString)
            .addInput("start", "node.mgmc.string_substring.port.start", NodeDefinition.PortType.FLOAT, colorFloat, true, 0)
            .addInput("end", "node.mgmc.string_substring.port.end", NodeDefinition.PortType.FLOAT, colorFloat, true, 5)
            .addOutput("output", "node.mgmc.port.output", NodeDefinition.PortType.STRING, colorString)
            .build());

        register(new NodeDefinition.Builder("string_case", "node.mgmc.string_case.name")
            .category("node_category.mgmc.variable.string")
            .color(colorString)
            .addInput("string", "node.mgmc.port.input", NodeDefinition.PortType.STRING, colorString)
            .addInput("mode", "node.mgmc.string_case.port.mode", NodeDefinition.PortType.STRING, colorString, true, "UPPER",
                new String[]{"UPPER", "LOWER", "TRIM"})
            .addOutput("output", "node.mgmc.port.output", NodeDefinition.PortType.STRING, colorString)
            .build());

        // Variable -> Float
        register(new NodeDefinition.Builder("float", "node.mgmc.float.name")
            .category("node_category.mgmc.variable.float")
            .color(colorFloat)
            .addInput("value", "node.mgmc.port.value", NodeDefinition.PortType.FLOAT, colorFloat, true, 0.0)
            .addOutput("output", "node.mgmc.port.output", NodeDefinition.PortType.FLOAT, colorFloat)
            .build());

        // Variable -> Boolean
        register(new NodeDefinition.Builder("boolean", "node.mgmc.boolean.name")
            .category("node_category.mgmc.variable.boolean")
            .color(colorBoolean)
            .addInput("value", "node.mgmc.port.value", NodeDefinition.PortType.BOOLEAN, colorBoolean, true, true)
            .addOutput("output", "node.mgmc.port.output", NodeDefinition.PortType.BOOLEAN, colorBoolean)
            .build());

        // Variable -> General
        register(new NodeDefinition.Builder("get_variable", "node.mgmc.get_variable.name")
            .category("node_category.mgmc.variable")
            .color(0xFF44AA44)
            .addInput("name", "node.mgmc.port.name", NodeDefinition.PortType.STRING, colorString, true, "my_var")
            .addOutput("value", "node.mgmc.port.value", NodeDefinition.PortType.ANY, 0xFFAAAAAA)
            .build());

        register(new NodeDefinition.Builder("set_variable", "node.mgmc.set_variable.name")
            .category("node_category.mgmc.variable")
            .color(0xFF44AA44)
            .addInput("exec", "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, colorExec)
            .addInput("name", "node.mgmc.port.name", NodeDefinition.PortType.STRING, colorString, true, "my_var")
            .addInput("value", "node.mgmc.port.value", NodeDefinition.PortType.ANY, 0xFFAAAAAA)
            .addOutput("exec", "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, colorExec)
            .addOutput("value", "node.mgmc.port.value", NodeDefinition.PortType.ANY, 0xFFAAAAAA)
            .build());
    }
}
