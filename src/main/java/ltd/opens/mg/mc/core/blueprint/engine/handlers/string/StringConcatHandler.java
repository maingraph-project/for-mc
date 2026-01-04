package ltd.opens.mg.mc.core.blueprint.engine.handlers.string;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

public class StringConcatHandler implements NodeHandler {
    @Override
    public Object getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("output")) {
            String a = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "a", ctx));
            String b = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "b", ctx));
            return (a != null ? a : "") + (b != null ? b : "");
        }
        return null;
    }
}



