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

/**
 * 通用事件分发器
 * 负责接收平台特定事件并根据 NodeDefinition 中的元数据分发到蓝图引擎。
 */
public class EventDispatcher {

    /**
     * 事件节点索引快照（不可变，重建时整体替换以保证并发读取安全）。
     * 配合 {@code eventNodesVersion} 与 {@link NodeRegistry#getVersion()}，
     * 可在节点注册表发生变动（例如附属 mod 在引擎初始化之后才注册事件节点）后自动重建，
     * 不再依赖固定的初始化注册顺序。
     */
    private static volatile Map<MGMCEventType, List<NodeDefinition>> EVENT_NODES = new java.util.HashMap<>();
    private static long eventNodesVersion = -1;

    /**
     * 依据当前节点注册表重建事件索引。
     * 注册表（含附属 mod 在初始化后注册的节点）发生变动后调用即可生效，
     * 无需在引擎初始化阶段强制规定注册顺序。
     */
    private static void rebuildIndex() {
        Map<MGMCEventType, List<NodeDefinition>> index = new java.util.HashMap<>();
        for (NodeDefinition def : NodeRegistry.getAllDefinitions()) {
            Object meta = def.properties().get("event_metadata");
            if (meta instanceof EventMetadata metadata) {
                index.computeIfAbsent(metadata.eventType(), k -> new ArrayList<>()).add(def);
            }
        }
        EVENT_NODES = index;
        eventNodesVersion = NodeRegistry.getVersion();
    }

    /**
     * 初始化事件分发器（构建初始索引）。运行时若节点注册表发生变动，
     * {@link #dispatch} 会自动重建索引，因此本方法无需关心注册时机。
     */
    public static void init() {
        rebuildIndex();
        MaingraphforMC.LOGGER.info("EventDispatcher initialized with {} event types", EVENT_NODES.size());
    }

    public static void clear() {
        EVENT_NODES = new java.util.HashMap<>();
        eventNodesVersion = -1;
    }

    public static void dispatch(MGMCEventType type, MGMCEventContext context) {
        // 若注册表自上次构建后发生过变动（例如附属 mod 较晚注册了事件节点），重建索引。
        // 正常路径下仅做一次 long 比较，几乎零开销。
        if (eventNodesVersion != NodeRegistry.getVersion()) {
            rebuildIndex();
        }

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