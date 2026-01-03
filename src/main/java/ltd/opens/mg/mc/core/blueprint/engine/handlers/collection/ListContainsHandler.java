package ltd.opens.mg.mc.core.blueprint.engine.handlers.collection;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;

public class ListContainsHandler implements NodeHandler {
    @Override
    public String getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("result")) {
            String listStr = NodeLogicRegistry.evaluateInput(node, "list", ctx);
            String item = NodeLogicRegistry.evaluateInput(node, "item", ctx);
            
            if (listStr == null || listStr.isEmpty()) return "false";
            
            String[] items = listStr.split("\\|");
            for (String s : items) {
                if (s.equals(item)) return "true";
            }
            return "false";
        }
        return "false";
    }
}

