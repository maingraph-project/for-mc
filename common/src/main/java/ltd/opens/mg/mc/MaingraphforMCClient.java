package ltd.opens.mg.mc;

import com.mojang.blaze3d.platform.InputConstants;
import dev.architectury.event.EventResult;
import dev.architectury.event.events.client.ClientRawInputEvent;
import dev.architectury.event.events.client.ClientTooltipEvent;
import dev.architectury.registry.client.keymappings.KeyMappingRegistry;
import ltd.opens.mg.mc.client.ClientSetup;
import ltd.opens.mg.mc.client.gui.screens.AboutScreen;
import ltd.opens.mg.mc.client.gui.screens.BlueprintSelectionScreen;
import ltd.opens.mg.mc.core.registry.MGMCRegistries;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class MaingraphforMCClient {
    public static final String CATEGORY = "key.categories." + MaingraphforMC.MODID;

    public static final KeyMapping BLUEPRINT_KEY = new KeyMapping(
        "key.mgmc.open_blueprint",
        InputConstants.Type.KEYSYM,
        GLFW.GLFW_KEY_M,
        CATEGORY
    );

    public static void init() {
        // Register KeyMappings
        KeyMappingRegistry.register(BLUEPRINT_KEY);

        // Register Screens - Move to SETUP to avoid early registry access
        dev.architectury.event.events.common.LifecycleEvent.SETUP.register(MaingraphforMCClient::onSetup);

        // Register Events
        ClientRawInputEvent.KEY_PRESSED.register(MaingraphforMCClient::onKeyInput);
        ClientTooltipEvent.ITEM.register(MaingraphforMCClient::onItemTooltip);
    }

    private static void onSetup() {
        ClientSetup.registerScreens();
    }

    private static EventResult onKeyInput(Minecraft client, int keyCode, int scanCode, int action, int modifiers) {
        if (action == GLFW.GLFW_PRESS && keyCode == GLFW.GLFW_KEY_M) {
            if ((modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
                if (client.screen != null && client.screen.getFocused() instanceof net.minecraft.client.gui.components.EditBox) {
                    return EventResult.pass();
                }
                handleBlueprintKey();
                return EventResult.interruptTrue();
            }
        }
        return EventResult.pass();
    }

    private static void handleBlueprintKey() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            mc.setScreen(new BlueprintSelectionScreen());
        } else if (mc.player != null && mc.player.isCreative()) {
            mc.setScreen(new BlueprintSelectionScreen());
        } else if (mc.level != null) {
            mc.setScreen(new AboutScreen(null));
        }
    }

    private static void onItemTooltip(net.minecraft.world.item.ItemStack stack, List<Component> lines, net.minecraft.world.item.Item.TooltipContext context, net.minecraft.world.item.TooltipFlag flag) {
        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.isCreative()) {
             if (stack.has(MGMCRegistries.BLUEPRINT_SCRIPTS.get())) {
                 lines.add(Component.translatable("tooltip.mgmc.item_bound").withStyle(ChatFormatting.GOLD));
             }
        }
    }
}
