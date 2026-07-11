package ltd.opens.mg.mc.core.blueprint;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NodeRegistry {
    private static final Map<String, NodeDefinition> REGISTRY = new ConcurrentHashMap<>();
    private static boolean frozen = false;
    private static final java.util.concurrent.atomic.AtomicLong VERSION = new java.util.concurrent.atomic.AtomicLong();

    public static void freeze() {
        frozen = true;
    }

    public static boolean isFrozen() {
        return frozen;
    }

    /**
     * 注册表版本号，每次成功注册新节点时自增。
     * 供 EventDispatcher 在注册表发生变动（如附属 mod 晚注册事件节点）后感知并重建索引。
     */
    public static long getVersion() {
        return VERSION.get();
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
        VERSION.incrementAndGet();
    }

    public static NodeDefinition get(String id) {
        return REGISTRY.get(id);
    }

    public static Collection<NodeDefinition> getAllDefinitions() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }
}
