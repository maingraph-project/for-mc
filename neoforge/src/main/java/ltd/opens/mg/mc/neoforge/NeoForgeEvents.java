package ltd.opens.mg.mc.neoforge;

import ltd.opens.mg.mc.core.blueprint.EventDispatcher;
import ltd.opens.mg.mc.core.blueprint.events.MGMCEventType;
import ltd.opens.mg.mc.core.blueprint.events.MGMCEventContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

public class NeoForgeEvents {
    public static void init() {
        // 左键点击方块事件（NeoForge使用PlayerInteractEvent.LeftClickBlock）
        NeoForge.EVENT_BUS.addListener(PlayerInteractEvent.LeftClickBlock.class, event -> {
            Level level = event.getLevel();
            if (level.isClientSide()) return;

            Player player = event.getEntity();
            BlockPos pos = event.getPos();

            EventDispatcher.dispatch(MGMCEventType.BLOCK_LEFT_CLICK, MGMCEventContext.builder(level)
                .pos(pos)
                .player(player)
                .entity(player)
                .blockState(level.getBlockState(pos))
                .build());
        });
    }
}