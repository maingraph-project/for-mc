package ltd.opens.mg.mc.core.blueprint.engine;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.handlers.impl.*;

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

    public static String evaluateInput(JsonObject node, String pinId, NodeContext ctx) {
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
            JsonObject sourceNode = ctx.nodesMap.get(sourceId);
            if (sourceNode != null) {
                return evaluateOutput(sourceNode, sourceSocket, ctx);
            }
        }
        return "";
    }

    public static String evaluateOutput(JsonObject node, String pinId, NodeContext ctx) {
        String type = node.has("type") ? node.get("type").getAsString() : null;
        if (type == null) return "";

        NodeHandler handler = get(type);
        if (handler != null) {
            return handler.getValue(node, pinId, ctx);
        }
        return "";
    }

    public static void triggerExec(JsonObject node, String pinId, NodeContext ctx) {
        if (!node.has("outputs")) return;
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
                        handler.execute(targetNode, ctx);
                    }
                }
            }
        }
    }

    static {
        // Events
        register("on_mgrun", new OnMgRunHandler());

        // Functions
        register("print_chat", new PrintChatHandler());
        register("print_string", new PrintStringHandler());
        register("get_list_item", new GetListItemHandler());
        register("get_entity_info", new GetEntityInfoHandler());

        // Logic
        register("branch", new BranchHandler());
        register("cast", new CastHandler());

        // Data
        register("player_health", new PlayerHealthHandler());
        register("add_float", new AddFloatHandler());
    }
}
