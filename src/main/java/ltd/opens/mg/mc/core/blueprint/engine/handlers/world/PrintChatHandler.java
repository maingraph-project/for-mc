package ltd.opens.mg.mc.core.blueprint.engine.handlers.world;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import java.util.UUID;

public class PrintChatHandler implements NodeHandler {
    @Override
    public void execute(JsonObject node, NodeContext ctx) {
        String message = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "message", ctx));
        
        if (ctx.level != null && !ctx.level.isClientSide()) {
            if (ctx.triggerUuid != null && !ctx.triggerUuid.isEmpty()) {
                try {
                    ServerPlayer player = ctx.level.getServer().getPlayerList().getPlayer(UUID.fromString(ctx.triggerUuid));
                    if (player != null) {
                        player.sendSystemMessage(Component.literal(message));
                    }
                } catch (Exception e) {
                    ctx.level.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message), false);
                }
            } else {
                ctx.level.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message), false);
            }
        }
        
        NodeLogicRegistry.triggerExec(node, "exec", ctx);
    }
}

