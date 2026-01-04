package ltd.opens.mg.mc.core.blueprint.engine.handlers.entity;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.NameAndId;
import net.minecraft.server.permissions.LevelBasedPermissionSet;
import net.minecraft.core.registries.BuiltInRegistries;
import java.util.UUID;

public class GetEntityInfoHandler implements NodeHandler {
    @Override
    public Object getValue(JsonObject node, String pinId, NodeContext ctx) {
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
                            return ((LivingEntity) entity).getHealth();
                        }
                        return 0.0;
                    case "max_health":
                        if (entity instanceof LivingEntity) {
                            return ((LivingEntity) entity).getMaxHealth();
                        }
                        return 0.0;
                    case "is_living": return entity instanceof LivingEntity;
                    case "is_player": return entity instanceof Player;
                    case "is_online":
                        if (ctx.level != null) {
                            return ctx.level.getServer().getPlayerList().getPlayer(uuid) != null;
                        }
                        return false;
                    case "permission_level":
                        if (entity instanceof ServerPlayer serverPlayer && ctx.level != null) {
                            return ctx.level.getServer().getProfilePermissions(new NameAndId(serverPlayer.getUUID(), serverPlayer.getGameProfile().name())).level().id();
                        }
                        return 0.0;
                }
            }
        } catch (Exception e) {
            // Ignore errors
        }
        
        return getDefaultValue(pinId);
    }

    private Entity findEntity(UUID uuid, NodeContext ctx) {
        if (ctx.level == null) return null;

        if (ctx.level instanceof ServerLevel serverLevel) {
            return serverLevel.getEntity(uuid);
        } else {
            // Level doesn't have a direct allEntities() or similar simple getter in all versions
            // but we can use getEntities(null, ...) with a large bounding box if needed.
            // However, this handler should primarily run on server.
            return null;
        }
    }

    private Object getDefaultValue(String pinId) {
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




