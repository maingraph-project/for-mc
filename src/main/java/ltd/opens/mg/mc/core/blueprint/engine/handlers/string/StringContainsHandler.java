package ltd.opens.mg.mc.core.blueprint.engine.handlers.string;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;

public class StringContainsHandler implements NodeHandler {
    @Override
    public String getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("result")) {
            String s = NodeLogicRegistry.evaluateInput(node, "string", ctx);
            String sub = NodeLogicRegistry.evaluateInput(node, "substring", ctx);
            if (s == null || sub == null) return "false";
            return String.valueOf(s.contains(sub));
        }
        return "false";
    }
}

