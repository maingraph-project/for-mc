package ltd.opens.mg.mc.client.gui.blueprint.io;


import ltd.opens.mg.mc.client.gui.components.*;
import com.google.gson.*;
import ltd.opens.mg.mc.MaingraphforMC;
import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeRegistry;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class BlueprintIO {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static String serialize(List<GuiNode> nodes, List<GuiConnection> connections) {
        try {
            JsonObject root = new JsonObject();
            
            // Execution data for BlueprintEngine
            JsonArray execution = new JsonArray();
            for (GuiNode node : nodes) {
                JsonObject nodeObj = new JsonObject();
                nodeObj.addProperty("id", node.id);
                nodeObj.addProperty("type", node.typeId);
                
                JsonObject inputs = new JsonObject();
                for (GuiNode.NodePort port : node.inputs) {
                    GuiConnection conn = findConnectionTo(node, port.id, connections);
                    JsonObject input = new JsonObject();
                    if (conn != null) {
                        input.addProperty("type", "link");
                        input.addProperty("nodeId", conn.from.id);
                        input.addProperty("socket", conn.fromPort);
                    } else {
                        input.addProperty("type", "value");
                        JsonElement val = node.inputValues.get(port.id);
                        if (val != null) {
                            input.add("value", val);
                        } else {
                            input.add("value", GSON.toJsonTree(port.defaultValue != null ? port.defaultValue : ""));
                        }
                    }
                    inputs.add(port.id, input);
                }
                nodeObj.add("inputs", inputs);

                JsonObject outputs = new JsonObject();
                for (GuiNode.NodePort port : node.outputs) {
                    JsonArray targets = new JsonArray();
                    for (GuiConnection conn : connections) {
                        if (conn.from == node && conn.fromPort.equals(port.id)) {
                            JsonObject target = new JsonObject();
                            target.addProperty("nodeId", conn.to.id);
                            target.addProperty("socket", conn.toPort);
                            targets.add(target);
                        }
                    }
                    outputs.add(port.id, targets);
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
                
                // Save dynamic outputs (like for switch node)
                JsonArray dynamicOutputs = new JsonArray();
                for (GuiNode.NodePort port : node.outputs) {
                    boolean isDefault = false;
                    for (NodeDefinition.PortDefinition defPort : node.definition.outputs()) {
                        if (defPort.id().equals(port.id)) {
                            isDefault = true;
                            break;
                        }
                    }
                    if (!isDefault) {
                        JsonObject pObj = new JsonObject();
                        pObj.addProperty("id", port.id);
                        pObj.addProperty("name", port.displayName);
                        pObj.addProperty("type", port.type.name());
                        dynamicOutputs.add(pObj);
                    }
                }
                if (dynamicOutputs.size() > 0) {
                    nodeObj.add("dynamicOutputs", dynamicOutputs);
                }

                // Save dynamic inputs (like for string_combine node)
                JsonArray dynamicInputs = new JsonArray();
                for (GuiNode.NodePort port : node.inputs) {
                    boolean isDefault = false;
                    for (NodeDefinition.PortDefinition defPort : node.definition.inputs()) {
                        if (defPort.id().equals(port.id)) {
                            isDefault = true;
                            break;
                        }
                    }
                    if (!isDefault) {
                        JsonObject pObj = new JsonObject();
                        pObj.addProperty("id", port.id);
                        pObj.addProperty("name", port.displayName);
                        pObj.addProperty("type", port.type.name());
                        pObj.addProperty("hasInput", port.hasInput);
                        dynamicInputs.add(pObj);
                    }
                }
                if (dynamicInputs.size() > 0) {
                    nodeObj.add("dynamicInputs", dynamicInputs);
                }
                
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

            return GSON.toJson(root);
        } catch (Exception e) {
            MaingraphforMC.LOGGER.error("Failed to serialize blueprint", e);
            return null;
        }
    }

    public static void save(Path dataFile, List<GuiNode> nodes, List<GuiConnection> connections) {
        // Special Case: "wwssadadab" is a magic name that doesn't save to JSON
        if (dataFile != null && dataFile.getFileName().toString().startsWith("wwssadadab")) {
            MaingraphforMC.LOGGER.info("Magic blueprint detected: Skipping JSON generation for {}", dataFile.getFileName());
            return;
        }

        try {
            String json = serialize(nodes, connections);
            if (json != null) {
                Files.writeString(dataFile, json);
                MaingraphforMC.LOGGER.info("Saved blueprint to {}", dataFile.toAbsolutePath());
            }
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
        // Special Case: "wwssadadab" - Lock blueprint and tile nodes
        if (dataFile != null && dataFile.getFileName().toString().startsWith("wwssadadab")) {
            loadMagicBlueprint(nodes, connections);
            return;
        }

        try {
            if (!Files.exists(dataFile)) return;
            String json = Files.readString(dataFile);
            loadFromString(json, nodes, connections);
        } catch (Exception e) {
            MaingraphforMC.LOGGER.error("Failed to load blueprint", e);
        }
    }

    public static void loadFromString(String json, List<GuiNode> nodes, List<GuiConnection> connections) {
        try {
            if (json == null || json.isEmpty()) return;
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            
            nodes.clear();
            connections.clear();
            
            Map<String, GuiNode> nodeMap = new HashMap<>();
            
            JsonObject ui = root.has("ui") ? root.getAsJsonObject("ui") : root;

            if (ui.has("nodes")) {
                for (JsonElement e : ui.getAsJsonArray("nodes")) {
                    JsonObject obj = e.getAsJsonObject();
                    String nodeTypeId = obj.has("defId") ? obj.get("defId").getAsString() : obj.get("type").getAsString();
                    NodeDefinition def = NodeRegistry.get(nodeTypeId);
                    if (def != null) {
                        GuiNode node = new GuiNode(def, obj.get("x").getAsFloat(), obj.get("y").getAsFloat());
                        node.id = obj.get("id").getAsString();
                        if (obj.has("inputValues")) {
                            node.inputValues = obj.getAsJsonObject("inputValues");
                        }
                        if (obj.has("dynamicOutputs")) {
                            for (JsonElement p : obj.getAsJsonArray("dynamicOutputs")) {
                                JsonObject pObj = p.getAsJsonObject();
                                NodeDefinition.PortType portType = NodeDefinition.PortType.valueOf(pObj.get("type").getAsString());
                                node.addOutput(
                                    pObj.get("id").getAsString(),
                                    pObj.get("name").getAsString(),
                                    portType,
                                    getPortColor(portType)
                                );
                            }
                        }
                        if (obj.has("dynamicInputs")) {
                            for (JsonElement p : obj.getAsJsonArray("dynamicInputs")) {
                                JsonObject pObj = p.getAsJsonObject();
                                NodeDefinition.PortType portType = NodeDefinition.PortType.valueOf(pObj.get("type").getAsString());
                                node.addInput(
                                    pObj.get("id").getAsString(),
                                    pObj.get("name").getAsString(),
                                    portType,
                                    getPortColor(portType),
                                    pObj.has("hasInput") && pObj.get("hasInput").getAsBoolean(),
                                    "",
                                    null
                                );
                            }
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
            MaingraphforMC.LOGGER.error("Failed to load blueprint from string", e);
        }
    }

    private static void loadMagicBlueprint(List<GuiNode> nodes, List<GuiConnection> connections) {
        nodes.clear();
        connections.clear();
        
        Collection<NodeDefinition> allDefs = NodeRegistry.getAllDefinitions();
        
        // Group by category for better organization
        Map<String, List<NodeDefinition>> byCategory = new TreeMap<>();
        for (NodeDefinition def : allDefs) {
            byCategory.computeIfAbsent(def.category(), k -> new ArrayList<>()).add(def);
        }

        int startX = 50;
        int currentY = 50;
        int columnSpacing = 30;
        int rowSpacing = 40;

        for (Map.Entry<String, List<NodeDefinition>> entry : byCategory.entrySet()) {
            int currentX = startX;
            int maxRowHeight = 0;
            
            // Add category label as a special note? (Optional, maybe later)
            
            for (NodeDefinition def : entry.getValue()) {
                // Estimate size
                int maxPorts = Math.max(def.inputs().size(), def.outputs().size());
                int estimatedHeight = 15 + 10 + maxPorts * 15 + 10;
                
                // Estimate width
                int maxInputW = 0;
                for (NodeDefinition.PortDefinition p : def.inputs()) {
                    maxInputW = Math.max(maxInputW, 10 + p.displayName().length() * 6 + (p.hasInput() ? 55 : 0));
                }
                int maxOutputW = 0;
                for (NodeDefinition.PortDefinition p : def.outputs()) {
                    maxOutputW = Math.max(maxOutputW, 10 + p.displayName().length() * 6);
                }
                int estimatedWidth = Math.max(120, Math.max(def.name().length() * 7, maxInputW + maxOutputW + 25));

                GuiNode node = new GuiNode(def, currentX, currentY);
                nodes.add(node);
                
                currentX += estimatedWidth + columnSpacing;
                maxRowHeight = Math.max(maxRowHeight, estimatedHeight);
                
                // Wrap to next line if row gets too long
                if (currentX > 2500) {
                    currentX = startX;
                    currentY += maxRowHeight + rowSpacing;
                    maxRowHeight = 0;
                }
            }
            
            // Next category starts on a new row with extra spacing
            currentY += maxRowHeight + rowSpacing * 2;
        }
    }

    private static int getPortColor(NodeDefinition.PortType type) {
        switch (type) {
            case EXEC: return 0xFFFFFFFF;
            case STRING: return 0xFFDA00DA;
            case FLOAT: return 0xFF36CF36;
            case BOOLEAN: return 0xFF920101;
            case LIST: return 0xFFFFCC00;
            case UUID: return 0xFF55FF55;
            case ANY: return 0xFFAAAAAA;
            default: return 0xFFFFFFFF;
        }
    }
}


