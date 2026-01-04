package ltd.opens.mg.mc.core.blueprint.engine.handlers.string;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

public class StringReplaceHandler implements NodeHandler {
    @Override
    public Object getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("output")) {
            String s = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "string", ctx));
            String oldVal = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "old", ctx));
            String newVal = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "new", ctx));
            if (s == null) return null;
            if (oldVal == null || oldVal.isEmpty()) return s;
            return s.replace(oldVal, newVal != null ? newVal : "");
        }
        return null;
    }
}



