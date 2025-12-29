package ltd.opens.mg.mc.core.blueprint.engine;

import com.google.gson.JsonObject;
import java.util.Map;

public class NodeContext {
    public final String eventName;
    public final String[] args;
    public final String triggerUuid;
    public final String triggerName;
    public final double triggerX;
    public final double triggerY;
    public final double triggerZ;
    public final Map<String, JsonObject> nodesMap;

    public NodeContext(String eventName, String[] args, String triggerUuid, String triggerName, 
                       double triggerX, double triggerY, double triggerZ, Map<String, JsonObject> nodesMap) {
        this.eventName = eventName;
        this.args = args;
        this.triggerUuid = triggerUuid;
        this.triggerName = triggerName;
        this.triggerX = triggerX;
        this.triggerY = triggerY;
        this.triggerZ = triggerZ;
        this.nodesMap = nodesMap;
    }
}
