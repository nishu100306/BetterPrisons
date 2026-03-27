package BetterPrisons.modid.ui.custom.widgets;

import BetterPrisons.modid.ui.custom.core.Component;
import BetterPrisons.modid.ui.custom.core.Theme;
import BetterPrisons.modid.ui.custom.core.TooltipProvider;
import BetterPrisons.modid.ui.custom.rendering.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Dropdown menu widget for selecting from a list of options.
 */
public class DropdownWidget extends Component implements TooltipProvider {
    private String label;
    private List<String> options;
    private int selectedIndex;
    private int defaultIndex;
    private String tooltip;
    private boolean expanded = false;

    private static final int DROPDOWN_HEIGHT = 20;
    private static final int OPTION_HEIGHT = 18;
    private static final int ARROW_SIZE = 6;
    private static final int LABEL_SPACING = 8;
    private static final int MAX_VISIBLE_OPTIONS = 8;
    private static final int RESET_BUTTON_SIZE = 14;
    private static final int RESET_BUTTON_SPACING = 6;

    private int scrollOffset = 0;
    private Consumer<String> onChange;
    private int[] optionColors = null;

    public void setOptionColors(int[] colors) {
        this.optionColors = colors;
    }

    public void setOnChange(Consumer<String> onChange) {
        this.onChange = onChange;
    }

    public DropdownWidget(String label, List<String> options, int initialIndex) {
        this.label = label;
        this.options = new ArrayList<>(options);
        this.selectedIndex = Math.max(0, Math.min(options.size() - 1, initialIndex));
        this.defaultIndex = this.selectedIndex;
        this.height = DROPDOWN_HEIGHT;
    }

    public void setTooltip(String tooltip) {
        this.tooltip = tooltip;
    }

    public String getTooltip() {
        return tooltip;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public void setSelectedIndex(int index) {
        this.selectedIndex = Math.max(0, Math.min(options.size() - 1, index));
        this.expanded = false;
        if (onChange != null) onChange.accept(getSelectedValue());
    }

    public String getSelectedValue() {
        if (selectedIndex >= 0 && selectedIndex < options.size()) {
            return options.get(selectedIndex);
        }
        return "";
    }

    public void setSelectedValue(String value) {
        int index = options.indexOf(value);
        if (index >= 0) {
            setSelectedIndex(index);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible) return;

        // Update hover state
        updateHoverState(mouseX, mouseY);

        MinecraftClient client = MinecraftClient.getInstance();

        // Draw label
        context.drawText(client.textRenderer, label, x, y + (DROPDOWN_HEIGHT - 8) / 2, Theme.textPrimary, false);

        // Calculate dropdown position
        int labelWidth = client.textRenderer.getWidth(label);
        int dropdownX = x + labelWidth + LABEL_SPACING;
        int dropdownY = y;
        int dropdownWidth = 120;

        // Draw dropdown background
        int bgColor = hovered ? Theme.widgetBackgroundHover : Theme.widgetBackground;
        RenderUtils.drawRect(context, dropdownX, dropdownY, dropdownWidth, DROPDOWN_HEIGHT, bgColor);

        // Draw dropdown border
        int borderColor = expanded ? Theme.borderFocus : (hovered ? Theme.borderHover : Theme.borderPrimary);
        RenderUtils.drawRectOutline(context, dropdownX, dropdownY, dropdownWidth, DROPDOWN_HEIGHT, borderColor, 1);

        // Draw selected value
        String selectedText = getSelectedValue();
        if (selectedText.length() > 0) {
            int textWidth = client.textRenderer.getWidth(selectedText);
            if (textWidth > dropdownWidth - 20) {
                // Truncate if too long
                while (textWidth > dropdownWidth - 20 && selectedText.length() > 0) {
                    selectedText = selectedText.substring(0, selectedText.length() - 1);
                    textWidth = client.textRenderer.getWidth(selectedText + "...");
                }
                selectedText += "...";
            }
            int selectedTextColor = (optionColors != null && selectedIndex >= 0 && selectedIndex < optionColors.length)
                ? (0xFF000000 | (optionColors[selectedIndex] & 0xFFFFFF)) : Theme.textPrimary;
            context.drawText(client.textRenderer, selectedText, dropdownX + 4, dropdownY + 6, selectedTextColor, false);
        }

        // Draw arrow
        int arrowX = dropdownX + dropdownWidth - ARROW_SIZE - 4;
        int arrowY = dropdownY + (DROPDOWN_HEIGHT - ARROW_SIZE) / 2;
        drawArrow(context, arrowX, arrowY, expanded);

        // Draw reset button
        int resetX = dropdownX + dropdownWidth + RESET_BUTTON_SPACING;
        int resetY = dropdownY + (DROPDOWN_HEIGHT - RESET_BUTTON_SIZE) / 2;
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
        this.width = labelWidth + LABEL_SPACING + dropdownWidth + RESET_BUTTON_SPACING + RESET_BUTTON_SIZE;
    }

    /**
     * Renders the expanded dropdown list. Should be called after all other widgets
     * to ensure it appears on top.
     */
    public void renderExpandedList(DrawContext context, int mouseX, int mouseY, float delta) {
        if (!visible || !expanded) return;

        MinecraftClient client = MinecraftClient.getInstance();
        int labelWidth = client.textRenderer.getWidth(label);
        int dropdownX = x + labelWidth + LABEL_SPACING;
        int dropdownY = y;
        int dropdownWidth = 120;

        int listY = dropdownY + DROPDOWN_HEIGHT;
        int visibleOptions = Math.min(MAX_VISIBLE_OPTIONS, options.size());
        int listHeight = visibleOptions * OPTION_HEIGHT;

        // Draw list background
        RenderUtils.drawRect(context, dropdownX, listY, dropdownWidth, listHeight, Theme.widgetBackground);
        RenderUtils.drawRectOutline(context, dropdownX, listY, dropdownWidth, listHeight, Theme.borderPrimary, 1);

        // Draw options
        int endIndex = Math.min(scrollOffset + visibleOptions, options.size());
        for (int i = scrollOffset; i < endIndex; i++) {
            int optionY = listY + (i - scrollOffset) * OPTION_HEIGHT;
            boolean optionHovered = mouseX >= dropdownX && mouseX < dropdownX + dropdownWidth &&
                                   mouseY >= optionY && mouseY < optionY + OPTION_HEIGHT;

            // Highlight selected or hovered
            if (i == selectedIndex) {
                RenderUtils.drawRect(context, dropdownX, optionY, dropdownWidth, OPTION_HEIGHT, Theme.accentPrimary);
            } else if (optionHovered) {
                RenderUtils.drawRect(context, dropdownX, optionY, dropdownWidth, OPTION_HEIGHT, Theme.widgetBackgroundHover);
            }

            // Draw option text (use per-option color if provided)
            String optionText = options.get(i);
            int optionTextColor = (optionColors != null && i < optionColors.length)
                ? (0xFF000000 | (optionColors[i] & 0xFFFFFF)) : Theme.textPrimary;
            context.drawText(client.textRenderer, optionText, dropdownX + 4, optionY + 5, optionTextColor, false);
        }
    }

    public boolean isExpanded() {
        return expanded;
    }

    private void drawArrow(DrawContext context, int x, int y, boolean down) {
        // Simple triangle arrow
        if (down) {
            // Down arrow (expanded)
            for (int i = 0; i < ARROW_SIZE; i++) {
                int lineWidth = i * 2 + 1;
                RenderUtils.drawRect(context, x + ARROW_SIZE / 2 - i, y + i, lineWidth, 1, Theme.textSecondary);
            }
        } else {
            // Right arrow (collapsed)
            for (int i = 0; i < ARROW_SIZE; i++) {
                int lineHeight = ARROW_SIZE - Math.abs(i - ARROW_SIZE / 2) * 2;
                if (lineHeight > 0) {
                    RenderUtils.drawRect(context, x + i, y + ARROW_SIZE / 2 - lineHeight / 2, 1, lineHeight, Theme.textSecondary);
                }
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!visible || button != 0) return false;

        MinecraftClient client = MinecraftClient.getInstance();
        int labelWidth = client.textRenderer.getWidth(label);
        int dropdownX = x + labelWidth + LABEL_SPACING;
        int dropdownY = y;
        int dropdownWidth = 120;

        // Check if clicking reset button
        int resetX = dropdownX + dropdownWidth + RESET_BUTTON_SPACING;
        int resetY = dropdownY + (DROPDOWN_HEIGHT - RESET_BUTTON_SIZE) / 2;
        if (mouseX >= resetX && mouseX < resetX + RESET_BUTTON_SIZE &&
            mouseY >= resetY && mouseY < resetY + RESET_BUTTON_SIZE) {
            resetToDefault();
            return true;
        }

        // Check if clicking dropdown header
        if (mouseX >= dropdownX && mouseX < dropdownX + dropdownWidth &&
            mouseY >= dropdownY && mouseY < dropdownY + DROPDOWN_HEIGHT) {
            expanded = !expanded;
            return true;
        }

        // Check if clicking an option in expanded list
        if (expanded) {
            int listY = dropdownY + DROPDOWN_HEIGHT;
            int visibleOptions = Math.min(MAX_VISIBLE_OPTIONS, options.size());
            int listHeight = visibleOptions * OPTION_HEIGHT;

            if (mouseX >= dropdownX && mouseX < dropdownX + dropdownWidth &&
                mouseY >= listY && mouseY < listY + listHeight) {
                int clickedIndex = scrollOffset + (int) ((mouseY - listY) / OPTION_HEIGHT);
                if (clickedIndex < options.size()) {
                    setSelectedIndex(clickedIndex);
                    return true;
                }
            } else {
                // Clicked outside, close dropdown
                expanded = false;
                return true;
            }
        }

        return false;
    }

    public void resetToDefault() {
        setSelectedIndex(defaultIndex);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (!visible || !expanded) return false;

        if (isMouseOver(mouseX, mouseY)) {
            scrollOffset = Math.max(0, Math.min(options.size() - MAX_VISIBLE_OPTIONS,
                scrollOffset - (int) verticalAmount));
            return true;
        }
        return false;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        MinecraftClient client = MinecraftClient.getInstance();
        int labelWidth = client.textRenderer.getWidth(label);
        int dropdownX = x + labelWidth + LABEL_SPACING;
        int dropdownY = y;
        int dropdownWidth = 120;

        if (expanded) {
            int listY = dropdownY + DROPDOWN_HEIGHT;
            int visibleOptions = Math.min(MAX_VISIBLE_OPTIONS, options.size());
            int listHeight = visibleOptions * OPTION_HEIGHT;

            return (mouseX >= dropdownX && mouseX < dropdownX + dropdownWidth &&
                   mouseY >= dropdownY && mouseY < dropdownY + DROPDOWN_HEIGHT) ||
                   (mouseX >= dropdownX && mouseX < dropdownX + dropdownWidth &&
                   mouseY >= listY && mouseY < listY + listHeight);
        }

        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<String> getOptions() {
        return new ArrayList<>(options);
    }

    public void setOptions(List<String> options) {
        this.options = new ArrayList<>(options);
        if (selectedIndex >= options.size()) {
            selectedIndex = options.size() - 1;
        }
    }
}
