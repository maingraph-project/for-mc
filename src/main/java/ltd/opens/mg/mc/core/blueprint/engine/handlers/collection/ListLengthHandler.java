package ltd.opens.mg.mc.core.blueprint.engine.handlers.collection;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

import java.util.List;

public class ListLengthHandler implements NodeHandler {
    @Override
    public Object getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("length")) {
            try {
                List<Object> list = TypeConverter.toList(NodeLogicRegistry.evaluateInput(node, "list", ctx));
                return (double) list.size();
            } catch (Exception e) {
                return 0.0;
            }
        }
        return 0.0;
    }
}




