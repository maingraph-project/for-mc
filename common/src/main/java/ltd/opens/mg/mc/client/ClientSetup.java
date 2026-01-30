package ltd.opens.mg.mc.client;

import ltd.opens.mg.mc.client.gui.screens.BlueprintWorkbenchScreen;
import ltd.opens.mg.mc.core.registry.MGMCRegistries;
import dev.architectury.registry.menu.MenuRegistry;

public class ClientSetup {
    public static void registerScreens() {
        MenuRegistry.registerScreenFactory(MGMCRegistries.BLUEPRINT_WORKBENCH_MENU.get(), BlueprintWorkbenchScreen::new);
    }
}
