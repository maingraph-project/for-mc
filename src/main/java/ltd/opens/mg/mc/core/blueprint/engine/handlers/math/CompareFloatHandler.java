package ltd.opens.mg.mc.core.blueprint.engine.handlers.math;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

public class CompareFloatHandler implements NodeHandler {
    private final String mode;

    public CompareFloatHandler(String mode) {
        this.mode = mode;
    }

    @Override
    public Object getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("result")) {
            try {
                double a = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "a", ctx));
                double b = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "b", ctx));
                boolean res = false;
                switch (mode) {
                    case "eq": res = Math.abs(a - b) < 1e-9; break;
                    case "neq": res = Math.abs(a - b) >= 1e-9; break;
                    case "gt": res = a > b; break;
                    case "gte": res = a >= b; break;
                    case "lt": res = a < b; break;
                    case "lte": res = a <= b; break;
                }
                return res;
            } catch (Exception e) {}
        }
        return false;
    }
}





