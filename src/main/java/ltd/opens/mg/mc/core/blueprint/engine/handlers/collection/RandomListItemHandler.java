package ltd.opens.mg.mc.core.blueprint.engine.handlers.collection;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;

import java.util.Random;

public class RandomListItemHandler implements NodeHandler {
    private static final Random RANDOM = new Random();

    @Override
    public String getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("item")) {
            try {
                String listStr = NodeLogicRegistry.evaluateInput(node, "list", ctx);
                if (listStr == null || listStr.isEmpty()) return "";
                
                String[] items = listStr.split("\\|");
                if (items.length > 0) {
                    return items[RANDOM.nextInt(items.length)];
                }
            } catch (Exception e) {}
        }
        return "";
    }
}

