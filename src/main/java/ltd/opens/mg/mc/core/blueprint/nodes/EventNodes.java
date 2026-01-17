package ltd.opens.mg.mc.core.blueprint.nodes;

import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeHelper;
import ltd.opens.mg.mc.core.blueprint.NodePorts;
import ltd.opens.mg.mc.core.blueprint.NodeThemes;
import ltd.opens.mg.mc.core.blueprint.routing.BlueprintRouter;
import ltd.opens.mg.mc.core.blueprint.events.RegisterMGMCNodesEvent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import ltd.opens.mg.mc.core.blueprint.data.XYZ;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 事件类节点注册
 * 包含节点定义及其对应的数据提取逻辑
 */
public class EventNodes {

    private static final Map<UUID, XYZ> lastPlayerPositions = new ConcurrentHashMap<>();
    private static final Map<PlayerTickEvent.Post, XYZ> eventOldPosCache = Collections.synchronizedMap(new WeakHashMap<>());

    private static XYZ getOldPos(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        return eventOldPosCache.computeIfAbsent(event, e -> {
            XYZ old = lastPlayerPositions.get(player.getUUID());
            if (old == null) {
                old = new XYZ(player.getX(), player.getY(), player.getZ());
            }
            lastPlayerPositions.put(player.getUUID(), new XYZ(player.getX(), player.getY(), player.getZ()));
            return old;
        });
    }

    @SubscribeEvent
    public static void onRegister(RegisterMGMCNodesEvent event) {
        // --- 世界事件 ---
        NodeHelper.setup("on_mgrun", "node.mgmc.on_mgrun.name")
            .category("node_category.mgmc.events.world")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .execOut()
            .output(NodePorts.NAME, "node.mgmc.port.name", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.PARAMETERS, "node.mgmc.on_mgrun.port.parameters", NodeDefinition.PortType.LIST, NodeThemes.COLOR_PORT_LIST)
            .output(NodePorts.TRIGGER_ENTITY, "node.mgmc.port.trigger_entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .output(NodePorts.TRIGGER_NAME, "node.mgmc.port.trigger_name", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .registerValue((node, portId, ctx) -> switch (portId) {
                case NodePorts.NAME -> ctx.eventName;
                case NodePorts.PARAMETERS -> Arrays.asList(ctx.args);
                case NodePorts.TRIGGER_ENTITY -> ctx.triggerEntity;
                case NodePorts.TRIGGER_NAME -> ctx.triggerName != null ? ctx.triggerName : "";
                case NodePorts.XYZ -> ctx.triggerXYZ;
                default -> null;
            });

        // --- 玩家事件 ---
        NodeHelper.setup("on_break_block", "node.mgmc.on_break_block.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .execOut()
            .output(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.BLOCK_ID, "node.mgmc.port.block_id", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(BlockEvent.BreakEvent.class, (e, b) -> {
                b.triggerUuid(e.getPlayer().getUUID().toString())
                 .triggerName(e.getPlayer().getName().getString())
                 .triggerEntity(e.getPlayer())
                 .triggerX(e.getPos().getX()).triggerY(e.getPos().getY()).triggerZ(e.getPos().getZ())
                 .triggerBlockId(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(e.getState().getBlock()).toString());
            }, e -> net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(e.getState().getBlock()).toString(),
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.XYZ -> ctx.triggerXYZ;
                case NodePorts.BLOCK_ID -> ctx.triggerBlockId;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                default -> null;
            });

        NodeHelper.setup("on_place_block", "node.mgmc.on_place_block.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .execOut()
            .output(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.BLOCK_ID, "node.mgmc.port.block_id", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(BlockEvent.EntityPlaceEvent.class, (e, b) -> {
                if (e.getEntity() instanceof Player p) {
                    b.triggerUuid(p.getUUID().toString())
                     .triggerName(p.getName().getString())
                     .triggerEntity(p)
                     .triggerX(e.getPos().getX()).triggerY(e.getPos().getY()).triggerZ(e.getPos().getZ())
                     .triggerBlockId(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(e.getState().getBlock()).toString());
                }
            }, e -> net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(e.getState().getBlock()).toString(),
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.XYZ -> ctx.triggerXYZ;
                case NodePorts.BLOCK_ID -> ctx.triggerBlockId;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                default -> null;
            });

        NodeHelper.setup("on_interact_block", "node.mgmc.on_interact_block.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .execOut()
            .output(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.BLOCK_ID, "node.mgmc.port.block_id", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(PlayerInteractEvent.RightClickBlock.class, (e, b) -> {
                b.triggerUuid(e.getEntity().getUUID().toString())
                 .triggerName(e.getEntity().getName().getString())
                 .triggerEntity(e.getEntity())
                 .triggerX(e.getPos().getX()).triggerY(e.getPos().getY()).triggerZ(e.getPos().getZ())
                 .triggerBlockId(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(e.getLevel().getBlockState(e.getPos()).getBlock()).toString());
            }, e -> net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(e.getLevel().getBlockState(e.getPos()).getBlock()).toString(),
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.XYZ -> ctx.triggerXYZ;
                case NodePorts.BLOCK_ID -> ctx.triggerBlockId;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                default -> null;
            });

        NodeHelper.setup("on_player_join", "node.mgmc.on_player_join.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .execOut()
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .output(NodePorts.NAME, "node.mgmc.port.name", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .registerEvent(PlayerEvent.PlayerLoggedInEvent.class, (e, b) -> {
                b.triggerUuid(e.getEntity().getUUID().toString())
                 .triggerName(e.getEntity().getName().getString())
                 .triggerEntity(e.getEntity());
            }, e -> BlueprintRouter.PLAYERS_ID,
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.ENTITY -> ctx.triggerEntity;
                case NodePorts.NAME -> ctx.triggerName;
                default -> null;
            });

        NodeHelper.setup("on_player_death", "node.mgmc.on_player_death.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .execOut()
            .output(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .output(NodePorts.ATTACKER_ENTITY, "node.mgmc.port.attacker_entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(LivingDeathEvent.class, (e, b) -> {
                if (e.getEntity() instanceof Player p) {
                    b.triggerUuid(p.getUUID().toString())
                     .triggerName(p.getName().getString())
                     .triggerEntity(p)
                     .triggerX(p.getX()).triggerY(p.getY()).triggerZ(p.getZ())
                     .triggerExtraUuid(e.getSource().getEntity() != null ? e.getSource().getEntity().getUUID().toString() : "")
                     .triggerExtraEntity(e.getSource().getEntity());
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
            .execOut()
            .output(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(PlayerEvent.PlayerRespawnEvent.class, (e, b) -> {
                b.triggerUuid(e.getEntity().getUUID().toString())
                 .triggerName(e.getEntity().getName().getString())
                 .triggerEntity(e.getEntity())
                 .triggerX(e.getEntity().getX()).triggerY(e.getEntity().getY()).triggerZ(e.getEntity().getZ());
            }, e -> BlueprintRouter.PLAYERS_ID,
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.XYZ -> ctx.triggerXYZ;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                default -> null;
            });

        NodeHelper.setup("on_player_hurt", "node.mgmc.on_player_hurt.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .execOut()
            .output(NodePorts.DAMAGE_AMOUNT, "node.mgmc.port.damage_amount", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.ATTACKER_ENTITY, "node.mgmc.port.attacker_entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(LivingIncomingDamageEvent.class, (e, b) -> {
                if (e.getEntity() instanceof Player p) {
                    b.triggerUuid(p.getUUID().toString())
                     .triggerName(p.getName().getString())
                     .triggerEntity(p)
                     .triggerValue(e.getAmount())
                     .triggerExtraUuid(e.getSource().getEntity() != null ? e.getSource().getEntity().getUUID().toString() : "")
                     .triggerExtraEntity(e.getSource().getEntity());
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
            .execOut()
            .output(NodePorts.ITEM_ID, "node.mgmc.port.item_id", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(PlayerInteractEvent.RightClickItem.class, (e, b) -> {
                b.triggerUuid(e.getEntity().getUUID().toString())
                 .triggerName(e.getEntity().getName().getString())
                 .triggerEntity(e.getEntity())
                 .triggerItemId(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(e.getItemStack().getItem()).toString());
            }, e -> net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(e.getItemStack().getItem()).toString(),
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.ITEM_ID -> ctx.triggerItemId;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                default -> null;
            });

        NodeHelper.setup("on_player_attack", "node.mgmc.on_player_attack.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .execOut()
            .output(NodePorts.VICTIM_ENTITY, "node.mgmc.port.victim_entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(AttackEntityEvent.class, (e, b) -> {
                b.triggerUuid(e.getEntity().getUUID().toString())
                 .triggerName(e.getEntity().getName().getString())
                 .triggerEntity(e.getEntity())
                 .triggerExtraUuid(e.getTarget().getUUID().toString())
                 .triggerExtraEntity(e.getTarget());
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
            .execOut()
            .output(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .output(NodePorts.ATTACKER_ENTITY, "node.mgmc.port.attacker_entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(LivingDeathEvent.class, (e, b) -> {
                b.triggerUuid(e.getEntity().getUUID().toString())
                 .triggerName(e.getEntity().getName().getString())
                 .triggerEntity(e.getEntity())
                 .triggerX(e.getEntity().getX()).triggerY(e.getEntity().getY()).triggerZ(e.getEntity().getZ())
                 .triggerExtraUuid(e.getSource().getEntity() != null ? e.getSource().getEntity().getUUID().toString() : "")
                 .triggerExtraEntity(e.getSource().getEntity());
            }, e -> net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(e.getEntity().getType()).toString(),
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.XYZ -> ctx.triggerXYZ;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                case NodePorts.ATTACKER_ENTITY -> ctx.triggerExtraEntity;
                default -> null;
            });

        NodeHelper.setup("on_entity_hurt", "node.mgmc.on_entity_hurt.name")
            .category("node_category.mgmc.events.entity")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .execOut()
            .output(NodePorts.DAMAGE_AMOUNT, "node.mgmc.port.damage_amount", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .output(NodePorts.ATTACKER_ENTITY, "node.mgmc.port.attacker_entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(LivingIncomingDamageEvent.class, (e, b) -> {
                b.triggerUuid(e.getEntity().getUUID().toString())
                 .triggerName(e.getEntity().getName().getString())
                 .triggerEntity(e.getEntity())
                 .triggerValue(e.getAmount())
                 .triggerExtraUuid(e.getSource().getEntity() != null ? e.getSource().getEntity().getUUID().toString() : "")
                 .triggerExtraEntity(e.getSource().getEntity());
            }, e -> net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(e.getEntity().getType()).toString(),
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.DAMAGE_AMOUNT -> ctx.triggerValue;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                case NodePorts.ATTACKER_ENTITY -> ctx.triggerExtraEntity;
                default -> null;
            });

        NodeHelper.setup("on_entity_spawn", "node.mgmc.on_entity_spawn.name")
            .category("node_category.mgmc.events.entity")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .execOut()
            .output(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(EntityJoinLevelEvent.class, (e, b) -> {
                b.triggerUuid(e.getEntity().getUUID().toString())
                 .triggerName(e.getEntity().getName().getString())
                 .triggerEntity(e.getEntity())
                 .triggerX(e.getEntity().getX()).triggerY(e.getEntity().getY()).triggerZ(e.getEntity().getZ());
            }, e -> net.minecraft.core.registries.BuiltInRegistries.ENTITY_TYPE.getKey(e.getEntity().getType()).toString(),
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.XYZ -> ctx.triggerXYZ;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                default -> null;
            });

        NodeHelper.setup("on_item_pickup", "node.mgmc.on_item_pickup.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .execOut()
            .output(NodePorts.ITEM_ID, "node.mgmc.port.item_id", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent.Post.class, (e, b) -> {
                b.triggerUuid(e.getPlayer().getUUID().toString())
                 .triggerName(e.getPlayer().getName().getString())
                 .triggerEntity(e.getPlayer())
                 .triggerItemId(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(e.getItemEntity().getItem().getItem()).toString());
            }, e -> net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(e.getItemEntity().getItem().getItem()).toString(),
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.ITEM_ID -> ctx.triggerItemId;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                default -> null;
            });

        NodeHelper.setup("on_left_click_block", "node.mgmc.on_left_click_block.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .execOut()
            .output(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.BLOCK_ID, "node.mgmc.port.block_id", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(PlayerInteractEvent.LeftClickBlock.class, (e, b) -> {
                b.triggerUuid(e.getEntity().getUUID().toString())
                 .triggerName(e.getEntity().getName().getString())
                 .triggerEntity(e.getEntity())
                 .triggerX(e.getPos().getX()).triggerY(e.getPos().getY()).triggerZ(e.getPos().getZ())
                 .triggerBlockId(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(e.getLevel().getBlockState(e.getPos()).getBlock()).toString());
            }, e -> net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(e.getLevel().getBlockState(e.getPos()).getBlock()).toString(),
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.XYZ -> ctx.triggerXYZ;
                case NodePorts.BLOCK_ID -> ctx.triggerBlockId;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                default -> null;
            });

        NodeHelper.setup("on_player_move", "node.mgmc.on_player_move.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .execOut()
            .output(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.SPEED, "node.mgmc.port.speed", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerEvent(PlayerTickEvent.Post.class, (e, b) -> {
                Player player = e.getEntity();
                XYZ oldPos = getOldPos(e);
                double dx = player.getX() - oldPos.x();
                double dy = player.getY() - oldPos.y();
                double dz = player.getZ() - oldPos.z();
                double distanceSq = dx * dx + dy * dy + dz * dz;

                if (distanceSq > 1E-6) {
                    b.triggerUuid(player.getUUID().toString())
                     .triggerName(player.getName().getString())
                     .triggerEntity(player)
                     .triggerX(player.getX()).triggerY(player.getY()).triggerZ(player.getZ())
                     .triggerSpeed((float) Math.sqrt(distanceSq));
                }
            }, e -> {
                Player player = e.getEntity();
                XYZ oldPos = getOldPos(e);
                double dx = player.getX() - oldPos.x();
                double dy = player.getY() - oldPos.y();
                double dz = player.getZ() - oldPos.z();
                return (dx * dx + dy * dy + dz * dz > 1E-6) ? BlueprintRouter.PLAYERS_ID : null;
            },
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.XYZ -> ctx.triggerXYZ;
                case NodePorts.SPEED -> ctx.triggerSpeed;
                case NodePorts.ENTITY -> ctx.triggerEntity;
                default -> null;
            });

        NodeHelper.setup("on_player_leave", "node.mgmc.on_player_leave.name")
            .category("node_category.mgmc.events.player")
            .color(NodeThemes.COLOR_NODE_EVENT)
            .execOut()
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .output(NodePorts.NAME, "node.mgmc.port.name", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .registerEvent(PlayerEvent.PlayerLoggedOutEvent.class, (e, b) -> {
                lastPlayerPositions.remove(e.getEntity().getUUID());
                b.triggerUuid(e.getEntity().getUUID().toString())
                 .triggerName(e.getEntity().getName().getString())
                 .triggerEntity(e.getEntity());
            }, e -> BlueprintRouter.PLAYERS_ID,
            (node, portId, ctx) -> switch (portId) {
                case NodePorts.ENTITY -> ctx.triggerEntity;
                case NodePorts.NAME -> ctx.triggerName;
                default -> null;
            });
    }
}
