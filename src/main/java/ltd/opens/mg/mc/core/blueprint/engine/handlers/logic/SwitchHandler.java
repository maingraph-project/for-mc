package ltd.opens.mg.mc.core.blueprint.engine.handlers.logic;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

public class SwitchHandler implements NodeHandler {
    @Override
    public void execute(JsonObject node, NodeContext ctx) {
        String controlValue = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "control", ctx));
        
        if (node.has("outputs")) {
            JsonObject outputs = node.getAsJsonObject("outputs");
            for (String key : outputs.keySet()) {
                if (key.equals("default") || key.equals("exec")) continue;
                
                if (controlValue.equals(key)) {
                    NodeLogicRegistry.triggerExec(node, key, ctx);
                    return;
                }
            }
        }
        
        NodeLogicRegistry.triggerExec(node, "default", ctx);
    }
}



