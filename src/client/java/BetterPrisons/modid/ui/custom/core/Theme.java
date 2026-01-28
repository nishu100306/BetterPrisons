package BetterPrisons.modid.ui.custom.core;

import BetterPrisons.modid.Config;

/**
 * Centralized theme color definitions for the custom UI system.
 * Loads colors from the Config instance.
 */
public class Theme {
    // Screen and panel colors
    public static int screenBackground;
    public static int panelBackground;
    public static int widgetBackground;
    public static int widgetBackgroundHover;

    // Border colors
    public static int borderPrimary;
    public static int borderHover;
    public static int borderFocus;

    // Text colors
    public static int textPrimary;
    public static int textSecondary;
    public static int textDisabled;
    public static int textAccent;

    // Accent colors
    public static int accentPrimary;

    // Toggle colors
    public static int toggleOn;
    public static int toggleOff;
    public static int toggleHandle;

    // Slider colors
    public static int sliderTrack;
    public static int sliderFill;
    public static int sliderHandle;

    // Scrollbar colors
    public static int scrollbarTrack;
    public static int scrollbarThumb;
    public static int scrollbarThumbHover;

    // Sidebar colors
    public static int sidebarBackground;
    public static int sidebarItemSelected;
    public static int sidebarItemHover;

    // Tooltip colors
    public static int tooltipBackground;
    public static int tooltipBorder;
    public static int tooltipText;

    // Animation settings
    public static boolean animationsEnabled;

    /**
     * Loads theme colors from the config.
     * Should be called when the config is loaded or when theme settings change.
     */
    public static void reload(Config config) {
        // Note: These field names will be added to Config.java in Phase 4
        // For now, use default values
        try {
            screenBackground = getField(config, "themeScreenBackground", 0xFF1E1E1E);
            panelBackground = getField(config, "themePanelBackground", 0xFF252526);
            widgetBackground = getField(config, "themeWidgetBackground", 0xFF2D2D30);
            widgetBackgroundHover = getField(config, "themeWidgetBackgroundHover", 0xFF3E3E42);

            borderPrimary = getField(config, "themeBorderPrimary", 0xFF3E3E42);
            borderHover = getField(config, "themeBorderHover", 0xFF007ACC);
            borderFocus = getField(config, "themeBorderFocus", 0xFF0E639C);

            textPrimary = getField(config, "themeTextPrimary", 0xFFCCCCCC);
            textSecondary = getField(config, "themeTextSecondary", 0xFF858585);
            textDisabled = getField(config, "themeTextDisabled", 0xFF555555);
            textAccent = getField(config, "themeTextAccent", 0xFF4EC9B0);

            accentPrimary = getField(config, "themeAccentPrimary", 0xFF007ACC);

            toggleOn = getField(config, "themeToggleOn", 0xFF4EC9B0);
            toggleOff = getField(config, "themeToggleOff", 0xFF3E3E42);
            toggleHandle = getField(config, "themeToggleHandle", 0xFFFFFFFF);

            sliderTrack = getField(config, "themeSliderTrack", 0xFF3E3E42);
            sliderFill = getField(config, "themeSliderFill", 0xFF007ACC);
            sliderHandle = getField(config, "themeSliderHandle", 0xFFFFFFFF);

            scrollbarTrack = getField(config, "themeScrollbarTrack", 0xFF1E1E1E);
            scrollbarThumb = getField(config, "themeScrollbarThumb", 0xFF424242);
            scrollbarThumbHover = getField(config, "themeScrollbarThumbHover", 0xFF4E4E4E);

            sidebarBackground = getField(config, "themeSidebarBackground", 0xFF252526);
            sidebarItemSelected = getField(config, "themeSidebarItemSelected", 0xFF094771);
            sidebarItemHover = getField(config, "themeSidebarItemHover", 0xFF2A2D2E);

            tooltipBackground = getField(config, "themeTooltipBackground", 0xFF1E1E1E);
            tooltipBorder = getField(config, "themeTooltipBorder", 0xFF3E3E42);
            tooltipText = getField(config, "themeTooltipText", 0xFFCCCCCC);

            animationsEnabled = getBooleanField(config, "themeAnimationsEnabled", true);
        } catch (Exception e) {
            // If fields don't exist yet, use defaults
            loadDefaults();
        }
    }

    /**
     * Loads default theme colors.
     */
    public static void loadDefaults() {
        screenBackground = 0xFF1E1E1E;
        panelBackground = 0xFF252526;
        widgetBackground = 0xFF2D2D30;
        widgetBackgroundHover = 0xFF3E3E42;

        borderPrimary = 0xFF3E3E42;
        borderHover = 0xFF007ACC;
        borderFocus = 0xFF0E639C;

        textPrimary = 0xFFCCCCCC;
        textSecondary = 0xFF858585;
        textDisabled = 0xFF555555;
        textAccent = 0xFF4EC9B0;

        accentPrimary = 0xFF007ACC;

        toggleOn = 0xFF4EC9B0;
        toggleOff = 0xFF3E3E42;
        toggleHandle = 0xFFFFFFFF;

        sliderTrack = 0xFF3E3E42;
        sliderFill = 0xFF007ACC;
        sliderHandle = 0xFFFFFFFF;

        scrollbarTrack = 0xFF1E1E1E;
        scrollbarThumb = 0xFF424242;
        scrollbarThumbHover = 0xFF4E4E4E;

        sidebarBackground = 0xFF252526;
        sidebarItemSelected = 0xFF094771;
        sidebarItemHover = 0xFF2A2D2E;

        tooltipBackground = 0xFF1E1E1E;
        tooltipBorder = 0xFF3E3E42;
        tooltipText = 0xFFCCCCCC;

        animationsEnabled = true;
    }

    /**
     * Helper method to get an integer field from config using reflection.
     * Returns default value if field doesn't exist.
     */
    private static int getField(Config config, String fieldName, int defaultValue) {
        try {
            var field = Config.class.getField(fieldName);
            return field.getInt(config);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Helper method to get a boolean field from config using reflection.
     * Returns default value if field doesn't exist.
     */
    private static boolean getBooleanField(Config config, String fieldName, boolean defaultValue) {
        try {
            var field = Config.class.getField(fieldName);
            return field.getBoolean(config);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    // Initialize with defaults on class load
    static {
        loadDefaults();
    }
}
