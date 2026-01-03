package ltd.opens.mg.mc.core.blueprint.engine.handlers.collection;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;

public class ListAddHandler implements NodeHandler {
    @Override
    public String getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("list_out")) {
            String listStr = NodeLogicRegistry.evaluateInput(node, "list_in", ctx);
            String item = NodeLogicRegistry.evaluateInput(node, "item", ctx);
            
            if (item == null) item = "";
            if (listStr == null || listStr.isEmpty()) {
                return item;
            }
            // 避免添加空值导致列表格式混�?            if (item.contains("|")) {
                item = "\"" + item + "\""; // 简单转义处�?            }
            return listStr + "|" + item;
        }
        return "";
    }
}

