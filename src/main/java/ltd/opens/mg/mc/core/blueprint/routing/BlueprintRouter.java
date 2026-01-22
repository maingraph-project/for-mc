package ltd.opens.mg.mc.core.blueprint.routing;

import com.google.gson.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;
import java.nio.file.Files;
import java.nio.file.Path;

import ltd.opens.mg.mc.MaingraphforMC;
import ltd.opens.mg.mc.core.blueprint.BlueprintManager;
import ltd.opens.mg.mc.core.blueprint.engine.BlueprintEngine;

/**
 * 中央调度路由中心
 * 负责维护 Minecraft ID (ResourceLocation) 与蓝图文件路径之间的映射关系
 */
public class BlueprintRouter {
    private final Logger LOGGER = LogManager.getLogger();
    private final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    
    // 虚拟 ID 定义
    public static final String GLOBAL_ID = "mgmc:global";
    public static final String PLAYERS_ID = "mgmc:players";

    // 使用原子引用管理路由表，确保加载时的线程安全
    private final AtomicReference<Map<String, Set<String>>> routingTable = new AtomicReference<>(new ConcurrentHashMap<>());

    public BlueprintRouter() {
    }

    /**
     * 从指定世界的路由表文件加载
     */
    public void load(ServerLevel level) {
        Map<String, Set<String>> combinedTable = new ConcurrentHashMap<>();

        // 1. 加载全局映射 (仅在单机/非专服模式下加载)
        if (!BlueprintManager.isMultiplayer(level)) {
            Path globalPath = getMappingsPath(null);
            if (Files.exists(globalPath)) {
                try (FileReader reader = new FileReader(globalPath.toFile())) {
                    JsonObject json = GSON.fromJson(reader, JsonObject.class);
                    if (json != null) {
                        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                            JsonArray array = entry.getValue().getAsJsonArray();
                            Set<String> blueprints = Collections.newSetFromMap(new ConcurrentHashMap<>());
                            for (JsonElement e : array) {
                                blueprints.add(e.getAsString());
                            }
                            combinedTable.put(entry.getKey(), blueprints);
                        }
                    }
                    LOGGER.info("MGMC: Loaded {} global ID mappings", combinedTable.size());
                } catch (IOException e) {
                    LOGGER.error("MGMC: Failed to load global mappings", e);
                }
            }
        } else {
            LOGGER.info("MGMC: Global blueprints disabled in dedicated server mode.");
        }

        // 2. 加载存档映射并合并
        if (level != null) {
            Path savePath = getMappingsPath(level);
            if (Files.exists(savePath)) {
                try (FileReader reader = new FileReader(savePath.toFile())) {
                    JsonObject json = GSON.fromJson(reader, JsonObject.class);
                    if (json != null) {
                        for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                            JsonArray array = entry.getValue().getAsJsonArray();
                            Set<String> blueprints = combinedTable.computeIfAbsent(entry.getKey(), k -> Collections.newSetFromMap(new ConcurrentHashMap<>()));
                            for (JsonElement e : array) {
                                blueprints.add(e.getAsString());
                            }
                        }
                    }
                    LOGGER.info("MGMC: Merged ID mappings from {}", savePath);
                } catch (IOException e) {
                    LOGGER.error("MGMC: Failed to load save mappings from " + savePath, e);
                }
            } else {
                save(level); // 为存档创建初始文件
            }
        }

        routingTable.set(combinedTable);
    }

    /**
     * 保存路由表到指定世界的路由表文件
     */
    public Map<String, Set<String>> getRoutingTable() {
        return routingTable.get();
    }

    public synchronized void save(ServerLevel level) {
        Path filePath = getMappingsPath(level);
        try {
            Files.createDirectories(filePath.getParent());
            
            // 准备全局映射以便在保存存档时做差集
            Map<String, Set<String>> globalMappings = new HashMap<>();
            if (level != null) {
                Path globalPath = getMappingsPath(null);
                if (Files.exists(globalPath)) {
                    try (FileReader reader = new FileReader(globalPath.toFile())) {
                        JsonObject json = GSON.fromJson(reader, JsonObject.class);
                        if (json != null) {
                            for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                                if (!entry.getValue().isJsonArray()) continue;
                                Set<String> blueprints = new HashSet<>();
                                entry.getValue().getAsJsonArray().forEach(e -> blueprints.add(e.getAsString()));
                                globalMappings.put(entry.getKey(), blueprints);
                            }
                        }
                    } catch (IOException e) {
                        LOGGER.error("MGMC: Failed to load global mappings for subtraction", e);
                    }
                }
            }

            try (FileWriter writer = new FileWriter(filePath.toFile())) {
                JsonObject json = new JsonObject();
                Map<String, Set<String>> currentTable = routingTable.get();
                for (Map.Entry<String, Set<String>> entry : currentTable.entrySet()) {
                    String id = entry.getKey();
                    Set<String> blueprints = entry.getValue();
                    
                    if (level != null) {
                        // 如果是保存到存档，排除掉全局已有的映射，实现“增量保存”
                        Set<String> globalBps = globalMappings.getOrDefault(id, Collections.emptySet());
                        Set<String> localOnlyBps = new HashSet<>(blueprints);
                        localOnlyBps.removeAll(globalBps);
                        
                        if (!localOnlyBps.isEmpty()) {
                            JsonArray array = new JsonArray();
                            localOnlyBps.stream().sorted().forEach(array::add);
                            json.add(id, array);
                        }
                    } else {
                        // 如果是保存到全局，直接全部保存
                        JsonArray array = new JsonArray();
                        blueprints.stream().sorted().forEach(array::add);
                        json.add(id, array);
                    }
                }
                GSON.toJson(json, writer);
            }
        } catch (IOException e) {
            LOGGER.error("MGMC: Failed to save mappings to " + filePath, e);
        }
    }

    private Path getMappingsPath(ServerLevel level) {
        if (level == null) {
            return java.nio.file.Paths.get("mgmc_blueprints/.routing/mappings.json");
        }
        return level.getServer().getWorldPath(LevelResource.ROOT).resolve("mgmc_blueprints/.routing/mappings.json");
    }

    /**
     * 获取指定 ID 绑定的所有蓝图路径
     */
    public Set<String> getMappedBlueprints(String id) {
        return routingTable.get().getOrDefault(id, Collections.emptySet());
    }

    /**
     * 添加映射（注意：此方法目前仅由客户端通过网络请求触发，由 handleSaveMappings 统一处理保存）
     */
    public void addMapping(String id, String blueprintPath) {
        routingTable.get().compute(id, (k, blueprints) -> {
            if (blueprints == null) {
                blueprints = Collections.newSetFromMap(new ConcurrentHashMap<>());
            }
            blueprints.add(blueprintPath);
            return blueprints;
        });
    }

    /**
     * 移除映射
     */
    public void removeMapping(String id, String blueprintPath) {
        routingTable.get().computeIfPresent(id, (k, blueprints) -> {
            blueprints.remove(blueprintPath);
            return blueprints.isEmpty() ? null : blueprints;
        });
    }

    /**
     * 获取所有已订阅的 ID
     */
    public Set<String> getAllSubscribedIds() {
        return routingTable.get().keySet();
    }

    /**
     * 获取完整的路由表快照
     */
    public Map<String, Set<String>> getFullRoutingTable(ServerLevel level) {
        Map<String, Set<String>> copy = new HashMap<>();
        boolean isMultiplayer = BlueprintManager.isMultiplayer(level);
        Path saveDir = level != null ? MaingraphforMC.getServerManager().getBlueprintsDir(level) : null;

        routingTable.get().forEach((id, blueprints) -> {
            Set<String> filteredBlueprints = new HashSet<>();
            for (String path : blueprints) {
                // 如果是多人模式，检查该蓝图是否在存档目录中存在
                if (isMultiplayer && saveDir != null) {
                    if (Files.exists(saveDir.resolve(path))) {
                        filteredBlueprints.add(path);
                    }
                } else {
                    // 非多人模式，全部允许
                    filteredBlueprints.add(path);
                }
            }
            if (!filteredBlueprints.isEmpty()) {
                copy.put(id, filteredBlueprints);
            }
        });
        return copy;
    }

    /**
     * @deprecated Use {@link #getFullRoutingTable(ServerLevel)}
     */
    @Deprecated
    public Map<String, Set<String>> getFullRoutingTable() {
        return getFullRoutingTable(null);
    }

    /**
     * 批量更新路由表并保存
     */
    public void updateAllMappings(ServerLevel level, Map<String, Set<String>> newMappings) {
        routingTable.set(toConcurrentTable(newMappings));
        save(level);

        // 核心修复：当路由映射更新时，必须清理服务端缓存
        BlueprintManager manager = MaingraphforMC.getServerManager();
        if (manager != null) {
            manager.clearCaches();
            LOGGER.info("MGMC: Cleared BlueprintManager caches due to mapping update.");
        }
        BlueprintEngine.clearCaches();
        LOGGER.info("MGMC: Cleared BlueprintEngine caches due to mapping update.");
    }

    /**
     * 客户端专用的内存更新（不保存文件）
     */
    public void clientUpdateMappings(Map<String, Set<String>> newMappings) {
        routingTable.set(toConcurrentTable(newMappings));
    }

    private Map<String, Set<String>> toConcurrentTable(Map<String, Set<String>> source) {
        Map<String, Set<String>> newTable = new ConcurrentHashMap<>();
        source.forEach((k, v) -> {
            Set<String> set = Collections.newSetFromMap(new ConcurrentHashMap<>());
            set.addAll(v);
            newTable.put(k, set);
        });
        return newTable;
    }
}
