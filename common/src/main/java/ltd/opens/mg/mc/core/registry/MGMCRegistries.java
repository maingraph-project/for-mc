package ltd.opens.mg.mc.core.registry;

import ltd.opens.mg.mc.MaingraphforMC;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import dev.architectury.registry.registries.DeferredRegister;
import dev.architectury.registry.registries.RegistrySupplier;
import dev.architectury.registry.menu.MenuRegistry;
import ltd.opens.mg.mc.core.blueprint.inventory.BlueprintWorkbenchMenu;
import net.minecraft.network.codec.ByteBufCodecs;
import com.mojang.serialization.Codec;

import java.util.List;
import java.util.function.Supplier;

public class MGMCRegistries {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES = 
        DeferredRegister.create(MaingraphforMC.MODID, Registries.DATA_COMPONENT_TYPE);

    public static final DeferredRegister<MenuType<?>> MENU_TYPES = 
        DeferredRegister.create(MaingraphforMC.MODID, Registries.MENU);

    // 存储蓝图路径列表的组件
    public static final RegistrySupplier<DataComponentType<List<String>>> BLUEPRINT_SCRIPTS = 
        DATA_COMPONENT_TYPES.register("scripts", () -> DataComponentType.<List<String>>builder()
            .persistent(Codec.STRING.listOf())
            .networkSynchronized(ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()))
            .build());

    // 蓝图工作台菜单类型
    public static final RegistrySupplier<MenuType<BlueprintWorkbenchMenu>> BLUEPRINT_WORKBENCH_MENU = 
        MENU_TYPES.register("blueprint_workbench", () -> MenuRegistry.ofExtended(BlueprintWorkbenchMenu::new));

    public static void register() {
        DATA_COMPONENT_TYPES.register();
        MENU_TYPES.register();
    }
}
