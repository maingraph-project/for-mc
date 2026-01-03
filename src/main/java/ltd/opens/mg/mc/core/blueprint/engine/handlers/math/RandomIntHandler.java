package ltd.opens.mg.mc.core.blueprint.engine.handlers.math;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;

import java.util.Random;

public class RandomIntHandler implements NodeHandler {
    private static final Random RANDOM = new Random();

    @Override
    public String getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("result")) {
            try {
                int min = (int) Double.parseDouble(NodeLogicRegistry.evaluateInput(node, "min", ctx));
                int max = (int) Double.parseDouble(NodeLogicRegistry.evaluateInput(node, "max", ctx));
                if (max < min) {
                    int temp = max;
                    max = min;
                    min = temp;
                }
                // Random.nextInt(bound) is [0, bound)
                // We want [min, max] inclusive
                int range = max - min + 1;
                if (range <= 0) return String.valueOf(min);
                
                int randomValue = min + RANDOM.nextInt(range);
                return String.valueOf(randomValue);
            } catch (Exception e) {}
        }
        return "0";
    }
}

