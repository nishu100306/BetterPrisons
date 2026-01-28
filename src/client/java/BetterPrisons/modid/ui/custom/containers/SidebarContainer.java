package BetterPrisons.modid.ui.custom.containers;

import BetterPrisons.modid.ui.custom.core.Component;
import BetterPrisons.modid.ui.custom.core.Theme;
import BetterPrisons.modid.ui.custom.rendering.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Sidebar container for category navigation.
 * Displays vertical tabs with selection states.
 * Supports scrolling when tabs exceed container height.
 */
public class SidebarContainer extends ScrollContainer {
    private List<CategoryTab> tabs = new ArrayList<>();
    private int selectedIndex = 0;
    private Consumer<Integer> onSelectionChange;

    private static final int TAB_HEIGHT = 30;
    private static final int PADDING = 8;
    private static final int SEPARATOR_HEIGHT = 10;

    /**
     * Represents a category tab in the sidebar.
     */
    public static class CategoryTab {
        public String name;
        public boolean isSeparator;

        public CategoryTab(String name) {
            this.name = name;
            this.isSeparator = false;
        }

        public static CategoryTab separator() {
            CategoryTab tab = new CategoryTab("");
            tab.isSeparator = true;
            return tab;
        }
    }

    public SidebarContainer() {
        super();
    }

    /**
     * Adds a category tab to the sidebar.
     */
    public void addTab(String name) {
        tabs.add(new CategoryTab(name));
    }

    /**
     * Adds a separator between tab groups.
     */
    public void addSeparator() {
        tabs.add(CategoryTab.separator());
    }

    /**
     * Sets the callback for when selection changes.
     */
    public void setOnSelectionChange(Consumer<Integer> callback) {
        this.onSelectionChange = callback;
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        // Calculate content height
        calculateContentHeight();

        MinecraftClient client = MinecraftClient.getInstance();

        // Draw sidebar background
        RenderUtils.drawRect(context, x, y, width, height, Theme.sidebarBackground);

        // Apply scissor test for clipping (excluding scrollbar area)
        int contentWidth = scrollbarVisible ? width - 8 : width;
        RenderUtils.pushScissor(x, y, contentWidth, height);

        // Draw tabs with scroll offset applied
        int currentY = y + PADDING - scrollOffset;
        int tabIndex = 0;

        for (int i = 0; i < tabs.size(); i++) {
            CategoryTab tab = tabs.get(i);

            if (tab.isSeparator) {
                // Draw separator line
                int separatorY = currentY + SEPARATOR_HEIGHT / 2;
                RenderUtils.drawRect(context, x + PADDING, separatorY, contentWidth - PADDING * 2, 1, Theme.borderPrimary);
                currentY += SEPARATOR_HEIGHT;
            } else {
                // Check if tab is hovered
                boolean tabHovered = mouseX >= x && mouseX < x + contentWidth &&
                                    mouseY >= currentY && mouseY < currentY + TAB_HEIGHT;

                // Draw tab background
                int bgColor;
                if (tabIndex == selectedIndex) {
                    bgColor = Theme.sidebarItemSelected;
                } else if (tabHovered) {
                    bgColor = Theme.sidebarItemHover;
                } else {
                    bgColor = Theme.sidebarBackground;
                }

                if (bgColor != Theme.sidebarBackground) {
                    RenderUtils.drawRect(context, x, currentY, contentWidth, TAB_HEIGHT, bgColor);
                }

                // Draw tab text
                int textColor = tabIndex == selectedIndex ? Theme.textPrimary : Theme.textSecondary;
                context.drawText(client.textRenderer, tab.name, x + PADDING, currentY + (TAB_HEIGHT - 8) / 2, textColor, false);

                currentY += TAB_HEIGHT;
                tabIndex++;
            }
        }

        RenderUtils.popScissor();

        // Draw scrollbar if needed
        if (scrollbarVisible) {
            renderScrollbar(context, mouseX, mouseY);
        }

        // Draw right border
        RenderUtils.drawRect(context, x + width - 1, y, 1, height, Theme.borderPrimary);
    }

    /**
     * Renders the scrollbar.
     */
    private void renderScrollbar(DrawContext context, int mouseX, int mouseY) {
        int scrollbarX = x + width - 8;
        int scrollbarY = y;
        int scrollbarHeight = height;

        // Draw track
        RenderUtils.drawRect(context, scrollbarX, scrollbarY, 8, scrollbarHeight, Theme.scrollbarTrack);

        // Calculate thumb size and position
        float visibleRatio = (float) height / contentHeight;
        int thumbHeight = Math.max(20, (int) (scrollbarHeight * visibleRatio));

        float scrollRatio = (float) scrollOffset / (contentHeight - height);
        int thumbY = scrollbarY + (int) ((scrollbarHeight - thumbHeight) * scrollRatio);

        // Check if hovering over thumb
        boolean thumbHovered = mouseX >= scrollbarX && mouseX < scrollbarX + 8 &&
                              mouseY >= thumbY && mouseY < thumbY + thumbHeight;

        // Draw thumb
        int thumbColor = draggingScrollbar ? Theme.scrollbarThumbHover :
                        (thumbHovered ? Theme.scrollbarThumbHover : Theme.scrollbarThumb);
        RenderUtils.drawRect(context, scrollbarX + 2, thumbY, 8 - 4, thumbHeight, thumbColor);
    }

    @Override
    protected void calculateContentHeight() {
        int totalHeight = PADDING * 2;
        for (CategoryTab tab : tabs) {
            if (tab.isSeparator) {
                totalHeight += SEPARATOR_HEIGHT;
            } else {
                totalHeight += TAB_HEIGHT;
            }
        }
        contentHeight = totalHeight;
        scrollbarVisible = contentHeight > height;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible) return false;

        // Check scrollbar click first
        if (button == 0 && scrollbarVisible) {
            int scrollbarX = x + width - 8;

            if (mouseX >= scrollbarX && mouseX < scrollbarX + 8 &&
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

        // Check tab clicks (with scroll offset applied)
        if (button == 0) {
            int currentY = y + PADDING - scrollOffset;
            int tabIndex = 0;
            int contentWidth = scrollbarVisible ? width - 8 : width;

            for (CategoryTab tab : tabs) {
                if (tab.isSeparator) {
                    currentY += SEPARATOR_HEIGHT;
                } else {
                    if (mouseX >= x && mouseX < x + contentWidth &&
                        mouseY >= currentY && mouseY < currentY + TAB_HEIGHT) {
                        setSelectedIndex(tabIndex);
                        return true;
                    }
                    currentY += TAB_HEIGHT;
                    tabIndex++;
                }
            }
        }

        return false;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int index) {
        // Count non-separator tabs
        int nonSeparatorCount = 0;
        for (CategoryTab tab : tabs) {
            if (!tab.isSeparator) {
                nonSeparatorCount++;
            }
        }

        if (index >= 0 && index < nonSeparatorCount && index != selectedIndex) {
            selectedIndex = index;
            if (onSelectionChange != null) {
                onSelectionChange.accept(index);
            }
        }
    }

    public String getSelectedTabName() {
        int tabIndex = 0;
        for (CategoryTab tab : tabs) {
            if (!tab.isSeparator) {
                if (tabIndex == selectedIndex) {
                    return tab.name;
                }
                tabIndex++;
            }
        }
        return "";
    }

    public List<CategoryTab> getTabs() {
        return new ArrayList<>(tabs);
    }

    public int getTabCount() {
        int count = 0;
        for (CategoryTab tab : tabs) {
            if (!tab.isSeparator) {
                count++;
            }
        }
        return count;
    }
}
