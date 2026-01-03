package ltd.opens.mg.mc.core.blueprint.engine.handlers.collection;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;

public class GetListItemHandler implements NodeHandler {
    @Override
    public String getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("value")) {
            try {
                String listStr = NodeLogicRegistry.evaluateInput(node, "list", ctx);
                String indexStr = NodeLogicRegistry.evaluateInput(node, "index", ctx);
                
                if (listStr == null || listStr.isEmpty()) return "";
                
                String[] items = listStr.split("\\|");
                int index = (int) Double.parseDouble(indexStr);
                
                if (index >= 0 && index < items.length) {
                    return items[index];
                }
            } catch (Exception e) {}
        }
        return "";
    }
}

