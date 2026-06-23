package BetterPrisons.modid.ui.custom.screens;

import BetterPrisons.modid.BetterPrisonsClient;
import BetterPrisons.modid.Config;
import BetterPrisons.modid.misc.EnergyCalculator;
import BetterPrisons.modid.ui.custom.binding.BindingRegistry;
import BetterPrisons.modid.ui.custom.binding.ConfigBinding;
import BetterPrisons.modid.ui.custom.binding.FieldBinding;
import BetterPrisons.modid.ui.custom.containers.CategoryContainer;
import BetterPrisons.modid.ui.custom.containers.ColorPickerPopup;
import BetterPrisons.modid.ui.custom.containers.SidebarContainer;
import BetterPrisons.modid.ui.custom.core.Component;
import BetterPrisons.modid.ui.custom.core.Container;
import BetterPrisons.modid.ui.custom.core.Theme;
import BetterPrisons.modid.ui.custom.core.TooltipProvider;
import BetterPrisons.modid.ui.custom.rendering.RenderUtils;
import BetterPrisons.modid.ui.custom.widgets.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Custom configuration screen for BetterPrisons.
 * Replaces ClothConfig with a fully custom UI system.
 */
public class CustomConfigScreen extends Screen {
    private final Screen parent;
    private final Config config;

    private SidebarContainer sidebar;
    private List<CategoryContainer> categories = new ArrayList<>();
    private int currentCategoryIndex = 0;

    private Component doneButton;
    private Component editHudButton;

    private ColorPickerPopup activePopup = null;

    private static final int SIDEBAR_WIDTH = 150;
    private static final int TOP_BAR_HEIGHT = 30;
    private static final int BOTTOM_BAR_HEIGHT = 35;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_WIDTH = 100;
    private static final int BUTTON_SPACING = 10;

    public CustomConfigScreen(Screen parent) {
        super(Text.literal("BetterPrisons Settings"));
        this.parent = parent;
        this.config = BetterPrisonsClient.config;
    }

    @Override
    protected void init() {
        super.init();

        // Clear any existing bindings
        BindingRegistry.clear();

        // Load theme
        Theme.reload(config);

        // Create sidebar
        createSidebar();

        // Create categories
        createCategories();

        // Create buttons
        createButtons();
    }

    private void createSidebar() {
        sidebar = new SidebarContainer();
        sidebar.setPosition(0, 0);
        sidebar.setSize(SIDEBAR_WIDTH, height);

        // Add category tabs
        sidebar.addTab("Cooldown HUD");
        sidebar.addTab("Satchel HUD");
        sidebar.addTab("Stats HUD");
        sidebar.addTab("Enchant HUD");
        sidebar.addTab("Events HUD");
        sidebar.addTab("Waypoints");
        sidebar.addTab("Gang Pings");
        sidebar.addSeparator();
        sidebar.addTab("Super Breaker");
        sidebar.addTab("Peaceful Mining");
        sidebar.addTab("EasyView");
        sidebar.addTab("Item Cooldowns");
        sidebar.addTab("Misc");
        sidebar.addSeparator();
        sidebar.addTab("Tools");
        sidebar.addTab("Config Settings");

        sidebar.setSelectedIndex(currentCategoryIndex);
        sidebar.setOnSelectionChange(index -> {
            currentCategoryIndex = index;
        });
    }

    private void createCategories() {
        categories.add(createCooldownHudCategory());
        categories.add(createSatchelHudCategory());
        categories.add(createStatsHudCategory());
        categories.add(createEnchantHudCategory());
        categories.add(createEventsHudCategory());
        categories.add(createWaypointsCategory());
        categories.add(createGangPingsCategory());
        categories.add(createSuperBreakerCategory());
        categories.add(createPeacefulMiningCategory());
        categories.add(createEasyViewCategory());
        categories.add(createItemCooldownsCategory());
        categories.add(createMiscCategory());
        categories.add(createToolsCategory());
        categories.add(createConfigSettingsCategory());

        // Position categories
        for (CategoryContainer category : categories) {
            category.setPosition(SIDEBAR_WIDTH, TOP_BAR_HEIGHT);
            category.setSize(width - SIDEBAR_WIDTH, height - TOP_BAR_HEIGHT - BOTTOM_BAR_HEIGHT);
            category.layout(); // Re-layout children now that position is set
        }
    }

    private void createButtons() {
        // Position buttons in the bottom bar
        int bottomBarY = height - BOTTOM_BAR_HEIGHT;
        int buttonY = bottomBarY + (BOTTOM_BAR_HEIGHT - BUTTON_HEIGHT) / 2;

        // Done button (right side)
        int doneX = width - BUTTON_WIDTH - BUTTON_SPACING;
        doneButton = new ButtonComponent("Done", doneX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT, () -> {
            saveAndClose();
        });

        // Edit HUD Positions button (left of Done button)
        int hudButtonWidth = 150;
        int hudButtonX = doneX - hudButtonWidth - BUTTON_SPACING;
        editHudButton = new ButtonComponent("Edit HUD Positions", hudButtonX, buttonY, hudButtonWidth, BUTTON_HEIGHT, () -> {
            if (client != null) {
                client.setScreen(new EnhancedHudEditorScreen(this));
            }
        });
    }

    private void saveAndClose() {
        // Save all widget values to config
        saveAllWidgetValues();

        // Save config to file
        config.save();

        // Re-sync live HUD objects from the updated config
        BetterPrisonsClient.applyConfig();

        // Close screen
        if (client != null) {
            client.setScreen(parent);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Draw background
        RenderUtils.drawRect(context, 0, 0, width, height, Theme.screenBackground);

        // Render sidebar
        sidebar.render(context, mouseX, mouseY, delta);

        // Render current category (behind bars so it gets clipped)
        if (currentCategoryIndex >= 0 && currentCategoryIndex < categories.size()) {
            categories.get(currentCategoryIndex).render(context, mouseX, mouseY, delta);
        }

        // Draw top bar (ON TOP of content to cover overflow)
        RenderUtils.drawRect(context, SIDEBAR_WIDTH, 0, width - SIDEBAR_WIDTH, TOP_BAR_HEIGHT, Theme.panelBackground);
        RenderUtils.drawRect(context, SIDEBAR_WIDTH, TOP_BAR_HEIGHT - 1, width - SIDEBAR_WIDTH, 1, Theme.borderPrimary);

        // Draw top bar title
        if (currentCategoryIndex >= 0 && currentCategoryIndex < categories.size()) {
            String categoryTitle = sidebar.getSelectedTabName();
            MinecraftClient client = MinecraftClient.getInstance();
            int titleX = SIDEBAR_WIDTH + 15;
            int titleY = (TOP_BAR_HEIGHT - 8) / 2;
            context.drawText(client.textRenderer, categoryTitle, titleX, titleY, Theme.textPrimary, false);
        }

        // Draw bottom bar (ON TOP of content to cover overflow)
        int bottomBarY = height - BOTTOM_BAR_HEIGHT;
        RenderUtils.drawRect(context, SIDEBAR_WIDTH, bottomBarY, width - SIDEBAR_WIDTH, 1, Theme.borderPrimary);
        RenderUtils.drawRect(context, SIDEBAR_WIDTH, bottomBarY, width - SIDEBAR_WIDTH, BOTTOM_BAR_HEIGHT, Theme.panelBackground);

        // Render buttons (on top of bottom bar)
        doneButton.render(context, mouseX, mouseY, delta);
        editHudButton.render(context, mouseX, mouseY, delta);

        // Render active popup (on top of everything)
        if (activePopup != null) {
            activePopup.render(context, mouseX, mouseY, delta);
        }

        // Render expanded dropdown lists (on top of everything except popups)
        if (currentCategoryIndex >= 0 && currentCategoryIndex < categories.size()) {
            renderExpandedDropdowns(context, categories.get(currentCategoryIndex).getChildren(), mouseX, mouseY, delta);
        }

        // Render tooltips (always on top)
        renderTooltips(context, mouseX, mouseY);
    }

    private void renderExpandedDropdowns(DrawContext context, List<Component> components, int mouseX, int mouseY, float delta) {
        for (Component child : components) {
            // Render expanded dropdown list if this is a dropdown
            if (child instanceof DropdownWidget) {
                DropdownWidget dropdown = (DropdownWidget) child;
                if (dropdown.isExpanded()) {
                    dropdown.renderExpandedList(context, mouseX, mouseY, delta);
                }
            }

            // Check children of CollapsibleWidget
            if (child instanceof CollapsibleWidget) {
                CollapsibleWidget collapsible = (CollapsibleWidget) child;
                if (collapsible.isExpanded()) {
                    renderExpandedDropdowns(context, collapsible.getChildWidgets(), mouseX, mouseY, delta);
                }
            }
        }
    }

    private boolean handleExpandedDropdownClick(List<Component> components, double mouseX, double mouseY, int button) {
        for (Component child : components) {
            // Check if this is an expanded dropdown
            if (child instanceof DropdownWidget) {
                DropdownWidget dropdown = (DropdownWidget) child;
                if (dropdown.isExpanded()) {
                    // Let the dropdown handle the click, and consume the event
                    dropdown.mouseClicked(mouseX, mouseY, button);
                    return true;
                }
            }

            // Check children of CollapsibleWidget
            if (child instanceof CollapsibleWidget) {
                CollapsibleWidget collapsible = (CollapsibleWidget) child;
                if (collapsible.isExpanded()) {
                    if (handleExpandedDropdownClick(collapsible.getChildWidgets(), mouseX, mouseY, button)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean hasExpandedDropdown(List<Component> components) {
        for (Component child : components) {
            if (child instanceof DropdownWidget) {
                DropdownWidget dropdown = (DropdownWidget) child;
                if (dropdown.isExpanded()) {
                    return true;
                }
            }

            // Check children of CollapsibleWidget
            if (child instanceof CollapsibleWidget) {
                CollapsibleWidget collapsible = (CollapsibleWidget) child;
                if (collapsible.isExpanded() && hasExpandedDropdown(collapsible.getChildWidgets())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean handleExpandedDropdownScroll(List<Component> components, double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        for (Component child : components) {
            // Check if this is an expanded dropdown
            if (child instanceof DropdownWidget) {
                DropdownWidget dropdown = (DropdownWidget) child;
                if (dropdown.isExpanded()) {
                    // Let the dropdown handle the scroll
                    if (dropdown.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
                        return true;
                    }
                }
            }

            // Check children of CollapsibleWidget
            if (child instanceof CollapsibleWidget) {
                CollapsibleWidget collapsible = (CollapsibleWidget) child;
                if (collapsible.isExpanded()) {
                    if (handleExpandedDropdownScroll(collapsible.getChildWidgets(), mouseX, mouseY, horizontalAmount, verticalAmount)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private void renderTooltips(DrawContext context, int mouseX, int mouseY) {
        // Check current category for hovered widgets with tooltips
        if (currentCategoryIndex >= 0 && currentCategoryIndex < categories.size()) {
            CategoryContainer category = categories.get(currentCategoryIndex);
            if (checkTooltipsRecursive(context, category.getChildren(), mouseX, mouseY)) {
                return;
            }
        }
        TooltipWidget.clear();
    }

    private boolean checkTooltipsRecursive(DrawContext context, List<Component> components, int mouseX, int mouseY) {
        for (Component child : components) {
            // Check if this component has a tooltip
            if (child.isHovered() && child instanceof TooltipProvider) {
                String tooltip = ((TooltipProvider) child).getTooltip();
                if (tooltip != null && !tooltip.isEmpty()) {
                    TooltipWidget.setTooltip(tooltip, mouseX, mouseY);
                    TooltipWidget.render(context, mouseX, mouseY);
                    return true;
                }
            }

            // Check children of CollapsibleWidget
            if (child instanceof CollapsibleWidget) {
                CollapsibleWidget collapsible = (CollapsibleWidget) child;
                if (collapsible.isExpanded() && checkTooltipsRecursive(context, collapsible.getChildWidgets(), mouseX, mouseY)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(Click click, boolean bl) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        // Popup takes priority - block ALL events when open
        if (activePopup != null) {
            activePopup.mouseClicked(mouseX, mouseY, button);
            return true; // Always consume when popup is open
        }

        // Check if clicking on an expanded dropdown - handle it and block events to widgets below
        if (currentCategoryIndex >= 0 && currentCategoryIndex < categories.size()) {
            if (handleExpandedDropdownClick(categories.get(currentCategoryIndex).getChildren(), mouseX, mouseY, button)) {
                return true;
            }
        }

        // Buttons
        if (doneButton.mouseClicked(mouseX, mouseY, button)) return true;
        if (editHudButton.mouseClicked(mouseX, mouseY, button)) return true;

        // Sidebar
        if (sidebar.mouseClicked(mouseX, mouseY, button)) return true;

        // Current category
        if (currentCategoryIndex >= 0 && currentCategoryIndex < categories.size()) {
            if (categories.get(currentCategoryIndex).mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }

        return super.mouseClicked(click, bl);
    }

    @Override
    public boolean mouseReleased(Click click) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        // Popup takes priority - block ALL events when open
        if (activePopup != null) {
            activePopup.mouseReleased(mouseX, mouseY, button);
            return true; // Always consume when popup is open
        }

        // Check if any dropdown is expanded - consume event to prevent clicking through
        if (currentCategoryIndex >= 0 && currentCategoryIndex < categories.size()) {
            if (hasExpandedDropdown(categories.get(currentCategoryIndex).getChildren())) {
                return true; // Consume event when dropdown is open
            }
        }

        if (doneButton.mouseReleased(mouseX, mouseY, button)) return true;
        if (editHudButton.mouseReleased(mouseX, mouseY, button)) return true;
        if (sidebar.mouseReleased(mouseX, mouseY, button)) return true;

        if (currentCategoryIndex >= 0 && currentCategoryIndex < categories.size()) {
            if (categories.get(currentCategoryIndex).mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }

        return super.mouseReleased(click);
    }

    @Override
    public boolean mouseDragged(Click click, double deltaX, double deltaY) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        // Popup takes priority - block ALL events when open
        if (activePopup != null) {
            activePopup.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
            return true; // Always consume when popup is open
        }

        // Check if any dropdown is expanded - consume event to prevent clicking through
        if (currentCategoryIndex >= 0 && currentCategoryIndex < categories.size()) {
            if (hasExpandedDropdown(categories.get(currentCategoryIndex).getChildren())) {
                return true; // Consume event when dropdown is open
            }
        }

        // Sidebar (for scrollbar dragging)
        if (sidebar.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
            return true;
        }

        if (currentCategoryIndex >= 0 && currentCategoryIndex < categories.size()) {
            if (categories.get(currentCategoryIndex).mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
                return true;
            }
        }

        return super.mouseDragged(click, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // Popup takes priority - block ALL events when open
        if (activePopup != null) {
            activePopup.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
            return true; // Always consume when popup is open
        }

        // Allow scrolling expanded dropdowns
        if (currentCategoryIndex >= 0 && currentCategoryIndex < categories.size()) {
            if (handleExpandedDropdownScroll(categories.get(currentCategoryIndex).getChildren(), mouseX, mouseY, horizontalAmount, verticalAmount)) {
                return true;
            }
        }

        // Sidebar (for mouse wheel scrolling)
        if (sidebar.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
            return true;
        }

        if (currentCategoryIndex >= 0 && currentCategoryIndex < categories.size()) {
            if (categories.get(currentCategoryIndex).mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
                return true;
            }
        }

        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean keyPressed(KeyInput keyInput) {
        int keyCode = keyInput.key();
        int scanCode = keyInput.scancode();
        int modifiers = keyInput.modifiers();

        // Popup takes priority - block ALL events when open
        if (activePopup != null) {
            activePopup.keyPressed(keyCode, scanCode, modifiers);
            return true; // Always consume when popup is open
        }

        if (currentCategoryIndex >= 0 && currentCategoryIndex < categories.size()) {
            if (categories.get(currentCategoryIndex).keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
        }

        return super.keyPressed(keyInput);
    }

    @Override
    public boolean charTyped(CharInput charInput) {
        char chr = (char) charInput.codepoint();
        int modifiers = charInput.modifiers();

        // Popup takes priority - block ALL events when open
        if (activePopup != null) {
            activePopup.charTyped(chr, modifiers);
            return true; // Always consume when popup is open
        }

        if (currentCategoryIndex >= 0 && currentCategoryIndex < categories.size()) {
            if (categories.get(currentCategoryIndex).charTyped(chr, modifiers)) {
                return true;
            }
        }

        return super.charTyped(charInput);
    }

    @Override
    public void close() {
        saveAndClose();
    }

    // ===== CATEGORY CREATION METHODS =====

    private CategoryContainer createCooldownHudCategory() {
        CategoryContainer category = new CategoryContainer("Cooldown HUD");

        category.addWidget(createToggle("Enabled", "cooldownHudEnabled", true, "Enable/disable the Cooldown HUD"));
        category.addWidget(createIntSlider("Scale", "cooldownHudScale", 100, 70, 150, "%", "HUD scale percentage"));

        // Title settings
        CollapsibleWidget titleGroup = createCollapsible("Title", "Configure HUD title display");
        titleGroup.addWidget(createToggle("Show Title", "showCooldownHudTitle", true, "Show the HUD title"));
        titleGroup.addWidget(createColorPicker("Title Color", "cooldownHudTitleColor", 14550187));
        category.addWidget(titleGroup);

        // Background styling
        CollapsibleWidget bgGroup = createCollapsible("Background", "Configure background styling");
        bgGroup.addWidget(createColorPicker("Color", "cooldownBgColor", 0x000000));
        bgGroup.addWidget(createIntSlider("Opacity", "cooldownBgOpacity", 128, 0, 255, "", "0 = transparent, 255 = opaque"));
        category.addWidget(bgGroup);

        // Border styling
        CollapsibleWidget borderGroup = createCollapsible("Border", "Configure border styling");
        borderGroup.addWidget(createColorPicker("Color", "cooldownBorderColor", 0xFFFFFF));
        borderGroup.addWidget(createIntSlider("Opacity", "cooldownBorderOpacity", 128, 0, 255, "", "0 = transparent, 255 = opaque"));
        borderGroup.addWidget(createIntSlider("Thickness", "cooldownBorderThickness", 2, 0, 5, "px", "Border thickness in pixels"));
        category.addWidget(borderGroup);

        // Individual command settings (grouped in a main collapsible)
        CollapsibleWidget cooldownsGroup = createCollapsible("Cooldowns", "Configure individual cooldown displays");

        CollapsibleWidget homeGroup = createCollapsible("/home", "Configure /home cooldown display");
        homeGroup.addWidget(createToggle("Enabled", "homeEnabled", true, "Show /home cooldown"));
        homeGroup.addWidget(createColorPicker("Color", "homeColor", 1045763));
        cooldownsGroup.addWidget(homeGroup);

        CollapsibleWidget jetGroup = createCollapsible("/jet", "Configure /jet cooldown display");
        jetGroup.addWidget(createToggle("Enabled", "jetEnabled", true, "Show /jet cooldown"));
        jetGroup.addWidget(createColorPicker("Color", "jetColor", 14576132));
        cooldownsGroup.addWidget(jetGroup);

        CollapsibleWidget feedGroup = createCollapsible("/feed", "Configure /feed cooldown display");
        feedGroup.addWidget(createToggle("Enabled", "feedEnabled", true, "Show /feed cooldown"));
        feedGroup.addWidget(createColorPicker("Color", "feedColor", 6700312));
        cooldownsGroup.addWidget(feedGroup);

        CollapsibleWidget fixGroup = createCollapsible("/fix", "Configure /fix cooldown display");
        fixGroup.addWidget(createToggle("Enabled", "fixEnabled", true, "Show /fix cooldown"));
        fixGroup.addWidget(createColorPicker("Color", "fixColor", 12632256));
        cooldownsGroup.addWidget(fixGroup);

        CollapsibleWidget combatGroup = createCollapsible("Combat Tag", "Configure combat tag display");
        combatGroup.addWidget(createToggle("Enabled", "combatEnabled", true, "Show combat tag"));
        combatGroup.addWidget(createColorPicker("Color", "combatColor", 9835026));
        cooldownsGroup.addWidget(combatGroup);

        CollapsibleWidget tpaGroup = createCollapsible("/tpa", "Configure /tpa cooldown display");
        tpaGroup.addWidget(createToggle("Enabled", "tpaEnabled", true, "Show /tpa cooldown"));
        tpaGroup.addWidget(createColorPicker("Color", "tpaColor", 5636095));
        cooldownsGroup.addWidget(tpaGroup);

        CollapsibleWidget tpahereGroup = createCollapsible("/tpahere", "Configure /tpahere cooldown display");
        tpahereGroup.addWidget(createToggle("Enabled", "tpahereEnabled", true, "Show /tpahere cooldown"));
        tpahereGroup.addWidget(createColorPicker("Color", "tpahereColor", 5636095));
        cooldownsGroup.addWidget(tpahereGroup);

        CollapsibleWidget dangleGroup = createCollapsible("/dangle", "Configure /dangle cooldown display");
        dangleGroup.addWidget(createToggle("Enabled", "dangleEnabled", true, "Show /dangle cooldown"));
        dangleGroup.addWidget(createColorPicker("Color", "dangleColor", 0xFFAA00));
        cooldownsGroup.addWidget(dangleGroup);

        CollapsibleWidget adangleGroup = createCollapsible("/adangle", "Configure /adangle cooldown display");
        adangleGroup.addWidget(createToggle("Enabled", "adangleEnabled", true, "Show /adangle cooldown"));
        adangleGroup.addWidget(createColorPicker("Color", "adangleColor", 0x55FFFF));
        cooldownsGroup.addWidget(adangleGroup);

        CollapsibleWidget nearGroup = createCollapsible("/near", "Configure /near cooldown display");
        nearGroup.addWidget(createToggle("Enabled", "nearEnabled", true, "Show /near cooldown"));
        nearGroup.addWidget(createColorPicker("Color", "nearColor", 0x55FFFF));
        cooldownsGroup.addWidget(nearGroup);

        CollapsibleWidget pulseGroup = createCollapsible("/pulse", "Configure /pulse cooldown display");
        pulseGroup.addWidget(createToggle("Enabled", "pulseEnabled", true, "Show /pulse cooldown"));
        pulseGroup.addWidget(createColorPicker("Color", "pulseColor", 0xFF5555));
        cooldownsGroup.addWidget(pulseGroup);

        category.addWidget(cooldownsGroup);

        return category;
    }

    private CategoryContainer createSatchelHudCategory() {
        CategoryContainer category = new CategoryContainer("Satchel HUD");

        category.addWidget(createToggle("Enabled", "satchelHudEnabled", true, "Enable/disable the Satchel HUD"));
        category.addWidget(createIntSlider("Scale", "satchelHudScale", 100, 70, 150, "%", "HUD scale percentage"));
        category.addWidget(createToggle("Show Percentage", "satchelShowPercentage", false, "Show percentage instead of count"));
        category.addWidget(createToggle("Combine Similar Satchels", "combineSimilarSatchels", true, "Group satchels of same type"));

        // Title settings
        CollapsibleWidget titleGroup = createCollapsible("Title", "Configure HUD title display");
        titleGroup.addWidget(createToggle("Show Title", "showSatchelHudTitle", true, "Show the HUD title"));
        titleGroup.addWidget(createColorPicker("Title Color", "satchelHudTitleColor", 11722244));
        category.addWidget(titleGroup);

        // Bar styling
        CollapsibleWidget barGroup = createCollapsible("Bar", "Configure bar styling");
        barGroup.addWidget(createColorPicker("Bar Color", "satchelBarColor", 0xFF4488));
        category.addWidget(barGroup);

        // Background styling
        CollapsibleWidget bgGroup = createCollapsible("Background", "Configure background styling");
        bgGroup.addWidget(createColorPicker("Color", "satchelBgColor", 0x000000));
        bgGroup.addWidget(createIntSlider("Opacity", "satchelBgOpacity", 128, 0, 255, "", "0 = transparent, 255 = opaque"));
        category.addWidget(bgGroup);

        // Border styling
        CollapsibleWidget borderGroup = createCollapsible("Border", "Configure border styling");
        borderGroup.addWidget(createColorPicker("Color", "satchelBorderColor", 0xFFFFFF));
        borderGroup.addWidget(createIntSlider("Opacity", "satchelBorderOpacity", 128, 0, 255, "", "0 = transparent, 255 = opaque"));
        borderGroup.addWidget(createIntSlider("Thickness", "satchelBorderThickness", 2, 0, 5, "px", "Border thickness in pixels"));
        category.addWidget(borderGroup);

        // Capacity threshold colors
        CollapsibleWidget thresholdColors = createCollapsible("Capacity Threshold Colors", "Configure colors based on satchel fullness");
        thresholdColors.addWidget(createColorPicker("Color (<20%)", "satchelColorUnder20", 1045763));
        thresholdColors.addWidget(createColorPicker("Color (20-60%)", "satchelColor20to60", 16776960));
        thresholdColors.addWidget(createColorPicker("Color (60-95%)", "satchelColor60to95", 16746496));
        thresholdColors.addWidget(createColorPicker("Color (95%+)", "satchelColor95Plus", 11141120));
        category.addWidget(thresholdColors);

        return category;
    }

    private CategoryContainer createStatsHudCategory() {
        CategoryContainer category = new CategoryContainer("Stats HUD");

        category.addWidget(createToggle("Enabled", "statsHudEnabled", true, "Enable/disable the Stats HUD"));
        category.addWidget(createIntSlider("Scale", "statsHudScale", 100, 70, 150, "%", "HUD scale percentage"));
        category.addWidget(createToggle("Use Comma Formatting", "useCommaFormatting", false, "Format numbers with commas"));

        // Title settings
        CollapsibleWidget titleGroup = createCollapsible("Title", "Configure HUD title display");
        titleGroup.addWidget(createToggle("Show Title", "showStatsHudTitle", true, "Show the HUD title"));
        titleGroup.addWidget(createColorPicker("Title Color", "statsHudTitleColor", 14352636));
        category.addWidget(titleGroup);

        // Background styling
        CollapsibleWidget bgGroup = createCollapsible("Background", "Configure background styling");
        bgGroup.addWidget(createColorPicker("Color", "statsBgColor", 0x000000));
        bgGroup.addWidget(createIntSlider("Opacity", "statsBgOpacity", 128, 0, 255, "", "0 = transparent, 255 = opaque"));
        category.addWidget(bgGroup);

        // Border styling
        CollapsibleWidget borderGroup = createCollapsible("Border", "Configure border styling");
        borderGroup.addWidget(createColorPicker("Color", "statsBorderColor", 0xFFFFFF));
        borderGroup.addWidget(createIntSlider("Opacity", "statsBorderOpacity", 128, 0, 255, "", "0 = transparent, 255 = opaque"));
        borderGroup.addWidget(createIntSlider("Thickness", "statsBorderThickness", 2, 0, 5, "px", "Border thickness in pixels"));
        category.addWidget(borderGroup);

        // XP Display settings
        CollapsibleWidget xpGroup = createCollapsible("XP Display", "Configure XP-related display options");

        CollapsibleWidget currentXpGroup = createCollapsible("Current XP", "Configure current XP display");
        currentXpGroup.addWidget(createToggle("Enabled", "statsShowCurrentXP", true, "Display current XP"));
        currentXpGroup.addWidget(createColorPicker("Color", "statsCurrentXPColor", 1045763));
        xpGroup.addWidget(currentXpGroup);

        CollapsibleWidget xpHourGroup = createCollapsible("XP/Hour", "Configure XP per hour display");
        xpHourGroup.addWidget(createToggle("Enabled", "statsShowXPPerHour", true, "Display XP per hour"));
        xpHourGroup.addWidget(createColorPicker("Color", "statsXPPerHourColor", 1045763));
        xpGroup.addWidget(xpHourGroup);

        CollapsibleWidget xpMinGroup = createCollapsible("XP/Min", "Configure XP per minute display");
        xpMinGroup.addWidget(createToggle("Enabled", "statsShowXPPerMinute", true, "Display XP per minute"));
        xpMinGroup.addWidget(createColorPicker("Color", "statsXPPerMinuteColor", 1045763));
        xpGroup.addWidget(xpMinGroup);

        CollapsibleWidget sessionXpGroup = createCollapsible("Session XP", "Configure session XP display");
        sessionXpGroup.addWidget(createToggle("Enabled", "statsShowSessionXP", true, "Display session XP gain"));
        sessionXpGroup.addWidget(createColorPicker("Color", "statsSessionXPColor", 1045763));
        xpGroup.addWidget(sessionXpGroup);

        category.addWidget(xpGroup);

        // CE Display settings
        CollapsibleWidget ceGroup = createCollapsible("CE Display", "Configure CE-related display options");

        CollapsibleWidget currentCeGroup = createCollapsible("Current CE", "Configure current CE display");
        currentCeGroup.addWidget(createToggle("Enabled", "statsShowCurrentCE", true, "Display current CE"));
        currentCeGroup.addWidget(createColorPicker("Color", "statsCurrentCEColor", 240124));
        ceGroup.addWidget(currentCeGroup);

        CollapsibleWidget ceHourGroup = createCollapsible("CE/Hour", "Configure CE per hour display");
        ceHourGroup.addWidget(createToggle("Enabled", "statsShowCEPerHour", true, "Display CE per hour"));
        ceHourGroup.addWidget(createColorPicker("Color", "statsCEPerHourColor", 240124));
        ceGroup.addWidget(ceHourGroup);

        CollapsibleWidget ceMinGroup = createCollapsible("CE/Min", "Configure CE per minute display");
        ceMinGroup.addWidget(createToggle("Enabled", "statsShowCEPerMinute", true, "Display CE per minute"));
        ceMinGroup.addWidget(createColorPicker("Color", "statsCEPerMinuteColor", 240124));
        ceGroup.addWidget(ceMinGroup);

        CollapsibleWidget sessionCeGroup = createCollapsible("Session CE", "Configure session CE display");
        sessionCeGroup.addWidget(createToggle("Enabled", "statsShowSessionCE", true, "Display session CE gain"));
        sessionCeGroup.addWidget(createColorPicker("Color", "statsSessionCEColor", 240124));
        ceGroup.addWidget(sessionCeGroup);

        category.addWidget(ceGroup);

        // Session Info settings
        CollapsibleWidget sessionGroup = createCollapsible("Session Info", "Configure session information display");
        sessionGroup.addWidget(createToggle("Show Session Duration", "statsShowSessionDuration", true, "Display session duration"));
        sessionGroup.addWidget(createToggle("Show Millis on Duration", "statsShowMillisOnSessionDuration", false, "Show milliseconds"));
        sessionGroup.addWidget(createColorPicker("Session Duration Color", "statsSessionDurationColor", 14352636));
        sessionGroup.addWidget(createToggle("Show Time Till Level Up", "statsShowTimeTillLevelUp", true, "Show time until next level"));
        sessionGroup.addWidget(createColorPicker("Time Till Level Up Color", "statsTimeTillLevelUpColor", 0xFFD700));
        category.addWidget(sessionGroup);

        return category;
    }

    private CategoryContainer createEnchantHudCategory() {
        CategoryContainer category = new CategoryContainer("Enchant HUD");

        category.addWidget(createToggle("Enabled", "enchantHudEnabled", true, "Enable/disable the Enchant HUD"));
        category.addWidget(createIntSlider("Scale", "enchantHudScale", 100, 70, 150, "%", "HUD scale percentage"));
        category.addWidget(createColorPicker("Time Color", "enchantTimeColor", 1045763));

        // Title settings
        CollapsibleWidget titleGroup = createCollapsible("Title", "Configure HUD title display");
        titleGroup.addWidget(createToggle("Show Title", "showEnchantHudTitle", true, "Show the HUD title"));
        titleGroup.addWidget(createColorPicker("Title Color", "enchantHudTitleColor", 300510));
        category.addWidget(titleGroup);

        // Background styling
        CollapsibleWidget bgGroup = createCollapsible("Background", "Configure background styling");
        bgGroup.addWidget(createColorPicker("Color", "enchantBgColor", 0x000000));
        bgGroup.addWidget(createIntSlider("Opacity", "enchantBgOpacity", 128, 0, 255, "", "0 = transparent, 255 = opaque"));
        category.addWidget(bgGroup);

        // Border styling
        CollapsibleWidget borderGroup = createCollapsible("Border", "Configure border styling");
        borderGroup.addWidget(createColorPicker("Color", "enchantBorderColor", 0xFFFFFF));
        borderGroup.addWidget(createIntSlider("Opacity", "enchantBorderOpacity", 128, 0, 255, "", "0 = transparent, 255 = opaque"));
        borderGroup.addWidget(createIntSlider("Thickness", "enchantBorderThickness", 2, 0, 5, "px", "Border thickness in pixels"));
        category.addWidget(borderGroup);

        return category;
    }

    private CategoryContainer createEventsHudCategory() {
        CategoryContainer category = new CategoryContainer("Events HUD");

        category.addWidget(createToggle("Enabled", "eventsHudEnabled", true, "Enable/disable the Events HUD"));
        category.addWidget(createIntSlider("Scale", "eventsHudScale", 100, 70, 150, "%", "HUD scale percentage"));
        category.addWidget(createColorPicker("Text Color", "eventsTextColor", 14558468));

        // Meteor settings
        CollapsibleWidget meteorGroup = createCollapsible("Meteors", "Configure meteor display on the Events HUD");
        meteorGroup.addWidget(createColorPicker("Natural Heading Color", "eventsNaturalHeadingColor", 0x00FF00));
        meteorGroup.addWidget(createColorPicker("Summoned Heading Color", "eventsSummonedHeadingColor", 0xFF4500));
        meteorGroup.addWidget(createTextInput("Icon Item ID", "eventsIconItemId", "nether_quartz_ore", "Item ID for meteor icon (minecraft: prefix added automatically)"));
        meteorGroup.addWidget(createIntSlider("Crashed Display Duration", "eventsCrashedDisplayDuration", 15, 5, 60, "s", "Seconds to show crashed meteor"));
        meteorGroup.addWidget(createToggle("Show Distance", "meteorShowDistance", true, "Show distance to meteor in the Events HUD"));
        meteorGroup.addWidget(createIntSlider("Default Beam Opacity", "meteorBeamOpacity", 160, 0, 255, "", "Default beacon beam opacity for newly detected meteors (0=transparent, 255=opaque)"));
        category.addWidget(meteorGroup);

        // Title settings
        CollapsibleWidget titleGroup = createCollapsible("Title", "Configure HUD title display");
        titleGroup.addWidget(createToggle("Show Title", "showEventsHudTitle", true, "Show the HUD title"));
        titleGroup.addWidget(createColorPicker("Title Color", "eventsHudTitleColor", 14558468));
        category.addWidget(titleGroup);

        // Background styling
        CollapsibleWidget bgGroup = createCollapsible("Background", "Configure background styling");
        bgGroup.addWidget(createColorPicker("Color", "eventsBgColor", 0x000000));
        bgGroup.addWidget(createIntSlider("Opacity", "eventsBgOpacity", 128, 0, 255, "", "0 = transparent, 255 = opaque"));
        category.addWidget(bgGroup);

        // Border styling
        CollapsibleWidget borderGroup = createCollapsible("Border", "Configure border styling");
        borderGroup.addWidget(createColorPicker("Color", "eventsBorderColor", 0xFFFFFF));
        borderGroup.addWidget(createIntSlider("Opacity", "eventsBorderOpacity", 128, 0, 255, "", "0 = transparent, 255 = opaque"));
        borderGroup.addWidget(createIntSlider("Thickness", "eventsBorderThickness", 2, 0, 5, "px", "Border thickness in pixels"));
        category.addWidget(borderGroup);

        // Merchant settings
        CollapsibleWidget merchantGroup = createCollapsible("Merchants", "Configure merchant display on the Events HUD");
        merchantGroup.addWidget(createToggle("Enabled", "merchantsEnabled", true, "Show/hide all merchants on the Events HUD"));
        merchantGroup.addWidget(createIntSlider("Timeout", "merchantTimeoutMinutes", 20, 1, 60, "min", "Minutes before a merchant entry is automatically removed"));
        merchantGroup.addWidget(createIntSlider("Slain Display Duration", "merchantSlainDisplayDuration", 10, 1, 60, "s", "Seconds to keep a slain merchant visible before removing it"));
        merchantGroup.addWidget(createToggle("Show Distance", "merchantShowDistance", true, "Show distance to merchant in the Events HUD"));
        merchantGroup.addWidget(createIntSlider("Default Beam Opacity", "merchantBeamOpacity", 160, 0, 255, "", "Default beacon beam opacity for newly detected merchants (0=transparent, 255=opaque)"));

        // Tier toggles
        CollapsibleWidget tierToggles = createCollapsible("Tier Toggles", "Show or hide individual merchant tiers");
        tierToggles.addWidget(createToggle("Coal", "coalMerchantEnabled", true, "Show Coal Ore Merchants"));
        tierToggles.addWidget(createToggle("Iron", "ironMerchantEnabled", true, "Show Iron Ore Merchants"));
        tierToggles.addWidget(createToggle("Lapis", "lapisMerchantEnabled", true, "Show Lapis Ore Merchants"));
        tierToggles.addWidget(createToggle("Redstone", "redstoneMerchantEnabled", true, "Show Redstone Ore Merchants"));
        tierToggles.addWidget(createToggle("Gold", "goldMerchantEnabled", true, "Show Gold Ore Merchants"));
        tierToggles.addWidget(createToggle("Diamond", "diamondMerchantEnabled", true, "Show Diamond Ore Merchants"));
        tierToggles.addWidget(createToggle("Emerald", "emeraldMerchantEnabled", true, "Show Emerald Ore Merchants"));
        merchantGroup.addWidget(tierToggles);

        // Tier heading colors
        CollapsibleWidget tierColors = createCollapsible("Tier Colors", "Heading colors for each merchant tier");
        tierColors.addWidget(createColorPicker("Coal", "coalMerchantHeadingColor", 0x555555));
        tierColors.addWidget(createColorPicker("Iron", "ironMerchantHeadingColor", 0xAAAAAA));
        tierColors.addWidget(createColorPicker("Lapis", "lapisMerchantHeadingColor", 0x5555FF));
        tierColors.addWidget(createColorPicker("Redstone", "redstoneMerchantHeadingColor", 0xFF5555));
        tierColors.addWidget(createColorPicker("Gold", "goldMerchantHeadingColor", 0xFFAA00));
        tierColors.addWidget(createColorPicker("Diamond", "diamondMerchantHeadingColor", 0x55FFFF));
        tierColors.addWidget(createColorPicker("Emerald", "emeraldMerchantHeadingColor", 0x55FF55));
        merchantGroup.addWidget(tierColors);

        category.addWidget(merchantGroup);

        // Bandit Rush settings
        CollapsibleWidget banditRushGroup = createCollapsible("Bandit Rushes", "Configure bandit rush display on the Events HUD");
        banditRushGroup.addWidget(createToggle("Enabled", "banditRushEnabled", true, "Show bandit rush events (only in your badlands sub-world)"));
        banditRushGroup.addWidget(createIntSlider("Timeout", "banditRushTimeoutSeconds", 60, 10, 300, "s", "How long a bandit rush stays on the HUD before being removed"));
        banditRushGroup.addWidget(createColorPicker("Heading Color", "banditRushHeadingColor", 0xFFAA00));
        banditRushGroup.addWidget(createColorPicker("Text Color", "banditRushTextColor", 0xFFAAAA));
        banditRushGroup.addWidget(createTextInput("Icon Item ID", "banditRushIconItemId", "iron_sword", "Item ID for bandit rush icon (minecraft: prefix added automatically)"));
        banditRushGroup.addWidget(createToggle("Show Distance", "banditRushShowDistance", true, "Show distance to bandit rush in the Events HUD"));
        banditRushGroup.addWidget(createIntSlider("Beam Opacity", "banditRushBeamOpacity", 160, 0, 255, "", "Beacon beam opacity for bandit rushes (0=transparent, 255=opaque)"));
        banditRushGroup.addWidget(createToggle("Sound Notification", "banditRushSoundEnabled", true, "Play a sound when a bandit rush spawns in your sub-world"));
        banditRushGroup.addWidget(createDropdown("Sound", "banditRushSound",
            Arrays.asList("anvil", "bell", "xp_orb", "note_pling", "enchant", "level_up", "ender_eye"), "note_pling", "Notification sound"));
        banditRushGroup.addWidget(createIntSlider("Sound Volume", "banditRushSoundVolume", 100, 0, 200, "%", "Volume for bandit rush notification sound"));
        category.addWidget(banditRushGroup);

        // Meteorite Shower settings
        CollapsibleWidget showerGroup = createCollapsible("Meteorite Showers", "Configure meteorite shower display on the Events HUD");
        showerGroup.addWidget(createToggle("Enabled", "meteoriteShowerEnabled", true, "Show meteorite shower events"));
        showerGroup.addWidget(createIntSlider("Mineable Timeout", "meteoriteShowerTimeoutSeconds", 180, 10, 600, "s", "How long a crashed shower stays on the HUD before being removed"));
        showerGroup.addWidget(createColorPicker("Heading Color", "meteoriteShowerHeadingColor", 0xFF5500));
        showerGroup.addWidget(createColorPicker("Text Color", "meteoriteShowerTextColor", 0xFFAA88));
        showerGroup.addWidget(createTextInput("Icon Item ID", "meteoriteShowerIconItemId", "magma_block", "Item ID for meteorite shower icon (minecraft: prefix added automatically)"));
        showerGroup.addWidget(createToggle("Show Distance", "meteoriteShowerShowDistance", true, "Show distance to shower in the Events HUD"));
        showerGroup.addWidget(createIntSlider("Beam Opacity", "meteoriteShowerBeamOpacity", 160, 0, 255, "", "Beacon beam opacity for showers (0=transparent, 255=opaque)"));
        showerGroup.addWidget(createToggle("Sound Notification", "meteoriteShowerSoundEnabled", true, "Play a sound when a meteorite shower is announced"));
        showerGroup.addWidget(createDropdown("Sound", "meteoriteShowerSound",
            Arrays.asList("anvil", "bell", "xp_orb", "note_pling", "enchant", "level_up", "ender_eye"), "ender_eye", "Notification sound"));
        showerGroup.addWidget(createIntSlider("Sound Volume", "meteoriteShowerSoundVolume", 100, 0, 200, "%", "Volume for meteorite shower notification sound"));
        category.addWidget(showerGroup);

        return category;
    }

    private CategoryContainer createWaypointsCategory() {
        CategoryContainer category = new CategoryContainer("Waypoints");

        category.addWidget(createToggle("Enabled", "waypointsEnabled", true, "Master toggle for all waypoint overlays"));
        category.addWidget(new ButtonComponent("Open Waypoints", 0, 0, 160, 16,
            () -> { if (client != null) client.setScreen(new WaypointsScreen()); }));

        CollapsibleWidget screenGroup = createCollapsible("Screen Indicators", "2D screen-edge direction markers");
        screenGroup.addWidget(createToggle("Show Meteors", "waypointMeteorsEnabled", true, "Show meteor waypoint indicators"));
        screenGroup.addWidget(createToggle("Meteors: Edge Indicators", "waypointMeteorsEdgeEnabled", true, "Show meteor indicators at screen edge when off-screen"));
        screenGroup.addWidget(createToggle("Show Merchants", "waypointMerchantsEnabled", true, "Show merchant waypoint indicators"));
        screenGroup.addWidget(createToggle("Merchants: Edge Indicators", "waypointMerchantsEdgeEnabled", true, "Show merchant indicators at screen edge when off-screen"));
        screenGroup.addWidget(createToggle("Custom: Edge Indicators", "waypointCustomEdgeEnabled", false, "Show custom waypoint indicators at screen edge when off-screen"));
        screenGroup.addWidget(createToggle("Show Bandit Rushes", "waypointBanditRushEnabled", true, "Show bandit rush waypoint indicators (badlands only)"));
        screenGroup.addWidget(createToggle("Bandit Rushes: Edge Indicators", "waypointBanditRushEdgeEnabled", true, "Show bandit rush indicators at screen edge when off-screen"));
        screenGroup.addWidget(createToggle("Show Meteorite Showers", "waypointMeteoriteShowerEnabled", true, "Show meteorite shower waypoint indicators"));
        screenGroup.addWidget(createToggle("Meteorite Showers: Edge Indicators", "waypointMeteoriteShowerEdgeEnabled", true, "Show shower indicators at screen edge when off-screen"));
        category.addWidget(screenGroup);

        CollapsibleWidget beamGroup = createCollapsible("Beacon Beams", "3D vertical beam pillars in world space");
        beamGroup.addWidget(createToggle("Enabled", "beaconBeamsEnabled", true, "Show beacon beam pillars at event locations"));
        beamGroup.addWidget(createToggle("Visible Through Blocks", "beaconBeamThroughWalls", false, "Beam renders through walls (always visible)"));
        category.addWidget(beamGroup);

        CollapsibleWidget customGroup = createCollapsible("Custom Waypoints", "Player-defined named waypoints");
        customGroup.addWidget(createToggle("Enabled", "waypointCustomEnabled", true, "Show custom waypoints in overlay and as beacon beams"));
        customGroup.addWidget(new ButtonComponent("Manage Waypoints", 0, 0, 160, 16,
            () -> { if (client != null) client.setScreen(new WaypointsScreen()); }));

        CollapsibleWidget customDefaultsGroup = createCollapsible("New Waypoint Defaults", "Default values pre-filled when creating a new custom waypoint");
        customDefaultsGroup.addWidget(createIntSlider("Default Opacity", "customWaypointDefaultOpacity", 255, 0, 255, "", "Default beacon beam opacity for new waypoints (0=transparent, 255=opaque)"));
        customDefaultsGroup.addWidget(createFloatSlider("On-Screen Scale", "customWaypointOnScreenScale", 1.0f, 0.25f, 4.0f, "x", "Default icon scale when waypoint is visible on screen"));
        customDefaultsGroup.addWidget(createFloatSlider("Off-Screen Scale", "customWaypointOffScreenScale", 1.0f, 0.25f, 4.0f, "x", "Default icon scale for edge indicator when waypoint is off-screen"));
        customGroup.addWidget(customDefaultsGroup);

        category.addWidget(customGroup);

        return category;
    }

    private CategoryContainer createGangPingsCategory() {
        CategoryContainer category = new CategoryContainer("Gang Pings");

        category.addWidget(createToggle("Gang Pings Enabled", "gangPingEnabled", true, "Enable gang ping sending and receiving"));
        category.addWidget(createColorPicker("Gang Ping Color", "gangPingColor", 0xAA55FF));
        category.addWidget(createToggle("Truce Pings Enabled", "trucePingEnabled", true, "Enable truce ping sending and receiving"));
        category.addWidget(createColorPicker("Truce Ping Color", "trucePingColor", 0x55AAFF));
        category.addWidget(createToggle("Show Pings Not From Your Gang/Truce", "gangPingShowNonGang", false, "Accept pings from any chat, not just [GC] and [TC]"));

        CollapsibleWidget iconGroup = createCollapsible("Icon Settings", "Waypoint icon appearance and behavior (shared)");
        iconGroup.addWidget(createIntSlider("Icon Opacity", "gangPingBaseOpacity", 200, 0, 255, "", "Base icon opacity before distance fade (0=transparent, 255=opaque)"));
        iconGroup.addWidget(createToggle("Edge Indicators", "gangPingEdgeEnabled", true, "Show gang ping arrows at screen edge when off-screen"));
        iconGroup.addWidget(createToggle("Distance Scaling", "gangPingDistanceScaling", true, "Scale icon size based on distance (off = always use min scale)"));
        iconGroup.addWidget(createFloatSlider("Min Icon Scale", "gangPingIconMinScale", 0.5f, 0.1f, 3.0f, "x", "Minimum icon scale (used at close range or when distance scaling is off)"));
        iconGroup.addWidget(createFloatSlider("Max Icon Scale", "gangPingIconMaxScale", 1.5f, 0.1f, 3.0f, "x", "Maximum icon scale (reached at 75+ blocks)"));
        category.addWidget(iconGroup);

        CollapsibleWidget beamGroup = createCollapsible("Beacon Beams", "3D vertical beam pillars at gang ping locations");
        beamGroup.addWidget(createToggle("Enabled", "gangPingBeamEnabled", true, "Show beacon beam pillars at gang ping locations"));
        beamGroup.addWidget(createIntSlider("Beam Opacity", "gangPingBeamOpacity", 120, 0, 255, "", "Beacon beam opacity for gang pings"));
        category.addWidget(beamGroup);

        CollapsibleWidget soundGroup = createCollapsible("Sound", "Notification sound settings");
        soundGroup.addWidget(createToggle("Enabled", "gangPingSoundEnabled", true, "Play a notification sound when a gang ping is received"));
        soundGroup.addWidget(createIntSlider("Volume", "gangPingSoundVolume", 80, 0, 200, "%", "Volume for gang ping notification sound"));
        category.addWidget(soundGroup);

        CollapsibleWidget textGroup = createCollapsible("Text Display", "Configure which info lines show on the waypoint");
        textGroup.addWidget(createToggle("Show Name", "gangPingShowName", true, "Show player name on gang ping waypoint"));
        textGroup.addWidget(createToggle("Show Timer", "gangPingShowTimer", false, "Show time since ping below waypoint"));
        textGroup.addWidget(createToggle("Show Coords", "gangPingShowCoords", true, "Show coordinates and distance on waypoint"));
        textGroup.addWidget(createToggle("Show HP", "gangPingShowHp", false, "Show player health on waypoint"));
        textGroup.addWidget(createToggle("Show Facing", "gangPingShowFacing", false, "Show player facing direction on waypoint"));
        textGroup.addWidget(createFloatSlider("Text Scale", "gangPingTextScale", 1.0f, 0.5f, 2.0f, "x", "Scale of text labels on ping waypoints"));
        category.addWidget(textGroup);

        return category;
    }

    private CategoryContainer createSuperBreakerCategory() {
        CategoryContainer category = new CategoryContainer("Super Breaker");

        category.addWidget(createToggle("Aura Enabled", "superBreakerAuraEnabled", true, "Enable/disable visual aura"));
        category.addWidget(createIntSlider("Aura Scale", "superBreakerAuraScale", 100, 70, 150, "%", "Aura scale percentage"));
        category.addWidget(createColorPicker("Base Color", "superBreakerBaseColor", 16386570));
        category.addWidget(createIntSlider("Base Opacity", "superBreakerBaseOpacity", 79, 0, 255, "", "0 = transparent, 255 = opaque"));
        category.addWidget(createColorPicker("Light Color", "superBreakerLightColor", 1444602));
        category.addWidget(createIntSlider("Light Opacity", "superBreakerLightOpacity", 191, 0, 255, "", "0 = transparent, 255 = opaque"));
        category.addWidget(createToggle("Timer Enabled", "superBreakerTimerEnabled", true, "Show countdown timer on aura"));
        category.addWidget(createIntSlider("Timer Offset X", "superBreakerTimerOffsetX", 0, -100, 100, "px", "Horizontal offset from center"));
        category.addWidget(createIntSlider("Timer Offset Y", "superBreakerTimerOffsetY", -20, -100, 100, "px", "Vertical offset from center"));

        return category;
    }

    private CategoryContainer createPeacefulMiningCategory() {
        CategoryContainer category = new CategoryContainer("Peaceful Mining");

        category.addWidget(createToggle("Enabled", "peacefulMiningEnabled", true, "Enable/disable peaceful mining feature"));
        category.addWidget(createIntSlider("Opacity", "peacefulMiningOpacity", 50, 0, 255, "", "0 = transparent, 255 = opaque"));
        category.addWidget(createIntSlider("Distance", "peacefulMiningDistance", 8, 4, 16, " blocks", "Distance to hide entities"));
        category.addWidget(createToggle("Disable On Combat", "peacefulMiningDisableOnCombat", false, "Disable when in combat"));
        category.addWidget(createToggle("Triggered By Pickaxes", "peacefulMiningPickaxe", true, "Activate peaceful mining while holding a pickaxe"));
        category.addWidget(createToggle("Triggered By Maces", "peacefulMiningMace", true, "Activate peaceful mining while holding a mace"));
        category.addWidget(createToggle("Always On In PrisonBreak", "peacefulMiningAlwaysInPrisonbreak", true, "Always enable peaceful mining in the PrisonBreak world, regardless of held item"));

        return category;
    }


    private CategoryContainer createEasyViewCategory() {
        CategoryContainer category = new CategoryContainer("EasyView");

        category.addWidget(createToggle("Enabled", "easyViewEnabled", true, "Enable/disable EasyView feature"));

        // Individual element settings (grouped in collapsibles)
        CollapsibleWidget energyGroup = createCollapsible("Energy", "Configure energy display");
        energyGroup.addWidget(createToggle("Enabled", "easyViewEnergyEnabled", true, "Show energy info"));
        energyGroup.addWidget(createColorPicker("Color", "easyViewEnergyColor", 0xFFFFFF));
        energyGroup.addWidget(createToggle("Bold", "easyViewEnergyBold", true, "Bold energy text"));
        category.addWidget(energyGroup);

        CollapsibleWidget moneyGroup = createCollapsible("Money", "Configure money display");
        moneyGroup.addWidget(createToggle("Enabled", "easyViewMoneyEnabled", true, "Show money info"));
        moneyGroup.addWidget(createColorPicker("Color", "easyViewMoneyColor", 0x00FF00));
        moneyGroup.addWidget(createToggle("Bold", "easyViewMoneyBold", true, "Bold money text"));
        category.addWidget(moneyGroup);

        CollapsibleWidget gangPointsGroup = createCollapsible("Gang Points", "Configure gang points display");
        gangPointsGroup.addWidget(createToggle("Enabled", "easyViewGangPointsEnabled", true, "Show gang points"));
        gangPointsGroup.addWidget(createColorPicker("Color", "easyViewGangPointsColor", 65535));
        gangPointsGroup.addWidget(createToggle("Bold", "easyViewGangPointsBold", true, "Bold gang points text"));
        category.addWidget(gangPointsGroup);

        CollapsibleWidget blackScrollGroup = createCollapsible("Black Scroll", "Configure black scroll display");
        blackScrollGroup.addWidget(createToggle("Enabled", "easyViewBlackScrollEnabled", true, "Show black scroll info"));
        blackScrollGroup.addWidget(createColorPicker("Color", "easyViewBlackScrollColor", 0xFF00FF));
        blackScrollGroup.addWidget(createToggle("Bold", "easyViewBlackScrollBold", true, "Bold black scroll text"));
        category.addWidget(blackScrollGroup);

        CollapsibleWidget chargeOrbGroup = createCollapsible("Charge Orb", "Configure charge orb display");
        chargeOrbGroup.addWidget(createToggle("Enabled", "easyViewChargeOrbEnabled", true, "Show charge orb info"));
        chargeOrbGroup.addWidget(createColorPicker("Color", "easyViewChargeOrbColor", 16755200));
        chargeOrbGroup.addWidget(createToggle("Bold", "easyViewChargeOrbBold", true, "Bold charge orb text"));
        category.addWidget(chargeOrbGroup);

        // Item display settings
        CollapsibleWidget armorGroup = createCollapsible("Armor", "Configure armor display");
        armorGroup.addWidget(createToggle("Enabled", "easyViewArmorEnabled", true, "Show armor stats"));
        armorGroup.addWidget(createColorPicker("Color", "easyViewArmorColor", 0x00FF00));
        armorGroup.addWidget(createIntSlider("Scale", "easyViewArmorScale", 70, 50, 100, "%", "Armor text scale"));
        armorGroup.addWidget(createToggle("Bold", "easyViewArmorBold", true, "Bold armor text"));
        category.addWidget(armorGroup);

        CollapsibleWidget weaponsGroup = createCollapsible("Weapons", "Configure weapons display");
        weaponsGroup.addWidget(createToggle("Enabled", "easyViewWeaponsEnabled", true, "Show weapon stats"));
        weaponsGroup.addWidget(createColorPicker("Color", "easyViewWeaponsColor", 0x00FF00));
        weaponsGroup.addWidget(createIntSlider("Scale", "easyViewWeaponsScale", 70, 50, 100, "%", "Weapons text scale"));
        weaponsGroup.addWidget(createToggle("Bold", "easyViewWeaponsBold", true, "Bold weapons text"));
        category.addWidget(weaponsGroup);

        CollapsibleWidget pickaxesGroup = createCollapsible("Pickaxes", "Configure pickaxes display");
        pickaxesGroup.addWidget(createToggle("Enabled", "easyViewPickaxesEnabled", true, "Show pickaxe stats"));
        pickaxesGroup.addWidget(createColorPicker("Color", "easyViewPickaxesColor", 0x00FF00));
        pickaxesGroup.addWidget(createIntSlider("Scale", "easyViewPickaxesScale", 70, 50, 100, "%", "Pickaxes text scale"));
        pickaxesGroup.addWidget(createToggle("Bold", "easyViewPickaxesBold", true, "Bold pickaxes text"));
        category.addWidget(pickaxesGroup);

        CollapsibleWidget dustGroup = createCollapsible("Dust", "Configure dust display");
        dustGroup.addWidget(createToggle("Enabled", "easyViewDustEnabled", true, "Show dust info"));
        dustGroup.addWidget(createColorPicker("Color", "easyViewDustColor", 0xD2691E));
        dustGroup.addWidget(createToggle("Bold", "easyViewDustBold", true, "Bold dust text"));
        category.addWidget(dustGroup);

        CollapsibleWidget pagesGroup = createCollapsible("Pages", "Configure pages display");
        pagesGroup.addWidget(createToggle("Enabled", "easyViewPagesEnabled", true, "Show pages info"));
        pagesGroup.addWidget(createColorPicker("Color", "easyViewPagesColor", 0xF5DEB3));
        pagesGroup.addWidget(createToggle("Bold", "easyViewPagesBold", true, "Bold pages text"));
        pagesGroup.addWidget(createToggle("Tier Color", "easyViewPagesTierColor", false, "Use tier color from page name instead of custom color"));
        category.addWidget(pagesGroup);

        CollapsibleWidget prestigeTokenGroup = createCollapsible("Prestige Tokens", "Configure prestige token display");
        prestigeTokenGroup.addWidget(createToggle("Enabled", "easyViewPrestigeTokenEnabled", true, "Show prestige token level"));
        prestigeTokenGroup.addWidget(createColorPicker("Color", "easyViewPrestigeTokenColor", 0xFFD700));
        prestigeTokenGroup.addWidget(createToggle("Bold", "easyViewPrestigeTokenBold", true, "Bold prestige token text"));
        category.addWidget(prestigeTokenGroup);

        CollapsibleWidget xpBottleGroup = createCollapsible("XP Bottles", "Configure XP bottle display");
        xpBottleGroup.addWidget(createToggle("Enabled", "easyViewXpBottleEnabled", true, "Show XP bottle amount"));
        xpBottleGroup.addWidget(createColorPicker("Color", "easyViewXpBottleColor", 0xFFFFFF));
        xpBottleGroup.addWidget(createToggle("Bold", "easyViewXpBottleBold", true, "Bold XP bottle text"));
        xpBottleGroup.addWidget(createToggle("Tier Color", "easyViewXpBottleTierColor", true, "Use tier color from bottle name instead of custom color"));
        category.addWidget(xpBottleGroup);

        return category;
    }

    private CategoryContainer createItemCooldownsCategory() {
        CategoryContainer category = new CategoryContainer("Item Cooldowns");

        category.addWidget(createToggle("Enabled", "itemCooldownsEnabled", true, "Show cooldown timers on items"));

        CollapsibleWidget petsGroup = createCollapsible("Pets", "Configure pet cooldown display");
        petsGroup.addWidget(createToggle("Enabled", "itemCooldownsPetEnabled", true, "Show cooldown timer on pets"));
        petsGroup.addWidget(createColorPicker("Cooldown Color", "itemCooldownsPetCooldownColor", 0xFF5555));
        petsGroup.addWidget(createColorPicker("Active Color", "itemCooldownsPetActiveColor", 0x00FF00));
        petsGroup.addWidget(createToggle("Bold", "itemCooldownsPetBold", true, "Bold pet timer text"));
        category.addWidget(petsGroup);

        CollapsibleWidget trinketGroup = createCollapsible("Trinkets", "Configure trinket cooldown display");
        trinketGroup.addWidget(createToggle("Enabled", "itemCooldownsTrinketEnabled", true, "Show cooldown timer on trinkets"));
        trinketGroup.addWidget(createColorPicker("Color", "itemCooldownsTrinketColor", 0xFF5555));
        trinketGroup.addWidget(createToggle("Bold", "itemCooldownsTrinketBold", true, "Bold trinket timer text"));
        category.addWidget(trinketGroup);

        CollapsibleWidget banditBoxGroup = createCollapsible("Bandit Boxes", "Configure bandit box timer display");
        banditBoxGroup.addWidget(createToggle("Enabled", "itemCooldownsBanditBoxEnabled", true, "Show unlock timer on bandit boxes"));
        banditBoxGroup.addWidget(createColorPicker("Color", "itemCooldownsBanditBoxColor", 0x00FF00));
        banditBoxGroup.addWidget(createToggle("Bold", "itemCooldownsBanditBoxBold", true, "Bold bandit box timer text"));
        category.addWidget(banditBoxGroup);

        return category;
    }

    private CategoryContainer createMiscCategory() {
        CategoryContainer category = new CategoryContainer("Misc");

        category.addWidget(createToggle("Auto Trade", "autoTradeEnabled", true,
            "Shift-right-click another player to automatically send /trade <username>"));
        category.addWidget(createToggle("Bold XP/Energy Titles", "boldXpEnergyTitles", false,
            "Bold the on-screen title popups that show +XP and +Energy gains"));
        category.addWidget(createToggle("Chest Search", "chestSearchEnabled", true,
            "Adds a search bar and filter-rule sidebar to chests and containers for highlighting items"));

        // Clue Scrolls
        CollapsibleWidget clueScrollGroup = createCollapsible("Clue Scrolls",
            "Clue scroll sorting and helper options");
        clueScrollGroup.addWidget(createToggle("Sorting (Step Numbers)", "clueScrollSortingEnabled", true,
            "Shows the current clue step number large on clue scrolls in containers"));
        clueScrollGroup.addWidget(createColorPicker("Number Color", "clueScrollNumberColor", 0xFFFFFF));
        clueScrollGroup.addWidget(createToggle("Unmapped Report Tooltip", "clueScrollUnmappedTooltipEnabled", true,
            "Adds a tooltip asking you to report unmapped clue step types to nishu06 on Discord"));
        category.addWidget(clueScrollGroup);
        category.addWidget(createToggle("PrisonBreak Texture Pack", "prisonbreakTexturePackEnabled", true,
            "Auto-applies the bundled PrisonBreak ore texture pack while in the PrisonBreak world (causes a brief resource reload on enter/leave)"));

        // Enchant Book Costs
        CollapsibleWidget enchantBookGroup = createCollapsible("Enchant Book Costs",
            "Show upgrade energy cost on enchant book tooltips");
        enchantBookGroup.addWidget(createToggle("Enabled", "enchantBookCostsEnabled", true,
            "Display upgrade cost breakdown on enchant book tooltips"));
        enchantBookGroup.addWidget(createColorPicker("Text Color", "enchantBookCostsColor", 0xAA55FF));
        category.addWidget(enchantBookGroup);

        // Gang Point Expiry
        CollapsibleWidget gangPointGroup = createCollapsible("Gang Point Expiry",
            "Show local-time expiry countdown on gang point tooltips");
        gangPointGroup.addWidget(createToggle("Enabled", "gangPointExpiryEnabled", true,
            "Display a countdown and local-timezone expiry on gang point notes"));
        gangPointGroup.addWidget(createColorPicker("Text Color", "gangPointExpiryColor", 0x55FFFF));
        category.addWidget(gangPointGroup);

        // Held Item Scaling
        CollapsibleWidget heldItemGroup = createCollapsible("Held Item Scaling", "Scale held items in first person view");
        heldItemGroup.addWidget(createIntSlider("Pickaxe Scale", "heldItemPickaxeScale", 100, 25, 150, "%", "Scale for held pickaxes"));
        heldItemGroup.addWidget(createIntSlider("Sword Scale", "heldItemSwordScale", 100, 25, 150, "%", "Scale for held swords"));
        heldItemGroup.addWidget(createIntSlider("Axe Scale", "heldItemAxeScale", 100, 25, 150, "%", "Scale for held axes"));
        heldItemGroup.addWidget(createIntSlider("Other Items Scale", "heldItemOtherScale", 100, 25, 150, "%", "Scale for other items"));
        category.addWidget(heldItemGroup);

        // Pickaxe Drop Confirmation
        CollapsibleWidget pickaxeDropGroup = createCollapsible("Pickaxe Drop Confirmation", "Protect pickaxes from being dropped");
        pickaxeDropGroup.addWidget(createToggle("Enabled", "pickaxeDropConfirmationEnabled", true, "Require confirmation to drop pickaxes"));
        pickaxeDropGroup.addWidget(createToggle("Block Drop Entirely", "pickaxeDropBlockEnabled", false, "Completely prevent dropping pickaxes with the drop key"));
        pickaxeDropGroup.addWidget(createToggle("Block Inventory Drag", "pickaxeDropDragBlockEnabled", false, "Prevent dropping pickaxes by dragging them out of the inventory"));
        category.addWidget(pickaxeDropGroup);

        // Message Notifications
        CollapsibleWidget notifsGroup = createCollapsible("Message Notifications", "Sound notifications for private messages");
        notifsGroup.addWidget(createToggle("Enabled", "messageNotifsEnabled", true, "Enable/disable message notifications"));
        notifsGroup.addWidget(createDropdown("Sound", "messageNotifsSound",
            Arrays.asList("anvil", "bell", "xp_orb", "note_pling", "enchant", "level_up", "ender_eye"), "anvil", "Notification sound"));
        notifsGroup.addWidget(createIntSlider("Volume", "messageNotifsVolume", 100, 0, 200, "%", "Notification volume"));
        category.addWidget(notifsGroup);

        // Powerball Alerts
        CollapsibleWidget powerballGroup = createCollapsible("Powerball Alerts",
            "Alert when powerball cooldown has elapsed and is ready to use again");
        powerballGroup.addWidget(createToggle("Enabled", "powerballAlertEnabled", true,
            "Master toggle for powerball ready alerts"));
        powerballGroup.addWidget(createToggle("Show Title", "powerballAlertTitleEnabled", true,
            "Display a title on screen when powerball is ready"));
        powerballGroup.addWidget(createTextInput("Title Text", "powerballAlertTitleText", "Powerball Ready!",
            "Title text shown when powerball is ready"));
        powerballGroup.addWidget(createColorPicker("Title Color", "powerballAlertTitleColor", 0xFFAA00));
        powerballGroup.addWidget(createToggle("Sound Cue", "powerballAlertSoundEnabled", true,
            "Play a sound when powerball is ready"));
        powerballGroup.addWidget(createDropdown("Sound", "powerballAlertSound",
            Arrays.asList("anvil", "bell", "xp_orb", "note_pling", "enchant", "level_up", "ender_eye"),
            "level_up", "Alert sound"));
        powerballGroup.addWidget(createIntSlider("Sound Volume", "powerballAlertSoundVolume", 100, 0, 200, "%",
            "Volume for powerball ready sound"));
        category.addWidget(powerballGroup);

        return category;
    }

    // ===== TOOLS (NON-CONFIG) =====

    private CategoryContainer createToolsCategory() {
        CategoryContainer category = new CategoryContainer("Tools");

        // --- Energy Calculator ---
        CollapsibleWidget energyCalc = new CollapsibleWidget("Energy Calculator");
        energyCalc.setTooltip("Calculate energy cost for pickaxe and satchel upgrades");

        // Result label (updated reactively)
        LabelWidget resultLabel = new LabelWidget("Select options and press Calculate", Theme.textSecondary);

        // Category dropdown: Pick, Ore Satchel, Refined Satchel
        List<String> calcCategories = Arrays.asList("Pickaxe", "Ore Satchel", "Refined Satchel");
        DropdownWidget categoryDropdown = new DropdownWidget("Category", calcCategories, 0);

        // Type dropdown (changes based on category)
        List<String> pickTypes = Arrays.asList("Wood", "Stone", "Gold", "Iron", "Diamond");
        List<String> satchelTypes = Arrays.asList("Coal", "Iron", "Lapis", "Redstone", "Gold", "Diamond", "Emerald");
        DropdownWidget typeDropdown = new DropdownWidget("Type", pickTypes, 0);

        // Level sliders
        IntSliderWidget startLevel = new IntSliderWidget("Start Level", 1, 1, 750);
        startLevel.setTooltip("Current level");
        IntSliderWidget endLevel = new IntSliderWidget("End Level", 50, 1, 750);
        endLevel.setTooltip("Target level");

        // Recalculate helper
        Runnable recalculate = () -> {
            String cat = categoryDropdown.getSelectedValue();
            String type = typeDropdown.getSelectedValue();
            int start = startLevel.getValue();
            int end = endLevel.getValue();

            if (end <= start) {
                resultLabel.setText("End level must be greater than start level");
                resultLabel.setColor(0xFFFF5555);
                return;
            }

            try {
                long energy;
                String label;
                if (cat.equals("Pickaxe")) {
                    EnergyCalculator.PickType pickType = EnergyCalculator.PickType.valueOf(type.toUpperCase());
                    energy = EnergyCalculator.calcPickEnergy(pickType, start, end);
                    label = type + " Pick";
                } else if (cat.equals("Ore Satchel")) {
                    EnergyCalculator.SatchelType satchelType = EnergyCalculator.SatchelType.valueOf(type.toUpperCase());
                    energy = EnergyCalculator.calcSatchelOreEnergy(satchelType, start, end);
                    label = type + " Ore Satchel";
                } else {
                    EnergyCalculator.SatchelType satchelType = EnergyCalculator.SatchelType.valueOf(type.toUpperCase());
                    energy = EnergyCalculator.calcSatchelRefinedEnergy(satchelType, start, end);
                    label = type + " Refined Satchel";
                }
                resultLabel.setText(label + " " + start + " -> " + end + ": " + EnergyCalculator.formatEnergy(energy) + " energy");
                resultLabel.setColor(0xFF55FF55);
            } catch (IllegalArgumentException e) {
                resultLabel.setText("Invalid type for selected category");
                resultLabel.setColor(0xFFFF5555);
            }
        };

        // When category changes, swap type options and reset
        categoryDropdown.setOnChange(selected -> {
            if (selected.equals("Pickaxe")) {
                typeDropdown.setOptions(pickTypes);
                startLevel.setValue(1);
                endLevel.setValue(50);
            } else {
                typeDropdown.setOptions(satchelTypes);
                startLevel.setValue(1);
                endLevel.setValue(50);
            }
            typeDropdown.setSelectedIndex(0);
            recalculate.run();
        });

        typeDropdown.setOnChange(selected -> recalculate.run());
        startLevel.setOnChange(val -> recalculate.run());
        endLevel.setOnChange(val -> recalculate.run());

        energyCalc.addWidget(categoryDropdown);
        energyCalc.addWidget(typeDropdown);
        energyCalc.addWidget(startLevel);
        energyCalc.addWidget(endLevel);
        energyCalc.addWidget(resultLabel);

        // Initial calculation
        recalculate.run();

        category.addWidget(energyCalc);

        // --- Links ---
        CollapsibleWidget links = new CollapsibleWidget("Links");
        links.setTooltip("Useful external resources");

        LinkButtonWidget cosmicBuilds = new LinkButtonWidget("Open CosmicBuilds", "https://cosmicbuilds.com");
        cosmicBuilds.setTooltip("Opens cosmicbuilds.com in your browser");
        links.addWidget(cosmicBuilds);

        LinkButtonWidget bpDiscord = new LinkButtonWidget("BetterPrisons Discord", "https://discord.gg/QWYZJW3Avj");
        bpDiscord.setTooltip("Join the BetterPrisons Discord server");
        links.addWidget(bpDiscord);

        LinkButtonWidget cosmicDiscord = new LinkButtonWidget("Cosmic Discord", "https://discord.gg/cosmicgames");
        cosmicDiscord.setTooltip("Join the Cosmic Games Discord server");
        links.addWidget(cosmicDiscord);

        category.addWidget(links);

        return category;
    }

    private CategoryContainer createConfigSettingsCategory() {
        CategoryContainer category = new CategoryContainer("Config Settings");

        // Theme color customization
        category.addWidget(createColorPicker("Screen Background", "themeScreenBackground", 0xFF1E1E1E));
        category.addWidget(createColorPicker("Panel Background", "themePanelBackground", 0xFF252526));
        category.addWidget(createColorPicker("Widget Background", "themeWidgetBackground", 0xFF2D2D30));
        category.addWidget(createColorPicker("Widget Background (Hover)", "themeWidgetBackgroundHover", 0xFF3E3E42));
        category.addWidget(createColorPicker("Border Primary", "themeBorderPrimary", 0xFF3E3E42));
        category.addWidget(createColorPicker("Border Hover", "themeBorderHover", 0xFF007ACC));
        category.addWidget(createColorPicker("Border Focus", "themeBorderFocus", 0xFF0E639C));
        category.addWidget(createColorPicker("Text Primary", "themeTextPrimary", 0xFFCCCCCC));
        category.addWidget(createColorPicker("Text Secondary", "themeTextSecondary", 0xFF858585));
        category.addWidget(createColorPicker("Text Disabled", "themeTextDisabled", 0xFF555555));
        category.addWidget(createColorPicker("Text Accent", "themeTextAccent", 0xFF4EC9B0));
        category.addWidget(createColorPicker("Accent Primary", "themeAccentPrimary", 0xFF007ACC));
        category.addWidget(createColorPicker("Toggle On", "themeToggleOn", 0xFF4EC9B0));
        category.addWidget(createColorPicker("Toggle Off", "themeToggleOff", 0xFF3E3E42));
        category.addWidget(createColorPicker("Toggle Handle", "themeToggleHandle", 0xFFFFFFFF));
        category.addWidget(createColorPicker("Slider Track", "themeSliderTrack", 0xFF3E3E42));
        category.addWidget(createColorPicker("Slider Fill", "themeSliderFill", 0xFF007ACC));
        category.addWidget(createColorPicker("Slider Handle", "themeSliderHandle", 0xFFFFFFFF));
        category.addWidget(createColorPicker("Scrollbar Track", "themeScrollbarTrack", 0xFF1E1E1E));
        category.addWidget(createColorPicker("Scrollbar Thumb", "themeScrollbarThumb", 0xFF424242));
        category.addWidget(createColorPicker("Scrollbar Thumb (Hover)", "themeScrollbarThumbHover", 0xFF4E4E4E));
        category.addWidget(createColorPicker("Sidebar Background", "themeSidebarBackground", 0xFF252526));
        category.addWidget(createColorPicker("Sidebar Item Selected", "themeSidebarItemSelected", 0xFF094771));
        category.addWidget(createColorPicker("Sidebar Item Hover", "themeSidebarItemHover", 0xFF2A2D2E));
        category.addWidget(createColorPicker("Tooltip Background", "themeTooltipBackground", 0xFF1E1E1E));
        category.addWidget(createColorPicker("Tooltip Border", "themeTooltipBorder", 0xFF3E3E42));
        category.addWidget(createColorPicker("Tooltip Text", "themeTooltipText", 0xFFCCCCCC));

        return category;
    }

    // ===== WIDGET FACTORY METHODS =====

    private ToggleWidget createToggle(String label, String fieldName, boolean defaultValue, String tooltip) {
        ToggleWidget widget = new ToggleWidget(label, defaultValue);
        widget.setTooltip(tooltip);

        FieldBinding<Boolean> binding = new FieldBinding<>(config, fieldName, defaultValue);
        widget.setValue(binding.getValue());
        BindingRegistry.register(widget, binding);

        return widget;
    }

    private ColorPickerWidget createColorPicker(String label, String fieldName, int defaultValue) {
        ColorPickerWidget widget = new ColorPickerWidget(label, defaultValue);

        FieldBinding<Integer> binding = new FieldBinding<>(config, fieldName, defaultValue);
        widget.setColor(binding.getValue());
        BindingRegistry.register(widget, binding);

        // Add real-time update for theme colors
        if (fieldName.startsWith("theme")) {
            widget.setOnChange(newColor -> {
                binding.setValue(newColor);
                updateThemeField(fieldName, newColor);
            });
        }

        // Set popup callback to display popup with wrapped callbacks
        widget.setOnPopupOpen(popup -> {
            // Wrap the popup's callbacks to clear activePopup when closed
            Consumer<Integer> originalOnConfirm = popup.getOnConfirm();
            Runnable originalOnCancel = popup.getOnCancel();

            popup.setOnConfirm(color -> {
                if (originalOnConfirm != null) {
                    originalOnConfirm.accept(color);
                }
                this.activePopup = null;
            });

            popup.setOnCancel(() -> {
                if (originalOnCancel != null) {
                    originalOnCancel.run();
                }
                this.activePopup = null;
            });

            this.activePopup = popup;
        });

        return widget;
    }

    private IntSliderWidget createIntSlider(String label, String fieldName, int defaultValue, int min, int max, String suffix, String tooltip) {
        IntSliderWidget widget = new IntSliderWidget(label, defaultValue, min, max, suffix);
        widget.setTooltip(tooltip);

        FieldBinding<Integer> binding = new FieldBinding<>(config, fieldName, defaultValue);
        widget.setValue(binding.getValue());
        BindingRegistry.register(widget, binding);

        return widget;
    }

    private SliderWidget createFloatSlider(String label, String fieldName, float defaultValue, float min, float max, String suffix, String tooltip) {
        SliderWidget widget = new SliderWidget(label, defaultValue, min, max, suffix);
        widget.setTooltip(tooltip);
        widget.setDecimalPlaces(2);

        FieldBinding<Float> binding = new FieldBinding<>(config, fieldName, defaultValue);
        widget.setValue(binding.getValue());
        BindingRegistry.register(widget, binding);

        return widget;
    }

    private TextInputWidget createTextInput(String label, String fieldName, String defaultValue, String tooltip) {
        TextInputWidget widget = new TextInputWidget(label, defaultValue);
        widget.setTooltip(tooltip);

        FieldBinding<String> binding = new FieldBinding<>(config, fieldName, defaultValue);
        widget.setValue(binding.getValue());
        BindingRegistry.register(widget, binding);

        return widget;
    }

    private DropdownWidget createDropdown(String label, String fieldName, List<String> options, String defaultValue, String tooltip) {
        DropdownWidget widget = new DropdownWidget(label, options, options.indexOf(defaultValue));
        widget.setTooltip(tooltip);

        FieldBinding<String> binding = new FieldBinding<>(config, fieldName, defaultValue);
        String currentValue = binding.getValue();
        int index = options.indexOf(currentValue);
        if (index >= 0) {
            widget.setSelectedIndex(index);
        }
        BindingRegistry.register(widget, binding);

        return widget;
    }

    private CollapsibleWidget createCollapsible(String label, String tooltip) {
        CollapsibleWidget widget = new CollapsibleWidget(label);
        widget.setTooltip(tooltip);
        return widget;
    }

    /**
     * Updates a Theme field in real-time based on field name.
     */
    private void updateThemeField(String fieldName, int value) {
        switch (fieldName) {
            case "themeScreenBackground": Theme.screenBackground = value; break;
            case "themePanelBackground": Theme.panelBackground = value; break;
            case "themeWidgetBackground": Theme.widgetBackground = value; break;
            case "themeWidgetBackgroundHover": Theme.widgetBackgroundHover = value; break;
            case "themeBorderPrimary": Theme.borderPrimary = value; break;
            case "themeBorderHover": Theme.borderHover = value; break;
            case "themeBorderFocus": Theme.borderFocus = value; break;
            case "themeTextPrimary": Theme.textPrimary = value; break;
            case "themeTextSecondary": Theme.textSecondary = value; break;
            case "themeTextDisabled": Theme.textDisabled = value; break;
            case "themeTextAccent": Theme.textAccent = value; break;
            case "themeAccentPrimary": Theme.accentPrimary = value; break;
            case "themeToggleOn": Theme.toggleOn = value; break;
            case "themeToggleOff": Theme.toggleOff = value; break;
            case "themeToggleHandle": Theme.toggleHandle = value; break;
            case "themeSliderTrack": Theme.sliderTrack = value; break;
            case "themeSliderFill": Theme.sliderFill = value; break;
            case "themeSliderHandle": Theme.sliderHandle = value; break;
            case "themeScrollbarTrack": Theme.scrollbarTrack = value; break;
            case "themeScrollbarThumb": Theme.scrollbarThumb = value; break;
            case "themeScrollbarThumbHover": Theme.scrollbarThumbHover = value; break;
            case "themeSidebarBackground": Theme.sidebarBackground = value; break;
            case "themeSidebarItemSelected": Theme.sidebarItemSelected = value; break;
            case "themeSidebarItemHover": Theme.sidebarItemHover = value; break;
            case "themeTooltipBackground": Theme.tooltipBackground = value; break;
            case "themeTooltipBorder": Theme.tooltipBorder = value; break;
            case "themeTooltipText": Theme.tooltipText = value; break;
        }
    }

    /**
     * Saves all widget values back to config fields.
     */
    private void saveAllWidgetValues() {
        for (CategoryContainer category : categories) {
            for (Component child : category.getChildren()) {
                saveWidgetRecursive(child);
            }
        }
    }

    private void saveWidgetRecursive(Component widget) {
        // Save this widget if it has a binding
        if (BindingRegistry.hasBinding(widget)) {
            saveWidgetValue(widget);
        }

        // Recursively save children of CollapsibleWidget
        if (widget instanceof CollapsibleWidget) {
            CollapsibleWidget collapsible = (CollapsibleWidget) widget;
            for (Component child : collapsible.getChildWidgets()) {
                saveWidgetRecursive(child);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void saveWidgetValue(Component widget) {
        ConfigBinding<?> binding = BindingRegistry.getBinding(widget);
        if (binding == null) return;

        if (widget instanceof ToggleWidget) {
            ((ConfigBinding<Boolean>) binding).setValue(((ToggleWidget) widget).getValue());
        } else if (widget instanceof ColorPickerWidget) {
            ((ConfigBinding<Integer>) binding).setValue(((ColorPickerWidget) widget).getColor());
        } else if (widget instanceof IntSliderWidget) {
            ((ConfigBinding<Integer>) binding).setValue(((IntSliderWidget) widget).getValue());
        } else if (widget instanceof SliderWidget) {
            ((ConfigBinding<Float>) binding).setValue(((SliderWidget) widget).getValue());
        } else if (widget instanceof TextInputWidget) {
            ((ConfigBinding<String>) binding).setValue(((TextInputWidget) widget).getValue());
        } else if (widget instanceof DropdownWidget) {
            ((ConfigBinding<String>) binding).setValue(((DropdownWidget) widget).getSelectedValue());
        }
    }

    /**
     * Simple button component for Done and Edit HUD buttons.
     */
    private static class ButtonComponent extends Component {
        private final String label;
        private final Runnable onClick;

        public ButtonComponent(String label, int x, int y, int width, int height, Runnable onClick) {
            this.label = label;
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.onClick = onClick;
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            MinecraftClient client = MinecraftClient.getInstance();
            updateHoverState(mouseX, mouseY);

            int bgColor = hovered ? Theme.accentPrimary : Theme.widgetBackground;
            RenderUtils.drawRect(context, x, y, width, height, bgColor);
            RenderUtils.drawRectOutline(context, x, y, width, height, Theme.borderPrimary, 1);

            int textWidth = client.textRenderer.getWidth(label);
            int textX = x + (width - textWidth) / 2;
            int textY = y + (height - 8) / 2;
            context.drawText(client.textRenderer, label, textX, textY, Theme.textPrimary, false);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0 && isMouseOver(mouseX, mouseY)) {
                onClick.run();
                return true;
            }
            return false;
        }
    }
}
