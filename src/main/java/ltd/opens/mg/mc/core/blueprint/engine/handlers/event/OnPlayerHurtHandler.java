package ltd.opens.mg.mc.core.blueprint.engine.handlers.event;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

public class OnPlayerHurtHandler implements NodeHandler {
    @Override
    public Object getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("damage_amount")) return ctx.triggerValue;
        if (pinId.equals("attacker_uuid")) return ctx.triggerExtraUuid != null ? ctx.triggerExtraUuid : "";
        if (pinId.equals("uuid")) return ctx.triggerUuid != null ? ctx.triggerUuid : "";
        return null;
    }
}




