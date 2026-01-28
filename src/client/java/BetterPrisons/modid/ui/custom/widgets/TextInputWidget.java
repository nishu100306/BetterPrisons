package BetterPrisons.modid.ui.custom.widgets;

import BetterPrisons.modid.ui.custom.core.Component;
import BetterPrisons.modid.ui.custom.core.Theme;
import BetterPrisons.modid.ui.custom.rendering.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

/**
 * Text input widget for entering text values.
 * Features cursor, text selection, and keyboard navigation.
 */
public class TextInputWidget extends Component {
    private String label;
    private String value;
    private String placeholder;
    private String tooltip;
    private boolean focused = false;
    private int cursorPosition = 0;
    private int maxLength = 32;

    private static final int INPUT_HEIGHT = 20;
    private static final int INPUT_WIDTH = 120;
    private static final int LABEL_SPACING = 8;

    public TextInputWidget(String label, String initialValue) {
        this(label, initialValue, "");
    }

    public TextInputWidget(String label, String initialValue, String placeholder) {
        this.label = label;
        this.value = initialValue != null ? initialValue : "";
        this.placeholder = placeholder;
        this.cursorPosition = this.value.length();
        this.height = INPUT_HEIGHT;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public String getTooltip() {
        return tooltip;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value != null ? value : "";
        this.cursorPosition = Math.min(cursorPosition, this.value.length());
    }

    public void setMaxLength(int maxLength) {
        this.maxLength = maxLength;
        if (value.length() > maxLength) {
            value = value.substring(0, maxLength);
            cursorPosition = Math.min(cursorPosition, value.length());
        }
    }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        MinecraftClient client = MinecraftClient.getInstance();

        // Draw label
        context.drawText(client.textRenderer, label, x, y + (INPUT_HEIGHT - 8) / 2, Theme.textPrimary, false);

        // Calculate input position
        int labelWidth = client.textRenderer.getWidth(label);
        int inputX = x + labelWidth + LABEL_SPACING;
        int inputY = y;

        // Draw input background
        int bgColor = focused ? Theme.widgetBackgroundHover : Theme.widgetBackground;
        RenderUtils.drawRect(context, inputX, inputY, INPUT_WIDTH, INPUT_HEIGHT, bgColor);

        // Draw input border
        int borderColor = focused ? Theme.borderFocus : (hovered ? Theme.borderHover : Theme.borderPrimary);
        RenderUtils.drawRectOutline(context, inputX, inputY, INPUT_WIDTH, INPUT_HEIGHT, borderColor, 1);

        // Draw text or placeholder
        String displayText = value.isEmpty() && !focused ? placeholder : value;
        int textColor = value.isEmpty() && !focused ? Theme.textDisabled : Theme.textPrimary;

        if (!displayText.isEmpty()) {
            // Clip text if too long
            String clippedText = displayText;
            int textWidth = client.textRenderer.getWidth(clippedText);
            int maxWidth = INPUT_WIDTH - 8;

            if (textWidth > maxWidth) {
                // Show end of text when focused
                if (focused) {
                    while (textWidth > maxWidth && clippedText.length() > 0) {
                        clippedText = clippedText.substring(1);
                        textWidth = client.textRenderer.getWidth(clippedText);
                    }
                } else {
                    // Show start of text with ellipsis
                    while (textWidth > maxWidth && clippedText.length() > 0) {
                        clippedText = clippedText.substring(0, clippedText.length() - 1);
                        textWidth = client.textRenderer.getWidth(clippedText + "...");
                    }
                    clippedText += "...";
                }
            }

            context.drawText(client.textRenderer, clippedText, inputX + 4, inputY + 6, textColor, false);
        }

        // Draw cursor
        if (focused && System.currentTimeMillis() % 1000 < 500) {
            int cursorX = inputX + 4;
            if (cursorPosition > 0) {
                String beforeCursor = value.substring(0, Math.min(cursorPosition, value.length()));
                cursorX += client.textRenderer.getWidth(beforeCursor);
            }
            RenderUtils.drawRect(context, cursorX, inputY + 4, 1, 12, Theme.textPrimary);
        }

        // Update total width for layout
        this.width = labelWidth + LABEL_SPACING + INPUT_WIDTH;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return false;

        MinecraftClient client = MinecraftClient.getInstance();
        int labelWidth = client.textRenderer.getWidth(label);
        int inputX = x + labelWidth + LABEL_SPACING;
        int inputY = y;

        if (button == 0) {
            if (mouseX >= inputX && mouseX < inputX + INPUT_WIDTH &&
                mouseY >= inputY && mouseY < inputY + INPUT_HEIGHT) {
                focused = true;
                // Calculate cursor position from mouse
                int relativeX = (int) (mouseX - inputX - 4);
                cursorPosition = 0;
                int totalWidth = 0;
                for (int i = 0; i < value.length(); i++) {
                    int charWidth = client.textRenderer.getWidth(String.valueOf(value.charAt(i)));
                    if (totalWidth + charWidth / 2 > relativeX) {
                        break;
                    }
                    totalWidth += charWidth;
                    cursorPosition++;
                }
                return true;
            } else {
                focused = false;
            }
        }
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!focused) return false;

        // Only accept printable characters
        if (chr >= 32 && chr < 127 && value.length() < maxLength) {
            value = value.substring(0, cursorPosition) + chr + value.substring(cursorPosition);
            cursorPosition++;
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!focused) return false;

        switch (keyCode) {
            case GLFW.GLFW_KEY_BACKSPACE:
                if (cursorPosition > 0) {
                    value = value.substring(0, cursorPosition - 1) + value.substring(cursorPosition);
                    cursorPosition--;
                }
                return true;

            case GLFW.GLFW_KEY_DELETE:
                if (cursorPosition < value.length()) {
                    value = value.substring(0, cursorPosition) + value.substring(cursorPosition + 1);
                }
                return true;

            case GLFW.GLFW_KEY_LEFT:
                if (cursorPosition > 0) {
                    cursorPosition--;
                }
                return true;

            case GLFW.GLFW_KEY_RIGHT:
                if (cursorPosition < value.length()) {
                    cursorPosition++;
                }
                return true;

            case GLFW.GLFW_KEY_HOME:
                cursorPosition = 0;
                return true;

            case GLFW.GLFW_KEY_END:
                cursorPosition = value.length();
                return true;

            case GLFW.GLFW_KEY_ENTER:
            case GLFW.GLFW_KEY_ESCAPE:
                focused = false;
                return true;
        }

        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public boolean isFocused() {
        return focused;
    }

    public void setFocused(boolean focused) {
        this.focused = focused;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
