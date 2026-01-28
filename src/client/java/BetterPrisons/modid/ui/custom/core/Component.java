package BetterPrisons.modid.ui.custom.core;

import net.minecraft.client.gui.DrawContext;

/**
 * Base class for all UI components in the custom UI system.
 * Provides core functionality for rendering, layout, and event handling.
 */
public abstract class Component {
    protected int x;
    protected int y;
    protected int width;
    protected int height;
    protected boolean visible = true;
    protected boolean hovered = false;
    protected Container parent;
    protected Constraint constraint;

    public Component() {
        this.constraint = new Constraint();
    }

    /**
     * Sets the position of this component.
     */
    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Sets the size of this component.
     */
    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    /**
     * Applies the constraint to position and size this component relative to its parent.
     */
    public void layout() {
        if (constraint != null && parent != null) {
            constraint.apply(this, parent);
        }
    }

    /**
     * Renders this component.
     */
    public abstract void render(DrawContext context, int mouseX, int mouseY, float delta);

    /**
     * Called when the mouse is clicked.
     * @return true if the event was handled
     */
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return false;
    }

    /**
     * Called when the mouse is released.
     * @return true if the event was handled
     */
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return false;
    }

    /**
     * Called when the mouse is dragged.
     * @return true if the event was handled
     */
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        return false;
    }

    /**
     * Called when the mouse is scrolled.
     * @return true if the event was handled
     */
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        return false;
    }

    /**
     * Called when a key is pressed.
     * @return true if the event was handled
     */
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    /**
     * Called when a key is released.
     * @return true if the event was handled
     */
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        return false;
    }

    /**
     * Called when a character is typed.
     * @return true if the event was handled
     */
    public boolean charTyped(char chr, int modifiers) {
        return false;
    }

    /**
     * Updates the hover state based on mouse position.
     */
    public void updateHoverState(int mouseX, int mouseY) {
        this.hovered = isMouseOver(mouseX, mouseY);
    }

    /**
     * Checks if the mouse is over this component.
     */
    public boolean isMouseOver(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    // Getters and setters

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean isHovered() {
        return hovered;
    }

    public void setParent(Container parent) {
        this.parent = parent;
    }

    public Container getParent() {
        return parent;
    }

    public Constraint getConstraint() {
        return constraint;
    }

    public void setConstraint(Constraint constraint) {
        this.constraint = constraint;
    }
}
