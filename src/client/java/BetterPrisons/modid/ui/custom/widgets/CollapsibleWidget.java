package BetterPrisons.modid.ui.custom.widgets;

import BetterPrisons.modid.ui.custom.core.Component;
import BetterPrisons.modid.ui.custom.core.Container;
import BetterPrisons.modid.ui.custom.core.Theme;
import BetterPrisons.modid.ui.custom.core.TooltipProvider;
import BetterPrisons.modid.ui.custom.rendering.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Collapsible dropdown widget that contains sub-widgets.
 * Clicking the header toggles expansion to show/hide child widgets.
 */
public class CollapsibleWidget extends Container implements TooltipProvider {
    private String label;
    private boolean expanded = false;
    private String tooltip;

    private static final int HEADER_HEIGHT = 20;
    private static final int CHILD_INDENT = 20;
    private static final int CHILD_SPACING = 6;
    private static final int ARROW_SIZE = 6;

    private List<Component> childWidgets = new ArrayList<>();

    public CollapsibleWidget(String label) {
        this.label = label;
        this.height = HEADER_HEIGHT;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public String getTooltip() {
        return tooltip;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
        updateHeight();
    }

    /**
     * Adds a child widget to this collapsible section.
     */
    public void addWidget(Component widget) {
        childWidgets.add(widget);
        addChild(widget);
        updateHeight();
    }

    /**
     * Returns the list of child widgets.
     */
    public List<Component> getChildWidgets() {
        return childWidgets;
    }

    /**
     * Updates the total height based on expansion state.
     * Also notifies parent CollapsibleWidget to update its height.
     */
    private void updateHeight() {
        if (expanded) {
            int totalHeight = HEADER_HEIGHT;
            for (Component child : childWidgets) {
                if (child.isVisible()) {
                    totalHeight += child.getHeight() + CHILD_SPACING;
                }
            }
            this.height = totalHeight;
        } else {
            this.height = HEADER_HEIGHT;
        }

        // Notify parent to update its height if it's also a CollapsibleWidget
        if (parent instanceof CollapsibleWidget) {
            ((CollapsibleWidget) parent).updateHeight();
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        MinecraftClient client = MinecraftClient.getInstance();

        // Update hover state properly for tooltip support
        updateHoverState(mouseX, mouseY);
        boolean headerHovered = this.hovered;

        // Draw header background
        int headerBgColor = headerHovered ? Theme.widgetBackgroundHover : Theme.widgetBackground;
        RenderUtils.drawRect(context, x, y, width, HEADER_HEIGHT, headerBgColor);

        // Draw header border
        int borderColor = headerHovered ? Theme.borderHover : Theme.borderPrimary;
        RenderUtils.drawRectOutline(context, x, y, width, HEADER_HEIGHT, borderColor, 1);

        // Draw expansion arrow
        int arrowX = x + 6;
        int arrowY = y + (HEADER_HEIGHT - ARROW_SIZE) / 2;
        drawArrow(context, arrowX, arrowY, expanded);

        // Draw label
        int labelX = x + 6 + ARROW_SIZE + 6;
        int labelY = y + (HEADER_HEIGHT - 8) / 2;
        context.drawText(client.textRenderer, label, labelX, labelY, Theme.textPrimary, false);

        // Update and render children if expanded
        if (expanded) {
            int currentY = y + HEADER_HEIGHT + CHILD_SPACING;

            for (Component child : childWidgets) {
                if (child.isVisible()) {
                    child.setPosition(x + CHILD_INDENT, currentY);

                    // Set child width to account for indentation
                    int childWidth = width - CHILD_INDENT - 10;
                    if (child.getWidth() == 0 || child.getWidth() > childWidth) {
                        child.setSize(childWidth, child.getHeight());
                    }

                    child.render(context, mouseX, mouseY, delta);
                    currentY += child.getHeight() + CHILD_SPACING;
                }
            }
        }
    }

    /**
     * Draws a right-pointing or down-pointing arrow.
     */
    private void drawArrow(DrawContext context, int x, int y, boolean pointDown) {
        int color = Theme.textSecondary;

        if (pointDown) {
            // Down arrow (▼)
            for (int i = 0; i < ARROW_SIZE / 2; i++) {
                int lineY = y + i;
                int lineX = x + i;
                int lineWidth = ARROW_SIZE - (i * 2);
                RenderUtils.drawRect(context, lineX, lineY, lineWidth, 1, color);
            }
        } else {
            // Right arrow (▶)
            for (int i = 0; i < ARROW_SIZE / 2; i++) {
                int lineX = x + i;
                int lineY = y + i;
                int lineHeight = ARROW_SIZE - (i * 2);
                RenderUtils.drawRect(context, lineX, lineY, 1, lineHeight, color);
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return false;

        // Check if clicked on header
        if (button == 0 && isMouseOverHeader(mouseX, mouseY)) {
            expanded = !expanded;
            updateHeight();
            return true;
        }

        // Propagate to children if expanded
        if (expanded) {
            for (int i = childWidgets.size() - 1; i >= 0; i--) {
                Component child = childWidgets.get(i);
                if (child.isVisible() && child.mouseClicked(mouseX, mouseY, button)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (!visible || !expanded) return false;

        for (int i = childWidgets.size() - 1; i >= 0; i--) {
            Component child = childWidgets.get(i);
            if (child.isVisible() && child.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (!visible || !expanded) return false;

        for (int i = childWidgets.size() - 1; i >= 0; i--) {
            Component child = childWidgets.get(i);
            if (child.isVisible() && child.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!visible || !expanded) return false;

        for (int i = childWidgets.size() - 1; i >= 0; i--) {
            Component child = childWidgets.get(i);
            if (child.isVisible() && child.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!visible || !expanded) return false;

        for (int i = childWidgets.size() - 1; i >= 0; i--) {
            Component child = childWidgets.get(i);
            if (child.isVisible() && child.charTyped(chr, modifiers)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void updateHoverState(int mouseX, int mouseY) {
        this.hovered = isMouseOverHeader(mouseX, mouseY);

        if (expanded) {
            for (Component child : childWidgets) {
                child.updateHoverState(mouseX, mouseY);
            }
        }
    }

    /**
     * Checks if mouse is over the header area.
     */
    private boolean isMouseOverHeader(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width &&
               mouseY >= y && mouseY < y + HEADER_HEIGHT;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
