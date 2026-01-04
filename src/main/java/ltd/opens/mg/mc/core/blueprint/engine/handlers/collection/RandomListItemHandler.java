package ltd.opens.mg.mc.core.blueprint.engine.handlers.collection;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

import java.util.List;
import java.util.Random;

public class RandomListItemHandler implements NodeHandler {
    private static final Random RANDOM = new Random();

    @Override
    public Object getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("item")) {
            try {
                List<Object> list = TypeConverter.toList(NodeLogicRegistry.evaluateInput(node, "list", ctx));
                if (!list.isEmpty()) {
                    return list.get(RANDOM.nextInt(list.size()));
                }
            } catch (Exception e) {}
        }
        return null;
    }
}




