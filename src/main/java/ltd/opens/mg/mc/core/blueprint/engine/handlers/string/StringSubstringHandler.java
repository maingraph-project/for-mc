package ltd.opens.mg.mc.core.blueprint.engine.handlers.string;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

public class StringSubstringHandler implements NodeHandler {
    @Override
    public Object getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("output")) {
            try {
                String s = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "string", ctx));
                int start = TypeConverter.toInt(NodeLogicRegistry.evaluateInput(node, "start", ctx));
                int end = TypeConverter.toInt(NodeLogicRegistry.evaluateInput(node, "end", ctx));
                
                if (s == null || s.isEmpty()) return null;
                
                // 边界处理
                start = Math.max(0, Math.min(start, s.length()));
                end = Math.max(start, Math.min(end, s.length()));
                
                return s.substring(start, end);
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}



