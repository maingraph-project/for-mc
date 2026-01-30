package ltd.opens.mg.mc.core.blueprint;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NodeRegistry {
    private static final Map<String, NodeDefinition> REGISTRY = new ConcurrentHashMap<>();
    private static boolean frozen = false;

    public static void freeze() {
        frozen = true;
    }

    public static boolean isFrozen() {
        return frozen;
    }

    public static void register(NodeDefinition definition) {
        if (frozen) {
            throw new IllegalStateException("Cannot register node after registry is frozen: " + definition.id());
        }
        if (REGISTRY.containsKey(definition.id())) {
            NodeDefinition existing = REGISTRY.get(definition.id());
            String errorMsg = String.format(
                "\n\n================================================================\n" +
                "Maingraph For MC has detected critical errors: Node ID Conflict!\n" +
                "The following node ID is already registered:\n" +
                " - \"%s\" (Attempted by mod: %s, already registered by mod: %s)\n" +
                "================================================================\n",
                definition.id(), definition.registeredBy(), existing.registeredBy()
            );
            
            // 先在标准错误流和日志中打印，确保即便异常被拦截也能看到
            System.err.println(errorMsg);
            ltd.opens.mg.mc.MaingraphforMC.LOGGER.error(errorMsg);
            
            throw new IllegalStateException("Node ID Conflict: " + definition.id());
        }
        REGISTRY.put(definition.id(), definition);
    }

    public static NodeDefinition get(String id) {
        return REGISTRY.get(id);
    }

    public static Collection<NodeDefinition> getAllDefinitions() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }
}
