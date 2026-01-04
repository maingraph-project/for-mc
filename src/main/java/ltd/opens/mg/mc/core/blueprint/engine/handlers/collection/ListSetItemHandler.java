package ltd.opens.mg.mc.core.blueprint.engine.handlers.collection;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

import java.util.ArrayList;
import java.util.List;

public class ListSetItemHandler implements NodeHandler {
    @Override
    public Object getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("list_out")) {
            List<Object> list = new ArrayList<>(TypeConverter.toList(NodeLogicRegistry.evaluateInput(node, "list_in", ctx)));
            int index = TypeConverter.toInt(NodeLogicRegistry.evaluateInput(node, "index", ctx));
            Object value = NodeLogicRegistry.evaluateInput(node, "value", ctx);
            
            try {
                if (index >= 0 && index < list.size()) {
                    list.set(index, value);
                } else if (index == list.size()) {
                    list.add(value);
                }
                return list;
            } catch (Exception e) {
                return list;
            }
        }
        return null;
    }
}




