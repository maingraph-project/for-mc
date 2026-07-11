package ltd.opens.mg.mc.neoforge;

import ltd.opens.mg.mc.core.blueprint.EventDispatcher;
import ltd.opens.mg.mc.core.blueprint.events.MGMCEventType;
import ltd.opens.mg.mc.core.blueprint.events.MGMCEventContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.entity.player.ItemEntityPickupEvent;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;

public class NeoForgeEvents {
    // 在 Pre 阶段捕获被捡物品的 ID：Post 触发时物品可能已被并入背包（栈置空），
    // 此时再从 ItemEntity 读取会得到 air。Pre 与 Post 在同一线程上同步、紧邻触发，
    // 用 ThreadLocal 暂存即可保证两端语义一致。
    private static final ThreadLocal<String> PICKUP_ITEM_ID = new ThreadLocal<>();

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

        // 物品拾取事件（NeoForge使用ItemEntityPickupEvent）
        // Pre：物品尚完整，先捕获 ID
        NeoForge.EVENT_BUS.addListener(ItemEntityPickupEvent.Pre.class, event -> {
            ItemStack stack = event.getItemEntity().getItem();
            PICKUP_ITEM_ID.set(stack.isEmpty()
                ? "minecraft:air"
                : BuiltInRegistries.ITEM.getKey(stack.getItem()).toString());
        });

        // Post：物品可能已被并入背包，使用 Pre 阶段捕获的 ID
        NeoForge.EVENT_BUS.addListener(ItemEntityPickupEvent.Post.class, event -> {
            Level level = event.getPlayer().level();
            if (level.isClientSide()) return;

            Player player = event.getPlayer();
            String itemId = PICKUP_ITEM_ID.get();
            PICKUP_ITEM_ID.remove();
            if (itemId == null) itemId = "minecraft:air";

            EventDispatcher.dispatch(MGMCEventType.ITEM_PICKUP, MGMCEventContext.builder(level)
                .player(player)
                .entity(player)
                .itemId(itemId)
                .build());
        });
    }
}