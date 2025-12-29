package ltd.opens.mg.mc.client.gui;

import com.google.gson.*;
import ltd.opens.mg.mc.MaingraphforMC;
import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeRegistry;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BlueprintIO {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static void save(Path dataFile, List<GuiNode> nodes, List<GuiConnection> connections) {
        try {
            JsonObject root = new JsonObject();
            
            // Execution data for BlueprintEngine
            JsonArray execution = new JsonArray();
            for (GuiNode node : nodes) {
                JsonObject nodeObj = new JsonObject();
                nodeObj.addProperty("id", node.id);
                nodeObj.addProperty("type", node.typeId);
                
                JsonObject inputs = new JsonObject();
                for (NodeDefinition.PortDefinition port : node.definition.inputs()) {
                    GuiConnection conn = findConnectionTo(node, port.name(), connections);
                    JsonObject input = new JsonObject();
                    if (conn != null) {
                        input.addProperty("type", "link");
                        input.addProperty("nodeId", conn.from.id);
                        input.addProperty("socket", conn.fromPort);
                    } else {
                        input.addProperty("type", "value");
                        JsonElement val = node.inputValues.get(port.name());
                        input.add("value", val != null ? val : GSON.toJsonTree(port.defaultValue()));
                    }
                    inputs.add(port.name(), input);
                }
                nodeObj.add("inputs", inputs);

                JsonObject outputs = new JsonObject();
                for (NodeDefinition.PortDefinition port : node.definition.outputs()) {
                    JsonArray targets = new JsonArray();
                    for (GuiConnection conn : connections) {
                        if (conn.from == node && conn.fromPort.equals(port.name())) {
                            JsonObject target = new JsonObject();
                            target.addProperty("nodeId", conn.to.id);
                            target.addProperty("socket", conn.toPort);
                            targets.add(target);
                        }
                    }
                    outputs.add(port.name(), targets);
                }
                nodeObj.add("outputs", outputs);
                execution.add(nodeObj);
            }
            root.add("execution", execution);

            // UI data for restoration
            JsonObject ui = new JsonObject();
            JsonArray nodesArray = new JsonArray();
            for (GuiNode node : nodes) {
                JsonObject nodeObj = new JsonObject();
                nodeObj.addProperty("id", node.id);
                nodeObj.addProperty("defId", node.typeId);
                nodeObj.addProperty("x", node.x);
                nodeObj.addProperty("y", node.y);
                nodeObj.add("inputValues", node.inputValues);
                nodesArray.add(nodeObj);
            }
            ui.add("nodes", nodesArray);

            JsonArray connArray = new JsonArray();
            for (GuiConnection conn : connections) {
                JsonObject connObj = new JsonObject();
                connObj.addProperty("fromNode", conn.from.id);
                connObj.addProperty("fromSocket", conn.fromPort);
                connObj.addProperty("toNode", conn.to.id);
                connObj.addProperty("toSocket", conn.toPort);
                connArray.add(connObj);
            }
            ui.add("connections", connArray);
            root.add("ui", ui);

            String json = GSON.toJson(root);
            Files.writeString(dataFile, json);
            MaingraphforMC.LOGGER.info("Saved blueprint to {}", dataFile.toAbsolutePath());
        } catch (Exception e) {
            MaingraphforMC.LOGGER.error("Failed to save blueprint", e);
        }
    }

    private static GuiConnection findConnectionTo(GuiNode node, String portName, List<GuiConnection> connections) {
        for (GuiConnection conn : connections) {
            if (conn.to == node && conn.toPort.equals(portName)) {
                return conn;
            }
        }
        return null;
    }

    public static void load(Path dataFile, List<GuiNode> nodes, List<GuiConnection> connections) {
        try {
            if (!Files.exists(dataFile)) return;
            String json = Files.readString(dataFile);
            if (json == null || json.isEmpty()) return;
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            
            nodes.clear();
            connections.clear();
            
            Map<String, GuiNode> nodeMap = new HashMap<>();
            
            JsonObject ui = root.has("ui") ? root.getAsJsonObject("ui") : root;

            if (ui.has("nodes")) {
                for (JsonElement e : ui.getAsJsonArray("nodes")) {
                    JsonObject obj = e.getAsJsonObject();
                    String type = obj.has("defId") ? obj.get("defId").getAsString() : obj.get("type").getAsString();
                    NodeDefinition def = NodeRegistry.get(type);
                    if (def != null) {
                        GuiNode node = new GuiNode(def, obj.get("x").getAsFloat(), obj.get("y").getAsFloat());
                        node.id = obj.get("id").getAsString();
                        if (obj.has("inputValues")) {
                            node.inputValues = obj.getAsJsonObject("inputValues");
                        }
                        nodes.add(node);
                        nodeMap.put(node.id, node);
                    }
                }
            }
            
            if (ui.has("connections")) {
                for (JsonElement e : ui.getAsJsonArray("connections")) {
                    JsonObject obj = e.getAsJsonObject();
                    String fromId = obj.has("fromNode") ? obj.get("fromNode").getAsString() : obj.get("from").getAsString();
                    String toId = obj.has("toNode") ? obj.get("toNode").getAsString() : obj.get("to").getAsString();
                    String fromPort = obj.has("fromSocket") ? obj.get("fromSocket").getAsString() : obj.get("fromPort").getAsString();
                    String toPort = obj.has("toSocket") ? obj.get("toSocket").getAsString() : obj.get("toPort").getAsString();
                    
                    GuiNode from = nodeMap.get(fromId);
                    GuiNode to = nodeMap.get(toId);
                    if (from != null && to != null) {
                        connections.add(new GuiConnection(from, fromPort, to, toPort));
                    }
                }
            }
        } catch (Exception e) {
            MaingraphforMC.LOGGER.error("Failed to load blueprint", e);
        }
    }
}
