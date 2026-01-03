package ltd.opens.mg.mc.core.blueprint.engine.handlers.variable;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;

public class GetVariableHandler implements NodeHandler {
    @Override
    public String getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("value")) {
            try {
                String name = NodeLogicRegistry.evaluateInput(node, "name", ctx);
                if (name == null || name.trim().isEmpty()) return "";
                return ctx.variables.getOrDefault(name.trim(), "");
            } catch (Exception e) {
                return "";
            }
        }
        return "";
    }
}

