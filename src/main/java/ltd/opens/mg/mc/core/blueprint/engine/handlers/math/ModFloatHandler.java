package ltd.opens.mg.mc.core.blueprint.engine.handlers.math;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;

public class ModFloatHandler implements NodeHandler {
    @Override
    public String getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("result")) {
            try {
                double a = Double.parseDouble(NodeLogicRegistry.evaluateInput(node, "a", ctx));
                double b = Double.parseDouble(NodeLogicRegistry.evaluateInput(node, "b", ctx));
                if (b == 0) return "0";
                return String.valueOf(a % b);
            } catch (Exception e) {}
        }
        return "0";
    }
}

