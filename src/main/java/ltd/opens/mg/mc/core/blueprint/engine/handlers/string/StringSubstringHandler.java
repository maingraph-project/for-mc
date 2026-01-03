package ltd.opens.mg.mc.core.blueprint.engine.handlers.string;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;

public class StringSubstringHandler implements NodeHandler {
    @Override
    public String getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("output")) {
            try {
                String s = NodeLogicRegistry.evaluateInput(node, "string", ctx);
                String startStr = NodeLogicRegistry.evaluateInput(node, "start", ctx);
                String endStr = NodeLogicRegistry.evaluateInput(node, "end", ctx);
                
                if (s == null || s.isEmpty()) return "";
                int start = (int) Double.parseDouble(startStr);
                int end = (int) Double.parseDouble(endStr);
                
                // 边界处理
                start = Math.max(0, Math.min(start, s.length()));
                end = Math.max(start, Math.min(end, s.length()));
                
                return s.substring(start, end);
            } catch (Exception e) {
                return "";
            }
        }
        return "";
    }
}

