package ltd.opens.mg.mc.core.blueprint.engine.handlers.world;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;
import net.minecraft.world.level.Level;

public class ExplosionHandler implements NodeHandler {
    @Override
    public void execute(JsonObject node, NodeContext ctx) {
        double x = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "x", ctx));
        double y = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "y", ctx));
        double z = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "z", ctx));
        float radius = (float) TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "radius", ctx));
        if (radius == 0) radius = 3.0f;

        try {
            if (ctx.level != null) {
                // Server-side explosion
                ctx.level.explode(null, x, y, z, radius, Level.ExplosionInteraction.TNT);
            }
        } catch (Exception e) {
            // Ignore errors
        }

        NodeLogicRegistry.triggerExec(node, "exec", ctx);
    }
}



