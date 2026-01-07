package ltd.opens.mg.mc.core.blueprint.engine;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.HashMap;
import java.util.Map;

public class NodeLogicRegistry {
    private static final Map<String, NodeHandler> HANDLERS = new HashMap<>();

    public static void register(String typeId, NodeHandler handler) {
        HANDLERS.put(typeId, handler);
    }

    public static NodeHandler get(String typeId) {
        return HANDLERS.get(typeId);
    }

    public static Object evaluateInput(JsonObject node, String pinId, NodeContext ctx) {
        if (!node.has("inputs")) return null;
        JsonObject inputs = node.getAsJsonObject("inputs");
        if (!inputs.has(pinId)) return null;

        JsonObject input = inputs.getAsJsonObject(pinId);
        String type = input.get("type").getAsString();

        if (type.equals("value")) {
            if (!input.has("value")) return null;
            JsonElement val = input.get("value");
            
            if (ctx.formatVersion >= 2) {
                if (val.isJsonPrimitive()) {
                    var prim = val.getAsJsonPrimitive();
                    if (prim.isNumber()) return prim.getAsDouble();
                    if (prim.isBoolean()) return prim.getAsBoolean();
                    return prim.getAsString();
                } else if (val.isJsonArray()) {
                    JsonArray arr = val.getAsJsonArray();
                    java.util.List<Object> list = new java.util.ArrayList<>();
                    for (JsonElement e : arr) {
                        if (e.isJsonPrimitive()) {
                            var p = e.getAsJsonPrimitive();
                            if (p.isNumber()) list.add(p.getAsDouble());
                            else if (p.isBoolean()) list.add(p.getAsBoolean());
                            else list.add(p.getAsString());
                        } else {
                            list.add(e.toString());
                        }
                    }
                    return list;
                }
                return val;
            } else {
                return val.getAsString();
            }
        } else if (type.equals("link")) {
            String sourceId = input.get("nodeId").getAsString();
            String sourceSocket = input.get("socket").getAsString();
            JsonObject sourceNode = ctx.nodesMap.get(sourceId);
            if (sourceNode != null) {
                return evaluateOutput(sourceNode, sourceSocket, ctx);
            }
        }
        return null;
    }

    public static Object evaluateOutput(JsonObject node, String pinId, NodeContext ctx) {
        String type = node.has("type") ? node.get("type").getAsString() : null;
        if (type == null) return null;

        NodeHandler handler = get(type);
        if (handler != null) {
            return handler.getValue(node, pinId, ctx);
        }
        return null;
    }

    public static void triggerExec(JsonObject node, String pinId, NodeContext ctx) {
        if (!node.has("outputs")) return;
        
        // 增加执行计数并检查上限，防止死循环或大规模循环导致卡服
        ctx.nodeExecCount++;
        if (ctx.nodeExecCount > ltd.opens.mg.mc.Config.getMaxNodeExecutions()) {
            return;
        }

        JsonObject outputs = node.getAsJsonObject("outputs");
        if (!outputs.has(pinId)) return;

        JsonArray targets = outputs.getAsJsonArray(pinId);
        for (JsonElement t : targets) {
            JsonObject target = t.getAsJsonObject();
            String targetId = target.get("nodeId").getAsString();
            JsonObject targetNode = ctx.nodesMap.get(targetId);
            if (targetNode != null) {
                String type = targetNode.has("type") ? targetNode.get("type").getAsString() : null;
                if (type != null) {
                    NodeHandler handler = get(type);
                    if (handler != null) {
                        ctx.lastTriggeredPin = target.has("socket") ? target.get("socket").getAsString() : "exec";
                        handler.execute(targetNode, ctx);
                    }
                }
            }
        }
    }

    static {
        // Functions
        // Comparison
    }
}
