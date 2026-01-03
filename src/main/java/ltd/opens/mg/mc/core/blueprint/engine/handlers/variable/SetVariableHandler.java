package ltd.opens.mg.mc.core.blueprint.engine.handlers.variable;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;

public class SetVariableHandler implements NodeHandler {
    @Override
    public void execute(JsonObject node, NodeContext ctx) {
        try {
            String name = NodeLogicRegistry.evaluateInput(node, "name", ctx);
            String value = NodeLogicRegistry.evaluateInput(node, "value", ctx);
            
            if (name != null && !name.trim().isEmpty()) {
                ctx.variables.put(name.trim(), value != null ? value : "");
            }
        } catch (Exception e) {
            // 静默失败
        }
        
        NodeLogicRegistry.triggerExec(node, "exec", ctx);
    }

    @Override
    public String getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("value")) {
            return NodeLogicRegistry.evaluateInput(node, "value", ctx);
        }
        return "";
    }
}
