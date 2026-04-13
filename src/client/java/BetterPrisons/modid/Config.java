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
    public boolean eventsHudEnabled = true;
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
    public int eventsHudX = 8;
    public int eventsHudY = 74;

    // Colors (as integers)
    public int cooldownBarColor = 0xFF00FF;
    public int satchelBarColor = 0xFF4488;

    // Super Breaker Aura colors and opacity
    public int superBreakerBaseColor = 16386570; // RGB color (yellow)
    public int superBreakerBaseOpacity = 79; // 0-255 (79 = 0x4F, ~30% opacity)
    public int superBreakerLightColor = 1444602; // RGB color (blue)
    public int superBreakerLightOpacity = 191; // 0-255 (191 = 0xBF, ~75% opacity)
    public boolean superBreakerAuraEnabled = true;
    public boolean superBreakerTimerEnabled = true;
    public int superBreakerTimerOffsetX = 0;
    public int superBreakerTimerOffsetY = -20;

    // Peaceful Mining opacity
    public int peacefulMiningOpacity = 50; // 0-255
    public int peacefulMiningDistance = 8;
    public boolean peacefulMiningDisableOnCombat = false;

    // Pickaxe drop confirmation
    public boolean pickaxeDropConfirmationEnabled = true;
    public boolean pickaxeDropBlockEnabled = false;
    public boolean pickaxeDropDragBlockEnabled = false;

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
    public boolean easyViewPrestigeTokenEnabled = true;
    public boolean easyViewXpBottleEnabled = true;
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
    public int easyViewPrestigeTokenColor = 0xFFD700; // gold
    public int easyViewXpBottleColor = 0xFFFFFF; // white (default, overridden by tier color)

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
    public boolean easyViewPagesTierColor = false;
    public boolean easyViewPrestigeTokenBold = true;
    public boolean easyViewXpBottleBold = true;
    public boolean easyViewXpBottleTierColor = true;

    // Item Cooldowns
    public boolean itemCooldownsEnabled = true;
    public boolean itemCooldownsPetEnabled = true;
    public int itemCooldownsPetCooldownColor = 0xFF5555; // red — cooldown timer
    public int itemCooldownsPetActiveColor = 0x00FF00; // green — active duration timer
    public boolean itemCooldownsPetBold = true;
    public boolean itemCooldownsTrinketEnabled = true;
    public int itemCooldownsTrinketColor = 0xFF5555; // red
    public boolean itemCooldownsTrinketBold = true;
    public boolean itemCooldownsBanditBoxEnabled = true;
    public int itemCooldownsBanditBoxColor = 0x00FF00; // green — unlocking timer
    public boolean itemCooldownsBanditBoxBold = true;

    // HUD Scaling
    public int cooldownHudScale = 100;
    public int satchelHudScale = 100;
    public int statsHudScale = 100;
    public int enchantHudScale = 100;
    public int eventsHudScale = 100;
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

    // Events HUD styling
    public int eventsBgColor = 0x000000;
    public int eventsBgOpacity = 128;
    public int eventsBorderColor = 0xFFFFFF;
    public int eventsBorderOpacity = 128;
    public int eventsBorderThickness = 2;

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
    public boolean showEventsHudTitle = true;
    public int eventsHudTitleColor = 14558468; // pink

    // Events HUD text color
    public int eventsTextColor = 14558468; // pink

    // Events HUD heading colors
    public int eventsNaturalHeadingColor = 0x00FF00; // green
    public int eventsSummonedHeadingColor = 0xFF4500; // orange-red

    // Events HUD icon (minecraft: prefix is added automatically)
    public String eventsIconItemId = "nether_quartz_ore";

    // Events HUD crashed display duration (in seconds)
    public int eventsCrashedDisplayDuration = 15;

    // Events HUD distance display toggles
    public boolean meteorShowDistance = true;
    public boolean merchantShowDistance = true;

    // Merchant settings
    public boolean merchantsEnabled = true;
    public int merchantTimeoutMinutes = 20;
    public int merchantSlainDisplayDuration = 10;

    // Merchant tier toggles
    public boolean coalMerchantEnabled = true;
    public boolean ironMerchantEnabled = true;
    public boolean lapisMerchantEnabled = true;
    public boolean redstoneMerchantEnabled = true;
    public boolean goldMerchantEnabled = true;
    public boolean diamondMerchantEnabled = true;
    public boolean emeraldMerchantEnabled = true;

    // Merchant tier heading colors
    public int coalMerchantHeadingColor     = 0x555555; // dark gray
    public int ironMerchantHeadingColor     = 0xAAAAAA; // gray
    public int lapisMerchantHeadingColor    = 0x5555FF; // blue
    public int redstoneMerchantHeadingColor = 0xFF5555; // red
    public int goldMerchantHeadingColor     = 0xFFAA00; // gold
    public int diamondMerchantHeadingColor  = 0x55FFFF; // aqua
    public int emeraldMerchantHeadingColor  = 0x55FF55; // green

    // Bandit Rush settings
    public boolean banditRushEnabled = true;
    public int banditRushHeadingColor = 0xFFAA00; // yellow
    public int banditRushTextColor = 0xFFAAAA; // light red
    public boolean banditRushShowDistance = true;
    public int banditRushTimeoutSeconds = 60; // how long a rush stays on HUD
    public int banditRushBeamOpacity = 160;
    public String banditRushIconItemId = "iron_sword";
    public boolean banditRushSoundEnabled = true;
    public String banditRushSound = "note_pling"; // "anvil", "bell", "xp_orb", "note_pling", "enchant", "level_up", "ender_eye"
    public int banditRushSoundVolume = 100; // 0-200
    public boolean waypointBanditRushEnabled = true;
    public boolean waypointBanditRushEdgeEnabled = true;

    // Waypoint settings
    public boolean waypointsEnabled = true;
    public boolean waypointMeteorsEnabled = true;
    public boolean waypointMerchantsEnabled = true;
    public boolean waypointCustomEnabled = true;
    public boolean waypointMeteorsEdgeEnabled = true;
    public boolean waypointMerchantsEdgeEnabled = true;
    public boolean waypointCustomEdgeEnabled = true;
    public boolean beaconBeamsEnabled = true;
    public boolean beaconBeamThroughWalls = true;
    public int meteorBeamOpacity   = 160; // 0-255 default beam opacity for meteors
    public int merchantBeamOpacity = 160; // 0-255 default beam opacity for merchants
    // Defaults applied to newly created custom waypoints
    public int   customWaypointDefaultOpacity    = 255;
    public float customWaypointOnScreenScale     = 1.0f;
    public float customWaypointOffScreenScale    = 1.0f;

    // Gang Ping
    public boolean gangPingEnabled = true;
    public int gangPingColor = 0xAA55FF;         // purple
    public int gangPingBaseOpacity = 200;        // 0-255 base alpha before distance fade
    public boolean gangPingBeamEnabled = true;
    public int gangPingBeamOpacity = 120;
    public boolean gangPingEdgeEnabled = false;
    public boolean gangPingSoundEnabled = true;
    public int gangPingSoundVolume = 80;         // 0-200
    public boolean gangPingShowName = true;
    public boolean gangPingShowTimer = false;
    public boolean gangPingShowCoords = true;
    public boolean gangPingShowHp = false;
    public boolean gangPingShowFacing = false;
    public float gangPingTextScale = 1.0f;          // 0.5 - 2.0
    public float gangPingIconMinScale = 0.5f;       // minimum icon scale
    public float gangPingIconMaxScale = 1.5f;       // maximum icon scale (at 75+ blocks)
    public boolean gangPingDistanceScaling = true;   // false = always use min scale
    public boolean trucePingEnabled = true;
    public int trucePingColor = 0x55AAFF;         // light blue
    public boolean gangPingShowNonGang = false;   // show pings not from your gang/truce chat

    // Minimap
    public boolean minimapEnabled       = false;
    public int     minimapX             = 5;
    public int     minimapY             = 5;
    public int     minimapScale         = 100;
    public int     minimapSize          = 128;   // pixel diameter
    public int     minimapPixelsPerBlock = 1;    // zoom (1 = most detail)
    public boolean minimapCircleShape   = true;
    public boolean minimapRotating      = false; // false = north-up
    public boolean minimapShowWaypoints = true;
    public boolean minimapShowCoords    = true;
    public int     minimapBorderColor   = 0xFFFFFF;
    public int     minimapBorderOpacity = 220;
    public int     minimapBorderThickness = 2;

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
                this.eventsHudEnabled = loaded.eventsHudEnabled;
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
                this.eventsHudX = loaded.eventsHudX;
                this.eventsHudY = loaded.eventsHudY;
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

                this.eventsBgColor = loaded.eventsBgColor;
                this.eventsBgOpacity = loaded.eventsBgOpacity;
                this.eventsBorderColor = loaded.eventsBorderColor;
                this.eventsBorderOpacity = loaded.eventsBorderOpacity;
                this.eventsBorderThickness = loaded.eventsBorderThickness;

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
                this.showEventsHudTitle = loaded.showEventsHudTitle;
                this.eventsHudTitleColor = loaded.eventsHudTitleColor;

                // Load Events HUD text color
                this.eventsTextColor = loaded.eventsTextColor;

                // Load Events HUD heading colors
                this.eventsNaturalHeadingColor = loaded.eventsNaturalHeadingColor;
                this.eventsSummonedHeadingColor = loaded.eventsSummonedHeadingColor;

                // Load Events HUD icon
                this.eventsIconItemId = loaded.eventsIconItemId;

                // Load Events HUD crashed display duration
                this.eventsCrashedDisplayDuration = loaded.eventsCrashedDisplayDuration;
                this.meteorShowDistance = loaded.meteorShowDistance;
                this.merchantShowDistance = loaded.merchantShowDistance;

                // Load merchant settings
                this.merchantsEnabled = loaded.merchantsEnabled;
                this.merchantTimeoutMinutes = loaded.merchantTimeoutMinutes;
                this.merchantSlainDisplayDuration = loaded.merchantSlainDisplayDuration;
                this.coalMerchantEnabled = loaded.coalMerchantEnabled;
                this.ironMerchantEnabled = loaded.ironMerchantEnabled;
                this.lapisMerchantEnabled = loaded.lapisMerchantEnabled;
                this.redstoneMerchantEnabled = loaded.redstoneMerchantEnabled;
                this.goldMerchantEnabled = loaded.goldMerchantEnabled;
                this.diamondMerchantEnabled = loaded.diamondMerchantEnabled;
                this.emeraldMerchantEnabled = loaded.emeraldMerchantEnabled;
                this.coalMerchantHeadingColor = loaded.coalMerchantHeadingColor;
                this.ironMerchantHeadingColor = loaded.ironMerchantHeadingColor;
                this.lapisMerchantHeadingColor = loaded.lapisMerchantHeadingColor;
                this.redstoneMerchantHeadingColor = loaded.redstoneMerchantHeadingColor;
                this.goldMerchantHeadingColor = loaded.goldMerchantHeadingColor;
                this.diamondMerchantHeadingColor = loaded.diamondMerchantHeadingColor;
                this.emeraldMerchantHeadingColor = loaded.emeraldMerchantHeadingColor;

                // Load super breaker aura settings
                this.superBreakerBaseColor = loaded.superBreakerBaseColor;
                this.superBreakerBaseOpacity = loaded.superBreakerBaseOpacity;
                this.superBreakerLightColor = loaded.superBreakerLightColor;
                this.superBreakerLightOpacity = loaded.superBreakerLightOpacity;
                this.superBreakerAuraEnabled = loaded.superBreakerAuraEnabled;
                this.superBreakerTimerEnabled = loaded.superBreakerTimerEnabled;
                this.superBreakerTimerOffsetX = loaded.superBreakerTimerOffsetX;
                this.superBreakerTimerOffsetY = loaded.superBreakerTimerOffsetY;

                // Load peaceful mining settings
                this.peacefulMiningOpacity = loaded.peacefulMiningOpacity;
                this.peacefulMiningDistance = loaded.peacefulMiningDistance;
                this.peacefulMiningDisableOnCombat = loaded.peacefulMiningDisableOnCombat;
                this.pickaxeDropConfirmationEnabled = loaded.pickaxeDropConfirmationEnabled;
                this.pickaxeDropBlockEnabled = loaded.pickaxeDropBlockEnabled;
                this.pickaxeDropDragBlockEnabled = loaded.pickaxeDropDragBlockEnabled;

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
                this.easyViewPrestigeTokenEnabled = loaded.easyViewPrestigeTokenEnabled;
                this.easyViewXpBottleEnabled = loaded.easyViewXpBottleEnabled;
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
                this.easyViewPrestigeTokenColor = loaded.easyViewPrestigeTokenColor;
                this.easyViewXpBottleColor = loaded.easyViewXpBottleColor;

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
                this.easyViewPagesTierColor = loaded.easyViewPagesTierColor;
                this.easyViewPrestigeTokenBold = loaded.easyViewPrestigeTokenBold;
                this.easyViewXpBottleBold = loaded.easyViewXpBottleBold;
                this.easyViewXpBottleTierColor = loaded.easyViewXpBottleTierColor;

                // Load Item Cooldowns settings
                this.itemCooldownsEnabled = loaded.itemCooldownsEnabled;
                this.itemCooldownsPetEnabled = loaded.itemCooldownsPetEnabled;
                this.itemCooldownsPetCooldownColor = loaded.itemCooldownsPetCooldownColor;
                this.itemCooldownsPetActiveColor = loaded.itemCooldownsPetActiveColor;
                this.itemCooldownsPetBold = loaded.itemCooldownsPetBold;
                this.itemCooldownsTrinketEnabled = loaded.itemCooldownsTrinketEnabled;
                this.itemCooldownsTrinketColor = loaded.itemCooldownsTrinketColor;
                this.itemCooldownsTrinketBold = loaded.itemCooldownsTrinketBold;
                this.itemCooldownsBanditBoxEnabled = loaded.itemCooldownsBanditBoxEnabled;
                this.itemCooldownsBanditBoxColor = loaded.itemCooldownsBanditBoxColor;
                this.itemCooldownsBanditBoxBold = loaded.itemCooldownsBanditBoxBold;

                // Load HUD scaling settings
                this.cooldownHudScale = loaded.cooldownHudScale;
                this.satchelHudScale = loaded.satchelHudScale;
                this.statsHudScale = loaded.statsHudScale;
                this.enchantHudScale = loaded.enchantHudScale;
                this.eventsHudScale = loaded.eventsHudScale;
                this.superBreakerAuraScale = loaded.superBreakerAuraScale;

                // Load satchel percentage option
                this.satchelShowPercentage = loaded.satchelShowPercentage;

                this.combineSimilarSatchels = loaded.combineSimilarSatchels;

                // Load minimap settings
                this.minimapEnabled        = loaded.minimapEnabled;
                this.minimapX              = loaded.minimapX;
                this.minimapY              = loaded.minimapY;
                this.minimapScale          = loaded.minimapScale;
                this.minimapSize           = loaded.minimapSize;
                this.minimapPixelsPerBlock  = loaded.minimapPixelsPerBlock;
                this.minimapCircleShape    = loaded.minimapCircleShape;
                this.minimapRotating       = loaded.minimapRotating;
                this.minimapShowWaypoints  = loaded.minimapShowWaypoints;
                this.minimapShowCoords     = loaded.minimapShowCoords;
                this.minimapBorderColor    = loaded.minimapBorderColor;
                this.minimapBorderOpacity  = loaded.minimapBorderOpacity;
                this.minimapBorderThickness = loaded.minimapBorderThickness;

                // Load message notifications settings
                this.messageNotifsEnabled = loaded.messageNotifsEnabled;
                this.messageNotifsSound = loaded.messageNotifsSound;
                this.messageNotifsVolume = loaded.messageNotifsVolume;

                // Load waypoint settings
                this.waypointsEnabled = loaded.waypointsEnabled;
                this.waypointMeteorsEnabled = loaded.waypointMeteorsEnabled;
                this.waypointMerchantsEnabled = loaded.waypointMerchantsEnabled;
                this.waypointCustomEnabled = loaded.waypointCustomEnabled;
                this.waypointMeteorsEdgeEnabled = loaded.waypointMeteorsEdgeEnabled;
                this.waypointMerchantsEdgeEnabled = loaded.waypointMerchantsEdgeEnabled;
                this.waypointCustomEdgeEnabled = loaded.waypointCustomEdgeEnabled;
                this.beaconBeamsEnabled = loaded.beaconBeamsEnabled;
                this.beaconBeamThroughWalls = loaded.beaconBeamThroughWalls;
                this.meteorBeamOpacity   = loaded.meteorBeamOpacity;
                this.merchantBeamOpacity = loaded.merchantBeamOpacity;
                this.customWaypointDefaultOpacity = loaded.customWaypointDefaultOpacity;
                this.customWaypointOnScreenScale  = loaded.customWaypointOnScreenScale;
                this.customWaypointOffScreenScale = loaded.customWaypointOffScreenScale;

                // Load gang ping settings
                this.gangPingEnabled = loaded.gangPingEnabled;
                this.gangPingColor = loaded.gangPingColor;
                this.gangPingBaseOpacity = loaded.gangPingBaseOpacity;
                this.gangPingBeamEnabled = loaded.gangPingBeamEnabled;
                this.gangPingBeamOpacity = loaded.gangPingBeamOpacity;
                this.gangPingEdgeEnabled = loaded.gangPingEdgeEnabled;
                this.gangPingSoundEnabled = loaded.gangPingSoundEnabled;
                this.gangPingSoundVolume = loaded.gangPingSoundVolume;
                this.gangPingShowName = loaded.gangPingShowName;
                this.gangPingShowTimer = loaded.gangPingShowTimer;
                this.gangPingShowCoords = loaded.gangPingShowCoords;
                this.gangPingShowHp = loaded.gangPingShowHp;
                this.gangPingShowFacing = loaded.gangPingShowFacing;
                this.gangPingTextScale = loaded.gangPingTextScale;
                this.gangPingIconMinScale = loaded.gangPingIconMinScale;
                this.gangPingIconMaxScale = loaded.gangPingIconMaxScale;
                this.gangPingDistanceScaling = loaded.gangPingDistanceScaling;
                this.trucePingEnabled = loaded.trucePingEnabled;
                this.trucePingColor = loaded.trucePingColor;
                this.gangPingShowNonGang = loaded.gangPingShowNonGang;

                // Bandit Rush
                this.banditRushEnabled = loaded.banditRushEnabled;
                this.banditRushHeadingColor = loaded.banditRushHeadingColor;
                this.banditRushTextColor = loaded.banditRushTextColor;
                this.banditRushShowDistance = loaded.banditRushShowDistance;
                this.banditRushTimeoutSeconds = loaded.banditRushTimeoutSeconds;
                this.banditRushBeamOpacity = loaded.banditRushBeamOpacity;
                this.banditRushIconItemId = loaded.banditRushIconItemId;
                this.banditRushSoundEnabled = loaded.banditRushSoundEnabled;
                this.banditRushSound = loaded.banditRushSound;
                this.banditRushSoundVolume = loaded.banditRushSoundVolume;
                this.waypointBanditRushEnabled = loaded.waypointBanditRushEnabled;
                this.waypointBanditRushEdgeEnabled = loaded.waypointBanditRushEdgeEnabled;
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

