package ltd.opens.mg.mc.client.gui.blueprint.settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import dev.architectury.platform.Platform;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class SettingsRegistry {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String FILE_NAME = "mgmc_settings.json";

    private static final Map<String, SettingsDefinition> definitions = new LinkedHashMap<>();
    private static final Map<String, Object> values = new HashMap<>();
    private static final Map<String, Object> loadedValues = new HashMap<>();

    private static Path settingsFile;
    private static boolean loaded = false;

    private static void ensureLoaded() {
        if (loaded) return;
        loaded = true;
        try {
            if (settingsFile == null) {
                settingsFile = Platform.getConfigFolder().resolve(FILE_NAME);
            }
            if (Files.exists(settingsFile)) {
                String content = Files.readString(settingsFile);
                JsonElement root = JsonParser.parseString(content);
                if (root.isJsonObject()) {
                    JsonObject json = root.getAsJsonObject();
                    for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                        Object value = fromJson(entry.getValue());
                        if (value != null) {
                            loadedValues.put(entry.getKey(), value);
                        }
                    }
                }
                LOGGER.debug("MGMC: Loaded {} settings from {}", loadedValues.size(), settingsFile);
            }
        } catch (Exception e) {
            LOGGER.error("MGMC: Failed to load editor settings", e);
        }
    }

    public static void register(SettingsDefinition def) {
        ensureLoaded();
        definitions.put(def.id, def);

        Object value = def.defaultValue;
        if (loadedValues.containsKey(def.id)) {
            Object persisted = coerce(def, loadedValues.get(def.id));
            if (persisted != null) {
                value = persisted;
            }
        }
        values.put(def.id, value);
    }

    public static Collection<SettingsDefinition> getDefinitions() {
        return definitions.values();
    }

    public static Object get(String id) {
        return values.get(id);
    }

    public static boolean getBoolean(String id) {
        Object val = values.get(id);
        if (val instanceof Boolean) {
            return (Boolean) val;
        }
        return false;
    }

    public static float getFloat(String id) {
        Object val = values.get(id);
        if (val instanceof Number) {
            return ((Number) val).floatValue();
        }
        return 0.0f;
    }

    public static void set(String id, Object value) {
        if (definitions.containsKey(id)) {
            values.put(id, value);
            SettingsDefinition def = definitions.get(id);
            if (def.onChange != null) {
                def.onChange.accept(value);
            }
            save();
        }
    }

    public static void save() {
        ensureLoaded();
        if (settingsFile == null) {
            settingsFile = Platform.getConfigFolder().resolve(FILE_NAME);
        }
        try {
            JsonObject json = new JsonObject();
            for (Map.Entry<String, SettingsDefinition> entry : definitions.entrySet()) {
                String id = entry.getKey();
                Object val = values.get(id);
                if (val != null) {
                    json.add(id, toJson(val));
                }
            }
            Files.writeString(settingsFile, GSON.toJson(json));
            LOGGER.debug("MGMC: Saved {} settings to {}", definitions.size(), settingsFile);
        } catch (IOException e) {
            LOGGER.error("MGMC: Failed to save editor settings", e);
        }
    }

    private static Object fromJson(JsonElement el) {
        if (el == null || el.isJsonNull()) {
            return null;
        }
        if (el.isJsonPrimitive()) {
            JsonPrimitive p = el.getAsJsonPrimitive();
            if (p.isBoolean()) {
                return p.getAsBoolean();
            }
            if (p.isNumber()) {
                return p.getAsDouble();
            }
            return p.getAsString();
        }
        return null;
    }

    private static JsonElement toJson(Object val) {
        return GSON.toJsonTree(val);
    }

    private static Object coerce(SettingsDefinition def, Object raw) {
        switch (def.type) {
            case BOOLEAN:
                if (raw instanceof Boolean) {
                    return raw;
                }
                if (raw instanceof String) {
                    return Boolean.parseBoolean((String) raw);
                }
                return null;
            case FLOAT:
                if (raw instanceof Number) {
                    return ((Number) raw).floatValue();
                }
                if (raw instanceof String) {
                    try {
                        return Float.parseFloat((String) raw);
                    } catch (NumberFormatException ignored) {
                        return null;
                    }
                }
                return null;
            default:
                return null;
        }
    }
}
