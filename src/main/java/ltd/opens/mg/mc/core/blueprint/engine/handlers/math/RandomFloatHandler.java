package ltd.opens.mg.mc.core.blueprint.engine.handlers.math;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;

import java.util.Random;

public class RandomFloatHandler implements NodeHandler {
    private static final Random RANDOM = new Random();

    @Override
    public String getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("result")) {
            try {
                double min = Double.parseDouble(NodeLogicRegistry.evaluateInput(node, "min", ctx));
                double max = Double.parseDouble(NodeLogicRegistry.evaluateInput(node, "max", ctx));
                if (max < min) {
                    double temp = max;
                    max = min;
                    min = temp;
                }
                double randomValue = min + (max - min) * RANDOM.nextDouble();
                return String.valueOf(randomValue);
            } catch (Exception e) {}
        }
        return "0.0";
    }
}

