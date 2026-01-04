package ltd.opens.mg.mc.core.blueprint.engine.handlers.string;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

import java.util.ArrayList;
import java.util.List;

public class StringCombineHandler implements NodeHandler {
    @Override
    public Object getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("output")) {
            StringBuilder sb = new StringBuilder();
            if (node.has("inputs")) {
                JsonObject inputs = node.getAsJsonObject("inputs");
                
                // Get all input IDs and sort them to maintain order
                // Dynamic inputs are usually added as input_0, input_1, etc.
                List<String> keys = new ArrayList<>(inputs.keySet());
                keys.sort((a, b) -> {
                    // Try to sort numerically if they end with numbers
                    try {
                        int numA = extractNumber(a);
                        int numB = extractNumber(b);
                        return Integer.compare(numA, numB);
                    } catch (Exception e) {
                        return a.compareTo(b);
                    }
                });

                for (String key : keys) {
                    // Skip exec if it exists (though string_combine shouldn't have one)
                    if (key.equals("exec")) continue;
                    
                    String val = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, key, ctx));
                    if (val != null) {
                        sb.append(val);
                    }
                }
            }
            return sb.toString();
        }
        return null;
    }

    private int extractNumber(String s) {
        String num = s.replaceAll("\\D", "");
        return num.isEmpty() ? 0 : Integer.parseInt(num);
    }
}



