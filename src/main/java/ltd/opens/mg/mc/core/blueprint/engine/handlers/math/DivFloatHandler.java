package ltd.opens.mg.mc.core.blueprint.engine.handlers.math;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

public class DivFloatHandler implements NodeHandler {
    @Override
    public Object getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("result")) {
            try {
                double a = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "a", ctx));
                double b = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "b", ctx));
                if (b == 0) return 0.0;
                return a / b;
            } catch (Exception e) {}
        }
        return 0.0;
    }
}




