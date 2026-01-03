package ltd.opens.mg.mc.core.blueprint.engine.handlers.logic;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;

import java.util.Random;

public class RandomBoolHandler implements NodeHandler {
    private static final Random RANDOM = new Random();

    @Override
    public String getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("result")) {
            try {
                double chance = Double.parseDouble(NodeLogicRegistry.evaluateInput(node, "chance", ctx));
                boolean result = RANDOM.nextDouble() < chance;
                return String.valueOf(result);
            } catch (Exception e) {}
        }
        return "false";
    }
}

