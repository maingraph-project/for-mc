package ltd.opens.mg.mc.client.gui.screens;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.function.Consumer;

public class InputModalScreen extends Screen {
    public enum Mode {
        INPUT,
        SELECTION
    }

    private final Screen parent;
    private final String titleStr;
    private final String initialValue;
    private final Consumer<String> onConfirm;
    private EditBox editBox;
    private final boolean isNumeric;
    private final String[] options;
    private final Mode mode;

    public InputModalScreen(Screen parent, String title, String initialValue, boolean isNumeric, Consumer<String> onConfirm) {
        this(parent, title, initialValue, isNumeric, null, Mode.INPUT, onConfirm);
    }

    public InputModalScreen(Screen parent, String title, String initialValue, boolean isNumeric, String[] options, Mode mode, Consumer<String> onConfirm) {
        super(Component.literal(title));
        this.parent = parent;
        this.titleStr = title;
        this.initialValue = initialValue;
        this.isNumeric = isNumeric;
        this.options = options;
        this.mode = mode;
        this.onConfirm = onConfirm;
    }

    @Override
    protected void init() {
        int width = 200;
        int height = (mode == Mode.SELECTION && options != null) ? Math.min(200, 40 + options.length * 22 + 30) : 80;
        int startX = (this.width - width) / 2;
        int startY = (this.height - height) / 2;

        if (mode == Mode.INPUT) {
            // Text Input Mode
            this.editBox = new EditBox(this.font, startX + 10, startY + 30, width - 20, 20, Component.translatable("gui.mgmc.modal.input_label"));
            this.editBox.setMaxLength(32767);
            this.editBox.setValue(initialValue);
            if (isNumeric) {
                this.editBox.setFilter(s -> s.isEmpty() || s.matches("^-?\\d*\\.?\\d*$"));
            }
            this.addRenderableWidget(this.editBox);
            this.setInitialFocus(this.editBox);

            this.addRenderableWidget(Button.builder(Component.translatable("gui.mgmc.modal.confirm"), (btn) -> {
                onConfirm.accept(editBox.getValue());
                if (this.minecraft.screen == this) {
                    this.minecraft.setScreen(parent);
                }
            }).bounds(startX + 10, startY + 55, 85, 20).build());

            this.addRenderableWidget(Button.builder(Component.translatable("gui.mgmc.modal.cancel"), (btn) -> {
                this.minecraft.setScreen(parent);
            }).bounds(startX + 105, startY + 55, 85, 20).build());
        } else {
            // Selection Mode
            if (options != null) {
                int btnY = startY + 30;
                for (int i = 0; i < options.length; i++) {
                    final String opt = options[i];
                    Button b = Button.builder(Component.literal(opt), (btn) -> {
                        onConfirm.accept(opt);
                        if (this.minecraft.screen == this) {
                            this.minecraft.setScreen(parent);
                        }
                    }).bounds(startX + 10, btnY + (i * 22), width - 20, 20).build();
                    
                    this.addRenderableWidget(b);
                }
            }

            this.addRenderableWidget(Button.builder(Component.translatable("gui.mgmc.modal.cancel"), (btn) -> {
                this.minecraft.setScreen(parent);
            }).bounds(startX + 10, startY + height - 25, width - 20, 20).build());
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0x88000000);
        
        int width = 200;
        int height = (mode == Mode.SELECTION && options != null) ? Math.min(200, 40 + options.length * 22 + 30) : 80;
        int startX = (this.width - width) / 2;
        int startY = (this.height - height) / 2;
        
        guiGraphics.fill(startX, startY, startX + width, startY + height, 0xEE1A1A1A);
        guiGraphics.renderOutline(startX, startY, width, height, 0xFFFFFFFF);
        
        guiGraphics.drawString(font, titleStr, startX + 10, startY + 10, 0xFFFFFFFF, false);
        
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            onConfirm.accept(editBox.getValue());
            this.minecraft.setScreen(parent);
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.minecraft.setScreen(parent);
            return true;
        }
        return super.keyPressed(event);
    }
}
