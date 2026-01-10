package ltd.opens.mg.mc.core.blueprint.nodes;

import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeHelper;
import ltd.opens.mg.mc.core.blueprint.NodePorts;
import ltd.opens.mg.mc.core.blueprint.NodeThemes;

import ltd.opens.mg.mc.core.blueprint.events.RegisterMGMCNodesEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.Arrays;

/**
 * 事件类节点注册
 * 包含节点定义及其对应的数据提取逻辑
 */
public class EventNodes {

    @SubscribeEvent
    public static void onRegister(RegisterMGMCNodesEvent event) {
        // --- 世界事件 ---
        NodeHelper.setup("on_mgrun", "node.mgmc.on_mgrun.name")
            .category("node_category.mgmc.events.world")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .execOut()
            .output(NodePorts.NAME, "node.mgmc.port.name", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.PARAMETERS, "node.mgmc.on_mgrun.port.parameters", NodeDefinition.PortType.LIST, NodeThemes.COLOR_PORT_LIST)
            .output(NodePorts.TRIGGER_UUID, "node.mgmc.on_mgrun.port.trigger_uuid", NodeDefinition.PortType.UUID, NodeThemes.COLOR_PORT_UUID)
            .output(NodePorts.TRIGGER_NAME, "node.mgmc.on_mgrun.port.trigger_name", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.TRIGGER_X, "node.mgmc.on_mgrun.port.trigger_x", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.TRIGGER_Y, "node.mgmc.on_mgrun.port.trigger_y", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.TRIGGER_Z, "node.mgmc.on_mgrun.port.trigger_z", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .registerValue((node, portId, ctx) -> switch (portId) {
                case NodePorts.NAME -> ctx.eventName;
                case NodePorts.PARAMETERS -> Arrays.asList(ctx.args);
                case NodePorts.TRIGGER_UUID -> ctx.triggerUuid != null ? ctx.triggerUuid : "";
                case NodePorts.TRIGGER_NAME -> ctx.triggerName != null ? ctx.triggerName : "";
                case NodePorts.TRIGGER_X -> ctx.triggerX;
                case NodePorts.TRIGGER_Y -> ctx.triggerY;
                case NodePorts.TRIGGER_Z -> ctx.triggerZ;
                default -> null;
            });

        // --- 玩家事件 ---
        NodeHelper.setup("on_player_move", "node.mgmc.on_player_move.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .execOut()
            .output(NodePorts.X, "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.Y, "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.Z, "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.SPEED, "node.mgmc.on_player_move.port.speed", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.UUID, "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, NodeThemes.COLOR_PORT_UUID)
            .registerValue((node, portId, ctx) -> switch (portId) {
                case NodePorts.X -> ctx.triggerX;
                case NodePorts.Y -> ctx.triggerY;
                case NodePorts.Z -> ctx.triggerZ;
                case NodePorts.SPEED -> ctx.triggerSpeed;
                case NodePorts.UUID -> ctx.triggerUuid;
                default -> null;
            });

        NodeHelper.setup("on_break_block", "node.mgmc.on_break_block.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .execOut()
            .output(NodePorts.X, "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.Y, "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.Z, "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.BLOCK_ID, "node.mgmc.port.block_id", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.UUID, "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, NodeThemes.COLOR_PORT_UUID)
            .registerValue((node, portId, ctx) -> switch (portId) {
                case NodePorts.X -> ctx.triggerX;
                case NodePorts.Y -> ctx.triggerY;
                case NodePorts.Z -> ctx.triggerZ;
                case NodePorts.BLOCK_ID -> ctx.triggerBlockId;
                case NodePorts.UUID -> ctx.triggerUuid;
                default -> null;
            });

        NodeHelper.setup("on_place_block", "node.mgmc.on_place_block.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .execOut()
            .output(NodePorts.X, "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.Y, "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.Z, "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.BLOCK_ID, "node.mgmc.port.block_id", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.UUID, "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, NodeThemes.COLOR_PORT_UUID)
            .registerValue((node, portId, ctx) -> switch (portId) {
                case NodePorts.X -> ctx.triggerX;
                case NodePorts.Y -> ctx.triggerY;
                case NodePorts.Z -> ctx.triggerZ;
                case NodePorts.BLOCK_ID -> ctx.triggerBlockId;
                case NodePorts.UUID -> ctx.triggerUuid;
                default -> null;
            });

        NodeHelper.setup("on_interact_block", "node.mgmc.on_interact_block.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .execOut()
            .output(NodePorts.X, "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.Y, "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.Z, "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.BLOCK_ID, "node.mgmc.port.block_id", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.UUID, "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, NodeThemes.COLOR_PORT_UUID)
            .registerValue((node, portId, ctx) -> switch (portId) {
                case NodePorts.X -> ctx.triggerX;
                case NodePorts.Y -> ctx.triggerY;
                case NodePorts.Z -> ctx.triggerZ;
                case NodePorts.BLOCK_ID -> ctx.triggerBlockId;
                case NodePorts.UUID -> ctx.triggerUuid;
                default -> null;
            });

        NodeHelper.setup("on_player_join", "node.mgmc.on_player_join.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .execOut()
            .output(NodePorts.UUID, "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, NodeThemes.COLOR_PORT_UUID)
            .output(NodePorts.NAME, "node.mgmc.port.name", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .registerValue((node, portId, ctx) -> switch (portId) {
                case NodePorts.UUID -> ctx.triggerUuid;
                case NodePorts.NAME -> ctx.triggerName;
                default -> null;
            });

        NodeHelper.setup("on_player_death", "node.mgmc.on_player_death.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .execOut()
            .output(NodePorts.X, "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.Y, "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.Z, "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.UUID, "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, NodeThemes.COLOR_PORT_UUID)
            .registerValue((node, portId, ctx) -> switch (portId) {
                case NodePorts.X -> ctx.triggerX;
                case NodePorts.Y -> ctx.triggerY;
                case NodePorts.Z -> ctx.triggerZ;
                case NodePorts.UUID -> ctx.triggerUuid;
                default -> null;
            });

        NodeHelper.setup("on_player_respawn", "node.mgmc.on_player_respawn.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .execOut()
            .output(NodePorts.X, "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.Y, "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.Z, "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.UUID, "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, NodeThemes.COLOR_PORT_UUID)
            .registerValue((node, portId, ctx) -> switch (portId) {
                case NodePorts.X -> ctx.triggerX;
                case NodePorts.Y -> ctx.triggerY;
                case NodePorts.Z -> ctx.triggerZ;
                case NodePorts.UUID -> ctx.triggerUuid;
                default -> null;
            });

        NodeHelper.setup("on_player_hurt", "node.mgmc.on_player_hurt.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .execOut()
            .output(NodePorts.DAMAGE_AMOUNT, "node.mgmc.port.damage_amount", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.ATTACKER_UUID, "node.mgmc.port.attacker_uuid", NodeDefinition.PortType.UUID, NodeThemes.COLOR_PORT_UUID)
            .output(NodePorts.UUID, "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, NodeThemes.COLOR_PORT_UUID)
            .registerValue((node, portId, ctx) -> switch (portId) {
                case NodePorts.DAMAGE_AMOUNT -> ctx.triggerValue;
                case NodePorts.ATTACKER_UUID -> ctx.triggerExtraUuid;
                case NodePorts.UUID -> ctx.triggerUuid;
                default -> null;
            });

        NodeHelper.setup("on_use_item", "node.mgmc.on_use_item.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .execOut()
            .output(NodePorts.ITEM_ID, "node.mgmc.port.item_id", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.UUID, "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, NodeThemes.COLOR_PORT_UUID)
            .registerValue((node, portId, ctx) -> switch (portId) {
                case NodePorts.ITEM_ID -> ctx.triggerItemId;
                case NodePorts.UUID -> ctx.triggerUuid;
                default -> null;
            });

        NodeHelper.setup("on_player_attack", "node.mgmc.on_player_attack.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .execOut()
            .output(NodePorts.VICTIM_UUID, "node.mgmc.port.victim_uuid", NodeDefinition.PortType.UUID, NodeThemes.COLOR_PORT_UUID)
            .output(NodePorts.UUID, "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, NodeThemes.COLOR_PORT_UUID)
            .registerValue((node, portId, ctx) -> switch (portId) {
                case NodePorts.VICTIM_UUID -> ctx.triggerExtraUuid;
                case NodePorts.UUID -> ctx.triggerUuid;
                default -> null;
            });

        // --- 实体事件 ---
        NodeHelper.setup("on_entity_death", "node.mgmc.on_entity_death.name")
            .category("node_category.mgmc.events.entity")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .execOut()
            .output(NodePorts.X, "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.Y, "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.Z, "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.VICTIM_UUID, "node.mgmc.port.victim_uuid", NodeDefinition.PortType.UUID, NodeThemes.COLOR_PORT_UUID)
            .output(NodePorts.ATTACKER_UUID, "node.mgmc.port.attacker_uuid", NodeDefinition.PortType.UUID, NodeThemes.COLOR_PORT_UUID)
            .registerValue((node, portId, ctx) -> switch (portId) {
                case NodePorts.X -> ctx.triggerX;
                case NodePorts.Y -> ctx.triggerY;
                case NodePorts.Z -> ctx.triggerZ;
                case NodePorts.VICTIM_UUID -> ctx.triggerUuid;
                case NodePorts.ATTACKER_UUID -> ctx.triggerExtraUuid;
                default -> null;
            });

        NodeHelper.setup("on_entity_hurt", "node.mgmc.on_entity_hurt.name")
            .category("node_category.mgmc.events.entity")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .execOut()
            .output(NodePorts.DAMAGE_AMOUNT, "node.mgmc.port.damage_amount", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.VICTIM_UUID, "node.mgmc.port.victim_uuid", NodeDefinition.PortType.UUID, NodeThemes.COLOR_PORT_UUID)
            .output(NodePorts.ATTACKER_UUID, "node.mgmc.port.attacker_uuid", NodeDefinition.PortType.UUID, NodeThemes.COLOR_PORT_UUID)
            .registerValue((node, portId, ctx) -> switch (portId) {
                case NodePorts.DAMAGE_AMOUNT -> ctx.triggerValue;
                case NodePorts.VICTIM_UUID -> ctx.triggerUuid;
                case NodePorts.ATTACKER_UUID -> ctx.triggerExtraUuid;
                default -> null;
            });

        NodeHelper.setup("on_entity_spawn", "node.mgmc.on_entity_spawn.name")
            .category("node_category.mgmc.events.entity")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .execOut()
            .output(NodePorts.X, "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.Y, "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.Z, "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.UUID, "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, NodeThemes.COLOR_PORT_UUID)
            .registerValue((node, portId, ctx) -> switch (portId) {
                case NodePorts.X -> ctx.triggerX;
                case NodePorts.Y -> ctx.triggerY;
                case NodePorts.Z -> ctx.triggerZ;
                case NodePorts.UUID -> ctx.triggerUuid;
                default -> null;
            });
    }
}
