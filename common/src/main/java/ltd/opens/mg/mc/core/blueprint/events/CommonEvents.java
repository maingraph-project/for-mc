package ltd.opens.mg.mc.core.blueprint.events;

import dev.architectury.event.EventResult;
import dev.architectury.event.CompoundEventResult;
import dev.architectury.event.events.common.BlockEvent;
import dev.architectury.event.events.common.EntityEvent;
import dev.architectury.event.events.common.InteractionEvent;
import dev.architectury.event.events.common.PlayerEvent;
import ltd.opens.mg.mc.core.blueprint.EventDispatcher;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity.RemovalReason;

public class CommonEvents {
    public static void init() {
        BlockEvent.BREAK.register((level, pos, state, player, xp) -> {
            if (level.isClientSide()) return EventResult.pass();
            EventDispatcher.dispatch(MGMCEventType.BLOCK_BREAK, MGMCEventContext.builder(level)
                .pos(pos)
                .blockState(state)
                .player(player)
                .build());
            return EventResult.pass();
        });

        BlockEvent.PLACE.register((level, pos, state, placer) -> {
            if (level.isClientSide()) return EventResult.pass();
            Player player = (placer instanceof Player) ? (Player) placer : null;
            EventDispatcher.dispatch(MGMCEventType.BLOCK_PLACE, MGMCEventContext.builder(level)
                .pos(pos)
                .blockState(state)
                .player(player)
                .entity(placer)
                .build());
            return EventResult.pass();
        });

        InteractionEvent.RIGHT_CLICK_BLOCK.register((player, hand, pos, face) -> {
            if (player.level().isClientSide()) return EventResult.pass();
            EventDispatcher.dispatch(MGMCEventType.BLOCK_INTERACT, MGMCEventContext.builder(player.level())
                .pos(pos)
                .player(player)
                .blockState(player.level().getBlockState(pos))
                .build());
            return EventResult.pass();
        });
        
        InteractionEvent.RIGHT_CLICK_ITEM.register((player, hand) -> {
            if (player.level().isClientSide()) return CompoundEventResult.pass();
            EventDispatcher.dispatch(MGMCEventType.ITEM_USE, MGMCEventContext.builder(player.level())
                .player(player)
                .entity(player)
                .item(player.getItemInHand(hand))
                .build());
            return CompoundEventResult.pass();
        });

        PlayerEvent.ATTACK_ENTITY.register((player, level, target, hand, result) -> {
            if (level.isClientSide()) return EventResult.pass();
            EventDispatcher.dispatch(MGMCEventType.PLAYER_ATTACK, MGMCEventContext.builder(level)
                .player(player)
                .entity(player)
                .targetEntity(target)
                .build());
            return EventResult.pass();
        });

        PlayerEvent.PLAYER_JOIN.register(player -> {
            if (player.level().isClientSide()) return;
            EventDispatcher.dispatch(MGMCEventType.PLAYER_JOIN, MGMCEventContext.builder(player.level())
                .player(player)
                .entity(player)
                .build());
        });
        
        EntityEvent.ADD.register((entity, level) -> {
            if (level.isClientSide()) return EventResult.pass();
            EventDispatcher.dispatch(MGMCEventType.ENTITY_SPAWN, MGMCEventContext.builder(level)
                .entity(entity)
                .build());
            return EventResult.pass();
        });

        EntityEvent.LIVING_DEATH.register((entity, source) -> {
            if (entity.level().isClientSide()) return EventResult.pass();
            
            EventDispatcher.dispatch(MGMCEventType.ENTITY_DEATH, MGMCEventContext.builder(entity.level())
                .entity(entity)
                .damageSource(source)
                .build());
                
            if (entity instanceof Player player) {
                EventDispatcher.dispatch(MGMCEventType.PLAYER_DEATH, MGMCEventContext.builder(player.level())
                    .player(player)
                    .entity(player)
                    .damageSource(source)
                    .build());
            }
            return EventResult.pass();
        });

        PlayerEvent.PLAYER_RESPAWN.register((player, conqueredEnd, reason) -> {
            if (player.level().isClientSide()) return;
            EventDispatcher.dispatch(MGMCEventType.PLAYER_RESPAWN, MGMCEventContext.builder(player.level())
                .player(player)
                .entity(player)
                .build());
        });

        EntityEvent.LIVING_HURT.register((entity, source, amount) -> {
            if (entity.level().isClientSide()) return EventResult.pass();
            
            EventDispatcher.dispatch(MGMCEventType.ENTITY_HURT, MGMCEventContext.builder(entity.level())
                .entity(entity)
                .damageSource(source)
                .amount(amount)
                .build());
                
            if (entity instanceof Player player) {
                EventDispatcher.dispatch(MGMCEventType.PLAYER_HURT, MGMCEventContext.builder(player.level())
                    .player(player)
                    .entity(player)
                    .damageSource(source)
                    .amount(amount)
                    .build());
            }
            return EventResult.pass();
        });
    }
}
