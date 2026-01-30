package ltd.opens.mg.mc.client.utils;

import ltd.opens.mg.mc.core.blueprint.routing.BlueprintRouter;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class IdMetadataHelper {

    public static class IdInfo {
        public final String id;
        public final Component name;
        public final ItemStack icon;
        public final boolean isBuiltIn;

        public IdInfo(String id, Component name, ItemStack icon, boolean isBuiltIn) {
            this.id = id;
            this.name = name;
            this.icon = icon;
            this.isBuiltIn = isBuiltIn;
        }
    }

    public static IdInfo getInfo(String id) {
        if (id.equals(BlueprintRouter.GLOBAL_ID)) {
            return new IdInfo(id, Component.translatable("gui.mgmc.mapping.id.global"), new ItemStack(Items.GLOBE_BANNER_PATTERN), true);
        }
        if (id.equals(BlueprintRouter.PLAYERS_ID)) {
            return new IdInfo(id, Component.translatable("gui.mgmc.mapping.id.players"), new ItemStack(Items.PLAYER_HEAD), true);
        }

        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl != null) {
            // 尝试作为物品
            net.minecraft.world.item.Item item = BuiltInRegistries.ITEM.get(rl);
            if (item != null && item != Items.AIR) {
                ItemStack stack = new ItemStack(item);
                return new IdInfo(id, stack.getHoverName(), stack, false);
            }
            // 尝试作为方块
            Block block = BuiltInRegistries.BLOCK.get(rl);
            if (block != null && block != net.minecraft.world.level.block.Blocks.AIR) {
                ItemStack stack = new ItemStack(block);
                return new IdInfo(id, stack.getHoverName(), stack, false);
            }
            // 尝试作为实体
            EntityType<?> entityType = BuiltInRegistries.ENTITY_TYPE.get(rl);
            if (entityType != null) {
                Component name = entityType.getDescription();
                return new IdInfo(id, name, new ItemStack(Items.ZOMBIE_HEAD), false);
            }
        }

        return new IdInfo(id, Component.literal(id), ItemStack.EMPTY, false);
    }

    /**
     * 获取所有可用的 ID 建议（用于搜索框建议列表的初始数据）
     */
    public static List<IdInfo> getAllPotentialIds() {
        List<IdInfo> suggestions = new ArrayList<>();
        
        // 1. 系统 ID
        suggestions.add(getInfo(BlueprintRouter.GLOBAL_ID));
        suggestions.add(getInfo(BlueprintRouter.PLAYERS_ID));

        // 2. 注册表中的所有项
        BuiltInRegistries.ITEM.keySet().forEach(rl -> suggestions.add(getInfo(rl.toString())));
        BuiltInRegistries.ENTITY_TYPE.keySet().forEach(rl -> suggestions.add(getInfo(rl.toString())));
        
        return suggestions;
    }
}
