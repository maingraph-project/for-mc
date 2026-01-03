package ltd.opens.mg.mc.core.blueprint.engine.handlers.logic;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;

public class ForLoopHandler implements NodeHandler {
    @Override
    public void execute(JsonObject node, NodeContext ctx) {
        // 如果触发的是 break 入参，则请求中断
        if ("break".equals(ctx.lastTriggeredPin)) {
            ctx.breakRequested = true;
            return;
        }

        String startStr = NodeLogicRegistry.evaluateInput(node, "start", ctx);
        String endStr = NodeLogicRegistry.evaluateInput(node, "end", ctx);
        
        try {
            int start = (int) Double.parseDouble(startStr.isEmpty() ? "0" : startStr);
            int end = (int) Double.parseDouble(endStr.isEmpty() ? "0" : endStr);
            
            // Support nested loops by saving the previous break state
            boolean previousBreakRequested = ctx.breakRequested;
            ctx.breakRequested = false;

            for (int i = start; i <= end; i++) {
                // Store current index in the node object so getValue can retrieve it
                node.addProperty("_index", String.valueOf(i));
                
                // Trigger loop body
                NodeLogicRegistry.triggerExec(node, "loop_body", ctx);
                
                // Check if break was requested
                if (ctx.breakRequested) {
                    ctx.breakRequested = false; 
                    break;
                }
            }
            
            // Restore previous break state (so a break in an inner loop doesn't automatically break the outer one)
            // UNLESS the break was meant for the outer loop? 
            // Usually in blueprints, Break only breaks the immediate parent loop.
            ctx.breakRequested = previousBreakRequested;
        } catch (Exception e) {
            // Ignore parse errors
        }
        
        // Trigger completed
        NodeLogicRegistry.triggerExec(node, "completed", ctx);
    }

    @Override
    public String getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("index")) {
            return node.has("_index") ? node.get("_index").getAsString() : "0";
        }
        return "";
    }
}

