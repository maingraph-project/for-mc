package ltd.opens.mg.mc.core.blueprint.engine;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.MaingraphforMC;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;

public class TickScheduler {

    private static final Map<net.minecraft.world.level.Level, PriorityQueue<SuspendedTask>> TASKS = new HashMap<>();
    private static final Comparator<SuspendedTask> TASK_ORDER = Comparator.comparingLong(t -> t.targetGameTime);

    public static void schedule(NodeContext context, JsonObject node, String pinId, int ticks) {
        net.minecraft.world.level.Level level = context.level;
        if (level == null || level.isClientSide) {
            MaingraphforMC.LOGGER.debug("Skipping scheduling task for client or null level");
            return;
        }

        synchronized (TASKS) {
            TASKS.computeIfAbsent(level, k -> new PriorityQueue<>(TASK_ORDER))
                .add(new SuspendedTask(context, node, pinId, ticks));
        }
        if (ticks > 0) {
            MaingraphforMC.LOGGER.debug("Scheduled blueprint task for {} ticks later", ticks);
        }
    }

    public static void tick() {
        List<SuspendedTask> toFire = new ArrayList<>();
        
        synchronized (TASKS) {
            if (TASKS.isEmpty()) return;

            var levelIterator = TASKS.entrySet().iterator();
            while (levelIterator.hasNext()) {
                var entry = levelIterator.next();
                var level = entry.getKey();
                var queue = entry.getValue();
                if (level == null || level.isClientSide) {
                    levelIterator.remove();
                    continue;
                }

                long now = level.getGameTime();
                while (!queue.isEmpty() && queue.peek().targetGameTime <= now) {
                    toFire.add(queue.poll());
                }

                if (queue.isEmpty()) {
                    levelIterator.remove();
                }
            }
        }

        // 在同步块外部执行，避免死锁和并发修改异常
        for (SuspendedTask task : toFire) {
            try {
                MaingraphforMC.LOGGER.debug("Resuming suspended blueprint task");
                NodeLogicRegistry.triggerExec(task.node, task.pinId, task.context);
            } catch (Exception e) {
                MaingraphforMC.LOGGER.error("Error resuming suspended blueprint task", e);
            }
        }
    }

    public static void clear() {
        synchronized (TASKS) {
            TASKS.clear();
        }
    }

    public static void clear(net.minecraft.world.level.Level level) {
        if (level == null) return;
        synchronized (TASKS) {
            TASKS.remove(level);
        }
    }

    private static class SuspendedTask {
        final NodeContext context;
        final JsonObject node;
        final String pinId;
        final net.minecraft.world.level.Level level;
        final long targetGameTime;

        SuspendedTask(NodeContext context, JsonObject node, String pinId, int ticks) {
            this.context = context;
            this.node = node;
            this.pinId = pinId;
            this.level = context.level;
            long baseTime = (this.level != null) ? this.level.getGameTime() : 0L;
            this.targetGameTime = baseTime + Math.max(0, ticks);
        }
    }
}
