package BetterPrisons.modid.ui.custom.widgets;

import BetterPrisons.modid.ui.custom.containers.ColorPickerPopup;
import BetterPrisons.modid.ui.custom.core.Component;
import BetterPrisons.modid.ui.custom.core.Theme;
import BetterPrisons.modid.ui.custom.rendering.ColorUtils;
import BetterPrisons.modid.ui.custom.rendering.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.function.Consumer;

/**
 * Color picker widget that displays a color preview and opens a popup when clicked.
 */
public class ColorPickerWidget extends Component {
    private String label;
    private int color;
    private String tooltip;
    private static final int PREVIEW_SIZE = 20;
    private static final int LABEL_SPACING = 8;

    private ColorPickerPopup popup;
    private Consumer<ColorPickerPopup> onPopupOpen;

    public ColorPickerWidget(String label, int initialColor) {
        this.label = label;
        this.color = initialColor;
        this.height = PREVIEW_SIZE;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public String getTooltip() {
        return tooltip;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    /**
     * Sets a callback to be invoked when the popup is opened.
     * This is used by the parent screen to add the popup to its rendering.
     */
    public void setOnPopupOpen(Consumer<ColorPickerPopup> callback) {
        this.onPopupOpen = callback;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        MinecraftClient client = MinecraftClient.getInstance();

        // Draw label
        context.drawText(client.textRenderer, label, x, y + (PREVIEW_SIZE - 8) / 2, Theme.textPrimary, false);

        // Calculate preview position
        int labelWidth = client.textRenderer.getWidth(label);
        int previewX = x + labelWidth + LABEL_SPACING;
        int previewY = y;

        // Draw color preview
        RenderUtils.drawRect(context, previewX, previewY, PREVIEW_SIZE, PREVIEW_SIZE, color);

        // Draw preview border
        int borderColor = hovered ? Theme.borderHover : Theme.borderPrimary;
        RenderUtils.drawRectOutline(context, previewX, previewY, PREVIEW_SIZE, PREVIEW_SIZE, borderColor, 1);

        // Draw hex value
        String hexText = "#" + ColorUtils.toHex(color, false);
        int hexX = previewX + PREVIEW_SIZE + LABEL_SPACING;
        int hexY = y + (PREVIEW_SIZE - 8) / 2;
        context.drawText(client.textRenderer, hexText, hexX, hexY, Theme.textSecondary, false);

        // Update total width for layout
        int hexWidth = client.textRenderer.getWidth(hexText);
        this.width = labelWidth + LABEL_SPACING + PREVIEW_SIZE + LABEL_SPACING + hexWidth;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return false;

        if (button == 0 && isMouseOver(mouseX, mouseY)) {
            openColorPicker();
            return true;
        }
        return false;
    }

    private void openColorPicker() {
        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        popup = new ColorPickerPopup(
            color,
            newColor -> {
                this.color = newColor;
                popup = null;
            },
            () -> {
                popup = null;
            }
        );

        popup.centerOn(screenWidth, screenHeight);
        popup.setVisible(true);

        // Notify parent screen to add the popup
        if (onPopupOpen != null) {
            onPopupOpen.accept(popup);
        }
    }

    public ColorPickerPopup getActivePopup() {
        return popup;
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
}
