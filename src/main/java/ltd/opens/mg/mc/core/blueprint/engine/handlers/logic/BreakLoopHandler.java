package ltd.opens.mg.mc.core.blueprint.engine.handlers.logic;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;

public class BreakLoopHandler implements NodeHandler {
    @Override
    public void execute(JsonObject node, NodeContext ctx) {
        ctx.breakRequested = true;
    }
}


