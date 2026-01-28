package BetterPrisons.modid.ui.custom.widgets;

import BetterPrisons.modid.ui.custom.core.Component;
import BetterPrisons.modid.ui.custom.core.Theme;
import BetterPrisons.modid.ui.custom.rendering.AnimationHelper;
import BetterPrisons.modid.ui.custom.rendering.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * Modern iOS-style toggle switch widget.
 * Features smooth sliding animation when toggled.
 */
public class ToggleWidget extends Component {
    private String label;
    private boolean value;
    private String tooltip;
    private AnimationHelper.Animation slideAnimation;
    private static final int TOGGLE_WIDTH = 36;
    private static final int TOGGLE_HEIGHT = 20;
    private static final int HANDLE_SIZE = 16;
    private static final int LABEL_SPACING = 8;

    public ToggleWidget(String label, boolean initialValue) {
        this.label = label;
        this.value = initialValue;
        this.slideAnimation = new AnimationHelper.Animation(initialValue ? 1.0f : 0.0f, 10.0f);
        this.height = 20;
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
        this.value = value;
        this.slideAnimation.setTarget(value ? 1.0f : 0.0f);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        MinecraftClient client = MinecraftClient.getInstance();

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

        // Draw toggle background
        int bgColor = value ? Theme.toggleOn : Theme.toggleOff;
        if (hovered) {
            bgColor = RenderUtils.blendColors(bgColor, Theme.borderHover, 0.3f);
        }
        RenderUtils.drawRoundedRect(context, toggleX, toggleY, TOGGLE_WIDTH, TOGGLE_HEIGHT, 10, bgColor);

        // Draw toggle border
        int borderColor = hovered ? Theme.borderHover : Theme.borderPrimary;
        RenderUtils.drawRectOutline(context, toggleX, toggleY, TOGGLE_WIDTH, TOGGLE_HEIGHT, borderColor, 1);

        // Draw handle with animation
        float progress = slideAnimation.getCurrent();
        int handleX = toggleX + 2 + (int) ((TOGGLE_WIDTH - HANDLE_SIZE - 4) * progress);
        int handleY = toggleY + 2;
        RenderUtils.drawCircle(context, handleX + HANDLE_SIZE / 2, handleY + HANDLE_SIZE / 2, HANDLE_SIZE / 2, Theme.toggleHandle);

        // Update total width for layout
        this.width = labelWidth + LABEL_SPACING + TOGGLE_WIDTH;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return false;

        if (button == 0 && isMouseOver(mouseX, mouseY)) {
            value = !value;
            slideAnimation.setTarget(value ? 1.0f : 0.0f);
            return true;
        }
        return false;
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
