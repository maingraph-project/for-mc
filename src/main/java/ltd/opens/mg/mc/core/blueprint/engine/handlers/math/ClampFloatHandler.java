package ltd.opens.mg.mc.core.blueprint.engine.handlers.math;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;

public class ClampFloatHandler implements NodeHandler {
    @Override
    public String getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("result")) {
            try {
                double val = Double.parseDouble(NodeLogicRegistry.evaluateInput(node, "value", ctx));
                double min = Double.parseDouble(NodeLogicRegistry.evaluateInput(node, "min", ctx));
                double max = Double.parseDouble(NodeLogicRegistry.evaluateInput(node, "max", ctx));
                return String.valueOf(Math.max(min, Math.min(max, val)));
            } catch (Exception e) {}
        }
        return "0";
    }
}

