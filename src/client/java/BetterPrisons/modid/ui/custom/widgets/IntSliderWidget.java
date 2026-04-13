package BetterPrisons.modid.ui.custom.widgets;

import BetterPrisons.modid.ui.custom.core.Component;
import BetterPrisons.modid.ui.custom.core.Theme;
import BetterPrisons.modid.ui.custom.core.TooltipProvider;
import BetterPrisons.modid.ui.custom.rendering.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.function.Consumer;

/**
 * Integer slider widget for selecting a value within a range.
 * Features draggable handle and value display.
 */
public class IntSliderWidget extends Component implements TooltipProvider {
    private String label;
    private int value;
    private int defaultValue;
    private int minValue;
    private int maxValue;
    private String suffix;
    private String tooltip;
    private boolean dragging = false;
    private boolean editing = false;
    private String editBuffer = "";
    private Consumer<Integer> onChange;

    private static final int SLIDER_WIDTH = 120;
    private static final int SLIDER_HEIGHT = 6;
    private static final int HANDLE_SIZE = 12;
    private static final int LABEL_SPACING = 8;
    private static final int RESET_BUTTON_SIZE = 14;
    private static final int RESET_BUTTON_SPACING = 6;

    public IntSliderWidget(String label, int initialValue, int minValue, int maxValue) {
        this(label, initialValue, minValue, maxValue, "");
    }

    public IntSliderWidget(String label, int initialValue, int minValue, int maxValue, String suffix) {
        this.label = label;
        this.value = Math.max(minValue, Math.min(maxValue, initialValue));
        this.defaultValue = this.value;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.suffix = suffix;
        this.height = 20;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public String getTooltip() {
        return tooltip;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        int newValue = Math.max(minValue, Math.min(maxValue, value));
        if (this.value != newValue) {
            this.value = newValue;
            if (onChange != null) {
                onChange.accept(newValue);
            }
        }
    }

    public void setOnChange(Consumer<Integer> onChange) {
        this.onChange = onChange;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        MinecraftClient client = MinecraftClient.getInstance();

        // Update hover state
        updateHoverState(mouseX, mouseY);

        // Draw label
        context.drawText(client.textRenderer, label, x, y + (height - 8) / 2, Theme.textPrimary, false);

        // Calculate slider position
        int labelWidth = client.textRenderer.getWidth(label);
        int sliderX = x + labelWidth + LABEL_SPACING;
        int sliderY = y + (height - SLIDER_HEIGHT) / 2;

        // Calculate value position (0.0 to 1.0)
        float normalizedValue = (float) (value - minValue) / (maxValue - minValue);
        int handleX = sliderX + (int) (normalizedValue * SLIDER_WIDTH) - HANDLE_SIZE / 2;

        // Draw slider track
        RenderUtils.drawRect(context, sliderX, sliderY, SLIDER_WIDTH, SLIDER_HEIGHT, Theme.sliderTrack);

        // Draw slider fill (from start to handle)
        int fillWidth = (int) (normalizedValue * SLIDER_WIDTH);
        if (fillWidth > 0) {
            RenderUtils.drawRect(context, sliderX, sliderY, fillWidth, SLIDER_HEIGHT, Theme.sliderFill);
        }

        // Draw slider border
        int borderColor = hovered || dragging ? Theme.borderHover : Theme.borderPrimary;
        RenderUtils.drawRectOutline(context, sliderX, sliderY, SLIDER_WIDTH, SLIDER_HEIGHT, borderColor, 1);

        // Draw handle
        int handleY = sliderY - (HANDLE_SIZE - SLIDER_HEIGHT) / 2;
        int handleColor = dragging ? Theme.borderFocus : (hovered ? Theme.borderHover : Theme.sliderHandle);
        RenderUtils.drawRect(context, handleX, handleY, HANDLE_SIZE, HANDLE_SIZE, handleColor);
        RenderUtils.drawRectOutline(context, handleX, handleY, HANDLE_SIZE, HANDLE_SIZE, borderColor, 1);

        // Draw value text (or edit field)
        int valueX = sliderX + SLIDER_WIDTH + LABEL_SPACING;
        int valueY = y + (height - 8) / 2;
        String valueText;
        int valueWidth;

        if (editing) {
            // Draw edit field with cursor
            String displayText = editBuffer + "_";
            valueWidth = client.textRenderer.getWidth(displayText);
            int editBgX = valueX - 2;
            int editBgY = valueY - 2;
            int editBgW = Math.max(valueWidth + 4, 40);
            int editBgH = 12;
            RenderUtils.drawRect(context, editBgX, editBgY, editBgW, editBgH, Theme.widgetBackground);
            RenderUtils.drawRectOutline(context, editBgX, editBgY, editBgW, editBgH, Theme.borderFocus, 1);
            context.drawText(client.textRenderer, displayText, valueX, valueY, Theme.textPrimary, false);
            valueText = displayText;
        } else {
            valueText = value + suffix;
            valueWidth = client.textRenderer.getWidth(valueText);
            context.drawText(client.textRenderer, valueText, valueX, valueY, Theme.textSecondary, false);
        }

        // Draw reset button (offset by edit box width when editing)
        int valueAreaWidth = editing ? Math.max(valueWidth + 4, 40) : valueWidth;
        int resetX = valueX + valueAreaWidth + RESET_BUTTON_SPACING;
        int resetY = y + (height - RESET_BUTTON_SIZE) / 2;
        boolean resetHovered = mouseX >= resetX && mouseX < resetX + RESET_BUTTON_SIZE &&
                              mouseY >= resetY && mouseY < resetY + RESET_BUTTON_SIZE;

        int resetBgColor = resetHovered ? Theme.widgetBackgroundHover : Theme.widgetBackground;
        RenderUtils.drawRect(context, resetX, resetY, RESET_BUTTON_SIZE, RESET_BUTTON_SIZE, resetBgColor);
        RenderUtils.drawRectOutline(context, resetX, resetY, RESET_BUTTON_SIZE, RESET_BUTTON_SIZE,
            resetHovered ? Theme.borderHover : Theme.borderPrimary, 1);

        // Draw reset icon (circular arrow)
        String resetIcon = "↻";
        int iconWidth = client.textRenderer.getWidth(resetIcon);
        context.drawText(client.textRenderer, resetIcon,
            resetX + (RESET_BUTTON_SIZE - iconWidth) / 2,
            resetY + (RESET_BUTTON_SIZE - 8) / 2,
            Theme.textSecondary, false);

        // Update total width for layout
        this.width = labelWidth + LABEL_SPACING + SLIDER_WIDTH + LABEL_SPACING + valueAreaWidth + RESET_BUTTON_SPACING + RESET_BUTTON_SIZE;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return false;

        if (button == 0) {
            MinecraftClient client = MinecraftClient.getInstance();
            int labelWidth = client.textRenderer.getWidth(label);
            int sliderX = x + labelWidth + LABEL_SPACING;
            String valueText = editing ? (editBuffer + "_") : (value + suffix);
            int valueWidth = client.textRenderer.getWidth(valueText);
            int valueX = sliderX + SLIDER_WIDTH + LABEL_SPACING;
            int resetX = valueX + Math.max(valueWidth, 40) + RESET_BUTTON_SPACING;
            int resetY = y + (height - RESET_BUTTON_SIZE) / 2;

            // Check if clicking reset button
            if (mouseX >= resetX && mouseX < resetX + RESET_BUTTON_SIZE &&
                mouseY >= resetY && mouseY < resetY + RESET_BUTTON_SIZE) {
                commitEdit();
                resetToDefault();
                return true;
            }

            // Check if clicking value text area
            int valueAreaX = valueX - 2;
            int valueAreaY = y + (height - 8) / 2 - 2;
            int valueAreaW = Math.max(editing ? Math.max(valueWidth + 4, 40) : valueWidth + 4, 40);
            if (mouseX >= valueAreaX && mouseX < valueAreaX + valueAreaW &&
                mouseY >= valueAreaY && mouseY < valueAreaY + 12) {
                if (!editing) {
                    editing = true;
                    editBuffer = String.valueOf(value);
                }
                return true;
            }

            // Clicking elsewhere commits edit
            if (editing) {
                commitEdit();
            }

            // Check if clicking slider
            if (isMouseOverSlider(mouseX, mouseY)) {
                dragging = true;
                updateValueFromMouse(mouseX);
                return true;
            }
        }
        return false;
    }

    private void commitEdit() {
        if (!editing) return;
        editing = false;
        try {
            int parsed = Integer.parseInt(editBuffer.trim());
            setValue(parsed);
        } catch (NumberFormatException ignored) {
            // Invalid input — keep current value
        }
        editBuffer = "";
    }

    private void cancelEdit() {
        editing = false;
        editBuffer = "";
    }

    public void resetToDefault() {
        setValue(defaultValue);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && dragging) {
            dragging = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (dragging) {
            updateValueFromMouse(mouseX);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!editing) return false;
        if (keyCode == 257 || keyCode == 335) { // Enter or numpad enter
            commitEdit();
            return true;
        }
        if (keyCode == 256) { // Escape
            cancelEdit();
            return true;
        }
        if (keyCode == 259 && !editBuffer.isEmpty()) { // Backspace
            editBuffer = editBuffer.substring(0, editBuffer.length() - 1);
            return true;
        }
        return true; // Consume all keys while editing
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!editing) return false;
        if ((chr >= '0' && chr <= '9') || (chr == '-' && editBuffer.isEmpty())) {
            editBuffer += chr;
            return true;
        }
        return true; // Consume but ignore non-numeric
    }

    public boolean isEditing() {
        return editing;
    }

    private void updateValueFromMouse(double mouseX) {
        MinecraftClient client = MinecraftClient.getInstance();
        int labelWidth = client.textRenderer.getWidth(label);
        int sliderX = x + labelWidth + LABEL_SPACING;

        // Calculate new value based on mouse position
        float normalizedValue = (float) (mouseX - sliderX) / SLIDER_WIDTH;
        normalizedValue = Math.max(0, Math.min(1, normalizedValue));

        int newValue = minValue + Math.round(normalizedValue * (maxValue - minValue));
        setValue(newValue);
    }

    private boolean isMouseOverSlider(double mouseX, double mouseY) {
        MinecraftClient client = MinecraftClient.getInstance();
        int labelWidth = client.textRenderer.getWidth(label);
        int sliderX = x + labelWidth + LABEL_SPACING;
        int sliderY = y;

        return mouseX >= sliderX && mouseX < sliderX + SLIDER_WIDTH &&
               mouseY >= sliderY && mouseY < sliderY + height;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getMinValue() {
        return minValue;
    }

    public int getMaxValue() {
        return maxValue;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}
