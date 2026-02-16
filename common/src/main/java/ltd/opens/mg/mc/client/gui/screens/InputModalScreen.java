package ltd.opens.mg.mc.client.gui.screens;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.ObjectSelectionList;
import net.minecraft.client.gui.screens.Screen;
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
    private SelectionList selectionList;
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
        int height = (mode == Mode.SELECTION && options != null) ? Math.min(240, 40 + options.length * 22 + 30) : 80;
        int startX = (this.width - width) / 2;
        int startY = (this.height - height) / 2;

        if (mode == Mode.INPUT) {
            // Text Input Mode
            this.editBox = new EditBox(this.font, startX + 10, startY + 30, width - 20, 20, Component.translatable("gui.mgmc.modal.input_label"));
            this.editBox.setMaxLength(32767);
            this.editBox.setValue(initialValue);
            this.editBox.setTextColor(0xFFFFFFFF);
            this.editBox.setCanLoseFocus(false);
            this.editBox.setFocused(true);
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
                int listHeight = height - 60;
                this.selectionList = new SelectionList(this.minecraft, width - 20, listHeight, startY + 30, 20);
                this.selectionList.setX(startX + 10);
                
                for (String opt : options) {
                    this.selectionList.add(new StringEntry(opt));
                }
                
                // Select initial value if matches
                if (initialValue != null) {
                    for (StringEntry entry : this.selectionList.children()) {
                        if (entry.value.equals(initialValue)) {
                            this.selectionList.setSelected(entry);
                            this.selectionList.centerOn(entry);
                            break;
                        }
                    }
                }
                
                this.addRenderableWidget(this.selectionList);
            }

            // Cancel Button (Centered)
            this.addRenderableWidget(Button.builder(Component.translatable("gui.mgmc.modal.cancel"), (btn) -> {
                this.minecraft.setScreen(parent);
            }).bounds(startX + (width - 85) / 2, startY + height - 25, 85, 20).build());
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.fill(0, 0, this.width, this.height, 0x88000000);
        
        int width = 200;
        int height = (mode == Mode.SELECTION && options != null) ? Math.min(240, 40 + options.length * 22 + 30) : 80;
        int startX = (this.width - width) / 2;
        int startY = (this.height - height) / 2;
        
        guiGraphics.fill(startX, startY, startX + width, startY + height, 0xEE1A1A1A);
        guiGraphics.renderOutline(startX, startY, width, height, 0xFFFFFFFF);
        
        guiGraphics.drawString(font, titleStr, startX + 10, startY + 10, 0xFFFFFFFF, false);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (mode == Mode.INPUT && editBox != null) {
            // Force focus to ensure input is captured
            if (!editBox.isFocused()) {
                editBox.setFocused(true);
            }
            if (editBox.charTyped(codePoint, modifiers)) {
                return true;
            }
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (mode == Mode.INPUT && editBox != null) {
                onConfirm.accept(editBox.getValue());
                this.minecraft.setScreen(parent);
                return true;
            } else if (mode == Mode.SELECTION && selectionList != null) {
                if (selectionList.getSelected() != null) {
                    onConfirm.accept(selectionList.getSelected().value);
                    this.minecraft.setScreen(parent);
                    return true;
                }
            }
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.minecraft.setScreen(parent);
            return true;
        }
        if (mode == Mode.INPUT && editBox != null) {
            // Force focus to ensure input is captured
            if (!editBox.isFocused()) {
                editBox.setFocused(true);
            }
            if (editBox.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    class SelectionList extends ObjectSelectionList<StringEntry> {
        public SelectionList(Minecraft minecraft, int width, int height, int y, int itemHeight) {
            super(minecraft, width, height, y, itemHeight);
        }
        
        public void add(StringEntry entry) {
            super.addEntry(entry);
        }
        
        public void centerOn(StringEntry entry) {
            super.centerScrollOn(entry);
        }

        @Override
        public int getRowWidth() {
            return this.width - 10;
        }

        @Override
        protected int getScrollbarPosition() {
            return this.getX() + this.width - 6;
        }
    }

    class StringEntry extends ObjectSelectionList.Entry<StringEntry> {
        final String value;
        private long lastClickTime;

        public StringEntry(String value) {
            this.value = value;
        }

        @Override
        public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean isMouseOver, float partialTick) {
            if (isMouseOver) {
                guiGraphics.fill(left, top, left + width, top + height, 0x44FFFFFF);
            }
            // Draw string centered vertically
            guiGraphics.drawString(font, value, left + 5, top + (height - 8) / 2, 0xFFFFFFFF, false);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                long now = System.currentTimeMillis();
                if (now - this.lastClickTime < 250L) {
                    // Double click -> Confirm
                    onConfirm.accept(this.value);
                    if (minecraft.screen == InputModalScreen.this) {
                        minecraft.setScreen(parent);
                    }
                    return true;
                }
                this.lastClickTime = now;
                selectionList.setSelected(this);
                return true;
            }
            return false;
        }

        @Override
        public Component getNarration() {
            return Component.literal(value);
        }
    }
}
