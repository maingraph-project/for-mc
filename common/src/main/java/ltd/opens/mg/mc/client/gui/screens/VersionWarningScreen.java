package ltd.opens.mg.mc.client.gui.screens;

import ltd.opens.mg.mc.client.network.NetworkService;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class VersionWarningScreen extends Screen {
    private final Screen parent;
    private final String blueprintName;
    private final int version;

    public VersionWarningScreen(Screen parent, String blueprintName, int version) {
        super(Component.translatable("gui.mgmc.version_warning.title"));
        this.parent = parent;
        this.blueprintName = blueprintName;
        this.version = version;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // 1. Copy and Open (Highlighted/Primary)
        this.addRenderableWidget(Button.builder(Component.translatable("gui.mgmc.version_warning.copy_and_open"), b -> {
            copyAndOpen();
        }).bounds(centerX - 100, centerY + 20, 200, 20).build());

        // 2. Exit (Highlighted/Secondary)
        this.addRenderableWidget(Button.builder(Component.translatable("gui.mgmc.version_warning.exit"), b -> {
            this.minecraft.setScreen(this.parent);
        }).bounds(centerX - 100, centerY + 45, 200, 20).build());

        // 3. Open anyway (Small/De-emphasized)
        this.addRenderableWidget(Button.builder(Component.translatable("gui.mgmc.version_warning.open_anyway"), b -> {
            openAnyway();
        }).bounds(centerX - 50, centerY + 80, 100, 14).build());
    }

    private void copyAndOpen() {
        if (blueprintName != null) {
            // Request server to duplicate the file (works for both local and remote servers)
            String baseName = blueprintName.endsWith(".json") ? blueprintName.substring(0, blueprintName.length() - 5) : blueprintName;
            String newName = baseName + "_new.json";
            
            NetworkService.getInstance().duplicateBlueprint(blueprintName, newName);
            
            // Open the NEW blueprint with forceOpen=true to avoid infinite loop
            this.minecraft.setScreen(new BlueprintScreen(this.parent, newName, true));
        }
    }

    private void openAnyway() {
        this.minecraft.setScreen(new BlueprintScreen(this.parent, blueprintName, true));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        guiGraphics.drawCenteredString(this.font, this.title, centerX, centerY - 50, 0xFFFF5555);
        
        Component message = Component.translatable("gui.mgmc.version_warning.message", version, 5);
        int y = centerY - 30;
        for (net.minecraft.util.FormattedCharSequence line : this.font.split(message, 250)) {
            guiGraphics.drawCenteredString(this.font, line, centerX, y, 0xFFAAAAAA);
            y += 12;
        }
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }
}
