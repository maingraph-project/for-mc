package ltd.opens.mg.mc.core.blueprint.engine.handlers.string;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

public class StringContainsHandler implements NodeHandler {
    @Override
    public Object getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("result")) {
            String s = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "string", ctx));
            String sub = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "substring", ctx));
            if (s == null || sub == null) return false;
            return s.contains(sub);
        }
        return false;
    }
}




