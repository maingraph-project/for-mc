package ltd.opens.mg.mc.core.blueprint.engine.handlers.event;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

public class OnUseItemHandler implements NodeHandler {
    @Override
    public Object getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("item_id")) return ctx.triggerItemId != null ? ctx.triggerItemId : "";
        if (pinId.equals("uuid")) return ctx.triggerUuid != null ? ctx.triggerUuid : "";
        return null;
    }
}



