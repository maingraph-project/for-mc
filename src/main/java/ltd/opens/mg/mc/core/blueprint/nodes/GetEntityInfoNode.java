package ltd.opens.mg.mc.core.blueprint.nodes;

import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeHelper;
import ltd.opens.mg.mc.core.blueprint.NodePorts;
import ltd.opens.mg.mc.core.blueprint.NodeThemes;
import ltd.opens.mg.mc.core.blueprint.data.XYZ;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

import ltd.opens.mg.mc.core.blueprint.events.RegisterMGMCNodesEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.UUID;

/**
 * 获取实体信息节点 (Get Entity Info Node)
 * 独立类实现，方便维护复杂的输出逻辑
 */
public class GetEntityInfoNode {

    @SubscribeEvent
    public static void onRegister(RegisterMGMCNodesEvent event) {
        // 1. get_entity_info_byuuid
        NodeHelper.setup("get_entity_info_byuuid", "node.mgmc.get_entity_info_byuuid.name")
            .category("node_category.mgmc.variable.entity")
            .color(NodeThemes.COLOR_NODE_ENTITY)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/variable/entity/get_entity_info_byuuid")
            .input(NodePorts.UUID, "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, NodeThemes.COLOR_PORT_UUID, "")
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .output(NodePorts.UUID, "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, NodeThemes.COLOR_PORT_UUID)
            .output(NodePorts.NAME, "node.mgmc.port.name", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.TYPE, "node.mgmc.get_entity_info.port.type", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.REGISTRY_NAME, "node.mgmc.get_entity_info.port.registry_name", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.HEALTH, "node.mgmc.get_entity_info.port.health", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.MAX_HEALTH, "node.mgmc.get_entity_info.port.max_health", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.IS_LIVING, "node.mgmc.get_entity_info.port.is_living", NodeDefinition.PortType.BOOLEAN, NodeThemes.COLOR_PORT_BOOLEAN)
            .output(NodePorts.IS_PLAYER, "node.mgmc.get_entity_info.port.is_player", NodeDefinition.PortType.BOOLEAN, NodeThemes.COLOR_PORT_BOOLEAN)
            .output(NodePorts.IS_ONLINE, "node.mgmc.get_entity_info.port.is_online", NodeDefinition.PortType.BOOLEAN, NodeThemes.COLOR_PORT_BOOLEAN)
            .output(NodePorts.PERMISSION_LEVEL, "node.mgmc.get_entity_info.port.permission_level", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .registerValue((node, pinId, ctx) -> {
                String uuidStr = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.UUID, ctx));
                if (uuidStr == null || uuidStr.isEmpty()) return getDefaultValue(pinId);

                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    Entity entity = findEntity(uuid, ctx);
                    return getEntityInfo(entity, uuid, pinId, ctx);
                } catch (Exception ignored) {}
                return getDefaultValue(pinId);
            });

        // 2. get_entity_info_byentity
        NodeHelper.setup("get_entity_info_byentity", "node.mgmc.get_entity_info_byentity.name")
            .category("node_category.mgmc.variable.entity")
            .color(NodeThemes.COLOR_NODE_ENTITY)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/variable/entity/get_entity_info_byentity")
            .input(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .output(NodePorts.UUID, "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, NodeThemes.COLOR_PORT_UUID)
            .output(NodePorts.NAME, "node.mgmc.port.name", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.TYPE, "node.mgmc.get_entity_info.port.type", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.REGISTRY_NAME, "node.mgmc.get_entity_info.port.registry_name", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING)
            .output(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.HEALTH, "node.mgmc.get_entity_info.port.health", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.MAX_HEALTH, "node.mgmc.get_entity_info.port.max_health", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .output(NodePorts.IS_LIVING, "node.mgmc.get_entity_info.port.is_living", NodeDefinition.PortType.BOOLEAN, NodeThemes.COLOR_PORT_BOOLEAN)
            .output(NodePorts.IS_PLAYER, "node.mgmc.get_entity_info.port.is_player", NodeDefinition.PortType.BOOLEAN, NodeThemes.COLOR_PORT_BOOLEAN)
            .output(NodePorts.IS_ONLINE, "node.mgmc.get_entity_info.port.is_online", NodeDefinition.PortType.BOOLEAN, NodeThemes.COLOR_PORT_BOOLEAN)
            .output(NodePorts.PERMISSION_LEVEL, "node.mgmc.get_entity_info.port.permission_level", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .registerValue((node, pinId, ctx) -> {
                Object entityObj = NodeLogicRegistry.evaluateInput(node, NodePorts.ENTITY, ctx);
                if (entityObj instanceof Entity entity) {
                    return getEntityInfo(entity, entity.getUUID(), pinId, ctx);
                }
                return getDefaultValue(pinId);
            });

        // 3. get_entity_by_uuid
        NodeHelper.setup("get_entity_by_uuid", "node.mgmc.get_entity_by_uuid.name")
            .category("node_category.mgmc.variable.entity")
            .color(NodeThemes.COLOR_NODE_ENTITY)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/variable/entity/get_entity_by_uuid")
            .input(NodePorts.UUID, "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, NodeThemes.COLOR_PORT_UUID, "")
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .registerValue((node, pinId, ctx) -> {
                String uuidStr = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.UUID, ctx));
                if (uuidStr == null || uuidStr.isEmpty()) return null;
                try {
                    return findEntity(UUID.fromString(uuidStr), ctx);
                } catch (Exception ignored) {}
                return null;
            });

        // 4. self (获取当前挂载的目标)
        NodeHelper.setup("self", "node.mgmc.self.name")
            .category("node_category.mgmc.variable.entity")
            .color(NodeThemes.COLOR_NODE_ENTITY)
            .property("web_url", "http://zhcn-docs.mc.maingraph.nb6.ltd/nodes/variable/entity/self")
            .output(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .output(NodePorts.UUID, "node.mgmc.port.uuid", NodeDefinition.PortType.UUID, NodeThemes.COLOR_PORT_UUID)
            .registerValue((node, pinId, ctx) -> {
                Entity self = ctx.triggerEntity;
                if (self == null) return null;
                if (NodePorts.ENTITY.equals(pinId)) return self;
                if (NodePorts.UUID.equals(pinId)) return self.getUUID().toString();
                return null;
            });
    }

    private static Object getEntityInfo(Entity entity, UUID uuid, String pinId, NodeContext ctx) {
        if (pinId.equals(NodePorts.ENTITY)) return entity;
        if (entity == null) return getDefaultValue(pinId);
        
        switch (pinId) {
            case NodePorts.UUID: return uuid.toString();
            case NodePorts.NAME: return entity.getName().getString();
            case NodePorts.TYPE: return entity.getType().getDescription().getString();
            case NodePorts.REGISTRY_NAME:
                return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
            case NodePorts.XYZ: return new XYZ(entity.getX(), entity.getY(), entity.getZ());
            case NodePorts.HEALTH:
                if (entity instanceof LivingEntity) {
                    return (double) ((LivingEntity) entity).getHealth();
                }
                return 0.0;
            case NodePorts.MAX_HEALTH:
                if (entity instanceof LivingEntity) {
                    return (double) ((LivingEntity) entity).getMaxHealth();
                }
                return 0.0;
            case NodePorts.IS_LIVING: return entity instanceof LivingEntity;
            case NodePorts.IS_PLAYER: return entity instanceof Player;
            case NodePorts.IS_ONLINE:
                if (ctx.level != null && ctx.level.getServer() != null) {
                    return ctx.level.getServer().getPlayerList().getPlayer(uuid) != null;
                }
                return false;
            case NodePorts.PERMISSION_LEVEL:
                if (entity instanceof ServerPlayer serverPlayer && ctx.level != null && ctx.level.getServer() != null) {
                    return (double) ctx.level.getServer().getProfilePermissions(new NameAndId(serverPlayer.getUUID(), serverPlayer.getGameProfile().name())).level().id();
                }
                return 0.0;
        }
        return getDefaultValue(pinId);
    }

    private static Entity findEntity(UUID uuid, NodeContext ctx) {
        if (ctx.level instanceof ServerLevel serverLevel) {
            return serverLevel.getEntity(uuid);
        }
        return null;
    }

    private static Object getDefaultValue(String pinId) {
        switch (pinId) {
            case NodePorts.XYZ:
                return XYZ.ZERO;
            case NodePorts.UUID:
            case NodePorts.NAME:
            case NodePorts.TYPE:
            case NodePorts.REGISTRY_NAME:
                return "";
            case NodePorts.HEALTH:
            case NodePorts.MAX_HEALTH:
            case NodePorts.PERMISSION_LEVEL:
                return 0.0;
            case NodePorts.IS_LIVING:
            case NodePorts.IS_PLAYER:
            case NodePorts.IS_ONLINE:
                return false;
            default:
                return "";
        }
    }
}
