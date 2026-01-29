package ltd.opens.mg.mc.core.blueprint;

import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.events.MGMCEventContext;
import ltd.opens.mg.mc.core.blueprint.events.MGMCEventType;

import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * 存储事件节点的监听元数据
 */
public record EventMetadata(
    MGMCEventType eventType,
    BiConsumer<MGMCEventContext, NodeContext.Builder> contextPopulator,
    Function<MGMCEventContext, String> routingIdExtractor
) {
}
