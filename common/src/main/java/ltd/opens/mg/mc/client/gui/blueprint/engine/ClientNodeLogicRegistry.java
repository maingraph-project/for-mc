package ltd.opens.mg.mc.client.gui.blueprint.engine;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ltd.opens.mg.mc.MaingraphforMC;
import ltd.opens.mg.mc.network.MGMCNetwork;
import ltd.opens.mg.mc.network.payloads.ClientActionResponsePayload;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

/**
 * 客户端节点逻辑注册表
 * 用于处理从服务端发送过来的客户端节点动作请求
 */
public class ClientNodeLogicRegistry {
    private static final Map<String, BiConsumer<JsonObject, ClientActionContext>> HANDLERS = new ConcurrentHashMap<>();

    public static void register(String actionType, BiConsumer<JsonObject, ClientActionContext> handler) {
        HANDLERS.put(actionType, handler);
    }

    public static void execute(String blueprintName, String nodeId, String actionType, String data) {
        BiConsumer<JsonObject, ClientActionContext> handler = HANDLERS.get(actionType);
        if (handler != null) {
            try {
                JsonObject params = JsonParser.parseString(data).getAsJsonObject();
                ClientActionContext ctx = new ClientActionContext(blueprintName, nodeId);
                handler.accept(params, ctx);
            } catch (Exception e) {
                MaingraphforMC.LOGGER.error("Error executing client action: " + actionType, e);
            }
        } else {
            MaingraphforMC.LOGGER.warn("No handler registered for client action: " + actionType);
        }
    }

    public static record ClientActionContext(String blueprintName, String nodeId) {
        public void respond(JsonObject result) {
            MGMCNetwork.sendToServer(new ClientActionResponsePayload(blueprintName, nodeId, result.toString()));
        }
    }
}
