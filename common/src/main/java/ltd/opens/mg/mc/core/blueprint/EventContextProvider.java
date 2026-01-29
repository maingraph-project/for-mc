package ltd.opens.mg.mc.core.blueprint;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.Nullable;

/**
 * 事件上下文提供者接口
 * 用于从特定事件中提取 Level 和 Player 信息
 */
@FunctionalInterface
public interface EventContextProvider<T> {
    /**
     * 从事件中提取 Level
     */
    @Nullable
    Level getLevel(T event);

    /**
     * 从事件中提取 Player（可选）
     */
    @Nullable
    default Player getPlayer(T event) {
        return null;
    }
}
