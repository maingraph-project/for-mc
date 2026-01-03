package ltd.opens.mg.mc.core.blueprint.engine.handlers.string;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;

public class StringLengthHandler implements NodeHandler {
    @Override
    public String getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("length")) {
            String s = NodeLogicRegistry.evaluateInput(node, "string", ctx);
            return s != null ? String.valueOf((float) s.length()) : "0.0";
        }
        return "0.0";
    }
}

