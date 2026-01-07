package ltd.opens.mg.mc.core.blueprint;

import ltd.opens.mg.mc.core.blueprint.nodes.EventNodes;
import ltd.opens.mg.mc.core.blueprint.nodes.LogicNodes;
import ltd.opens.mg.mc.core.blueprint.nodes.MathNodes;
import ltd.opens.mg.mc.core.blueprint.nodes.VariableNodes;
import ltd.opens.mg.mc.core.blueprint.nodes.ConversionNodes;
import ltd.opens.mg.mc.core.blueprint.nodes.ControlFlowNodes;
import ltd.opens.mg.mc.core.blueprint.nodes.StringNodes;
import ltd.opens.mg.mc.core.blueprint.nodes.ListNodes;
import ltd.opens.mg.mc.core.blueprint.nodes.ActionNodes;
import ltd.opens.mg.mc.core.blueprint.nodes.GetEntityInfoNode;

/**
 * 蓝图节点统一初始化入口
 */
public class NodeInitializer {
    /**
     * 初始化所有新版节点
     */
    public static void init() {
        EventNodes.register();
        MathNodes.register();
        LogicNodes.register();
        VariableNodes.register();
        ConversionNodes.register();
        ControlFlowNodes.register();
        StringNodes.register();
        ListNodes.register();
        ActionNodes.register();
        GetEntityInfoNode.register();
    }
}
