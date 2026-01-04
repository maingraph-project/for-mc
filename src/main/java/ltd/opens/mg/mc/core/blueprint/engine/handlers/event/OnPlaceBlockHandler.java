package ltd.opens.mg.mc.core.blueprint.engine.handlers.event;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

public class OnPlaceBlockHandler implements NodeHandler {
    @Override
    public Object getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("x")) return ctx.triggerX;
        if (pinId.equals("y")) return ctx.triggerY;
        if (pinId.equals("z")) return ctx.triggerZ;
        if (pinId.equals("block_id")) return ctx.triggerBlockId != null ? ctx.triggerBlockId : "";
        if (pinId.equals("uuid")) return ctx.triggerUuid != null ? ctx.triggerUuid : "";
        return null;
    }
}




