package BetterPrisons.modid.ui.custom.containers;

import BetterPrisons.modid.ui.custom.core.Component;
import BetterPrisons.modid.ui.custom.core.Container;
import BetterPrisons.modid.ui.custom.core.Theme;
import BetterPrisons.modid.ui.custom.rendering.RenderUtils;
import net.minecraft.client.gui.DrawContext;

/**
 * Scrollable container with vertical scrolling and scissor clipping.
 * Features a scrollbar and mouse wheel support.
 */
public class ScrollContainer extends Container {
    protected int scrollOffset = 0;
    protected int contentHeight = 0;
    protected boolean scrollbarVisible = false;
    protected boolean draggingScrollbar = false;
    protected int dragStartY = 0;
    protected int dragStartScroll = 0;

    private static final int SCROLLBAR_WIDTH = 8;
    private static final int SCROLLBAR_PADDING = 2;

    public ScrollContainer() {
        super();
    }

    /**
     * Calculates the total content height based on children.
     */
    protected void calculateContentHeight() {
        contentHeight = 0;
        for (Component child : children) {
            if (child.isVisible()) {
                int childBottom = child.getY() - y + child.getHeight();
                if (childBottom > contentHeight) {
                    contentHeight = childBottom;
                }
            }
        }
        scrollbarVisible = contentHeight > height;
    }

    @Override
    public void layout() {
        super.layout();
        calculateContentHeight();

        // Clamp scroll offset
        int maxScroll = Math.max(0, contentHeight - height);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        calculateContentHeight();

        // Apply scissor test for clipping
        RenderUtils.pushScissor(x, y, width - (scrollbarVisible ? SCROLLBAR_WIDTH : 0), height);

        // Render children directly without matrix transforms
        for (Component child : children) {
            if (child.isVisible()) {
                child.render(context, mouseX, mouseY, delta);
            }
        }

        RenderUtils.popScissor();

        // Draw scrollbar if needed
        if (scrollbarVisible) {
            renderScrollbar(context, mouseX, mouseY);
        }
    }

    private void renderScrollbar(DrawContext context, int mouseX, int mouseY) {
        int scrollbarX = x + width - SCROLLBAR_WIDTH;
        int scrollbarY = y;
        int scrollbarHeight = height;

        // Draw track
        RenderUtils.drawRect(context, scrollbarX, scrollbarY, SCROLLBAR_WIDTH, scrollbarHeight, Theme.scrollbarTrack);

        // Calculate thumb size and position
        float visibleRatio = (float) height / contentHeight;
        int thumbHeight = Math.max(20, (int) (scrollbarHeight * visibleRatio));

        float scrollRatio = (float) scrollOffset / (contentHeight - height);
        int thumbY = scrollbarY + (int) ((scrollbarHeight - thumbHeight) * scrollRatio);

        // Check if hovering over thumb
        boolean thumbHovered = mouseX >= scrollbarX && mouseX < scrollbarX + SCROLLBAR_WIDTH &&
                              mouseY >= thumbY && mouseY < thumbY + thumbHeight;

        // Draw thumb
        int thumbColor = draggingScrollbar ? Theme.scrollbarThumbHover :
                        (thumbHovered ? Theme.scrollbarThumbHover : Theme.scrollbarThumb);
        RenderUtils.drawRect(context, scrollbarX + SCROLLBAR_PADDING, thumbY,
                           SCROLLBAR_WIDTH - SCROLLBAR_PADDING * 2, thumbHeight, thumbColor);
    }

    @Override
    public void updateHoverState(int mouseX, int mouseY) {
        super.updateHoverState(mouseX, mouseY);

        for (Component child : children) {
            child.updateHoverState(mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return false;

        // Check scrollbar click
        if (button == 0 && scrollbarVisible) {
            int scrollbarX = x + width - SCROLLBAR_WIDTH;

            if (mouseX >= scrollbarX && mouseX < scrollbarX + SCROLLBAR_WIDTH &&
                mouseY >= y && mouseY < y + height) {

                // Calculate thumb position
                float visibleRatio = (float) height / contentHeight;
                int thumbHeight = Math.max(20, (int) (height * visibleRatio));
                float scrollRatio = (float) scrollOffset / (contentHeight - height);
                int thumbY = y + (int) ((height - thumbHeight) * scrollRatio);

                if (mouseY >= thumbY && mouseY < thumbY + thumbHeight) {
                    // Clicked on thumb
                    draggingScrollbar = true;
                    dragStartY = (int) mouseY;
                    dragStartScroll = scrollOffset;
                    return true;
                } else {
                    // Clicked on track, jump to position
                    float clickRatio = (float) (mouseY - y) / height;
                    scrollOffset = (int) (clickRatio * (contentHeight - height));
                    scrollOffset = Math.max(0, Math.min(contentHeight - height, scrollOffset));
                    return true;
                }
            }
        }

        // Propagate to children
        for (int i = children.size() - 1; i >= 0; i--) {
            Component child = children.get(i);
            if (child.isVisible() && child.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggingScrollbar) {
            draggingScrollbar = false;
            return true;
        }

        // Propagate to children
        for (int i = children.size() - 1; i >= 0; i--) {
            Component child = children.get(i);
            if (child.isVisible() && child.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (draggingScrollbar) {
            int deltaScroll = (int) ((mouseY - dragStartY) * contentHeight / height);
            scrollOffset = dragStartScroll + deltaScroll;
            scrollOffset = Math.max(0, Math.min(contentHeight - height, scrollOffset));
            return true;
        }

        // Propagate to children
        for (int i = children.size() - 1; i >= 0; i--) {
            Component child = children.get(i);
            if (child.isVisible() && child.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
                return true;
            }
        }

        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!visible) return false;

        // Check if mouse is over this container
        if (isMouseOver(mouseX, mouseY)) {
            scrollOffset -= (int) (verticalAmount * 20);
            scrollOffset = Math.max(0, Math.min(Math.max(0, contentHeight - height), scrollOffset));
            return true;
        }

        // Propagate to children
        for (int i = children.size() - 1; i >= 0; i--) {
            Component child = children.get(i);
            if (child.isVisible() && child.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
                return true;
            }
        }

        return false;
    }

    public int getScrollOffset() {
        return scrollOffset;
    }

    public void setScrollOffset(int scrollOffset) {
        this.scrollOffset = scrollOffset;
        calculateContentHeight();
        int maxScroll = Math.max(0, contentHeight - height);
        this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, maxScroll));
    }

    public int getContentHeight() {
        return contentHeight;
    }

    public boolean isScrollbarVisible() {
        return scrollbarVisible;
    }
}
