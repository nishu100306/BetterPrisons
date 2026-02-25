package BetterPrisons.modid.ui.custom.widgets;

import BetterPrisons.modid.ui.custom.core.Component;
import BetterPrisons.modid.ui.custom.core.Theme;
import BetterPrisons.modid.ui.custom.core.TooltipProvider;
import BetterPrisons.modid.ui.custom.rendering.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

/**
 * Keybind widget for capturing and displaying keyboard shortcuts.
 */
public class KeybindWidget extends Component implements TooltipProvider {
    private String label;
    private int keyCode;
    private String tooltip;
    private boolean listening = false;

    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_HEIGHT = 20;
    private static final int LABEL_SPACING = 8;

    public KeybindWidget(String label, int initialKeyCode) {
        this.label = label;
        this.keyCode = initialKeyCode;
        this.height = BUTTON_HEIGHT;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public String getTooltip() {
        return tooltip;
    }

    public int getKeyCode() {
        return keyCode;
    }

    public void setKeyCode(int keyCode) {
        this.keyCode = keyCode;
    }

    public String getKeyName() {
        if (keyCode == GLFW.GLFW_KEY_UNKNOWN) {
            return "None";
        }
        return InputUtil.fromKeyCode(keyCode, 0).getLocalizedText().getString();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        MinecraftClient client = MinecraftClient.getInstance();

        // Update hover state
        updateHoverState(mouseX, mouseY);

        // Draw label
        context.drawText(client.textRenderer, label, x, y + (BUTTON_HEIGHT - 8) / 2, Theme.textPrimary, false);

        // Calculate button position
        int labelWidth = client.textRenderer.getWidth(label);
        int buttonX = x + labelWidth + LABEL_SPACING;
        int buttonY = y;

        // Draw button background
        int bgColor;
        if (listening) {
            bgColor = Theme.accentPrimary;
        } else if (hovered) {
            bgColor = Theme.widgetBackgroundHover;
        } else {
            bgColor = Theme.widgetBackground;
        }
        RenderUtils.drawRect(context, buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT, bgColor);

        // Draw button border
        int borderColor = listening ? Theme.borderFocus : (hovered ? Theme.borderHover : Theme.borderPrimary);
        RenderUtils.drawRectOutline(context, buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT, borderColor, 1);

        // Draw key name or "Press a key..."
        String displayText = listening ? "Press a key..." : getKeyName();
        int textWidth = client.textRenderer.getWidth(displayText);
        int textX = buttonX + (BUTTON_WIDTH - textWidth) / 2;
        int textY = buttonY + 6;
        context.drawText(client.textRenderer, displayText, textX, textY, Theme.textPrimary, false);

        // Update total width for layout
        this.width = labelWidth + LABEL_SPACING + BUTTON_WIDTH;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return false;

        if (button == 0 && isMouseOverButton(mouseX, mouseY)) {
            listening = !listening;
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!listening) return false;

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            // Cancel listening
            listening = false;
            return true;
        }

        if (keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE) {
            // Clear keybind
            this.keyCode = GLFW.GLFW_KEY_UNKNOWN;
            listening = false;
            return true;
        }

        // Set new keybind
        this.keyCode = keyCode;
        listening = false;
        return true;
    }

    private boolean isMouseOverButton(double mouseX, double mouseY) {
        MinecraftClient client = MinecraftClient.getInstance();
        int labelWidth = client.textRenderer.getWidth(label);
        int buttonX = x + labelWidth + LABEL_SPACING;
        int buttonY = y;

        return mouseX >= buttonX && mouseX < buttonX + BUTTON_WIDTH &&
               mouseY >= buttonY && mouseY < buttonY + BUTTON_HEIGHT;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public boolean isListening() {
        return listening;
    }

    public void setListening(boolean listening) {
        this.listening = listening;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
