package ltd.opens.mg.mc.client.gui.blueprint.settings;

import java.util.function.Consumer;

public class SettingsHelper {
    private final SettingsDefinition.Builder builder;

    private SettingsHelper(String id, String labelKey) {
        this.builder = new SettingsDefinition.Builder(id, labelKey);
    }

    public static SettingsHelper setup(String id, String labelKey) {
        return new SettingsHelper(id, labelKey);
    }

    public SettingsHelper description(String key) {
        builder.description(key);
        return this;
    }

    public SettingsHelper type(SettingType type) {
        builder.type(type);
        return this;
    }

    public SettingsHelper defaultValue(Object val) {
        builder.defaultValue(val);
        return this;
    }

    public SettingsHelper onChange(Consumer<Object> onChange) {
        builder.onChange(onChange);
        return this;
    }

    public void register() {
        SettingsRegistry.register(builder.build());
    }
}
