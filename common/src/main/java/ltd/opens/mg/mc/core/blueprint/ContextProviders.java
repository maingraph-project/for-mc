package ltd.opens.mg.mc.core.blueprint;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 事件上下文提供者注册表
 */
public class ContextProviders {
    private static final Map<Class<?>, EventContextProvider<?>> PROVIDERS = new ConcurrentHashMap<>();

    /**
     * 注册一个新的事件上下文提供者
     */
    public static <T> void register(Class<T> eventClass, EventContextProvider<T> provider) {
        PROVIDERS.put(eventClass, provider);
    }

    /**
     * 获取指定事件的上下文提供者
     */
    @SuppressWarnings("unchecked")
    public static <T> EventContextProvider<T> getProvider(Class<T> eventClass) {
        // 首先尝试直接匹配
        EventContextProvider<T> provider = (EventContextProvider<T>) PROVIDERS.get(eventClass);
        if (provider != null) return provider;

        // 尝试匹配父类
        for (Map.Entry<Class<?>, EventContextProvider<?>> entry : PROVIDERS.entrySet()) {
            if (entry.getKey().isAssignableFrom(eventClass)) {
                return (EventContextProvider<T>) entry.getValue();
            }
        }

        return (EventContextProvider<T>) DEFAULT_PROVIDER;
    }

    private static final EventContextProvider<Object> DEFAULT_PROVIDER = new EventContextProvider<>() {
        @Override
        public Level getLevel(Object event) {
            return null;
        }

        @Override
        public Player getPlayer(Object event) {
            return null;
        }
    };
}
