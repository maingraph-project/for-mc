package ltd.opens.mg.mc.core.blueprint.inventory;

import ltd.opens.mg.mc.core.registry.MGMCRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.Container;

public class BlueprintWorkbenchMenu extends AbstractContainerMenu {
    private final Player player;

    public BlueprintWorkbenchMenu(int containerId, Inventory playerInventory) {
        this(containerId, playerInventory, (Container) null);
    }

    public BlueprintWorkbenchMenu(int containerId, Inventory playerInventory, FriendlyByteBuf extraData) {
        this(containerId, playerInventory, (Container) null);
    }

    public BlueprintWorkbenchMenu(int containerId, Inventory playerInventory, Container container) {
        super(MGMCRegistries.BLUEPRINT_WORKBENCH_MENU.get(), containerId);
        this.player = playerInventory.player;
        
        // 不再添加任何 Slot，直接读取手持物品
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }
    
    public ItemStack getTargetItem() {
        return this.player.getMainHandItem();
    }
}
