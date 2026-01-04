package ltd.opens.mg.mc.core.blueprint.engine.handlers.math;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

public class AddFloatHandler implements NodeHandler {
    @Override
    public Object getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("result")) {
            double a = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "a", ctx));
            double b = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "b", ctx));
            return a + b;
        }
        return 0.0;
    }
}




