package ltd.opens.mg.mc.core.blueprint.nodes;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.NodeDefinition;
import ltd.opens.mg.mc.core.blueprint.NodeHelper;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;
import ltd.opens.mg.mc.core.blueprint.NodePorts;
import ltd.opens.mg.mc.core.blueprint.NodeThemes;
import ltd.opens.mg.mc.core.blueprint.engine.TickScheduler;
import ltd.opens.mg.mc.core.blueprint.events.RegisterMGMCNodesEvent;
import net.neoforged.bus.api.SubscribeEvent;

/**
 * 控制流相关节点
 */
public class ControlFlowNodes {

    @SubscribeEvent
    public static void onRegister(RegisterMGMCNodesEvent event) {
        // 分支节点 (Branch)
        NodeHelper.setup("branch", "node.mgmc.branch.name")
            .category("node_category.mgmc.logic.control")
            .color(NodeThemes.COLOR_NODE_CONTROL)
            .input(NodePorts.EXEC, "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .input(NodePorts.CONDITION, "node.mgmc.port.condition", NodeDefinition.PortType.BOOLEAN, NodeThemes.COLOR_PORT_BOOLEAN, true, true)
            .output(NodePorts.TRUE, "node.mgmc.port.true", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .output(NodePorts.FALSE, "node.mgmc.port.false", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .registerExec((node, ctx) -> {
                boolean condition = TypeConverter.toBoolean(NodeLogicRegistry.evaluateInput(node, NodePorts.CONDITION, ctx));
                NodeLogicRegistry.triggerExec(node, condition ? NodePorts.TRUE : NodePorts.FALSE, ctx);
            });

        // 切换器节点 (Switch) - 修复硬编码，标记为动态节点
        NodeHelper.setup("switch", "node.mgmc.switch.name")
            .category("node_category.mgmc.logic.control")
            .color(NodeThemes.COLOR_NODE_CONTROL)
            .property("dynamic_outputs", true) // 标记为动态输出端口节点
            .button("node.mgmc.switch.add_branch", "add_output_modal")
            .input(NodePorts.EXEC, "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .input(NodePorts.CONTROL, "node.mgmc.switch.port.control", NodeDefinition.PortType.ANY, NodeThemes.COLOR_PORT_ANY)
            .output(NodePorts.DEFAULT, "node.mgmc.switch.port.default", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .registerExec((node, ctx) -> {
                String controlValue = TypeConverter.toString(NodeLogicRegistry.evaluateInput(node, NodePorts.CONTROL, ctx));
                if (node.has("outputs")) {
                    var outputs = node.getAsJsonObject("outputs");
                    for (String key : outputs.keySet()) {
                        if (key.equals(NodePorts.DEFAULT) || key.equals(NodePorts.EXEC)) continue;
                        if (controlValue.equals(key)) {
                            NodeLogicRegistry.triggerExec(node, key, ctx);
                            return;
                        }
                    }
                }
                NodeLogicRegistry.triggerExec(node, NodePorts.DEFAULT, ctx);
            });

        // 循环节点 (For Loop)
        NodeHelper.setup("for_loop", "node.mgmc.for_loop.name")
            .category("node_category.mgmc.logic.control")
            .color(NodeThemes.COLOR_NODE_CONTROL)
            .input(NodePorts.EXEC, "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .input(NodePorts.START, "node.mgmc.for_loop.port.start", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, true, 0)
            .input(NodePorts.END, "node.mgmc.for_loop.port.end", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, true, 10)
            .input(NodePorts.BREAK, "node.mgmc.for_loop.port.break", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .output(NodePorts.LOOP_BODY, "node.mgmc.for_loop.port.loop_body", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .output(NodePorts.COMPLETED, "node.mgmc.for_loop.port.completed", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .output(NodePorts.INDEX, "node.mgmc.for_loop.port.index", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT)
            .register(new NodeHelper.NodeHandlerAdapter() {
                @Override
                public void execute(JsonObject node, NodeContext ctx) {
                    if (NodePorts.BREAK.equals(ctx.lastTriggeredPin)) {
                        ctx.breakRequested.set(true);
                        return;
                    }
                    int start = TypeConverter.toInt(NodeLogicRegistry.evaluateInput(node, NodePorts.START, ctx));
                    int end = TypeConverter.toInt(NodeLogicRegistry.evaluateInput(node, NodePorts.END, ctx));
                    boolean previousBreakRequested = ctx.breakRequested.get();
                    ctx.breakRequested.set(false);
                    String nodeId = node.get("id").getAsString();
                    for (int i = start; i <= end; i++) {
                        ctx.setRuntimeData(nodeId, "index", i);
                        NodeLogicRegistry.triggerExec(node, NodePorts.LOOP_BODY, ctx);
                        if (ctx.breakRequested.get()) {
                            ctx.breakRequested.set(false);
                            break;
                        }
                    }
                    ctx.breakRequested.set(previousBreakRequested);
                    NodeLogicRegistry.triggerExec(node, NodePorts.COMPLETED, ctx);
                }

                @Override
                public Object getValue(JsonObject node, String portId, NodeContext ctx) {
                    if (NodePorts.INDEX.equals(portId)) {
                        return ctx.getRuntimeData(node.get("id").getAsString(), "index", 0);
                    }
                    return null;
                }
            });

        // 等待刻 (Wait Tick)
        NodeHelper.setup("wait_tick", "node.mgmc.wait_tick.name")
            .category("node_category.mgmc.logic.control")
            .color(NodeThemes.COLOR_NODE_CONTROL)
            .execIn()
            .input(NodePorts.TICKS, "node.mgmc.port.ticks", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, true, 20)
            .execOut()
            .registerExec((node, ctx) -> {
                int ticks = TypeConverter.toInt(NodeLogicRegistry.evaluateInput(node, NodePorts.TICKS, ctx));
                if (ticks <= 0) {
                    NodeLogicRegistry.triggerExec(node, NodePorts.EXEC, ctx);
                } else {
                    TickScheduler.schedule(ctx, node, NodePorts.EXEC, ticks);
                }
            });

        // 等待秒 (Wait Second)
        NodeHelper.setup("wait_s", "node.mgmc.wait_s.name")
            .category("node_category.mgmc.logic.control")
            .color(NodeThemes.COLOR_NODE_CONTROL)
            .execIn()
            .input(NodePorts.SECONDS, "node.mgmc.port.seconds", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, true, 1.0)
            .execOut()
            .registerExec((node, ctx) -> {
                double seconds = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.SECONDS, ctx));
                int ticks = (int) (seconds * 20);
                if (ticks <= 0) {
                    NodeLogicRegistry.triggerExec(node, NodePorts.EXEC, ctx);
                } else {
                    TickScheduler.schedule(ctx, node, NodePorts.EXEC, ticks);
                }
            });

        // 中断节点 (Break Loop)
        NodeHelper.setup("break_loop", "node.mgmc.break_loop.name")
            .category("node_category.mgmc.logic.control")
            .color(NodeThemes.COLOR_NODE_CONTROL)
            .input(NodePorts.EXEC, "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .output(NodePorts.BREAK, "node.mgmc.break_loop.port.break", NodeDefinition.PortType.EXEC, NodeThemes.COLOR_PORT_EXEC)
            .registerExec((node, ctx) -> {
                ctx.breakRequested.set(true);
            });
    }
}
