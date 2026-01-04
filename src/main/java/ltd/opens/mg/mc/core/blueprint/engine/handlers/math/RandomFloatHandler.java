package ltd.opens.mg.mc.core.blueprint.engine.handlers.math;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

import java.util.Random;

public class RandomFloatHandler implements NodeHandler {
    private static final Random RANDOM = new Random();

    @Override
    public Object getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("result")) {
            try {
                double min = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "min", ctx));
                double max = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, "max", ctx));
                if (max < min) {
                    double temp = max;
                    max = min;
                    min = temp;
                }
                double randomValue = min + (max - min) * RANDOM.nextDouble();
                return randomValue;
            } catch (Exception e) {}
        }
        return 0.0;
    }
}





