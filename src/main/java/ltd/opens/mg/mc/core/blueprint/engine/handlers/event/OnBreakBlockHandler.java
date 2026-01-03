package ltd.opens.mg.mc.core.blueprint.engine.handlers.event;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;

public class OnBreakBlockHandler implements NodeHandler {
    @Override
    public String getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("x")) return String.valueOf(ctx.triggerX);
        if (pinId.equals("y")) return String.valueOf(ctx.triggerY);
        if (pinId.equals("z")) return String.valueOf(ctx.triggerZ);
        if (pinId.equals("block_id")) return ctx.triggerBlockId != null ? ctx.triggerBlockId : "";
        if (pinId.equals("uuid")) return ctx.triggerUuid != null ? ctx.triggerUuid : "";
        return "";
    }
}

