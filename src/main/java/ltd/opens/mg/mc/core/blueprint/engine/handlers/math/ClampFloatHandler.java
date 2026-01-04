package ltd.opens.mg.mc.core.blueprint.engine.handlers.math;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

public class ClampFloatHandler implements NodeHandler {
    @Override
    public Object getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("result")) {
            try {
                double val = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "value", ctx));
                double min = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "min", ctx));
                double max = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "max", ctx));
                return Math.max(min, Math.min(max, val));
            } catch (Exception e) {}
        }
        return 0.0;
    }
}




