package BetterPrisons.modid.ui.custom.containers;

import BetterPrisons.modid.ui.custom.core.Component;
import BetterPrisons.modid.ui.custom.core.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * Container for a category of settings.
 * Extends ScrollContainer and provides vertical layout with spacing.
 */
public class CategoryContainer extends ScrollContainer {
    private String title;
    private static final int PADDING = 12;
    private static final int SPACING = 8;
    private static final int TITLE_HEIGHT = 20;

    public CategoryContainer(String title) {
        super();
        this.title = title;
    }

    /**
     * Adds a widget to this category. Call layout() after adding all widgets.
     */
    public void addWidget(Component widget) {
        addChild(widget);
        // Don't layout on every add - caller should call layout() once at the end
    }

    /**
     * Layouts children vertically with spacing.
     */
    private void layoutChildren() {
        int currentY = PADDING;

        for (Component child : children) {
            if (child.isVisible()) {
                // Apply scroll offset to Y position
                int finalY = y + currentY - scrollOffset;
                child.setPosition(x + PADDING * 3, finalY);
                currentY += child.getHeight() + SPACING;
            }
        }

        calculateContentHeight();
    }

    @Override
    public void layout() {
        // Apply our own constraint (from Component.layout())
        if (constraint != null && parent != null) {
            constraint.apply(this, parent);
        }

        // Layout children using our custom positioning
        layoutChildren();

        // DON'T call super.layout() - it would reset all child positions to parent position!
        // We handle child positioning manually in layoutChildren()
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        // Update child positions based on current scroll offset
        layoutChildren();

        // Render children (scrollable content) - ScrollContainer handles the coordinate transformation
        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    protected void calculateContentHeight() {
        int currentY = PADDING;

        for (Component child : children) {
            if (child.isVisible()) {
                currentY += child.getHeight() + SPACING;
            }
        }

        contentHeight = currentY + PADDING;
        scrollbarVisible = contentHeight > height;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the padding value.
     */
    public static int getPadding() {
        return PADDING;
    }

    /**
     * Gets the spacing between widgets.
     */
    public static int getSpacing() {
        return SPACING;
    }
}
