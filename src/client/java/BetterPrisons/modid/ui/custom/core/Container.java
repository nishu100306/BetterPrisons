package BetterPrisons.modid.ui.custom.core;

import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

/**
 * A component that can contain and manage child components.
 * Events are propagated to children in reverse order (top-to-bottom z-order).
 */
public class Container extends Component {
    protected List<Component> children = new ArrayList<>();

    /**
     * Adds a child component to this container.
     */
    public void addChild(Component child) {
        children.add(child);
        child.setParent(this);
    }

    /**
     * Removes a child component from this container.
     */
    public void removeChild(Component child) {
        children.remove(child);
        child.setParent(null);
    }

    /**
     * Removes all children from this container.
     */
    public void clearChildren() {
        for (Component child : children) {
            child.setParent(null);
        }
        children.clear();
    }

    /**
     * Applies layout to all children.
     */
    @Override
    public void layout() {
        super.layout();
        for (Component child : children) {
            child.layout();
        }
    }

    /**
     * Renders all visible children.
     */
    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        for (Component child : children) {
            if (child.isVisible()) {
                child.render(context, mouseX, mouseY, delta);
            }
        }
    }

    /**
     * Updates hover state for all children.
     */
    @Override
    public void updateHoverState(int mouseX, int mouseY) {
        super.updateHoverState(mouseX, mouseY);
        for (Component child : children) {
            child.updateHoverState(mouseX, mouseY);
        }
    }

    /**
     * Propagates mouse click events to children in reverse order (top-to-bottom).
     */
    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        for (int i = children.size() - 1; i >= 0; i--) {
            Component child = children.get(i);
            if (child.isVisible() && child.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Propagates mouse release events to children in reverse order.
     */
    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (int i = children.size() - 1; i >= 0; i--) {
            Component child = children.get(i);
            if (child.isVisible() && child.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Propagates mouse drag events to children in reverse order.
     */
    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        for (int i = children.size() - 1; i >= 0; i--) {
            Component child = children.get(i);
            if (child.isVisible() && child.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Propagates mouse scroll events to children in reverse order.
     */
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (int i = children.size() - 1; i >= 0; i--) {
            Component child = children.get(i);
            if (child.isVisible() && child.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Propagates key press events to children in reverse order.
     */
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        for (int i = children.size() - 1; i >= 0; i--) {
            Component child = children.get(i);
            if (child.isVisible() && child.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Propagates key release events to children in reverse order.
     */
    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        for (int i = children.size() - 1; i >= 0; i--) {
            Component child = children.get(i);
            if (child.isVisible() && child.keyReleased(keyCode, scanCode, modifiers)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Propagates character typed events to children in reverse order.
     */
    @Override
    public boolean charTyped(char chr, int modifiers) {
        for (int i = children.size() - 1; i >= 0; i--) {
            Component child = children.get(i);
            if (child.isVisible() && child.charTyped(chr, modifiers)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets all children of this container.
     */
    public List<Component> getChildren() {
        return children;
    }
}
