package ltd.opens.mg.mc.core.blueprint.engine.handlers.collection;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

import java.util.List;
import java.util.stream.Collectors;

public class ListJoinHandler implements NodeHandler {
    @Override
    public Object getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("string")) {
            List<Object> list = TypeConverter.toList(NodeLogicRegistry.evaluateInput(node, "list", ctx));
            String delim = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "delimiter", ctx));
            
            if (delim == null) delim = "";
            
            return list.stream()
                    .map(TypeConverter::toString)
                    .collect(Collectors.joining(delim));
        }
        return null;
    }
}




