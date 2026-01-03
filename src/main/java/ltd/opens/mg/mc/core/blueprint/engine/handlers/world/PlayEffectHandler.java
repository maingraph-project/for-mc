package ltd.opens.mg.mc.core.blueprint.engine.handlers.world;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import net.minecraft.server.level.ServerLevel;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;

public class PlayEffectHandler implements NodeHandler {
    @Override
    public void execute(JsonObject node, NodeContext ctx) {
        String effectName = NodeLogicRegistry.evaluateInput(node, "effect", ctx);
        String xStr = NodeLogicRegistry.evaluateInput(node, "x", ctx);
        String yStr = NodeLogicRegistry.evaluateInput(node, "y", ctx);
        String zStr = NodeLogicRegistry.evaluateInput(node, "z", ctx);

        try {
            double x = xStr.isEmpty() ? 0 : Double.parseDouble(xStr);
            double y = yStr.isEmpty() ? 0 : Double.parseDouble(yStr);
            double z = zStr.isEmpty() ? 0 : Double.parseDouble(zStr);

            if (ctx.level instanceof ServerLevel serverLevel) {
                Identifier id = Identifier.parse(effectName);
                
                var particleTypeOptional = BuiltInRegistries.PARTICLE_TYPE.getOptional(id);
                if (particleTypeOptional.isPresent()) {
                    ParticleType<?> type = particleTypeOptional.get();
                    if (type instanceof ParticleOptions options) {
                        serverLevel.sendParticles(options, x, y, z, 20, 0.5, 0.5, 0.5, 0.05);
                    }
                }
            }
        } catch (Exception e) {
            // Ignore errors
        }

        NodeLogicRegistry.triggerExec(node, "exec", ctx);
    }
}

