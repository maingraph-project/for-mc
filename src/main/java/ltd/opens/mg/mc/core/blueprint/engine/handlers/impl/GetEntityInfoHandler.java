package ltd.opens.mg.mc.core.blueprint.engine.handlers.impl;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import java.util.UUID;

public class GetEntityInfoHandler implements NodeHandler {
    @Override
    public String getValue(JsonObject node, String pinId, NodeContext ctx) {
        String uuidStr = NodeLogicRegistry.evaluateInput(node, "uuid", ctx);
        if (uuidStr == null || uuidStr.isEmpty()) return getDefaultValue(pinId);

        try {
            UUID uuid = UUID.fromString(uuidStr);
            Entity entity = findEntity(uuid);

            if (entity != null) {
                switch (pinId) {
                    case "name": return entity.getName().getString();
                    case "type": return entity.getType().getDescription().getString();
                    case "pos_x": return String.valueOf(entity.getX());
                    case "pos_y": return String.valueOf(entity.getY());
                    case "pos_z": return String.valueOf(entity.getZ());
                    case "health":
                        if (entity instanceof LivingEntity) {
                            return String.valueOf(((LivingEntity) entity).getHealth());
                        }
                        return "0";
                    case "max_health":
                        if (entity instanceof LivingEntity) {
                            return String.valueOf(((LivingEntity) entity).getMaxHealth());
                        }
                        return "0";
                    case "is_living": return String.valueOf(entity instanceof LivingEntity);
                    case "is_player": return String.valueOf(entity instanceof Player);
                }
            }
        } catch (Exception e) {
            // Ignore errors
        }
        
        return getDefaultValue(pinId);
    }

    private Entity findEntity(UUID uuid) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return null;

        // 1. Check local player first (most common for trigger_uuid)
        if (mc.player != null && mc.player.getUUID().equals(uuid)) {
            return mc.player;
        }

        // 2. Check all loaded entities
        for (Entity e : mc.level.entitiesForRendering()) {
            if (e.getUUID().equals(uuid)) {
                return e;
            }
        }

        return null;
    }

    private String getDefaultValue(String pinId) {
        switch (pinId) {
            case "pos_x":
            case "pos_y":
            case "pos_z":
            case "health":
            case "max_health":
                return "0";
            case "is_living":
            case "is_player":
                return "false";
            default:
                return "";
        }
    }
}
