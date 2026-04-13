package BetterPrisons.modid.ui.custom.widgets;

import BetterPrisons.modid.ui.custom.core.Component;
import BetterPrisons.modid.ui.custom.core.Theme;
import BetterPrisons.modid.ui.custom.core.TooltipProvider;
import BetterPrisons.modid.ui.custom.rendering.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Util;

import java.net.URI;

/**
 * A button widget that opens a URL in the system browser when clicked.
 * Styled as a clickable link with hover effects.
 */
public class LinkButtonWidget extends Component implements TooltipProvider {
    private final String label;
    private final String url;
    private String tooltip;

    private static final int BUTTON_HEIGHT = 20;
    private static final int PADDING_X = 8;

    public LinkButtonWidget(String label, String url) {
        this.label = label;
        this.url = url;
        this.height = BUTTON_HEIGHT;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    @Override
    public String getTooltip() {
        return tooltip;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        MinecraftClient client = MinecraftClient.getInstance();
        updateHoverState(mouseX, mouseY);

        int textWidth = client.textRenderer.getWidth(label);
        int buttonWidth = textWidth + PADDING_X * 2;
        this.width = buttonWidth;

        // Background
        int bgColor = hovered ? Theme.widgetBackgroundHover : Theme.widgetBackground;
        RenderUtils.drawRect(context, x, y, buttonWidth, BUTTON_HEIGHT, bgColor);

        // Border
        int borderColor = hovered ? Theme.accentPrimary : Theme.borderPrimary;
        RenderUtils.drawRectOutline(context, x, y, buttonWidth, BUTTON_HEIGHT, borderColor, 1);

        // Text (accent color for link feel)
        int textColor = hovered ? Theme.accentPrimary : Theme.textPrimary;
        int textX = x + PADDING_X;
        int textY = y + (BUTTON_HEIGHT - 8) / 2;
        context.drawText(client.textRenderer, label, textX, textY, textColor, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible || button != 0) return false;

        if (isMouseOver(mouseX, mouseY)) {
            try {
                Util.getOperatingSystem().open(URI.create(url));
            } catch (Exception ignored) {
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        MinecraftClient client = MinecraftClient.getInstance();
        int buttonWidth = client.textRenderer.getWidth(label) + PADDING_X * 2;
        return mouseX >= x && mouseX < x + buttonWidth &&
               mouseY >= y && mouseY < y + BUTTON_HEIGHT;
    }
}
