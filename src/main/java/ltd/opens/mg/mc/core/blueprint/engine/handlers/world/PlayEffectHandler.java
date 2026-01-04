package ltd.opens.mg.mc.core.blueprint.engine.handlers.world;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;
import net.minecraft.server.level.ServerLevel;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.BuiltInRegistries;

public class PlayEffectHandler implements NodeHandler {
    @Override
    public void execute(JsonObject node, NodeContext ctx) {
        String effectName = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "effect", ctx));
        double x = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "x", ctx));
        double y = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "y", ctx));
        double z = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "z", ctx));

        try {
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



