package ltd.opens.mg.mc.core.blueprint.engine.handlers.collection;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListSetItemHandler implements NodeHandler {
    @Override
    public String getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("list_out")) {
            String listStr = NodeLogicRegistry.evaluateInput(node, "list_in", ctx);
            String indexStr = NodeLogicRegistry.evaluateInput(node, "index", ctx);
            String value = NodeLogicRegistry.evaluateInput(node, "value", ctx);
            
            if (listStr == null || listStr.isEmpty()) return value;
            
            try {
                String[] items = listStr.split("\\|");
                int index = (int) Double.parseDouble(indexStr);
                
                List<String> list = new ArrayList<>(Arrays.asList(items));
                if (index >= 0 && index < list.size()) {
                    list.set(index, value);
                } else if (index == list.size()) {
                    list.add(value);
                }
                return String.join("|", list);
            } catch (Exception e) {
                return listStr;
            }
        }
        return "";
    }
}

