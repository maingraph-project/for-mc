package ltd.opens.mg.mc.core.blueprint.engine.handlers.collection;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;

public class ListLengthHandler implements NodeHandler {
    @Override
    public String getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("length")) {
            try {
                String listStr = NodeLogicRegistry.evaluateInput(node, "list", ctx);
                if (listStr == null || listStr.isEmpty()) return "0";
                
                // 使用正则表达式分割，并处理可能的空项
                String[] items = listStr.split("\\|", -1);
                return String.valueOf((float) items.length);
            } catch (Exception e) {
                return "0.0";
            }
        }
        return "0.0";
    }
}

