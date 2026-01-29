package ltd.opens.mg.mc.core.blueprint;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.MaingraphforMC;
import ltd.opens.mg.mc.core.blueprint.engine.BlueprintEngine;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.events.MGMCEventContext;
import ltd.opens.mg.mc.core.blueprint.events.MGMCEventType;
import ltd.opens.mg.mc.core.blueprint.routing.BlueprintRouter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 通用事件分发器
 * 负责接收平台特定事件并根据 NodeDefinition 中的元数据分发到蓝图引擎。
 */
public class EventDispatcher {

    private static final Map<MGMCEventType, List<NodeDefinition>> EVENT_NODES = new ConcurrentHashMap<>();

    /**
     * 初始化分发器，从注册表中提取所有事件节点。
     */
    public static void init() {
        EVENT_NODES.clear();

        for (NodeDefinition def : NodeRegistry.getAllDefinitions()) {
            Object meta = def.properties().get("event_metadata");
            if (meta instanceof EventMetadata metadata) {
                EVENT_NODES.computeIfAbsent(metadata.eventType(), k -> new ArrayList<>()).add(def);
            }
        }
        MaingraphforMC.LOGGER.info("EventDispatcher initialized with {} event types", EVENT_NODES.size());
    }

    public static void clear() {
        // No cache to clear for now
    }

    public static void dispatch(MGMCEventType type, MGMCEventContext context) {
        List<NodeDefinition> defs = EVENT_NODES.get(type);
        if (defs == null || defs.isEmpty()) return;

        Level level = context.getLevel();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        for (NodeDefinition def : defs) {
            EventMetadata metadata = (EventMetadata) def.properties().get("event_metadata");
            if (metadata == null) continue;

            String routingId = null;
            try {
                routingId = metadata.routingIdExtractor().apply(context);
            } catch (Exception e) {
                MaingraphforMC.LOGGER.error("MGMC: Error extracting routing ID for node " + def.id(), e);
            }
            
            if (routingId == null) continue;

            // 收集所有可能的 ID
            List<String> ids = new ArrayList<>();
            ids.add(BlueprintRouter.GLOBAL_ID);
            ids.add(routingId);
            
            Player player = context.getPlayer();
            if (player != null) {
                ids.add(BlueprintRouter.PLAYERS_ID);
                ids.add(player.getUUID().toString());
            }

            // 获取绑定的蓝图
            var manager = MaingraphforMC.getServerManager();
            if (manager == null) continue;
            List<JsonObject> blueprints = new ArrayList<>(manager.getBlueprintsForId(serverLevel, ids.toArray(new String[0])));

            // 检查物品绑定的脚本
            if (player != null) {
                checkItemScripts(player.getMainHandItem(), serverLevel, blueprints);
                checkItemScripts(player.getOffhandItem(), serverLevel, blueprints);
            }

            if (blueprints.isEmpty()) continue;

            // 构造 Context
            NodeContext.Builder contextBuilder = new NodeContext.Builder(serverLevel);
            try {
                metadata.contextPopulator().accept(context, contextBuilder);
            } catch (Exception e) {
                MaingraphforMC.LOGGER.error("MGMC: Error populating context for node " + def.id(), e);
                continue;
            }
            
            // 执行蓝图
            for (JsonObject blueprint : blueprints) {
                MaingraphforMC.LOGGER.debug("MGMC: Executing blueprint for node {} (IDs: {})", def.id(), ids);
                BlueprintEngine.execute(serverLevel, blueprint, def.id(), contextBuilder);
            }
        }
    }

    private static void checkItemScripts(net.minecraft.world.item.ItemStack stack, ServerLevel level, List<JsonObject> out) {
        if (stack.isEmpty()) return;
        var manager = MaingraphforMC.getServerManager();
        if (manager == null) return;
        
        if (stack.has(ltd.opens.mg.mc.core.registry.MGMCRegistries.BLUEPRINT_SCRIPTS.get())) {
             List<String> scripts = stack.get(ltd.opens.mg.mc.core.registry.MGMCRegistries.BLUEPRINT_SCRIPTS.get());
             if (scripts != null) {
                for (String path : scripts) {
                    JsonObject bp = manager.getBlueprint(level, path);
                    if (bp != null) {
                        out.add(bp);
                    }
                }
             }
        }
    }
}