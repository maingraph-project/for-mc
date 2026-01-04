package ltd.opens.mg.mc.core.blueprint.engine.handlers.entity;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;
import net.minecraft.server.level.ServerPlayer;
import java.util.UUID;

public class RunCommandAsPlayerHandler implements NodeHandler {
    @Override
    public void execute(JsonObject node, NodeContext ctx) {
        String uuidStr = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "uuid", ctx));
        String command = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "command", ctx));

        if (ctx.level != null && !ctx.level.isClientSide() && ctx.level.getServer() != null) {
            ServerPlayer player = null;
            if (uuidStr != null && !uuidStr.isEmpty()) {
                try {
                    player = ctx.level.getServer().getPlayerList().getPlayer(UUID.fromString(uuidStr));
                } catch (Exception ignored) {}
            }

            // Fallback to trigger player if no UUID provided or invalid
            if (player == null && ctx.triggerUuid != null && !ctx.triggerUuid.isEmpty()) {
                try {
                    player = ctx.level.getServer().getPlayerList().getPlayer(UUID.fromString(ctx.triggerUuid));
                } catch (Exception ignored) {}
            }

            if (player != null && command != null && !command.isEmpty()) {
                // Ensure command doesn't start with /
                if (command.startsWith("/")) {
                    command = command.substring(1);
                }
                ctx.level.getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack(), command);
            }
        }

        NodeLogicRegistry.triggerExec(node, "exec", ctx);
    }
}



