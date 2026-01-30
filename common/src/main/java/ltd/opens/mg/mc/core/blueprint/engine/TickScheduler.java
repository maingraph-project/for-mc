package ltd.opens.mg.mc.core.blueprint.engine;

import com.google.gson.JsonObject;
import ltd.opens.mg.mc.MaingraphforMC;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TickScheduler {

    private static final List<SuspendedTask> TASKS = new ArrayList<>();

    public static void schedule(NodeContext context, JsonObject node, String pinId, int ticks) {
        synchronized (TASKS) {
            TASKS.add(new SuspendedTask(context, node, pinId, ticks));
        }
        if (ticks > 0) {
            MaingraphforMC.LOGGER.info("Scheduled blueprint task for {} ticks later", ticks);
        }
    }

    public static void tick(net.minecraft.world.level.Level level) {
        if (level.isClientSide) return;
        
        List<SuspendedTask> toFire = new ArrayList<>();
        
        synchronized (TASKS) {
            if (TASKS.isEmpty()) return;

            Iterator<SuspendedTask> iterator = TASKS.iterator();
            while (iterator.hasNext()) {
                SuspendedTask task = iterator.next();
                task.remainingTicks--;
                if (task.remainingTicks <= 0) {
                    toFire.add(task);
                    iterator.remove();
                }
            }
        }

        // 在同步块外部执行，避免死锁和并发修改异常
        for (SuspendedTask task : toFire) {
            try {
                MaingraphforMC.LOGGER.info("Resuming suspended blueprint task");
                NodeLogicRegistry.triggerExec(task.node, task.pinId, task.context);
            } catch (Exception e) {
                MaingraphforMC.LOGGER.error("Error resuming suspended blueprint task", e);
            }
        }
    }

    private static class SuspendedTask {
        final NodeContext context;
        final JsonObject node;
        final String pinId;
        int remainingTicks;

        SuspendedTask(NodeContext context, JsonObject node, String pinId, int ticks) {
            this.context = context;
            this.node = node;
            this.pinId = pinId;
            this.remainingTicks = ticks;
        }
    }
}
