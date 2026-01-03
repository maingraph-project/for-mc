package ltd.opens.mg.mc.core.blueprint.engine.handlers.logic;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;

public class BranchHandler implements NodeHandler {
    @Override
    public void execute(JsonObject node, NodeContext ctx) {
        String conditionStr = NodeLogicRegistry.evaluateInput(node, "condition", ctx);
        boolean condition = Boolean.parseBoolean(conditionStr);
        NodeLogicRegistry.triggerExec(node, condition ? "true" : "false", ctx);
    }
}

