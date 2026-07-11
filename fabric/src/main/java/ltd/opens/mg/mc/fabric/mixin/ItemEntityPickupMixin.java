package ltd.opens.mg.mc.fabric.mixin;

import ltd.opens.mg.mc.MaingraphforMC;
import ltd.opens.mg.mc.core.blueprint.EventDispatcher;
import ltd.opens.mg.mc.core.blueprint.events.MGMCEventType;
import ltd.opens.mg.mc.core.blueprint.events.MGMCEventContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric 端物品拾取检测。
 * 1.21.1 中玩家拾取物品由 {@code ItemEntity#playerTouch(Player)} 触发，
 * 物品成功放入背包后会调用 {@code LivingEntity#onItemPickup(ItemEntity)}。
 * 该回调等价于 NeoForge 的 {@code ItemEntityPickupEvent.Post}，
 * 因此在此处钩取即可稳定捕获“拾取成功”事件（玩家 / 物品 ID 均可用）。
 */
@Mixin(LivingEntity.class)
public abstract class ItemEntityPickupMixin {

    @Inject(method = "onItemPickup(Lnet/minecraft/world/entity/item/ItemEntity;)V", at = @At("HEAD"))
    private void mgmc$onItemPickup(ItemEntity itemEntity, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (!(self instanceof Player player)) return;

        Level level = player.level();
        if (level.isClientSide()) return;

        ItemStack stack = itemEntity.getItem();
        String itemId = stack.isEmpty()
            ? "minecraft:air"
            : BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();

        MaingraphforMC.LOGGER.debug("MGMC: item_pickup mixin fired, item={}", itemId);

        EventDispatcher.dispatch(MGMCEventType.ITEM_PICKUP,
            MGMCEventContext.builder(level)
                .player(player)
                .entity(player)
                .itemId(itemId)
                .build());
    }
}
