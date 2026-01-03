package ltd.opens.mg.mc.core.blueprint.engine.handlers.world;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import net.minecraft.world.level.Level;

public class ExplosionHandler implements NodeHandler {
    @Override
    public void execute(JsonObject node, NodeContext ctx) {
        String xStr = NodeLogicRegistry.evaluateInput(node, "x", ctx);
        String yStr = NodeLogicRegistry.evaluateInput(node, "y", ctx);
        String zStr = NodeLogicRegistry.evaluateInput(node, "z", ctx);
        String radiusStr = NodeLogicRegistry.evaluateInput(node, "radius", ctx);

        try {
            double x = xStr.isEmpty() ? 0 : Double.parseDouble(xStr);
            double y = yStr.isEmpty() ? 0 : Double.parseDouble(yStr);
            double z = zStr.isEmpty() ? 0 : Double.parseDouble(zStr);
            float radius = radiusStr.isEmpty() ? 3.0f : Float.parseFloat(radiusStr);

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

