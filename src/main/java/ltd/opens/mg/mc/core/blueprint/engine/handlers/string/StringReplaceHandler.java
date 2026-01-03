package ltd.opens.mg.mc.core.blueprint.engine.handlers.string;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;

public class StringReplaceHandler implements NodeHandler {
    @Override
    public String getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("output")) {
            String s = NodeLogicRegistry.evaluateInput(node, "string", ctx);
            String oldVal = NodeLogicRegistry.evaluateInput(node, "old", ctx);
            String newVal = NodeLogicRegistry.evaluateInput(node, "new", ctx);
            if (s == null) return "";
            if (oldVal == null || oldVal.isEmpty()) return s;
            return s.replace(oldVal, newVal != null ? newVal : "");
        }
        return "";
    }
}

