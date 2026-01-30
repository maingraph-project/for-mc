package ltd.opens.mg.mc.core.blueprint.engine;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.core.blueprint.data.XYZ;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class NodeContext {
    public final Level level;
    public final String eventName;
    public final String[] args;
    public final String triggerUuid;
    public final String triggerName;
    public final Entity triggerEntity;
    public final double triggerX;
    public final double triggerY;
    public final double triggerZ;
    public final XYZ triggerXYZ;
    public final double triggerSpeed;
    public final String triggerBlockId;
    public final String triggerItemId;
    public final double triggerValue;
    public final String triggerExtraUuid;
    public final Entity triggerExtraEntity;
    public final Map<String, JsonObject> nodesMap;
    public final int formatVersion;
    public final Map<String, Object> properties;
    public final Map<String, Object> variables = new ConcurrentHashMap<>();
    public final Map<String, Map<String, Object>> runtimeData = new ConcurrentHashMap<>();
    public final AtomicBoolean breakRequested = new AtomicBoolean(false);
    public final AtomicInteger nodeExecCount = new AtomicInteger(0);
    public volatile String lastTriggeredPin;
    public volatile String currentBlueprintName = "";
    
    // 用于子蓝图调用的返回数据
    public final java.util.List<Object> returnList = new java.util.ArrayList<>();
    public final NodeContext parentContext;

    public Object getRuntimeData(String nodeId, String key, Object defaultValue) {
        Map<String, Object> nodeData = runtimeData.get(nodeId);
        if (nodeData == null) return defaultValue;
        return nodeData.getOrDefault(key, defaultValue);
    }

    public void setRuntimeData(String nodeId, String key, Object value) {
        runtimeData.computeIfAbsent(nodeId, k -> new ConcurrentHashMap<>()).put(key, value);
    }

    public NodeContext(Level level, String eventName, String[] args, String triggerUuid, String triggerName, Entity triggerEntity,
                       double triggerX, double triggerY, double triggerZ, double triggerSpeed,
                       String triggerBlockId, String triggerItemId, double triggerValue, String triggerExtraUuid, Entity triggerExtraEntity,
                       Map<String, JsonObject> nodesMap, int formatVersion, Map<String, Object> properties, NodeContext parentContext) {
        this.level = level;
        this.eventName = eventName;
        this.args = args;
        this.triggerUuid = triggerUuid;
        this.triggerName = triggerName;
        this.triggerEntity = triggerEntity;
        this.triggerX = triggerX;
        this.triggerY = triggerY;
        this.triggerZ = triggerZ;
        this.triggerXYZ = new XYZ(triggerX, triggerY, triggerZ);
        this.triggerSpeed = triggerSpeed;
        this.triggerBlockId = triggerBlockId;
        this.triggerItemId = triggerItemId;
        this.triggerValue = triggerValue;
        this.triggerExtraUuid = triggerExtraUuid;
        this.triggerExtraEntity = triggerExtraEntity;
        this.nodesMap = nodesMap;
        this.formatVersion = formatVersion;
        this.properties = properties != null ? properties : new HashMap<>();
        this.parentContext = parentContext;
    }

    public NodeContext(Level level, String eventName, String[] args, String triggerUuid, String triggerName, Entity triggerEntity,
                       double triggerX, double triggerY, double triggerZ, double triggerSpeed,
                       String triggerBlockId, String triggerItemId, double triggerValue, String triggerExtraUuid, Entity triggerExtraEntity,
                       Map<String, JsonObject> nodesMap, int formatVersion) {
        this(level, eventName, args, triggerUuid, triggerName, triggerEntity, triggerX, triggerY, triggerZ, triggerSpeed,
             triggerBlockId, triggerItemId, triggerValue, triggerExtraUuid, triggerExtraEntity, nodesMap, formatVersion, new HashMap<>(), null);
    }

    public static class Builder {
        private Level level;
        private String eventName = "";
        private String[] args = new String[0];
        private String triggerUuid = "";
        private String triggerName = "";
        private Entity triggerEntity;
        private double triggerX;
        private double triggerY;
        private double triggerZ;
        private double triggerSpeed;
        private String triggerBlockId = "";
        private String triggerItemId = "";
        private double triggerValue;
        private String triggerExtraUuid = "";
        private Entity triggerExtraEntity;
        private Map<String, JsonObject> nodesMap = new HashMap<>();
        private int formatVersion = 1;
        private String blueprintName = "";
        private Map<String, Object> properties = new HashMap<>();
        private NodeContext parentContext;

        public Builder(Level level) {
            this.level = level;
        }

        public Builder parentContext(NodeContext parentContext) {
            this.parentContext = parentContext;
            return this;
        }

        public Builder blueprintName(String name) {
            this.blueprintName = name;
            return this;
        }

        public Builder eventName(String eventName) { this.eventName = eventName; return this; }
        public Builder args(String[] args) { this.args = args; return this; }
        public Builder triggerUuid(String triggerUuid) { this.triggerUuid = triggerUuid; return this; }
        public Builder triggerName(String triggerName) { this.triggerName = triggerName; return this; }
        public Builder triggerEntity(Entity entity) { this.triggerEntity = entity; return this; }
        public Builder triggerX(double triggerX) { this.triggerX = triggerX; return this; }
        public Builder triggerY(double triggerY) { this.triggerY = triggerY; return this; }
        public Builder triggerZ(double triggerZ) { this.triggerZ = triggerZ; return this; }
        public Builder triggerSpeed(double triggerSpeed) { this.triggerSpeed = triggerSpeed; return this; }
        public Builder triggerBlockId(String triggerBlockId) { this.triggerBlockId = triggerBlockId; return this; }
        public Builder triggerItemId(String triggerItemId) { this.triggerItemId = triggerItemId; return this; }
        public Builder triggerValue(double triggerValue) { this.triggerValue = triggerValue; return this; }
        public Builder triggerExtraUuid(String triggerExtraUuid) { this.triggerExtraUuid = triggerExtraUuid; return this; }
        public Builder triggerExtraEntity(Entity entity) { this.triggerExtraEntity = entity; return this; }
        public Builder nodesMap(Map<String, JsonObject> nodesMap) { this.nodesMap = nodesMap; return this; }
        public Builder formatVersion(int formatVersion) { this.formatVersion = formatVersion; return this; }

        public Builder addProperty(String key, Object value) {
            this.properties.put(key, value);
            return this;
        }

        public NodeContext build() {
            NodeContext ctx = new NodeContext(level, eventName, args, triggerUuid, triggerName, triggerEntity, 
                                 triggerX, triggerY, triggerZ, triggerSpeed, 
                                 triggerBlockId, triggerItemId, triggerValue, triggerExtraUuid, triggerExtraEntity, 
                                 nodesMap, formatVersion, properties, parentContext);
            ctx.currentBlueprintName = blueprintName;
            return ctx;
        }
    }
}
