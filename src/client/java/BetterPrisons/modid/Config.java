package BetterPrisons.modid;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

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
    public boolean peacefulMiningEnabled = false;
    public boolean satchelShowPercentage = false;
    public boolean combineSimilarSatchels = true;

    // HUD positions (x, y for each element)
    public int cooldownHudX = 10;
    public int cooldownHudY = 10;
    public int satchelHudX = 10;
    public int satchelHudY = 100;
    public int statsHudX = 10;
    public int statsHudY = 200;
    public int enchantHudX = 10;
    public int enchantHudY = 300;
    public int meteorHudX = 10;
    public int meteorHudY = 400;

    // Colors (as integers)
    public int cooldownBarColor = 0xFF00FF;
    public int satchelBarColor = 0xFF4488;

    // Super Breaker Aura colors and opacity
    public int superBreakerBaseColor = 0x0A88F0; // RGB color (dark blue)
    public int superBreakerBaseOpacity = 79; // 0-255 (79 = 0x4F, ~30% opacity)
    public int superBreakerLightColor = 0x0A88F0; // RGB color (same blue)
    public int superBreakerLightOpacity = 191; // 0-255 (191 = 0xBF, ~75% opacity)
    public boolean superBreakerAuraEnabled = true;

    // Peaceful Mining opacity
    public int peacefulMiningOpacity = 77; // 0-255 (77 = ~30% opacity, matches 0.3f)
    public int peacefulMiningDistance = 8;

    // EasyView settings
    public boolean easyViewEnabled = true;
    public boolean easyViewEnergyEnabled = true;
    public boolean easyViewMoneyEnabled = true;
    public boolean easyViewGangPointsEnabled = true;
    public boolean easyViewBlackScrollEnabled = true;
    public boolean easyViewChargeOrbEnabled = true;
    public int easyViewEnergyColor = 0xFFFFFF;
    public int easyViewMoneyColor = 0x00FF00;
    public int easyViewGangPointsColor = 0xFFAA00;
    public int easyViewBlackScrollColor = 0xFF00FF;
    public int easyViewChargeOrbColor = 0x00FFFF;

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
    public int enchantTimeColor = 0xFFFFFF;

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

    // Stats HUD text colors (RGB, no alpha)
    public int statsCurrentXPColor = 0xFFFFFF;
    public int statsXPPerHourColor = 0xAAAAAA;
    public int statsXPPerMinuteColor = 0xAAAAAA;
    public int statsSessionXPColor = 0xAAAAAA;
    public int statsCurrentCEColor = 0xFFFFFF;
    public int statsCEPerHourColor = 0xAAAAAA;
    public int statsCEPerMinuteColor = 0xAAAAAA;
    public int statsSessionCEColor = 0xAAAAAA;
    public int statsSessionDurationColor = 0x888888;

    // Satchel HUD capacity threshold colors (RGB, no alpha)
    public int satchelColorUnder20 = 0xAA0000;    // Dark red - nearly empty
    public int satchelColor20to60 = 0xFF8800;     // Orange - low
    public int satchelColor60to95 = 0xFFFF00;     // Yellow - medium
    public int satchelColor95Plus = 0x00AA00;     // Green - nearly full

    // Cooldown command configs (enabled and color for each command)
    public boolean homeEnabled = true;
    public int homeColor = 0xFFFFFF;
    public boolean jetEnabled = true;
    public int jetColor = 0xFFFFFF;
    public boolean feedEnabled = true;
    public int feedColor = 0xFFFFFF;
    public boolean fixEnabled = true;
    public int fixColor = 0xFFFFFF;
    public boolean combatEnabled = true;
    public int combatColor = 0xFFFFFF;

    // HUD titles (show/hide and color for each HUD)
    public boolean showCooldownHudTitle = true;
    public int cooldownHudTitleColor = 0xFFFFFF;
    public boolean showSatchelHudTitle = true;
    public int satchelHudTitleColor = 0xFFFFFF;
    public boolean showStatsHudTitle = true;
    public int statsHudTitleColor = 0xFFFFFF;
    public boolean showEnchantHudTitle = true;
    public int enchantHudTitleColor = 0xFFFFFF;
    public boolean showMeteorHudTitle = true;
    public int meteorHudTitleColor = 0xFFFFFF;

    // Meteor HUD text color
    public int meteorTextColor = 0xFFFFFF;

    // Meteor HUD icon
    public String meteorIconItemId = "minecraft:nether_quartz_ore";

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

                // Load Meteor HUD icon
                this.meteorIconItemId = loaded.meteorIconItemId;

                // Load super breaker aura settings
                this.superBreakerBaseColor = loaded.superBreakerBaseColor;
                this.superBreakerBaseOpacity = loaded.superBreakerBaseOpacity;
                this.superBreakerLightColor = loaded.superBreakerLightColor;
                this.superBreakerLightOpacity = loaded.superBreakerLightOpacity;
                this.superBreakerAuraEnabled = loaded.superBreakerAuraEnabled;

                // Load peaceful mining settings
                this.peacefulMiningOpacity = loaded.peacefulMiningOpacity;
                this.peacefulMiningDistance = loaded.peacefulMiningDistance;

                // Load EasyView settings
                this.easyViewEnabled = loaded.easyViewEnabled;
                this.easyViewEnergyEnabled = loaded.easyViewEnergyEnabled;
                this.easyViewMoneyEnabled = loaded.easyViewMoneyEnabled;
                this.easyViewGangPointsEnabled = loaded.easyViewGangPointsEnabled;
                this.easyViewBlackScrollEnabled = loaded.easyViewBlackScrollEnabled;
                this.easyViewChargeOrbEnabled = loaded.easyViewChargeOrbEnabled;
                this.easyViewEnergyColor = loaded.easyViewEnergyColor;
                this.easyViewMoneyColor = loaded.easyViewMoneyColor;
                this.easyViewGangPointsColor = loaded.easyViewGangPointsColor;
                this.easyViewBlackScrollColor = loaded.easyViewBlackScrollColor;
                this.easyViewChargeOrbColor = loaded.easyViewChargeOrbColor;

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

    public Screen createConfigScreen(Screen parent) {
        ConfigBuilder builder = ConfigBuilder.create()
                .setParentScreen(parent)
                .setTitle(Text.literal("BetterPrisons Config"))
                .setSavingRunnable(this::save);

        ConfigEntryBuilder entryBuilder = builder.entryBuilder();

        // HUD Toggles Category
        ConfigCategory toggles = builder.getOrCreateCategory(Text.literal("HUD Toggles"));
        toggles.addEntry(entryBuilder.startBooleanToggle(Text.literal("Cooldown HUD"), cooldownHudEnabled)
                .setDefaultValue(true)
                .setSaveConsumer(val -> {
                    cooldownHudEnabled = val;
                    BetterPrisonsClient.cooldownHud.enabled = val;
                })
                .build());
        toggles.addEntry(entryBuilder.startBooleanToggle(Text.literal("Satchel HUD"), satchelHudEnabled)
                .setDefaultValue(true)
                .setSaveConsumer(val -> {
                    satchelHudEnabled = val;
                    BetterPrisonsClient.satchelHud.enabled = val;
                })
                .build());
        toggles.addEntry(entryBuilder.startBooleanToggle(Text.literal("Stats HUD"), statsHudEnabled)
                .setDefaultValue(true)
                .setSaveConsumer(val -> {
                    statsHudEnabled = val;
                    BetterPrisonsClient.statsHud.enabled = val;
                })
                .build());
        toggles.addEntry(entryBuilder.startBooleanToggle(Text.literal("Enchant HUD"), enchantHudEnabled)
                .setDefaultValue(true)
                .setSaveConsumer(val -> {
                    enchantHudEnabled = val;
                    BetterPrisonsClient.enchantHud.enabled = val;
                })
                .build());
        toggles.addEntry(entryBuilder.startBooleanToggle(Text.literal("Meteor HUD"), meteorHudEnabled)
                .setDefaultValue(true)
                .setSaveConsumer(val -> {
                    meteorHudEnabled = val;
                    BetterPrisonsClient.meteorHud.enabled = val;
                })
                .build());
        toggles.addEntry(entryBuilder.startBooleanToggle(Text.literal("Use Comma Formatting for Stats"), useCommaFormatting)
                .setDefaultValue(false)
                .setTooltip(Text.literal("When enabled, numbers show as '1,234,567'. When disabled, numbers show as '1.2M'"))
                .setSaveConsumer(val -> useCommaFormatting = val)
                .build());

        // Peaceful Mining Category (moved to be first after toggles)
        ConfigCategory peacefulCategory = builder.getOrCreateCategory(Text.literal("Peaceful Mining"));
        peacefulCategory.addEntry(entryBuilder.startBooleanToggle(Text.literal("Enable Peaceful Mining"), peacefulMiningEnabled)
                .setDefaultValue(false)
                .setTooltip(Text.literal("When enabled and holding a pickaxe, other players appear translucent and non-interactable"))
                .setSaveConsumer(val -> peacefulMiningEnabled = val)
                .build());
        peacefulCategory.addEntry(entryBuilder.startIntSlider(Text.literal("Player Opacity"), peacefulMiningOpacity, 0, 255)
                .setDefaultValue(77)
                .setTooltip(Text.literal("Opacity of other players when peaceful mining is active (0 = invisible, 255 = fully visible)"))
                .setSaveConsumer(val -> peacefulMiningOpacity = val)
                .build());
        peacefulCategory.addEntry(entryBuilder.startIntField(Text.literal("Interaction Distance"), peacefulMiningDistance)
                .setDefaultValue(8)
                .setTooltip(Text.literal("Radius of distance that Peaceful Mining affects other players around you"))
                .setSaveConsumer(val -> peacefulMiningDistance = val)
                .build());

        // EasyView Category
        ConfigCategory easyViewCategory = builder.getOrCreateCategory(Text.literal("EasyView"));

        // Master toggle
        easyViewCategory.addEntry(entryBuilder.startBooleanToggle(Text.literal("Enable EasyView"), easyViewEnabled)
                .setDefaultValue(true)
                .setTooltip(Text.literal("Master toggle for all EasyView overlays"))
                .setSaveConsumer(val -> {
                    easyViewEnabled = val;
                    BetterPrisonsClient.easyView.enabled = val;
                })
                .build());

        easyViewCategory.addEntry(entryBuilder.startTextDescription(Text.literal("")).build()); // Spacer

        // Cosmic Energy settings
        easyViewCategory.addEntry(entryBuilder.startBooleanToggle(Text.literal("Show Cosmic Energy"), easyViewEnergyEnabled)
                .setDefaultValue(true)
                .setTooltip(Text.literal("Show compact text for Cosmic Energy items"))
                .setSaveConsumer(val -> easyViewEnergyEnabled = val)
                .build());
        easyViewCategory.addEntry(entryBuilder.startColorField(Text.literal("Cosmic Energy Color"), easyViewEnergyColor)
                .setDefaultValue(0xFFFFFF)
                .setTooltip(Text.literal("Color of Cosmic Energy text overlay"))
                .setSaveConsumer(val -> easyViewEnergyColor = val)
                .build());

        easyViewCategory.addEntry(entryBuilder.startTextDescription(Text.literal("")).build()); // Spacer

        // Money Note settings
        easyViewCategory.addEntry(entryBuilder.startBooleanToggle(Text.literal("Show Money Notes"), easyViewMoneyEnabled)
                .setDefaultValue(true)
                .setTooltip(Text.literal("Show compact text for Money Note items"))
                .setSaveConsumer(val -> easyViewMoneyEnabled = val)
                .build());
        easyViewCategory.addEntry(entryBuilder.startColorField(Text.literal("Money Note Color"), easyViewMoneyColor)
                .setDefaultValue(0x00FF00)
                .setTooltip(Text.literal("Color of Money Note text overlay"))
                .setSaveConsumer(val -> easyViewMoneyColor = val)
                .build());

        easyViewCategory.addEntry(entryBuilder.startTextDescription(Text.literal("")).build()); // Spacer

        // Gang Points settings
        easyViewCategory.addEntry(entryBuilder.startBooleanToggle(Text.literal("Show Gang Points"), easyViewGangPointsEnabled)
                .setDefaultValue(true)
                .setTooltip(Text.literal("Show compact text for Gang Point items"))
                .setSaveConsumer(val -> easyViewGangPointsEnabled = val)
                .build());
        easyViewCategory.addEntry(entryBuilder.startColorField(Text.literal("Gang Points Color"), easyViewGangPointsColor)
                .setDefaultValue(0xFFAA00)
                .setTooltip(Text.literal("Color of Gang Points text overlay"))
                .setSaveConsumer(val -> easyViewGangPointsColor = val)
                .build());

        easyViewCategory.addEntry(entryBuilder.startTextDescription(Text.literal("")).build()); // Spacer

        // Black Scroll settings
        easyViewCategory.addEntry(entryBuilder.startBooleanToggle(Text.literal("Show Black Scrolls"), easyViewBlackScrollEnabled)
                .setDefaultValue(true)
                .setTooltip(Text.literal("Show percentage for Black Scroll items"))
                .setSaveConsumer(val -> easyViewBlackScrollEnabled = val)
                .build());
        easyViewCategory.addEntry(entryBuilder.startColorField(Text.literal("Black Scroll Color"), easyViewBlackScrollColor)
                .setDefaultValue(0xFF00FF)
                .setTooltip(Text.literal("Color of Black Scroll text overlay"))
                .setSaveConsumer(val -> easyViewBlackScrollColor = val)
                .build());

        easyViewCategory.addEntry(entryBuilder.startTextDescription(Text.literal("")).build()); // Spacer

        // Charge Orb settings
        easyViewCategory.addEntry(entryBuilder.startBooleanToggle(Text.literal("Show Charge Orbs"), easyViewChargeOrbEnabled)
                .setDefaultValue(true)
                .setTooltip(Text.literal("Show percentage for Charge Orb items"))
                .setSaveConsumer(val -> easyViewChargeOrbEnabled = val)
                .build());
        easyViewCategory.addEntry(entryBuilder.startColorField(Text.literal("Charge Orb Color"), easyViewChargeOrbColor)
                .setDefaultValue(0x00FFFF)
                .setTooltip(Text.literal("Color of Charge Orb text overlay"))
                .setSaveConsumer(val -> easyViewChargeOrbColor = val)
                .build());

        // Cooldown HUD Category
        ConfigCategory cooldownStyling = builder.getOrCreateCategory(Text.literal("Cooldown HUD"));

        // Title settings
        cooldownStyling.addEntry(entryBuilder.startBooleanToggle(Text.literal("Show Title"), showCooldownHudTitle)
                .setDefaultValue(true)
                .setTooltip(Text.literal("Show 'Cooldown HUD' title above the HUD"))
                .setSaveConsumer(val -> showCooldownHudTitle = val)
                .build());
        cooldownStyling.addEntry(entryBuilder.startColorField(Text.literal("Title Color"), cooldownHudTitleColor)
                .setDefaultValue(0xFFFFFF)
                .setTooltip(Text.literal("Color of the HUD title text"))
                .setSaveConsumer(val -> cooldownHudTitleColor = val)
                .build());
        cooldownStyling.addEntry(entryBuilder.startTextDescription(Text.literal("")).build()); // Spacer

        cooldownStyling.addEntry(entryBuilder.startColorField(Text.literal("Background Color"), cooldownBgColor)
                .setDefaultValue(0x000000)
                .setSaveConsumer(val -> cooldownBgColor = val)
                .build());
        cooldownStyling.addEntry(entryBuilder.startIntSlider(Text.literal("Background Opacity"), cooldownBgOpacity, 0, 255)
                .setDefaultValue(128)
                .setSaveConsumer(val -> cooldownBgOpacity = val)
                .build());
        cooldownStyling.addEntry(entryBuilder.startColorField(Text.literal("Border Color"), cooldownBorderColor)
                .setDefaultValue(0xFFFFFF)
                .setSaveConsumer(val -> cooldownBorderColor = val)
                .build());
        cooldownStyling.addEntry(entryBuilder.startIntSlider(Text.literal("Border Opacity"), cooldownBorderOpacity, 0, 255)
                .setDefaultValue(128)
                .setSaveConsumer(val -> cooldownBorderOpacity = val)
                .build());
        cooldownStyling.addEntry(entryBuilder.startIntField(Text.literal("Border Thickness"), cooldownBorderThickness)
                .setDefaultValue(2)
                .setSaveConsumer(val -> cooldownBorderThickness = val)
                .build());

        // Command Settings
        cooldownStyling.addEntry(entryBuilder.startTextDescription(Text.literal("")).build()); // Spacer
        cooldownStyling.addEntry(entryBuilder.startTextDescription(Text.literal("Command Settings")).build());

        // Home command
        cooldownStyling.addEntry(entryBuilder.startTextDescription(Text.literal("")).build()); // Spacer
        cooldownStyling.addEntry(entryBuilder.startBooleanToggle(Text.literal("Home Enabled"), homeEnabled)
                .setDefaultValue(true)
                .setTooltip(Text.literal("Enable/disable Home cooldown tracking"))
                .setSaveConsumer(val -> homeEnabled = val)
                .build());
        cooldownStyling.addEntry(entryBuilder.startColorField(Text.literal("Home Color"), homeColor)
                .setDefaultValue(0xFFFFFF)
                .setTooltip(Text.literal("Text color for Home cooldown"))
                .setSaveConsumer(val -> homeColor = val)
                .build());

        // Jet command
        cooldownStyling.addEntry(entryBuilder.startTextDescription(Text.literal("")).build()); // Spacer
        cooldownStyling.addEntry(entryBuilder.startBooleanToggle(Text.literal("Jet Enabled"), jetEnabled)
                .setDefaultValue(true)
                .setTooltip(Text.literal("Enable/disable Jet cooldown tracking"))
                .setSaveConsumer(val -> jetEnabled = val)
                .build());
        cooldownStyling.addEntry(entryBuilder.startColorField(Text.literal("Jet Color"), jetColor)
                .setDefaultValue(0xFFFFFF)
                .setTooltip(Text.literal("Text color for Jet cooldown"))
                .setSaveConsumer(val -> jetColor = val)
                .build());

        // Feed command
        cooldownStyling.addEntry(entryBuilder.startTextDescription(Text.literal("")).build()); // Spacer
        cooldownStyling.addEntry(entryBuilder.startBooleanToggle(Text.literal("Feed Enabled"), feedEnabled)
                .setDefaultValue(true)
                .setTooltip(Text.literal("Enable/disable Feed cooldown tracking"))
                .setSaveConsumer(val -> feedEnabled = val)
                .build());
        cooldownStyling.addEntry(entryBuilder.startColorField(Text.literal("Feed Color"), feedColor)
                .setDefaultValue(0xFFFFFF)
                .setTooltip(Text.literal("Text color for Feed cooldown"))
                .setSaveConsumer(val -> feedColor = val)
                .build());

        // Fix command
        cooldownStyling.addEntry(entryBuilder.startTextDescription(Text.literal("")).build()); // Spacer
        cooldownStyling.addEntry(entryBuilder.startBooleanToggle(Text.literal("Fix Enabled"), fixEnabled)
                .setDefaultValue(true)
                .setTooltip(Text.literal("Enable/disable Fix cooldown tracking"))
                .setSaveConsumer(val -> fixEnabled = val)
                .build());
        cooldownStyling.addEntry(entryBuilder.startColorField(Text.literal("Fix Color"), fixColor)
                .setDefaultValue(0xFFFFFF)
                .setTooltip(Text.literal("Text color for Fix cooldown"))
                .setSaveConsumer(val -> fixColor = val)
                .build());

        // Combat command
        cooldownStyling.addEntry(entryBuilder.startTextDescription(Text.literal("")).build()); // Spacer
        cooldownStyling.addEntry(entryBuilder.startBooleanToggle(Text.literal("Combat Enabled"), combatEnabled)
                .setDefaultValue(true)
                .setTooltip(Text.literal("Enable/disable Combat cooldown tracking"))
                .setSaveConsumer(val -> combatEnabled = val)
                .build());
        cooldownStyling.addEntry(entryBuilder.startColorField(Text.literal("Combat Color"), combatColor)
                .setDefaultValue(0xFFFFFF)
                .setTooltip(Text.literal("Text color for Combat cooldown"))
                .setSaveConsumer(val -> combatColor = val)
                .build());

        // Satchel HUD Category
        ConfigCategory satchelStyling = builder.getOrCreateCategory(Text.literal("Satchel HUD"));

        // Title settings
        satchelStyling.addEntry(entryBuilder.startBooleanToggle(Text.literal("Show Title"), showSatchelHudTitle)
                .setDefaultValue(true)
                .setTooltip(Text.literal("Show 'Satchel HUD' title above the HUD"))
                .setSaveConsumer(val -> showSatchelHudTitle = val)
                .build());
        satchelStyling.addEntry(entryBuilder.startColorField(Text.literal("Title Color"), satchelHudTitleColor)
                .setDefaultValue(0xFFFFFF)
                .setTooltip(Text.literal("Color of the HUD title text"))
                .setSaveConsumer(val -> satchelHudTitleColor = val)
                .build());
        satchelStyling.addEntry(entryBuilder.startTextDescription(Text.literal("")).build()); // Spacer

        satchelStyling.addEntry(entryBuilder.startBooleanToggle(Text.literal("Show Percentage"), satchelShowPercentage)
                .setDefaultValue(false)
                .setTooltip(Text.literal("When enabled, satchel capacity is shown as percentage instead of actual values"))
                .setSaveConsumer(val -> satchelShowPercentage = val)
                .build());
        satchelStyling.addEntry(entryBuilder.startTextDescription(Text.literal("")).build()); // Spacer
        satchelStyling.addEntry(entryBuilder.startColorField(Text.literal("Background Color"), satchelBgColor)
                .setDefaultValue(0x000000)
                .setSaveConsumer(val -> satchelBgColor = val)
                .build());
        satchelStyling.addEntry(entryBuilder.startIntSlider(Text.literal("Background Opacity"), satchelBgOpacity, 0, 255)
                .setDefaultValue(128)
                .setSaveConsumer(val -> satchelBgOpacity = val)
                .build());
        satchelStyling.addEntry(entryBuilder.startColorField(Text.literal("Border Color"), satchelBorderColor)
                .setDefaultValue(0xFFFFFF)
                .setSaveConsumer(val -> satchelBorderColor = val)
                .build());
        satchelStyling.addEntry(entryBuilder.startIntSlider(Text.literal("Border Opacity"), satchelBorderOpacity, 0, 255)
                .setDefaultValue(128)
                .setSaveConsumer(val -> satchelBorderOpacity = val)
                .build());
        satchelStyling.addEntry(entryBuilder.startIntField(Text.literal("Border Thickness"), satchelBorderThickness)
                .setDefaultValue(2)
                .setSaveConsumer(val -> satchelBorderThickness = val)
                .build());
        satchelStyling.addEntry(entryBuilder.startBooleanToggle(Text.literal("Combine Similar Satchels"), combineSimilarSatchels)
                .setDefaultValue(true)
                .setTooltip(Text.literal("When enabled, satchels of the same type will be combined into a single entry in the HUD"))
                .setSaveConsumer(val -> combineSimilarSatchels = val)
                .build());

        // Satchel capacity threshold colors
        satchelStyling.addEntry(entryBuilder.startTextDescription(Text.literal("")).build()); // Spacer
        satchelStyling.addEntry(entryBuilder.startTextDescription(Text.literal("Capacity Threshold Colors")).build());
        satchelStyling.addEntry(entryBuilder.startColorField(Text.literal("Under 20% Color"), satchelColorUnder20)
                .setDefaultValue(0xAA0000)
                .setTooltip(Text.literal("Color for satchels under 20% full (dark red)"))
                .setSaveConsumer(val -> satchelColorUnder20 = val)
                .build());
        satchelStyling.addEntry(entryBuilder.startColorField(Text.literal("20-60% Color"), satchelColor20to60)
                .setDefaultValue(0xFF8800)
                .setTooltip(Text.literal("Color for satchels 20-60% full (orange)"))
                .setSaveConsumer(val -> satchelColor20to60 = val)
                .build());
        satchelStyling.addEntry(entryBuilder.startColorField(Text.literal("60-95% Color"), satchelColor60to95)
                .setDefaultValue(0xFFFF00)
                .setTooltip(Text.literal("Color for satchels 60-95% full (yellow)"))
                .setSaveConsumer(val -> satchelColor60to95 = val)
                .build());
        satchelStyling.addEntry(entryBuilder.startColorField(Text.literal("95%+ Color"), satchelColor95Plus)
                .setDefaultValue(0x00AA00)
                .setTooltip(Text.literal("Color for satchels 95% or more full (green)"))
                .setSaveConsumer(val -> satchelColor95Plus = val)
                .build());

        // Stats HUD Category
        ConfigCategory statsStyling = builder.getOrCreateCategory(Text.literal("Stats HUD"));

        // Title settings
        statsStyling.addEntry(entryBuilder.startBooleanToggle(Text.literal("Show Title"), showStatsHudTitle)
                .setDefaultValue(true)
                .setTooltip(Text.literal("Show 'Stats HUD' title above the HUD"))
                .setSaveConsumer(val -> showStatsHudTitle = val)
                .build());
        statsStyling.addEntry(entryBuilder.startColorField(Text.literal("Title Color"), statsHudTitleColor)
                .setDefaultValue(0xFFFFFF)
                .setTooltip(Text.literal("Color of the HUD title text"))
                .setSaveConsumer(val -> statsHudTitleColor = val)
                .build());
        statsStyling.addEntry(entryBuilder.startTextDescription(Text.literal("")).build()); // Spacer

        statsStyling.addEntry(entryBuilder.startColorField(Text.literal("Background Color"), statsBgColor)
                .setDefaultValue(0x000000)
                .setSaveConsumer(val -> statsBgColor = val)
                .build());
        statsStyling.addEntry(entryBuilder.startIntSlider(Text.literal("Background Opacity"), statsBgOpacity, 0, 255)
                .setDefaultValue(128)
                .setSaveConsumer(val -> statsBgOpacity = val)
                .build());
        statsStyling.addEntry(entryBuilder.startColorField(Text.literal("Border Color"), statsBorderColor)
                .setDefaultValue(0xFFFFFF)
                .setSaveConsumer(val -> statsBorderColor = val)
                .build());
        statsStyling.addEntry(entryBuilder.startIntSlider(Text.literal("Border Opacity"), statsBorderOpacity, 0, 255)
                .setDefaultValue(128)
                .setSaveConsumer(val -> statsBorderOpacity = val)
                .build());
        statsStyling.addEntry(entryBuilder.startIntField(Text.literal("Border Thickness"), statsBorderThickness)
                .setDefaultValue(2)
                .setSaveConsumer(val -> statsBorderThickness = val)
                .build());

        // Stats HUD Element Toggles
        statsStyling.addEntry(entryBuilder.startTextDescription(Text.literal("")).build()); // Spacer
        statsStyling.addEntry(entryBuilder.startBooleanToggle(Text.literal("Show Current XP"), statsShowCurrentXP)
                .setDefaultValue(true)
                .setSaveConsumer(val -> statsShowCurrentXP = val)
                .build());
        statsStyling.addEntry(entryBuilder.startBooleanToggle(Text.literal("Show XP/hr"), statsShowXPPerHour)
                .setDefaultValue(true)
                .setSaveConsumer(val -> statsShowXPPerHour = val)
                .build());
        statsStyling.addEntry(entryBuilder.startBooleanToggle(Text.literal("Show XP/min"), statsShowXPPerMinute)
                .setDefaultValue(true)
                .setSaveConsumer(val -> statsShowXPPerMinute = val)
                .build());
        statsStyling.addEntry(entryBuilder.startBooleanToggle(Text.literal("Show Session XP"), statsShowSessionXP)
                .setDefaultValue(true)
                .setSaveConsumer(val -> statsShowSessionXP = val)
                .build());
        statsStyling.addEntry(entryBuilder.startBooleanToggle(Text.literal("Show Current CE"), statsShowCurrentCE)
                .setDefaultValue(true)
                .setSaveConsumer(val -> statsShowCurrentCE = val)
                .build());
        statsStyling.addEntry(entryBuilder.startBooleanToggle(Text.literal("Show CE/hr"), statsShowCEPerHour)
                .setDefaultValue(true)
                .setSaveConsumer(val -> statsShowCEPerHour = val)
                .build());
        statsStyling.addEntry(entryBuilder.startBooleanToggle(Text.literal("Show CE/min"), statsShowCEPerMinute)
                .setDefaultValue(true)
                .setSaveConsumer(val -> statsShowCEPerMinute = val)
                .build());
        statsStyling.addEntry(entryBuilder.startBooleanToggle(Text.literal("Show Session CE"), statsShowSessionCE)
                .setDefaultValue(true)
                .setSaveConsumer(val -> statsShowSessionCE = val)
                .build());
        statsStyling.addEntry(entryBuilder.startBooleanToggle(Text.literal("Show Session Duration"), statsShowSessionDuration)
                .setDefaultValue(true)
                .setSaveConsumer(val -> statsShowSessionDuration = val)
                .build());
        statsStyling.addEntry(entryBuilder.startBooleanToggle(Text.literal("Show Milliseconds on Session Duration"), statsShowMillisOnSessionDuration)
                .setDefaultValue(false)
                .setSaveConsumer(val -> statsShowMillisOnSessionDuration = val)
                .build());

        // Text Color Fields
        statsStyling.addEntry(entryBuilder.startTextDescription(Text.literal("")).build()); // Spacer
        statsStyling.addEntry(entryBuilder.startTextDescription(Text.literal("Text Colors")).build());
        statsStyling.addEntry(entryBuilder.startColorField(Text.literal("Current XP Color"), statsCurrentXPColor)
                .setDefaultValue(0xFFFFFF)
                .setSaveConsumer(val -> statsCurrentXPColor = val)
                .build());
        statsStyling.addEntry(entryBuilder.startColorField(Text.literal("XP/hr Color"), statsXPPerHourColor)
                .setDefaultValue(0xAAAAAA)
                .setSaveConsumer(val -> statsXPPerHourColor = val)
                .build());
        statsStyling.addEntry(entryBuilder.startColorField(Text.literal("XP/min Color"), statsXPPerMinuteColor)
                .setDefaultValue(0xAAAAAA)
                .setSaveConsumer(val -> statsXPPerMinuteColor = val)
                .build());
        statsStyling.addEntry(entryBuilder.startColorField(Text.literal("Session XP Color"), statsSessionXPColor)
                .setDefaultValue(0xAAAAAA)
                .setSaveConsumer(val -> statsSessionXPColor = val)
                .build());
        statsStyling.addEntry(entryBuilder.startColorField(Text.literal("Current CE Color"), statsCurrentCEColor)
                .setDefaultValue(0xFFFFFF)
                .setSaveConsumer(val -> statsCurrentCEColor = val)
                .build());
        statsStyling.addEntry(entryBuilder.startColorField(Text.literal("CE/hr Color"), statsCEPerHourColor)
                .setDefaultValue(0xAAAAAA)
                .setSaveConsumer(val -> statsCEPerHourColor = val)
                .build());
        statsStyling.addEntry(entryBuilder.startColorField(Text.literal("CE/min Color"), statsCEPerMinuteColor)
                .setDefaultValue(0xAAAAAA)
                .setSaveConsumer(val -> statsCEPerMinuteColor = val)
                .build());
        statsStyling.addEntry(entryBuilder.startColorField(Text.literal("Session CE Color"), statsSessionCEColor)
                .setDefaultValue(0xAAAAAA)
                .setSaveConsumer(val -> statsSessionCEColor = val)
                .build());
        statsStyling.addEntry(entryBuilder.startColorField(Text.literal("Session Duration Color"), statsSessionDurationColor)
                .setDefaultValue(0x888888)
                .setSaveConsumer(val -> statsSessionDurationColor = val)
                .build());

        // Enchant HUD Category
        ConfigCategory enchantStyling = builder.getOrCreateCategory(Text.literal("Enchant HUD"));

        // Title settings
        enchantStyling.addEntry(entryBuilder.startBooleanToggle(Text.literal("Show Title"), showEnchantHudTitle)
                .setDefaultValue(true)
                .setTooltip(Text.literal("Show 'Enchant HUD' title above the HUD"))
                .setSaveConsumer(val -> showEnchantHudTitle = val)
                .build());
        enchantStyling.addEntry(entryBuilder.startColorField(Text.literal("Title Color"), enchantHudTitleColor)
                .setDefaultValue(0xFFFFFF)
                .setTooltip(Text.literal("Color of the HUD title text"))
                .setSaveConsumer(val -> enchantHudTitleColor = val)
                .build());
        enchantStyling.addEntry(entryBuilder.startTextDescription(Text.literal("")).build()); // Spacer

        enchantStyling.addEntry(entryBuilder.startColorField(Text.literal("Background Color"), enchantBgColor)
                .setDefaultValue(0x000000)
                .setSaveConsumer(val -> enchantBgColor = val)
                .build());
        enchantStyling.addEntry(entryBuilder.startIntSlider(Text.literal("Background Opacity"), enchantBgOpacity, 0, 255)
                .setDefaultValue(128)
                .setSaveConsumer(val -> enchantBgOpacity = val)
                .build());
        enchantStyling.addEntry(entryBuilder.startColorField(Text.literal("Border Color"), enchantBorderColor)
                .setDefaultValue(0xFFFFFF)
                .setSaveConsumer(val -> enchantBorderColor = val)
                .build());
        enchantStyling.addEntry(entryBuilder.startIntSlider(Text.literal("Border Opacity"), enchantBorderOpacity, 0, 255)
                .setDefaultValue(128)
                .setSaveConsumer(val -> enchantBorderOpacity = val)
                .build());
        enchantStyling.addEntry(entryBuilder.startIntField(Text.literal("Border Thickness"), enchantBorderThickness)
                .setDefaultValue(2)
                .setSaveConsumer(val -> enchantBorderThickness = val)
                .build());
        enchantStyling.addEntry(entryBuilder.startColorField(Text.literal("Timer Color"), enchantTimeColor)
                .setDefaultValue(0xFFFFFF)
                .setSaveConsumer(val -> enchantTimeColor = val)
                .build());

        // Super Breaker Aura Section (within Enchant HUD)

        enchantStyling.addEntry(entryBuilder.startTextDescription(Text.literal("")).build()); // Spacer
        enchantStyling.addEntry(entryBuilder.startTextDescription(Text.literal("Super Breaker Aura Settings")).build());
        enchantStyling.addEntry(entryBuilder.startBooleanToggle(Text.literal("Enable Super Breaker Aura"), superBreakerAuraEnabled)
                .setDefaultValue(true)
                .setTooltip(Text.literal("When enabled an aura effect plays on screen while Super Breaker is active"))
                .setSaveConsumer(val -> superBreakerAuraEnabled = val)
                .build());
        enchantStyling.addEntry(entryBuilder.startColorField(Text.literal("Aura Base Color"), superBreakerBaseColor)
                .setDefaultValue(0x0A88F0)
                .setTooltip(Text.literal("Color of the darker/base aura ring"))
                .setSaveConsumer(val -> superBreakerBaseColor = val)
                .build());
        enchantStyling.addEntry(entryBuilder.startIntSlider(Text.literal("Aura Base Opacity"), superBreakerBaseOpacity, 0, 255)
                .setDefaultValue(79)
                .setTooltip(Text.literal("Opacity of the darker/base aura ring (0 = transparent, 255 = opaque)"))
                .setSaveConsumer(val -> superBreakerBaseOpacity = val)
                .build());
        enchantStyling.addEntry(entryBuilder.startColorField(Text.literal("Aura Light Color"), superBreakerLightColor)
                .setDefaultValue(0x0A88F0)
                .setTooltip(Text.literal("Color of the brighter/progress aura ring"))
                .setSaveConsumer(val -> superBreakerLightColor = val)
                .build());
        enchantStyling.addEntry(entryBuilder.startIntSlider(Text.literal("Aura Light Opacity"), superBreakerLightOpacity, 0, 255)
                .setDefaultValue(191)
                .setTooltip(Text.literal("Opacity of the brighter/progress aura ring (0 = transparent, 255 = opaque)"))
                .setSaveConsumer(val -> superBreakerLightOpacity = val)
                .build());

        // Meteor HUD Category
        ConfigCategory meteorStyling = builder.getOrCreateCategory(Text.literal("Meteor HUD"));

        // Title settings
        meteorStyling.addEntry(entryBuilder.startBooleanToggle(Text.literal("Show Title"), showMeteorHudTitle)
                .setDefaultValue(true)
                .setTooltip(Text.literal("Show 'Meteor HUD' title above the HUD"))
                .setSaveConsumer(val -> showMeteorHudTitle = val)
                .build());
        meteorStyling.addEntry(entryBuilder.startColorField(Text.literal("Title Color"), meteorHudTitleColor)
                .setDefaultValue(0xFFFFFF)
                .setTooltip(Text.literal("Color of the HUD title text"))
                .setSaveConsumer(val -> meteorHudTitleColor = val)
                .build());
        meteorStyling.addEntry(entryBuilder.startTextDescription(Text.literal("")).build()); // Spacer

        meteorStyling.addEntry(entryBuilder.startColorField(Text.literal("Background Color"), meteorBgColor)
                .setDefaultValue(0x000000)
                .setSaveConsumer(val -> meteorBgColor = val)
                .build());
        meteorStyling.addEntry(entryBuilder.startIntSlider(Text.literal("Background Opacity"), meteorBgOpacity, 0, 255)
                .setDefaultValue(128)
                .setSaveConsumer(val -> meteorBgOpacity = val)
                .build());
        meteorStyling.addEntry(entryBuilder.startColorField(Text.literal("Border Color"), meteorBorderColor)
                .setDefaultValue(0xFFFFFF)
                .setSaveConsumer(val -> meteorBorderColor = val)
                .build());
        meteorStyling.addEntry(entryBuilder.startIntSlider(Text.literal("Border Opacity"), meteorBorderOpacity, 0, 255)
                .setDefaultValue(128)
                .setSaveConsumer(val -> meteorBorderOpacity = val)
                .build());
        meteorStyling.addEntry(entryBuilder.startIntField(Text.literal("Border Thickness"), meteorBorderThickness)
                .setDefaultValue(2)
                .setSaveConsumer(val -> meteorBorderThickness = val)
                .build());

        // Text color
        meteorStyling.addEntry(entryBuilder.startTextDescription(Text.literal("")).build()); // Spacer
        meteorStyling.addEntry(entryBuilder.startColorField(Text.literal("Text Color"), meteorTextColor)
                .setDefaultValue(0xFFFFFF)
                .setTooltip(Text.literal("Color of meteor coordinate text"))
                .setSaveConsumer(val -> meteorTextColor = val)
                .build());

        // Icon settings
        meteorStyling.addEntry(entryBuilder.startTextDescription(Text.literal("")).build()); // Spacer
        meteorStyling.addEntry(entryBuilder.startStrField(Text.literal("Meteor Icon"), meteorIconItemId)
                .setDefaultValue("minecraft:nether_quartz_ore")
                .setTooltip(Text.literal("Item ID for the meteor icon (e.g., minecraft:nether_quartz_ore)"))
                .setSaveConsumer(val -> meteorIconItemId = val)
                .build());

        // HUD Scaling Category
        ConfigCategory scalingCategory = builder.getOrCreateCategory(Text.literal("HUD Scaling"));
        scalingCategory.addEntry(entryBuilder.startIntSlider(Text.literal("Cooldown HUD Scale"), cooldownHudScale, 70, 150)
                .setDefaultValue(100)
                .setMin(70)
                .setMax(150)
                .setTooltip(Text.literal("% Scale multiplier for Cooldown HUD (70% to 150%)"))
                .setSaveConsumer(val -> cooldownHudScale = val)
                .build());
        scalingCategory.addEntry(entryBuilder.startIntSlider(Text.literal("Satchel HUD Scale"), satchelHudScale, 70, 150)
                .setDefaultValue(100)
                .setMin(70)
                .setMax(150)
                .setTooltip(Text.literal("% Scale multiplier for Satchel HUD (70% to 150%)"))
                .setSaveConsumer(val -> satchelHudScale = val)
                .build());
        scalingCategory.addEntry(entryBuilder.startIntSlider(Text.literal("Stats HUD Scale"), statsHudScale, 70, 150)
                .setDefaultValue(100)
                .setMin(70)
                .setMax(150)
                .setTooltip(Text.literal("% Scale multiplier for Stats HUD (70% to 150%)"))
                .setSaveConsumer(val -> statsHudScale = val)
                .build());
        scalingCategory.addEntry(entryBuilder.startIntSlider(Text.literal("Enchant HUD Scale"), enchantHudScale, 70, 150)
                .setDefaultValue(100)
                .setMin(70)
                .setMax(150)
                .setTooltip(Text.literal("% Scale multiplier for Enchant HUD (70% to 150%)"))
                .setSaveConsumer(val -> enchantHudScale = val)
                .build());
        scalingCategory.addEntry(entryBuilder.startIntSlider(Text.literal("Meteor HUD Scale"), meteorHudScale, 70, 150)
                .setDefaultValue(100)
                .setMin(70)
                .setMax(150)
                .setTooltip(Text.literal("% Scale multiplier for Meteor HUD (70% to 150%)"))
                .setSaveConsumer(val -> meteorHudScale = val)
                .build());
        scalingCategory.addEntry(entryBuilder.startIntSlider(Text.literal("Super Breaker Aura Scale"), superBreakerAuraScale, 70, 150)
                .setDefaultValue(100)
                .setMin(70)
                .setMax(150)
                .setTooltip(Text.literal("% Scale multiplier for Super Breaker Aura (70% to 150%)"))
                .setSaveConsumer(val -> superBreakerAuraScale = val)
                .build());

        Screen configScreen = builder.build();

        // Wrap the config screen to add HUD editor button
        return new ConfigScreenWrapper(configScreen, parent);
    }

    // Wrapper class to add custom button to config screen
    private static class ConfigScreenWrapper extends Screen {
        private final Screen wrappedScreen;
        private final Screen parent;
        private ButtonWidget hudEditorButton;

        protected ConfigScreenWrapper(Screen wrappedScreen, Screen parent) {
            super(wrappedScreen.getTitle());
            this.wrappedScreen = wrappedScreen;
            this.parent = parent;
        }

        @Override
        protected void init() {
            super.init();

            // Initialize wrapped screen
            if (wrappedScreen != null) {
                wrappedScreen.init(this.client, this.width, this.height);
            }

            // Add HUD editor button
            hudEditorButton = this.addDrawableChild(ButtonWidget.builder(
                    Text.literal("Edit HUD Positions"),
                    button -> {
                        MinecraftClient.getInstance().setScreen(new HudEditorScreen(this));
                    }
            ).dimensions(this.width - (100 + 12), this.height - 27, 100, 20).build());
        }

        @Override
        public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            // Render wrapped screen (this includes background)
            if (wrappedScreen != null) {
                wrappedScreen.render(context, mouseX, mouseY, delta);
            }

            // Render our custom button
            if (hudEditorButton != null) {
                hudEditorButton.render(context, mouseX, mouseY, delta);
            }
        }

        @Override
        public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
            // Don't render background - let wrapped screen handle it
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (super.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
            return wrappedScreen != null && wrappedScreen.mouseClicked(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseReleased(double mouseX, double mouseY, int button) {
            if (super.mouseReleased(mouseX, mouseY, button)) {
                return true;
            }
            return wrappedScreen != null && wrappedScreen.mouseReleased(mouseX, mouseY, button);
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
            if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) {
                return true;
            }
            return wrappedScreen != null && wrappedScreen.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
            if (super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount)) {
                return true;
            }
            return wrappedScreen != null && wrappedScreen.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            if (super.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
            return wrappedScreen != null && wrappedScreen.keyPressed(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
            if (super.keyReleased(keyCode, scanCode, modifiers)) {
                return true;
            }
            return wrappedScreen != null && wrappedScreen.keyReleased(keyCode, scanCode, modifiers);
        }

        @Override
        public boolean charTyped(char chr, int modifiers) {
            if (super.charTyped(chr, modifiers)) {
                return true;
            }
            return wrappedScreen != null && wrappedScreen.charTyped(chr, modifiers);
        }

        @Override
        public void close() {
            if (this.client != null) {
                this.client.setScreen(parent);
            }
        }
    }
}
