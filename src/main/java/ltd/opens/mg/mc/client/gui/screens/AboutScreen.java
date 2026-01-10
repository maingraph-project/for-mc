package ltd.opens.mg.mc.client.gui.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;

public class AboutScreen extends Screen {
    private final Screen parent;
    private int titleClickCount = 0;

    public AboutScreen(Screen parent) {
        super(Component.translatable("gui.mgmc.about.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Website Button
        this.addRenderableWidget(Button.builder(Component.translatable("gui.mgmc.about.website"), b -> {
            String url = "https://mc.maingraph.opens.ltd";
            Minecraft.getInstance().setScreen(new ConfirmLinkScreen(confirmed -> {
                if (confirmed) {
                    try {
                        java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                Minecraft.getInstance().setScreen(this);
            }, url, true));
        }).bounds(centerX - 100, centerY + 30, 200, 20).build());

        // Back Button
        this.addRenderableWidget(Button.builder(Component.translatable("gui.mgmc.blueprint_selection.back"), b -> {
            Minecraft.getInstance().setScreen(this.parent);
        }).bounds(centerX - 100, centerY + 60, 200, 20).build());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Draw Title (Clickable for Easter Egg)
        boolean hovered = mouseX >= centerX - 100 && mouseX <= centerX + 100 && mouseY >= centerY - 65 && mouseY <= centerY - 45;
        int titleColor = hovered ? 0xFFFFFFFF : 0xFFFFAA00;
        guiGraphics.drawCenteredString(this.font, this.title, centerX, centerY - 60, titleColor);

        // Draw Info - Use split logic for long text
        int textWidth = 300;
        int yOffset = -30;
        
        // Description
        String info = Component.translatable("gui.mgmc.about.info").getString();
        java.util.List<net.minecraft.util.FormattedCharSequence> infoLines = this.font.split(Component.literal(info), textWidth);
        for (net.minecraft.util.FormattedCharSequence line : infoLines) {
            guiGraphics.drawCenteredString(this.font, line, centerX, centerY + yOffset, 0xFFAAAAAA);
            yOffset += 12;
        }
        yOffset += 10;

        // Link
        guiGraphics.drawCenteredString(this.font, Component.translatable("gui.mgmc.about.link"), centerX, centerY + yOffset, 0xFF55FFFF);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean isDouble) {
        double mouseX = event.x();
        double mouseY = event.y();
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Check if title is clicked (Adjusted for new position)
        if (mouseX >= centerX - 100 && mouseX <= centerX + 100 && mouseY >= centerY - 65 && mouseY <= centerY - 45) {
            titleClickCount++;
            if (titleClickCount >= 3) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.CHICKEN_AMBIENT, 1.0F));
            }
            return true;
        }

        return super.mouseClicked(event, isDouble);
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(this.parent);
    }
}
