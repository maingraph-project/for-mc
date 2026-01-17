package ltd.opens.mg.mc.core.blueprint.engine;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ltd.opens.mg.mc.MaingraphforMC;
import net.minecraft.world.level.Level;

import java.util.*;

public class BlueprintEngine {

    private static final ThreadLocal<Integer> RECURSION_DEPTH = ThreadLocal.withInitial(() -> 0);
    
    // 缓存蓝图的索引信息，使用 WeakHashMap 防止内存泄漏
    private static final Map<JsonObject, Map<String, List<JsonObject>>> EVENT_INDEX_CACHE = Collections.synchronizedMap(new WeakHashMap<>());
    private static final Map<JsonObject, Map<String, JsonObject>> NODE_MAP_CACHE = Collections.synchronizedMap(new WeakHashMap<>());

    public static void execute(Level level, String json, String eventType, String name, String[] args, 
                                String triggerUuid, String triggerName, double tx, double ty, double tz, double speed) {
        execute(level, json, eventType, name, args, triggerUuid, triggerName, tx, ty, tz, speed, "", "", 0.0, "");
    }

    public static void execute(Level level, String json, String eventType, String name, String[] args, 
                                String triggerUuid, String triggerName, double tx, double ty, double tz, double speed,
                                String triggerBlockId, String triggerItemId, double triggerValue, String triggerExtraUuid) {
        try {
            JsonObject root = JsonParser.parseString(json).getAsJsonObject();
            execute(level, root, eventType, name, args, triggerUuid, triggerName, tx, ty, tz, speed, triggerBlockId, triggerItemId, triggerValue, triggerExtraUuid);
        } catch (Exception e) {
            MaingraphforMC.LOGGER.error("Error parsing blueprint JSON", e);
        }
    }

    public static void execute(Level level, JsonObject root, String eventType, String name, String[] args, 
                                String triggerUuid, String triggerName, double tx, double ty, double tz, double speed) {
        execute(level, root, eventType, name, args, triggerUuid, triggerName, tx, ty, tz, speed, "", "", 0.0, "");
    }

    public static void execute(Level level, JsonObject root, String eventType, String name, String[] args, 
                                String triggerUuid, String triggerName, double tx, double ty, double tz, double speed,
                                String triggerBlockId, String triggerItemId, double triggerValue, String triggerExtraUuid) {
        NodeContext.Builder builder = new NodeContext.Builder(level)
            .blueprintName(name)
            .eventName(eventType)
            .args(args)
            .triggerUuid(triggerUuid)
            .triggerName(triggerName)
            .triggerX(tx).triggerY(ty).triggerZ(tz)
            .triggerSpeed(speed)
            .triggerBlockId(triggerBlockId)
            .triggerItemId(triggerItemId)
            .triggerValue(triggerValue)
            .triggerExtraUuid(triggerExtraUuid);
        
        // 自动尝试根据 UUID 获取实体，以便向后兼容 /mgrun 等命令
        if (level instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            try {
                if (triggerUuid != null && !triggerUuid.isEmpty()) {
                    builder.triggerEntity(serverLevel.getEntity(java.util.UUID.fromString(triggerUuid)));
                }
                if (triggerExtraUuid != null && !triggerExtraUuid.isEmpty()) {
                    builder.triggerExtraEntity(serverLevel.getEntity(java.util.UUID.fromString(triggerExtraUuid)));
                }
            } catch (Exception ignored) {}
        }
        
        execute(level, root, eventType, builder);
    }

    public static void clearCaches() {
        EVENT_INDEX_CACHE.clear();
        NODE_MAP_CACHE.clear();
    }

    public static void execute(Level level, JsonObject root, String eventType, NodeContext.Builder contextBuilder) {
        if (RECURSION_DEPTH.get() >= ltd.opens.mg.mc.Config.getMaxRecursionDepth()) {
            return;
        }

        RECURSION_DEPTH.set(RECURSION_DEPTH.get() + 1);
        try {
            if (!root.has("execution") || !root.get("execution").isJsonArray()) {
                return;
            }

            // 1. 获取或构建节点 ID 映射表 (用于 NodeContext)
            Map<String, JsonObject> nodesMap = NODE_MAP_CACHE.computeIfAbsent(root, r -> {
                JsonArray executionNodes = r.getAsJsonArray("execution");
                Map<String, JsonObject> map = new HashMap<>();
                for (JsonElement e : executionNodes) {
                    if (!e.isJsonObject()) continue;
                    JsonObject node = e.getAsJsonObject();
                    if (node.has("id")) {
                        map.put(node.get("id").getAsString(), node);
                    }
                }
                return map;
            });

            // 2. 获取或构建事件索引表 (用于快速定位入口节点)
            Map<String, List<JsonObject>> eventIndex = EVENT_INDEX_CACHE.computeIfAbsent(root, r -> {
                JsonArray executionNodes = r.getAsJsonArray("execution");
                Map<String, List<JsonObject>> index = new HashMap<>();
                for (JsonElement e : executionNodes) {
                    if (!e.isJsonObject()) continue;
                    JsonObject node = e.getAsJsonObject();
                    String type = node.has("type") ? node.get("type").getAsString() : null;
                    if (type != null) {
                        // 统一存储不带命名空间的类型
                        String pureType = type.contains(":") ? type.substring(type.indexOf(":") + 1) : type;
                        index.computeIfAbsent(pureType, k -> new ArrayList<>()).add(node);
                    }
                }
                return index;
            });

            int formatVersion = root.has("format_version") ? root.get("format_version").getAsInt() : 1;
            String blueprintName = root.has("name") ? root.get("name").getAsString() : "unknown";
            
            NodeContext ctx = contextBuilder
                .blueprintName(blueprintName)
                .nodesMap(nodesMap)
                .formatVersion(formatVersion)
                .build();

            // 3. 根据事件类型快速检索
            String pureEvent = eventType.contains(":") ? eventType.substring(eventType.indexOf(":") + 1) : eventType;
            List<JsonObject> targetNodes = eventIndex.get(pureEvent);
            
            if (targetNodes != null) {
                for (JsonObject node : targetNodes) {
                    // 检查 'name' 输出是否匹配请求的名称 (针对 mgrun 等自定义事件)
                    String nodeName = TypeConverter.toString(NodeLogicRegistry.evaluateOutput(node, "name", ctx));
                    if (ctx.eventName.isEmpty() || ctx.eventName.equals(nodeName)) {
                        NodeLogicRegistry.triggerExec(node, "exec", ctx);
                    }
                }
            }
        } catch (Exception e) {
            MaingraphforMC.LOGGER.error("Error executing blueprint", e);
        } finally {
            RECURSION_DEPTH.set(RECURSION_DEPTH.get() - 1);
        }
    }
}
