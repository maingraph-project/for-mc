package ltd.opens.mg.mc.core.blueprint.engine.handlers.entity;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;
import net.minecraft.world.entity.player.Player;
import java.util.UUID;

public class PlayerHealthHandler implements NodeHandler {
    @Override
    public Object getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("value") && ctx.level != null) {
            Player player = null;
            if (ctx.triggerUuid != null && !ctx.triggerUuid.isEmpty()) {
                try {
                    player = ctx.level.getPlayerByUUID(UUID.fromString(ctx.triggerUuid));
                } catch (Exception e) {}
            }
            if (player != null) {
                return player.getHealth();
            }
        }
        return 0.0;
    }
}



