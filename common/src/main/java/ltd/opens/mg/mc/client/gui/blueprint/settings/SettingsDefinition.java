package ltd.opens.mg.mc.client.gui.blueprint.settings;

import java.util.function.Consumer;

public class SettingsDefinition {
    public final String id;
    public final String labelKey;
    public final String descriptionKey;
    public final SettingType type;
    public final Object defaultValue;
    public final Consumer<Object> onChange;
    public final float min;
    public final float max;

    public SettingsDefinition(String id, String labelKey, String descriptionKey, SettingType type, Object defaultValue, Consumer<Object> onChange, float min, float max) {
        this.id = id;
        this.labelKey = labelKey;
        this.descriptionKey = descriptionKey;
        this.type = type;
        this.defaultValue = defaultValue;
        this.onChange = onChange;
        this.min = min;
        this.max = max;
    }

    public static class Builder {
        private String id;
        private String labelKey;
        private String descriptionKey;
        private SettingType type;
        private Object defaultValue;
        private Consumer<Object> onChange;
        private float min = 0.0f;
        private float max = 1.0f;

        public Builder(String id, String labelKey) {
            this.id = id;
            this.labelKey = labelKey;
        }

        public Builder description(String descriptionKey) {
            this.descriptionKey = descriptionKey;
            return this;
        }

        public Builder type(SettingType type) {
            this.type = type;
            return this;
        }

        public Builder defaultValue(Object defaultValue) {
            this.defaultValue = defaultValue;
            return this;
        }

        public Builder onChange(Consumer<Object> onChange) {
            this.onChange = onChange;
            return this;
        }

        public Builder range(float min, float max) {
            this.min = min;
            this.max = max;
            return this;
        }

        public SettingsDefinition build() {
            return new SettingsDefinition(id, labelKey, descriptionKey, type, defaultValue, onChange, min, max);
        }
    }
}
