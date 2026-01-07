package ltd.opens.mg.mc.core.blueprint.nodes;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeHelper;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;

import java.util.Optional;
import java.util.UUID;

/**
 * 动作节点 (Action Nodes)
 */
public class ActionNodes {
    private static final int COLOR_ACTION = 0xFFCC3333; // 红色表示动作
    private static final int COLOR_STRING = 0xFFFFAA00;
    private static final int COLOR_FLOAT = 0xFF55FF55;

    public static void register() {
        // print_chat (聊天输出)
        NodeHelper.setup("print_chat", "node.mgmc.print_chat.name")
            .category("node_category.mgmc.action.player")
            .color(COLOR_ACTION)
            .input("exec", "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, 0)
            .input("message", "node.mgmc.port.message", NodeDefinition.PortType.STRING, COLOR_STRING, "")
            .output("exec", "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, 0)
            .registerExec((node, ctx) -> {
                String message = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "message", ctx));
                if (ctx.level != null && !ctx.level.isClientSide()) {
                    if (ctx.triggerUuid != null && !ctx.triggerUuid.isEmpty()) {
                        try {
                            ServerPlayer player = ctx.level.getServer().getPlayerList().getPlayer(UUID.fromString(ctx.triggerUuid));
                            if (player != null) {
                                player.sendSystemMessage(Component.literal(message));
                            } else {
                                ctx.level.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message), false);
                            }
                        } catch (Exception e) {
                            ctx.level.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message), false);
                        }
                    } else {
                        ctx.level.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message), false);
                    }
                }
                NodeLogicRegistry.triggerExec(node, "exec", ctx);
            });

        // run_command_as_player (以玩家身份运行命令)
        NodeHelper.setup("run_command_as_player", "node.mgmc.run_command_as_player.name")
            .category("node_category.mgmc.action.player")
            .color(COLOR_ACTION)
            .input("exec", "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, 0)
            .input("uuid", "node.mgmc.port.uuid", NodeDefinition.PortType.STRING, COLOR_STRING, "")
            .input("command", "node.mgmc.port.command", NodeDefinition.PortType.STRING, COLOR_STRING, "")
            .output("exec", "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, 0)
            .registerExec((node, ctx) -> {
                String uuidStr = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "uuid", ctx));
                String command = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "command", ctx));

                if (ctx.level != null && !ctx.level.isClientSide() && ctx.level.getServer() != null) {
                    ServerPlayer player = null;
                    if (uuidStr != null && !uuidStr.isEmpty()) {
                        try {
                            player = ctx.level.getServer().getPlayerList().getPlayer(UUID.fromString(uuidStr));
                        } catch (Exception ignored) {}
                    }

                    if (player == null && ctx.triggerUuid != null && !ctx.triggerUuid.isEmpty()) {
                        try {
                            player = ctx.level.getServer().getPlayerList().getPlayer(UUID.fromString(ctx.triggerUuid));
                        } catch (Exception ignored) {}
                    }

                    if (player != null && command != null && !command.isEmpty()) {
                        if (command.startsWith("/")) {
                            command = command.substring(1);
                        }
                        ctx.level.getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack(), command);
                    }
                }
                NodeLogicRegistry.triggerExec(node, "exec", ctx);
            });

        // play_effect (播放特效)
        NodeHelper.setup("play_effect", "node.mgmc.play_effect.name")
            .category("node_category.mgmc.action.world")
            .color(COLOR_ACTION)
            .input("exec", "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, 0)
            .input("effect", "node.mgmc.port.effect", NodeDefinition.PortType.STRING, COLOR_STRING, "minecraft:heart")
            .input("x", "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .input("y", "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .input("z", "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .output("exec", "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, 0)
            .registerExec((node, ctx) -> {
                String effectName = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "effect", ctx));
                double x = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "x", ctx));
                double y = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "y", ctx));
                double z = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "z", ctx));

                try {
                    if (ctx.level instanceof ServerLevel serverLevel) {
                        Identifier id = Identifier.parse(effectName);
                        Optional<ParticleType<?>> particleTypeOptional = BuiltInRegistries.PARTICLE_TYPE.getOptional(id);
                        if (particleTypeOptional.isPresent()) {
                            ParticleType<?> type = particleTypeOptional.get();
                            if (type instanceof ParticleOptions options) {
                                serverLevel.sendParticles(options, x, y, z, 20, 0.5, 0.5, 0.5, 0.05);
                            }
                        }
                    }
                } catch (Exception ignored) {}
                NodeLogicRegistry.triggerExec(node, "exec", ctx);
            });

        // explosion (爆炸)
        NodeHelper.setup("explosion", "node.mgmc.explosion.name")
            .category("node_category.mgmc.action.world")
            .color(COLOR_ACTION)
            .input("exec", "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, 0)
            .input("radius", "node.mgmc.port.radius", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 3.0)
            .input("x", "node.mgmc.port.x", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .input("y", "node.mgmc.port.y", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .input("z", "node.mgmc.port.z", NodeDefinition.PortType.FLOAT, COLOR_FLOAT, 0.0)
            .output("exec", "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, 0)
            .registerExec((node, ctx) -> {
                double x = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "x", ctx));
                double y = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "y", ctx));
                double z = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "z", ctx));
                float radius = (float) TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "radius", ctx));
                if (radius <= 0) radius = 3.0f;

                try {
                    if (ctx.level != null) {
                        ctx.level.explode(null, x, y, z, radius, Level.ExplosionInteraction.TNT);
                    }
                } catch (Exception ignored) {}
                NodeLogicRegistry.triggerExec(node, "exec", ctx);
            });
    }
}
