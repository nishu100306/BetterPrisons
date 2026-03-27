package BetterPrisons.modid.ui.custom.widgets;

import BetterPrisons.modid.ui.custom.containers.ColorPickerPopup;
import BetterPrisons.modid.ui.custom.core.Component;
import BetterPrisons.modid.ui.custom.core.Theme;
import BetterPrisons.modid.ui.custom.core.TooltipProvider;
import BetterPrisons.modid.ui.custom.rendering.ColorUtils;
import BetterPrisons.modid.ui.custom.rendering.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.function.Consumer;

/**
 * Color picker widget that displays a color preview and opens a popup when clicked.
 */
public class ColorPickerWidget extends Component implements TooltipProvider {
    private String label;
    private int color;
    private int defaultColor;
    private String tooltip;
    private static final int PREVIEW_SIZE = 20;
    private static final int LABEL_SPACING = 8;
    private static final int RESET_BUTTON_SIZE = 14;
    private static final int RESET_BUTTON_SPACING = 6;

    private ColorPickerPopup popup;
    private Consumer<ColorPickerPopup> onPopupOpen;
    private Consumer<Integer> onChange;

    public ColorPickerWidget(String label, int initialColor) {
        this.label = label;
        this.color = 0xFF000000 | (initialColor & 0xFFFFFF);
        this.defaultColor = this.color;
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
        this.color = 0xFF000000 | (color & 0xFFFFFF);
        if (onChange != null) {
            onChange.accept(this.color);
        }
    }

    public void setOnChange(Consumer<Integer> onChange) {
        this.onChange = onChange;
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

        // Update hover state
        updateHoverState(mouseX, mouseY);

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

        // Draw reset button
        int hexWidth = client.textRenderer.getWidth(hexText);
        int resetX = hexX + hexWidth + RESET_BUTTON_SPACING;
        int resetY = y + (PREVIEW_SIZE - RESET_BUTTON_SIZE) / 2;
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
        this.width = labelWidth + LABEL_SPACING + PREVIEW_SIZE + LABEL_SPACING + hexWidth + RESET_BUTTON_SPACING + RESET_BUTTON_SIZE;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return false;

        if (button == 0) {
            MinecraftClient client = MinecraftClient.getInstance();
            int labelWidth = client.textRenderer.getWidth(label);
            int previewX = x + labelWidth + LABEL_SPACING;
            String hexText = "#" + ColorUtils.toHex(color, false);
            int hexWidth = client.textRenderer.getWidth(hexText);
            int hexX = previewX + PREVIEW_SIZE + LABEL_SPACING;
            int resetX = hexX + hexWidth + RESET_BUTTON_SPACING;
            int resetY = y + (PREVIEW_SIZE - RESET_BUTTON_SIZE) / 2;

            // Check if clicking reset button
            if (mouseX >= resetX && mouseX < resetX + RESET_BUTTON_SIZE &&
                mouseY >= resetY && mouseY < resetY + RESET_BUTTON_SIZE) {
                resetToDefault();
                return true;
            }

            // Check if clicking color picker
            if (isMouseOver(mouseX, mouseY)) {
                openColorPicker();
                return true;
            }
        }
        return false;
    }

    public void resetToDefault() {
        setColor(defaultColor);
    }

    private void openColorPicker() {
        MinecraftClient client = MinecraftClient.getInstance();
        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        popup = new ColorPickerPopup(
            color,
            newColor -> {
                setColor(newColor);
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
