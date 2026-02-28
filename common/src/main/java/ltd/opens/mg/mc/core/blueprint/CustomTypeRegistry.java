package ltd.opens.mg.mc.core.blueprint;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自定义端口类型注册表（附属 Mod 使用）
 */
public final class CustomTypeRegistry {
    private static final Map<String, String> TYPES = new ConcurrentHashMap<>();

    private CustomTypeRegistry() {}

    /**
     * 注册附属 Mod 自定义类型。typeId 不得包含 ":"，会自动加上 modId 前缀。
     */
    public static String registerAddonType(String modId, String typeId) {
        if (modId == null || modId.trim().isEmpty()) {
            throw new IllegalArgumentException("modId cannot be empty");
        }
        if (typeId == null || typeId.trim().isEmpty()) {
            throw new IllegalArgumentException("typeId cannot be empty");
        }
        if (typeId.contains(":")) {
            throw new IllegalArgumentException("typeId must not contain ':'");
        }
        String fullId = modId + ":" + typeId;
        TYPES.put(fullId, fullId);
        return fullId;
    }

    public static boolean isRegistered(String typeId) {
        return TYPES.containsKey(typeId);
    }
}
