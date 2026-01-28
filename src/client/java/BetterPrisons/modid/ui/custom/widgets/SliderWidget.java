package BetterPrisons.modid.ui.custom.widgets;

import BetterPrisons.modid.ui.custom.core.Component;
import BetterPrisons.modid.ui.custom.core.Theme;
import BetterPrisons.modid.ui.custom.rendering.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * Float slider widget for selecting a decimal value within a range.
 * Similar to IntSliderWidget but for floating-point values.
 */
public class SliderWidget extends Component {
    private String label;
    private float value;
    private float minValue;
    private float maxValue;
    private String suffix;
    private String tooltip;
    private boolean dragging = false;
    private int decimalPlaces = 2;

    private static final int SLIDER_WIDTH = 120;
    private static final int SLIDER_HEIGHT = 6;
    private static final int HANDLE_SIZE = 12;
    private static final int LABEL_SPACING = 8;

    public SliderWidget(String label, float initialValue, float minValue, float maxValue) {
        this(label, initialValue, minValue, maxValue, "");
    }

    public SliderWidget(String label, float initialValue, float minValue, float maxValue, String suffix) {
        this.label = label;
        this.value = Math.max(minValue, Math.min(maxValue, initialValue));
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

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = Math.max(minValue, Math.min(maxValue, value));
    }

    public void setDecimalPlaces(int decimalPlaces) {
        this.decimalPlaces = decimalPlaces;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        MinecraftClient client = MinecraftClient.getInstance();

        // Draw label
        context.drawText(client.textRenderer, label, x, y + (height - 8) / 2, Theme.textPrimary, false);

        // Calculate slider position
        int labelWidth = client.textRenderer.getWidth(label);
        int sliderX = x + labelWidth + LABEL_SPACING;
        int sliderY = y + (height - SLIDER_HEIGHT) / 2;

        // Calculate value position (0.0 to 1.0)
        float normalizedValue = (value - minValue) / (maxValue - minValue);
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

        // Draw value text
        String valueText = String.format("%." + decimalPlaces + "f", value) + suffix;
        int valueWidth = client.textRenderer.getWidth(valueText);
        int valueX = sliderX + SLIDER_WIDTH + LABEL_SPACING;
        int valueY = y + (height - 8) / 2;
        context.drawText(client.textRenderer, valueText, valueX, valueY, Theme.textSecondary, false);

        // Update total width for layout
        this.width = labelWidth + LABEL_SPACING + SLIDER_WIDTH + LABEL_SPACING + valueWidth;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return false;

        if (button == 0 && isMouseOverSlider(mouseX, mouseY)) {
            dragging = true;
            updateValueFromMouse(mouseX);
            return true;
        }
        return false;
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

    private void updateValueFromMouse(double mouseX) {
        MinecraftClient client = MinecraftClient.getInstance();
        int labelWidth = client.textRenderer.getWidth(label);
        int sliderX = x + labelWidth + LABEL_SPACING;

        // Calculate new value based on mouse position
        float normalizedValue = (float) (mouseX - sliderX) / SLIDER_WIDTH;
        normalizedValue = Math.max(0, Math.min(1, normalizedValue));

        float newValue = minValue + normalizedValue * (maxValue - minValue);
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

    public float getMinValue() {
        return minValue;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }
}
