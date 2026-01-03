package ltd.opens.mg.mc.core.blueprint.engine.handlers.event;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;

public class OnPlayerAttackHandler implements NodeHandler {
    @Override
    public String getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("victim_uuid")) return ctx.triggerExtraUuid != null ? ctx.triggerExtraUuid : "";
        if (pinId.equals("uuid")) return ctx.triggerUuid != null ? ctx.triggerUuid : "";
        return "";
    }
}

