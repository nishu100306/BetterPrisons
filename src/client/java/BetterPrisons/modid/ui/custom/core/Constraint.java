package BetterPrisons.modid.ui.custom.core;

/**
 * Fluent API for positioning and sizing components relative to their parent.
 * Supports anchors and absolute/relative positioning.
 */
public class Constraint {
    public enum Anchor {
        LEFT,
        RIGHT,
        TOP,
        BOTTOM,
        CENTER
    }

    private Anchor horizontalAnchor;
    private Anchor verticalAnchor;
    private Integer xOffset;
    private Integer yOffset;
    private Integer widthValue;
    private Integer heightValue;
    private boolean widthRelative = false;
    private boolean heightRelative = false;

    /**
     * Sets the left anchor with optional offset.
     */
    public Constraint left(int offset) {
        this.horizontalAnchor = Anchor.LEFT;
        this.xOffset = offset;
        return this;
    }

    /**
     * Sets the right anchor with optional offset.
     */
    public Constraint right(int offset) {
        this.horizontalAnchor = Anchor.RIGHT;
        this.xOffset = offset;
        return this;
    }

    /**
     * Sets the top anchor with optional offset.
     */
    public Constraint top(int offset) {
        this.verticalAnchor = Anchor.TOP;
        this.yOffset = offset;
        return this;
    }

    /**
     * Sets the bottom anchor with optional offset.
     */
    public Constraint bottom(int offset) {
        this.verticalAnchor = Anchor.BOTTOM;
        this.yOffset = offset;
        return this;
    }

    /**
     * Sets the horizontal center anchor with optional offset.
     */
    public Constraint centerX(int offset) {
        this.horizontalAnchor = Anchor.CENTER;
        this.xOffset = offset;
        return this;
    }

    /**
     * Sets the horizontal center anchor.
     */
    public Constraint centerX() {
        return centerX(0);
    }

    /**
     * Sets the vertical center anchor with optional offset.
     */
    public Constraint centerY(int offset) {
        this.verticalAnchor = Anchor.CENTER;
        this.yOffset = offset;
        return this;
    }

    /**
     * Sets the vertical center anchor.
     */
    public Constraint centerY() {
        return centerY(0);
    }

    /**
     * Sets the width in pixels.
     */
    public Constraint width(int width) {
        this.widthValue = width;
        this.widthRelative = false;
        return this;
    }

    /**
     * Sets the width as a percentage of parent width (0-100).
     */
    public Constraint widthPercent(int percent) {
        this.widthValue = percent;
        this.widthRelative = true;
        return this;
    }

    /**
     * Sets the height in pixels.
     */
    public Constraint height(int height) {
        this.heightValue = height;
        this.heightRelative = false;
        return this;
    }

    /**
     * Sets the height as a percentage of parent height (0-100).
     */
    public Constraint heightPercent(int percent) {
        this.heightValue = percent;
        this.heightRelative = true;
        return this;
    }

    /**
     * Applies this constraint to a component based on its parent.
     */
    public void apply(Component component, Container parent) {
        int parentX = parent.getX();
        int parentY = parent.getY();
        int parentWidth = parent.getWidth();
        int parentHeight = parent.getHeight();

        // Calculate width
        if (widthValue != null) {
            int width = widthRelative ? (parentWidth * widthValue / 100) : widthValue;
            component.setSize(width, component.getHeight());
        }

        // Calculate height
        if (heightValue != null) {
            int height = heightRelative ? (parentHeight * heightValue / 100) : heightValue;
            component.setSize(component.getWidth(), height);
        }

        // Calculate X position
        int x = parentX;
        if (horizontalAnchor != null) {
            switch (horizontalAnchor) {
                case LEFT:
                    x = parentX + (xOffset != null ? xOffset : 0);
                    break;
                case RIGHT:
                    x = parentX + parentWidth - component.getWidth() - (xOffset != null ? xOffset : 0);
                    break;
                case CENTER:
                    x = parentX + (parentWidth - component.getWidth()) / 2 + (xOffset != null ? xOffset : 0);
                    break;
            }
        }

        // Calculate Y position
        int y = parentY;
        if (verticalAnchor != null) {
            switch (verticalAnchor) {
                case TOP:
                    y = parentY + (yOffset != null ? yOffset : 0);
                    break;
                case BOTTOM:
                    y = parentY + parentHeight - component.getHeight() - (yOffset != null ? yOffset : 0);
                    break;
                case CENTER:
                    y = parentY + (parentHeight - component.getHeight()) / 2 + (yOffset != null ? yOffset : 0);
                    break;
            }
        }

        component.setPosition(x, y);
    }
}
