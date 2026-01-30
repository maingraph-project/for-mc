package ltd.opens.mg.mc.core.blueprint;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import ltd.opens.mg.mc.core.blueprint.engine.TypeConverter;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.storage.LevelResource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 实体变量管理器，负责持久化存储实体的持久化变量（基于 UUID）
 */
public class EntityVariableManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "mgmc_entity_variables.json";
    
    // Map<EntityUUID, Map<VariableName, Value>>
    private final Map<String, Map<String, Object>> entityVariables = new ConcurrentHashMap<>();
    private Path variablesFile;

    public void load(ServerLevel level) {
        this.variablesFile = level.getServer().getWorldPath(LevelResource.ROOT).resolve(FILE_NAME);
        entityVariables.clear();
        
        if (Files.exists(variablesFile)) {
            try {
                String content = Files.readString(variablesFile);
                JsonObject root = JsonParser.parseString(content).getAsJsonObject();
                
                root.entrySet().forEach(entityEntry -> {
                    String uuid = entityEntry.getKey();
                    if (entityEntry.getValue().isJsonObject()) {
                        Map<String, Object> vars = new ConcurrentHashMap<>();
                        JsonObject varsJson = entityEntry.getValue().getAsJsonObject();
                        
                        varsJson.entrySet().forEach(varEntry -> {
                            com.google.gson.JsonElement value = varEntry.getValue();
                            if (value.isJsonPrimitive()) {
                                JsonPrimitive primitive = value.getAsJsonPrimitive();
                                if (primitive.isBoolean()) vars.put(varEntry.getKey(), primitive.getAsBoolean());
                                else if (primitive.isNumber()) vars.put(varEntry.getKey(), primitive.getAsNumber().doubleValue());
                                else if (primitive.isString()) vars.put(varEntry.getKey(), primitive.getAsString());
                            } else if (value.isJsonArray()) {
                                vars.put(varEntry.getKey(), TypeConverter.toList(value));
                            } else {
                                vars.put(varEntry.getKey(), value);
                            }
                        });
                        entityVariables.put(uuid, vars);
                    }
                });
                LOGGER.info("MGMC: Loaded persistent variables for {} entities from {}", entityVariables.size(), variablesFile);
            } catch (Exception e) {
                LOGGER.error("Failed to load entity variables", e);
            }
        }
    }

    public void save() {
        if (variablesFile == null) return;
        
        try {
            String json = GSON.toJson(entityVariables);
            Files.writeString(variablesFile, json);
            LOGGER.debug("MGMC: Saved entity variables to {}", variablesFile);
        } catch (IOException e) {
            LOGGER.error("Failed to save entity variables", e);
        }
    }

    public Object get(String uuid, String name) {
        Map<String, Object> vars = entityVariables.get(uuid);
        return vars != null ? vars.get(name) : null;
    }

    public void set(String uuid, String name, Object value) {
        if (uuid == null || uuid.isEmpty()) return;
        
        if (value == null) {
            Map<String, Object> vars = entityVariables.get(uuid);
            if (vars != null) {
                vars.remove(name);
                if (vars.isEmpty()) {
                    entityVariables.remove(uuid);
                }
            }
        } else {
            entityVariables.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>()).put(name, value);
        }
    }

    public void clear() {
        entityVariables.clear();
        variablesFile = null;
    }
}
