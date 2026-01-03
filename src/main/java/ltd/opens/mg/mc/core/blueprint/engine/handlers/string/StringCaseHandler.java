package ltd.opens.mg.mc.core.blueprint.engine.handlers.string;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;

public class StringCaseHandler implements NodeHandler {
    @Override
    public String getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("output")) {
            String s = NodeLogicRegistry.evaluateInput(node, "string", ctx);
            String mode = NodeLogicRegistry.evaluateInput(node, "mode", ctx);
            if (s == null) return "";
            if ("UPPER".equalsIgnoreCase(mode)) return s.toUpperCase();
            if ("LOWER".equalsIgnoreCase(mode)) return s.toLowerCase();
            if ("TRIM".equalsIgnoreCase(mode)) return s.trim();
            return s;
        }
        return "";
    }
}

