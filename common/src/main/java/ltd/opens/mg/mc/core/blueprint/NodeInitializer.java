package ltd.opens.mg.mc.core.blueprint;

import ltd.opens.mg.mc.core.blueprint.nodes.*;

/**
 * 蓝图节点统一初始化入口
 */
public class NodeInitializer {
    /**
     * 初始化所有节点
     */
    public static void init() {
        // 注册所有内置节点
        MathNodes.register();
        LogicNodes.register();
        VariableNodes.register();
        ConversionNodes.register();
        ControlFlowNodes.register();
        StringNodes.register();
        ListNodes.register();
        ActionNodes.register();
        EventNodes.register();
        ClientNodes.register();
        GetEntityInfoNode.register();
        RaycastNode.register();
        InventoryNodes.register();
        SpecialNodes.register();

        // 发布注册事件，通知外部模块 (如果有必要，使用 Architectury Event)
        // 目前简化为直接注册
        ltd.opens.mg.mc.MaingraphforMC.LOGGER.info("MGMCNodes registered.");
    }
}
