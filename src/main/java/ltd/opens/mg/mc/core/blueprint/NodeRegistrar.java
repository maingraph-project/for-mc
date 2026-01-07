package ltd.opens.mg.mc.core.blueprint;

public class NodeRegistrar {
    private static final int colorExec = 0xFFFFFFFF;
    private static final int colorString = 0xFFDA00DA;
    private static final int colorFloat = 0xFF36CF36;
    private static final int colorBoolean = 0xFF920101;
    private static final int colorObject = 0xFF00AAFF;
    private static final int colorList = 0xFFFFCC00;
    private static final int colorUUID = 0xFF55FF55;
    private static final int colorEnum = 0xFFFFAA00;

    public static void registerAll() {
        registerActions();
        registerVariables();
        registerLogic();
        registerStrings();
        registerLists();
    }

    private static void registerEvents() {
        NodeRegistry.register(new NodeDefinition.Builder("on_mgrun", "node.mgmc.on_mgrun.name")
            .category("node_category.mgmc.events.world")
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

        NodeRegistry.register(new NodeDefinition.Builder("on_player_move", "node.mgmc.on_player_move.name")
            .category("node_category.mgmc.events.player")
            .color(0xFF0088FF)
            .addOutput("exec", "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, colorExec)
            .addOutput("x", "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("y", "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("z", "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("speed", "node.mgmc.on_player_move.port.speed", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("uuid", "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, colorUUID)
            .build());

        NodeRegistry.register(new NodeDefinition.Builder("on_break_block", "node.mgmc.on_break_block.name")
            .category("node_category.mgmc.events.player")
            .color(0xFF0088FF)
            .addOutput("exec", "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, colorExec)
            .addOutput("x", "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("y", "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("z", "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("block_id", "node.mgmc.port.block_id", NodeDefinition.PortType.STRING, colorString)
            .addOutput("uuid", "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, colorUUID)
            .build());

        NodeRegistry.register(new NodeDefinition.Builder("on_place_block", "node.mgmc.on_place_block.name")
            .category("node_category.mgmc.events.player")
            .color(0xFF0088FF)
            .addOutput("exec", "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, colorExec)
            .addOutput("x", "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("y", "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("z", "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("block_id", "node.mgmc.port.block_id", NodeDefinition.PortType.STRING, colorString)
            .addOutput("uuid", "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, colorUUID)
            .build());

        NodeRegistry.register(new NodeDefinition.Builder("on_interact_block", "node.mgmc.on_interact_block.name")
            .category("node_category.mgmc.events.player")
            .color(0xFF0088FF)
            .addOutput("exec", "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, colorExec)
            .addOutput("x", "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("y", "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("z", "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("block_id", "node.mgmc.port.block_id", NodeDefinition.PortType.STRING, colorString)
            .addOutput("uuid", "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, colorUUID)
            .build());

        NodeRegistry.register(new NodeDefinition.Builder("on_player_join", "node.mgmc.on_player_join.name")
            .category("node_category.mgmc.events.player")
            .color(0xFF0088FF)
            .addOutput("exec", "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, colorExec)
            .addOutput("uuid", "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, colorUUID)
            .addOutput("trigger_name", "node.mgmc.port.name", NodeDefinition.PortType.STRING, colorString)
            .build());

        NodeRegistry.register(new NodeDefinition.Builder("on_player_death", "node.mgmc.on_player_death.name")
            .category("node_category.mgmc.events.player")
            .color(0xFF0088FF)
            .addOutput("exec", "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, colorExec)
            .addOutput("x", "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("y", "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("z", "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("uuid", "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, colorUUID)
            .build());

        NodeRegistry.register(new NodeDefinition.Builder("on_player_respawn", "node.mgmc.on_player_respawn.name")
            .category("node_category.mgmc.events.player")
            .color(0xFF0088FF)
            .addOutput("exec", "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, colorExec)
            .addOutput("x", "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("y", "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("z", "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("uuid", "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, colorUUID)
            .build());

        NodeRegistry.register(new NodeDefinition.Builder("on_player_hurt", "node.mgmc.on_player_hurt.name")
            .category("node_category.mgmc.events.player")
            .color(0xFF0088FF)
            .addOutput("exec", "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, colorExec)
            .addOutput("damage_amount", "node.mgmc.port.damage_amount", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("attacker_uuid", "node.mgmc.port.attacker_uuid", NodeDefinition.PortType.UUID, colorUUID)
            .addOutput("uuid", "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, colorUUID)
            .build());

        NodeRegistry.register(new NodeDefinition.Builder("on_use_item", "node.mgmc.on_use_item.name")
            .category("node_category.mgmc.events.player")
            .color(0xFF0088FF)
            .addOutput("exec", "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, colorExec)
            .addOutput("item_id", "node.mgmc.port.item_id", NodeDefinition.PortType.STRING, colorString)
            .addOutput("uuid", "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, colorUUID)
            .build());

        NodeRegistry.register(new NodeDefinition.Builder("on_player_attack", "node.mgmc.on_player_attack.name")
            .category("node_category.mgmc.events.player")
            .color(0xFF0088FF)
            .addOutput("exec", "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, colorExec)
            .addOutput("victim_uuid", "node.mgmc.port.victim_uuid", NodeDefinition.PortType.UUID, colorUUID)
            .addOutput("uuid", "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, colorUUID)
            .build());

        NodeRegistry.register(new NodeDefinition.Builder("on_entity_death", "node.mgmc.on_entity_death.name")
            .category("node_category.mgmc.events.entity")
            .color(0xFF0088FF)
            .addOutput("exec", "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, colorExec)
            .addOutput("x", "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("y", "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("z", "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("victim_uuid", "node.mgmc.port.victim_uuid", NodeDefinition.PortType.UUID, colorUUID)
            .addOutput("attacker_uuid", "node.mgmc.port.attacker_uuid", NodeDefinition.PortType.UUID, colorUUID)
            .build());

        NodeRegistry.register(new NodeDefinition.Builder("on_entity_hurt", "node.mgmc.on_entity_hurt.name")
            .category("node_category.mgmc.events.entity")
            .color(0xFF0088FF)
            .addOutput("exec", "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, colorExec)
            .addOutput("damage_amount", "node.mgmc.port.damage_amount", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("victim_uuid", "node.mgmc.port.victim_uuid", NodeDefinition.PortType.UUID, colorUUID)
            .addOutput("attacker_uuid", "node.mgmc.port.attacker_uuid", NodeDefinition.PortType.UUID, colorUUID)
            .build());

        NodeRegistry.register(new NodeDefinition.Builder("on_entity_spawn", "node.mgmc.on_entity_spawn.name")
            .category("node_category.mgmc.events.entity")
            .color(0xFF0088FF)
            .addOutput("exec", "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, colorExec)
            .addOutput("x", "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("y", "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("z", "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, colorFloat)
            .addOutput("uuid", "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, colorUUID)
            .build());
    }

    private static void registerActions() {
        NodeRegistry.register(new NodeDefinition.Builder("print_chat", "node.mgmc.print_chat.name")
            .category("node_category.mgmc.action.player")
            .color(0xFF4488FF)
            .addInput("exec", "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, colorExec)
            .addInput("message", "node.mgmc.port.message", NodeDefinition.PortType.STRING, colorString, true, "Hello Chat")
            .addOutput("exec", "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, colorExec)
            .build());

        NodeRegistry.register(new NodeDefinition.Builder("run_command_as_player", "node.mgmc.run_command_as_player.name")
            .category("node_category.mgmc.action.player")
            .color(0xFF4488FF)
            .addInput("exec", "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, colorExec)
            .addInput("uuid", "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, colorUUID)
            .addInput("command", "node.mgmc.run_command_as_player.port.command", NodeDefinition.PortType.STRING, colorString, true, "say hello")
            .addOutput("exec", "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, colorExec)
            .build());

        NodeRegistry.register(new NodeDefinition.Builder("play_effect", "node.mgmc.play_effect.name")
            .category("node_category.mgmc.action.world")
            .color(0xFF4488FF)
            .addInput("exec", "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, colorExec)
            .addInput("effect", "node.mgmc.play_effect.port.effect", NodeDefinition.PortType.STRING, colorString, true, "minecraft:happy_villager")
            .addInput("x", "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, colorFloat, true, 0.0)
            .addInput("y", "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, colorFloat, true, 0.0)
            .addInput("z", "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, colorFloat, true, 0.0)
            .addOutput("exec", "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, colorExec)
            .build());

        NodeRegistry.register(new NodeDefinition.Builder("explosion", "node.mgmc.explosion.name")
            .category("node_category.mgmc.action.world")
            .color(0xFFFF4444)
            .addInput("exec", "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, colorExec)
            .addInput("x", "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, colorFloat, true, 0.0)
            .addInput("y", "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, colorFloat, true, 0.0)
            .addInput("z", "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, colorFloat, true, 0.0)
            .addInput("radius", "node.mgmc.explosion.port.radius", NodeDefinition.PortType.FLOAT, colorFloat, true, 3.0)
            .addOutput("exec", "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, colorExec)
            .build());
    }

    private static void registerVariables() {
        NodeRegistry.register(new NodeDefinition.Builder("get_entity_info", "node.mgmc.get_entity_info.name")
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
            .addOutput("is_online", "node.mgmc.get_entity_info.port.is_online", NodeDefinition.PortType.BOOLEAN, colorBoolean)
            .addOutput("permission_level", "node.mgmc.get_entity_info.port.permission_level", NodeDefinition.PortType.FLOAT, colorFloat)
            .build());
    }

    private static void registerLogic() {
    }

    private static void registerStrings() {
    }

    private static void registerLists() {
        NodeRegistry.register(new NodeDefinition.Builder("get_list_item", "node.mgmc.get_list_item.name")
            .category("node_category.mgmc.variable.list")
            .color(0xFF44AA44)
            .addInput("list", "node.mgmc.get_list_item.port.list", NodeDefinition.PortType.LIST, colorList)
            .addInput("index", "node.mgmc.get_list_item.port.index", NodeDefinition.PortType.FLOAT, colorFloat, true, 0)
            .addOutput("value", "node.mgmc.get_list_item.port.value", NodeDefinition.PortType.ANY, 0xFFAAAAAA)
            .build());

        NodeRegistry.register(new NodeDefinition.Builder("list_add", "node.mgmc.list_add.name")
            .category("node_category.mgmc.variable.list")
            .color(0xFF44AA44)
            .addInput("list_in", "node.mgmc.port.list", NodeDefinition.PortType.LIST, colorList)
            .addInput("item", "node.mgmc.port.value", NodeDefinition.PortType.ANY, 0xFFAAAAAA)
            .addOutput("list_out", "node.mgmc.port.list", NodeDefinition.PortType.LIST, colorList)
            .build());

        NodeRegistry.register(new NodeDefinition.Builder("list_remove", "node.mgmc.list_remove.name")
            .category("node_category.mgmc.variable.list")
            .color(0xFF44AA44)
            .addInput("list_in", "node.mgmc.port.list", NodeDefinition.PortType.LIST, colorList)
            .addInput("index", "node.mgmc.port.index", NodeDefinition.PortType.FLOAT, colorFloat, true, 0)
            .addOutput("list_out", "node.mgmc.port.list", NodeDefinition.PortType.LIST, colorList)
            .build());

        NodeRegistry.register(new NodeDefinition.Builder("list_length", "node.mgmc.list_length.name")
            .category("node_category.mgmc.variable.list")
            .color(0xFF44AA44)
            .addInput("list", "node.mgmc.port.list", NodeDefinition.PortType.LIST, colorList)
            .addOutput("length", "node.mgmc.port.length", NodeDefinition.PortType.FLOAT, colorFloat)
            .build());

        NodeRegistry.register(new NodeDefinition.Builder("list_contains", "node.mgmc.list_contains.name")
            .category("node_category.mgmc.variable.list")
            .color(0xFF44AA44)
            .addInput("list", "node.mgmc.port.list", NodeDefinition.PortType.LIST, colorList)
            .addInput("item", "node.mgmc.port.value", NodeDefinition.PortType.ANY, 0xFFAAAAAA)
            .addOutput("result", "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, colorBoolean)
            .build());

        NodeRegistry.register(new NodeDefinition.Builder("list_set_item", "node.mgmc.list_set_item.name")
            .category("node_category.mgmc.variable.list")
            .color(0xFF44AA44)
            .addInput("list_in", "node.mgmc.port.list", NodeDefinition.PortType.LIST, colorList)
            .addInput("index", "node.mgmc.port.index", NodeDefinition.PortType.FLOAT, colorFloat, true, 0)
            .addInput("value", "node.mgmc.port.value", NodeDefinition.PortType.ANY, 0xFFAAAAAA)
            .addOutput("list_out", "node.mgmc.port.list", NodeDefinition.PortType.LIST, colorList)
            .build());

        NodeRegistry.register(new NodeDefinition.Builder("list_join", "node.mgmc.list_join.name")
            .category("node_category.mgmc.variable.list")
            .color(0xFF44AA44)
            .addInput("list", "node.mgmc.port.list", NodeDefinition.PortType.LIST, colorList)
            .addInput("delimiter", "node.mgmc.port.delimiter", NodeDefinition.PortType.STRING, colorString, true, ",")
            .addOutput("string", "node.mgmc.port.output", NodeDefinition.PortType.STRING, colorString)
            .build());
            
        NodeRegistry.register(new NodeDefinition.Builder("random_list_item", "node.mgmc.random_list_item.name")
            .category("node_category.mgmc.variable.list")
            .color(0xFF44AA44)
            .addInput("list", "node.mgmc.port.list", NodeDefinition.PortType.LIST, colorList)
            .addOutput("item", "node.mgmc.port.value", NodeDefinition.PortType.ANY, 0xFFAAAAAA)
            .build());
    }
}
