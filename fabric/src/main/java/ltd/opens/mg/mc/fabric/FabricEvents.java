package ltd.opens.mg.mc.fabric;

import ltd.opens.mg.mc.core.blueprint.EventDispatcher;
import ltd.opens.mg.mc.core.blueprint.events.MGMCEventType;
import ltd.opens.mg.mc.core.blueprint.events.MGMCEventContext;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.Direction;

public class FabricEvents {
    public static void init() {
        // 左键点击方块事件
        AttackBlockCallback.EVENT.register((player, level, hand, pos, direction) -> {
            if (level.isClientSide()) return InteractionResult.PASS;

            EventDispatcher.dispatch(MGMCEventType.BLOCK_LEFT_CLICK, MGMCEventContext.builder(level)
                .pos(pos)
                .player(player)
                .entity(player)
                .blockState(level.getBlockState(pos))
                .build());

            // 不取消事件，让其他处理器继续处理
            return InteractionResult.PASS;
        });
    }
}