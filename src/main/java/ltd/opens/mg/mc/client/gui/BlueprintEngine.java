package ltd.opens.mg.mc.client.gui;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ltd.opens.mg.mc.MaingraphforMC;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlueprintEngine {
    private static Map<String, JsonObject> nodesMap = new HashMap<>();

    public static void execute(String json, String eventType, String name, String[] args) {
        try {
            MaingraphforMC.LOGGER.info("Executing blueprint: {} for event {}", name, eventType);
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            
            if (!root.has("execution") || !root.get("execution").isJsonArray()) {
                MaingraphforMC.LOGGER.warn("Missing 'execution' array in blueprint JSON");
                return;
            }

            JsonArray executionNodes = root.getAsJsonArray("execution");
            nodesMap.clear();
            
            for (JsonElement e : executionNodes) {
                if (!e.isJsonObject()) continue;
                JsonObject node = e.getAsJsonObject();
                if (node.has("id")) {
                    nodesMap.put(node.get("id").getAsString(), node);
                }
            }

            for (JsonElement e : executionNodes) {
                if (!e.isJsonObject()) continue;
                JsonObject node = e.getAsJsonObject();
                if (node.has("type") && node.get("type").getAsString().equals(eventType)) {
                    // Check if the 'name' output matches the requested name
                    String nodeName = evaluateOutput(node, "name", name, args);
                    if (name.isEmpty() || name.equals(nodeName)) {
                        executeExec(node, "exec", name, args);
                    }
                }
            }
        } catch (Exception e) {
            MaingraphforMC.LOGGER.error("Error executing blueprint", e);
        }
    }

    private static void processNode(JsonObject node, String name, String[] args) {
        String type = getString(node, "type");
        if (type == null) return;
        
        if (type.equals("print_chat")) {
            String message = evaluateInput(node, "message", name, args);
            Minecraft.getInstance().gui.getChat().addMessage(Component.literal(message));
            executeExec(node, "exec", name, args);
        } else if (type.equals("branch")) {
            String conditionStr = evaluateInput(node, "condition", name, args);
            boolean condition = Boolean.parseBoolean(conditionStr);
            executeExec(node, condition ? "true" : "false", name, args);
        } else if (type.equals("print_string")) {
            String message = evaluateInput(node, "in_string", name, args);
            MaingraphforMC.LOGGER.info("[Blueprint] {}", message);
            executeExec(node, "exec", name, args);
        }
    }

    private static void executeExec(JsonObject node, String pinId, String name, String[] args) {
        if (!node.has("outputs")) return;
        JsonObject outputs = node.getAsJsonObject("outputs");
        if (!outputs.has(pinId)) return;
        
        JsonArray targets = outputs.getAsJsonArray(pinId);
        for (JsonElement t : targets) {
            JsonObject target = t.getAsJsonObject();
            String targetId = target.get("nodeId").getAsString();
            JsonObject targetNode = nodesMap.get(targetId);
            if (targetNode != null) {
                processNode(targetNode, name, args);
            }
        }
    }

    private static String evaluateInput(JsonObject node, String pinId, String name, String[] args) {
        if (!node.has("inputs")) return "";
        JsonObject inputs = node.getAsJsonObject("inputs");
        if (!inputs.has(pinId)) return "";
        
        JsonObject input = inputs.getAsJsonObject(pinId);
        String type = input.get("type").getAsString();
        
        if (type.equals("value")) {
            return input.has("value") ? input.get("value").getAsString() : "";
        } else if (type.equals("link")) {
            String sourceId = input.get("nodeId").getAsString();
            String sourceSocket = input.get("socket").getAsString();
            JsonObject sourceNode = nodesMap.get(sourceId);
            if (sourceNode != null) {
                return evaluateOutput(sourceNode, sourceSocket, name, args);
            }
        }
        return "";
    }

    private static String evaluateOutput(JsonObject node, String pinId, String name, String[] args) {
        String type = getString(node, "type");
        if (type == null) return "";

        if (type.equals("on_mgrun")) {
            if (pinId.equals("name")) return name;
        } else if (type.equals("get_arg")) {
            if (pinId.equals("value")) {
                try {
                    String indexStr = evaluateInput(node, "index", name, args);
                    int index = (int) Double.parseDouble(indexStr);
                    if (index >= 0 && index < args.length) {
                        return args[index];
                    }
                } catch (Exception e) { }
                return "";
            }
        } else if (type.equals("player_health")) {
            if (pinId.equals("value")) {
                if (Minecraft.getInstance().player != null) return String.valueOf(Minecraft.getInstance().player.getHealth());
            }
        } else if (type.equals("add_float")) {
            if (pinId.equals("result")) {
                try {
                    double a = Double.parseDouble(evaluateInput(node, "a", name, args));
                    double b = Double.parseDouble(evaluateInput(node, "b", name, args));
                    return String.valueOf(a + b);
                } catch (Exception e) { }
                return "0";
            }
        }
        return "";
    }

    private static String getString(JsonObject obj, String... keys) {
        for (String key : keys) {
            if (obj.has(key) && !obj.get(key).isJsonNull()) {
                return obj.get(key).getAsString();
            }
        }
        return null;
    }
}
