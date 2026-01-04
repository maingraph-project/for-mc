package ltd.opens.mg.mc.core.blueprint.engine.handlers.variable;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

public class GetVariableHandler implements NodeHandler {
    @Override
    public Object getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("value")) {
            try {
                String name = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, "name", ctx));
                if (name == null || name.trim().isEmpty()) return null;
                return ctx.variables.get(name.trim());
            } catch (Exception e) {
                return null;
            }
        }
        return null;
    }
}



