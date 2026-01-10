package ltd.opens.mg.mc.core.blueprint.routing;

import com.google.gson.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 中央调度路由中心
 * 负责维护 Minecraft ID (ResourceLocation) 与蓝图文件路径之间的映射关系
 */
public class BlueprintRouter {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String MAPPINGS_FILE = "mgmc_blueprints/mappings.json";
    
    // 虚拟 ID 定义
    public static final String GLOBAL_ID = "mgmc:global";
    public static final String PLAYERS_ID = "mgmc:players";

    // 内存中的路由表: ID -> 蓝图路径列表
    private static final Map<String, Set<String>> routingTable = new ConcurrentHashMap<>();

    /**
     * 初始化路由表，从 mappings.json 加载
     */
    public static void init() {
        File file = new File(MAPPINGS_FILE);
        if (!file.exists()) {
            save(); // 创建初始文件
            return;
        }

        try (FileReader reader = new FileReader(file)) {
            JsonObject json = GSON.fromJson(reader, JsonObject.class);
            routingTable.clear();
            if (json != null) {
                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    JsonArray array = entry.getValue().getAsJsonArray();
                    Set<String> blueprints = Collections.newSetFromMap(new ConcurrentHashMap<>());
                    for (JsonElement e : array) {
                        blueprints.add(e.getAsString());
                    }
                    routingTable.put(entry.getKey(), blueprints);
                }
            }
            LOGGER.info("MGMC: Loaded {} ID mappings from {}", routingTable.size(), MAPPINGS_FILE);
        } catch (IOException e) {
            LOGGER.error("MGMC: Failed to load mappings.json", e);
        }
    }

    /**
     * 保存路由表到 mappings.json
     */
    public static synchronized void save() {
        File dir = new File("mgmc_blueprints");
        if (!dir.exists()) dir.mkdirs();

        try (FileWriter writer = new FileWriter(MAPPINGS_FILE)) {
            JsonObject json = new JsonObject();
            for (Map.Entry<String, Set<String>> entry : routingTable.entrySet()) {
                JsonArray array = new JsonArray();
                entry.getValue().forEach(array::add);
                json.add(entry.getKey(), array);
            }
            GSON.toJson(json, writer);
        } catch (IOException e) {
            LOGGER.error("MGMC: Failed to save mappings.json", e);
        }
    }

    /**
     * 获取指定 ID 绑定的所有蓝图路径
     */
    public static Set<String> getMappedBlueprints(String id) {
        return routingTable.getOrDefault(id, Collections.emptySet());
    }

    /**
     * 添加映射
     */
    public static void addMapping(String id, String blueprintPath) {
        routingTable.computeIfAbsent(id, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                    .add(blueprintPath);
        save();
    }

    /**
     * 移除映射
     */
    public static void removeMapping(String id, String blueprintPath) {
        Set<String> blueprints = routingTable.get(id);
        if (blueprints != null) {
            blueprints.remove(blueprintPath);
            if (blueprints.isEmpty()) {
                routingTable.remove(id);
            }
            save();
        }
    }

    /**
     * 获取所有已订阅的 ID
     */
    public static Set<String> getAllSubscribedIds() {
        return routingTable.keySet();
    }

    /**
     * 获取完整的路由表快照
     */
    public static Map<String, Set<String>> getFullRoutingTable() {
        Map<String, Set<String>> copy = new HashMap<>();
        routingTable.forEach((k, v) -> copy.put(k, new HashSet<>(v)));
        return copy;
    }

    /**
     * 批量更新路由表
     */
    public static void updateAllMappings(Map<String, Set<String>> newMappings) {
        routingTable.clear();
        newMappings.forEach((k, v) -> {
            Set<String> set = Collections.newSetFromMap(new ConcurrentHashMap<>());
            set.addAll(v);
            routingTable.put(k, set);
        });
        save();
    }
}
