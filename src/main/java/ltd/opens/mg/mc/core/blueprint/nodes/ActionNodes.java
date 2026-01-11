package ltd.opens.mg.mc.core.blueprint.nodes;

import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeHelper;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;
import ltd.opens.mg.mc.core.blueprint.NodePorts;
import ltd.opens.mg.mc.core.blueprint.NodeThemes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;

import ltd.opens.mg.mc.core.blueprint.data.XYZ;
import ltd.opens.mg.mc.core.blueprint.events.RegisterMGMCNodesEvent;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.entity.LivingEntity;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * 动作节点 (Action Nodes)
 */
public class ActionNodes {

    @SubscribeEvent
    public static void onRegister(RegisterMGMCNodesEvent event) {
        // print_chat (聊天输出)
        NodeHelper.setup("print_chat", "node.mgmc.print_chat.name")
            .category("node_category.mgmc.action.player")
            .color(NodeThemes.COLOR_NODE_ACTION)
            .input(NodePorts.EXEC, "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .input(NodePorts.MESSAGE, "node.mgmc.port.message", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING, "")
            .output(NodePorts.EXEC, "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .registerExec((node, ctx) -> {
                String message = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.MESSAGE, ctx));
                if (ctx.level != null && !ctx.level.isClientSide()) {
                    if (ctx.triggerEntity instanceof ServerPlayer player) {
                        player.sendSystemMessage(Component.literal(message));
                    } else {
                        ctx.level.getServer().getPlayerList().broadcastSystemMessage(Component.literal(message), false);
                    }
                }
                NodeLogicRegistry.triggerExec(node, NodePorts.EXEC, ctx);
            });

        // run_command_as_player (以玩家身份运行命令)
        NodeHelper.setup("run_command_as_player", "node.mgmc.run_command_as_player.name")
            .category("node_category.mgmc.action.player")
            .color(NodeThemes.COLOR_NODE_ACTION)
            .input(NodePorts.EXEC, "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .input(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .input(NodePorts.COMMAND, "node.mgmc.port.command", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING, "")
            .output(NodePorts.EXEC, "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .registerExec((node, ctx) -> {
                Object entityObj = NodeLogicRegistry.evaluateInput(node, NodePorts.ENTITY, ctx);
                String command = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.COMMAND, ctx));

                if (ctx.level != null && !ctx.level.isClientSide() && ctx.level.getServer() != null) {
                    ServerPlayer player = null;
                    if (entityObj instanceof ServerPlayer sp) {
                        player = sp;
                    } else if (ctx.triggerEntity instanceof ServerPlayer sp) {
                        player = sp;
                    }

                    if (player != null && command != null && !command.isEmpty()) {
                        if (command.startsWith("/")) {
                            command = command.substring(1);
                        }
                        ctx.level.getServer().getCommands().performPrefixedCommand(player.createCommandSourceStack(), command);
                    }
                }
                NodeLogicRegistry.triggerExec(node, NodePorts.EXEC, ctx);
            });

        // play_effect (播放特效)
        NodeHelper.setup("play_effect", "node.mgmc.play_effect.name")
            .category("node_category.mgmc.action.world")
            .color(NodeThemes.COLOR_NODE_ACTION)
            .input(NodePorts.EXEC, "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .input(NodePorts.EFFECT, "node.mgmc.port.effect", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING, "minecraft:heart")
            .input(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.EXEC, "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .registerExec((node, ctx) -> {
                String effectName = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.EFFECT, ctx));
                XYZ pos = TypeConverter.toXYZ(NodeLogicRegistry.evaluateInput(node, NodePorts.XYZ, ctx));

                try {
                    if (ctx.level instanceof ServerLevel serverLevel) {
                        Identifier id = Identifier.parse(effectName);
                        Optional<ParticleType<?>> particleTypeOptional = BuiltInRegistries.PARTICLE_TYPE.getOptional(id);
                        if (particleTypeOptional.isPresent()) {
                            ParticleType<?> type = particleTypeOptional.get();
                            if (type instanceof ParticleOptions options) {
                                serverLevel.sendParticles(options, pos.x(), pos.y(), pos.z(), 20, 0.5, 0.5, 0.5, 0.05);
                            }
                        }
                    }
                } catch (Exception ignored) {}
                NodeLogicRegistry.triggerExec(node, NodePorts.EXEC, ctx);
            });

        // explosion (爆炸)
        NodeHelper.setup("explosion", "node.mgmc.explosion.name")
            .category("node_category.mgmc.action.world")
            .color(NodeThemes.COLOR_NODE_ACTION)
            .input(NodePorts.EXEC, "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .input(NodePorts.RADIUS, "node.mgmc.port.radius", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 3.0)
            .input(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.EXEC, "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .registerExec((node, ctx) -> {
                XYZ pos = TypeConverter.toXYZ(NodeLogicRegistry.evaluateInput(node, NodePorts.XYZ, ctx));
                float radius = (float) TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.RADIUS, ctx));
                if (radius <= 0) radius = 3.0f;

                try {
                    if (ctx.level != null) {
                        ctx.level.explode(null, pos.x(), pos.y(), pos.z(), radius, Level.ExplosionInteraction.TNT);
                    }
                } catch (Exception ignored) {}
                NodeLogicRegistry.triggerExec(node, NodePorts.EXEC, ctx);
            });

        // teleport_entity (传送实体)
        NodeHelper.setup("teleport_entity", "node.mgmc.teleport_entity.name")
            .category("node_category.mgmc.action.entity")
            .color(NodeThemes.COLOR_NODE_ACTION)
            .input(NodePorts.EXEC, "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .input(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .input(NodePorts.XYZ, "node.mgmc.port.xyz", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.EXEC, "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .registerExec((node, ctx) -> {
                Object entityObj = NodeLogicRegistry.evaluateInput(node, NodePorts.ENTITY, ctx);
                XYZ pos = TypeConverter.toXYZ(NodeLogicRegistry.evaluateInput(node, NodePorts.XYZ, ctx));
                
                Entity entity = null;
                if (entityObj instanceof Entity e) entity = e;
                else if (ctx.triggerEntity != null) entity = ctx.triggerEntity;

                if (entity != null && pos != null) {
                    entity.teleportTo(pos.x(), pos.y(), pos.z());
                }
                NodeLogicRegistry.triggerExec(node, NodePorts.EXEC, ctx);
            });

        // damage_entity (伤害实体)
        NodeHelper.setup("damage_entity", "node.mgmc.damage_entity.name")
            .category("node_category.mgmc.action.entity")
            .color(NodeThemes.COLOR_NODE_ACTION)
            .input(NodePorts.EXEC, "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .input(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .input(NodePorts.DAMAGE_AMOUNT, "node.mgmc.port.damage_amount", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 1.0)
            .output(NodePorts.EXEC, "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .registerExec((node, ctx) -> {
                Object entityObj = NodeLogicRegistry.evaluateInput(node, NodePorts.ENTITY, ctx);
                float amount = (float) TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.DAMAGE_AMOUNT, ctx));
                
                Entity entity = null;
                if (entityObj instanceof Entity e) entity = e;
                else if (ctx.triggerEntity != null) entity = ctx.triggerEntity;

                if (entity != null && amount > 0) {
                    if (entity.level() instanceof ServerLevel sl) {
                        entity.hurtServer(sl, entity.damageSources().generic(), amount);
                    }
                }
                NodeLogicRegistry.triggerExec(node, NodePorts.EXEC, ctx);
            });

        // heal_entity (治疗实体)
        NodeHelper.setup("heal_entity", "node.mgmc.heal_entity.name")
            .category("node_category.mgmc.action.entity")
            .color(NodeThemes.COLOR_NODE_ACTION)
            .input(NodePorts.EXEC, "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .input(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .input(NodePorts.HEAL_AMOUNT, "node.mgmc.port.heal_amount", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 1.0)
            .output(NodePorts.EXEC, "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .registerExec((node, ctx) -> {
                Object entityObj = NodeLogicRegistry.evaluateInput(node, NodePorts.ENTITY, ctx);
                float amount = (float) TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.HEAL_AMOUNT, ctx));
                
                LivingEntity entity = null;
                if (entityObj instanceof LivingEntity le) entity = le;
                else if (ctx.triggerEntity instanceof LivingEntity le) entity = le;

                if (entity != null && amount > 0) {
                    entity.heal(amount);
                }
                NodeLogicRegistry.triggerExec(node, NodePorts.EXEC, ctx);
            });

        // kill_entity (杀死实体)
        NodeHelper.setup("kill_entity", "node.mgmc.kill_entity.name")
            .category("node_category.mgmc.action.entity")
            .color(NodeThemes.COLOR_NODE_ACTION)
            .input(NodePorts.EXEC, "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .input(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .output(NodePorts.EXEC, "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .registerExec((node, ctx) -> {
                Object entityObj = NodeLogicRegistry.evaluateInput(node, NodePorts.ENTITY, ctx);
                
                Entity entity = null;
                if (entityObj instanceof Entity e) entity = e;
                else if (ctx.triggerEntity != null) entity = ctx.triggerEntity;

                if (entity != null) {
                    if (ctx.level instanceof ServerLevel sl) {
                        entity.kill(sl);
                    } else {
                        entity.discard();
                    }
                }
                NodeLogicRegistry.triggerExec(node, NodePorts.EXEC, ctx);
            });

        // set_entity_on_fire (设置实体着火)
        NodeHelper.setup("set_entity_on_fire", "node.mgmc.set_entity_on_fire.name")
            .category("node_category.mgmc.action.entity")
            .color(NodeThemes.COLOR_NODE_ACTION)
            .input(NodePorts.EXEC, "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .input(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .input(NodePorts.SECONDS, "node.mgmc.port.seconds", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 5.0)
            .output(NodePorts.EXEC, "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .registerExec((node, ctx) -> {
                Object entityObj = NodeLogicRegistry.evaluateInput(node, NodePorts.ENTITY, ctx);
                int seconds = (int) TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.SECONDS, ctx));
                
                Entity entity = null;
                if (entityObj instanceof Entity e) entity = e;
                else if (ctx.triggerEntity != null) entity = ctx.triggerEntity;

                if (entity != null && seconds > 0) {
                    entity.setRemainingFireTicks(seconds * 20);
                }
                NodeLogicRegistry.triggerExec(node, NodePorts.EXEC, ctx);
            });

        // add_potion_effect (添加药水效果)
        NodeHelper.setup("add_potion_effect", "node.mgmc.add_potion_effect.name")
            .category("node_category.mgmc.action.entity")
            .color(NodeThemes.COLOR_NODE_ACTION)
            .input(NodePorts.EXEC, "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .input(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .input(NodePorts.EFFECT, "node.mgmc.port.effect", NodeDefinition.PortType.STRING, NodeThemes.COLOR_PORT_STRING, "minecraft:speed")
            .input(NodePorts.DURATION, "node.mgmc.port.duration", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 10.0)
            .input(NodePorts.AMPLIFIER, "node.mgmc.port.amplifier", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 0.0)
            .input(NodePorts.SHOW_PARTICLES, "node.mgmc.port.show_particles", NodeDefinition.PortType.BOOLEAN, NodeThemes.COLOR_PORT_BOOLEAN, true)
            .output(NodePorts.EXEC, "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .registerExec((node, ctx) -> {
                Object entityObj = NodeLogicRegistry.evaluateInput(node, NodePorts.ENTITY, ctx);
                String effectName = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.EFFECT, ctx));
                int duration = (int) (TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.DURATION, ctx)) * 20);
                int amplifier = (int) TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.AMPLIFIER, ctx));
                boolean showParticles = TypeConverter.toBoolean(NodeLogicRegistry.evaluateInput(node, NodePorts.SHOW_PARTICLES, ctx));
                
                LivingEntity entity = null;
                if (entityObj instanceof LivingEntity le) entity = le;
                else if (ctx.triggerEntity instanceof LivingEntity le) entity = le;

                if (entity != null && effectName != null) {
                    try {
                        Optional<Holder.Reference<MobEffect>> holder = BuiltInRegistries.MOB_EFFECT.get(Identifier.parse(effectName));
                        if (holder.isPresent()) {
                            entity.addEffect(new MobEffectInstance(holder.get(), duration, amplifier, false, showParticles));
                        }
                    } catch (Exception ignored) {}
                }
                NodeLogicRegistry.triggerExec(node, NodePorts.EXEC, ctx);
            });

        // set_entity_velocity (设置实体速度)
        NodeHelper.setup("set_entity_velocity", "node.mgmc.set_entity_velocity.name")
            .category("node_category.mgmc.action.entity")
            .color(NodeThemes.COLOR_NODE_ACTION)
            .input(NodePorts.EXEC, "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .input(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .input(NodePorts.XYZ, "node.mgmc.port.velocity", NodeDefinition.PortType.XYZ, NodeThemes.COLOR_PORT_XYZ)
            .output(NodePorts.EXEC, "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .registerExec((node, ctx) -> {
                Object entityObj = NodeLogicRegistry.evaluateInput(node, NodePorts.ENTITY, ctx);
                XYZ vel = TypeConverter.toXYZ(NodeLogicRegistry.evaluateInput(node, NodePorts.XYZ, ctx));
                
                Entity entity = null;
                if (entityObj instanceof Entity e) entity = e;
                else if (ctx.triggerEntity != null) entity = ctx.triggerEntity;

                if (entity != null && vel != null) {
                    entity.setDeltaMovement(new Vec3(vel.x(), vel.y(), vel.z()));
                    entity.hurtMarked = true; // 确保同步到客户端
                }
                NodeLogicRegistry.triggerExec(node, NodePorts.EXEC, ctx);
            });

        // clear_potion_effects (清除所有药水效果)
        NodeHelper.setup("clear_potion_effects", "node.mgmc.clear_potion_effects.name")
            .category("node_category.mgmc.action.entity")
            .color(NodeThemes.COLOR_NODE_ACTION)
            .input(NodePorts.EXEC, "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .input(NodePorts.ENTITY, "node.mgmc.port.entity", NodeDefinition.PortType.ENTITY, NodeThemes.COLOR_PORT_ENTITY)
            .output(NodePorts.EXEC, "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .registerExec((node, ctx) -> {
                Object entityObj = NodeLogicRegistry.evaluateInput(node, NodePorts.ENTITY, ctx);
                
                LivingEntity entity = null;
                if (entityObj instanceof LivingEntity le) entity = le;
                else if (ctx.triggerEntity instanceof LivingEntity le) entity = le;

                if (entity != null) {
                    entity.removeAllEffects();
                }
                NodeLogicRegistry.triggerExec(node, NodePorts.EXEC, ctx);
            });
    }
}
