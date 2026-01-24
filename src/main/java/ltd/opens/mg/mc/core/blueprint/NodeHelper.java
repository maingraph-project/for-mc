package ltd.opens.mg.mc.core.blueprint;

import ltd.opens.mg.mc.core.blueprint.engine.NodeHandler;
import ltd.opens.mg.mc.core.blueprint.engine.NodeLogicRegistry;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * 节点注册助手类，提供链式调用 API 统一元数据与逻辑注册。
 */
public class NodeHelper {
    private final NodeDefinition.Builder builder;
    private final String id;

    private NodeHelper(String id, String nameKey) {
        this.id = id;
        this.builder = new NodeDefinition.Builder(id, nameKey);
    }

    /**
     * 开始配置一个新节点
     * @param id 节点唯一标识（若不包含 ":" 则自动补全为当前 ModID）
     * @param nameKey 节点名称的 i18n key
     * @return NodeHelper 实例
     */
    public static NodeHelper setup(String id, String nameKey) {
        String finalId = id;
        if (!id.contains(":")) {
            String namespace = "mgmc";
            try {
                String activeNamespace = net.neoforged.fml.ModLoadingContext.get().getActiveNamespace();
                if (activeNamespace != null && !activeNamespace.isEmpty()) {
                    namespace = activeNamespace;
                }
            } catch (Throwable ignored) {
                // 忽略非 Mod 加载环境下的异常
            }
            finalId = namespace + ":" + id;
        }
        return new NodeHelper(finalId, nameKey);
    }

    public String getId() {
        return id;
    }

    /**
     * 设置节点分类
     * @param categoryKey 分类的 i18n key (支持嵌套路径如 mgmc.math)
     * @return NodeHelper 实例
     */
    public NodeHelper category(String categoryKey) {
        builder.category(categoryKey);
        return this;
    }

    /**
     * 设置节点颜色
     */
    public NodeHelper color(int color) {
        builder.color(color);
        return this;
    }

    /**
     * 设置节点属性
     */
    public NodeHelper property(String key, Object value) {
        builder.addProperty(key, value);
        return this;
    }

    /**
     * 添加输入端口
     */
    public NodeHelper input(String id, String displayNameKey, NodeDefinition.PortType type, int color) {
        builder.addInput(id, displayNameKey, type, color);
        return this;
    }

    public NodeHelper input(String id, String displayNameKey, NodeDefinition.PortType type, int color, boolean hasInput, Object defaultValue) {
        builder.addInput(id, displayNameKey, type, color, hasInput, defaultValue);
        return this;
    }

    public NodeHelper input(String id, String displayNameKey, NodeDefinition.PortType type, int color, boolean hasInput, Object defaultValue, String[] options) {
        builder.addInput(id, displayNameKey, type, color, hasInput, defaultValue, options);
        return this;
    }

    public NodeHelper input(String id, String displayNameKey, NodeDefinition.PortType type, int color, Object defaultValue) {
        return input(id, displayNameKey, type, color, true, defaultValue);
    }

    /**
     * 添加输出端口
     */
    public NodeHelper output(String id, String displayNameKey, NodeDefinition.PortType type, int color) {
        builder.addOutput(id, displayNameKey, type, color);
        return this;
    }

    /**
     * 添加执行输入端口 (默认 ID 为 "exec")
     */
    public NodeHelper execIn() {
        return input("exec", "node.mgmc.port.exec_in", NodeDefinition.PortType.EXEC, 0xFFFFFFFF);
    }

    public NodeHelper execIn(String id, String displayNameKey) {
        return input(id, displayNameKey, NodeDefinition.PortType.EXEC, 0xFFFFFFFF);
    }

    /**
     * 添加执行输出端口 (默认 ID 为 "exec")
     */
    public NodeHelper execOut() {
        return output("exec", "node.mgmc.port.exec_out", NodeDefinition.PortType.EXEC, 0xFFFFFFFF);
    }

    public NodeHelper execOut(String id, String displayNameKey) {
        return output(id, displayNameKey, NodeDefinition.PortType.EXEC, 0xFFFFFFFF);
    }

    /**
     * 为节点添加自定义标记或属性（用于消除 Handler 中的硬编码逻辑）
     * @param key 标记键
     * @param value 标记值
     * @return NodeHelper 实例
     */
    public NodeHelper flag(String key, Object value) {
        builder.addProperty(key, value);
        return this;
    }

    /**
     * 为节点配置一个底部的交互按钮（如“添加分支”、“添加输入”）
     * @param labelKey 按钮文字的 i18n key
     * @param actionType 动作类型（如 "add_output_modal", "add_input_indexed"）
     * @return NodeHelper 实例
     */
    public NodeHelper button(String labelKey, String actionType) {
        builder.addProperty("ui_button_label", labelKey);
        builder.addProperty("ui_button_action", actionType);
        return this;
    }

    /**
     * 注册节点（元数据与逻辑同时注册）
     * @param handler 节点执行逻辑处理器
     */
    public void register(NodeHandler handler) {
        if (handler == null) {
            throw new IllegalArgumentException("Node handler cannot be null for node: " + id);
        }
        registerMetadataOnly();
        NodeLogicRegistry.register(id, handler);
    }

    /**
     * 仅注册节点元数据（不注册执行逻辑）
     * 适用于纯客户端环境（如编辑器）或只需要显示定义的场景，实现逻辑与元数据解耦。
     */
    public void registerMetadataOnly() {
        NodeRegistry.register(builder.build());
    }

    public void registerExec(SimpleExecuteHandler handler) {
        register(new NodeHandler() {
            @Override
            public void execute(com.google.gson.JsonObject node, ltd.opens.mg.mc.core.blueprint.engine.NodeContext ctx) {
                handler.handle(node, ctx);
            }
        });
    }

    /**
     * 注册一个 Minecraft 事件节点。
     * 该方法会将事件监听所需的元数据存储到 NodeDefinition 中，
     * 以便 EventDispatcher 能够自动发现并分发事件。
     *
     * @param eventClass Minecraft 事件类
     * @param contextPopulator 填充 NodeContext 的逻辑
     * @param routingIdExtractor 提取路由 ID 的逻辑（如 blockId, itemId 等）
     * @param valueHandler 节点数据提取逻辑
     */
    public <T extends net.neoforged.bus.api.Event> void registerEvent(
            Class<T> eventClass,
            java.util.function.BiConsumer<T, ltd.opens.mg.mc.core.blueprint.engine.NodeContext.Builder> contextPopulator,
            java.util.function.Function<T, String> routingIdExtractor,
            SimpleValueHandler valueHandler) {
        
        registerEvent(eventClass, ContextProviders.getProvider(eventClass), contextPopulator, routingIdExtractor, valueHandler);
    }

    /**
     * 注册一个带有自定义 ContextProvider 的 Minecraft 事件节点。
     */
    @SuppressWarnings("unchecked")
    public <T extends net.neoforged.bus.api.Event> void registerEvent(
            Class<T> eventClass,
            EventContextProvider<T> provider,
            java.util.function.BiConsumer<T, ltd.opens.mg.mc.core.blueprint.engine.NodeContext.Builder> contextPopulator,
            java.util.function.Function<T, String> routingIdExtractor,
            SimpleValueHandler valueHandler) {

        EventMetadata metadata = new EventMetadata(
            eventClass,
            (event, builder) -> contextPopulator.accept((T) event, builder),
            event -> routingIdExtractor.apply((T) event),
            (EventContextProvider<net.neoforged.bus.api.Event>) provider
        );

        builder.addProperty("event_metadata", metadata);
        registerValue(valueHandler);
    }

    /**
     * 注册一个不需要路由 ID 的 Minecraft 事件节点（如玩家加入世界）。
     */
    public <T extends net.neoforged.bus.api.Event> void registerEvent(
            Class<T> eventClass,
            java.util.function.BiConsumer<T, ltd.opens.mg.mc.core.blueprint.engine.NodeContext.Builder> contextPopulator,
            SimpleValueHandler valueHandler) {
        registerEvent(eventClass, contextPopulator, e -> ltd.opens.mg.mc.core.blueprint.routing.BlueprintRouter.GLOBAL_ID, valueHandler);
    }

    public static abstract class NodeHandlerAdapter implements NodeHandler {
        @Override
        public void execute(com.google.gson.JsonObject node, ltd.opens.mg.mc.core.blueprint.engine.NodeContext ctx) {}

        @Override
        public Object getValue(com.google.gson.JsonObject node, String portId, ltd.opens.mg.mc.core.blueprint.engine.NodeContext ctx) {
            return null;
        }
    }

    @FunctionalInterface
    public interface SimpleExecuteHandler {
        void handle(com.google.gson.JsonObject node, ltd.opens.mg.mc.core.blueprint.engine.NodeContext ctx);
    }

    /**
     * 为简单节点提供快速注册接口（仅支持 getValue 逻辑）
     */
    /**
     * 极简逻辑注册（适用于只读数据的节点，如变量、事件数据获取）
     */
    public void registerValue(SimpleValueHandler valueHandler) {
        register(new NodeHandler() {
            @Override
            public Object getValue(com.google.gson.JsonObject node, String portId, ltd.opens.mg.mc.core.blueprint.engine.NodeContext ctx) {
                return valueHandler.handle(node, portId, ctx);
            }
        });
    }

    @FunctionalInterface
    public interface SimpleValueHandler {
        Object handle(com.google.gson.JsonObject node, String portId, ltd.opens.mg.mc.core.blueprint.engine.NodeContext ctx);
    }

    /**
     * 获取内部 Builder（用于扩展或特殊配置）
     */
    public NodeDefinition.Builder getBuilder() {
        return builder;
    }

    // --- 高阶封装方法，减少样板代码 ---

    /**
     * 快速添加标准双参数数学输入（A, B）
     */
    public NodeHelper mathInputs(double defaultA, double defaultB) {
        return this.input(NodePorts.A, "node.mgmc.port.a", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, defaultA)
                   .input(NodePorts.B, "node.mgmc.port.b", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, defaultB);
    }

    /**
     * 快速添加标准数学输出（RESULT）
     */
    public NodeHelper mathOutput() {
        return this.output(NodePorts.RESULT, "node.mgmc.port.output", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT);
    }

    /**
     * 注册双参数数学运算逻辑
     */
    public void registerMathOp(BiFunction<Double, Double, Double> op) {
        // 自动补充默认输入输出
        if (builder.getInputs().isEmpty()) mathInputs(0.0, 0.0);
        if (builder.getOutputs().isEmpty()) mathOutput();

        registerValue((node, portId, ctx) -> {
            double a = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.A, ctx));
            double b = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.B, ctx));
            return op.apply(a, b);
        });
    }

    /**
     * 注册单参数数学运算逻辑
     */
    public void registerUnaryMathOp(Function<Double, Double> op) {
        if (builder.getInputs().isEmpty()) {
            input(NodePorts.INPUT, "node.mgmc.port.input", NodeDefinition.PortType.FLOAT, NodeThemes.COLOR_PORT_FLOAT, 0.0);
        }
        if (builder.getOutputs().isEmpty()) mathOutput();

        registerValue((node, portId, ctx) -> {
            double input = TypeConverter.toDouble(NodeLogicRegistry.evaluateInput(node, NodePorts.INPUT, ctx));
            return op.apply(input);
        });
    }
}
