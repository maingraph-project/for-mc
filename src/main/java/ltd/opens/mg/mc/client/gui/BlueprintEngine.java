package ltd.opens.mg.mc.client.gui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ltd.opens.mg.mc.MaingraphforMC;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;

import java.util.HashMap;
import java.util.Map;

public class BlueprintEngine {

    public static void execute(String json, String eventType, String name, String[] args, 
                                String triggerUuid, String triggerName, double tx, double ty, double tz) {
        try {
            MaingraphforMC.LOGGER.info("Executing blueprint: {} for event {}", name, eventType);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            
            if (!root.has("execution") || !root.get("execution").isJsonArray()) {
                MaingraphforMC.LOGGER.warn("Missing 'execution' array in blueprint JSON");
                return;
            }

            JsonArray executionNodes = root.getAsJsonArray("execution");
            Map<String, JsonObject> nodesMap = new HashMap<>();
            
            for (JsonElement e : executionNodes) {
                if (!e.isJsonObject()) continue;
                JsonObject node = e.getAsJsonObject();
                if (node.has("id")) {
                    nodesMap.put(node.get("id").getAsString(), node);
                }
            }

            NodeContext ctx = new NodeContext(name, args, triggerUuid, triggerName, tx, ty, tz, nodesMap);

            for (JsonElement e : executionNodes) {
                if (!e.isJsonObject()) continue;
                JsonObject node = e.getAsJsonObject();
                String type = node.has("type") ? node.get("type").getAsString() : null;
                
                if (type != null && type.equals(eventType)) {
                    // Check if the 'name' output matches the requested name
                    String nodeName = NodeLogicRegistry.evaluateOutput(node, "name", ctx);
                    if (name.isEmpty() || name.equals(nodeName)) {
                        NodeLogicRegistry.triggerExec(node, "exec", ctx);
                    }
                }
            }
        } catch (Exception e) {
            MaingraphforMC.LOGGER.error("Error executing blueprint", e);
        }
    }
}
