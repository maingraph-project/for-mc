package ltd.opens.mg.mc.core.blueprint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record NodeDefinition(
    String id,
    String name,
    String category,
    int color,
    List<PortDefinition> inputs,
    List<PortDefinition> outputs,
    Map<String, Object> properties
) {
    public static class Builder {
        private final String id;
        private final String name;
        private String category = "General";
        private int color = 0xFF444444;
        private final List<PortDefinition> inputs = new ArrayList<>();
        private final List<PortDefinition> outputs = new ArrayList<>();
        private final Map<String, Object> properties = new HashMap<>();

        public Builder(String id, String name) {
            this.id = id;
            this.name = name;
        }

        public Builder category(String category) {
            this.category = category;
            return this;
        }

        public Builder color(int color) {
            this.color = color;
            return this;
        }

        public Builder addProperty(String key, Object value) {
            this.properties.put(key, value);
            return this;
        }

        public Builder addInput(String id, String displayName, PortType type, int color) {
            inputs.add(new PortDefinition(id, displayName, type, color, false, null, null));
            return this;
        }

        public Builder addInput(String id, PortType type, int color) {
            return addInput(id, id, type, color);
        }

        public Builder addInput(String id, String displayName, PortType type, int color, boolean hasInput, Object defaultValue) {
            inputs.add(new PortDefinition(id, displayName, type, color, hasInput, defaultValue, null));
            return this;
        }

        public Builder addInput(String id, PortType type, int color, boolean hasInput, Object defaultValue) {
            return addInput(id, id, type, color, hasInput, defaultValue);
        }

        public Builder addInput(String id, String displayName, PortType type, int color, boolean hasInput, Object defaultValue, String[] options) {
            inputs.add(new PortDefinition(id, displayName, type, color, hasInput, defaultValue, options));
            return this;
        }

        public Builder addInput(String id, PortType type, int color, boolean hasInput, Object defaultValue, String[] options) {
            return addInput(id, id, type, color, hasInput, defaultValue, options);
        }

        public Builder addOutput(String id, String displayName, PortType type, int color) {
            outputs.add(new PortDefinition(id, displayName, type, color, false, null, null));
            return this;
        }

        public Builder addOutput(String id, PortType type, int color) {
            return addOutput(id, id, type, color);
        }

        public NodeDefinition build() {
            return new NodeDefinition(id, name, category, color, List.copyOf(inputs), List.copyOf(outputs), Map.copyOf(properties));
        }
    }

    public record PortDefinition(String id, String displayName, PortType type, int color, boolean hasInput, Object defaultValue, String[] options) {}

    public enum PortType {
        EXEC, STRING, FLOAT, BOOLEAN, OBJECT, LIST, UUID, ENUM, ANY
    }
}
