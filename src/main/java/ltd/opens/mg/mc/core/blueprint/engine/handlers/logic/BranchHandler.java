package ltd.opens.mg.mc.core.blueprint.engine.handlers.logic;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

public class BranchHandler implements NodeHandler {
    @Override
    public void execute(JsonObject node, NodeContext ctx) {
        boolean condition = TypeConverter.toBoolean(NodeLogicRegistry.evaluateInput(node, "condition", ctx));
        NodeLogicRegistry.triggerExec(node, condition ? "true" : "false", ctx);
    }
}


