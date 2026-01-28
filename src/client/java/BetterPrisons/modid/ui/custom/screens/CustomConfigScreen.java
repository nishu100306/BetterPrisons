package BetterPrisons.modid.ui.custom.screens;

import BetterPrisons.modid.BetterPrisonsClient;
import BetterPrisons.modid.Config;
import BetterPrisons.modid.ui.custom.binding.BindingRegistry;
import BetterPrisons.modid.ui.custom.binding.ConfigBinding;
import BetterPrisons.modid.ui.custom.binding.FieldBinding;
import BetterPrisons.modid.ui.custom.containers.CategoryContainer;
import BetterPrisons.modid.ui.custom.containers.ColorPickerPopup;
import BetterPrisons.modid.ui.custom.containers.SidebarContainer;
import BetterPrisons.modid.ui.custom.core.Component;
import BetterPrisons.modid.ui.custom.core.Container;
import BetterPrisons.modid.ui.custom.core.Theme;
import BetterPrisons.modid.ui.custom.rendering.RenderUtils;
import BetterPrisons.modid.ui.custom.widgets.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        sidebar.setPosition(0, TOP_BAR_HEIGHT);
        sidebar.setSize(SIDEBAR_WIDTH, height - TOP_BAR_HEIGHT - BOTTOM_BAR_HEIGHT);

        // Add category tabs
        sidebar.addTab("Cooldown HUD");
        sidebar.addTab("Satchel HUD");
        sidebar.addTab("Stats HUD");
        sidebar.addTab("Enchant HUD");
        sidebar.addTab("Meteor HUD");
        sidebar.addSeparator();
        sidebar.addTab("Super Breaker");
        sidebar.addTab("Peaceful Mining");
        sidebar.addTab("Held Item Scaling");
        sidebar.addTab("EasyView");
        sidebar.addTab("Pickaxe Drop");
        sidebar.addTab("Notifications");
        sidebar.addSeparator();
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
        categories.add(createMeteorHudCategory());
        categories.add(createSuperBreakerCategory());
        categories.add(createPeacefulMiningCategory());
        categories.add(createHeldItemScalingCategory());
        categories.add(createEasyViewCategory());
        categories.add(createPickaxeDropCategory());
        categories.add(createNotificationsCategory());
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

        // Render tooltips (always on top)
        renderTooltips(mouseX, mouseY);
    }

    private void renderTooltips(int mouseX, int mouseY) {
        // Check current category for hovered widgets with tooltips
        if (currentCategoryIndex >= 0 && currentCategoryIndex < categories.size()) {
            CategoryContainer category = categories.get(currentCategoryIndex);
            for (Component child : category.getChildren()) {
                if (child.isHovered() && child instanceof TooltipProvider) {
                    String tooltip = ((TooltipProvider) child).getTooltip();
                    if (tooltip != null && !tooltip.isEmpty()) {
                        TooltipWidget.setTooltip(tooltip, mouseX, mouseY);
                        TooltipWidget.render(null, mouseX, mouseY);
                        return;
                    }
                }
            }
        }
        TooltipWidget.clear();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Popup takes priority - block ALL events when open
        if (activePopup != null) {
            activePopup.mouseClicked(mouseX, mouseY, button);
            return true; // Always consume when popup is open
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

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        // Popup takes priority - block ALL events when open
        if (activePopup != null) {
            activePopup.mouseReleased(mouseX, mouseY, button);
            return true; // Always consume when popup is open
        }

        if (doneButton.mouseReleased(mouseX, mouseY, button)) return true;
        if (editHudButton.mouseReleased(mouseX, mouseY, button)) return true;
        if (sidebar.mouseReleased(mouseX, mouseY, button)) return true;

        if (currentCategoryIndex >= 0 && currentCategoryIndex < categories.size()) {
            if (categories.get(currentCategoryIndex).mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
        }

        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        // Popup takes priority - block ALL events when open
        if (activePopup != null) {
            activePopup.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
            return true; // Always consume when popup is open
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

        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        // Popup takes priority - block ALL events when open
        if (activePopup != null) {
            activePopup.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
            return true; // Always consume when popup is open
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
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
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

        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
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

        return super.charTyped(chr, modifiers);
    }

    @Override
    public void close() {
        saveAndClose();
    }

    // ===== CATEGORY CREATION METHODS =====

    private CategoryContainer createCooldownHudCategory() {
        CategoryContainer category = new CategoryContainer("Cooldown HUD");

        category.addWidget(createToggle("Enabled", "cooldownHudEnabled", true, "Enable/disable the Cooldown HUD"));
        category.addWidget(createToggle("Show Title", "showCooldownHudTitle", true, "Show the HUD title"));
        category.addWidget(createIntSlider("Scale", "cooldownHudScale", 100, 70, 150, "%", "HUD scale percentage"));
        category.addWidget(createColorPicker("Title Color", "cooldownHudTitleColor", 14550187));
        category.addWidget(createColorPicker("Bar Color", "cooldownBarColor", 0xFF00FF));

        // Background styling
        category.addWidget(createColorPicker("Background Color", "cooldownBgColor", 0x000000));
        category.addWidget(createIntSlider("Background Opacity", "cooldownBgOpacity", 128, 0, 255, "", "0 = transparent, 255 = opaque"));
        category.addWidget(createColorPicker("Border Color", "cooldownBorderColor", 0xFFFFFF));
        category.addWidget(createIntSlider("Border Opacity", "cooldownBorderOpacity", 128, 0, 255, "", "0 = transparent, 255 = opaque"));
        category.addWidget(createIntSlider("Border Thickness", "cooldownBorderThickness", 2, 0, 5, "px", "Border thickness in pixels"));

        // Individual command toggles and colors
        category.addWidget(createToggle("/home Enabled", "homeEnabled", true, "Show /home cooldown"));
        category.addWidget(createColorPicker("/home Color", "homeColor", 1045763));
        category.addWidget(createToggle("/jet Enabled", "jetEnabled", true, "Show /jet cooldown"));
        category.addWidget(createColorPicker("/jet Color", "jetColor", 14576132));
        category.addWidget(createToggle("/feed Enabled", "feedEnabled", true, "Show /feed cooldown"));
        category.addWidget(createColorPicker("/feed Color", "feedColor", 6700312));
        category.addWidget(createToggle("/fix Enabled", "fixEnabled", true, "Show /fix cooldown"));
        category.addWidget(createColorPicker("/fix Color", "fixColor", 12632256));
        category.addWidget(createToggle("Combat Enabled", "combatEnabled", true, "Show combat tag"));
        category.addWidget(createColorPicker("Combat Color", "combatColor", 9835026));
        category.addWidget(createToggle("/tpa Enabled", "tpaEnabled", true, "Show /tpa cooldown"));
        category.addWidget(createColorPicker("/tpa Color", "tpaColor", 5636095));
        category.addWidget(createToggle("/tpahere Enabled", "tpahereEnabled", true, "Show /tpahere cooldown"));
        category.addWidget(createColorPicker("/tpahere Color", "tpahereColor", 5636095));

        return category;
    }

    private CategoryContainer createSatchelHudCategory() {
        CategoryContainer category = new CategoryContainer("Satchel HUD");

        category.addWidget(createToggle("Enabled", "satchelHudEnabled", true, "Enable/disable the Satchel HUD"));
        category.addWidget(createToggle("Show Title", "showSatchelHudTitle", true, "Show the HUD title"));
        category.addWidget(createIntSlider("Scale", "satchelHudScale", 100, 70, 150, "%", "HUD scale percentage"));
        category.addWidget(createColorPicker("Title Color", "satchelHudTitleColor", 11722244));
        category.addWidget(createColorPicker("Bar Color", "satchelBarColor", 0xFF4488));
        category.addWidget(createToggle("Show Percentage", "satchelShowPercentage", false, "Show percentage instead of count"));
        category.addWidget(createToggle("Combine Similar Satchels", "combineSimilarSatchels", true, "Group satchels of same type"));

        // Background styling
        category.addWidget(createColorPicker("Background Color", "satchelBgColor", 0x000000));
        category.addWidget(createIntSlider("Background Opacity", "satchelBgOpacity", 128, 0, 255, "", "0 = transparent, 255 = opaque"));
        category.addWidget(createColorPicker("Border Color", "satchelBorderColor", 0xFFFFFF));
        category.addWidget(createIntSlider("Border Opacity", "satchelBorderOpacity", 128, 0, 255, "", "0 = transparent, 255 = opaque"));
        category.addWidget(createIntSlider("Border Thickness", "satchelBorderThickness", 2, 0, 5, "px", "Border thickness in pixels"));

        // Capacity threshold colors
        category.addWidget(createColorPicker("Color (<20%)", "satchelColorUnder20", 1045763));
        category.addWidget(createColorPicker("Color (20-60%)", "satchelColor20to60", 16776960));
        category.addWidget(createColorPicker("Color (60-95%)", "satchelColor60to95", 16746496));
        category.addWidget(createColorPicker("Color (95%+)", "satchelColor95Plus", 11141120));

        return category;
    }

    private CategoryContainer createStatsHudCategory() {
        CategoryContainer category = new CategoryContainer("Stats HUD");

        category.addWidget(createToggle("Enabled", "statsHudEnabled", true, "Enable/disable the Stats HUD"));
        category.addWidget(createToggle("Show Title", "showStatsHudTitle", true, "Show the HUD title"));
        category.addWidget(createIntSlider("Scale", "statsHudScale", 100, 70, 150, "%", "HUD scale percentage"));
        category.addWidget(createColorPicker("Title Color", "statsHudTitleColor", 14352636));

        // Background styling
        category.addWidget(createColorPicker("Background Color", "statsBgColor", 0x000000));
        category.addWidget(createIntSlider("Background Opacity", "statsBgOpacity", 128, 0, 255, "", "0 = transparent, 255 = opaque"));
        category.addWidget(createColorPicker("Border Color", "statsBorderColor", 0xFFFFFF));
        category.addWidget(createIntSlider("Border Opacity", "statsBorderOpacity", 128, 0, 255, "", "0 = transparent, 255 = opaque"));
        category.addWidget(createIntSlider("Border Thickness", "statsBorderThickness", 2, 0, 5, "px", "Border thickness in pixels"));

        // Element toggles
        category.addWidget(createToggle("Show Current XP", "statsShowCurrentXP", true, "Display current XP"));
        category.addWidget(createToggle("Show XP/Hour", "statsShowXPPerHour", true, "Display XP per hour"));
        category.addWidget(createToggle("Show XP/Min", "statsShowXPPerMinute", true, "Display XP per minute"));
        category.addWidget(createToggle("Show Session XP", "statsShowSessionXP", true, "Display session XP gain"));
        category.addWidget(createToggle("Show Current CE", "statsShowCurrentCE", true, "Display current CE"));
        category.addWidget(createToggle("Show CE/Hour", "statsShowCEPerHour", true, "Display CE per hour"));
        category.addWidget(createToggle("Show CE/Min", "statsShowCEPerMinute", true, "Display CE per minute"));
        category.addWidget(createToggle("Show Session CE", "statsShowSessionCE", true, "Display session CE gain"));
        category.addWidget(createToggle("Show Session Duration", "statsShowSessionDuration", true, "Display session duration"));
        category.addWidget(createToggle("Show Millis on Duration", "statsShowMillisOnSessionDuration", false, "Show milliseconds"));
        category.addWidget(createToggle("Show Time Till Level Up", "statsShowTimeTillLevelUp", true, "Show time until next level"));

        // Element colors
        category.addWidget(createColorPicker("Current XP Color", "statsCurrentXPColor", 1045763));
        category.addWidget(createColorPicker("XP/Hour Color", "statsXPPerHourColor", 1045763));
        category.addWidget(createColorPicker("XP/Min Color", "statsXPPerMinuteColor", 1045763));
        category.addWidget(createColorPicker("Session XP Color", "statsSessionXPColor", 1045763));
        category.addWidget(createColorPicker("Current CE Color", "statsCurrentCEColor", 240124));
        category.addWidget(createColorPicker("CE/Hour Color", "statsCEPerHourColor", 240124));
        category.addWidget(createColorPicker("CE/Min Color", "statsCEPerMinuteColor", 240124));
        category.addWidget(createColorPicker("Session CE Color", "statsSessionCEColor", 240124));
        category.addWidget(createColorPicker("Session Duration Color", "statsSessionDurationColor", 14352636));
        category.addWidget(createColorPicker("Time Till Level Up Color", "statsTimeTillLevelUpColor", 0xFFD700));

        return category;
    }

    private CategoryContainer createEnchantHudCategory() {
        CategoryContainer category = new CategoryContainer("Enchant HUD");

        category.addWidget(createToggle("Enabled", "enchantHudEnabled", true, "Enable/disable the Enchant HUD"));
        category.addWidget(createToggle("Show Title", "showEnchantHudTitle", true, "Show the HUD title"));
        category.addWidget(createIntSlider("Scale", "enchantHudScale", 100, 70, 150, "%", "HUD scale percentage"));
        category.addWidget(createColorPicker("Title Color", "enchantHudTitleColor", 300510));
        category.addWidget(createColorPicker("Time Color", "enchantTimeColor", 1045763));

        // Background styling
        category.addWidget(createColorPicker("Background Color", "enchantBgColor", 0x000000));
        category.addWidget(createIntSlider("Background Opacity", "enchantBgOpacity", 128, 0, 255, "", "0 = transparent, 255 = opaque"));
        category.addWidget(createColorPicker("Border Color", "enchantBorderColor", 0xFFFFFF));
        category.addWidget(createIntSlider("Border Opacity", "enchantBorderOpacity", 128, 0, 255, "", "0 = transparent, 255 = opaque"));
        category.addWidget(createIntSlider("Border Thickness", "enchantBorderThickness", 2, 0, 5, "px", "Border thickness in pixels"));

        return category;
    }

    private CategoryContainer createMeteorHudCategory() {
        CategoryContainer category = new CategoryContainer("Meteor HUD");

        category.addWidget(createToggle("Enabled", "meteorHudEnabled", true, "Enable/disable the Meteor HUD"));
        category.addWidget(createToggle("Show Title", "showMeteorHudTitle", true, "Show the HUD title"));
        category.addWidget(createIntSlider("Scale", "meteorHudScale", 100, 70, 150, "%", "HUD scale percentage"));
        category.addWidget(createColorPicker("Title Color", "meteorHudTitleColor", 14558468));
        category.addWidget(createColorPicker("Text Color", "meteorTextColor", 14558468));
        category.addWidget(createColorPicker("Natural Heading Color", "meteorNaturalHeadingColor", 0x00FF00));
        category.addWidget(createColorPicker("Summoned Heading Color", "meteorSummonedHeadingColor", 0xFF4500));
        category.addWidget(createTextInput("Icon Item ID", "meteorIconItemId", "minecraft:nether_quartz_ore", "Item ID for meteor icon"));
        category.addWidget(createIntSlider("Crashed Display Duration", "meteorCrashedDisplayDuration", 15, 5, 60, "s", "Seconds to show crashed meteor"));

        // Background styling
        category.addWidget(createColorPicker("Background Color", "meteorBgColor", 0x000000));
        category.addWidget(createIntSlider("Background Opacity", "meteorBgOpacity", 128, 0, 255, "", "0 = transparent, 255 = opaque"));
        category.addWidget(createColorPicker("Border Color", "meteorBorderColor", 0xFFFFFF));
        category.addWidget(createIntSlider("Border Opacity", "meteorBorderOpacity", 128, 0, 255, "", "0 = transparent, 255 = opaque"));
        category.addWidget(createIntSlider("Border Thickness", "meteorBorderThickness", 2, 0, 5, "px", "Border thickness in pixels"));

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

        return category;
    }

    private CategoryContainer createPeacefulMiningCategory() {
        CategoryContainer category = new CategoryContainer("Peaceful Mining");

        category.addWidget(createToggle("Enabled", "peacefulMiningEnabled", true, "Enable/disable peaceful mining feature"));
        category.addWidget(createIntSlider("Opacity", "peacefulMiningOpacity", 50, 0, 255, "", "0 = transparent, 255 = opaque"));
        category.addWidget(createIntSlider("Distance", "peacefulMiningDistance", 8, 4, 16, " blocks", "Distance to hide entities"));
        category.addWidget(createToggle("Disable On Combat", "peacefulMiningDisableOnCombat", false, "Disable when in combat"));

        return category;
    }

    private CategoryContainer createHeldItemScalingCategory() {
        CategoryContainer category = new CategoryContainer("Held Item Scaling");

        category.addWidget(createIntSlider("Pickaxe Scale", "heldItemPickaxeScale", 100, 25, 150, "%", "Scale for held pickaxes"));
        category.addWidget(createIntSlider("Sword Scale", "heldItemSwordScale", 100, 25, 150, "%", "Scale for held swords"));
        category.addWidget(createIntSlider("Axe Scale", "heldItemAxeScale", 100, 25, 150, "%", "Scale for held axes"));
        category.addWidget(createIntSlider("Other Items Scale", "heldItemOtherScale", 100, 25, 150, "%", "Scale for other items"));

        return category;
    }

    private CategoryContainer createEasyViewCategory() {
        CategoryContainer category = new CategoryContainer("EasyView");

        category.addWidget(createToggle("Enabled", "easyViewEnabled", true, "Enable/disable EasyView feature"));

        // Individual element toggles and colors
        category.addWidget(createToggle("Energy Enabled", "easyViewEnergyEnabled", true, "Show energy info"));
        category.addWidget(createColorPicker("Energy Color", "easyViewEnergyColor", 0xFFFFFF));
        category.addWidget(createToggle("Money Enabled", "easyViewMoneyEnabled", true, "Show money info"));
        category.addWidget(createColorPicker("Money Color", "easyViewMoneyColor", 0x00FF00));
        category.addWidget(createToggle("Gang Points Enabled", "easyViewGangPointsEnabled", true, "Show gang points"));
        category.addWidget(createColorPicker("Gang Points Color", "easyViewGangPointsColor", 65535));
        category.addWidget(createToggle("Black Scroll Enabled", "easyViewBlackScrollEnabled", true, "Show black scroll info"));
        category.addWidget(createColorPicker("Black Scroll Color", "easyViewBlackScrollColor", 0xFF00FF));
        category.addWidget(createToggle("Charge Orb Enabled", "easyViewChargeOrbEnabled", true, "Show charge orb info"));
        category.addWidget(createColorPicker("Charge Orb Color", "easyViewChargeOrbColor", 16755200));

        // Item display settings
        category.addWidget(createToggle("Armor Enabled", "easyViewArmorEnabled", true, "Show armor stats"));
        category.addWidget(createColorPicker("Armor Color", "easyViewArmorColor", 0x00FF00));
        category.addWidget(createIntSlider("Armor Scale", "easyViewArmorScale", 70, 50, 100, "%", "Armor text scale"));
        category.addWidget(createToggle("Armor Bold", "easyViewArmorBold", true, "Bold armor text"));

        category.addWidget(createToggle("Weapons Enabled", "easyViewWeaponsEnabled", true, "Show weapon stats"));
        category.addWidget(createColorPicker("Weapons Color", "easyViewWeaponsColor", 0x00FF00));
        category.addWidget(createIntSlider("Weapons Scale", "easyViewWeaponsScale", 70, 50, 100, "%", "Weapons text scale"));
        category.addWidget(createToggle("Weapons Bold", "easyViewWeaponsBold", true, "Bold weapons text"));

        category.addWidget(createToggle("Pickaxes Enabled", "easyViewPickaxesEnabled", true, "Show pickaxe stats"));
        category.addWidget(createColorPicker("Pickaxes Color", "easyViewPickaxesColor", 0x00FF00));
        category.addWidget(createIntSlider("Pickaxes Scale", "easyViewPickaxesScale", 70, 50, 100, "%", "Pickaxes text scale"));
        category.addWidget(createToggle("Pickaxes Bold", "easyViewPickaxesBold", true, "Bold pickaxes text"));

        category.addWidget(createToggle("Dust Enabled", "easyViewDustEnabled", true, "Show dust info"));
        category.addWidget(createColorPicker("Dust Color", "easyViewDustColor", 0xD2691E));

        category.addWidget(createToggle("Pages Enabled", "easyViewPagesEnabled", true, "Show pages info"));
        category.addWidget(createColorPicker("Pages Color", "easyViewPagesColor", 0xF5DEB3));

        return category;
    }

    private CategoryContainer createPickaxeDropCategory() {
        CategoryContainer category = new CategoryContainer("Pickaxe Drop Confirmation");

        category.addWidget(createToggle("Enabled", "pickaxeDropConfirmationEnabled", true, "Require confirmation to drop pickaxes"));

        return category;
    }

    private CategoryContainer createNotificationsCategory() {
        CategoryContainer category = new CategoryContainer("Message Notifications");

        category.addWidget(createToggle("Enabled", "messageNotifsEnabled", true, "Enable/disable message notifications"));
        category.addWidget(createDropdown("Sound", "messageNotifsSound",
            Arrays.asList("anvil", "bell", "xp_orb", "note_pling", "enchant", "level_up", "ender_eye"), "anvil", "Notification sound"));
        category.addWidget(createIntSlider("Volume", "messageNotifsVolume", 100, 0, 200, "%", "Notification volume"));

        return category;
    }

    private CategoryContainer createConfigSettingsCategory() {
        CategoryContainer category = new CategoryContainer("Config Settings");

        category.addWidget(createToggle("Use Comma Formatting", "useCommaFormatting", false, "Format numbers with commas"));
        category.addWidget(createToggle("Theme Animations", "themeAnimationsEnabled", true, "Enable smooth animations in UI"));

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

        // Set popup callback to display popup
        widget.setOnPopupOpen(popup -> {
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

    /**
     * Saves all widget values back to config fields.
     */
    private void saveAllWidgetValues() {
        for (CategoryContainer category : categories) {
            for (Component child : category.getChildren()) {
                if (BindingRegistry.hasBinding(child)) {
                    saveWidgetValue(child);
                }
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

    /**
     * Interface for components that provide tooltips.
     */
    private interface TooltipProvider {
        String getTooltip();
    }
}
