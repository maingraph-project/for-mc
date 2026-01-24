package ltd.opens.mg.mc;

import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import ltd.opens.mg.mc.client.gui.screens.BlueprintSelectionScreen;
import ltd.opens.mg.mc.client.gui.screens.AboutScreen;

import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.settings.KeyModifier;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import ltd.opens.mg.mc.core.registry.MGMCRegistries;
import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.NeoForge;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = MaingraphforMC.MODID, dist = Dist.CLIENT)
public class MaingraphforMCClient {
    public static final KeyMapping.Category MGMC_CATEGORY = new KeyMapping.Category(Identifier.parse(MaingraphforMC.MODID + ":main"));

    public static final KeyMapping BLUEPRINT_KEY = new KeyMapping(
        "key.mgmc.open_blueprint",
        KeyConflictContext.UNIVERSAL,
        KeyModifier.CONTROL,
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_M,
        MGMC_CATEGORY
    );

    public MaingraphforMCClient(ModContainer container, IEventBus modEventBus) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modEventBus.addListener(this::onRegisterKeyMappings);
        modEventBus.addListener(this::onClientSetup);
        modEventBus.addListener(ltd.opens.mg.mc.client.ClientSetup::registerScreens);
        
        // Register for game events (ClientTickEvent, etc.)
        NeoForge.EVENT_BUS.register(this);
    }

    private void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.registerCategory(MGMC_CATEGORY);
        event.register(BLUEPRINT_KEY);
    }

    @SubscribeEvent
    public void onKeyInput(InputEvent.Key event) {
        if (event.getAction() == GLFW.GLFW_PRESS && event.getKey() == GLFW.GLFW_KEY_M) {
            if ((event.getModifiers() & GLFW.GLFW_MOD_CONTROL) != 0) {
                Minecraft mc = Minecraft.getInstance();
                // If we are in a screen, check if we're typing in an EditBox
                if (mc.screen != null && mc.screen.getFocused() instanceof net.minecraft.client.gui.components.EditBox) {
                    return;
                }
                handleBlueprintKey();
            }
        }
    }

    private void handleBlueprintKey() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            // Main Menu - Global Mode
            mc.setScreen(new BlueprintSelectionScreen());
        } else if (mc.player != null && mc.player.isCreative()) {
            // In Game - Server Mode (Local or Remote)
            mc.setScreen(new BlueprintSelectionScreen());
        } else if (mc.level != null) {
            mc.setScreen(new AboutScreen(null));
        }
    }

    @SubscribeEvent
    public void onClientTick(ClientTickEvent.Post event) {
        // We now handle this in onKeyInput for better screen support
    }

    @SubscribeEvent
    public void onItemTooltip(ItemTooltipEvent event) {
        if (event.getEntity() != null && event.getEntity().isCreative()) {
            if (event.getItemStack().has(MGMCRegistries.BLUEPRINT_SCRIPTS.get())) {
                event.getToolTip().add(Component.translatable("tooltip.mgmc.item_bound").withStyle(ChatFormatting.GOLD));
            }
        }
    }

    private void onClientSetup(FMLClientSetupEvent event) {
        // Client setup logic
    }
}
