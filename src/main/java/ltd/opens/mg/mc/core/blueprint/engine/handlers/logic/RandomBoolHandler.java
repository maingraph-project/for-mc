package ltd.opens.mg.mc.core.blueprint.engine.handlers.logic;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

import java.util.Random;

public class RandomBoolHandler implements NodeHandler {
    private static final Random RANDOM = new Random();

    @Override
    public Object getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("result")) {
            try {
                double chance = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "chance", ctx));
                boolean result = RANDOM.nextDouble() < chance;
                return result;
            } catch (Exception e) {}
        }
        return false;
    }
}



