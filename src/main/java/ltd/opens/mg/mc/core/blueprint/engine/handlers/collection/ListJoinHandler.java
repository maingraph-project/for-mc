package ltd.opens.mg.mc.core.blueprint.engine.handlers.collection;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;

public class ListJoinHandler implements NodeHandler {
    @Override
    public String getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("string")) {
            String listStr = NodeLogicRegistry.evaluateInput(node, "list", ctx);
            String delim = NodeLogicRegistry.evaluateInput(node, "delimiter", ctx);
            
            if (listStr == null || listStr.isEmpty()) return "";
            if (delim == null) delim = "";
            
            String[] items = listStr.split("\\|");
            return String.join(delim, items);
        }
        return "";
    }
}

