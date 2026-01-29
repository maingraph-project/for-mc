package ltd.opens.mg.mc.core.blueprint.nodes;

import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeHelper;
import ltd.opens.mg.mc.core.blueprint.NodePorts;
import ltd.opens.mg.mc.core.blueprint.NodeThemes;
import ltd.opens.mg.mc.core.blueprint.events.MGMCEventType;
import ltd.opens.mg.mc.core.blueprint.routing.BlueprintRouter;
import net.minecraft.world.entity.player.Player;
import ltd.opens.mg.mc.core.blueprint.data.XYZ;

import java.util.*;

/**
 * 事件类节点注册
 * 包含节点定义及其对应的数据提取逻辑
 */
public class EventNodes {

    public static void register() {
        // --- 世界事件 ---
        NodeHelper.setup("on_mgrun", "node.mgmc.on_mgrun.name")
            .category("node_category.mgmc.events.world")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/world/on_mgrun")
            .execOut()
            .output(NodePorts.NAME, "node.mgmc.port.name", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.PARAMETERS, "node.mgmc.on_mgrun.port.parameters", NodeDefinition.PortType.LIST, NodeThemes.COLOR_PORT_LIST)
            .output(NodePorts.TRIGGER_ENTITY, "node.mgmc.port.trigger_entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .output(NodePorts.TRIGGER_NAME, "node.mgmc.port.trigger_name", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .registerValue((node, portId, ctx) -> switch (portId) {
                case NodePorts.NAME -> ctx.eventName;
                case NodePorts.PARAMETERS -> ctx.args != null ? Arrays.asList(ctx.args) : Collections.emptyList();
                case NodePorts.TRIGGER_ENTITY -> ctx.triggerEntity;
                case NodePorts.TRIGGER_NAME -> ctx.triggerName != null ? ctx.triggerName : "";
                case NodePorts.XYZ -> ctx.triggerXYZ;
                default -> null;
            });

        // --- 玩家事件 ---
        NodeHelper.setup("on_break_block", "node.mgmc.on_break_block.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/player/on_break_block")
            .execOut()
            .output(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.BLOCK_ID, "node.mgmc.port.block_id", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(MGMCEventType.BLOCK_BREAK, (e, b) -> {
                if (e.getPlayer() != null) {
                    b.triggerUuid(e.getPlayer().getUUID().toString())
                     .triggerName(e.getPlayer().getName().getString())
                     .triggerEntity(e.getPlayer());
                }
                if (e.getPos() != null) {
                    b.triggerX(e.getPos().getX()).triggerY(e.getPos().getY()).triggerZ(e.getPos().getZ());
                }
                if (e.getBlockState() != null) {
                    b.triggerBlockId(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(e.getBlockState().getBlock()).toString());
                }
            }, e -> e.getBlockState() != null ? net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(e.getBlockState().getBlock()).toString() : "",
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.XYZ -> ctx.triggerXYZ;
                case NodePorts.BLOCK_ID -> ctx.triggerBlockId;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                default -> null;
            });

        NodeHelper.setup("on_place_block", "node.mgmc.on_place_block.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/player/on_place_block")
            .execOut()
            .output(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.BLOCK_ID, "node.mgmc.port.block_id", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(MGMCEventType.BLOCK_PLACE, (e, b) -> {
                if (e.getPlayer() != null) {
                    b.triggerUuid(e.getPlayer().getUUID().toString())
                     .triggerName(e.getPlayer().getName().getString())
                     .triggerEntity(e.getPlayer());
                }
                if (e.getPos() != null) {
                    b.triggerX(e.getPos().getX()).triggerY(e.getPos().getY()).triggerZ(e.getPos().getZ());
                }
                if (e.getBlockState() != null) {
                    b.triggerBlockId(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(e.getBlockState().getBlock()).toString());
                }
            }, e -> e.getBlockState() != null ? net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(e.getBlockState().getBlock()).toString() : "",
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.XYZ -> ctx.triggerXYZ;
                case NodePorts.BLOCK_ID -> ctx.triggerBlockId;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                default -> null;
            });

        NodeHelper.setup("on_interact_block", "node.mgmc.on_interact_block.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/player/on_interact_block")
            .execOut()
            .output(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.BLOCK_ID, "node.mgmc.port.block_id", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(MGMCEventType.BLOCK_INTERACT, (e, b) -> {
                if (e.getPlayer() != null) {
                    b.triggerUuid(e.getPlayer().getUUID().toString())
                     .triggerName(e.getPlayer().getName().getString())
                     .triggerEntity(e.getPlayer());
                }
                if (e.getPos() != null) {
                    b.triggerX(e.getPos().getX()).triggerY(e.getPos().getY()).triggerZ(e.getPos().getZ());
                }
                if (e.getBlockState() != null) {
                    b.triggerBlockId(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(e.getBlockState().getBlock()).toString());
                }
            }, e -> e.getBlockState() != null ? net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(e.getBlockState().getBlock()).toString() : "",
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.XYZ -> ctx.triggerXYZ;
                case NodePorts.BLOCK_ID -> ctx.triggerBlockId;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                default -> null;
            });

        NodeHelper.setup("on_player_join", "node.mgmc.on_player_join.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/player/on_player_join")
            .execOut()
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .output(NodePorts.NAME, "node.mgmc.port.name", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .registerEvent(MGMCEventType.PLAYER_JOIN, (e, b) -> {
                if (e.getEntity() != null) {
                    b.triggerUuid(e.getEntity().getUUID().toString())
                     .triggerName(e.getEntity().getName().getString())
                     .triggerEntity(e.getEntity());
                }
            }, e -> BlueprintRouter.PLAYERS_ID,
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.ENTITY -> ctx.triggerEntity;
                case NodePorts.NAME -> ctx.triggerName;
                default -> null;
            });

        NodeHelper.setup("on_player_death", "node.mgmc.on_player_death.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/player/on_player_death")
            .execOut()
            .output(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .output(NodePorts.ATTACKER_ENTITY, "node.mgmc.port.attacker_entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(MGMCEventType.PLAYER_DEATH, (e, b) -> {
                if (e.getEntity() != null) {
                    b.triggerUuid(e.getEntity().getUUID().toString())
                     .triggerName(e.getEntity().getName().getString())
                     .triggerEntity(e.getEntity())
                     .triggerX(e.getEntity().getX()).triggerY(e.getEntity().getY()).triggerZ(e.getEntity().getZ());
                }
                if (e.getDamageSource() != null) {
                    b.triggerExtraUuid(e.getDamageSource().getEntity() != null ? e.getDamageSource().getEntity().getUUID().toString() : "")
                     .triggerExtraEntity(e.getDamageSource().getEntity());
                }
            }, e -> e.getEntity() instanceof Player ? BlueprintRouter.PLAYERS_ID : null,
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.XYZ -> ctx.triggerXYZ;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                case NodePorts.ATTACKER_ENTITY -> ctx.triggerExtraEntity;
                default -> null;
            });

        NodeHelper.setup("on_player_respawn", "node.mgmc.on_player_respawn.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/player/on_player_respawn")
            .execOut()
            .output(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(MGMCEventType.PLAYER_RESPAWN, (e, b) -> {
                if (e.getPlayer() != null) {
                    b.triggerUuid(e.getPlayer().getUUID().toString())
                     .triggerName(e.getPlayer().getName().getString())
                     .triggerEntity(e.getPlayer())
                     .triggerX(e.getPlayer().getX()).triggerY(e.getPlayer().getY()).triggerZ(e.getPlayer().getZ());
                }
            }, e -> BlueprintRouter.PLAYERS_ID,
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.XYZ -> ctx.triggerXYZ;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                default -> null;
            });

        NodeHelper.setup("on_player_hurt", "node.mgmc.on_player_hurt.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/player/on_player_hurt")
            .execOut()
            .output(NodePorts.DAMAGE_AMOUNT, "node.mgmc.port.damage_amount", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.ATTACKER_ENTITY, "node.mgmc.port.attacker_entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(MGMCEventType.PLAYER_HURT, (e, b) -> {
                if (e.getEntity() != null) {
                    b.triggerUuid(e.getEntity().getUUID().toString())
                     .triggerName(e.getEntity().getName().getString())
                     .triggerEntity(e.getEntity())
                     .triggerValue(e.getAmount());
                }
                if (e.getDamageSource() != null) {
                    b.triggerExtraUuid(e.getDamageSource().getEntity() != null ? e.getDamageSource().getEntity().getUUID().toString() : "")
                     .triggerExtraEntity(e.getDamageSource().getEntity());
                }
            }, e -> e.getEntity() instanceof Player ? BlueprintRouter.PLAYERS_ID : null,
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.DAMAGE_AMOUNT -> ctx.triggerValue;
                case NodePorts.ATTACKER_ENTITY -> ctx.triggerExtraEntity;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                default -> null;
            });

        NodeHelper.setup("on_use_item", "node.mgmc.on_use_item.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/player/on_use_item")
            .execOut()
            .output(NodePorts.ITEM_ID, "node.mgmc.port.item_id", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(MGMCEventType.ITEM_USE, (e, b) -> {
                if (e.getPlayer() != null) {
                    b.triggerUuid(e.getPlayer().getUUID().toString())
                     .triggerName(e.getPlayer().getName().getString())
                     .triggerEntity(e.getPlayer());
                }
                if (e.getItem() != null) {
                    b.triggerItemId(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(e.getItem().getItem()).toString());
                }
            }, e -> e.getItem() != null ? net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(e.getItem().getItem()).toString() : "",
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.ITEM_ID -> ctx.triggerItemId;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                default -> null;
            });

        NodeHelper.setup("on_player_attack", "node.mgmc.on_player_attack.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/player/on_player_attack")
            .execOut()
            .output(NodePorts.VICTIM_ENTITY, "node.mgmc.port.victim_entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(MGMCEventType.PLAYER_ATTACK, (e, b) -> {
                if (e.getEntity() != null) {
                    b.triggerUuid(e.getEntity().getUUID().toString())
                     .triggerName(e.getEntity().getName().getString())
                     .triggerEntity(e.getEntity());
                }
                if (e.getTargetEntity() != null) {
                    b.triggerExtraUuid(e.getTargetEntity().getUUID().toString())
                     .triggerExtraEntity(e.getTargetEntity());
                }
            }, e -> BlueprintRouter.PLAYERS_ID,
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.VICTIM_ENTITY -> ctx.triggerExtraEntity;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                default -> null;
            });

        // --- 实体事件 ---
        NodeHelper.setup("on_entity_death", "node.mgmc.on_entity_death.name")
            .category("node_category.mgmc.events.entity")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/entity/on_entity_death")
            .execOut()
            .output(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .output(NodePorts.ATTACKER_ENTITY, "node.mgmc.port.attacker_entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(MGMCEventType.ENTITY_DEATH, (e, b) -> {
                if (e.getEntity() != null) {
                    b.triggerUuid(e.getEntity().getUUID().toString())
                     .triggerName(e.getEntity().getName().getString())
                     .triggerEntity(e.getEntity())
                     .triggerX(e.getEntity().getX()).triggerY(e.getEntity().getY()).triggerZ(e.getEntity().getZ());
                }
                if (e.getDamageSource() != null) {
                    b.triggerExtraUuid(e.getDamageSource().getEntity() != null ? e.getDamageSource().getEntity().getUUID().toString() : "")
                     .triggerExtraEntity(e.getDamageSource().getEntity());
                }
            }, e -> e.getEntity() != null ? net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(e.getEntity().getType()).toString() : "",
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.XYZ -> ctx.triggerXYZ;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                case NodePorts.ATTACKER_ENTITY -> ctx.triggerExtraEntity;
                default -> null;
            });

        NodeHelper.setup("on_entity_hurt", "node.mgmc.on_entity_hurt.name")
            .category("node_category.mgmc.events.entity")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/entity/on_entity_hurt")
            .execOut()
            .output(NodePorts.DAMAGE_AMOUNT, "node.mgmc.port.damage_amount", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .output(NodePorts.ATTACKER_ENTITY, "node.mgmc.port.attacker_entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(MGMCEventType.ENTITY_HURT, (e, b) -> {
                if (e.getEntity() != null) {
                    b.triggerUuid(e.getEntity().getUUID().toString())
                     .triggerName(e.getEntity().getName().getString())
                     .triggerEntity(e.getEntity())
                     .triggerValue(e.getAmount());
                }
                if (e.getDamageSource() != null) {
                    b.triggerExtraUuid(e.getDamageSource().getEntity() != null ? e.getDamageSource().getEntity().getUUID().toString() : "")
                     .triggerExtraEntity(e.getDamageSource().getEntity());
                }
            }, e -> e.getEntity() != null ? net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(e.getEntity().getType()).toString() : "",
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.DAMAGE_AMOUNT -> ctx.triggerValue;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                case NodePorts.ATTACKER_ENTITY -> ctx.triggerExtraEntity;
                default -> null;
            });

        NodeHelper.setup("on_entity_spawn", "node.mgmc.on_entity_spawn.name")
            .category("node_category.mgmc.events.entity")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/events/entity/on_entity_spawn")
            .execOut()
            .output(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(MGMCEventType.ENTITY_SPAWN, (e, b) -> {
                if (e.getEntity() != null) {
                    b.triggerUuid(e.getEntity().getUUID().toString())
                     .triggerName(e.getEntity().getName().getString())
                     .triggerEntity(e.getEntity())
                     .triggerX(e.getEntity().getX()).triggerY(e.getEntity().getY()).triggerZ(e.getEntity().getZ());
                }
            }, e -> e.getEntity() != null ? net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(e.getEntity().getType()).toString() : "",
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.XYZ -> ctx.triggerXYZ;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                default -> null;
            });
    }
}