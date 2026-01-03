package ltd.opens.mg.mc.core.blueprint.engine.handlers.logic;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

public class CastHandler implements NodeHandler {
    @Override
    public String getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("output")) {
            String input = NodeLogicRegistry.evaluateInput(node, "input", ctx);
            String targetType = NodeLogicRegistry.evaluateInput(node, "to_type", ctx);
            
            // 使用转换引擎进行转换
            return TypeConverter.cast(input, targetType);
        }
        return "";
    }
}

