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
 * 全局变量管理器，负责持久化存储蓝图运行时的全局变量
 */
public class GlobalVariableManager {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "mgmc_variables.json";
    
    private final Map<String, Object> globalVariables = new ConcurrentHashMap<>();
    private Path variablesFile;

    public void load(ServerLevel level) {
        this.variablesFile = level.getServer().getWorldPath(LevelResource.ROOT).resolve(FILE_NAME);
        globalVariables.clear();
        
        if (Files.exists(variablesFile)) {
            try {
                String content = Files.readString(variablesFile);
                JsonObject json = JsonParser.parseString(content).getAsJsonObject();
                json.entrySet().forEach(entry -> {
                    com.google.gson.JsonElement value = entry.getValue();
                    if (value.isJsonPrimitive()) {
                        JsonPrimitive primitive = value.getAsJsonPrimitive();
                        if (primitive.isBoolean()) globalVariables.put(entry.getKey(), primitive.getAsBoolean());
                        else if (primitive.isNumber()) globalVariables.put(entry.getKey(), primitive.getAsNumber().doubleValue());
                        else if (primitive.isString()) globalVariables.put(entry.getKey(), primitive.getAsString());
                    } else if (value.isJsonArray()) {
                        globalVariables.put(entry.getKey(), TypeConverter.toList(value));
                    } else {
                        globalVariables.put(entry.getKey(), value);
                    }
                });
                LOGGER.info("MGMC: Loaded {} global variables from {}", globalVariables.size(), variablesFile);
            } catch (Exception e) {
                LOGGER.error("Failed to load global variables", e);
            }
        }
    }

    public void save() {
        if (variablesFile == null) return;
        
        try {
            String json = GSON.toJson(globalVariables);
            Files.writeString(variablesFile, json);
            LOGGER.debug("MGMC: Saved global variables to {}", variablesFile);
        } catch (IOException e) {
            LOGGER.error("Failed to save global variables", e);
        }
    }

    public Object get(String name) {
        return globalVariables.get(name);
    }

    public void set(String name, Object value) {
        if (value == null) {
            globalVariables.remove(name);
        } else {
            globalVariables.put(name, value);
        }
    }

    public void clear() {
        globalVariables.clear();
        variablesFile = null;
    }
}
