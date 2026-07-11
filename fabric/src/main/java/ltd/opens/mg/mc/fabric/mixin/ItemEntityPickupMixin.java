package ltd.opens.mg.mc.fabric.mixin;

import ltd.opens.mg.mc.core.blueprint.EventDispatcher;
import ltd.opens.mg.mc.core.blueprint.events.MGMCEventType;
import ltd.opens.mg.mc.core.blueprint.events.MGMCEventContext;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fabric 端物品拾取检测。
 * fabric-api 0.102.0 已移除 {@code PlayerPickupItemCallback}，因此通过 mixin 钩取
 * {@code ItemEntity#playerTouch(Player)} —— 这是玩家与地面上 ItemEntity 碰撞时的唯一入口，
 * 也是 NeoForge {@code ItemEntityPickupEvent} 的等价触发路径。
 *
 * 注意：拾取逻辑会把 ItemEntity 的栈置空（完全拾取时 discard），因此在方法返回时再读取
 * {@code getItem()} 会得到 air。必须在 HEAD 阶段（拾取前）就捕获物品 ID 与数量，
 * 返回时据此判断并分发，上下文与 NeoForge 端保持一致（player / entity / itemId）。
 */
@Mixin(ItemEntity.class)
public abstract class ItemEntityPickupMixin {

    @Inject(method = "playerTouch(Lnet/minecraft/world/entity/player/Player;)V", at = @At("HEAD"))
    private void mgmc$capture(Player player, CallbackInfo ci) {
        ItemStack stack = ((ItemEntity) (Object) this).getItem();
        this.mgmc$beforeCount = stack.getCount();
        this.mgmc$itemId = stack.isEmpty()
            ? "minecraft:air"
            : BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
    }

    @Inject(method = "playerTouch(Lnet/minecraft/world/entity/player/Player;)V", at = @At("RETURN"))
    private void mgmc$onPlayerTouch(Player player, CallbackInfo ci) {
        ItemEntity self = (ItemEntity) (Object) this;
        // 仅当确有物品进入背包时触发：完全拾取后实体被 discard，或部分拾取后剩余数量变少。
        // 非拾取的早期返回不会改变数量，也不会移除实体，因此不会误触发。
        if (!self.isRemoved() && self.getItem().getCount() >= this.mgmc$beforeCount) return;

        Level level = player.level();
        if (level.isClientSide()) return;

        EventDispatcher.dispatch(MGMCEventType.ITEM_PICKUP,
            MGMCEventContext.builder(level)
                .player(player)
                .entity(player)
                .itemId(this.mgmc$itemId)
                .build());
    }

    private transient int mgmc$beforeCount = 0;
    private transient String mgmc$itemId = "minecraft:air";
}
