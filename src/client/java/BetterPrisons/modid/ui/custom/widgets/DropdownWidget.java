package BetterPrisons.modid.ui.custom.widgets;

import BetterPrisons.modid.ui.custom.core.Component;
import BetterPrisons.modid.ui.custom.core.Theme;
import BetterPrisons.modid.ui.custom.rendering.RenderUtils;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Dropdown menu widget for selecting from a list of options.
 */
public class DropdownWidget extends Component {
    private String label;
    private List<String> options;
    private int selectedIndex;
    private String tooltip;
    private boolean expanded = false;

    private static final int DROPDOWN_HEIGHT = 20;
    private static final int OPTION_HEIGHT = 18;
    private static final int ARROW_SIZE = 6;
    private static final int LABEL_SPACING = 8;
    private static final int MAX_VISIBLE_OPTIONS = 8;

    private int scrollOffset = 0;

    public DropdownWidget(String label, List<String> options, int initialIndex) {
        this.label = label;
        this.options = new ArrayList<>(options);
        this.selectedIndex = Math.max(0, Math.min(options.size() - 1, initialIndex));
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
            context.drawText(client.textRenderer, selectedText, dropdownX + 4, dropdownY + 6, Theme.textPrimary, false);
        }

        // Draw arrow
        int arrowX = dropdownX + dropdownWidth - ARROW_SIZE - 4;
        int arrowY = dropdownY + (DROPDOWN_HEIGHT - ARROW_SIZE) / 2;
        drawArrow(context, arrowX, arrowY, expanded);

        // Draw expanded list if open
        if (expanded) {
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

                // Draw option text
                String optionText = options.get(i);
                context.drawText(client.textRenderer, optionText, dropdownX + 4, optionY + 5, Theme.textPrimary, false);
            }
        }

        // Update total width for layout
        this.width = labelWidth + LABEL_SPACING + dropdownWidth;
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
