package ltd.opens.mg.mc.client.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import java.nio.file.Path;
import java.nio.file.Files;
public class VersionWarningScreen extends Screen {
    private final Screen parent;
    private final Path dataFile;
    private final String blueprintName;
    private final int version;

    public VersionWarningScreen(Screen parent, Path dataFile, int version) {
        super(Component.translatable("gui.mgmc.version_warning.title"));
        this.parent = parent;
        this.dataFile = dataFile;
        this.blueprintName = null;
        this.version = version;
    }

    public VersionWarningScreen(Screen parent, String blueprintName, int version) {
        super(Component.translatable("gui.mgmc.version_warning.title"));
        this.parent = parent;
        this.dataFile = null;
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
        if (dataFile != null) {
            try {
                String fileName = dataFile.getFileName().toString();
                String baseName = fileName.endsWith(".json") ? fileName.substring(0, fileName.length() - 5) : fileName;
                Path newFile = dataFile.getParent().resolve(baseName + "_v4.json");
                
                // If destination exists, try adding a number
                int i = 1;
                while (Files.exists(newFile)) {
                    newFile = dataFile.getParent().resolve(baseName + "_v4_" + i + ".json");
                    i++;
                }
                
                Files.copy(dataFile, newFile);
                this.minecraft.setScreen(new BlueprintScreen(this.parent, newFile));
            } catch (Exception e) {
                e.printStackTrace();
                openAnyway(); // Fallback
            }
        } else if (blueprintName != null) {
            // Multiplayer: Request server to duplicate the file
            String baseName = blueprintName.endsWith(".json") ? blueprintName.substring(0, blueprintName.length() - 5) : blueprintName;
            String newName = baseName + "_v4.json";
            
            if (this.minecraft.getConnection() != null) {
                // We don't have a way to check if file exists on server easily here, 
                // but the DuplicateBlueprintPayload will just overwrite or fail silently on server for now.
                // In a real scenario, we might want a response payload.
                this.minecraft.getConnection().send(new net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket(
                    new ltd.opens.mg.mc.network.payloads.DuplicateBlueprintPayload(blueprintName, newName)
                ));
                
                // Open the NEW blueprint
                this.minecraft.setScreen(new BlueprintScreen(this.parent, newName));
            } else {
                openAnyway();
            }
        }
    }

    private void openAnyway() {
        if (dataFile != null) {
            this.minecraft.setScreen(new BlueprintScreen(this.parent, dataFile, true));
        } else {
            this.minecraft.setScreen(new BlueprintScreen(this.parent, blueprintName, true));
        }
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
