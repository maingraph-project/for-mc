package ltd.opens.mg.mc.core.blueprint;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.MaingraphforMC;
import ltd.opens.mg.mc.core.blueprint.engine.BlueprintEngine;
import ltd.opens.mg.mc.core.blueprint.engine.NodeContext;
import ltd.opens.mg.mc.core.blueprint.routing.BlueprintRouter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.Event;
import net.neoforged.neoforge.common.NeoForge;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * 通用事件分发器
 * 负责监听 Minecraft 事件并根据 NodeDefinition 中的元数据分发到蓝图引擎。
 */
public class EventDispatcher {

    private static final Map<Class<? extends Event>, List<NodeDefinition>> EVENT_NODES = new ConcurrentHashMap<>();
    private static final Set<Class<? extends Event>> REGISTERED_CLASSES = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final Map<Class<? extends Event>, List<NodeDefinition>> CONCRETE_CACHE = new ConcurrentHashMap<>();

    /**
     * 初始化分发器，从注册表中提取所有事件节点并注册监听。
     */
    public static void init() {
        EVENT_NODES.clear();
        REGISTERED_CLASSES.clear();
        CONCRETE_CACHE.clear();

        for (NodeDefinition def : NodeRegistry.getAllDefinitions()) {
            EventMetadata metadata = (EventMetadata) def.properties().get("event_metadata");
            if (metadata != null) {
                EVENT_NODES.computeIfAbsent(metadata.eventClass(), k -> new ArrayList<>()).add(def);
                registerListener(metadata.eventClass());
            }
        }
        MaingraphforMC.LOGGER.info("EventDispatcher initialized with {} event types", EVENT_NODES.size());
    }

    public static void clear() {
        CONCRETE_CACHE.clear();
    }

    private static <T extends Event> void registerListener(Class<T> eventClass) {
        if (REGISTERED_CLASSES.contains(eventClass)) return;
        
        NeoForge.EVENT_BUS.addListener(eventClass, (Consumer<T>) EventDispatcher::handleEvent);
        REGISTERED_CLASSES.add(eventClass);
    }

    private static <T extends Event> void handleEvent(T event) {
        Class<? extends Event> concreteClass = event.getClass();
        List<NodeDefinition> defs = CONCRETE_CACHE.computeIfAbsent(concreteClass, clazz -> {
            List<NodeDefinition> matched = new ArrayList<>();
            for (Map.Entry<Class<? extends Event>, List<NodeDefinition>> entry : EVENT_NODES.entrySet()) {
                if (entry.getKey().isAssignableFrom(clazz)) {
                    matched.addAll(entry.getValue());
                }
            }
            return matched.isEmpty() ? Collections.emptyList() : matched;
        });

        // 获取 Level (通过 ContextProvider)
        for (NodeDefinition def : defs) {
            EventMetadata metadata = (EventMetadata) def.properties().get("event_metadata");
            if (metadata == null) continue;

            Level level = metadata.contextProvider().getLevel(event);
            if (!(level instanceof ServerLevel serverLevel)) {
                continue;
            }

            String routingId = null;
            try {
                routingId = metadata.routingIdExtractor().apply(event);
            } catch (Exception e) {
                MaingraphforMC.LOGGER.error("MGMC: Error extracting routing ID for node " + def.id(), e);
            }
            
            if (routingId == null) continue;

            // 收集所有可能的 ID
            List<String> ids = new ArrayList<>();
            ids.add(BlueprintRouter.GLOBAL_ID);
            ids.add(routingId);
            
            // 获取 Player (通过 ContextProvider)
            Player player = metadata.contextProvider().getPlayer(event);
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
                metadata.contextPopulator().accept(event, contextBuilder);
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

    // 移除硬编码方法
    private static void checkItemScripts(net.minecraft.world.item.ItemStack stack, ServerLevel level, List<JsonObject> out) {
        if (stack.isEmpty()) return;
        var manager = MaingraphforMC.getServerManager();
        if (manager == null) return;
        
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
