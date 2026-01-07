package ltd.opens.mg.mc.core.blueprint;

public class NodeRegistrar {
    private static final int colorExec = 0xFFFFFFFF;
    private static final int colorString = 0xFFDA00DA;
    private static final int colorFloat = 0xFF55FF55;
    private static final int colorBoolean = 0xFF920101;
    private static final int colorObject = 0xFF00AAFF;
    private static final int colorList = 0xFFFFCC00;
    private static final int colorUUID = 0xFF55FF55;
    private static final int colorEnum = 0xFFFFAA00;

    /**
     * 旧版节点注册入口。
     * 现已逐步迁移至 NodeInitializer 模块化注册。
     */
    public static void registerAll() {
        // registerEvents(); // 事件节点暂未迁移
        registerVariables();
        registerStrings();
    }

    private static void registerEvents() {
        // 暂保留事件节点注册，直到 EventNodes.java 完成完整逻辑重构
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

    private static void registerVariables() {
        // 已迁移至各 Node 类中注册
    }

    private static void registerLogic() {
        // 已迁移至 LogicNodes.java
    }

    private static void registerStrings() {
        // 已迁移至 StringNodes.java
    }
}
