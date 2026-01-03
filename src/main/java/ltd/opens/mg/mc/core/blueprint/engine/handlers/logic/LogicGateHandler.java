package ltd.opens.mg.mc.core.blueprint.engine.handlers.logic;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;

public class LogicGateHandler implements NodeHandler {
    private final String mode;

    public LogicGateHandler(String mode) {
        this.mode = mode;
    }

    @Override
    public String getValue(JsonObject node, String pinId, NodeContext ctx) {
        if (pinId.equals("result")) {
            try {
                if (mode.equals("not")) {
                    boolean a = Boolean.parseBoolean(NodeLogicRegistry.evaluateInput(node, "a", ctx));
                    return String.valueOf(!a);
                } else {
                    boolean a = Boolean.parseBoolean(NodeLogicRegistry.evaluateInput(node, "a", ctx));
                    boolean b = Boolean.parseBoolean(NodeLogicRegistry.evaluateInput(node, "b", ctx));
                    boolean res = false;
                    switch (mode) {
                        case "and": res = a && b; break;
                        case "or": res = a || b; break;
                        case "xor": res = a ^ b; break;
                        case "nand": res = !(a && b); break;
                        case "nor": res = !(a || b); break;
                    }
                    return String.valueOf(res);
                }
            } catch (Exception e) {}
        }
        return "false";
    }
}

