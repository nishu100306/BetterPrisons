package BetterPrisons.modid.ui.custom.widgets;

import BetterPrisons.modid.ui.custom.core.Component;
import BetterPrisons.modid.ui.custom.core.Theme;
import BetterPrisons.modid.ui.custom.core.TooltipProvider;
import BetterPrisons.modid.ui.custom.rendering.AnimationHelper;
import BetterPrisons.modid.ui.custom.rendering.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.function.Consumer;

/**
 * Rectangular toggle switch widget.
 * Features smooth sliding animation when toggled.
 */
public class ToggleWidget extends Component implements TooltipProvider {
    private String label;
    private boolean value;
    private boolean defaultValue;
    private String tooltip;
    private AnimationHelper.Animation slideAnimation;
    private Consumer<Boolean> onChange;
    private static final int TOGGLE_WIDTH = 28;
    private static final int TOGGLE_HEIGHT = 14;
    private static final int HANDLE_SIZE = 10;
    private static final int HANDLE_PADDING = 2;
    private static final int LABEL_SPACING = 8;
    private static final int RESET_BUTTON_SIZE = 14;
    private static final int RESET_BUTTON_SPACING = 6;

    public ToggleWidget(String label, boolean initialValue) {
        this.label = label;
        this.value = initialValue;
        this.defaultValue = initialValue;
        this.slideAnimation = new AnimationHelper.Animation(initialValue ? 1.0f : 0.0f, 10.0f);
        this.height = TOGGLE_HEIGHT;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public String getTooltip() {
        return tooltip;
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        if (this.value != value) {
            this.value = value;
            this.slideAnimation.setTarget(value ? 1.0f : 0.0f);
            if (onChange != null) {
                onChange.accept(value);
            }
        }
    }

    public void setOnChange(Consumer<Boolean> onChange) {
        this.onChange = onChange;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        MinecraftClient client = MinecraftClient.getInstance();

        // Update hover state
        updateHoverState(mouseX, mouseY);

        // Update animation
        if (Theme.animationsEnabled) {
            slideAnimation.update(delta);
        } else {
            slideAnimation.setCurrent(value ? 1.0f : 0.0f);
        }

        // Draw label
        context.drawText(client.textRenderer, label, x, y + (height - 8) / 2, Theme.textPrimary, false);

        // Calculate toggle position
        int labelWidth = client.textRenderer.getWidth(label);
        int toggleX = x + labelWidth + LABEL_SPACING;
        int toggleY = y;

        // Draw toggle background (rectangular design - fills entire toggle area)
        int bgColor = value ? Theme.toggleOn : Theme.toggleOff;
        if (hovered) {
            bgColor = RenderUtils.blendColors(bgColor, Theme.borderHover, 0.3f);
        }
        RenderUtils.drawRect(context, toggleX, toggleY, TOGGLE_WIDTH, TOGGLE_HEIGHT, bgColor);

        // Draw toggle border
        int borderColor = hovered ? Theme.borderHover : Theme.borderPrimary;
        RenderUtils.drawRectOutline(context, toggleX, toggleY, TOGGLE_WIDTH, TOGGLE_HEIGHT, borderColor, 1);

        // Draw handle with animation (square design)
        float progress = slideAnimation.getCurrent();
        int maxTravel = TOGGLE_WIDTH - HANDLE_SIZE - (HANDLE_PADDING * 2);
        int handleX = toggleX + HANDLE_PADDING + (int) (maxTravel * progress);
        int handleY = toggleY + HANDLE_PADDING;
        RenderUtils.drawRect(context, handleX, handleY, HANDLE_SIZE, HANDLE_SIZE, Theme.toggleHandle);

        // Draw reset button
        int resetX = toggleX + TOGGLE_WIDTH + RESET_BUTTON_SPACING;
        int resetY = toggleY;
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
        this.width = labelWidth + LABEL_SPACING + TOGGLE_WIDTH + RESET_BUTTON_SPACING + RESET_BUTTON_SIZE;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return false;

        if (button == 0) {
            MinecraftClient client = MinecraftClient.getInstance();
            int labelWidth = client.textRenderer.getWidth(label);
            int toggleX = x + labelWidth + LABEL_SPACING;
            int resetX = toggleX + TOGGLE_WIDTH + RESET_BUTTON_SPACING;
            int resetY = y;

            // Check if clicking reset button
            if (mouseX >= resetX && mouseX < resetX + RESET_BUTTON_SIZE &&
                mouseY >= resetY && mouseY < resetY + RESET_BUTTON_SIZE) {
                resetToDefault();
                return true;
            }

            // Check if clicking toggle
            if (isMouseOver(mouseX, mouseY)) {
                value = !value;
                slideAnimation.setTarget(value ? 1.0f : 0.0f);
                if (onChange != null) {
                    onChange.accept(value);
                }
                return true;
            }
        }
        return false;
    }

    public void resetToDefault() {
        setValue(defaultValue);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        // Expand hitbox to include both label and toggle
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
