package ltd.opens.mg.mc.core.blueprint.engine.handlers.variable;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

public class SetVariableHandler implements NodeHandler {
    @Override
    public void execute(JsonObject node, NodeContext ctx) {
        try {
            String name = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "name", ctx));
            Object value = NodeLogicRegistry.evaluateInput(node, "value", ctx);
            
            if (name != null && !name.trim().isEmpty()) {
                ctx.variables.put(name.trim(), value);
            }
        } catch (Exception e) {
            // 静默失败
        }
        
        NodeLogicRegistry.triggerExec(node, "exec", ctx);
    }

    @Override
    public Object getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("value")) {
            return NodeLogicRegistry.evaluateInput(node, "value", ctx);
        }
        return null;
    }
}


