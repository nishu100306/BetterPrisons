package BetterPrisons.modid;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class Config {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File CONFIG_FILE = new File("config/betterprisons/config.json");

    // Feature toggles
    public boolean cooldownHudEnabled = true;
    public boolean satchelHudEnabled = true;
    public boolean statsHudEnabled = true;
    public boolean enchantHudEnabled = true;
    public boolean meteorHudEnabled = true;
    public boolean useCommaFormatting = false;
    public boolean peacefulMiningEnabled = true;
    public boolean satchelShowPercentage = false;
    public boolean combineSimilarSatchels = true;

    // HUD positions (x, y for each element)
    public int cooldownHudX = 6;
    public int cooldownHudY = 7;
    public int satchelHudX = 7;
    public int satchelHudY = 127;
    public int statsHudX = 535;
    public int statsHudY = 212;
    public int enchantHudX = 517;
    public int enchantHudY = 4;
    public int meteorHudX = 8;
    public int meteorHudY = 74;

    // Colors (as integers)
    public int cooldownBarColor = 0xFF00FF;
    public int satchelBarColor = 0xFF4488;

    // Super Breaker Aura colors and opacity
    public int superBreakerBaseColor = 16386570; // RGB color (yellow)
    public int superBreakerBaseOpacity = 79; // 0-255 (79 = 0x4F, ~30% opacity)
    public int superBreakerLightColor = 1444602; // RGB color (blue)
    public int superBreakerLightOpacity = 191; // 0-255 (191 = 0xBF, ~75% opacity)
    public boolean superBreakerAuraEnabled = true;

    // Peaceful Mining opacity
    public int peacefulMiningOpacity = 50; // 0-255
    public int peacefulMiningDistance = 8;
    public boolean peacefulMiningDisableOnCombat = false;

    // Pickaxe drop confirmation
    public boolean pickaxeDropConfirmationEnabled = true;

    // Held item scaling (25-150%)
    public int heldItemPickaxeScale = 100;
    public int heldItemSwordScale = 100;
    public int heldItemAxeScale = 100;
    public int heldItemOtherScale = 100;

    // EasyView settings
    public boolean easyViewEnabled = true;
    public boolean easyViewEnergyEnabled = true;
    public boolean easyViewMoneyEnabled = true;
    public boolean easyViewGangPointsEnabled = true;
    public boolean easyViewBlackScrollEnabled = true;
    public boolean easyViewChargeOrbEnabled = true;
    public boolean easyViewArmorEnabled = true;
    public boolean easyViewWeaponsEnabled = true;
    public boolean easyViewPickaxesEnabled = true;
    public boolean easyViewDustEnabled = true;
    public boolean easyViewPagesEnabled = true;
    public int easyViewEnergyColor = 0xFFFFFF;
    public int easyViewMoneyColor = 0x00FF00;
    public int easyViewGangPointsColor = 65535; // cyan
    public int easyViewBlackScrollColor = 0xFF00FF;
    public int easyViewChargeOrbColor = 16755200; // yellow-ish
    public int easyViewArmorColor = 0x00FF00; // light green
    public int easyViewWeaponsColor = 0x00FF00; // light green
    public int easyViewPickaxesColor = 0x00FF00; // light green
    public int easyViewDustColor = 0xD2691E; // chocolate brown
    public int easyViewPagesColor = 0xF5DEB3; // wheat/parchment

    // EasyView scale settings (70 = 0.7 scale)
    public int easyViewPickaxesScale = 70;
    public int easyViewWeaponsScale = 70;
    public int easyViewArmorScale = 70;

    // EasyView bold settings
    public boolean easyViewEnergyBold = true;
    public boolean easyViewMoneyBold = true;
    public boolean easyViewGangPointsBold = true;
    public boolean easyViewBlackScrollBold = true;
    public boolean easyViewChargeOrbBold = true;
    public boolean easyViewArmorBold = true;
    public boolean easyViewWeaponsBold = true;
    public boolean easyViewPickaxesBold = true;
    public boolean easyViewDustBold = true;
    public boolean easyViewPagesBold = true;

    // HUD Scaling
    public int cooldownHudScale = 100;
    public int satchelHudScale = 100;
    public int statsHudScale = 100;
    public int enchantHudScale = 100;
    public int meteorHudScale = 100;
    public int superBreakerAuraScale = 100;

    // Cooldown HUD styling
    public int cooldownBgColor = 0x000000;
    public int cooldownBgOpacity = 128;
    public int cooldownBorderColor = 0xFFFFFF;
    public int cooldownBorderOpacity = 128;
    public int cooldownBorderThickness = 2;

    // Satchel HUD styling
    public int satchelBgColor = 0x000000;
    public int satchelBgOpacity = 128;
    public int satchelBorderColor = 0xFFFFFF;
    public int satchelBorderOpacity = 128;
    public int satchelBorderThickness = 2;

    // Stats HUD styling
    public int statsBgColor = 0x000000;
    public int statsBgOpacity = 128;
    public int statsBorderColor = 0xFFFFFF;
    public int statsBorderOpacity = 128;
    public int statsBorderThickness = 2;

    // Enchant HUD styling
    public int enchantBgColor = 0x000000;
    public int enchantBgOpacity = 128;
    public int enchantBorderColor = 0xFFFFFF;
    public int enchantBorderOpacity = 128;
    public int enchantBorderThickness = 2;
    public int enchantTimeColor = 1045763; // green

    // Meteor HUD styling
    public int meteorBgColor = 0x000000;
    public int meteorBgOpacity = 128;
    public int meteorBorderColor = 0xFFFFFF;
    public int meteorBorderOpacity = 128;
    public int meteorBorderThickness = 2;

    // Stats HUD element toggles
    public boolean statsShowCurrentXP = true;
    public boolean statsShowXPPerHour = true;
    public boolean statsShowXPPerMinute = true;
    public boolean statsShowSessionXP = true;
    public boolean statsShowCurrentCE = true;
    public boolean statsShowCEPerHour = true;
    public boolean statsShowCEPerMinute = true;
    public boolean statsShowSessionCE = true;
    public boolean statsShowSessionDuration = true;
    public boolean statsShowMillisOnSessionDuration = false;
    public boolean statsShowTimeTillLevelUp = true;

    // Stats HUD text colors (RGB, no alpha)
    public int statsCurrentXPColor = 1045763; // green
    public int statsXPPerHourColor = 1045763; // green
    public int statsXPPerMinuteColor = 1045763; // green
    public int statsSessionXPColor = 1045763; // green
    public int statsCurrentCEColor = 240124; // blue
    public int statsCEPerHourColor = 240124; // blue
    public int statsCEPerMinuteColor = 240124; // blue
    public int statsSessionCEColor = 240124; // blue
    public int statsSessionDurationColor = 14352636; // purple
    public int statsTimeTillLevelUpColor = 0xFFD700; // gold

    // Satchel HUD capacity threshold colors (RGB, no alpha)
    public int satchelColorUnder20 = 1045763;    // green - nearly empty
    public int satchelColor20to60 = 16776960;     // yellow - low
    public int satchelColor60to95 = 16746496;     // orange - medium
    public int satchelColor95Plus = 11141120;     // dark red - nearly full

    // Cooldown command configs (enabled and color for each command)
    public boolean homeEnabled = true;
    public int homeColor = 1045763; // green
    public boolean jetEnabled = true;
    public int jetColor = 14576132; // pink
    public boolean feedEnabled = true;
    public int feedColor = 6700312; // brown
    public boolean fixEnabled = true;
    public int fixColor = 12632256; // gold
    public boolean combatEnabled = true;
    public int combatColor = 9835026; // dark red
    public boolean tpaEnabled = true;
    public int tpaColor = 5636095; // light green
    public boolean tpahereEnabled = true;
    public int tpahereColor = 5636095; // light green

    // HUD titles (show/hide and color for each HUD)
    public boolean showCooldownHudTitle = true;
    public int cooldownHudTitleColor = 14550187; // yellow
    public boolean showSatchelHudTitle = true;
    public int satchelHudTitleColor = 11722244; // light green
    public boolean showStatsHudTitle = true;
    public int statsHudTitleColor = 14352636; // purple
    public boolean showEnchantHudTitle = true;
    public int enchantHudTitleColor = 300510; // cyan
    public boolean showMeteorHudTitle = true;
    public int meteorHudTitleColor = 14558468; // pink

    // Meteor HUD text color
    public int meteorTextColor = 14558468; // pink

    // Meteor HUD heading colors
    public int meteorNaturalHeadingColor = 0x00FF00; // green
    public int meteorSummonedHeadingColor = 0xFF4500; // orange-red

    // Meteor HUD icon (minecraft: prefix is added automatically)
    public String meteorIconItemId = "nether_quartz_ore";

    // Meteor HUD crashed display duration (in seconds)
    public int meteorCrashedDisplayDuration = 15;

    // Message Notifications
    public boolean messageNotifsEnabled = true;
    public String messageNotifsSound = "anvil"; // "anvil", "bell", "xp_orb", "note_pling", "enchant", "level_up", "ender_eye"
    public int messageNotifsVolume = 100; // 0-200

    // Theme colors for custom UI system
    public int themeScreenBackground = 0xFF1E1E1E;
    public int themePanelBackground = 0xFF252526;
    public int themeWidgetBackground = 0xFF2D2D30;
    public int themeWidgetBackgroundHover = 0xFF3E3E42;
    public int themeBorderPrimary = 0xFF3E3E42;
    public int themeBorderHover = 0xFF007ACC;
    public int themeBorderFocus = 0xFF0E639C;
    public int themeTextPrimary = 0xFFCCCCCC;
    public int themeTextSecondary = 0xFF858585;
    public int themeTextDisabled = 0xFF555555;
    public int themeTextAccent = 0xFF4EC9B0;
    public int themeAccentPrimary = 0xFF007ACC;
    public int themeToggleOn = 0xFF4EC9B0;
    public int themeToggleOff = 0xFF3E3E42;
    public int themeToggleHandle = 0xFFFFFFFF;
    public int themeSliderTrack = 0xFF3E3E42;
    public int themeSliderFill = 0xFF007ACC;
    public int themeSliderHandle = 0xFFFFFFFF;
    public int themeScrollbarTrack = 0xFF1E1E1E;
    public int themeScrollbarThumb = 0xFF424242;
    public int themeScrollbarThumbHover = 0xFF4E4E4E;
    public int themeSidebarBackground = 0xFF252526;
    public int themeSidebarItemSelected = 0xFF094771;
    public int themeSidebarItemHover = 0xFF2A2D2E;
    public int themeTooltipBackground = 0xFF1E1E1E;
    public int themeTooltipBorder = 0xFF3E3E42;
    public int themeTooltipText = 0xFFCCCCCC;
    public boolean themeAnimationsEnabled = true;

    public void load() {
        if (!CONFIG_FILE.exists()) {
            save();
            return;
        }

        try (FileReader reader = new FileReader(CONFIG_FILE)) {
            Config loaded = GSON.fromJson(reader, Config.class);
            if (loaded != null) {
                this.cooldownHudEnabled = loaded.cooldownHudEnabled;
                this.satchelHudEnabled = loaded.satchelHudEnabled;
                this.statsHudEnabled = loaded.statsHudEnabled;
                this.enchantHudEnabled = loaded.enchantHudEnabled;
                this.meteorHudEnabled = loaded.meteorHudEnabled;
                this.useCommaFormatting = loaded.useCommaFormatting;
                this.peacefulMiningEnabled = loaded.peacefulMiningEnabled;
                this.cooldownHudX = loaded.cooldownHudX;
                this.cooldownHudY = loaded.cooldownHudY;
                this.satchelHudX = loaded.satchelHudX;
                this.satchelHudY = loaded.satchelHudY;
                this.statsHudX = loaded.statsHudX;
                this.statsHudY = loaded.statsHudY;
                this.enchantHudX = loaded.enchantHudX;
                this.enchantHudY = loaded.enchantHudY;
                this.meteorHudX = loaded.meteorHudX;
                this.meteorHudY = loaded.meteorHudY;
                this.cooldownBarColor = loaded.cooldownBarColor;
                this.satchelBarColor = loaded.satchelBarColor;

                // Load styling settings
                this.cooldownBgColor = loaded.cooldownBgColor;
                this.cooldownBgOpacity = loaded.cooldownBgOpacity;
                this.cooldownBorderColor = loaded.cooldownBorderColor;
                this.cooldownBorderOpacity = loaded.cooldownBorderOpacity;
                this.cooldownBorderThickness = loaded.cooldownBorderThickness;

                this.satchelBgColor = loaded.satchelBgColor;
                this.satchelBgOpacity = loaded.satchelBgOpacity;
                this.satchelBorderColor = loaded.satchelBorderColor;
                this.satchelBorderOpacity = loaded.satchelBorderOpacity;
                this.satchelBorderThickness = loaded.satchelBorderThickness;

                this.statsBgColor = loaded.statsBgColor;
                this.statsBgOpacity = loaded.statsBgOpacity;
                this.statsBorderColor = loaded.statsBorderColor;
                this.statsBorderOpacity = loaded.statsBorderOpacity;
                this.statsBorderThickness = loaded.statsBorderThickness;

                this.enchantBgColor = loaded.enchantBgColor;
                this.enchantBgOpacity = loaded.enchantBgOpacity;
                this.enchantBorderColor = loaded.enchantBorderColor;
                this.enchantBorderOpacity = loaded.enchantBorderOpacity;
                this.enchantBorderThickness = loaded.enchantBorderThickness;
                this.enchantTimeColor = loaded.enchantTimeColor;

                this.meteorBgColor = loaded.meteorBgColor;
                this.meteorBgOpacity = loaded.meteorBgOpacity;
                this.meteorBorderColor = loaded.meteorBorderColor;
                this.meteorBorderOpacity = loaded.meteorBorderOpacity;
                this.meteorBorderThickness = loaded.meteorBorderThickness;

                // Load Stats HUD element toggles
                this.statsShowCurrentXP = loaded.statsShowCurrentXP;
                this.statsShowXPPerHour = loaded.statsShowXPPerHour;
                this.statsShowXPPerMinute = loaded.statsShowXPPerMinute;
                this.statsShowSessionXP = loaded.statsShowSessionXP;
                this.statsShowCurrentCE = loaded.statsShowCurrentCE;
                this.statsShowCEPerHour = loaded.statsShowCEPerHour;
                this.statsShowCEPerMinute = loaded.statsShowCEPerMinute;
                this.statsShowSessionCE = loaded.statsShowSessionCE;
                this.statsShowSessionDuration = loaded.statsShowSessionDuration;
                this.statsShowMillisOnSessionDuration = loaded.statsShowMillisOnSessionDuration;
                this.statsShowTimeTillLevelUp = loaded.statsShowTimeTillLevelUp;

                // Load Stats HUD text colors
                this.statsCurrentXPColor = loaded.statsCurrentXPColor;
                this.statsXPPerHourColor = loaded.statsXPPerHourColor;
                this.statsXPPerMinuteColor = loaded.statsXPPerMinuteColor;
                this.statsSessionXPColor = loaded.statsSessionXPColor;
                this.statsCurrentCEColor = loaded.statsCurrentCEColor;
                this.statsCEPerHourColor = loaded.statsCEPerHourColor;
                this.statsCEPerMinuteColor = loaded.statsCEPerMinuteColor;
                this.statsSessionCEColor = loaded.statsSessionCEColor;
                this.statsSessionDurationColor = loaded.statsSessionDurationColor;
                this.statsTimeTillLevelUpColor = loaded.statsTimeTillLevelUpColor;

                // Load Satchel HUD threshold colors
                this.satchelColorUnder20 = loaded.satchelColorUnder20;
                this.satchelColor20to60 = loaded.satchelColor20to60;
                this.satchelColor60to95 = loaded.satchelColor60to95;
                this.satchelColor95Plus = loaded.satchelColor95Plus;

                // Load Cooldown command configs
                this.homeEnabled = loaded.homeEnabled;
                this.homeColor = loaded.homeColor;
                this.jetEnabled = loaded.jetEnabled;
                this.jetColor = loaded.jetColor;
                this.feedEnabled = loaded.feedEnabled;
                this.feedColor = loaded.feedColor;
                this.fixEnabled = loaded.fixEnabled;
                this.fixColor = loaded.fixColor;
                this.combatEnabled = loaded.combatEnabled;
                this.combatColor = loaded.combatColor;
                this.tpaEnabled = loaded.tpaEnabled;
                this.tpaColor = loaded.tpaColor;
                this.tpahereEnabled = loaded.tpahereEnabled;
                this.tpahereColor = loaded.tpahereColor;

                // Load HUD title settings
                this.showCooldownHudTitle = loaded.showCooldownHudTitle;
                this.cooldownHudTitleColor = loaded.cooldownHudTitleColor;
                this.showSatchelHudTitle = loaded.showSatchelHudTitle;
                this.satchelHudTitleColor = loaded.satchelHudTitleColor;
                this.showStatsHudTitle = loaded.showStatsHudTitle;
                this.statsHudTitleColor = loaded.statsHudTitleColor;
                this.showEnchantHudTitle = loaded.showEnchantHudTitle;
                this.enchantHudTitleColor = loaded.enchantHudTitleColor;
                this.showMeteorHudTitle = loaded.showMeteorHudTitle;
                this.meteorHudTitleColor = loaded.meteorHudTitleColor;

                // Load Meteor HUD text color
                this.meteorTextColor = loaded.meteorTextColor;

                // Load Meteor HUD heading colors
                this.meteorNaturalHeadingColor = loaded.meteorNaturalHeadingColor;
                this.meteorSummonedHeadingColor = loaded.meteorSummonedHeadingColor;

                // Load Meteor HUD icon
                this.meteorIconItemId = loaded.meteorIconItemId;

                // Load Meteor HUD crashed display duration
                this.meteorCrashedDisplayDuration = loaded.meteorCrashedDisplayDuration;

                // Load super breaker aura settings
                this.superBreakerBaseColor = loaded.superBreakerBaseColor;
                this.superBreakerBaseOpacity = loaded.superBreakerBaseOpacity;
                this.superBreakerLightColor = loaded.superBreakerLightColor;
                this.superBreakerLightOpacity = loaded.superBreakerLightOpacity;
                this.superBreakerAuraEnabled = loaded.superBreakerAuraEnabled;

                // Load peaceful mining settings
                this.peacefulMiningOpacity = loaded.peacefulMiningOpacity;
                this.peacefulMiningDistance = loaded.peacefulMiningDistance;
                this.peacefulMiningDisableOnCombat = loaded.peacefulMiningDisableOnCombat;
                this.pickaxeDropConfirmationEnabled = loaded.pickaxeDropConfirmationEnabled;

                // Load held item scaling settings
                this.heldItemPickaxeScale = loaded.heldItemPickaxeScale;
                this.heldItemSwordScale = loaded.heldItemSwordScale;
                this.heldItemAxeScale = loaded.heldItemAxeScale;
                this.heldItemOtherScale = loaded.heldItemOtherScale;

                // Load EasyView settings
                this.easyViewEnabled = loaded.easyViewEnabled;
                this.easyViewEnergyEnabled = loaded.easyViewEnergyEnabled;
                this.easyViewMoneyEnabled = loaded.easyViewMoneyEnabled;
                this.easyViewGangPointsEnabled = loaded.easyViewGangPointsEnabled;
                this.easyViewBlackScrollEnabled = loaded.easyViewBlackScrollEnabled;
                this.easyViewChargeOrbEnabled = loaded.easyViewChargeOrbEnabled;
                this.easyViewArmorEnabled = loaded.easyViewArmorEnabled;
                this.easyViewWeaponsEnabled = loaded.easyViewWeaponsEnabled;
                this.easyViewPickaxesEnabled = loaded.easyViewPickaxesEnabled;
                this.easyViewDustEnabled = loaded.easyViewDustEnabled;
                this.easyViewPagesEnabled = loaded.easyViewPagesEnabled;
                this.easyViewEnergyColor = loaded.easyViewEnergyColor;
                this.easyViewMoneyColor = loaded.easyViewMoneyColor;
                this.easyViewGangPointsColor = loaded.easyViewGangPointsColor;
                this.easyViewBlackScrollColor = loaded.easyViewBlackScrollColor;
                this.easyViewChargeOrbColor = loaded.easyViewChargeOrbColor;
                this.easyViewArmorColor = loaded.easyViewArmorColor;
                this.easyViewWeaponsColor = loaded.easyViewWeaponsColor;
                this.easyViewPickaxesColor = loaded.easyViewPickaxesColor;
                this.easyViewDustColor = loaded.easyViewDustColor;
                this.easyViewPagesColor = loaded.easyViewPagesColor;

                // Load EasyView scale settings
                this.easyViewPickaxesScale = loaded.easyViewPickaxesScale;
                this.easyViewWeaponsScale = loaded.easyViewWeaponsScale;
                this.easyViewArmorScale = loaded.easyViewArmorScale;

                // Load EasyView bold settings
                this.easyViewEnergyBold = loaded.easyViewEnergyBold;
                this.easyViewMoneyBold = loaded.easyViewMoneyBold;
                this.easyViewGangPointsBold = loaded.easyViewGangPointsBold;
                this.easyViewBlackScrollBold = loaded.easyViewBlackScrollBold;
                this.easyViewChargeOrbBold = loaded.easyViewChargeOrbBold;
                this.easyViewArmorBold = loaded.easyViewArmorBold;
                this.easyViewWeaponsBold = loaded.easyViewWeaponsBold;
                this.easyViewPickaxesBold = loaded.easyViewPickaxesBold;
                this.easyViewDustBold = loaded.easyViewDustBold;
                this.easyViewPagesBold = loaded.easyViewPagesBold;

                // Load HUD scaling settings
                this.cooldownHudScale = loaded.cooldownHudScale;
                this.satchelHudScale = loaded.satchelHudScale;
                this.statsHudScale = loaded.statsHudScale;
                this.enchantHudScale = loaded.enchantHudScale;
                this.meteorHudScale = loaded.meteorHudScale;
                this.superBreakerAuraScale = loaded.superBreakerAuraScale;

                // Load satchel percentage option
                this.satchelShowPercentage = loaded.satchelShowPercentage;

                this.combineSimilarSatchels = loaded.combineSimilarSatchels;

                // Load message notifications settings
                this.messageNotifsEnabled = loaded.messageNotifsEnabled;
                this.messageNotifsSound = loaded.messageNotifsSound;
                this.messageNotifsVolume = loaded.messageNotifsVolume;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void save() {
        CONFIG_FILE.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(CONFIG_FILE)) {
            GSON.toJson(this, writer);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

