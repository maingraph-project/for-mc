package ltd.opens.mg.mc.core.blueprint.nodes;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeHelper;
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

import java.util.UUID;

/**
 * 获取实体信息节点 (Get Entity Info Node)
 * 独立类实现，方便维护复杂的输出逻辑
 */
public class GetEntityInfoNode {
    private static final int COLOR_ENTITY = 0xFF33CCCC; // 青色表示实体相关
    private static final int COLOR_STRING = 0xFFFFAA00;
    private static final int COLOR_FLOAT = 0xFF55FF55;
    private static final int COLOR_BOOLEAN = 0xFF5555FF;

    public static void register() {
        NodeHelper.setup("get_entity_info", "node.mgmc.get_entity_info.name")
            .category("node_category.mgmc.variable.entity")
            .color(COLOR_ENTITY)
            .input("uuid", "node.mgmc.port.uuid", NodeDefinition.PortType.STRING, COLOR_STRING, "")
            .output("name", "node.mgmc.port.name", NodeDefinition.PortType.STRING, COLOR_STRING)
            .output("type", "node.mgmc.get_entity_info.port.type", NodeDefinition.PortType.STRING, COLOR_STRING)
            .output("registry_name", "node.mgmc.get_entity_info.port.registry_name", NodeDefinition.PortType.STRING, COLOR_STRING)
            .output("pos_x", "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .output("pos_y", "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .output("pos_z", "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .output("health", "node.mgmc.get_entity_info.port.health", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .output("max_health", "node.mgmc.get_entity_info.port.max_health", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .output("is_living", "node.mgmc.get_entity_info.port.is_living", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN)
            .output("is_player", "node.mgmc.get_entity_info.port.is_player", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN)
            .output("is_online", "node.mgmc.get_entity_info.port.is_online", NodeDefinition.PortType.BOOLEAN, COLOR_BOOLEAN)
            .output("permission_level", "node.mgmc.get_entity_info.port.permission_level", NodeDefinition.PortType.FLOAT, COLOR_FLOAT)
            .registerValue((node, pinId, ctx) -> {
                String uuidStr = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "uuid", ctx));
                if (uuidStr == null || uuidStr.isEmpty()) return getDefaultValue(pinId);

                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    Entity entity = findEntity(uuid, ctx);

                    if (entity != null) {
                        switch (pinId) {
                            case "name": return entity.getName().getString();
                            case "type": return entity.getType().getDescription().getString();
                            case "registry_name": 
                                return BuiltInRegistries.ENTITY_TYPE.getKey(entity.getType()).toString();
                            case "pos_x": return entity.getX();
                            case "pos_y": return entity.getY();
                            case "pos_z": return entity.getZ();
                            case "health":
                                if (entity instanceof LivingEntity) {
                                    return (double) ((LivingEntity) entity).getHealth();
                                }
                                return 0.0;
                            case "max_health":
                                if (entity instanceof LivingEntity) {
                                    return (double) ((LivingEntity) entity).getMaxHealth();
                                }
                                return 0.0;
                            case "is_living": return entity instanceof LivingEntity;
                            case "is_player": return entity instanceof Player;
                            case "is_online":
                                if (ctx.level != null && ctx.level.getServer() != null) {
                                    return ctx.level.getServer().getPlayerList().getPlayer(uuid) != null;
                                }
                                return false;
                            case "permission_level":
                                if (entity instanceof ServerPlayer serverPlayer && ctx.level != null && ctx.level.getServer() != null) {
                                    return (double) ctx.level.getServer().getProfilePermissions(new NameAndId(serverPlayer.getUUID(), serverPlayer.getGameProfile().name())).level().id();
                                }
                                return 0.0;
                        }
                    }
                } catch (Exception ignored) {}
                
                return getDefaultValue(pinId);
            });
    }

    private static Entity findEntity(UUID uuid, NodeContext ctx) {
        if (ctx.level instanceof ServerLevel serverLevel) {
            return serverLevel.getEntity(uuid);
        }
        return null;
    }

    private static Object getDefaultValue(String pinId) {
        switch (pinId) {
            case "pos_x":
            case "pos_y":
            case "pos_z":
            case "health":
            case "max_health":
            case "permission_level":
                return 0.0;
            case "is_living":
            case "is_player":
            case "is_online":
                return false;
            default:
                return "";
        }
    }
}
