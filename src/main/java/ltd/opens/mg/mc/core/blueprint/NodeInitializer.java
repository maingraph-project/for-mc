package ltd.opens.mg.mc.core.blueprint;

import ltd.opens.mg.mc.core.blueprint.nodes.EventNodes;
import ltd.opens.mg.mc.core.blueprint.nodes.MathNodes;

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
    }
}
