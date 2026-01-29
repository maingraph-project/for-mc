package ltd.opens.mg.mc.core.blueprint;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ltd.opens.mg.mc.core.blueprint.routing.BlueprintRouter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class BlueprintManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final ExecutorService IO_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "MGMC-IO-Thread");
        t.setDaemon(true);
        return t;
    });

    private final Map<String, CachedBlueprint> blueprintCache = new ConcurrentHashMap<>();
    private final List<JsonObject> allBlueprintsCache = new ArrayList<>();
    private long lastAllBlueprintsRefresh = 0;
    private static final long CACHE_REFRESH_INTERVAL = 1000; // 1 second

    private final BlueprintRouter router;
    private final List<LogEntry> logCache = Collections.synchronizedList(new LinkedList<>());
    private static final int MAX_LOG_SIZE = 100;

    public static record LogEntry(String blueprintName, String nodeId, String level, String message, long timestamp) {}

    public BlueprintManager() {
        this.router = new BlueprintRouter();
    }

    public void addLog(String blueprintName, String nodeId, String level, String message) {
        logCache.add(0, new LogEntry(blueprintName, nodeId, level, message, System.currentTimeMillis()));
        while (logCache.size() > MAX_LOG_SIZE) {
            logCache.remove(logCache.size() - 1);
        }
    }

    public List<LogEntry> getLogs() {
        return new ArrayList<>(logCache);
    }

    public BlueprintRouter getRouter() {
        return router;
    }

    public void clearCaches() {
        blueprintCache.clear();
        allBlueprintsCache.clear();
        lastAllBlueprintsRefresh = 0;
    }

    private static record CachedBlueprint(JsonObject json, long lastModified, long version) {}

    private boolean isValidFileName(String name) {
        if (name == null || name.isEmpty()) return false;
        // 允许中文和其他非特殊字符
        return !name.contains("..") && !name.contains("/") && !name.contains("\\") && !name.matches(".*[\\\\/:*?\"<>|].*");
    }

    private boolean containsNodeOfType(JsonObject blueprint, String typeId) {
        if (!blueprint.has("execution") || !blueprint.get("execution").isJsonArray()) {
            return false;
        }
        com.google.gson.JsonArray executionNodes = blueprint.getAsJsonArray("execution");
        for (com.google.gson.JsonElement e : executionNodes) {
            if (!e.isJsonObject()) continue;
            JsonObject node = e.getAsJsonObject();
            if (node.has("type")) {
                String type = node.get("type").getAsString();
                if (type.equals(typeId) || type.endsWith(":" + typeId)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Path getBlueprintsDir(ServerLevel level) {
        if (level == null) {
            return getGlobalBlueprintsDir();
        }
        Path dir = level.getServer().getWorldPath(LevelResource.ROOT).resolve("mgmc_blueprints");
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (Exception e) {
                LOGGER.error("Failed to create blueprints directory: " + dir, e);
            }
        }
        return dir;
    }

    public static Path getGlobalBlueprintsDir() {
        Path dir = java.nio.file.Paths.get("mgmc_blueprints");
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (Exception e) {
                LogManager.getLogger().error("Failed to create global blueprints directory: " + dir, e);
            }
        }
        return dir;
    }

    public static boolean isMultiplayer(ServerLevel level) {
        if (level == null || level.getServer() == null) return false;
        return level.getServer().isDedicatedServer() || level.getServer().isPublished();
    }

    public JsonObject getBlueprint(ServerLevel level, String name) {
        if (!isValidFileName(name)) return null;
        try {
            final String fileName = name.endsWith(".json") ? name : name + ".json";
            
            // 1. 尝试从存档目录加载
            Path dataFile = null;
            if (level != null) {
                dataFile = getBlueprintsDir(level).resolve(fileName);
            }
            
            // 2. 如果存档目录没有，且不是专服/联机模式，尝试从全局目录加载
            if ((dataFile == null || !Files.exists(dataFile)) && level != null) {
                if (!isMultiplayer(level)) {
                    Path globalFile = getGlobalBlueprintsDir().resolve(fileName);
                    if (Files.exists(globalFile)) {
                        dataFile = globalFile;
                    }
                }
            }
            
            // 如果是主界面模式 (level == null)，直接用全局目录
            if (level == null) {
                dataFile = getGlobalBlueprintsDir().resolve(fileName);
            }
            
            if (dataFile != null && Files.exists(dataFile)) {
                long lastModified = Files.getLastModifiedTime(dataFile).toMillis();
                CachedBlueprint cached = blueprintCache.get(fileName);
                
                // 如果缓存不存在或文件已更新，则重新加载
                if (cached == null || lastModified > cached.lastModified) {
                    synchronized (this) { // 简单同步防止并发重复读取同一文件
                        // 双重检查
                        cached = blueprintCache.get(fileName);
                        if (cached == null || lastModified > cached.lastModified) {
                            String json = Files.readString(dataFile);
                            JsonObject obj = JsonParser.parseString(json).getAsJsonObject();
                            long version = 0;
                            if (obj.has("_version") && obj.get("_version").isJsonPrimitive() && obj.get("_version").getAsJsonPrimitive().isNumber()) {
                                version = obj.get("_version").getAsLong();
                            }
                            cached = new CachedBlueprint(obj, lastModified, version);
                            blueprintCache.put(fileName, cached);
                        }
                    }
                }
                return cached.json;
            }
        } catch (Exception e) {
            LOGGER.error("Failed to read blueprint: " + name, e);
        }
        return null;
    }

    public long getBlueprintVersion(ServerLevel level, String name) {
        if (!isValidFileName(name)) return -1;
        String fileName = name.endsWith(".json") ? name : name + ".json";
        getBlueprint(level, fileName); // Ensure it's in cache
        CachedBlueprint cached = blueprintCache.get(fileName);
        return cached != null ? cached.version : -1;
    }

    public CompletableFuture<SaveResult> saveBlueprintAsync(ServerLevel level, String name, String data, long expectedVersion) {
        if (!isValidFileName(name)) {
            return CompletableFuture.completedFuture(new SaveResult(false, "Invalid file name.", -1));
        }
        
        return CompletableFuture.supplyAsync(() -> {
            try {
                String fileName = name.endsWith(".json") ? name : name + ".json";
                Path dataFile = getBlueprintsDir(level).resolve(fileName);
                
                long currentVersion = getBlueprintVersion(level, name);
                
                if (currentVersion != -1 && expectedVersion != -1 && currentVersion != expectedVersion) {
                    return new SaveResult(false, "Race condition detected: blueprint has been modified by another user.", currentVersion);
                }

                JsonObject obj = JsonParser.parseString(data).getAsJsonObject();

                // Security check: run_command_as_server node
                if (!ltd.opens.mg.mc.Config.isServerRunCommandNodeAllowed()) {
                    if (containsNodeOfType(obj, "run_command_as_server")) {
                        return new SaveResult(false, "Security violation: 'Run Command as Server' node is disabled in server configuration.", -1);
                    }
                }

                long newVersion = (currentVersion == -1 ? 0 : currentVersion) + 1;
                obj.addProperty("_version", newVersion);
                obj.addProperty("format_version", 5);
                
                // 移除对 "name" 字段的写入，现在以前台显示的文件名为准
                Files.writeString(dataFile, obj.toString());
                
                blueprintCache.put(fileName, new CachedBlueprint(obj, System.currentTimeMillis(), newVersion));
                synchronized (allBlueprintsCache) {
                    lastAllBlueprintsRefresh = 0; 
                }
                
                return new SaveResult(true, "Saved successfully.", newVersion);
            } catch (Exception e) {
                return new SaveResult(false, "Save failed: " + e.getMessage(), -1);
            }
        }, IO_EXECUTOR);
    }

    public CompletableFuture<Boolean> deleteBlueprintAsync(ServerLevel level, String name) {
        if (!isValidFileName(name)) return CompletableFuture.completedFuture(false);
        return CompletableFuture.supplyAsync(() -> {
            try {
                String fileName = name.endsWith(".json") ? name : name + ".json";
                Path dataFile = getBlueprintsDir(level).resolve(fileName);
                if (Files.exists(dataFile)) {
                    Files.delete(dataFile);
                    blueprintCache.remove(fileName);
                    synchronized (allBlueprintsCache) {
                        lastAllBlueprintsRefresh = 0;
                    }
                    return true;
                }
            } catch (IOException e) {
                LOGGER.error("Failed to delete blueprint: " + name, e);
            }
            return false;
        }, IO_EXECUTOR);
    }

    public CompletableFuture<Boolean> renameBlueprintAsync(ServerLevel level, String oldName, String newName) {
        if (!isValidFileName(oldName) || !isValidFileName(newName)) return CompletableFuture.completedFuture(false);
        return CompletableFuture.supplyAsync(() -> {
            try {
                String oldFileName = oldName.endsWith(".json") ? oldName : oldName + ".json";
                String newFileName = newName.endsWith(".json") ? newName : newName + ".json";
                Path oldPath = getBlueprintsDir(level).resolve(oldFileName);
                Path newPath = getBlueprintsDir(level).resolve(newFileName);

                if (Files.exists(oldPath) && !Files.exists(newPath)) {
                    // Load, update name property, and save to new location
                    JsonObject bp = getBlueprint(level, oldName);
                    if (bp != null) {
                        // 移除对 "name" 字段的写入，现在以前台显示的文件名为准
                        Files.writeString(newPath, bp.toString());
                        Files.delete(oldPath);
                        
                        blueprintCache.remove(oldFileName);
                        blueprintCache.put(newFileName, new CachedBlueprint(bp, System.currentTimeMillis(), getBlueprintVersion(level, oldName)));
                        
                        synchronized (allBlueprintsCache) {
                            lastAllBlueprintsRefresh = 0;
                        }
                        return true;
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Failed to rename blueprint: " + oldName + " to " + newName, e);
            }
            return false;
        }, IO_EXECUTOR);
    }

    public CompletableFuture<Boolean> duplicateBlueprintAsync(ServerLevel level, String sourceName, String targetName) {
        if (!isValidFileName(sourceName) || !isValidFileName(targetName)) return CompletableFuture.completedFuture(false);
        return CompletableFuture.supplyAsync(() -> {
            try {
                String sourceFileName = sourceName.endsWith(".json") ? sourceName : sourceName + ".json";
                String targetFileName = targetName.endsWith(".json") ? targetName : targetName + ".json";
                Path sourcePath = getBlueprintsDir(level).resolve(sourceFileName);
                Path targetPath = getBlueprintsDir(level).resolve(targetFileName);

                if (Files.exists(sourcePath) && !Files.exists(targetPath)) {
                    JsonObject bp = getBlueprint(level, sourceName);
                    if (bp != null) {
                        // Create a deep copy to avoid modifying the original in cache
                        JsonObject copy = JsonParser.parseString(bp.toString()).getAsJsonObject();
                        // 移除对 "name" 字段的写入，现在以前台显示的文件名为准
                        copy.addProperty("_version", 1L); // Reset version for new file
                        
                        Files.writeString(targetPath, copy.toString());
                        
                        blueprintCache.put(targetFileName, new CachedBlueprint(copy, System.currentTimeMillis(), 1L));
                        
                        synchronized (allBlueprintsCache) {
                            lastAllBlueprintsRefresh = 0;
                        }
                        return true;
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Failed to duplicate blueprint: " + sourceName + " to " + targetName, e);
            }
            return false;
        }, IO_EXECUTOR);
    }

    public SaveResult saveBlueprint(ServerLevel level, String name, String data, long expectedVersion) {
        try {
            return saveBlueprintAsync(level, name, data, expectedVersion).get(5, TimeUnit.SECONDS);
        } catch (Exception e) {
            return new SaveResult(false, "Save timeout or error: " + e.getMessage(), -1);
        }
    }

    public record SaveResult(boolean success, String message, long newVersion) {}

    public Collection<JsonObject> getAllBlueprints(ServerLevel level) {
        return getAllBlueprints(level, false);
    }

    public Collection<JsonObject> getAllBlueprints(ServerLevel level, boolean force) {
        long now = System.currentTimeMillis();
        synchronized (allBlueprintsCache) {
            if (!force && now - lastAllBlueprintsRefresh < CACHE_REFRESH_INTERVAL) {
                return new ArrayList<>(allBlueprintsCache);
            }
        }

        Map<String, JsonObject> all = new LinkedHashMap<>();
        try {
            // 1. 优先加载存档蓝图
            Path dir = getBlueprintsDir(level);
            try (var stream = Files.list(dir)) {
                stream.filter(p -> p.toString().endsWith(".json")).forEach(p -> {
                    String name = p.getFileName().toString();
                    JsonObject bp = getBlueprint(level, name);
                    if (bp != null) all.put(name, bp);
                });
            }

            // 2. 如果不是专服/联机模式，加载全局蓝图（不覆盖同名存档蓝图）
            if (!isMultiplayer(level)) {
                Path globalDir = getGlobalBlueprintsDir();
                try (var stream = Files.list(globalDir)) {
                    stream.filter(p -> p.toString().endsWith(".json")).forEach(p -> {
                        String name = p.getFileName().toString();
                        if (!all.containsKey(name)) {
                            JsonObject bp = getBlueprint(level, name);
                            if (bp != null) all.put(name, bp);
                        }
                    });
                }
            }

            synchronized (allBlueprintsCache) {
                allBlueprintsCache.clear();
                allBlueprintsCache.addAll(all.values());
                lastAllBlueprintsRefresh = now;
            }
        } catch (IOException e) {
            LOGGER.error("Failed to list blueprints", e);
        }

        return new ArrayList<>(all.values());
    }

    public List<JsonObject> getBlueprintsForId(ServerLevel level, String... ids) {
        List<JsonObject> result = new ArrayList<>();
        Set<String> processedPaths = new HashSet<>();
        
        for (String id : ids) {
            for (String path : router.getMappedBlueprints(id)) {
                if (processedPaths.add(path)) {
                    JsonObject bp = getBlueprint(level, path);
                    if (bp != null) result.add(bp);
                }
            }
        }
        return result;
    }
}
