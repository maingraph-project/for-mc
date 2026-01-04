package ltd.opens.mg.mc.core.blueprint.engine.handlers.string;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.MaingraphforMC;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

public class PrintStringHandler implements NodeHandler {
    @Override
    public void execute(JsonObject node, NodeContext ctx) {
        String message = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "in_string", ctx));
        MaingraphforMC.LOGGER.info("[Blueprint] {}", message);
        NodeLogicRegistry.triggerExec(node, "exec", ctx);
    }
}



