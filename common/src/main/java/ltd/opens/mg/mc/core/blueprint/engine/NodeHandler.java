package ltd.opens.mg.mc.core.blueprint.engine;

import com.google.gson.JsonObject;

public interface NodeHandler {
    /**
     * For action nodes (EXEC flow)
     */
    default void execute(JsonObject node, NodeContext ctx) {}

    /**
     * For data nodes (Value providers)
     */
    default Object getValue(JsonObject node, String pinId, NodeContext ctx) {
        return null;
    }
}
