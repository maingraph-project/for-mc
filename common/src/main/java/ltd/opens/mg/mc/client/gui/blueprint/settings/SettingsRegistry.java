package ltd.opens.mg.mc.client.gui.blueprint.settings;

import java.util.*;

public class SettingsRegistry {
    private static final Map<String, SettingsDefinition> definitions = new LinkedHashMap<>();
    private static final Map<String, Object> values = new HashMap<>();

    public static void register(SettingsDefinition def) {
        definitions.put(def.id, def);
        if (!values.containsKey(def.id)) {
            values.put(def.id, def.defaultValue);
        }
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

    public static void set(String id, Object value) {
        if (definitions.containsKey(id)) {
            values.put(id, value);
            SettingsDefinition def = definitions.get(id);
            if (def.onChange != null) {
                def.onChange.accept(value);
            }
        }
    }
}
